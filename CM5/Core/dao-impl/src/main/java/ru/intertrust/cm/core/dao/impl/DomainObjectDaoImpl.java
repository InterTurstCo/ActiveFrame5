package ru.intertrust.cm.core.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.business.api.util.MD5Utils;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.dao.access.*;
import ru.intertrust.cm.core.dao.api.*;
import ru.intertrust.cm.core.dao.api.extension.*;
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

    @Autowired
    @Qualifier("masterNamedParameterJdbcTemplate")
    private NamedParameterJdbcOperations masterJdbcTemplate;

    @Autowired
    @Qualifier("switchableNamedParameterJdbcTemplate")
    private NamedParameterJdbcOperations switchableJdbcTemplate;

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private IdGenerator idGenerator;

    private DomainObjectCacheService domainObjectCacheService;

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
        List<FieldModification>[] fieldModification = new ArrayList[1];
        fieldModification[0] = new ArrayList<FieldModification>();

        List<Id> beforeSaveInvalicContexts = dynamicGroupService.getInvalidGroupsBeforeChange(domainObject, fieldModification[0]);

        GenericDomainObject[] result = update(new DomainObject[]{domainObject}, accessToken, true, fieldModification);
        domainObjectCacheService.putOnUpdate(result[0], accessToken);

        permissionService.notifyDomainObjectChangeStatus(domainObject);
        dynamicGroupService.notifyDomainObjectChanged(domainObject, fieldModification[0], beforeSaveInvalicContexts);

        // Вызов точки расширения после смены статуса
        List<String> parentTypes = getAllParentTypes(domainObject.getTypeName());
        //Добавляем в список типов пустую строку, чтобы вызвались обработчики с неуказанным фильтром
        parentTypes.add("");
        for (String typeName : parentTypes) {
            AfterChangeStatusExtentionHandler extension = extensionService
                    .getExtentionPoint(AfterChangeStatusExtentionHandler.class, typeName);
            extension.onAfterChangeStatus(domainObject);
        }

        //Добавляем слушателя комита транзакции, чтобы вызвать точки расширения после транзакции
        DomainObjectActionListener listener = getTransactionListener();
        listener.addChangeStatusDomainObject(objectId);

        return result[0];
    }

    @Override
    public DomainObject create(DomainObject domainObject, AccessToken accessToken) {
        DomainObject[] domainObjects = createMany(new DomainObject[]{domainObject}, accessToken);
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
            refreshDynamiGroupsAndAclForCreate(createdObject);

            // Добавляем слушателя комита транзакции, чтобы вызвать точки расширения после транзакции
            DomainObjectActionListener listener = getTransactionListener();
            listener.addCreatedDomainObject(createdObject);
        }

        return createdObjects;
    }

    private List<Id> convertToIds(DomainObject[] createdObjects) {
        List<Id> idsList = new ArrayList<>();

        for (DomainObject domainObject : createdObjects) {
            idsList.add((RdbmsId) domainObject.getId());

        }
        return idsList;
    }

    private DomainObjectActionListener getTransactionListener(){
        DomainObjectActionListener listener = userTransactionService.getListener(DomainObjectActionListener.class);
        if (listener == null){
            listener = new DomainObjectActionListener(userTransactionService.getTransactionId());
            userTransactionService.addListener(listener);
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

        DomainObject[] result = saveMany(new DomainObject[]{domainObject}, accessToken);
        return result[0];
    }


    private DomainObject[] saveMany(DomainObject[] domainObjects, AccessToken accessToken)
            throws InvalidIdException, ObjectNotFoundException,
            OptimisticLockException {

        checkIfAuditLog(domainObjects);
        DomainObject result[] = null;

        //Получение измененных полей
        List<FieldModification> [] changedFields = new List[domainObjects.length];

        for (int i = 0; i < domainObjects.length; i++) {
            changedFields[i] = getModifiedFieldNames(domainObjects[i]);
        }


        // Вызов точки расширения до сохранения
        List<String> parentTypes = getAllParentTypes(domainObjects[0].getTypeName());
        //Добавляем в список типов пустую строку, чтобы вызвались обработчики с неуказанным фильтром
        parentTypes.add("");
        for (int i = 0; i < domainObjects.length; i++) {
            for (String typeName : parentTypes) {
                BeforeSaveExtensionHandler beforeSaveExtension = extensionService
                        .getExtentionPoint(BeforeSaveExtensionHandler.class, typeName);
                beforeSaveExtension.onBeforeSave(domainObjects[i], changedFields[i]);
            }
        }

        DomainObjectVersion.AuditLogOperation operation = null;

        // Сохранение в базе
        if (domainObjects[0].isNew()) {
            result = createMany(domainObjects, accessToken);
            operation = DomainObjectVersion.AuditLogOperation.CREATE;
        } else {

            List<Id>[] beforeChangeInvalidGroups = getBeforeChangeInvalidGroups(domainObjects, changedFields);
            result = update(domainObjects, accessToken, changedFields);
            refreshDynamicGroupsAndAclForUpdate(domainObjects, changedFields, beforeChangeInvalidGroups);

            operation = DomainObjectVersion.AuditLogOperation.UPDATE;
        }

        for (int i = 0; i < result.length; i++) {
            String auditLogTableName = DataStructureNamingHelper.getALTableSqlName(domainObjects[i].getTypeName());
            Integer auditLogType = domainObjectTypeIdCache.getId(auditLogTableName);

            // Запись в auditLog
            createAuditLog(result[i], result[i].getTypeName(),
                    auditLogType, accessToken, operation);

            // Вызов точки расширения после сохранения
            for (String typeName : parentTypes) {
                AfterSaveExtensionHandler afterSaveExtension = extensionService
                        .getExtentionPoint(AfterSaveExtensionHandler.class, typeName);
                afterSaveExtension.onAfterSave(result[i], changedFields[i]);
            }

            //Добавляем слушателя комита транзакции, чтобы вызвать точки расширения после транзакции
            DomainObjectActionListener listener = getTransactionListener();
            listener.addSavedDomainObject(result[i], changedFields[i]);

        }

        return result;
    }

    private List<Id>[] getBeforeChangeInvalidGroups(DomainObject[] domainObjects, List<FieldModification>[] changedFields) {
        List<Id> beforeChangeInvalidGroups[] = new List[domainObjects.length];

        for (int i = 0; i < domainObjects.length; i++) {
            beforeChangeInvalidGroups[i] = dynamicGroupService.getInvalidGroupsBeforeChange(domainObjects[i], changedFields[i]);
        }
        return beforeChangeInvalidGroups;
    }

    private void
    refreshDynamicGroupsAndAclForUpdate(DomainObject[] domainObjects, List<FieldModification>[] changedFields, List<Id>[] beforeChangeInvalidGroups) {
        for (int i = 0; i < domainObjects.length; i++) {
            refreshDynamiGroupsAndAclForUpdate(domainObjects[i], changedFields[i], beforeChangeInvalidGroups[i]);
        }
    }

    /**
     * Проверяет, являются ли переданные объекты аудит логом. Так как передается массив однотипных объектов,
     * то проверяется тип первого объекта. Если передан аудит лог объект - выбрасывается исключение.
     * @param domainObjects
     */
    private void checkIfAuditLog(DomainObject[] domainObjects) {
        if (domainObjects[0] != null && configurationExplorer.isAuditLogType(domainObjects[0].getTypeName())) {
            throw new FatalException("It is not allowed to modify Audit Log using CRUD service, table: " + domainObjects[0].getTypeName());
        }
    }

    @Override
    public List<DomainObject> save(List<DomainObject> domainObjects, AccessToken accessToken) {

        List<DomainObject> result = new ArrayList<>();

        List<List<DomainObject>> groupObjectsByType = groupObjectsByType(domainObjects);
        for (List<DomainObject> groupObjects : groupObjectsByType) {
            DomainObject[] newDomainObjects = saveMany(
                    groupObjects.toArray(new DomainObject[groupObjects.size()]), accessToken);
            result.addAll(Arrays.asList(newDomainObjects));
        }
        return result;

    }

    private DomainObject[]
    update(DomainObject[] domainObjects, AccessToken accessToken, List<FieldModification>[] changedFields)
            throws InvalidIdException, ObjectNotFoundException,
            OptimisticLockException {

        for (DomainObject domainObject : domainObjects) {
            accessControlService.verifyAccessToken(accessToken, domainObject.getId(), DomainObjectAccessType.WRITE);
        }

        boolean isUpdateStatus = false;

        GenericDomainObject[] updatedObjects = update(domainObjects, accessToken, isUpdateStatus, changedFields);

        for (GenericDomainObject updatedObject : updatedObjects) {
            domainObjectCacheService.putOnUpdate(updatedObject, accessToken);
        }

        return updatedObjects;

    }

    private GenericDomainObject[] update(DomainObject[] domainObjects, AccessToken accessToken, boolean isUpdateStatus,
                                         List<FieldModification>[] changedFields) {

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

        List<Id> beforeChangeInvalidGroups [] = new List[domainObjects.length];

        for (int i = 0; i < domainObjects.length; i++) {
            beforeChangeInvalidGroups[i] = dynamicGroupService.getInvalidGroupsBeforeChange(domainObjects[i], changedFields[i]);
        }

        DomainObject[] parentDOs =
                updateParentDO(domainObjectTypeConfig, domainObjects, accessToken, isUpdateStatus, changedFields);

        String query = generateUpdateQuery(domainObjectTypeConfig, isUpdateStatus);

        Date currentDate = new Date();
        // В случае если сохранялся родительский объект то берем дату
        // модификации из нее, иначе в базе и возвращаемом доменном объекте
        // будут различные даты изменения и изменение объект отвалится по ошибке
        // OptimisticLockException
        if (parentDOs != null) {
            currentDate = parentDOs[0].getModifiedDate();
        }

        if (query != null) {

            Map<String, Object>[] parameters = new Map[domainObjects.length];
            for (int i = 0; i < updatedObjects.length; i++) {
                parameters[i] = initializeUpdateParameters(
                        updatedObjects[i], domainObjectTypeConfig, accessToken, currentDate, isUpdateStatus);
            }

            int[] count = masterJdbcTemplate.batchUpdate(query, parameters);

            for (int i = 0; i < updatedObjects.length; i++) {
                if (count[i] == 0 && (!exists(updatedObjects[i].getId()))) {
                    throw new ObjectNotFoundException(updatedObjects[i].getId());
                }

                if (!isDerived(domainObjectTypeConfig)) {
                    if (count[i] == 0) {
                        throw new OptimisticLockException(updatedObjects[i]);
                    }
                }

            }
        }

        for (int i = 0; i < updatedObjects.length; i++) {

            updatedObjects[i].setModifiedDate(currentDate);
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
                                                    List<Id> beforeChangeInvalicContexts) {
        dynamicGroupService.notifyDomainObjectChanged(domainObject, modifiedFields, beforeChangeInvalicContexts);
        permissionService.notifyDomainObjectChanged(domainObject, modifiedFields);
    }

    private List<FieldModification> getModifiedFieldNames(
            DomainObject domainObject) {

        List<FieldModification> modifiedFieldNames = new ArrayList<FieldModification>();

        //Для нового объекта все поля отличные от null попадают в список измененных
        if (domainObject.isNew()) {
            for (String fieldName : domainObject.getFields()) {
                Value<?> newValue = domainObject.getValue(fieldName);
                if (newValue != null){
                    modifiedFieldNames.add(new FieldModificationImpl(fieldName, null, newValue));
                }
            }
        } else {
            //для ранее созданного объекта получаем объект не сохраненный из хранилища и вычисляем измененные поля
            AccessToken accessToken = createSystemAccessToken();
            DomainObject originalDomainObject = find(domainObject.getId(),
                    accessToken);

            for (String fieldName : domainObject.getFields()) {
                Value originalValue = originalDomainObject.getValue(fieldName);
                Value newValue = domainObject.getValue(fieldName);
                if (isValueChanged(originalValue, newValue)) {
                    modifiedFieldNames.add(new FieldModificationImpl(fieldName,
                            originalValue, newValue));
                }

            }
        }
        return modifiedFieldNames;
    }

    private boolean isValueChanged(Value originalValue, Value newValue) {
        if (newValue == null && originalValue == null) {
            return false;
        }

        if (newValue != null && originalValue == null) {
            return true;
        }
        return originalValue != null && !originalValue.equals(newValue);
    }

    @Override
    public void delete(Id id, AccessToken accessToken) throws InvalidIdException,
            ObjectNotFoundException {
        String domainObjectType = domainObjectTypeIdCache.getName(id);
        if (configurationExplorer.isAuditLogType(domainObjectType)) {
            throw new FatalException("It is not allowed to delete Audit Log using CRUD service, table: " + domainObjectType);
        }
        deleteMany(new Id[]{id}, accessToken, false);
    }

    private int deleteMany(Id[] ids, AccessToken accessToken, boolean ignoreObjectNotFound) throws InvalidIdException,
            ObjectNotFoundException {

        checkIfAuditLog(ids);

        for (Id id : ids) {
            validateIdType(id);
            accessControlService.verifyAccessToken(accessToken, id, DomainObjectAccessType.DELETE);
        }


        RdbmsId firstRdbmsId = (RdbmsId) ids[0];
        DomainObjectTypeConfig domainObjectTypeConfig = configurationExplorer
                .getConfig(DomainObjectTypeConfig.class, getDOTypeName(firstRdbmsId.getTypeId()));

        // Получаем удаляемый доменный объект для вызова точек расширения и пересчета динамических групп. Чтение объекта
        // идет от имени системы, т.к. прав на чтение может не быть у пользователя.
        AccessToken systemAccessToken = createSystemAccessToken();

        DomainObject[] deletedObjects = new DomainObject[ids.length];
        Map<Id, List<String>> objectsParentTypes = new HashMap<Id, List<String>>();
        int i = 0;
        for (Id id : ids) {
            DomainObject deletedObject = find(id, systemAccessToken);
            deletedObjects[i++] = deletedObject;
            //Прверка наличия доменного объекта
            if (deletedObject == null){        
                //Если взведен флаг игнорировать отсутствие ДО то пропускаем идентификатор, иначе бросаем исключение
                if (ignoreObjectNotFound){
                    continue;
                }else{
                    throw new ObjectNotFoundException(id);
                }
            }
            List<Id> beforeChangeInvalidGroups = dynamicGroupService.getInvalidGroupsBeforeDelete(deletedObject);

            // Точка расширения до удаления
            List<String> parentTypes = getAllParentTypes(domainObjectTypeConfig.getName());
            //Добавляем в список типов пустую строку, чтобы вызвались обработчики с неуказанным фильтром
            parentTypes.add("");
            for (String typeName : parentTypes) {
                BeforeDeleteExtensionHandler beforeDeleteEH = extensionService
                        .getExtentionPoint(BeforeDeleteExtensionHandler.class, typeName);
                beforeDeleteEH.onBeforeDelete(deletedObject);
            }
            objectsParentTypes.put(id, parentTypes);

            //Пересчет прав непосредственно перед удалением объекта из базы, чтобы не нарушать целостность данных
            refreshDynamiGroupsAndAclForDelete(deletedObject, beforeChangeInvalidGroups);
        }

        //непосредственно удаление из базыы
        int deleted = internalDelete(ids, ignoreObjectNotFound);

        //Удалене из кэша
        for (Id id : ids) {
            domainObjectCacheService.evict(id);
        }

        // Пишем в аудит лог
        for (DomainObject deletedObject : deletedObjects) {
            if (deletedObject == null){
                continue;
            }
            String auditLogTableName = DataStructureNamingHelper.getALTableSqlName(deletedObject.getTypeName());
            Integer auditLogType = domainObjectTypeIdCache.getId(auditLogTableName);

            createAuditLog(deletedObject, deletedObject.getTypeName(), auditLogType, accessToken, DomainObjectVersion.AuditLogOperation.DELETE);
        }

        // Точка расширения после удаления, вызывается с установкой фильтра текущего типа и всех наследников
        for (DomainObject deletedObject : deletedObjects) {
            if (deletedObject == null) {
                continue;
            }

            for (String typeName : objectsParentTypes.get(deletedObject.getId())) {
                AfterDeleteExtensionHandler afterDeleteEH = extensionService.getExtentionPoint(AfterDeleteExtensionHandler.class, typeName);

                afterDeleteEH.onAfterDelete(deletedObject);

                //Добавляем слушателя коммита транзакции, чтобы вызвать точки расширения после транзакции
                DomainObjectActionListener listener = getTransactionListener();
                listener.addDeletedDomainObject(deletedObject);
            }
        }

        return deleted;
    }

    /**
     * Проверяет, являются ли переданные объекты аудит логом. Так как передается массив однотипных объектов,
     * топроверяется тип первого объекта. Если передан аудит лог объект - выбрасывается исключение.
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
     * Получение всей цепочки родительских типов начиная от переданноготв параметре
     * @param name
     * @return
     */
    private List<String> getAllParentTypes(String name) {
        List<String> result = new ArrayList<String>();
        result.add(name);

        DomainObjectTypeConfig domainObjectTypeConfig = configurationExplorer
                .getConfig(DomainObjectTypeConfig.class, name);
        if (domainObjectTypeConfig.getExtendsAttribute() != null) {
            result.addAll(getAllParentTypes(domainObjectTypeConfig.getExtendsAttribute()));
        }

        return result;
    }


    /**
     * Удаление объекта из базяы
     * @param deletedIds
     */
    private int internalDelete(Id [] deletedIds, boolean ignoreObjectNotFound) {
        RdbmsId rdbmsId = (RdbmsId) deletedIds[0];

        DomainObjectTypeConfig domainObjectTypeConfig = configurationExplorer
                .getConfig(DomainObjectTypeConfig.class,
                        getDOTypeName(rdbmsId.getTypeId()));

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
            if (!ignoreObjectNotFound){
                throw new ObjectNotFoundException(rdbmsId);
            }
        }

        // Удаление родительского объекта, перенесено ниже удаления дочернего
        // объекта для того чтобы не ругались foreign key
        Id [] parentIds = new Id[deletedIds.length];
        for (int j = 0; j < parentIds.length; j++) {
            Id parentId = getParentId((RdbmsId) deletedIds[j], domainObjectTypeConfig);
            if (parentId == null) return count;
            parentIds[j] = parentId;
        }

        internalDelete(parentIds, ignoreObjectNotFound);

        return count;
    }

    private void refreshDynamiGroupsAndAclForDelete(DomainObject deletedObject, List<Id> beforeChangeInvalicContexts) {
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
     * группирует доменные объекты по типам и типу операции (create/update) в порядке следования
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

        RdbmsId rdbmsId = (RdbmsId) id;
        validateIdType(id);

        StringBuilder query = new StringBuilder();
        query.append(generateExistsQuery(getDOTypeName(rdbmsId.getTypeId())));

        Map<String, Object> parameters = initializeExistsParameters(id);
        long total = switchableJdbcTemplate.queryForObject(query.toString(), parameters,
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

        RdbmsId rdbmsId = (RdbmsId) id;
        String typeName = getDOTypeName(rdbmsId.getTypeId());

        String query = domainObjectQueryHelper.generateFindQuery(typeName, accessToken, false);
        Map<String, Object> parameters = domainObjectQueryHelper.initializeParameters(rdbmsId, accessToken);

        DomainObject result = switchableJdbcTemplate.query(query, parameters,
                new SingleObjectRowMapper(typeName, configurationExplorer, domainObjectTypeIdCache));

        if (result != null) {
            domainObjectCacheService.putOnRead(result, accessToken);
            eventLogService.logAccessDomainObjectEvent(result.getId(), EventLogService.ACCESS_OBJECT_READ, true);
        } else if (eventLogService.isAccessDomainObjectEventEnabled(id, EventLogService.ACCESS_OBJECT_READ, false)) {
            if (exists(id)) {
                eventLogService.logAccessDomainObjectEvent(id, EventLogService.ACCESS_OBJECT_READ, false);
            }
        }

        return result;
    }

    @Override
    public DomainObject findAndLock(Id id, AccessToken accessToken) {
        if (id == null) {
            throw new IllegalArgumentException("Object id can not be null");
        }

        accessControlService.verifyAccessToken(accessToken, id, DomainObjectAccessType.WRITE);

        RdbmsId rdbmsId = (RdbmsId) id;
        String typeName = getDOTypeName(rdbmsId.getTypeId());

        String query = domainObjectQueryHelper.generateFindQuery(typeName, accessToken, true);
        Map<String, Object> parameters = domainObjectQueryHelper.initializeParameters(rdbmsId, accessToken);

        DomainObject result = masterJdbcTemplate.query(query, parameters, new SingleObjectRowMapper(
                typeName, configurationExplorer, domainObjectTypeIdCache));

        if (result != null) {
            domainObjectCacheService.putOnRead(result, accessToken);
            eventLogService.logAccessDomainObjectEvent(result.getId(), EventLogService.ACCESS_OBJECT_READ, true);
        } else if (eventLogService.isAccessDomainObjectEventEnabled(id, EventLogService.ACCESS_OBJECT_READ, false)) {
            if (exists(id)) {
                eventLogService.logAccessDomainObjectEvent(id, EventLogService.ACCESS_OBJECT_READ, false);
            }
        }

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

        String[] cacheKey = new String[] { domainObjectType,
                String.valueOf(offset),String.valueOf(limit) };
        List<DomainObject> result = domainObjectCacheService.getAll(accessToken, cacheKey);
        if (result != null) {
            return result;
        }

        String query = generateFindAllQuery(domainObjectType, exactType, offset, limit, accessToken);

        Map<String, Object> parameters = domainObjectQueryHelper.initializeParameters(accessToken);
        if (exactType) {
            parameters.put(RESULT_TYPE_ID, domainObjectTypeIdCache.getId(domainObjectType));
        }

        result = switchableJdbcTemplate.query(query, parameters,
                new MultipleObjectRowMapper(domainObjectType,
                        configurationExplorer, domainObjectTypeIdCache));

        domainObjectCacheService.putAllOnRead(result, accessToken, cacheKey);

        eventLogService.logAccessDomainObjectEventByDo(result, EventLogService.ACCESS_OBJECT_READ, true);

        return result;
    }

    private AccessToken createSystemAccessToken() {
        return accessControlService.createSystemAccessToken("DomainObjectDaoImpl");
    }

    @Override
    public List<DomainObject> find(List<Id> ids, AccessToken accessToken) {
        if (ids == null || ids.size() == 0) {
            return new ArrayList<>();
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
        List<DomainObject> readDomainObjects;
        if (!idsToRead.isEmpty()) {
            String tableAlias = getSqlAlias(typeName);
            String query = domainObjectQueryHelper.generateMultiObjectFindQuery(typeName, accessToken, false);
            Map<String, Object> parameters = domainObjectQueryHelper.initializeParameters(new ArrayList<Id>(idsToRead), accessToken);
            readDomainObjects = switchableJdbcTemplate.query(query, parameters,
                    new MultipleObjectRowMapper(domainObjectType, configurationExplorer, domainObjectTypeIdCache));
            domainObjectCacheService.putAllOnRead(readDomainObjects, accessToken);
        } else {
            readDomainObjects = new ArrayList<>(0);
        }

        if (cachedDomainObjects == null) {
            return readDomainObjects;
        } else {
            List result = cachedDomainObjects;
            result.addAll(readDomainObjects);
            return result;
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
        return findLinkedDomainObjects(domainObjectId, linkedType,  linkedField, false, offset, limit, accessToken);
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
            String[] cacheKey = new String[] {linkedType, linkedField};
            List<DomainObject> domainObjects =
                    domainObjectCacheService.getAll(domainObjectId, accessToken, cacheKey);

            if (domainObjects != null) {
                return domainObjects;
            }
        }

        Map<String, Object> parameters = domainObjectQueryHelper.initializeParameters(accessToken);
        parameters.put(PARAM_DOMAIN_OBJECT_ID, ((RdbmsId) domainObjectId).getId());
        parameters.put(PARAM_DOMAIN_OBJECT_TYPE_ID, ((RdbmsId) domainObjectId).getTypeId());
        if (exactType) {
            parameters.put(RESULT_TYPE_ID, domainObjectTypeIdCache.getId(linkedType));
        }

        String query = buildFindChildrenQuery(linkedType, linkedField, exactType, offset, limit, accessToken);

        List<DomainObject> domainObjects = switchableJdbcTemplate.query(query, parameters,
                new MultipleObjectRowMapper(linkedType, configurationExplorer, domainObjectTypeIdCache));

        if (domainObjects == null) {
            domainObjects = new ArrayList<>();
        }

        for (DomainObject domainObject : domainObjects) {
            domainObjectCacheService.putOnRead(domainObject, accessToken);
        }

        if (linkedDomainObjectCacheEnabled) {
            String[] cacheKey = new String[] {linkedType, linkedField};
            domainObjectCacheService.putAllOnRead(domainObjectId, domainObjects, accessToken, cacheKey);
        }

        eventLogService.logAccessDomainObjectEventByDo(domainObjects, EventLogService.ACCESS_OBJECT_READ, true);

        return domainObjects;
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
            String[] cacheKey = new String[] { linkedType,linkedField,
                    String.valueOf(offset),String.valueOf(limit) };

            List<DomainObject> domainObjects =
                    domainObjectCacheService.getAll(domainObjectId, accessToken, cacheKey);

            if (domainObjects != null) {
                return extractIds(domainObjects);
            }
        }

        Map<String, Object> parameters = domainObjectQueryHelper.initializeParameters(accessToken);
        parameters.put(PARAM_DOMAIN_OBJECT_ID, ((RdbmsId) domainObjectId).getId());
        parameters.put(PARAM_DOMAIN_OBJECT_TYPE_ID, ((RdbmsId) domainObjectId).getTypeId());
        if (exactType) {
            parameters.put(RESULT_TYPE_ID, domainObjectTypeIdCache.getId(linkedType));
        }

        String query = buildFindChildrenIdsQuery(linkedType, linkedField, exactType, offset, limit, accessToken);

        return switchableJdbcTemplate.query(query, parameters, new MultipleIdRowMapper(linkedType));
    }

    @Override
    public DomainObject findByUniqueKey(String domainObjectType, Map<String, Value> uniqueKeyValuesByName, AccessToken accessToken) {
        DomainObject result = domainObjectCacheService.get(domainObjectType, uniqueKeyValuesByName, accessToken);
        if (result != null) {
            return result;
        }

        result = retrieveWithoutLoggingByUniqueKey(domainObjectType, uniqueKeyValuesByName, accessToken, false);

        if (result != null) {
            eventLogService.logAccessDomainObjectEvent(result.getId(), EventLogService.ACCESS_OBJECT_READ, true);
        } else {
            // Проверяем существование доменного объекта с уникальным ключом и логируем доступ
            DomainObject domainObject = retrieveWithoutLoggingByUniqueKey(domainObjectType, uniqueKeyValuesByName,
                    createSystemAccessToken(), false);
            if (domainObject != null && eventLogService.isAccessDomainObjectEventEnabled(domainObject.getId(), EventLogService.ACCESS_OBJECT_READ, false)) {
                eventLogService.logAccessDomainObjectEvent(domainObject.getId(), EventLogService.ACCESS_OBJECT_READ, false);
            }
        }

        return result;
    }

    @Override
    public DomainObject finAndLockByUniqueKey(String domainObjectType, Map<String, Value> uniqueKeyValuesByName, AccessToken accessToken) {
        DomainObject result = retrieveWithoutLoggingByUniqueKey(domainObjectType, uniqueKeyValuesByName, accessToken, true);

        if (result != null) {
            eventLogService.logAccessDomainObjectEvent(result.getId(), EventLogService.ACCESS_OBJECT_READ, true);
        } else {
            // Проверяем существование доменного объекта с уникальным ключом и логируем доступ
            DomainObject domainObject = retrieveWithoutLoggingByUniqueKey(domainObjectType, uniqueKeyValuesByName, createSystemAccessToken(), false);
            if (domainObject != null && eventLogService.isAccessDomainObjectEventEnabled(domainObject.getId(), EventLogService.ACCESS_OBJECT_READ, false)) {
                eventLogService.logAccessDomainObjectEvent(domainObject.getId(), EventLogService.ACCESS_OBJECT_READ, false);
            }
        }

        return result;
    }

    protected DomainObject retrieveWithoutLoggingByUniqueKey(String domainObjectType, Map<String, Value> uniqueKeyValuesByName,
                                                             AccessToken accessToken, boolean lock) {
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
            String name = uniqueKeyFieldConfig.getName().toLowerCase();

            Value value = uniqueKeyValues.get(name);
            parameters.put(name, value.get());
        }

        DomainObject result = switchableJdbcTemplate.query(query, parameters,
                new SingleObjectRowMapper(domainObjectType, configurationExplorer, domainObjectTypeIdCache));

        if (result != null) {
            domainObjectCacheService.putOnRead(result, accessToken);
        }

        return result;
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

            parameters.put("access_object_id", ((RdbmsId) getAccessObjectId(domainObject)).getId());

        }
        parameters.put("type_id", type);

        List<FieldConfig> feldConfigs = domainObjectTypeConfig
                .getDomainObjectFieldsConfig().getFieldConfigs();

        initializeDomainParameters(domainObject, feldConfigs, parameters);

        return parameters;
    }

    /**
     * Возвращает текущего пользователя. Если вызов идет от имени системы, то возвращает null, иначе возвращается
     * текущий пользователь из EJB контекста.
     * @param accessToken
     * @return
     */
    private Id getCurrentUser(AccessToken accessToken) {
        return currentUserAccessor.getCurrentUserId();
    }

    /**
     * Создает SQL запрос для нахождения всех доменных объектов определенного типа
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
        appendColumnsQueryPart(query, typeName);
        if (!exactType) {
            appendChildColumns(query, typeName);
        }

        query.append(" from ");

        appendTableNameQueryPart(query, typeName);
        if (!exactType) {
            appendChildTables(query, typeName);
        }

        query.append(" where 1=1");

        if (exactType) {
            query.append(" and ").append(tableAlias).append(".").append(wrap(TYPE_COLUMN)).append(" = :").append(RESULT_TYPE_ID);
        }

        Id personId = currentUserAccessor.getCurrentUserId();
        boolean isAdministratorWithAllPermissions = isAdministratorWithAllPermissions(personId, typeName);

        if (accessToken.isDeferred() && !(configurationExplorer.isReadPermittedToEverybody(typeName) || isAdministratorWithAllPermissions)) {

            // Проверка прав для аудит лог объектов выполняются от имени родительского объекта.
            typeName = domainObjectQueryHelper.getRelevantType(typeName);
            //В случае заимствованных прав формируем запрос с "чужой" таблицей xxx_read
            String matrixReferenceTypeName = configurationExplorer.getMatrixReferenceTypeName(typeName);
            String aclReadTable = null;
            if (matrixReferenceTypeName != null){
                aclReadTable = AccessControlUtility.getAclReadTableNameFor(configurationExplorer, matrixReferenceTypeName);
            }else{
                aclReadTable = AccessControlUtility.getAclReadTableNameFor(configurationExplorer, typeName);
            }

            String rootType = configurationExplorer.getDomainObjectRootType(typeName).toLowerCase();

            query.append(" and ");

            query.append("exists (select a.").append(wrap("object_id")).append(" from ").append(wrap(aclReadTable)).append(" a");
            query.append(" inner join ").append(wrap("group_group")).append(" gg on a.").append(wrap("group_id"))
                    .append(" = gg.").append(wrap("parent_group_id"));
            query.append(" inner join ").append(wrap("group_member")).append(" gm on gg.")
                    .append(wrap("child_group_id")).append(" = gm.").append(wrap("usergroup"));
            query.append("inner join ").append(DaoUtils.wrap(rootType)).append(" rt on a.")
                    .append(DaoUtils.wrap("object_id"))
                    .append(" = rt.").append(DaoUtils.wrap("access_object_id"));
            query.append(" where gm.").append(wrap("person_id")).append(" = :user_id and ").
                    append(rootType).append(".").append(wrap("id")).append(" = ").append(tableAlias).append(".").append(wrap("id")).append(")");
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
    protected String generateUpdateQuery(
            DomainObjectTypeConfig domainObjectTypeConfig, boolean isUpdateStatus) {

        StringBuilder query = new StringBuilder();

        String tableName = getSqlName(domainObjectTypeConfig);

        List<FieldConfig> feldConfigs = domainObjectTypeConfig
                .getDomainObjectFieldsConfig().getFieldConfigs();

        feldConfigs = removeImmutableFields(feldConfigs);

        List<String> columnNames = DataStructureNamingHelper
                .getColumnNames(feldConfigs);

        String fieldsWithparams = DaoUtils
                .generateCommaSeparatedListWithParams(columnNames);

        if (isDerived(domainObjectTypeConfig) && fieldsWithparams.isEmpty()) {
            return null;
        }
        query.append("update ").append(wrap(tableName)).append(" set ");

        if (!isDerived(domainObjectTypeConfig)) {
            query.append(wrap(UPDATED_DATE_COLUMN)).append("=:current_date, ");
            query.append(wrap(UPDATED_BY)).append("=:updated_by, ");
            query.append(wrap(UPDATED_BY_TYPE_COLUMN)).append("=:updated_by_type, ");

            if (isUpdateStatus) {
                query.append(wrap(STATUS_FIELD_NAME)).append("=:status, ");
            }

        }

        query.append(fieldsWithparams);
        query.append(" where ").append(wrap(ID_COLUMN)).append("=:id");

        if (!isDerived(domainObjectTypeConfig)) {
            query.append(" and ").append(wrap(UPDATED_DATE_COLUMN)).append("=:updated_date");
        }

        return query.toString();

    }

    private List<FieldConfig> removeImmutableFields(List<FieldConfig> fieldConfigs) {
        List<FieldConfig> ret = new ArrayList<>(fieldConfigs.size());
        for (FieldConfig fieldConfig : fieldConfigs) {
            if (!fieldConfig.isImmutable()) ret.add(fieldConfig);
        }
        return ret;
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

        Map<String, Object> parameters = new HashMap<String, Object>();

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
        List<FieldConfig> fieldConfigs = domainObjectTypeConfig
                .getDomainObjectFieldsConfig().getFieldConfigs();

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
    protected String generateCreateQuery(
            DomainObjectTypeConfig domainObjectTypeConfig) {
        List<FieldConfig> fieldConfigs = domainObjectTypeConfig
                .getFieldConfigs();

        String tableName = getSqlName(domainObjectTypeConfig);
        List<String> columnNames = DataStructureNamingHelper
                .getColumnNames(fieldConfigs);

        String commaSeparatedColumns =
                new DelimitedListFormatter<String>().formatAsDelimitedList(columnNames, ", ", "\"");
        String commaSeparatedParameters = DaoUtils
                .generateCommaSeparatedParameters(columnNames);

        StringBuilder query = new StringBuilder();
        query.append("insert into ").append(wrap(tableName)).append(" (").append(wrap(ID_COLUMN)).append(", ");
        query.append(wrap(TYPE_COLUMN));

        if (!isDerived(domainObjectTypeConfig)) {
            query.append(", ");
            query.append(wrap(CREATED_DATE_COLUMN)).append(", ")
                    .append(wrap(UPDATED_DATE_COLUMN)).append(", ");

            query.append(wrap(CREATED_BY)).append(", ")
                    .append(wrap(CREATED_BY_TYPE_COLUMN)).append(", ");

            query.append(wrap(UPDATED_BY)).append(", ")
                    .append(wrap(UPDATED_BY_TYPE_COLUMN)).append(", ");

            query.append(wrap(STATUS_FIELD_NAME)).append(", ")
                    .append(wrap(STATUS_TYPE_COLUMN)).append(", ");

            query.append(wrap(ACCESS_OBJECT_ID));
        }

        if (commaSeparatedColumns.length() > 0) {
            query.append(", ").append(commaSeparatedColumns);
        }

        query.append(") values (:id , :type_id");

        if (!isDerived(domainObjectTypeConfig)) {
            query.append(", :created_date, :updated_date, :created_by, :created_by_type, :updated_by, :updated_by_type, :status, :status_type, :access_object_id");
        }

        if (commaSeparatedParameters.length() > 0) {
            query.append(", ").append(commaSeparatedParameters);
        }

        query.append(")");

        return query.toString();

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

        RdbmsId rdbmsId = (RdbmsId) id;
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("id", rdbmsId.getId());

        return parameters;
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
                continue;
            }

            // хэшируем Encrypted строки
            if (fieldConfig instanceof StringFieldConfig){
                StringFieldConfig stringFieldConfig = (StringFieldConfig) fieldConfig;
                if (stringFieldConfig.getEncrypted() != null
                        && stringFieldConfig.getEncrypted()){
                    String str = (String) value.get();
                    if (wasUpdated(domainObject, stringFieldConfig, str)){
                        value = new StringValue(MD5Utils.getMD5AsHex(str));
                    }
                }
            }

            setParameter(parameterName, value, parameters);
        }
    }

    private boolean wasUpdated(DomainObject domainObject, StringFieldConfig stringFieldConfig, String str) {

        if (domainObject.isNew()) return true;

        AccessToken accessToken = createSystemAccessToken();
        DomainObject originalDomainObject = find(domainObject.getId(), accessToken);
        if (originalDomainObject == null) return true;

        String originalString = originalDomainObject.getString(stringFieldConfig.getName());

        if (str == null) return originalString != null;

        return !str.equals(originalString);

    }

    protected String buildFindChildrenQuery(String linkedType, String linkedField, boolean exactType,
                                            int offset, int limit, AccessToken accessToken) {
        String tableAlias = getSqlAlias(linkedType);
        String tableHavingLinkedFieldAlias = getSqlAlias(findInHierarchyDOTypeHavingField(linkedType, linkedField));

        StringBuilder query = new StringBuilder("select ");
        appendColumnsQueryPart(query, linkedType);
        if (!exactType) {
            appendChildColumns(query, linkedType);
        }

        query.append(" from ");

        appendTableNameQueryPart(query, linkedType);
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
            appendAccessControlLogicToQuery(query, linkedType);
        }

        applyOffsetAndLimitWithDefaultOrdering(query, tableAlias, offset, limit);

        return query.toString();
    }

    protected String buildFindChildrenIdsQuery(String linkedType, String linkedField, boolean exactType,
                                               int offset, int limit, AccessToken accessToken) {
        String doTypeHavingLinkedField = findInHierarchyDOTypeHavingField(linkedType, linkedField);
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
            appendAccessControlLogicToQuery(query, linkedType);
        }

        applyOffsetAndLimitWithDefaultOrdering(query, tableAlias, offset, limit);

        return query.toString();
    }

    private void appendAccessControlLogicToQuery(StringBuilder query,
                                                 String linkedType) {
        boolean isAuditLog = configurationExplorer.isAuditLogType(linkedType);
        String originalLinkedType = DataStructureNamingHelper.getSqlName(linkedType);

        // Проверка прав для аудит лог объектов выполняются от имени родительского объекта.        
        linkedType = domainObjectQueryHelper.getRelevantType(linkedType);

        Id personId = currentUserAccessor.getCurrentUserId();
        boolean isAdministratorWithAllPermissions = isAdministratorWithAllPermissions(personId, linkedType);

        //Добавляем учет ReadPermittedToEverybody
        if (!(configurationExplorer.isReadPermittedToEverybody(linkedType) || isAdministratorWithAllPermissions)) {
            // Проверка прав для аудит лог объектов выполняются от имени родительского объекта.
            linkedType = domainObjectQueryHelper.getRelevantType(linkedType);
            //В случае заимствованных прав формируем запрос с "чужой" таблицей xxx_read
            String matrixReferenceTypeName = configurationExplorer.getMatrixReferenceTypeName(linkedType);
            String childAclReadTable = null;
            if (matrixReferenceTypeName != null){
                childAclReadTable = AccessControlUtility.getAclReadTableNameFor(configurationExplorer, matrixReferenceTypeName);
            }else{
                childAclReadTable = AccessControlUtility.getAclReadTableNameFor(configurationExplorer, linkedType);
            }
            String topLevelParentType = ConfigurationExplorerUtils.getTopLevelParentType(configurationExplorer, linkedType);
            String topLevelAuditTable = getALTableSqlName(topLevelParentType);

            String rootType = configurationExplorer.getDomainObjectRootType(linkedType).toLowerCase();

            query.append(" and exists (select r." + wrap("object_id") + " from ").append(wrap(childAclReadTable)).append(" r ");

            query.append(" inner join ").append(DaoUtils.wrap("group_group")).append(" gg on r.").append(DaoUtils.wrap("group_id"))
                    .append(" = gg.").append(DaoUtils.wrap("parent_group_id"));
            query.append(" inner join ").append(DaoUtils.wrap("group_member")).append(" gm on gg.")
                    .append(DaoUtils.wrap("child_group_id")).append(" = gm.").append(DaoUtils.wrap("usergroup"));
            query.append("inner join ").append(DaoUtils.wrap(rootType)).append(" rt on r.")
                    .append(DaoUtils.wrap("object_id"))
                    .append(" = rt.").append(DaoUtils.wrap("access_object_id"));
            if (isAuditLog) {
                query.append(" inner join ").append(wrap(topLevelAuditTable)).append(" pal on ").append(originalLinkedType).append(".")
                        .append(wrap(Configuration.ID_COLUMN)).append(" = pal.").append(wrap(Configuration.ID_COLUMN));
            }

            query.append("where gm.").append(wrap("person_id")).append(" = :user_id and rt.").append(wrap("id")).append(" = ");
            if (!isAuditLog) {
                query.append(originalLinkedType).append(".").append(DaoUtils.wrap(ID_COLUMN));

            } else {
                query.append(topLevelAuditTable).append(".").append(DaoUtils.wrap(Configuration.DOMAIN_OBJECT_ID_COLUMN));
            }
            query.append(")");
        }
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
            if (parentDOs != null) {
                updatedObjects[i].setCreatedDate(parentDOs[i].getCreatedDate());
                updatedObjects[i].setModifiedDate(parentDOs[i].getModifiedDate());
            } else {
                Date currentDate = new Date();
                updatedObjects[i].setCreatedDate(currentDate);
                updatedObjects[i].setModifiedDate(currentDate);
            }

            setInitialStatus(initialStatus, updatedObjects[i]);
            updatedObjects[i].setCreatedBy(currentUser);
            updatedObjects[i].setModifiedBy(currentUser);
        }

        String query = generateCreateQuery(domainObjectTypeConfig);

        Map<String, Object>[] parameters = new Map[updatedObjects.length];

        for (int i = 0; i < updatedObjects.length; i++) {

            GenericDomainObject domainObject = updatedObjects[i];
            verifyAccessTokenOnCreate(accessToken, domainObject);

            Object id;
            if (parentDOs != null) {
                id = ((RdbmsId) parentDOs[i].getId()).getId();
            } else {
                id = idGenerator.generateId(domainObjectTypeIdCache.getId(domainObjectTypeConfig.getName()));
            }

            RdbmsId doId = new RdbmsId(type, (Long) id);
            updatedObjects[i].setId(doId);
            updatedObjects[i].resetDirty();

            parameters[i] = initializeCreateParameters(
                    updatedObjects[i], domainObjectTypeConfig, type, accessToken);
        }
        masterJdbcTemplate.batchUpdate(query, parameters);

        return updatedObjects;
    }

    private void verifyAccessTokenOnCreate(AccessToken accessToken, GenericDomainObject domainObject) {
        String domainObjectType = domainObject.getTypeName();
        Id[] parentIds = AccessControlUtility.getImmutableParentIds(domainObject, configurationExplorer);

        if (parentIds != null && parentIds.length > 0) {
            AccessType accessType = new CreateChildAccessType(domainObjectType);
            for (Id parentId : parentIds) {
                AccessToken linkAccessToken = accessControlService.createAccessToken(currentUserAccessor.getCurrentUser(), parentId, new CreateChildAccessType(domainObjectType));
                accessControlService.verifyAccessToken(linkAccessToken, parentId, accessType);
            }
        } else {

            AccessType accessType = new CreateObjectAccessType(domainObjectType, null);
            accessControlService.verifyAccessToken(accessToken, null, accessType);
        }
    }

    /**
     * Устанавливает начальный статус, если у доменного объекта поле статус не выставлено.
     * Если начальный статус не указан в конфигурации доменного объекта, то используется начальный статус родительского объекта (рекурсивно)
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

        Map<String, Value> uniqueKeyValuesByName = new HashMap<>();
        uniqueKeyValuesByName.put("name", new StringValue(statusName));

        return findByUniqueKey(STATUS_DO, uniqueKeyValuesByName, accessToken);
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

                Map<String, Object> parameters = new HashMap<String, Object>();
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

                    Long accessObjectId = domainObject.getValue(DomainObjectDao.ACCESS_OBJECT_ID) != null ?
                            (Long) domainObject.getValue(DomainObjectDao.ACCESS_OBJECT_ID).get() : null;
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

                List<FieldConfig> feldConfigs = domainObjectTypeConfig
                        .getDomainObjectFieldsConfig().getFieldConfigs();

                if (operation == DomainObjectVersion.AuditLogOperation.DELETE) {
                    initializeDomainParameters(null, feldConfigs, parameters);
                } else {
                    initializeDomainParameters(domainObject, feldConfigs,
                            parameters);
                }

                masterJdbcTemplate.update(query, parameters);
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
                                          boolean isUpdateStatus, List<FieldModification>[] changedFields) {

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

        return update(parentObjects, accessToken, isUpdateStatus, changedFields);
    }

    private void appendTableNameQueryPart(StringBuilder query, String typeName) {
        String tableName = getSqlName(typeName);
        query.append(wrap(tableName)).append(" ").append(getSqlAlias(tableName));
        appendParentTable(query, typeName);
    }

    private void appendColumnsQueryPart(StringBuilder query, String typeName) {
        DomainObjectTypeConfig config = configurationExplorer.getConfig(
                DomainObjectTypeConfig.class, typeName);

        query.append(getSqlAlias(typeName)).append(".*");

        if (isDerived(config)) {
            appendParentColumns(query, config);
        }
    }

    private void appendParentTable(StringBuilder query, String typeName) {
        DomainObjectTypeConfig config = configurationExplorer.getConfig(
                DomainObjectTypeConfig.class, typeName);

        if (config.getExtendsAttribute() == null) {
            return;
        }

        String tableAlias = getSqlAlias(typeName);

        String parentTableName = getSqlName(config.getExtendsAttribute());
        String parentTableAlias = getSqlAlias(config.getExtendsAttribute());

        query.append(" inner join ").append(wrap(parentTableName)).append(" ")
                .append(parentTableAlias);
        query.append(" on ").append(tableAlias).append(".").append(wrap(ID_COLUMN))
                .append(" = ");
        query.append(parentTableAlias).append(".").append(wrap(ID_COLUMN));

        appendParentTable(query, config.getExtendsAttribute());
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

    private String findInHierarchyDOTypeHavingField(String doType, String fieldName) {
        FieldConfig fieldConfig = configurationExplorer.getFieldConfig(doType, fieldName, false);
        if (fieldConfig != null) {
            return doType;
        } else {
            DomainObjectTypeConfig doTypeConfig = configurationExplorer.getConfig(DomainObjectTypeConfig.class, doType);
            if (doTypeConfig != null && doTypeConfig.getExtendsAttribute() != null) {
                return findInHierarchyDOTypeHavingField(doTypeConfig.getExtendsAttribute(), fieldName);
            } else {
                throw new ConfigurationException("Field '" + fieldName +
                        "' is not found in hierarchy of domain object type '" + doType + "'");
            }
        }
    }

    private void appendParentColumns(StringBuilder query,
                                     DomainObjectTypeConfig config) {
        DomainObjectTypeConfig parentConfig = configurationExplorer.getConfig(
                DomainObjectTypeConfig.class, config.getExtendsAttribute());

        appendColumnsExceptId(query, parentConfig);

        if (parentConfig.getExtendsAttribute() != null) {
            appendParentColumns(query, parentConfig);
        } else {
            query.append(", ").append(wrap(CREATED_DATE_COLUMN));
            query.append(", ").append(wrap(UPDATED_DATE_COLUMN));
            query.append(", ").append(wrap(CREATED_BY));
            query.append(", ").append(wrap(CREATED_BY_TYPE_COLUMN));
            query.append(", ").append(wrap(UPDATED_BY));
            query.append(", ").append(wrap(UPDATED_BY_TYPE_COLUMN));

            query.append(", ").append(wrap(STATUS_FIELD_NAME));
            query.append(", ").append(wrap(STATUS_TYPE_COLUMN));
        }
    }

    private void appendChildColumns(StringBuilder query, String typeName) {
        Collection<DomainObjectTypeConfig> childConfigs = configurationExplorer.findChildDomainObjectTypes(typeName, true);

        if (childConfigs == null || childConfigs.isEmpty()) {
            return;
        }

        for (DomainObjectTypeConfig childConfig : childConfigs) {
            appendColumnsExceptId(query, childConfig);
        }
    }

    private void appendColumnsExceptId(StringBuilder query, DomainObjectTypeConfig domainObjectTypeConfig) {
        String tableAlias = getSqlAlias(domainObjectTypeConfig.getName());
        for (FieldConfig fieldConfig : domainObjectTypeConfig.getFieldConfigs()) {
            if (ID_COLUMN.equals(fieldConfig.getName())) {
                continue;
            }

            query.append(", ").append(tableAlias).append(".")
                    .append(wrap(getSqlName(fieldConfig)));

            if (fieldConfig instanceof ReferenceFieldConfig) {
                query.append(", ").append(tableAlias).append(".")
                        .append(wrap(getReferenceTypeColumnName(fieldConfig.getName())));
            } else if (fieldConfig instanceof DateTimeWithTimeZoneFieldConfig) {
                query.append(", ").append(tableAlias).append(".")
                        .append(wrap(getTimeZoneIdColumnName(fieldConfig.getName())));
            }
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

    private String getDOTypeName(Integer typeId) {
        return domainObjectTypeIdCache.getName(typeId);
    }

    private void applyOffsetAndLimitWithDefaultOrdering(StringBuilder query,
                                                        String tableAlias, int offset, int limit) {
        if (limit != 0) {
            query.append(" order by ").append(tableAlias).append(".").append(wrap(ID_COLUMN));
            DaoUtils.applyOffsetAndLimit(query, offset, limit);
        }
    }

    private List<Id> extractIds(List<DomainObject> domainObjectList) {
        if (domainObjectList == null || domainObjectList.isEmpty()) {
            return new ArrayList<>();
        }

        List<Id> result = new ArrayList<>(domainObjectList.size());
        for (DomainObject domainObject : domainObjectList) {
            result.add(domainObject.getId());
        }

        return result;
    }

    /**
     * Получение идентификатора объекта в разрезе которого получаются права на сохраняемый доменный объект
     * @param domainObject
     * @return
     */
    private Id getAccessObjectId(DomainObject domainObject) {
        //Получаем матрицу и смотрим атрибут matrix_reference_field
        AccessMatrixConfig matrixConfig = null;
        //Получаем здесь тип, так как в случае наследования domainObject.getTypeName() возвращает некорректный тип
        String type = domainObjectTypeIdCache.getName(domainObject.getId());
        DomainObjectTypeConfig childDomainObjectTypeConfig = configurationExplorer.getConfig(DomainObjectTypeConfig.class, type);

        //Ищим матрицу для типа с учетом иерархии типов
        while((matrixConfig = configurationExplorer.getAccessMatrixByObjectType(childDomainObjectTypeConfig.getName())) == null
                && childDomainObjectTypeConfig.getExtendsAttribute() != null){
            childDomainObjectTypeConfig = configurationExplorer.getConfig(DomainObjectTypeConfig.class, childDomainObjectTypeConfig.getExtendsAttribute());
        }

        //По умолчанию access_object_id равен идентификатору самого объекта
        Id result = domainObject.getId();

        //Нашли матрицу и у нее установлен атрибут matrix-reference-field, вычисляем access_object_id  
        if (matrixConfig != null && matrixConfig.getMatrixReference() != null){
            if (matrixConfig.getMatrixReference().indexOf(".") > 0){
                // TODO здесь надо добавить обработку backlink
                throw new UnsupportedOperationException("Not implemented access referencing using backlink.");
            }else{
                Id refValue = domainObject.getReference(matrixConfig.getMatrixReference());
                if (refValue == null){
                    throw new FatalException("Field " + matrixConfig.getMatrixReference() + " mast has value. This field is matrix-reference-field");
                }

                //Вызываем рекурсивно данный метод на случай если в родительском типе так же указано заимствование матрицы
                AccessToken accessToken = accessControlService
                        .createSystemAccessToken(this.getClass().getName());
                result = getAccessObjectId(find(refValue, accessToken));
            }
        }

        return result;
    }

    private UniqueKeyConfig findUniqueKeyConfig(String domainObjectType, List<UniqueKeyConfig> uniqueKeyConfigs, CaseInsensitiveMap<Value> uniqueKeyValuesByName) {
        Set<String> uniqueKeyNamesParams = uniqueKeyValuesByName.keySet();
        for (UniqueKeyConfig uniqueKeyConfig : uniqueKeyConfigs) {

            Set<String> uniqueKeyFieldNames = new HashSet<>();
            for (UniqueKeyFieldConfig keyFieldConfig : uniqueKeyConfig.getUniqueKeyFieldConfigs()) {
                String fieldName = keyFieldConfig.getName().toLowerCase();
                uniqueKeyFieldNames.add(fieldName);
            }

            if (uniqueKeyNamesParams.equals(uniqueKeyFieldNames)) {
                return uniqueKeyConfig;
            }
        }

        throw new IllegalArgumentException("The configuration of the domain object type \"" + domainObjectType +
                "\" has no unique key (" + Arrays.toString(uniqueKeyNamesParams.toArray()) + ")" );
    }

    private class DomainObjectActionListener implements ActionListener {

        private DomainObjectsModification domainObjectsModification;

        private DomainObjectActionListener(String transactionId) {
            domainObjectsModification = new DomainObjectsModification(transactionId);
        }

        @Override
        public void onAfterCommit() {
            //Точки расширения вызываем в специальном EJB чтобы открылась новая транзакция
            afterCommitExtensionPointService.afterCommit(domainObjectsModification);
        }

        public void addCreatedDomainObject(DomainObject domainObject){
            if (isIgnoredOnCreateAndSave(domainObject)) {
                return;
            }
            domainObjectsModification.addCreatedDomainObject(domainObject);
        }

        public void addChangeStatusDomainObject(Id id){
            domainObjectsModification.addChangeStatusDomainObject(id);
        }

        public void addDeletedDomainObject(DomainObject domainObject){
            domainObjectsModification.addDeletedDomainObject(domainObject);
        }

        public void addSavedDomainObject(DomainObject domainObject, List<FieldModification> newFields) {
            if (isIgnoredOnCreateAndSave(domainObject)) {
                return;
            }

            domainObjectsModification.addSavedDomainObject(domainObject, newFields);
        }

        @Override
        public void onRollback() {
            // Ничего не делаем            
        }

        @Override
        public void onBeforeCommit() {
            // Ничего не делаем

        }

        private boolean isIgnoredOnCreateAndSave(DomainObject domainObject) {
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
}
