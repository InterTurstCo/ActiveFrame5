package ru.intertrust.cm.core.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.business.api.util.MD5Utils;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.dao.access.*;
import ru.intertrust.cm.core.dao.api.*;
import ru.intertrust.cm.core.dao.api.extension.*;
import ru.intertrust.cm.core.dao.exception.DaoException;
import ru.intertrust.cm.core.dao.exception.InvalidIdException;
import ru.intertrust.cm.core.dao.exception.OptimisticLockException;
import ru.intertrust.cm.core.dao.impl.access.AccessControlUtility;
import ru.intertrust.cm.core.dao.impl.extension.AfterCommitExtensionPointService;
import ru.intertrust.cm.core.dao.impl.utils.*;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.core.model.ObjectNotFoundException;

import java.util.*;


import static ru.intertrust.cm.core.business.api.dto.GenericDomainObject.STATUS_DO;
import static ru.intertrust.cm.core.business.api.dto.GenericDomainObject.STATUS_FIELD_NAME;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.*;
import static ru.intertrust.cm.core.dao.impl.utils.DaoUtils.*;
import static ru.intertrust.cm.core.dao.impl.utils.DateUtils.getGMTDate;

/**
 * Класс реализации работы с доменным объектом
 * @author atsvetkov
 * 
 */
public class DomainObjectDaoImpl implements DomainObjectDao {

    private static final Logger logger = LoggerFactory.getLogger(DomainObjectDaoImpl.class);

    private static final String PARAM_DOMAIN_OBJECT_ID = "domain_object_id";
    private static final String PARAM_DOMAIN_OBJECT_TYPE_ID = "domain_object_typeid";
    private static final String RESULT_TYPE_ID = "result_type_id";
    private static final int BATCH_SIZE = 1000;

    @Autowired
    @Qualifier("jdbcTemplate")
    private JdbcOperations masterJdbcOperations;

    @Autowired
    @Qualifier("masterNamedParameterJdbcTemplate")
    private NamedParameterJdbcOperations masterJdbcTemplate;

    @Autowired
    @Qualifier("switchableNamedParameterJdbcTemplate")
    private NamedParameterJdbcOperations switchableJdbcTemplate;

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private DomainEntitiesCloner cloner;

    @Autowired
    private IdGenerator idGenerator;

    private DomainObjectCacheService domainObjectCacheService;

    @Autowired
    private GlobalCacheClient globalCacheClient;

    @Autowired
    private GlobalCacheManager globalCacheManager;

    @Autowired
    private DomainObjectTypeIdCache domainObjectTypeIdCache;

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private ExtensionService extensionService;

    @Autowired
    private DynamicGroupService dynamicGroupService;

    @Autowired
    private PermissionServiceDao permissionService;

    @Autowired
    private CurrentUserAccessor currentUserAccessor;

    @Autowired
    private UserTransactionService userTransactionService;

    @Autowired
    private EventLogService eventLogService;

    @Autowired
    private DomainObjectQueryHelper domainObjectQueryHelper;

    @Autowired
    private AfterCommitExtensionPointService afterCommitExtensionPointService;

    @Autowired
    private UserGroupGlobalCache userGroupCache;

    @Autowired
    public void setDomainObjectCacheService(
            DomainObjectCacheServiceImpl domainObjectCacheService) {
        this.domainObjectCacheService = domainObjectCacheService;
    }

    public void setPermissionService(PermissionServiceDao permissionService) {
        this.permissionService = permissionService;
    }

    public void setMasterJdbcTemplate(NamedParameterJdbcOperations masterJdbcTemplate) {
        this.masterJdbcTemplate = masterJdbcTemplate;
    }

    public void setDomainObjectQueryHelper(DomainObjectQueryHelper domainObjectQueryHelper) {
        this.domainObjectQueryHelper = domainObjectQueryHelper;
    }

    /**
     * Устанавливает генератор для создания уникальных идентифиткаторово
     * 
     * @param idGenerator
     */
    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    /**
     * Устанавливает {@link #configurationExplorer}
     * 
     * @param configurationExplorer
     *            {@link #configurationExplorer}
     */
    public void setConfigurationExplorer(
            ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    public void setDomainObjectTypeIdCache(
            DomainObjectTypeIdCache domainObjectTypeIdCache) {
        this.domainObjectTypeIdCache = domainObjectTypeIdCache;
    }

    public void setAccessControlService(
            AccessControlService accessControlService) {
        this.accessControlService = accessControlService;
    }

    public void setDynamicGroupService(DynamicGroupService dynamicGroupService) {
        this.dynamicGroupService = dynamicGroupService;
    }

    public void setExtensionService(ExtensionService extensionService) {
        this.extensionService = extensionService;
    }

    @Override
    public DomainObject setStatus(Id objectId, Id status, AccessToken accessToken) {
        checkIfAuditLog(new Id[] {objectId });

        String domainObjectType = domainObjectTypeIdCache.getName(objectId);
        if (configurationExplorer.isAuditLogType(domainObjectType)) {
            throw new FatalException("It is not allowed to change Audit Log using CRUD service, table: " + domainObjectType);
        }

        accessControlService.verifySystemAccessToken(accessToken);
        DomainObject domainObject = find(objectId, accessToken);
        ((GenericDomainObject) domainObject).setStatus(status);
        DomainObjectModification[] domainObjectModifications = new DomainObjectModification[1];
        domainObjectModifications[0] = getModifiedFieldsAndValidate(domainObject);

        Set<Id> beforeSaveInvalicContexts = dynamicGroupService.getInvalidGroupsBeforeChange(domainObject, domainObjectModifications[0].getFieldModifications());

        GenericDomainObject result = update(new DomainObject[] {domainObject }, accessToken, true, domainObjectModifications)[0];
        domainObjectCacheService.putOnUpdate(result, accessToken);
        globalCacheClient.notifyUpdate(result, accessToken, false);

        permissionService.notifyDomainObjectChangeStatus(domainObject);
        dynamicGroupService.notifyDomainObjectChanged(domainObject, domainObjectModifications[0].getFieldModifications(), beforeSaveInvalicContexts);

        // Добавляем слушателя комита транзакции, чтобы вызвать точки расширения
        // после транзакции
        // Это ОБЯЗАТЕЛЬНО должно предшествовать вызову точек расширения, чтобы
        // в слушателе отразились корректные состояния доменных объектов
        // (так как точки расширения могут менять состояние сохраняемого
        // доменного объекта)
        DomainObjectActionListener listener = getTransactionListener();
        listener.addChangeStatusDomainObject(result);

        // Вызов точки расширения после смены статуса
        String[] parentTypes = configurationExplorer.getDomainObjectTypesHierarchyBeginningFromType(domainObject.getTypeName());
        for (String typeName : parentTypes) {
            extensionService.getExtentionPoint(AfterChangeStatusExtentionHandler.class, typeName).onAfterChangeStatus(domainObject);
        }
        // вызываем обработчики с неуказанным фильтром
        extensionService.getExtentionPoint(AfterChangeStatusExtentionHandler.class, "").onAfterChangeStatus(domainObject);

        return result;
    }

    @Override
    public DomainObject create(DomainObject domainObject, AccessToken accessToken) {
        DomainObject[] domainObjects = createMany(new DomainObject[] {domainObject }, accessToken);
        return domainObjects[0];
    }

    private DomainObject[] createMany(DomainObject[] domainObjects, AccessToken accessToken) {
        checkIfAuditLog(domainObjects);

        String initialStatus = getInitialStatus(domainObjects[0]);

        DomainObject createdObjects[] = create(domainObjects,
                domainObjectTypeIdCache.getId(domainObjects[0].getTypeName()), accessToken, initialStatus);

        List<Id> domainObjectIds = convertToIds(createdObjects);
        // Добавляем временные права на чтение для новых объектов
        permissionService.grantNewObjectPermissions(domainObjectIds);

        for (DomainObject createdObject : createdObjects) {
            domainObjectCacheService.putOnUpdate(createdObject, accessToken);
            globalCacheClient.notifyCreate(createdObject, accessToken);
            refreshDynamiGroupsAndAclForCreate(createdObject);

            // Добавляем слушателя комита транзакции, чтобы вызвать точки
            // расширения после транзакции
            DomainObjectActionListener listener = getTransactionListener();
            listener.addCreatedDomainObject(createdObject);
        }

        return createdObjects;
    }

    private List<Id> convertToIds(DomainObject[] createdObjects) {
        List<Id> idsList = new ArrayList<>(createdObjects.length);

        for (DomainObject domainObject : createdObjects) {
            idsList.add(domainObject.getId());

        }
        return idsList;
    }

    private DomainObjectActionListener getTransactionListener() {
        DomainObjectActionListener listener = userTransactionService.getListener(DomainObjectActionListener.class);
        if (listener == null) {
            listener = new DomainObjectActionListener(userTransactionService.getTransactionId());
            userTransactionService.addListener(listener);
            userTransactionService.addListener(new CacheCommitNotifier(listener.domainObjectsModification));
        }
        return listener;
    }

    private String getInitialStatus(DomainObject domainObject) {
        DomainObjectTypeConfig domainObjectTypeConfig = configurationExplorer
                .getConfig(DomainObjectTypeConfig.class,
                        domainObject.getTypeName());

        String initialStatus = null;
        if (domainObjectTypeConfig != null) {
            initialStatus = domainObjectTypeConfig.getInitialStatus();

            if (initialStatus == null) {
                initialStatus = getParentInitialStatus(domainObjectTypeConfig);
            }
        }
        return initialStatus;
    }

    private String getParentInitialStatus(DomainObjectTypeConfig domainObjectTypeConfig) {

        String parentObjectType = domainObjectTypeConfig.getExtendsAttribute();

        if (parentObjectType != null) {

            DomainObjectTypeConfig parentObjectTypeConfig = configurationExplorer
                    .getConfig(DomainObjectTypeConfig.class,
                            parentObjectType);

            if (parentObjectTypeConfig.getInitialStatus() != null) {
                return parentObjectTypeConfig.getInitialStatus();
            } else {
                return getParentInitialStatus(parentObjectTypeConfig);

            }
        }
        return null;
    }

    private void refreshDynamiGroupsAndAclForCreate(DomainObject createdObject) {
        dynamicGroupService.notifyDomainObjectCreated(createdObject);
        permissionService.notifyDomainObjectCreated(createdObject);
    }

    @Override
    public DomainObject save(DomainObject domainObject, AccessToken accessToken)
            throws InvalidIdException, ObjectNotFoundException,
            OptimisticLockException {
        String domainObjectType = domainObject.getTypeName();

        DomainObject[] result = saveMany(new DomainObject[] {domainObject }, accessToken);
        return result[0];
    }

    private DomainObject[] saveMany(DomainObject[] domainObjects, AccessToken accessToken)
            throws InvalidIdException, ObjectNotFoundException,
            OptimisticLockException {

        // Трассировка сохранения со стеком вызова. Нужна для поиска
        // OptimisticLockException
        if (logger.isTraceEnabled()) {
            String message = "Save domain objects:\n";
            for (int i = 0; i < domainObjects.length; i++) {
                message += "DomainObject-" + i + ": " + domainObjects[i].toString();
            }
            message += "\nCall stack:\n";
            StackTraceElement[] stackElements = Thread.currentThread().getStackTrace();
            // Начинать надо с первого, так как нулевой это метод
            // getStackTrace()
            for (int i = 1; i < stackElements.length; i++) {
                StackTraceElement stackTraceElement = stackElements[i];
                message += "\t" + stackTraceElement.toString() + "\n";
            }

            logger.trace(message);
        }

        checkIfAuditLog(domainObjects);
        DomainObject result[] = null;

        // Получение измененных полей
        DomainObjectModification[] domainObjectModifications = new DomainObjectModification[domainObjects.length];

        for (int i = 0; i < domainObjects.length; i++) {
            domainObjectModifications[i] = getModifiedFieldsAndValidate(domainObjects[i]);
        }

        // Вызов точки расширения до сохранения
        String[] parentTypes = configurationExplorer.getDomainObjectTypesHierarchyBeginningFromType(domainObjects[0].getTypeName());
        for (int i = 0; i < domainObjects.length; i++) {
            DomainObject domainObject = domainObjects[i];
            List<FieldModification> fieldsModification = domainObjectModifications[i].getFieldModifications();
            for (String typeName : parentTypes) {
                extensionService.getExtentionPoint(BeforeSaveExtensionHandler.class, typeName).onBeforeSave(domainObject, fieldsModification);
            }
            // вызываем обработчики с неуказанным фильтром
            extensionService.getExtentionPoint(BeforeSaveExtensionHandler.class, "").onBeforeSave(domainObject, fieldsModification);
        }

        DomainObjectVersion.AuditLogOperation operation = null;

        // Сохранение в базе
        if (domainObjects[0].isNew()) {
            result = createMany(domainObjects, accessToken);
            operation = DomainObjectVersion.AuditLogOperation.CREATE;
        } else {

            Set<Id>[] beforeChangeInvalidGroups = getBeforeChangeInvalidGroups(domainObjects, domainObjectModifications);
            result = update(domainObjects, accessToken, domainObjectModifications);
            refreshDynamicGroupsAndAclForUpdate(domainObjects, domainObjectModifications, beforeChangeInvalidGroups);

            operation = DomainObjectVersion.AuditLogOperation.UPDATE;
        }

        for (int i = 0; i < result.length; i++) {
            String auditLogTableName = DataStructureNamingHelper.getALTableSqlName(domainObjects[i].getTypeName());
            Integer auditLogType = domainObjectTypeIdCache.getId(auditLogTableName);

            DomainObject domainObject = result[i];
            // Запись в auditLog
            createAuditLog(domainObject, domainObject.getTypeName(), auditLogType, accessToken, operation);

            // Добавляем слушателя комита транзакции, чтобы вызвать точки
            // расширения после транзакции.
            // Это ОБЯЗАТЕЛЬНО должно предшествовать вызову точек расширения,
            // чтобы в слушателе отразились корректные состояния доменных
            // объектов
            List<FieldModification> doChangedFields = domainObjectModifications[i].getFieldModifications();
            DomainObjectActionListener listener = getTransactionListener();
            listener.addSavedDomainObject(domainObject, doChangedFields);

            // Вызов точки расширения после сохранения
            for (String typeName : parentTypes) {
                extensionService.getExtentionPoint(AfterSaveExtensionHandler.class, typeName).onAfterSave(domainObject, doChangedFields);
            }
            extensionService.getExtentionPoint(AfterSaveExtensionHandler.class, "").onAfterSave(domainObject, doChangedFields);

        }

        return result;
    }

    private Set<Id>[] getBeforeChangeInvalidGroups(DomainObject[] domainObjects,DomainObjectModification[] domainObjectModifications) {
        Set<Id> beforeChangeInvalidGroups[] = new HashSet[domainObjects.length];

        for (int i = 0; i < domainObjects.length; i++) {
            beforeChangeInvalidGroups[i] = dynamicGroupService.getInvalidGroupsBeforeChange(domainObjects[i], domainObjectModifications[i].getFieldModifications());
        }
        return beforeChangeInvalidGroups;
    }

    private void
            refreshDynamicGroupsAndAclForUpdate(DomainObject[] domainObjects, DomainObjectModification[] domainObjectModifications, Set<Id>[] beforeChangeInvalidGroups) {
        for (int i = 0; i < domainObjects.length; i++) {
            refreshDynamiGroupsAndAclForUpdate(domainObjects[i], domainObjectModifications[i].getFieldModifications(), beforeChangeInvalidGroups[i]);
        }
    }

    /**
     * Проверяет, являются ли переданные объекты аудит логом. Так как передается
     * массив однотипных объектов, то проверяется тип первого объекта. Если
     * передан аудит лог объект - выбрасывается исключение.
     * @param domainObjects
     */
    private void checkIfAuditLog(DomainObject[] domainObjects) {
        if (domainObjects[0] != null && configurationExplorer.isAuditLogType(domainObjects[0].getTypeName())) {
            throw new FatalException("It is not allowed to modify Audit Log using CRUD service, table: " + domainObjects[0].getTypeName());
        }
    }

    @Override
    public List<DomainObject> save(List<DomainObject> domainObjects, AccessToken accessToken) {

        List<DomainObject> result = new ArrayList<>(domainObjects.size());

        List<List<DomainObject>> groupObjectsByType = groupObjectsByType(domainObjects);
        for (List<DomainObject> groupObjects : groupObjectsByType) {
            DomainObject[] newDomainObjects = saveMany(
                    groupObjects.toArray(new DomainObject[groupObjects.size()]), accessToken);
            result.addAll(Arrays.asList(newDomainObjects));
        }
        return result;

    }

    private DomainObject[]
            update(DomainObject[] domainObjects, AccessToken accessToken, DomainObjectModification[] domainObjectModifications)
                    throws InvalidIdException, ObjectNotFoundException,
                    OptimisticLockException {

        for (DomainObject domainObject : domainObjects) {
            accessControlService.verifyAccessToken(accessToken, domainObject.getId(), DomainObjectAccessType.WRITE);
        }

        boolean isUpdateStatus = false;

        GenericDomainObject[] updatedObjects = update(domainObjects, accessToken, isUpdateStatus, domainObjectModifications);

        for (int i=0; i<updatedObjects.length; i++) {
            domainObjectCacheService.putOnUpdate(updatedObjects[i], accessToken);
            globalCacheClient.notifyUpdate(updatedObjects[i], accessToken, domainObjectModifications[i].isStampChanged());
        }

        return updatedObjects;

    }

    private GenericDomainObject[] update(DomainObject[] domainObjects, AccessToken accessToken, boolean isUpdateStatus,
                                         DomainObjectModification[] domainObjectModifications) {

        GenericDomainObject[] updatedObjects = new GenericDomainObject[domainObjects.length];
        for (int i = 0; i < domainObjects.length; i++) {
            updatedObjects[i] = new GenericDomainObject(domainObjects[i]);
        }

        DomainObjectTypeConfig domainObjectTypeConfig = configurationExplorer
                .getConfig(DomainObjectTypeConfig.class,
                        updatedObjects[0].getTypeName());

        for (GenericDomainObject updatedObject : updatedObjects) {
            validateIdType(updatedObject.getId());
        }

        Set<Id> beforeChangeInvalidGroups[] = new HashSet[domainObjects.length];

        for (int i = 0; i < domainObjects.length; i++) {
            beforeChangeInvalidGroups[i] = dynamicGroupService.getInvalidGroupsBeforeChange(domainObjects[i], domainObjectModifications[i].getFieldModifications());
        }

        DomainObject[] parentDOs =
                updateParentDO(domainObjectTypeConfig, domainObjects, accessToken, isUpdateStatus, domainObjectModifications);

        Query query = generateUpdateQuery(domainObjectTypeConfig, isUpdateStatus);

        Date[] newModifiedDates = new Date[domainObjects.length];
        Date now = new Date();
        // В случае если сохранялся родительский объект то берем дату
        // модификации из нее, иначе в базе и возвращаемом доменном объекте
        // будут различные даты изменения и изменение объект отвалится по ошибке
        // OptimisticLockException
        if (parentDOs != null) {
            for (int i = 0; i < updatedObjects.length; i++) {
                newModifiedDates[i] = parentDOs[i].getModifiedDate();
            }
        } else { // root DO
            for (int i = 0; i < updatedObjects.length; i++) {
                GenericDomainObject updatedObject = updatedObjects[i];
                final Date modifiedDate = updatedObject.getModifiedDate();
                if (now.compareTo(modifiedDate) > 0) {
                    newModifiedDates[i] = now;
                } else {
                    newModifiedDates[i] = new Date(modifiedDate.getTime() + 1);
                }
            }
        }

        if (query != null) {
            List<Map<String, Object>> parameters = new ArrayList<>(domainObjects.length);
            for (int i = 0; i < updatedObjects.length; i++) {
                parameters.add(initializeUpdateParameters(
                        updatedObjects[i], domainObjectTypeConfig, accessToken, newModifiedDates[i], isUpdateStatus));
            }

            BatchPreparedStatementSetter batchPreparedStatementSetter =
                    new BatchPreparedStatementSetter(query);
            int[][] count = masterJdbcOperations.batchUpdate(query.getQuery(), parameters, BATCH_SIZE, batchPreparedStatementSetter);

            int n = 0;
            for (int j = 0; j < count.length; j++) {
                for (int i = 0; i < count[j].length; i++) {
                    if (count[j][i] == 0 && (!exists(updatedObjects[n].getId()))) {
                        throw new ObjectNotFoundException(updatedObjects[n].getId());
                    }

                    if (!isDerived(domainObjectTypeConfig)) {
                        if (count[j][i] == 0) {
                            HashSet<Id> toInvalidate = new HashSet<>();
                            for (GenericDomainObject updatedObject : updatedObjects) {
                                toInvalidate.add(updatedObject.getId());
                            }
                            globalCacheClient.invalidateCurrentNode(new CacheInvalidation(toInvalidate, false));
                            throw new OptimisticLockException(updatedObjects[n]);
                        }
                    }

                    n += count[j][i];
                }
            }
        }

        final Id currentUser = getCurrentUser(accessToken);
        for (int i = 0; i < updatedObjects.length; i++) {

            updatedObjects[i].setModifiedDate(newModifiedDates[i]);
            updatedObjects[i].setModifiedBy(currentUser);
            updatedObjects[i].resetDirty();

            if (isUpdateStatus) {
                Id statusValue = updatedObjects[i].getReference(STATUS_FIELD_NAME);
                if (statusValue != null) {
                    updatedObjects[i].setReference("status", statusValue);
                }

            }
        }

        return updatedObjects;
    }

    private void refreshDynamiGroupsAndAclForUpdate(DomainObject domainObject, List<FieldModification> modifiedFields,
            Set<Id> beforeChangeInvalicContexts) {
        dynamicGroupService.notifyDomainObjectChanged(domainObject, modifiedFields, beforeChangeInvalicContexts);
        permissionService.notifyDomainObjectChanged(domainObject, modifiedFields);
    }

    private DomainObjectModification getModifiedFieldsAndValidate(DomainObject domainObject) {
        // Для нового объекта все поля отличные от null попадают в список
        // измененных
        final String domainObjectTypeName = domainObject.getTypeName();
        final ArrayList<String> fields = domainObject.getFields();
        DomainObjectModification result = new DomainObjectModification();
        if (domainObject.isNew()) {
            for (String fieldName : fields) {
                final FieldConfig fieldConfig = configurationExplorer.getFieldConfig(domainObjectTypeName, fieldName);
                if (fieldConfig == null) {
                    final String msg = "Trying to save non-existing field. Type: " + domainObjectTypeName + ", field: " + fieldName;
                    logger.warn(msg);
                    logger.debug(msg, new DaoException());
                }
                Value<?> newValue = domainObject.getValue(fieldName);
                if (newValue != null && newValue.get() != null) {
                    result.getFieldModifications().add(new FieldModificationImpl(fieldName, null, newValue));
                }
            }
        } else {
            // для ранее созданного объекта получаем объект не сохраненный из
            // хранилища и вычисляем измененные поля
            AccessToken accessToken = createSystemAccessToken();
            DomainObject originalDomainObject = find(domainObject.getId(),
                    accessToken);

            for (String fieldName : fields) {
                final FieldConfig fieldConfig = configurationExplorer.getFieldConfig(domainObjectTypeName, fieldName);
                if (fieldConfig == null) {
                    final String msg = "Trying to save non-existing field. Type: " + domainObjectTypeName + ", field: " + fieldName;
                    logger.warn(msg);
                    logger.debug(msg, new DaoException());
                } else {
                    Value originalValue = originalDomainObject.getValue(fieldName);
                    Value newValue = domainObject.getValue(fieldName);
                    if (isValueChanged(originalValue, newValue)) {
                        if (fieldConfig.isImmutable()) {
                            throw new FatalException("Trying to modify immutable field. Type: " + domainObjectTypeName + ", field: " + fieldName
                                    + ", original value: " + originalValue + ", new value: " + newValue);
                        }
                        result.getFieldModifications().add(new FieldModificationImpl(fieldName,
                                originalValue, newValue));

                        if (fieldName.equalsIgnoreCase(SECURITY_STAMP_COLUMN)){
                            result.setStampChanged(true);
                        }
                    }
                }
            }
        }
        return result;
    }

    private boolean isValueChanged(Value originalValue, Value newValue) {
        final boolean originalIsEmpty = originalValue == null || originalValue.get() == null;
        final boolean newIsEmpty = newValue == null || newValue.get() == null;
        if (originalIsEmpty && newIsEmpty) {
            return false;
        }
        if (!newIsEmpty && originalIsEmpty || !originalIsEmpty && newIsEmpty) {
            return true;
        }
        return !originalValue.equals(newValue);
    }

    @Override
    public void delete(Id id, AccessToken accessToken) throws InvalidIdException,
            ObjectNotFoundException {
        String domainObjectType = domainObjectTypeIdCache.getName(id);
        if (configurationExplorer.isAuditLogType(domainObjectType)) {
            throw new FatalException("It is not allowed to delete Audit Log using CRUD service, table: " + domainObjectType);
        }
        deleteMany(new Id[] {id }, accessToken, false);
    }

    private int deleteMany(Id[] ids, AccessToken accessToken, boolean ignoreObjectNotFound) throws InvalidIdException,
            ObjectNotFoundException {

        checkIfAuditLog(ids);

        for (Id id : ids) {
            validateIdType(id);
            accessControlService.verifyAccessToken(accessToken, id, DomainObjectAccessType.DELETE);
        }

        final RdbmsId firstRdbmsId = (RdbmsId) ids[0];
        final DomainObjectTypeConfig domainObjectTypeConfig = configurationExplorer
                .getConfig(DomainObjectTypeConfig.class, getDOTypeName(firstRdbmsId));
        final String[] parentTypes = configurationExplorer.getDomainObjectTypesHierarchyBeginningFromType(domainObjectTypeConfig.getName());

        // Получаем удаляемый доменный объект для вызова точек расширения и
        // пересчета динамических групп. Чтение объекта
        // идет от имени системы, т.к. прав на чтение может не быть у
        // пользователя.
        final AccessToken systemAccessToken = createSystemAccessToken();

        DomainObject[] deletedObjects = new DomainObject[ids.length];
        int i = 0;
        for (Id id : ids) {
            DomainObject deletedObject = find(id, systemAccessToken);
            deletedObjects[i++] = deletedObject;
            // Прверка наличия доменного объекта
            if (deletedObject == null) {
                // Если взведен флаг игнорировать отсутствие ДО то пропускаем
                // идентификатор, иначе бросаем исключение
                if (ignoreObjectNotFound) {
                    continue;
                } else {
                    throw new ObjectNotFoundException(id);
                }
            }
            Set<Id> beforeChangeInvalidGroups = dynamicGroupService.getInvalidGroupsBeforeDelete(deletedObject);

            // Точка расширения до удаления
            for (String typeName : parentTypes) {
                extensionService.getExtentionPoint(BeforeDeleteExtensionHandler.class, typeName).onBeforeDelete(deletedObject);
            }
            // вызваем обработчики с неуказанным фильтром
            extensionService.getExtentionPoint(BeforeDeleteExtensionHandler.class, "").onBeforeDelete(deletedObject);

            // Пересчет прав непосредственно перед удалением объекта из базы,
            // чтобы не нарушать целостность данных
            refreshDynamiGroupsAndAclForDelete(deletedObject, beforeChangeInvalidGroups);
        }

        // непосредственно удаление из базыы
        int deleted = internalDelete(ids, ignoreObjectNotFound);

        // Удалене из кэша
        for (Id id : ids) {
            domainObjectCacheService.evict(id);
            globalCacheClient.notifyDelete(id);
        }
        
        // Трассировка сохранения со стеком вызова. Нужна для поиска
        // ObjectNotFoundException
        if (logger.isTraceEnabled()) {
            String message = "Delete domain objects:\n";
            for (int q = 0; q < deletedObjects.length; q++) {
                message += "DomainObject-" + q + ": " + deletedObjects[q].toString();
            }
            message += "\nCall stack:\n";
            StackTraceElement[] stackElements = Thread.currentThread().getStackTrace();
            // Начинать надо с первого, так как нулевой это метод
            // getStackTrace()
            for (int q = 1; q < stackElements.length; q++) {
                StackTraceElement stackTraceElement = stackElements[q];
                message += "\t" + stackTraceElement.toString() + "\n";
            }

            logger.trace(message);
        }

        // Пишем в аудит лог
        for (DomainObject deletedObject : deletedObjects) {
            if (deletedObject == null) {
                continue;
            }
            String auditLogTableName = DataStructureNamingHelper.getALTableSqlName(deletedObject.getTypeName());
            Integer auditLogType = domainObjectTypeIdCache.getId(auditLogTableName);

            createAuditLog(deletedObject, deletedObject.getTypeName(), auditLogType, accessToken, DomainObjectVersion.AuditLogOperation.DELETE);
        }

        // Точка расширения после удаления, вызывается с установкой фильтра
        // текущего типа и всех наследников
        for (DomainObject deletedObject : deletedObjects) {
            if (deletedObject == null) {
                continue;
            }

            // Добавляем слушателя коммита транзакции, чтобы вызвать точки
            // расширения после транзакции
            DomainObjectActionListener listener = getTransactionListener();
            listener.addDeletedDomainObject(deletedObject);

            for (String typeName : parentTypes) {
                extensionService.getExtentionPoint(AfterDeleteExtensionHandler.class, typeName).onAfterDelete(deletedObject);
            }
            extensionService.getExtentionPoint(AfterDeleteExtensionHandler.class, "").onAfterDelete(deletedObject);
        }

        return deleted;
    }

    /**
     * Проверяет, являются ли переданные объекты аудит логом. Так как передается
     * массив однотипных объектов, топроверяется тип первого объекта. Если
     * передан аудит лог объект - выбрасывается исключение.
     * @param ids
     */
    private void checkIfAuditLog(Id[] ids) {
        if (ids[0] != null) {
            String domainObjectType = domainObjectTypeIdCache.getName(ids[0]);
            if (configurationExplorer.isAuditLogType(domainObjectType)) {
                throw new FatalException("It is not allowed to modify Audit Log using CRUD service, table: " + domainObjectType);
            }
        }
    }

    /**
     * Удаление объекта из базяы
     * @param deletedIds
     */
    private int internalDelete(Id[] deletedIds, boolean ignoreObjectNotFound) {
        RdbmsId rdbmsId = (RdbmsId) deletedIds[0];

        DomainObjectTypeConfig domainObjectTypeConfig = configurationExplorer
                .getConfig(DomainObjectTypeConfig.class,
                        getDOTypeName(rdbmsId));

        String query = generateDeleteQuery(domainObjectTypeConfig);

        Map<String, Object>[] parameters = new Map[deletedIds.length];
        int i = 0;
        for (Id deletedId : deletedIds) {
            parameters[i++] = domainObjectQueryHelper.initializeParameters(deletedId);
        }

        int[] deletedObjects = masterJdbcTemplate.batchUpdate(query, parameters);

        int count = 0;
        for (int deletedObject : deletedObjects) {
            count += deletedObject;
        }

        if (count < deletedIds.length) {
            if (!ignoreObjectNotFound) {
                throw new ObjectNotFoundException(rdbmsId);
            }
        }

        // Удаление родительского объекта, перенесено ниже удаления дочернего
        // объекта для того чтобы не ругались foreign key
        Id[] parentIds = new Id[deletedIds.length];
        for (int j = 0; j < parentIds.length; j++) {
            Id parentId = getParentId((RdbmsId) deletedIds[j], domainObjectTypeConfig);
            if (parentId == null)
                return count;
            parentIds[j] = parentId;
        }

        internalDelete(parentIds, ignoreObjectNotFound);

        return count;
    }

    private void refreshDynamiGroupsAndAclForDelete(DomainObject deletedObject, Set<Id> beforeChangeInvalicContexts) {
        if (deletedObject != null) {
            dynamicGroupService.notifyDomainObjectDeleted(deletedObject, beforeChangeInvalicContexts);
            permissionService.notifyDomainObjectDeleted(deletedObject);
        }
    }

    @Override
    public int delete(List<Id> ids, AccessToken accessToken) {
        // TODO как обрабатывать ошибки при удалении каждого доменного
        // объекта...

        List<List<Id>> idsByTypes = groupIdsByType(ids);

        int count = 0;
        for (List<Id> idsByType : idsByTypes) {
            try {
                count += deleteMany(idsByType.toArray(new Id[idsByType.size()]), accessToken, true);
            } catch (ObjectNotFoundException e) {
                // ничего не делаем пока
            }
        }
        return count;
    }

    /**
     * группирует ID по типам в порядке следования
     * @param ids
     * @return
     */
    protected List<List<Id>> groupIdsByType(List<Id> ids) {
        List<List<Id>> result = new ArrayList<>();

        int prevType = 0;
        List<Id> oneTypeList = null;
        for (Id id : ids) {
            try {
                validateIdType(id);
                RdbmsId rdbmsId = (RdbmsId) id;
                int typeId = rdbmsId.getTypeId();

                if (typeId != prevType || oneTypeList == null) {
                    oneTypeList = new ArrayList<>();
                    result.add(oneTypeList);
                }

                oneTypeList.add(id);
                prevType = typeId;

            } catch (InvalidIdException e) {
            }
        }

        return result;
    }

    /**
     * группирует доменные объекты по типам и типу операции (create/update) в
     * порядке следования
     * @param objects
     * @return
     */
    protected List<List<DomainObject>> groupObjectsByType(List<DomainObject> objects) {
        List<List<DomainObject>> result = new ArrayList<>();

        String prevTypeName = null;
        boolean prevIsNew = false;
        List<DomainObject> oneTypeList = null;

        for (DomainObject object : objects) {
            try {
                String typeName = object.getTypeName();
                boolean isNew = object.isNew();

                if (oneTypeList == null || !typeName.equals(prevTypeName) || isNew != prevIsNew) {
                    oneTypeList = new ArrayList<>();
                    result.add(oneTypeList);
                }

                oneTypeList.add(object);
                prevTypeName = typeName;
                prevIsNew = isNew;

            } catch (InvalidIdException e) {
            }
        }

        return result;
    }

    @Override
    public boolean exists(Id id) throws InvalidIdException {
        AccessToken systemAccessToken = createSystemAccessToken();

        if (domainObjectCacheService.get(id, systemAccessToken) != null) {
            return true;
        }
        DomainObject existingObject = globalCacheClient.getDomainObject(id, systemAccessToken);
        validateCachedById(id, systemAccessToken, existingObject);
        if (GenericDomainObject.isAbsent(existingObject)) {
            return false;
        } else if (existingObject != null) {
            return true;
        }

        RdbmsId rdbmsId = (RdbmsId) id;
        validateIdType(id);

        Map<String, Object> parameters = initializeExistsParameters(id);
        long total = switchableJdbcTemplate.queryForObject(generateExistsQuery(getDOTypeName(rdbmsId)), parameters,
                Long.class);

        return total > 0;
    }

    @Override
    public DomainObject find(Id id, AccessToken accessToken) {
        if (id == null) {
            throw new IllegalArgumentException("Object id can not be null");
        }

        accessControlService.verifyAccessToken(accessToken, id, DomainObjectAccessType.READ);

        DomainObject domainObject = domainObjectCacheService.get(id, accessToken);
        if (domainObject != null) {
            return domainObject;
        }
        return findInStorage(id, accessToken, false);
    }

    @Override
    public DomainObject findAndLock(Id id, AccessToken accessToken) {
        if (id == null) {
            throw new IllegalArgumentException("Object id can not be null");
        }

        accessControlService.verifyAccessToken(accessToken, id, DomainObjectAccessType.WRITE);
        return findInStorage(id, accessToken, true);
    }

    private DomainObject findInStorage(Id id, AccessToken accessToken, boolean lock) {
        DomainObject result = null;
        if (!lock) {
            result = globalCacheClient.getDomainObject(id, accessToken);
            validateCachedById(id, accessToken, result);
        }
        if (result == null) {
            result = findInDbById(id, accessToken, lock);
        }
        if (GenericDomainObject.isAbsent(result)) {
            result = null;
        }
        if (result != null) {
            domainObjectCacheService.putOnRead(result, accessToken);
            eventLogService.logAccessDomainObjectEvent(id, EventLogService.ACCESS_OBJECT_READ, true);
        } else if (eventLogService.isAccessDomainObjectEventEnabled(id, EventLogService.ACCESS_OBJECT_READ, false)) {
            if (exists(id)) {
                eventLogService.logAccessDomainObjectEvent(id, EventLogService.ACCESS_OBJECT_READ, false);
            }
        }
        return result;
    }

    private DomainObject findInDbById(Id id, AccessToken accessToken, boolean lock) {
        RdbmsId rdbmsId = (RdbmsId) id;
        String typeName = getDOTypeName(rdbmsId);
        String query = domainObjectQueryHelper.generateFindQuery(typeName, accessToken, lock);
        Map<String, Object> parameters = domainObjectQueryHelper.initializeParameters(rdbmsId, accessToken);
        DomainObject result = masterJdbcTemplate.query(query, parameters, new SingleObjectRowMapper(typeName, configurationExplorer, domainObjectTypeIdCache));
        globalCacheClient.notifyRead(id, result, accessToken);
        return result;
    }

    @Override
    public List<DomainObject> findAll(String domainObjectType, AccessToken accessToken) {
        return findAll(domainObjectType, false, 0, 0, accessToken);
    }

    @Override
    public List<DomainObject> findAll(String domainObjectType, int offset, int limit, AccessToken accessToken) {
        return findAll(domainObjectType, false, offset, limit, accessToken);
    }

    @Override
    public List<DomainObject> findAll(String domainObjectType, boolean exactType, AccessToken accessToken) {
        return findAll(domainObjectType, exactType, 0, 0, accessToken);
    }

    @Override
    public List<DomainObject> findAll(String domainObjectType, boolean exactType, int offset, int limit,
            AccessToken accessToken) {
        if (domainObjectType == null || domainObjectType.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Domain Object type can not be null or empty");
        }

        if (ConfigurationExplorer.REFERENCE_TYPE_ANY.equals(domainObjectType)) {
            throw new IllegalArgumentException(
                    "'*' is not a valid Domain Object type");
        }

        String[] cacheKey = new String[] {domainObjectType, String.valueOf(exactType),
                String.valueOf(offset), String.valueOf(limit) };
        List<DomainObject> result = domainObjectCacheService.getAll(accessToken, cacheKey);
        if (result != null) {
            return result;
        }

        if (offset == 0 && limit == 0) {
            result = globalCacheClient.getAllDomainObjects(domainObjectType, exactType, accessToken);
            validateCachedAllObjects(domainObjectType, exactType, accessToken, result);
        }
        if (result == null) {
            result = findAllObjectsInDB(domainObjectType, exactType, offset, limit, accessToken);

            domainObjectCacheService.putAllOnRead(result, accessToken, cacheKey);
            globalCacheClient.notifyReadAll(domainObjectType, exactType, result, accessToken);
        }

        eventLogService.logAccessDomainObjectEventByDo(result, EventLogService.ACCESS_OBJECT_READ, true);

        return result;
    }

    private List<DomainObject> findAllObjectsInDB(String domainObjectType, boolean exactType, int offset, int limit, AccessToken accessToken) {
        String query = generateFindAllQuery(domainObjectType, exactType, offset, limit, accessToken);

        Map<String, Object> parameters = domainObjectQueryHelper.initializeParameters(accessToken);
        if (exactType) {
            parameters.put(RESULT_TYPE_ID, domainObjectTypeIdCache.getId(domainObjectType));
        }

        return switchableJdbcTemplate.query(query, parameters,
                new MultipleObjectRowMapper(domainObjectType,
                        configurationExplorer, domainObjectTypeIdCache));
    }

    private AccessToken createSystemAccessToken() {
        return accessControlService.createSystemAccessToken("DomainObjectDaoImpl");
    }

    @Override
    public List<DomainObject> find(List<Id> ids, AccessToken accessToken) {
        if (ids == null || ids.size() == 0) {
            return new ArrayList<>(0);
        }
        List<DomainObject> allDomainObjects = new ArrayList<>(ids.size());

        IdSorterByType idSorterByType = new IdSorterByType(
                ids.toArray(new RdbmsId[ids.size()]));

        for (final Integer domainObjectType : idSorterByType
                .getDomainObjectTypeIds()) {
            List<Id> idsOfSingleType = idSorterByType.getIdsOfType(domainObjectType);
            String doTypeName = domainObjectTypeIdCache
                    .getName(domainObjectType);
            allDomainObjects.addAll(findDomainObjects(doTypeName,
                    idsOfSingleType, accessToken, doTypeName));
        }

        eventLogService.logAccessDomainObjectEventByDo(allDomainObjects, EventLogService.ACCESS_OBJECT_READ, true);

        return idSorterByType.restoreOriginalOrder(allDomainObjects);
    }

    /**
     * Поиск доменных объектов одного типа, учитывая наследование.
     * 
     * @param ids
     *            идентификаторы доменных объектов
     * @param accessToken
     *            маркер доступа
     * @param domainObjectType
     *            тип доменного объекта
     * @return список доменных объектов
     */
    private List<DomainObject> findDomainObjects(String typeName, List<Id> ids,
            AccessToken accessToken, String domainObjectType) {
        List<DomainObject> cachedDomainObjects = domainObjectCacheService
                .getAll(ids, accessToken);
        if (cachedDomainObjects != null
                && cachedDomainObjects.size() == ids.size()) {
            return cachedDomainObjects;
        }

        LinkedHashSet<Id> idsToRead = new LinkedHashSet<>(ids);
        if (cachedDomainObjects != null) {
            for (DomainObject domainObject : cachedDomainObjects) {
                idsToRead.remove(domainObject.getId());
            }
        }
        ArrayList<DomainObject> cachedRestOfObjects = globalCacheClient.getDomainObjects(idsToRead, accessToken);
        validateCachedList(idsToRead, accessToken, cachedRestOfObjects);
        if (cachedRestOfObjects != null) {
            int nullObjects = 0;
            if (cachedDomainObjects == null) {
                cachedDomainObjects = new ArrayList<>(cachedRestOfObjects.size());
            }
            for (DomainObject obj : cachedRestOfObjects) {
                if (GenericDomainObject.isAbsent(obj)) {
                    ++nullObjects;
                } else if (obj != null) {
                    domainObjectCacheService.putOnRead(obj, accessToken);
                    cachedDomainObjects.add(obj);
                }
            }
            if (cachedDomainObjects.size() + nullObjects == ids.size()) {
                return cachedDomainObjects;
            }
            for (DomainObject domainObject : cachedRestOfObjects) {
                if (domainObject != null) {
                    idsToRead.remove(domainObject.getId());
                }
            }
        }
        List<DomainObject> readDomainObjects;
        if (!idsToRead.isEmpty()) {
            String tableAlias = getSqlAlias(typeName);
            String query = domainObjectQueryHelper.generateMultiObjectFindQuery(typeName, accessToken, false);
            Map<String, Object> parameters = domainObjectQueryHelper.initializeParameters(new ArrayList<>(idsToRead), accessToken);
            readDomainObjects = switchableJdbcTemplate.query(query, parameters,
                    new MultipleObjectRowMapper(domainObjectType, configurationExplorer, domainObjectTypeIdCache));
            domainObjectCacheService.putAllOnRead(readDomainObjects, accessToken);
            globalCacheClient.notifyRead(idsToRead, readDomainObjects, accessToken);
        } else {
            readDomainObjects = Collections.emptyList();
        }

        if (cachedDomainObjects == null) {
            return readDomainObjects;
        } else {
            cachedDomainObjects.addAll(readDomainObjects);
            return cachedDomainObjects;
        }

    }

    @Override
    public List<DomainObject> findLinkedDomainObjects(Id domainObjectId,
            String linkedType, String linkedField, AccessToken accessToken) {
        return findLinkedDomainObjects(domainObjectId, linkedType, linkedField, false,
                0, 0, accessToken);
    }

    @Override
    public List<DomainObject> findLinkedDomainObjects(Id domainObjectId,
            String linkedType, String linkedField, int offset, int limit,
            AccessToken accessToken) {
        return findLinkedDomainObjects(domainObjectId, linkedType, linkedField, false, offset, limit, accessToken);
    }

    @Override
    public List<DomainObject> findLinkedDomainObjects(Id domainObjectId,
            String linkedType, String linkedField, boolean exactType, AccessToken accessToken) {
        return findLinkedDomainObjects(domainObjectId, linkedType, linkedField, exactType,
                0, 0, accessToken);
    }

    @Override
    public List<DomainObject> findLinkedDomainObjects(Id domainObjectId,
            String linkedType, String linkedField, boolean exactType, int offset, int limit,
            AccessToken accessToken) {
        // Кэш используется только, когда не используется пэйджинг
        boolean linkedDomainObjectCacheEnabled = limit == 0 && offset == 0;

        if (linkedDomainObjectCacheEnabled) {
            String[] cacheKey = new String[] {linkedType, linkedField, String.valueOf(exactType) };
            List<DomainObject> domainObjects =
                    domainObjectCacheService.getAll(domainObjectId, accessToken, cacheKey);

            if (domainObjects != null) {
                return domainObjects;
            }

            domainObjects = globalCacheClient.getLinkedDomainObjects(domainObjectId, linkedType, linkedField, exactType, accessToken);
            validateCachedLinkedObjects(domainObjectId, linkedType, linkedField, exactType, 0, 0, accessToken, domainObjects);
            if (domainObjects != null) {
                domainObjectCacheService.putAllOnRead(domainObjectId, domainObjects, accessToken, cacheKey);
                eventLogService.logAccessDomainObjectEventByDo(domainObjects, EventLogService.ACCESS_OBJECT_READ, true);
                return domainObjects;
            }
        }

        Map<String, Object> parameters = domainObjectQueryHelper.initializeParameters(accessToken);
        parameters.put(PARAM_DOMAIN_OBJECT_ID, ((RdbmsId) domainObjectId).getId());
        parameters.put(PARAM_DOMAIN_OBJECT_TYPE_ID, ((RdbmsId) domainObjectId).getTypeId());
        if (exactType) {
            parameters.put(RESULT_TYPE_ID, domainObjectTypeIdCache.getId(linkedType));
        }

        //String query = buildFindChildrenQuery(linkedType, linkedField, exactType, offset, limit, accessToken);
        final Pair<List<DomainObject>, Long> queryResult = findLinkedDomainObjectsInDB(domainObjectId, linkedType, linkedField, exactType, offset, limit,
                accessToken);
        List<DomainObject> domainObjects = queryResult.getFirst();

        if (linkedDomainObjectCacheEnabled) {
            String[] cacheKey = new String[] {linkedType, linkedField, String.valueOf(exactType)};
            domainObjectCacheService.putAllOnRead(domainObjectId, domainObjects, accessToken, cacheKey);
            globalCacheClient.notifyLinkedObjectsRead(domainObjectId, linkedType, linkedField, exactType, domainObjects, queryResult.getSecond(), accessToken);
        } else { // putAllOnRead adds all objects to the cache
            for (DomainObject domainObject : domainObjects) {
                domainObjectCacheService.putOnRead(domainObject, accessToken);
            }
            globalCacheClient.notifyRead(domainObjects, accessToken);
        }

        eventLogService.logAccessDomainObjectEventByDo(domainObjects, EventLogService.ACCESS_OBJECT_READ, true);

        return domainObjects;
    }

    private Pair<List<DomainObject>, Long> findLinkedDomainObjectsInDB(Id domainObjectId,
            String linkedType, String linkedField, boolean exactType, int offset, int limit,
            AccessToken accessToken) {
        Map<String, Object> parameters = domainObjectQueryHelper.initializeParameters(accessToken);
        parameters.put(PARAM_DOMAIN_OBJECT_ID, ((RdbmsId) domainObjectId).getId());
        parameters.put(PARAM_DOMAIN_OBJECT_TYPE_ID, ((RdbmsId) domainObjectId).getTypeId());
        if (exactType) {
            parameters.put(RESULT_TYPE_ID, domainObjectTypeIdCache.getId(linkedType));
        }

        String query = buildFindChildrenQuery(linkedType, linkedField, exactType, offset, limit, accessToken);
        long time = System.currentTimeMillis(); // time, sql request is sent to
                                                // DB
        List<DomainObject> domainObjects = switchableJdbcTemplate.query(query, parameters,
                new MultipleObjectRowMapper(linkedType, configurationExplorer, domainObjectTypeIdCache));

        if (domainObjects == null) {
            domainObjects = new ArrayList<>(0);
        }

        return new Pair<>(domainObjects, time);
    }

    @Override
    public List<Id> findLinkedDomainObjectsIds(Id domainObjectId,
            String linkedType, String linkedField, AccessToken accessToken) {
        return findLinkedDomainObjectsIds(domainObjectId, linkedType, linkedField, false, 0, 0, accessToken);
    }

    @Override
    public List<Id> findLinkedDomainObjectsIds(Id domainObjectId,
            String linkedType, String linkedField, int offset, int limit,
            AccessToken accessToken) {
        return findLinkedDomainObjectsIds(domainObjectId, linkedType, linkedField, false, offset, limit, accessToken);
    }

    @Override
    public List<Id> findLinkedDomainObjectsIds(Id domainObjectId, String linkedType, String linkedField,
            boolean exactType, AccessToken accessToken) {
        return findLinkedDomainObjectsIds(domainObjectId, linkedType, linkedField, exactType, 0, 0, accessToken);
    }

    @Override
    public List<Id> findLinkedDomainObjectsIds(Id domainObjectId, String linkedType, String linkedField,
            boolean exactType, int offset, int limit, AccessToken accessToken) {
        if (offset == 0 && limit == 0) {
            String[] cacheKey = new String[] {linkedType, linkedField, String.valueOf(exactType),
                    String.valueOf(offset), String.valueOf(limit) };

            List<DomainObject> domainObjects =
                    domainObjectCacheService.getAll(domainObjectId, accessToken, cacheKey);

            if (domainObjects != null) {
                return extractIds(domainObjects);
            }

            final List<Id> ids = globalCacheClient.getLinkedDomainObjectsIds(domainObjectId, linkedType, linkedField, exactType, accessToken);
            validateCachedLinkedObjectsIds(domainObjectId, linkedType, linkedField, exactType, 0, 0, accessToken, ids);
            if (ids != null) {
                return ids;
            }
        }

        Pair<List<Id>, Long> queryResult = findLinkedDomainObjectsIdsInDB(domainObjectId, linkedType, linkedField, exactType, offset, limit, accessToken);
        final List<Id> result = queryResult.getFirst();
        globalCacheClient.notifyLinkedObjectsIdsRead(domainObjectId, linkedType, linkedField, exactType, queryResult.getFirst(), queryResult.getSecond(),
                accessToken);
        return result;
    }

    private Pair<List<Id>, Long> findLinkedDomainObjectsIdsInDB(Id domainObjectId, String linkedType, String linkedField, boolean exactType,
            int offset, int limit, AccessToken accessToken) {
        Map<String, Object> parameters = domainObjectQueryHelper.initializeParameters(accessToken);
        parameters.put(PARAM_DOMAIN_OBJECT_ID, ((RdbmsId) domainObjectId).getId());
        parameters.put(PARAM_DOMAIN_OBJECT_TYPE_ID, ((RdbmsId) domainObjectId).getTypeId());
        if (exactType) {
            parameters.put(RESULT_TYPE_ID, domainObjectTypeIdCache.getId(linkedType));
        }

        String query = buildFindChildrenIdsQuery(linkedType, linkedField, exactType, offset, limit, accessToken);

        long time = System.currentTimeMillis();
        return new Pair<>(switchableJdbcTemplate.query(query, parameters, new MultipleIdRowMapper(linkedType)), time);
    }

    @Override
    public DomainObject findByUniqueKey(String domainObjectType, Map<String, Value> uniqueKeyValuesByName, AccessToken accessToken) {
        return findByUniqueKeyImpl(domainObjectType, uniqueKeyValuesByName, accessToken, true);
    }

    @Override
    public DomainObject finAndLockByUniqueKey(String domainObjectType, Map<String, Value> uniqueKeyValuesByName, AccessToken accessToken) {
        return findByUniqueKeyInStorage(domainObjectType, uniqueKeyValuesByName, accessToken, true, true);
    }

    private DomainObject findByUniqueKeyImpl(String domainObjectType, Map<String, Value> uniqueKeyValuesByName, AccessToken accessToken, boolean logAccess) {
        DomainObject result = domainObjectCacheService.get(domainObjectType, uniqueKeyValuesByName, accessToken);
        if (result != null) {
            return result;
        }

        return findByUniqueKeyInStorage(domainObjectType, uniqueKeyValuesByName, accessToken, false, logAccess);
    }

    protected DomainObject findByUniqueKeyInStorage(String domainObjectType, Map<String, Value> uniqueKeyValuesByName,
            AccessToken accessToken, boolean lock, boolean logAccess) {
        DomainObject result = null;
        if (!lock) {
            result = globalCacheClient.getDomainObject(domainObjectType, uniqueKeyValuesByName, accessToken);
            validateCachedByUniqueKey(domainObjectType, uniqueKeyValuesByName, accessToken, logAccess, result);
        }
        if (result == null) {
            final Pair<DomainObject, Long> queryResult = findByUniqueKeyInDB(domainObjectType, uniqueKeyValuesByName, accessToken, lock, logAccess);
            result = queryResult.getFirst();
            // CMFIVE-27416
            String domainObjectRealType = null;
            if (result != null && !result.isAbsent() && (domainObjectRealType = getDOTypeName(result.getId())) != null && !domainObjectRealType.equalsIgnoreCase(domainObjectType)) {
                long time = System.currentTimeMillis();
                result = findInStorage(result.getId(), accessToken, lock);
                globalCacheClient.notifyReadByUniqueKey(domainObjectType, uniqueKeyValuesByName, result, queryResult.getSecond(), accessToken);
                globalCacheClient.notifyReadByUniqueKey(domainObjectRealType, uniqueKeyValuesByName, result, time, accessToken);
            } else {
                globalCacheClient.notifyReadByUniqueKey(domainObjectType, uniqueKeyValuesByName, result, queryResult.getSecond(), accessToken);
            }
            // CMFIVE-27416
        }
        if (GenericDomainObject.isAbsent(result)) {
            result = null;
        }
        if (result != null) {
            domainObjectCacheService.putOnRead(result, accessToken);
        }
        if (logAccess) {
            if (result != null) {
                eventLogService.logAccessDomainObjectEvent(result.getId(), EventLogService.ACCESS_OBJECT_READ, true);
            } else {
                // Проверяем существование доменного объекта с уникальным ключом
                // и логируем доступ
                // todo: проверить сначала необходимость логгирования
                final AccessToken systemAccessToken = createSystemAccessToken();
                DomainObject domainObject = findByUniqueKeyImpl(domainObjectType, uniqueKeyValuesByName, systemAccessToken, false);
                if (domainObject != null) {
                    if (eventLogService.isAccessDomainObjectEventEnabled(domainObject.getId(), EventLogService.ACCESS_OBJECT_READ, false)) {
                        eventLogService.logAccessDomainObjectEvent(domainObject.getId(), EventLogService.ACCESS_OBJECT_READ, false);
                    }
                }
            }
        }

        return result;
    }

    private void validateCachedById(Id id, AccessToken accessToken, DomainObject cached) {
        if (cached == null || !globalCacheManager.isDebugEnabled()) {
            return;
        }
        final DomainObject dbResult = findInDbById(id, accessToken, false);
        if (!cacheResultValid(cached, dbResult)) {
            logger.error("CACHE ERROR! Find by Id: " + id);
        }
    }

    private void validateCachedList(Collection<Id> ids, AccessToken accessToken, List<DomainObject> cached) {
        if (!globalCacheManager.isDebugEnabled()) {
            return;
        }
        int i = -1;
        for (Id id : ids) {
            ++i;
            final DomainObject cachedObject = cached.get(i);
            if (cachedObject == null) {
                continue;
            }
            final DomainObject dbResult = findInDbById(id, accessToken, false);
            if (!cacheResultValid(cachedObject, dbResult)) {
                logger.error("CACHE ERROR! Find by list: " + ids);
            }
        }
    }

    private void validateCachedAllObjects(String type, boolean exactType, AccessToken accessToken, List<DomainObject> cached) {
        if (cached == null || !globalCacheManager.isDebugEnabled()) {
            return;
        }
        final List<DomainObject> allDbObjects = findAllObjectsInDB(type, exactType, 0, 0, accessToken);
        if (!new HashSet<>(allDbObjects).equals(new HashSet<>(cached))) {
            logger.error("CACHE ERROR! Find all objects, type: " + type + ", exact type: " + exactType);
        }
    }

    private void validateCachedLinkedObjects(Id domainObjectId,
            String linkedType, String linkedField, boolean exactType, int offset, int limit,
            AccessToken accessToken, List<DomainObject> cached) {
        if (cached == null || !globalCacheManager.isDebugEnabled()) {
            return;
        }
        List<DomainObject> linked = findLinkedDomainObjectsInDB(domainObjectId, linkedType, linkedField, exactType, offset, limit, accessToken).getFirst();
        if (!new HashSet<>(linked).equals(new HashSet<>(cached))) {
            logger.error("CACHE ERROR! Find linked objects. TX ID: " + userTransactionService.getTransactionId());
        }
    }

    private void validateCachedLinkedObjectsIds(Id domainObjectId,
            String linkedType, String linkedField, boolean exactType, int offset, int limit,
            AccessToken accessToken, List<Id> cached) {
        if (cached == null || !globalCacheManager.isDebugEnabled()) {
            return;
        }
        List<Id> linked = findLinkedDomainObjectsIdsInDB(domainObjectId, linkedType, linkedField, exactType, offset, limit, accessToken).getFirst();
        if (!new HashSet<>(linked).equals(new HashSet<>(cached))) {
            logger.error("CACHE ERROR! Find linked objects IDs");
        }
    }

    private void validateCachedByUniqueKey(String domainObjectType, Map<String, Value> uniqueKeyValuesByName, AccessToken accessToken, boolean logAccess,
            DomainObject cached) {
        if (cached == null || !globalCacheManager.isDebugEnabled()) {
            return;
        }
        final DomainObject dbResult = findByUniqueKeyInDB(domainObjectType, uniqueKeyValuesByName, accessToken, false, logAccess).getFirst();
        if (!cacheResultValid(cached, dbResult)) {
            logger.error("CACHE ERROR! Find by unique key: " + uniqueKeyValuesByName);
        }
    }

    private boolean cacheResultValid(DomainObject cached, DomainObject queried) {
        if (cached == null) {
            return true;
        }
        return cached.equals(queried);
    }

    protected Pair<DomainObject, Long> findByUniqueKeyInDB(String domainObjectType, Map<String, Value> uniqueKeyValuesByName,
            AccessToken accessToken, boolean lock, boolean logAccess) {
        CaseInsensitiveMap<Value> uniqueKeyValues = new CaseInsensitiveMap<>(uniqueKeyValuesByName);

        DomainObjectTypeConfig domainObjectTypeConfig = configurationExplorer.getDomainObjectTypeConfig(domainObjectType);
        if (domainObjectTypeConfig == null) {
            throw new IllegalArgumentException("Unknown domain object type:" + domainObjectType);
        }

        List<UniqueKeyConfig> uniqueKeyConfigs = domainObjectTypeConfig.getUniqueKeyConfigs();
        UniqueKeyConfig uniqueKeyConfig = findUniqueKeyConfig(domainObjectType, uniqueKeyConfigs, uniqueKeyValues);

        String query = domainObjectQueryHelper.generateFindQuery(domainObjectType, uniqueKeyConfig, accessToken, lock);

        Map<String, Object> parameters = domainObjectQueryHelper.initializeParameters(accessToken);
        for (UniqueKeyFieldConfig uniqueKeyFieldConfig : uniqueKeyConfig.getUniqueKeyFieldConfigs()) {
            String name = Case.toLower(uniqueKeyFieldConfig.getName());

            Value value = uniqueKeyValues.get(name);
            parameters.put(name, value.get());

            FieldConfig fieldConfig = configurationExplorer.getFieldConfig(domainObjectType, name);
            initializeDomainParameter(fieldConfig, value, parameters);
        }

        long time = System.currentTimeMillis();
        return new Pair<>(switchableJdbcTemplate.query(query, parameters,
                new SingleObjectRowMapper(domainObjectType, configurationExplorer, domainObjectTypeIdCache)), time);
    }

    @Override
    public Id createId(String type, long id) {
        return new RdbmsId(domainObjectTypeIdCache.getId(type), id);
    }

    /**
     * Инициализирует параметры для для создания доменного объекта
     * 
     * @param domainObject
     *            доменный объект
     * @param domainObjectTypeConfig
     *            конфигурация доменного объекта
     * @return карту объектов содержащую имя параметра и его значение
     */
    protected Map<String, Object> initializeCreateParameters(
            DomainObject domainObject,
            DomainObjectTypeConfig domainObjectTypeConfig, Integer type, AccessToken accessToken) {

        RdbmsId rdbmsId = (RdbmsId) domainObject.getId();

        Map<String, Object> parameters = domainObjectQueryHelper.initializeParameters(rdbmsId);

        if (!isDerived(domainObjectTypeConfig)) {
            parameters.put("created_date",
                    getGMTDate(domainObject.getCreatedDate()));
            parameters.put("updated_date",
                    getGMTDate(domainObject.getModifiedDate()));

            Id currentUser = domainObject.getCreatedBy();
            Long currentUserId = currentUser != null ? ((RdbmsId) currentUser).getId() : null;
            Integer currentUserType = currentUser != null ? ((RdbmsId) currentUser).getTypeId() : null;

            parameters.put("created_by", currentUserId);
            parameters.put("created_by_type", currentUserType);

            parameters.put("updated_by", currentUserId);
            parameters.put("updated_by_type", currentUserType);

            Long statusId = domainObject.getStatus() != null ? ((RdbmsId) domainObject.getStatus()).getId() : null;
            Integer statusTypeId =
                    domainObject.getStatus() != null ? ((RdbmsId) domainObject.getStatus()).getTypeId() : null;

            parameters.put("status", statusId);
            parameters.put("status_type", statusTypeId);


            Long stampId = domainObject.getStamp() != null ? ((RdbmsId) domainObject.getStamp()).getId() : null;
            Integer stampTypeId =
                    domainObject.getStamp() != null ? ((RdbmsId) domainObject.getStamp()).getTypeId() : null;
            parameters.put(SECURITY_STAMP_COLUMN, stampId);
            parameters.put(SECURITY_STAMP_TYPE_COLUMN, stampTypeId);

            final RdbmsId accessObjectId = (RdbmsId) getAccessObjectId(domainObject);
            parameters.put("access_object_id", accessObjectId.getId());
            parameters.put("___access_object_id", accessObjectId);

        }

        List<FieldConfig> feldConfigs = domainObjectTypeConfig
                .getDomainObjectFieldsConfig().getFieldConfigs();

        initializeDomainParameters(domainObject, feldConfigs, parameters);

        return parameters;
    }

    /**
     * Возвращает текущего пользователя. Если вызов идет от имени системы, то
     * возвращает null, иначе возвращается текущий пользователь из EJB
     * контекста.
     * @param accessToken
     * @return
     */
    private Id getCurrentUser(AccessToken accessToken) {
        return currentUserAccessor.getCurrentUserId();
    }

    /**
     * Создает SQL запрос для нахождения всех доменных объектов определенного
     * типа
     * 
     * @param typeName
     *            тип доменного объекта
     * @return SQL запрос для нахождения доменного объекта
     */
    protected String generateFindAllQuery(String typeName, boolean exactType, int offset,
            int limit, AccessToken accessToken) {
        String tableAlias = getSqlAlias(typeName);

        StringBuilder query = new StringBuilder();
        query.append("select ");
        domainObjectQueryHelper.appendColumnsQueryPart(query, typeName);
        if (!exactType) {
            appendChildColumns(query, typeName);
        }

        query.append(" from ");

        domainObjectQueryHelper.appendTableNameQueryPart(query, typeName);
        if (!exactType) {
            appendChildTables(query, typeName);
        }

        query.append(" where 1=1");

        if (exactType) {
            query.append(" and ").append(tableAlias).append(".").append(wrap(TYPE_COLUMN)).append(" = :").append(RESULT_TYPE_ID);
        }

        if (accessToken.isDeferred()) {
            domainObjectQueryHelper.appendAccessControlLogicToQuery(query, typeName);
        }

        applyOffsetAndLimitWithDefaultOrdering(query, tableAlias, offset, limit);

        return query.toString();
    }

    /**
     * Создает SQL запрос для модификации доменного объекта
     * 
     * @param domainObjectTypeConfig
     *            конфигурация доменного объекта
     * @return строку запроса для модиификации доменного объекта с параметрами
     */
    protected Query generateUpdateQuery(DomainObjectTypeConfig domainObjectTypeConfig, boolean isUpdateStatus) {
        Query query = new Query();
        StringBuilder queryBuilder = new StringBuilder();

        String tableName = getSqlName(domainObjectTypeConfig);

        List<FieldConfig> fieldConfigs = configurationExplorer.getDomainObjectTypeMutableFields(domainObjectTypeConfig.getName(), false);

        List<String> columnNames = DataStructureNamingHelper
                .getColumnNames(fieldConfigs);

        String fieldsWithParams = DaoUtils
                .generateCommaSeparatedListWithParams(columnNames);

        if (isDerived(domainObjectTypeConfig) && fieldsWithParams.isEmpty()) {
            return null;
        }
        queryBuilder.append("update ").append(wrap(tableName)).append(" set ");

        if (!isDerived(domainObjectTypeConfig)) {
            queryBuilder.append(wrap(UPDATED_DATE_COLUMN)).append("=?, ");
            query.addDateParameter("current_date");

            queryBuilder.append(wrap(UPDATED_BY)).append("=?, ");
            query.addReferenceParameter(UPDATED_BY);

            queryBuilder.append(wrap(UPDATED_BY_TYPE_COLUMN)).append("=?, ");
            query.addReferenceTypeParameter(UPDATED_BY_TYPE_COLUMN);

            if (isUpdateStatus) {
                queryBuilder.append(wrap(STATUS_FIELD_NAME)).append("=?, ");
                queryBuilder.append(wrap(STATUS_TYPE_COLUMN)).append("=?, ");
                query.addReferenceParameters(STATUS_FIELD_NAME);
            }

            queryBuilder.append(wrap(SECURITY_STAMP_COLUMN)).append("=?, ");
            queryBuilder.append(wrap(SECURITY_STAMP_TYPE_COLUMN)).append("=?, ");
            query.addReferenceParameters(SECURITY_STAMP_COLUMN);
        }

        if (columnNames.size() > 0) {
            queryBuilder.append(fieldsWithParams);
            query.addParameters(columnNames, fieldConfigs);
        }

        queryBuilder.append(" where ").append(wrap(ID_COLUMN)).append("=?");
        query.addReferenceParameter(ID_COLUMN);

        if (!isDerived(domainObjectTypeConfig)) {
            // Время урезаем до милисекунд из за CMFIVE-7267 Postgres Specific
            queryBuilder.append(" and date_trunc('milliseconds', ").append(wrap(UPDATED_DATE_COLUMN)).append(")=?");
            query.addDateParameter(UPDATED_DATE_COLUMN);
        }

        query.setQuery(queryBuilder.toString());
        return query;

    }

    /**
     * Инициализирует параметры для для создания доменного объекта
     * 
     * @param domainObject
     *            доменный объект
     * @param domainObjectTypeConfig
     *            конфигурация доменного объекта
     * @return карту объектов содержащую имя параметра и его значение
     */
    protected Map<String, Object> initializeUpdateParameters(
            DomainObject domainObject,
            DomainObjectTypeConfig domainObjectTypeConfig, AccessToken accessToken, Date currentDate,
            boolean isUpdateStatus) {

        List<FieldConfig> fieldConfigs = domainObjectTypeConfig.getDomainObjectFieldsConfig().getFieldConfigs();
        Map<String, Object> parameters = new HashMap<>(fieldConfigs.size() * 2 + 7);

        RdbmsId rdbmsId = (RdbmsId) domainObject.getId();

        parameters.put("id", rdbmsId.getId());
        parameters.put("current_date", getGMTDate(currentDate));
        parameters.put("updated_date",
                getGMTDate(domainObject.getModifiedDate()));

        Id currentUser = getCurrentUser(accessToken);

        Long currentUserId = currentUser != null ? ((RdbmsId) currentUser).getId() : null;
        Integer currentUserType = currentUser != null ? ((RdbmsId) currentUser).getTypeId() : null;

        parameters.put("updated_by", currentUserId);
        parameters.put("updated_by_type", currentUserType);

        if (isUpdateStatus) {
            parameters.put("status", ((RdbmsId) domainObject.getStatus()).getId());
            parameters.put("status_type", ((RdbmsId) domainObject.getStatus()).getTypeId());
        }

        if (domainObject.getStamp() != null) {
            parameters.put(SECURITY_STAMP_COLUMN, ((RdbmsId) domainObject.getStamp()).getId());
            parameters.put(SECURITY_STAMP_TYPE_COLUMN, ((RdbmsId) domainObject.getStamp()).getTypeId());
        }else{
            parameters.put(SECURITY_STAMP_COLUMN, null);
            parameters.put(SECURITY_STAMP_TYPE_COLUMN, null);
        }

        initializeDomainParameters(domainObject, fieldConfigs, parameters);

        return parameters;

    }

    /**
     * Создает SQL запрос для создания доменного объекта
     * 
     * @param domainObjectTypeConfig
     *            конфигурация доменного объекта
     * @return строку запроса для создания доменного объекта с параметрами
     */
    protected Query generateCreateQuery(
            DomainObjectTypeConfig domainObjectTypeConfig) {
        Query query = new Query();

        List<FieldConfig> fieldConfigs = domainObjectTypeConfig
                .getFieldConfigs();

        String tableName = getSqlName(domainObjectTypeConfig);
        List<String> columnNames = DataStructureNamingHelper
                .getColumnNames(fieldConfigs);

        String commaSeparatedColumns =
                new DelimitedListFormatter<String>().formatAsDelimitedList(columnNames, ", ", "\"");

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("insert into ").append(wrap(tableName)).append(" (").append(wrap(ID_COLUMN)).append(", ");
        query.addReferenceParameter(ID_COLUMN);

        queryBuilder.append(wrap(TYPE_COLUMN));
        query.addReferenceTypeParameter(TYPE_COLUMN);

        if (!isDerived(domainObjectTypeConfig)) {
            queryBuilder.append(", ");
            queryBuilder.append(wrap(CREATED_DATE_COLUMN)).append(", ")
                    .append(wrap(UPDATED_DATE_COLUMN)).append(", ");
            query.addParameters(DateTimeFieldConfig.class, CREATED_DATE_COLUMN, UPDATED_DATE_COLUMN);

            queryBuilder.append(wrap(CREATED_BY)).append(", ")
                    .append(wrap(CREATED_BY_TYPE_COLUMN)).append(", ");
            query.addReferenceParameters(CREATED_BY);

            queryBuilder.append(wrap(UPDATED_BY)).append(", ")
                    .append(wrap(UPDATED_BY_TYPE_COLUMN)).append(", ");
            query.addReferenceParameters(UPDATED_BY);

            queryBuilder.append(wrap(STATUS_FIELD_NAME)).append(", ")
                    .append(wrap(STATUS_TYPE_COLUMN)).append(", ");
            query.addReferenceParameters(STATUS_FIELD_NAME);

            queryBuilder.append(wrap(SECURITY_STAMP_COLUMN)).append(", ")
                    .append(wrap(SECURITY_STAMP_TYPE_COLUMN)).append(", ");
            query.addReferenceParameters(SECURITY_STAMP_COLUMN);

            queryBuilder.append(wrap(ACCESS_OBJECT_ID));
            query.addLongParameter(ACCESS_OBJECT_ID);
        }

        if (commaSeparatedColumns.length() > 0) {
            queryBuilder.append(", ").append(commaSeparatedColumns);
            query.addParameters(columnNames, fieldConfigs);
        }

        queryBuilder.append(") values (");

        for (int i = 0; i < query.getNameToParameterInfoMap().size(); i++) {
            if (i > 0) {
                queryBuilder.append(", ");
            }

            queryBuilder.append("?");
        }

        queryBuilder.append(")");

        query.setQuery(queryBuilder.toString());
        return query;
    }

    /**
     * Формирование запроса на добавление записи в таблицу аудита
     * 
     * @param domainObjectTypeConfig
     * @return
     */
    protected String generateCreateAuditLogQuery(
            DomainObjectTypeConfig domainObjectTypeConfig) {
        List<FieldConfig> fieldConfigs = domainObjectTypeConfig
                .getFieldConfigs();

        String tableName = getALTableSqlName(domainObjectTypeConfig.getName());
        List<String> columnNames = DataStructureNamingHelper
                .getColumnNames(fieldConfigs);

        String commaSeparatedColumns =
                new DelimitedListFormatter<String>().formatAsDelimitedList(columnNames, ", ", "\"");
        String commaSeparatedParameters = DaoUtils
                .generateCommaSeparatedParameters(columnNames);

        StringBuilder query = new StringBuilder();
        query.append("insert into ").append(wrap(tableName)).append("(");
        query.append(wrap(ID_COLUMN)).append(", ");
        query.append(wrap(TYPE_COLUMN));
        if (!isDerived(domainObjectTypeConfig)) {
            query.append(", ");
            query.append(wrap(CREATED_DATE_COLUMN)).append(", ");
            query.append(wrap(UPDATED_DATE_COLUMN)).append(", ");

            query.append(wrap(CREATED_BY)).append(", ");
            query.append(wrap(CREATED_BY_TYPE_COLUMN)).append(", ");

            query.append(wrap(UPDATED_BY)).append(", ");
            query.append(wrap(UPDATED_BY_TYPE_COLUMN)).append(", ");

            query.append(wrap(STATUS_FIELD_NAME)).append(", ");
            query.append(wrap(STATUS_TYPE_COLUMN)).append(", ");
            query.append(wrap(ACCESS_OBJECT_ID)).append(", ");

            query.append(wrap(OPERATION_COLUMN)).append(", ");
            query.append(wrap(COMPONENT_COLUMN)).append(", ");
            query.append(wrap(DOMAIN_OBJECT_ID_COLUMN)).append(", ");
            query.append(wrap(DOMAIN_OBJECT_ID_TYPE_COLUMN)).append(", ");

            query.append(wrap(INFO_COLUMN)).append(", ");
            query.append(wrap(IP_ADDRESS_COLUMN));
        }

        if (commaSeparatedColumns.length() > 0) {
            query.append(", ").append(commaSeparatedColumns);
        }

        query.append(") values (:").append(ID_COLUMN).append(", :").append(TYPE_COLUMN);
        if (!isDerived(domainObjectTypeConfig)) {
            query.append(", :created_date, :updated_date, :created_by, :created_by_type, :updated_by, :updated_by_type, " +
                    " :status, :status_type, :access_object_id, :operation, :component, :domain_object_id, :domain_object_id_type, :info, :ip_address");
        }

        if (commaSeparatedParameters.length() > 0) {
            query.append(", ").append(commaSeparatedParameters);
        }

        query.append(")");

        return query.toString();

    }

    /**
     * Создает SQL запрос для удаления доменного объекта
     * 
     * @param domainObjectTypeConfig
     *            конфигурация доменного объекта
     * @return строку запроса для удаления доменного объекта с параметрами
     */
    protected String generateDeleteQuery(
            DomainObjectTypeConfig domainObjectTypeConfig) {

        String tableName = getSqlName(domainObjectTypeConfig);

        StringBuilder query = new StringBuilder();
        query.append("delete from ").append(wrap(tableName)).append(" where ").append(wrap(ID_COLUMN)).append("=:id");

        return query.toString();

    }

    /**
     * Создает SQL запрос для удаления всех доменных объектов
     * 
     * @param domainObjectTypeConfig
     *            конфигурация доменного объекта
     * @return строку запроса для удаления всех доменных объектов
     */
    protected String generateDeleteAllQuery(
            DomainObjectTypeConfig domainObjectTypeConfig) {

        String tableName = getSqlName(domainObjectTypeConfig);

        StringBuilder query = new StringBuilder();
        query.append("delete from ").append(wrap(tableName));

        return query.toString();

    }

    /**
     * Создает SQL запрос для проверки существует ли доменный объект
     * 
     * @param domainObjectName
     *            название доменного объекта
     * @return строку запроса для удаления доменного объекта с параметрами
     */
    protected String generateExistsQuery(String domainObjectName) {

        String tableName = getSqlName(domainObjectName);

        StringBuilder query = new StringBuilder();
        query.append("select count(*) from ").append(wrap(tableName)).append(" where ").
                append(wrap(ID_COLUMN)).append("=:id");

        return query.toString();

    }

    /**
     * Инициализирует параметры для удаления доменного объекта
     * 
     * @param id
     *            идентификатор доменных объектов для удаления
     * @return карту объектов содержащую имя параметра и его значение
     */
    protected Map<String, Object> initializeExistsParameters(Id id) {
        return Collections.<String, Object> singletonMap("id", ((RdbmsId) id).getId());
    }

    /**
     * Проверяет какого типа идентификатор
     */
    private void validateIdType(Id id) {
        if (id == null) {
            throw new InvalidIdException(id);
        }
        if (!(id instanceof RdbmsId)) {
            throw new InvalidIdException(id);
        }
    }

    private void initializeDomainParameters(DomainObject domainObject,
            List<FieldConfig> fieldConfigs, Map<String, Object> parameters) {
        for (FieldConfig fieldConfig : fieldConfigs) {
            Value value = null;
            // В случае удаление сюда придет null в параметре domainObject, при
            // этом значения параметров инициализируем null
            if (domainObject != null) {
                value = domainObject.getValue(fieldConfig.getName());
            }

            if (fieldConfig instanceof StringFieldConfig) {
                StringFieldConfig stringFieldConfig = (StringFieldConfig) fieldConfig;
                if (value != null && value.get() != null) {
                    if (stringFieldConfig.getEncrypted() != null
                            && stringFieldConfig.getEncrypted()) {
                        String str = (String) value.get();
                        if (wasUpdated(domainObject, stringFieldConfig, str)) {
                            value = new StringValue(MD5Utils.getMD5AsHex(str));
                            domainObject.setValue(fieldConfig.getName(), value);
                        }
                    }
                } else if ((value == null || value.get() == null)
                        && stringFieldConfig.isNotNull() && stringFieldConfig.getDefaultValue() != null) {
                    value = new StringValue(stringFieldConfig.getDefaultValue());
                }
            }
            initializeDomainParameter(fieldConfig, value, parameters);
        }
    }

    private void initializeDomainParameter(FieldConfig fieldConfig, Value value, Map<String, Object> parameters) {
        if (value != null && !value.getClass().equals(fieldConfig.getFieldType().getValueClass())) {
            // todo: later change to exception throwing
            // throw new DaoException("Trying to assign value: " + value +
            // " to Field of type: " + fieldConfig.getFieldType());
            final String msg = "Trying to assign value: " + value + " to Field of type: " + fieldConfig.getFieldType();
            logger.warn(msg);
            logger.debug(msg, new DaoException());
        }

        String columnName = getSqlName(fieldConfig.getName());
        String parameterName = generateParameter(columnName);

        if (value == null || value.get() == null) {
            parameters.put(parameterName, null);
            if (ReferenceFieldConfig.class.equals(fieldConfig.getClass())) {
                parameterName = generateParameter(getReferenceTypeColumnName(fieldConfig.getName()));
                parameters.put(parameterName, null);
            } else if (DateTimeWithTimeZoneFieldConfig.class.equals(fieldConfig.getClass())) {
                parameterName =
                        generateParameter(getTimeZoneIdColumnName(fieldConfig.getName()));
                parameters.put(parameterName, null);
            }
            return;
        }

        setParameter(parameterName, value, parameters, false);
    }

    private boolean wasUpdated(DomainObject domainObject, StringFieldConfig stringFieldConfig, String str) {

        if (domainObject.isNew())
            return true;

        AccessToken accessToken = createSystemAccessToken();
        DomainObject originalDomainObject = find(domainObject.getId(), accessToken);
        if (originalDomainObject == null)
            return true;

        String originalString = originalDomainObject.getString(stringFieldConfig.getName());

        if (str == null)
            return originalString != null;

        return !str.equals(originalString);

    }

    protected String buildFindChildrenQuery(String linkedType, String linkedField, boolean exactType,
            int offset, int limit, AccessToken accessToken) {
        String tableAlias = getSqlAlias(linkedType);
        String tableHavingLinkedFieldAlias =
                getSqlAlias(configurationExplorer.getFromHierarchyDomainObjectTypeHavingField(linkedType, linkedField));

        StringBuilder query = new StringBuilder(200);
        query.append("select ");
        domainObjectQueryHelper.appendColumnsQueryPart(query, linkedType);
        if (!exactType) {
            appendChildColumns(query, linkedType);
        }

        query.append(" from ");

        domainObjectQueryHelper.appendTableNameQueryPart(query, linkedType);
        if (!exactType) {
            appendChildTables(query, linkedType);
        }

        query.append(" where ").append(tableHavingLinkedFieldAlias).append(".").
                append(wrap(getSqlName(linkedField))).append(" = :").append(PARAM_DOMAIN_OBJECT_ID).
                append(" and ").append(wrap(getSqlName(getReferenceTypeColumnName(linkedField)))).
                append(" = :").append(PARAM_DOMAIN_OBJECT_TYPE_ID);

        if (exactType) {
            query.append(" and ").append(tableHavingLinkedFieldAlias).append(".").append(wrap(TYPE_COLUMN)).
                    append(" = :").append(RESULT_TYPE_ID);
        }

        boolean isDomainObject = configurationExplorer.getConfig(DomainObjectTypeConfig.class, DaoUtils.unwrap(linkedType)) != null;

        if (accessToken.isDeferred() && isDomainObject) {
            domainObjectQueryHelper.appendAccessControlLogicToQuery(query, linkedType);
        }

        applyOffsetAndLimitWithDefaultOrdering(query, tableAlias, offset, limit);

        return query.toString();
    }

    protected String buildFindChildrenIdsQuery(String linkedType, String linkedField, boolean exactType,
            int offset, int limit, AccessToken accessToken) {
        String doTypeHavingLinkedField = configurationExplorer.getFromHierarchyDomainObjectTypeHavingField(linkedType, linkedField);
        String tableName = getSqlName(doTypeHavingLinkedField);
        String tableAlias = getSqlAlias(tableName);

        StringBuilder query = new StringBuilder();
        query.append("select ").append(tableAlias).append(".").append(wrap(ID_COLUMN)).
                append(", ").append(tableAlias).append(".").append(wrap(getReferenceTypeColumnName(ID_COLUMN))).
                append(" from ").append(wrap(tableName)).append(" ").append(tableAlias);

        if (!doTypeHavingLinkedField.equalsIgnoreCase(linkedType)) {

            String parentTableName = getSqlName(linkedType);
            String parentTableAlias = getSqlAlias(linkedType);

            query.append(" inner join ").append(wrap(parentTableName)).append(" ")
                    .append(parentTableAlias);
            query.append(" on ").append(tableAlias).append(".").append(wrap(ID_COLUMN))
                    .append(" = ");
            query.append(parentTableAlias).append(".").append(wrap(ID_COLUMN));

        }

        query.append(" where ").append(tableAlias).append(".").append(wrap(getSqlName(linkedField))).
                append(" = :").append(PARAM_DOMAIN_OBJECT_ID).
                append(" and ").append(wrap(getSqlName(getReferenceTypeColumnName(linkedField))))
                .append(" = :").append(PARAM_DOMAIN_OBJECT_TYPE_ID);

        if (exactType) {
            query.append(" and ").append(tableAlias).append(".").append(wrap(TYPE_COLUMN)).
                    append(" = :").append(RESULT_TYPE_ID);
        }

        if (accessToken.isDeferred()) {
            domainObjectQueryHelper.appendAccessControlLogicToQuery(query, linkedType);
        }

        applyOffsetAndLimitWithDefaultOrdering(query, tableAlias, offset, limit);

        return query.toString();
    }

    private boolean isAdministratorWithAllPermissions(Id personId, String domainObjectType) {
        return AccessControlUtility.isAdministratorWithAllPermissions(personId, domainObjectType, userGroupCache, configurationExplorer);
    }

    private DomainObject[] create(DomainObject[] domainObjects, Integer type, AccessToken accessToken, String initialStatus) {
        DomainObjectTypeConfig domainObjectTypeConfig = configurationExplorer
                .getConfig(DomainObjectTypeConfig.class,
                        domainObjects[0].getTypeName());
        GenericDomainObject[] updatedObjects = new GenericDomainObject[domainObjects.length];
        for (int i = 0; i < domainObjects.length; i++) {
            updatedObjects[i] = new GenericDomainObject(domainObjects[i]);
        }

        DomainObject[] parentDOs = createParentDO(domainObjects,
                domainObjectTypeConfig, type, accessToken, initialStatus);

        Id currentUser = getCurrentUser(accessToken);

        for (int i = 0; i < updatedObjects.length; i++) {
            final GenericDomainObject updatedObject = updatedObjects[i];
            if (parentDOs != null) {
                final DomainObject parentDO = parentDOs[i];
                updatedObject.setCreatedDate(parentDO.getCreatedDate());
                updatedObject.setModifiedDate(parentDO.getModifiedDate());
                updatedObject.setValue("access_object_id", parentDO.getValue("access_object_id"));
            } else {
                Date currentDate = new Date();
                updatedObject.setCreatedDate(currentDate);
                updatedObject.setModifiedDate(currentDate);
            }

            setInitialStatus(initialStatus, updatedObject);
            updatedObject.setCreatedBy(currentUser);
            updatedObject.setModifiedBy(currentUser);
        }

        Query query = generateCreateQuery(domainObjectTypeConfig);

        ArrayList<Map<String, Object>> parameters = new ArrayList<>(updatedObjects.length);

        int doTypeId = domainObjectTypeIdCache.getId(domainObjectTypeConfig.getName());
        List ids = parentDOs == null ? idGenerator.generateIds(doTypeId, updatedObjects.length) : null;

        for (int i = 0; i < updatedObjects.length; i++) {

            GenericDomainObject domainObject = updatedObjects[i];
            accessControlService.verifyAccessTokenOnCreate(accessToken, domainObject, type);

            Object id;
            if (parentDOs != null) {
                id = ((RdbmsId) parentDOs[i].getId()).getId();
            } else {
                id = ids.get(i);
            }

            RdbmsId doId = new RdbmsId(type, (Long) id);
            updatedObjects[i].setId(doId);

            parameters.add(initializeCreateParameters(
                    updatedObjects[i], domainObjectTypeConfig, type, accessToken));
        }

        BatchPreparedStatementSetter batchPreparedStatementSetter =
                new BatchPreparedStatementSetter(query);
        masterJdbcOperations.batchUpdate(query.getQuery(), parameters, BATCH_SIZE, batchPreparedStatementSetter);

        for (int i = 0; i < updatedObjects.length; i++) {
            final Id accessObjectIdParam = (Id) parameters.get(i).get("___access_object_id");
            if (accessObjectIdParam != null) { // it won't be null for the root
                                               // element, if there's no
                                               // null-check - it would
                                               // overwrite child field with
                                               // NULL-value - undesired
                final GenericDomainObject updatedObject = updatedObjects[i];
                updatedObject.setReference("access_object_id", accessObjectIdParam);
                updatedObject.resetDirty();
            }
        }

        return updatedObjects;
    }

    /**
     * Устанавливает начальный статус, если у доменного объекта поле статус не
     * выставлено. Если начальный статус не указан в конфигурации доменного
     * объекта, то используется начальный статус родительского объекта
     * (рекурсивно)
     * @param initialStatus
     * @param updatedObject
     */
    private void setInitialStatus(String initialStatus, GenericDomainObject updatedObject) {
        if (updatedObject.getStatus() == null && initialStatus != null) {
            DomainObject status = getStatusByName(initialStatus);
            Id statusId = status.getId();
            updatedObject.setStatus(statusId);
        }
    }

    private DomainObject getStatusByName(String statusName) {
        AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
        return findByUniqueKey(STATUS_DO, Collections.<String, Value> singletonMap("name", new StringValue(statusName)), accessToken);
    }

    /**
     * Получение конфигурации включения аудит лога для типа
     * 
     * @param domainObjectTypeConfig
     * @return
     */
    private boolean isAuditLogEnable(
            DomainObjectTypeConfig domainObjectTypeConfig) {
        boolean result = false;

        // Если в конфигурации доменного объекта указан флаг включения аудит
        // лога то принимаем его
        if (domainObjectTypeConfig.isAuditLog() != null) {
            result = domainObjectTypeConfig.isAuditLog();
        } else {
            // Если в конфигурации доменного объекта НЕ указан флаг включения
            // аудит лога то принимаем конфигурацию из блока глобальной
            // конфигурации
            GlobalSettingsConfig globalSettings = configurationExplorer.getGlobalSettings();

            if (globalSettings != null && globalSettings.getAuditLog() != null) {
                result = globalSettings.getAuditLog().isEnable();
            }
        }
        return result;
    }

    /**
     * Запись информации аудит лог в базу
     * 
     * @param domainObject
     * @param auditLogType
     * @return
     */
    private Long createAuditLog(DomainObject domainObject, String domainObjectType,
            Integer auditLogType, AccessToken accessToken, DomainObjectVersion.AuditLogOperation operation) {
        Long id = null;
        if (domainObjectType != null) {
            DomainObjectTypeConfig domainObjectTypeConfig = configurationExplorer
                    .getConfig(DomainObjectTypeConfig.class, domainObjectType);

            // Проверка на включенность аудит лога, или если пришли рекурсивно
            // из подчиненного уровня, где аудит был включен
            if (isAuditLogEnable(domainObjectTypeConfig)
                    || !domainObject.getTypeName().equals(domainObjectType)) {

                id = createAuditLog(domainObject,
                        domainObjectTypeConfig.getExtendsAttribute(), auditLogType, accessToken,
                        operation);

                if (id == null) {
                    String auditLogTableName = DataStructureNamingHelper.getALTableSqlName(domainObjectType);
                    Integer auditLogTypeId = domainObjectTypeIdCache.getId(auditLogTableName);
                    id = (Long) idGenerator.generateId(auditLogTypeId);
                }

                String query = generateCreateAuditLogQuery(domainObjectTypeConfig);

                List<FieldConfig> fieldConfigs = domainObjectTypeConfig.getDomainObjectFieldsConfig().getFieldConfigs();
                Map<String, Object> parameters = new HashMap<>(fieldConfigs.size() * 2 + 20);
                parameters.put(DomainObjectDao.ID_COLUMN, id);
                parameters.put(DomainObjectDao.TYPE_COLUMN, auditLogType);

                if (!isDerived(domainObjectTypeConfig)) {
                    parameters.put(DomainObjectDao.OPERATION_COLUMN,
                            operation.getOperation());

                    Date createdDate = domainObject.getCreatedDate() != null ? domainObject.getCreatedDate() : new Date();
                    parameters.put(DomainObjectDao.CREATED_DATE_COLUMN,
                            getGMTDate(createdDate));

                    parameters.put(DomainObjectDao.UPDATED_DATE_COLUMN,
                            getGMTDate(domainObject.getModifiedDate()));

                    Id currentUser = getCurrentUser(accessToken);

                    Long currentUserId = currentUser != null ? ((RdbmsId) currentUser).getId() : null;
                    Integer currentUserType = currentUser != null ? ((RdbmsId) currentUser).getTypeId() : null;

                    Long createdById = domainObject.getCreatedBy() != null ? ((RdbmsId) domainObject.getCreatedBy()).getId() : null;
                    Long createdByType = domainObject.getCreatedBy() != null ? new Long(((RdbmsId) domainObject.getCreatedBy()).getTypeId()) : null;

                    parameters.put(DomainObjectDao.CREATED_BY, createdById);
                    parameters.put(DomainObjectDao.CREATED_BY_TYPE_COLUMN, createdByType);

                    parameters.put(DomainObjectDao.UPDATED_BY, currentUserId);
                    parameters.put(DomainObjectDao.UPDATED_BY_TYPE_COLUMN, currentUserType);

                    Long statusId = domainObject.getStatus() != null ? ((RdbmsId) domainObject.getStatus()).getId() : null;
                    Integer statusTypeId =
                            domainObject.getStatus() != null ? ((RdbmsId) domainObject.getStatus()).getTypeId() : null;

                    parameters.put(GenericDomainObject.STATUS_FIELD_NAME, statusId);
                    parameters.put(DomainObjectDao.STATUS_TYPE_COLUMN, statusTypeId);

                    Long accessObjectId = domainObject.getReference(DomainObjectDao.ACCESS_OBJECT_ID) != null ?
                            ((RdbmsId) domainObject.getReference(DomainObjectDao.ACCESS_OBJECT_ID)).getId() : null;
                    parameters.put(DomainObjectDao.ACCESS_OBJECT_ID, accessObjectId);

                    // TODO Получение имени компонента из AcceeToken
                    parameters.put(DomainObjectDao.COMPONENT_COLUMN, "");
                    parameters.put(DomainObjectDao.DOMAIN_OBJECT_ID_COLUMN,
                            ((RdbmsId) domainObject.getId()).getId());
                    parameters.put(DomainObjectDao.DOMAIN_OBJECT_ID_TYPE_COLUMN,
                            ((RdbmsId) domainObject.getId()).getTypeId());
                    parameters.put(DomainObjectDao.INFO_COLUMN, "");
                    // TODO Получение ip адреса
                    parameters.put(DomainObjectDao.IP_ADDRESS_COLUMN, "");

                }

                if (operation == DomainObjectVersion.AuditLogOperation.DELETE) {
                    initializeDomainParameters(null, fieldConfigs, parameters);
                } else {
                    initializeDomainParameters(domainObject, fieldConfigs,
                            parameters);
                }

                masterJdbcTemplate.update(query, parameters);
                final RdbmsId alId = new RdbmsId(auditLogType, id);
                getTransactionListener().addModifiedAutoDomainObjectId(alId);
                globalCacheClient.notifyDelete(alId); // it's a trick as the
                                                      // behavior should be the
                                                      // same as with removal
            }

        }
        return id;
    }

    private DomainObject[] createParentDO(DomainObject[] domainObjects,
            DomainObjectTypeConfig domainObjectTypeConfig, Integer type, AccessToken accessToken, String initialStatus) {
        if (!isDerived(domainObjectTypeConfig)) {
            return null;
        }

        GenericDomainObject[] parentDOs = new GenericDomainObject[domainObjects.length];

        for (int i = 0; i < domainObjects.length; i++) {
            parentDOs[i] = new GenericDomainObject(domainObjects[i]);
            parentDOs[i].setTypeName(domainObjectTypeConfig.getExtendsAttribute());
        }

        return create(parentDOs, type, accessToken, initialStatus);
    }

    private DomainObject[] updateParentDO(DomainObjectTypeConfig domainObjectTypeConfig, DomainObject domainObjects[],
            AccessToken accessToken,
            boolean isUpdateStatus, DomainObjectModification[] domainObjectModifications) {

        GenericDomainObject[] parentObjects = new GenericDomainObject[domainObjects.length];

        for (int i = 0; i < domainObjects.length; i++) {

            RdbmsId parentId = getParentId((RdbmsId) domainObjects[i].getId(), domainObjectTypeConfig);
            if (parentId == null) {
                return null;
            }

            GenericDomainObject parentObject = new GenericDomainObject(domainObjects[i]);
            parentObject.setId(parentId);
            parentObject.setTypeName(domainObjectTypeConfig.getExtendsAttribute());

            parentObjects[i] = parentObject;
        }

        return update(parentObjects, accessToken, isUpdateStatus, domainObjectModifications);
    }

    private void appendChildTables(StringBuilder query, String typeName) {
        Collection<DomainObjectTypeConfig> childConfigs = configurationExplorer.findChildDomainObjectTypes(typeName, true);

        if (childConfigs == null || childConfigs.isEmpty()) {
            return;
        }

        String tableAlias = getSqlAlias(typeName);

        for (DomainObjectTypeConfig childConfig : childConfigs) {
            String childTableName = getSqlName(childConfig.getName());
            String childTableAlias = getSqlAlias(childConfig.getName());

            query.append(" left outer join ").append(wrap(childTableName)).append(" ").append(childTableAlias).
                    append(" on (").append(tableAlias).append(".").append(wrap(ID_COLUMN)).append(" = ").
                    append(childTableAlias).append(".").append(wrap(ID_COLUMN)).append(" and ").
                    append(tableAlias).append(".").append(wrap(TYPE_COLUMN)).append(" = ").
                    append(childTableAlias).append(".").append(wrap(TYPE_COLUMN)).append(")");
        }
    }

    private void appendChildColumns(StringBuilder query, String typeName) {
        Collection<DomainObjectTypeConfig> childConfigs = configurationExplorer.findChildDomainObjectTypes(typeName, true);

        if (childConfigs == null || childConfigs.isEmpty()) {
            return;
        }

        for (DomainObjectTypeConfig childConfig : childConfigs) {
            domainObjectQueryHelper.appendColumns(query, childConfig);
        }
    }

    private boolean isDerived(DomainObjectTypeConfig domainObjectTypeConfig) {
        return domainObjectTypeConfig.getExtendsAttribute() != null;
    }

    private RdbmsId getParentId(RdbmsId id,
            DomainObjectTypeConfig domainObjectTypeConfig) {
        if (!isDerived(domainObjectTypeConfig)) {
            return null;
        }

        int parentType = domainObjectTypeIdCache.getId(domainObjectTypeConfig
                .getExtendsAttribute());
        return new RdbmsId(parentType, id.getId());
    }

    private String getDOTypeName(RdbmsId id) {
        String result = domainObjectTypeIdCache.getName(id);
        if (result == null){
            throw new ObjectNotFoundException(id);
        }
        return result;
    }

    // CMFIVE-27416
    private String getDOTypeName(Id id) {
        if (id instanceof RdbmsId) {
            return getDOTypeName((RdbmsId)id);
        }
        return null;
    }
    // CMFIVE-27416

    private void applyOffsetAndLimitWithDefaultOrdering(StringBuilder query,
            String tableAlias, int offset, int limit) {
        // При решении задачи CMFIVE-21464 обязательно добавлялась сортировка всегда,
        // По задаче CMFIVE-36708 эта сортировка удалена для ускорения.
        // В интересов вложений порядок сортировки будет соблюден добавлением лимита (limit != 0).
        if (limit != 0) {
            query.append(" order by ").append(tableAlias).append(".").append(wrap(ID_COLUMN));
            DaoUtils.applyOffsetAndLimit(query, offset, limit);
        }
    }

    private List<Id> extractIds(List<DomainObject> domainObjectList) {
        if (domainObjectList == null || domainObjectList.isEmpty()) {
            return new ArrayList<>(0);
        }

        List<Id> result = new ArrayList<>(domainObjectList.size());
        for (DomainObject domainObject : domainObjectList) {
            result.add(domainObject.getId());
        }

        return result;
    }

    /**
     * Получение идентификатора объекта в разрезе которого получаются права на
     * сохраняемый доменный объект
     * @param domainObject
     * @return
     */
    private Id getAccessObjectId(DomainObject domainObject) {
        // Получаем матрицу и смотрим атрибут matrix_reference_field
        AccessMatrixConfig matrixConfig = null;
        // Получаем здесь тип, так как в случае наследования
        // domainObject.getTypeName() возвращает некорректный тип
        String type = domainObjectTypeIdCache.getName(domainObject.getId());
        DomainObjectTypeConfig childDomainObjectTypeConfig = configurationExplorer.getConfig(DomainObjectTypeConfig.class, type);

        // Ищим матрицу для типа с учетом иерархии типов
        while ((matrixConfig = configurationExplorer.getAccessMatrixByObjectType(childDomainObjectTypeConfig.getName())) == null
                && childDomainObjectTypeConfig.getExtendsAttribute() != null) {
            childDomainObjectTypeConfig = configurationExplorer.getConfig(DomainObjectTypeConfig.class, childDomainObjectTypeConfig.getExtendsAttribute());
        }

        // По умолчанию access_object_id равен идентификатору самого объекта
        Id result = domainObject.getId();

        // Нашли матрицу и у нее установлен атрибут matrix-reference-field,
        // вычисляем access_object_id
        if (matrixConfig != null && matrixConfig.getMatrixReference() != null) {
            if (matrixConfig.getMatrixReference().indexOf(".") > 0) {
                // TODO здесь надо добавить обработку backlink
                throw new UnsupportedOperationException("Not implemented access referencing using backlink.");
            } else {
                Id refValue = domainObject.getReference(matrixConfig.getMatrixReference());
                if (refValue == null) {
                    throw new FatalException("Field " + matrixConfig.getMatrixReference() + " mast has value. This field is matrix-reference-field");
                }

                // Вызываем рекурсивно данный метод на случай если в
                // родительском типе так же указано заимствование матрицы
                AccessToken accessToken = accessControlService
                        .createSystemAccessToken(this.getClass().getName());
                result = getAccessObjectId(find(refValue, accessToken));
            }
        }

        return result;
    }

    private UniqueKeyConfig
            findUniqueKeyConfig(String domainObjectType, List<UniqueKeyConfig> uniqueKeyConfigs, CaseInsensitiveMap<Value> uniqueKeyValuesByName) {
        Set<String> uniqueKeyNamesParams = uniqueKeyValuesByName.keySet();
        for (UniqueKeyConfig uniqueKeyConfig : uniqueKeyConfigs) {

            Set<String> uniqueKeyFieldNames = new HashSet<>();
            for (UniqueKeyFieldConfig keyFieldConfig : uniqueKeyConfig.getUniqueKeyFieldConfigs()) {
                String fieldName = Case.toLower(keyFieldConfig.getName());
                uniqueKeyFieldNames.add(fieldName);
            }

            if (uniqueKeyNamesParams.equals(uniqueKeyFieldNames)) {
                return uniqueKeyConfig;
            }
        }

        throw new IllegalArgumentException("The configuration of the domain object type \"" + domainObjectType +
                "\" has no unique key (" + Arrays.toString(uniqueKeyNamesParams.toArray()) + ")");
    }

    private class DomainObjectActionListener implements ActionListener {

        private DomainObjectsModification domainObjectsModification;

        private DomainObjectActionListener(String transactionId) {
            domainObjectsModification = new DomainObjectsModification(transactionId);
        }

        @Override
        public void onAfterCommit() {
            // Точки расширения вызываем в специальном EJB чтобы открылась новая
            // транзакция
            afterCommitExtensionPointService.afterCommit(domainObjectsModification);
        }

        public void addCreatedDomainObject(DomainObject domainObject) {
            if (isAutoCreatedDomainObject(domainObject)) {
                addModifiedAutoDomainObjectId(domainObject.getId());
                return;
            }
            domainObjectsModification.addCreatedDomainObject(cloner.fastCloneDomainObject(domainObject));
        }

        private void addModifiedAutoDomainObjectId(Id id) {
            domainObjectsModification.addModifiedAutoDomainObject(cloner.fastCloneId(id));
        }

        public void addChangeStatusDomainObject(DomainObject domainObject) {
            domainObjectsModification.addChangeStatusDomainObject(cloner.fastCloneDomainObject(domainObject));
        }

        public void addDeletedDomainObject(DomainObject domainObject) {
            if (isAutoCreatedDomainObject(domainObject)) {
                addModifiedAutoDomainObjectId(domainObject.getId());
                return;
            }
            domainObjectsModification.addDeletedDomainObject(domainObject);
        }

        public void addSavedDomainObject(DomainObject domainObject, List<FieldModification> newFields) {
            if (isAutoCreatedDomainObject(domainObject)) {
                addModifiedAutoDomainObjectId(domainObject.getId());
                return;
            }

            domainObjectsModification.addSavedDomainObject(cloner.fastCloneDomainObject(domainObject), newFields);
        }

        @Override
        public void onRollback() {
        }

        @Override
        public void onBeforeCommit() {
        }

        private boolean isAutoCreatedDomainObject(DomainObject domainObject) {
            if (configurationExplorer.isAuditLogType(domainObject.getTypeName())) {
                return true;
            }

            for (AutoCreatedType autoCreatedType : AutoCreatedType.values()) {
                if (autoCreatedType.getTypeName().equalsIgnoreCase(domainObject.getTypeName())) {
                    return true;
                }
            }

            return false;
        }
    }

    class CacheCommitNotifier implements ActionListener {
        private DomainObjectsModification domainObjectsModification;

        private CacheCommitNotifier(DomainObjectsModification domainObjectsModification) {
            this.domainObjectsModification = domainObjectsModification;
        }

        @Override
        public void onBeforeCommit() {
            // logger.warn("Before commit: " +
            // userTransactionService.getTransactionId());
        }

        @Override
        public void onAfterCommit() {
            // logger.warn("After commit: " +
            // userTransactionService.getTransactionId());
            globalCacheClient.notifyCommit(domainObjectsModification);
        }

        @Override
        public void onRollback() {
            globalCacheClient.notifyRollback(domainObjectsModification.getTransactionId());
        }
    }

    public static class DomainObjectModification{
        List<FieldModification> fieldModifications = new ArrayList<>();
        boolean stampChanged = false;

        public List<FieldModification> getFieldModifications() {
            return fieldModifications;
        }

        public void setFieldModifications(List<FieldModification> fieldModifications) {
            this.fieldModifications = fieldModifications;
        }

        public boolean isStampChanged() {
            return stampChanged;
        }

        public void setStampChanged(boolean stampChanged) {
            this.stampChanged = stampChanged;
        }
    }
}
