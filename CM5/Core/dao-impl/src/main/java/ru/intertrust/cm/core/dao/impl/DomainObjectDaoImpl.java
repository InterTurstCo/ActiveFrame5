package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.dao.access.*;
import ru.intertrust.cm.core.dao.api.DomainObjectCacheService;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.api.ExtensionService;
import ru.intertrust.cm.core.dao.api.IdGenerator;
import ru.intertrust.cm.core.dao.api.extension.AfterDeleteExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.AfterSaveExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.BeforeDeleteExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.BeforeSaveExtensionHandler;
import ru.intertrust.cm.core.dao.exception.InvalidIdException;
import ru.intertrust.cm.core.model.ObjectNotFoundException;
import ru.intertrust.cm.core.dao.exception.OptimisticLockException;
import ru.intertrust.cm.core.dao.impl.access.AccessControlUtility;
import ru.intertrust.cm.core.dao.impl.utils.*;

import java.util.*;

import static ru.intertrust.cm.core.business.api.dto.GenericDomainObject.STATUS_DO;
import static ru.intertrust.cm.core.business.api.dto.GenericDomainObject.STATUS_FIELD_NAME;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.*;
import static ru.intertrust.cm.core.dao.impl.utils.DaoUtils.generateParameter;
import static ru.intertrust.cm.core.dao.impl.utils.DaoUtils.setParameter;
import static ru.intertrust.cm.core.dao.impl.utils.DaoUtils.wrap;
import static ru.intertrust.cm.core.dao.impl.utils.DateUtils.getGMTDate;

/**
 * Класс реализации работы с доменным объектом
 * @author atsvetkov
 *
 */
public class DomainObjectDaoImpl implements DomainObjectDao {

    @Autowired
    private NamedParameterJdbcOperations jdbcTemplate;

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
    public void setDomainObjectCacheService(
            DomainObjectCacheServiceImpl domainObjectCacheService) {
        this.domainObjectCacheService = domainObjectCacheService;
    }

    public void setPermissionService(PermissionServiceDao permissionService) {
        this.permissionService = permissionService;
    }

    public void setJdbcTemplate(NamedParameterJdbcOperations jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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

    public DomainObject setStatus(Id objectId, Id status, AccessToken accessToken) {
        accessControlService.verifySystemAccessToken(accessToken);
        DomainObject domainObject = find(objectId, accessToken);
        ((GenericDomainObject) domainObject).setStatus(status);
        DomainObject result = update(domainObject, true, null);

        refreshDynamiGroupsAndAclForUpdate(result, null, null);

        return result;
    }

    @Override
    public DomainObject create(DomainObject domainObject, AccessToken accessToken) {

        accessControlService.verifySystemAccessToken(accessToken);

        String initialStatus = getInitialStatus(domainObject);

        DomainObject createdObject = create(domainObject,
                domainObjectTypeIdCache.getId(domainObject.getTypeName()), initialStatus);
        domainObjectCacheService.putObjectToCache(createdObject);

        refreshDynamiGroupsAndAclForCreate(createdObject);
        return createdObject;
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

        DomainObject result = null;

        //Получение измененных полей
        List<FieldModification> changedFields = getModifiedFieldNames(domainObject);

        // Вызов точки расширения до сохранения
        List<String> parentTypes = getAllParentTypes(domainObject.getTypeName());
        for (String typeName : parentTypes) {
            BeforeSaveExtensionHandler beforeSaveExtension = extensionService
                    .getExtentionPoint(BeforeSaveExtensionHandler.class, typeName);
            beforeSaveExtension.onBeforeSave(domainObject, changedFields);
        }

        DomainObjectVersion.AuditLogOperation operation = null;

        // Сохранение в базе
        if (domainObject.isNew()) {
            result = create(domainObject, accessToken);
            operation = DomainObjectVersion.AuditLogOperation.CREATE;
        } else {
            result = update(domainObject, accessToken, changedFields);
            operation = DomainObjectVersion.AuditLogOperation.UPDATE;
        }

        // Запись в auditLog
        createAuditLog(result, result.getTypeName(),
                domainObjectTypeIdCache.getId(domainObject.getTypeName()),
                operation);

        // Вызов точки расширения после сохранения
        for (String typeName : parentTypes) {
            AfterSaveExtensionHandler afterSaveExtension = extensionService
                    .getExtentionPoint(AfterSaveExtensionHandler.class, typeName);
            afterSaveExtension.onAfterSave(result, changedFields);
        }

        return result;
    }

    @Override
    public List<DomainObject> save(List<DomainObject> domainObjects, AccessToken accessToken) {
        // todo this can be optimized with batches

        List<DomainObject> result = new ArrayList<>();

        for (DomainObject domainObject : domainObjects) {
            DomainObject newDomainObject = save(domainObject, accessToken);
            result.add(newDomainObject);
        }
        return result;

    }

    private DomainObject
            update(DomainObject domainObject, AccessToken accessToken, List<FieldModification> changedFields)
                    throws InvalidIdException, ObjectNotFoundException,
                    OptimisticLockException {

        accessControlService.verifyAccessToken(accessToken, domainObject.getId(), DomainObjectAccessType.WRITE);

        boolean isUpdateStatus = false;

        GenericDomainObject updatedObject = update(domainObject, isUpdateStatus, changedFields);

        return updatedObject;

    }

    private GenericDomainObject update(DomainObject domainObject, boolean isUpdateStatus,
            List<FieldModification> changedFields) {
        GenericDomainObject updatedObject = new GenericDomainObject(
                domainObject);

        DomainObjectTypeConfig domainObjectTypeConfig = configurationExplorer
                .getConfig(DomainObjectTypeConfig.class,
                        updatedObject.getTypeName());

        validateIdType(updatedObject.getId());

        List<Id> beforeChangeInvalidGroups =
                dynamicGroupService.getInvalidGroupsBeforeChange(domainObject, changedFields);

        DomainObject parentDO = updateParentDO(domainObjectTypeConfig, domainObject, isUpdateStatus, changedFields);

        String query = generateUpdateQuery(domainObjectTypeConfig, isUpdateStatus);

        Date currentDate = new Date();
        // В случае если сохранялся родительский объект то берем дату
        // модификации из нее, иначе в базе и возвращаемом доменном объекте
        // будут различные даты изменения и изменение объект отвалится по ошибке
        // OptimisticLockException
        if (parentDO != null) {
            currentDate = parentDO.getModifiedDate();
        }

        Map<String, Object> parameters = initializeUpdateParameters(
                updatedObject, domainObjectTypeConfig, currentDate, isUpdateStatus);

        int count = jdbcTemplate.update(query, parameters);

        if (count == 0 && (!exists(updatedObject.getId()))) {
            throw new ObjectNotFoundException(updatedObject.getId());
        }

        if (!isDerived(domainObjectTypeConfig)) {
            if (count == 0) {
                throw new OptimisticLockException(updatedObject);
            }

            updatedObject.setModifiedDate(currentDate);
        }

        updatedObject.setModifiedDate(currentDate);
        updatedObject.resetDirty();

        domainObjectCacheService.putObjectToCache(updatedObject);

        if (isUpdateStatus) {
            Id statusValue = updatedObject.getReference(STATUS_FIELD_NAME);
            if (statusValue != null) {
                updatedObject.setReference("status", statusValue);
            }

        }
        refreshDynamiGroupsAndAclForUpdate(domainObject, changedFields, beforeChangeInvalidGroups);

        return updatedObject;
    }

    private void refreshDynamiGroupsAndAclForUpdate(DomainObject domainObject, List<FieldModification> modifiedFields,
            List<Id> beforeChangeInvalicContexts) {
        dynamicGroupService.notifyDomainObjectChanged(domainObject, modifiedFields, beforeChangeInvalicContexts);
        permissionService.notifyDomainObjectChanged(domainObject, modifiedFields);
    }

    private List<FieldModification> getModifiedFieldNames(
            DomainObject domainObject) {

        List<FieldModification> modifiedFieldNames = new ArrayList<FieldModification>();

        //Для нового объекта все поля попадают в список измененных
        if (domainObject.isNew()) {
            for (String fieldName : domainObject.getFields()) {
                Value newValue = domainObject.getValue(fieldName);
                modifiedFieldNames.add(new FieldModificationImpl(fieldName,
                        null, newValue));
            }
        } else {
            //для ранее созданного объекта получаем объект не сохраненный из хранилища и вычисляем измененные поля
            AccessToken accessToken = accessControlService
                    .createSystemAccessToken("DomainObjectDaoImpl");
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
        validateIdType(id);

        accessControlService.verifyAccessToken(accessToken, id, DomainObjectAccessType.DELETE);

        RdbmsId rdbmsId = (RdbmsId) id;
        DomainObjectTypeConfig domainObjectTypeConfig = configurationExplorer
                .getConfig(DomainObjectTypeConfig.class, getDOTypeName(rdbmsId.getTypeId()));

        // Получаем удаляемый доменный объект для нужд точек расширения
        AccessToken systemAccessToken = accessControlService
                .createSystemAccessToken("DomainObjectDaoImpl");

        DomainObject deletedObject = find(id, systemAccessToken);
        List<Id> beforeChangeInvalidGroups = dynamicGroupService.getInvalidGroupsBeforeDelete(deletedObject);

        // Точка расширения до удаления
        List<String> parentTypes = getAllParentTypes(domainObjectTypeConfig.getName());
        for (String typeName : parentTypes) {
            BeforeDeleteExtensionHandler beforeDeleteEH = extensionService
                    .getExtentionPoint(BeforeDeleteExtensionHandler.class, typeName);
            beforeDeleteEH.onBeforeDelete(deletedObject);
        }

        //непосредственно удаление из базыы
        internalDelete(id);
        //Удалене из кэша
        domainObjectCacheService.removeObjectFromCache(id);

        // Пишем в аудит лог
        createAuditLog(deletedObject, deletedObject.getTypeName(),
                domainObjectTypeIdCache.getId(deletedObject.getTypeName()),
                DomainObjectVersion.AuditLogOperation.DELETE);

        // Точка расширения после удаления, вызывается с установкой фильтра текущего типа и всех наследников
        for (String typeName : parentTypes) {
            AfterDeleteExtensionHandler afterDeleteEH = extensionService
                    .getExtentionPoint(AfterDeleteExtensionHandler.class, typeName);
            afterDeleteEH.onAfterDelete(deletedObject);
        }

        //Пересчет прав
        refreshDynamiGroupsAndAclForDelete(deletedObject, beforeChangeInvalidGroups);

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
     * @param deletedId
     */
    private void internalDelete(Id deletedId) {
        RdbmsId rdbmsId = (RdbmsId) deletedId;

        DomainObjectTypeConfig domainObjectTypeConfig = configurationExplorer
                .getConfig(DomainObjectTypeConfig.class,
                        getDOTypeName(rdbmsId.getTypeId()));

        String query = generateDeleteQuery(domainObjectTypeConfig);

        Map<String, Object> parameters = initializeIdParameter(rdbmsId);

        int count = jdbcTemplate.update(query, parameters);

        if (count == 0) {
            throw new ObjectNotFoundException(rdbmsId);
        }

        // Удаление родительского объекта, перенесено ниже удаления дочернего
        // объекта для того чтобы не ругались foreign key
        Id parentId = getParentId(rdbmsId, domainObjectTypeConfig);
        if (parentId != null) {
            internalDelete(parentId);
        }
    }

    private void refreshDynamiGroupsAndAclForDelete(DomainObject deletedObject, List<Id> beforeChangeInvalicContexts) {
        if (deletedObject != null) {
            dynamicGroupService.notifyDomainObjectDeleted(deletedObject, beforeChangeInvalicContexts);
            permissionService.notifyDomainObjectDeleted(deletedObject);
        }
    }

    @Override
    public int delete(Collection<Id> ids, AccessToken accessToken) {
        // todo: in a batch
        // TODO как обрабатывать ошибки при удалении каждого доменного
        // объекта...
        int count = 0;
        for (Id id : ids) {
            try {
                delete(id, accessToken);

                count++;
            } catch (ObjectNotFoundException e) {
                // ничего не делаем пока
            } catch (InvalidIdException e) {
                // //ничего не делаем пока
            }

        }
        return count;
    }

    @Override
    public boolean exists(Id id) throws InvalidIdException {
        if (domainObjectCacheService.getObjectToCache(id) != null) {
            return true;
        }

        RdbmsId rdbmsId = (RdbmsId) id;
        validateIdType(id);

        StringBuilder query = new StringBuilder();
        query.append(generateExistsQuery(getDOTypeName(rdbmsId.getTypeId())));

        Map<String, Object> parameters = initializeExistsParameters(id);
        long total = jdbcTemplate.queryForObject(query.toString(), parameters,
                Long.class);

        return total > 0;
    }

    @Override
    public DomainObject find(Id id, AccessToken accessToken) {
        if (id == null) {
            throw new IllegalArgumentException("Object id can not be null");
        }

        accessControlService.verifyAccessToken(accessToken, id, DomainObjectAccessType.READ);

        DomainObject domainObject = domainObjectCacheService
                .getObjectToCache(id);
        if (domainObject != null) {
            return domainObject;
        }

        RdbmsId rdbmsId = (RdbmsId) id;
        String typeName = getDOTypeName(rdbmsId.getTypeId());

        String query = generateFindQuery(typeName, accessToken, false);

        Map<String, Object> parameters = initializeIdParameter(rdbmsId);
        if (accessToken.isDeferred()) {
            parameters.putAll(getAclParameters(accessToken));
        }

        return jdbcTemplate.query(query, parameters, new SingleObjectRowMapper(
                typeName, configurationExplorer, domainObjectTypeIdCache));
    }

    @Override
    public DomainObject findAndLock(Id id, AccessToken accessToken) {
        if (id == null) {
            throw new IllegalArgumentException("Object id can not be null");
        }

        accessControlService.verifyAccessToken(accessToken, id, DomainObjectAccessType.WRITE);

        DomainObject domainObject = domainObjectCacheService.getObjectToCache(id);
        if (domainObject != null) {
            return domainObject;
        }

        RdbmsId rdbmsId = (RdbmsId) id;
        String typeName = getDOTypeName(rdbmsId.getTypeId());

        String query = generateFindQuery(typeName, accessToken, true);

        Map<String, Object> parameters = initializeIdParameter(rdbmsId);
        if (accessToken.isDeferred()) {
            parameters.putAll(getAclParameters(accessToken));
        }

        return jdbcTemplate.query(query, parameters, new SingleObjectRowMapper(
                typeName, configurationExplorer, domainObjectTypeIdCache));
    }

    @Override
    public List<DomainObject> findAll(String domainObjectType,
            AccessToken accessToken) {
        return findAll(domainObjectType, 0, 0, accessToken);
    }

    @Override
    public List<DomainObject> findAll(String domainObjectType, int offset,
            int limit, AccessToken accessToken) {
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
        List<DomainObject> result = domainObjectCacheService
                .getObjectToCache(cacheKey);
        if (result != null) {
            return result;
        }

        String query = generateFindAllQuery(domainObjectType, offset, limit,
                accessToken);

        Map<String, Object> parameters = new HashMap<String, Object>();
        if (accessToken.isDeferred()) {
            parameters.putAll(getAclParameters(accessToken));
        }

        result = jdbcTemplate.query(query, parameters,
                new MultipleObjectRowMapper(domainObjectType,
                        configurationExplorer, domainObjectTypeIdCache));
        domainObjectCacheService.putObjectToCache(result, cacheKey);

        return result;
    }

    @Override
    public List<DomainObject> find(List<Id> ids, AccessToken accessToken) {
        if (ids == null || ids.size() == 0) {
            return new ArrayList<>();
        }
        List<DomainObject> allDomainObjects = new ArrayList<DomainObject>();

        IdSorterByType idSorterByType = new IdSorterByType(
                ids.toArray(new RdbmsId[ids.size()]));

        for (final Integer domainObjectType : idSorterByType
                .getDomainObjectTypeIds()) {
            List<Id> idsOfSingleType = idSorterByType.getIdsOfType(domainObjectType);
            String doTypeName = domainObjectTypeIdCache
                    .getName(domainObjectType);
            allDomainObjects.addAll(findSingleTypeDomainObjects(
                    idsOfSingleType, accessToken, doTypeName));
        }

        return allDomainObjects;
    }

    /**
     * Поиск доменных объектов одного типа.
     *
     * @param ids
     *            идентификаторы доменных объектов
     * @param accessToken
     *            маркер доступа
     * @param domainObjectType
     *            тип доменного объекта
     * @return список доменных объектов
     */
    private List<DomainObject> findSingleTypeDomainObjects(List<Id> ids,
            AccessToken accessToken, String domainObjectType) {
        List<DomainObject> cachedDomainObjects = domainObjectCacheService
                .getObjectToCache(ids);
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

        StringBuilder query = new StringBuilder();

        Map<String, Object> aclParameters = new HashMap<String, Object>();

        if (accessToken.isDeferred()) {
            
            String aclReadTable = AccessControlUtility
                    .getAclReadTableNameFor(configurationExplorer, domainObjectType);
            query.append("select distinct t.* from " + domainObjectType + " t ");
            query.append(" inner join ").append(aclReadTable).append(" r on t.id = r.object_id ");
            query.append(" inner join ").append(wrap("group_group")).append(" gg on r.").append(wrap("group_id"))
                    .append(" = gg.").append(wrap("parent_group_id"));
            query.append(" inner join ").append(wrap("group_member")).append(" gm on gg.")
                    .append(wrap("child_group_id")).append(" = gm." + wrap("usergroup"));
            query.append(" where gm.person_id = :user_id and t.id in (:object_ids) ");

            aclParameters = getAclParameters(accessToken);             

        } else {
            query.append("select * from ").append(wrap(getSqlName(domainObjectType)))
                    .append(" where ").append(wrap(ID_COLUMN)).append(" in (:object_ids) ");
        }

        Map<String, Object> parameters = new HashMap<>();
        List<DomainObject> readDomainObjects;
        if (!idsToRead.isEmpty()) {

            List<Long> listIds = AccessControlUtility
                    .convertRdbmsIdsToLongIds(new ArrayList<>(idsToRead));
            parameters.put("object_ids", listIds);

            if (accessToken.isDeferred()) {
                parameters.putAll(aclParameters);
            }

            readDomainObjects = jdbcTemplate.query(query
                    .toString(), parameters, new MultipleObjectRowMapper(
                    domainObjectType, configurationExplorer,
                    domainObjectTypeIdCache));
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
        return findLinkedDomainObjects(domainObjectId, linkedType, linkedField,
                0, 0, accessToken);
    }

    @Override
    public List<DomainObject> findLinkedDomainObjects(Id domainObjectId,
            String linkedType, String linkedField, int offset, int limit,
            AccessToken accessToken) {
        String[] cacheKey = new String[] { linkedType,linkedField,
                String.valueOf(offset),String.valueOf(limit) };
        List<DomainObject> domainObjects = domainObjectCacheService
                .getObjectToCache(domainObjectId, cacheKey);
        if (domainObjects != null) {
            return domainObjects;
        }

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("domain_object_id", ((RdbmsId) domainObjectId).getId());
        String query = buildFindChildrenQuery(linkedType, linkedField, offset,
                limit, accessToken);
        if (accessToken.isDeferred()) {
            parameters.putAll(getAclParameters(accessToken));
        }

        domainObjects = jdbcTemplate.query(query, parameters,
                new MultipleObjectRowMapper(linkedType, configurationExplorer,
                        domainObjectTypeIdCache));
        domainObjectCacheService.putObjectToCache(domainObjectId,
                domainObjects, cacheKey);

        if (domainObjects == null || domainObjects.isEmpty()) {
            return domainObjects;
        }

        // Если тип доменного объекта является наследником linkedType, то необходимо извлечь доменный объект этого типа
        for (int i = 0; i < domainObjects.size(); i ++) {
            DomainObject domainObject = domainObjects.get(i);
            if (!linkedType.equals(domainObject.getTypeName())) {
                domainObjectCacheService.removeObjectFromCache(domainObject.getId());
                domainObjects.set(i, find(domainObject.getId(), accessToken));
            }
        }

        return domainObjects;
    }

    @Override
    public List<Id> findLinkedDomainObjectsIds(Id domainObjectId,
            String linkedType, String linkedField, AccessToken accessToken) {
        return findLinkedDomainObjectsIds(domainObjectId, linkedType,
                linkedField, 0, 0, accessToken);
    }

    @Override
    public List<Id> findLinkedDomainObjectsIds(Id domainObjectId,
            String linkedType, String linkedField, int offset, int limit,
            AccessToken accessToken) {
        String[] cacheKey = new String[] { linkedType,linkedField,
                String.valueOf(offset),String.valueOf(limit) };
        List<DomainObject> domainObjects = domainObjectCacheService
                .getObjectToCache(domainObjectId, cacheKey);
        if (domainObjects != null) {
            return extractIds(domainObjects);
        }

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("domain_object_id", ((RdbmsId) domainObjectId).getId());
        String query = buildFindChildrenIdsQuery(linkedType, linkedField,
                offset, limit, accessToken);
        if (accessToken.isDeferred()) {
            parameters.putAll(getAclParameters(accessToken));
        }
        Integer linkedTypeId = domainObjectTypeIdCache.getId(linkedType);
        return jdbcTemplate.query(query, parameters, new MultipleIdRowMapper(
                linkedTypeId));
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
            DomainObjectTypeConfig domainObjectTypeConfig, Integer type) {

        RdbmsId rdbmsId = (RdbmsId) domainObject.getId();

        Map<String, Object> parameters = initializeIdParameter(rdbmsId);

        if (!isDerived(domainObjectTypeConfig)) {
            parameters.put("created_date",
                    getGMTDate(domainObject.getCreatedDate()));
            parameters.put("updated_date",
                    getGMTDate(domainObject.getModifiedDate()));

            Long statusId = domainObject.getStatus() != null ? ((RdbmsId) domainObject.getStatus()).getId() : null;
            Integer statusTypeId =
                    domainObject.getStatus() != null ? ((RdbmsId) domainObject.getStatus()).getTypeId() : null;

            parameters.put("status", statusId);
            parameters.put("status_type", statusTypeId);

        }
        parameters.put("type_id", type);

        List<FieldConfig> feldConfigs = domainObjectTypeConfig
                .getDomainObjectFieldsConfig().getFieldConfigs();

        initializeDomainParameters(domainObject, feldConfigs, parameters);

        return parameters;
    }

    /**
     * Создает SQL запрос для нахождения доменного объекта
     *
     * @param typeName
     *            тип доменного объекта
     * @return SQL запрос для нахождения доменного объекта
     */
    protected String generateFindQuery(String typeName, AccessToken accessToken, boolean lock) {
        String tableAlias = getSqlAlias(typeName);

        StringBuilder query = new StringBuilder();
        query.append("select ");
        appendColumnsQueryPart(query, typeName);
        query.append(" from ");
        appendTableNameQueryPart(query, typeName);
        query.append(" where ").append(tableAlias).append(".").append(wrap(ID_COLUMN)).append("=:id ");
       
        if (accessToken.isDeferred()) {
            
            String aclReadTable = AccessControlUtility
                    .getAclReadTableName(configurationExplorer, typeName);
            
            query.append(" and exists (select a.object_id from ").append(aclReadTable).append(" a ");
            query.append(" inner join ").append(wrap("group_group")).append(" gg on a.")
                    .append(wrap("group_id")).append(" = gg.").append(wrap("parent_group_id"));
            query.append(" inner join ").append(wrap("group_member")).append(" gm on gg.")
                    .append(wrap("child_group_id")).append(" = gm.").append(wrap("usergroup"));
            query.append(" where gm.person_id = :user_id and a.object_id = :id)");
             
        }

        if (lock) {
            query.append("for update");
        }

        return query.toString();
    }



    /**
     * Создает SQL запрос для нахождения всех доменных объектов определенного типа
     *
     * @param typeName
     *            тип доменного объекта
     * @return SQL запрос для нахождения доменного объекта
     */
    protected String generateFindAllQuery(String typeName, int offset,
            int limit, AccessToken accessToken) {
        String tableAlias = getSqlAlias(typeName);

        StringBuilder query = new StringBuilder();
        query.append("select ");
        appendColumnsQueryPart(query, typeName);
        query.append(" from ");
        appendTableNameQueryPart(query, typeName);

        if (accessToken.isDeferred()) {                       
            
            String aclReadTable = AccessControlUtility
                    .getAclReadTableName(configurationExplorer, typeName);
            query.append(" where exists (select a.object_id from ").append(aclReadTable).append(" a");
            query.append(" inner join ").append(wrap("group_group")).append(" gg on a.").append(wrap("group_id"))
                    .append(" = gg.").append(wrap("parent_group_id"));
            query.append(" inner join ").append(wrap("group_member")).append(" gm on gg.")
                    .append(wrap("child_group_id")).append(" = gm.").append(wrap("usergroup"));
            query.append(" where gm.person_id = :user_id and a.object_id = ")
                    .append(tableAlias).append(".ID)");
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

        List<String> columnNames = DataStructureNamingHelper
                .getColumnNames(feldConfigs);

        String fieldsWithparams = DaoUtils
                .generateCommaSeparatedListWithParams(columnNames);

        query.append("update ").append(wrap(tableName)).append(" set ");

        if (!isDerived(domainObjectTypeConfig)) {
            query.append(wrap(UPDATED_DATE_COLUMN)).append("=:current_date, ");
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
            DomainObjectTypeConfig domainObjectTypeConfig, Date currentDate, boolean isUpdateStatus) {

        Map<String, Object> parameters = new HashMap<String, Object>();

        RdbmsId rdbmsId = (RdbmsId) domainObject.getId();

        parameters.put("id", rdbmsId.getId());
        parameters.put("current_date", getGMTDate(currentDate));
        parameters.put("updated_date",
                getGMTDate(domainObject.getModifiedDate()));
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
            query.append(", ").append(wrap(CREATED_DATE_COLUMN)).append(", ")
                    .append(wrap(UPDATED_DATE_COLUMN)).append(", ");

            query.append(wrap(STATUS_FIELD_NAME)).append(", ")
                    .append(wrap(STATUS_TYPE_COLUMN));

        }

        if (commaSeparatedColumns.length() > 0) {
            query.append(", ").append(commaSeparatedColumns);
        }

        query.append(") values (:id , :type_id");

        if (!isDerived(domainObjectTypeConfig)) {
            query.append(", :created_date, :updated_date, :status, :status_type");
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
            query.append(", ").append(wrap(OPERATION_COLUMN)).append(", ");
            query.append(wrap(UPDATED_DATE_COLUMN)).append(", ");
            query.append(wrap(COMPONENT_COLUMN)).append(", ");
            query.append(wrap(DOMAIN_OBJECT_ID_COLUMN)).append(", ");
            query.append(wrap(INFO_COLUMN)).append(", ");
            query.append(wrap(IP_ADDRESS_COLUMN));
        }

        if (commaSeparatedColumns.length() > 0) {
            query.append(", ").append(commaSeparatedColumns);
        }

        query.append(") values (:").append(ID_COLUMN).append(", :").append(TYPE_COLUMN);
        if (!isDerived(domainObjectTypeConfig)) {
            query.append(", :operation, :updated_date, :component, :domain_object_id, :info, :ip_address");
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
     * Инициализирует параметр c id доменного объекта
     *
     * @param id
     *            идентификатор доменного объекта
     * @return карту объектов содержащую имя параметра и его значение
     */
    protected Map<String, Object> initializeIdParameter(Id id) {
        RdbmsId rdbmsId = (RdbmsId) id;
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("id", rdbmsId.getId());
        return parameters;
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
        query.append("select ").append(wrap(ID_COLUMN)).append(" from ").append(wrap(tableName)).append(" where ").
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
     * Инициализация параметров для отложенной провеки доступа.
     *
     * @param accessToken
     * @return
     */
    protected Map<String, Object> getAclParameters(AccessToken accessToken) {
        long userId = ((UserSubject) accessToken.getSubject()).getUserId();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("user_id", userId);
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

            setParameter(parameterName, value, parameters);
        }
    }

    protected String buildFindChildrenQuery(String linkedType, String linkedField, int offset,
                                            int limit, AccessToken accessToken) {
        String tableAlias = getSqlAlias(linkedType);
        String tableHavingLinkedFieldAlias = getSqlAlias(findInHierarchyDOTypeHavingField(linkedType, linkedField));

        StringBuilder query = new StringBuilder();
        query.append("select ");
        appendColumnsQueryPart(query, linkedType);
        query.append(" from ");
        appendTableNameQueryPart(query, linkedType);
        query.append(" where ").append(tableHavingLinkedFieldAlias).append(".").
                append(wrap(getSqlName(linkedField))).append(" = :domain_object_id");

        if (accessToken.isDeferred()) {
             appendAccessControlLogicToQuery(query, linkedType);
        }

        applyOffsetAndLimitWithDefaultOrdering(query, tableAlias, offset, limit);

        return query.toString();
    }

    protected String buildFindChildrenIdsQuery(String linkedType,
            String linkedField, int offset, int limit, AccessToken accessToken) {
        String doTypeHavingLinkedField = findInHierarchyDOTypeHavingField(linkedType, linkedField);
        String tableName = getSqlName(doTypeHavingLinkedField);
        String tableAlias = getSqlAlias(tableName);

        StringBuilder query = new StringBuilder();
        query.append("select ").append(tableAlias).append(".").append(wrap(ID_COLUMN)).
                append(" from ").append(wrap(tableName)).append(" ").append(tableAlias).
                append(" where ").append(tableAlias).append(".").append(wrap(getSqlName(linkedField))).
                append(" = :domain_object_id");

        if (accessToken.isDeferred()) {
             appendAccessControlLogicToQuery(query, linkedType);
        }

        applyOffsetAndLimitWithDefaultOrdering(query, tableAlias, offset, limit);

        return query.toString();
    }

    private void appendAccessControlLogicToQuery(StringBuilder query,
            String linkedType) {

        String childAclReadTable = AccessControlUtility
                .getAclReadTableNameFor(configurationExplorer, linkedType);
        
        query.append(" and exists (select r.object_id from ").append(childAclReadTable).append(" r ");
        
        String linkedTypeAlias = getSqlAlias(linkedType);
        
        query.append(" inner join ").append(DaoUtils.wrap("group_group")).append(" gg on r.").append(DaoUtils.wrap("group_id"))
                .append(" = gg.").append(DaoUtils.wrap("parent_group_id"));
        query.append(" inner join ").append(DaoUtils.wrap("group_member")).append(" gm on gg.")
                .append(DaoUtils.wrap("child_group_id")).append(" = gm.").append(DaoUtils.wrap("usergroup"));
        query.append("where gm.person_id = :user_id and r.object_id = ").append(linkedTypeAlias).append(".").append(DaoUtils.wrap(ID_COLUMN)).append(")");
    }

    private DomainObject create(DomainObject domainObject, Integer type, String initialStatus) {
        DomainObjectTypeConfig domainObjectTypeConfig = configurationExplorer
                .getConfig(DomainObjectTypeConfig.class,
                        domainObject.getTypeName());

        GenericDomainObject updatedObject = new GenericDomainObject(
                domainObject);
        DomainObject parentDo = createParentDO(domainObject,
                domainObjectTypeConfig, type, initialStatus);

        if (parentDo != null) {
            updatedObject.setCreatedDate(parentDo.getCreatedDate());
            updatedObject.setModifiedDate(parentDo.getModifiedDate());

        } else {
            Date currentDate = new Date();
            updatedObject.setCreatedDate(currentDate);
            updatedObject.setModifiedDate(currentDate);

        }

        setInitialStatus(initialStatus, updatedObject);

        String query = generateCreateQuery(domainObjectTypeConfig);

        Object id;
        if (parentDo != null) {
            id = ((RdbmsId) parentDo.getId()).getId();
        } else {
            id = idGenerator.generateId(domainObjectTypeIdCache.getId(domainObjectTypeConfig.getName()));
        }

        RdbmsId doId = new RdbmsId(type, (Long) id);
        updatedObject.setId(doId);

        Map<String, Object> parameters = initializeCreateParameters(
                updatedObject, domainObjectTypeConfig, type);
        jdbcTemplate.update(query, parameters);

        return updatedObject;
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
        String query = "select s.* from " + wrap(STATUS_DO) + " s where s." + wrap("name") + "=:name";
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("name", statusName);
        DomainObject statusDO = jdbcTemplate.query(query, paramMap,
                new SingleObjectRowMapper(STATUS_DO, configurationExplorer,
                        domainObjectTypeIdCache));
        if (statusDO == null) {
            throw new IllegalArgumentException("Status not found: "
                    + statusName);
        }
        return statusDO;
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
     * @param type
     * @return
     */
    private Long createAuditLog(DomainObject domainObject, String typeName,
            Integer type, DomainObjectVersion.AuditLogOperation operation) {
        Long id = null;
        if (typeName != null) {
            DomainObjectTypeConfig domainObjectTypeConfig = configurationExplorer
                    .getConfig(DomainObjectTypeConfig.class, typeName);

            // Проверка на включенность аудит лога, или если пришли рекурсивно
            // из подчиненного уровня, где аудит был включен
            if (isAuditLogEnable(domainObjectTypeConfig)
                    || !domainObject.getTypeName().equals(typeName)) {

                id = createAuditLog(domainObject,
                        domainObjectTypeConfig.getExtendsAttribute(), type,
                        operation);

                if (id == null) {
                    id = (Long) idGenerator.generatetLogId(domainObjectTypeIdCache.getId(domainObjectTypeConfig.getName()));
                }

                String query = generateCreateAuditLogQuery(domainObjectTypeConfig);

                Map<String, Object> parameters = new HashMap<String, Object>();
                parameters.put(DomainObjectDao.ID_COLUMN, id);
                parameters.put(DomainObjectDao.TYPE_COLUMN, type);

                if (!isDerived(domainObjectTypeConfig)) {
                    parameters.put(DomainObjectDao.OPERATION_COLUMN,
                            operation.getOperation());
                    parameters.put(DomainObjectDao.UPDATED_DATE_COLUMN,
                            getGMTDate(domainObject.getModifiedDate()));
                    // TODO Получение имени компонента из AcceeToken
                    parameters.put(DomainObjectDao.COMPONENT_COLUMN, "");
                    parameters.put(DomainObjectDao.DOMAIN_OBJECT_ID_COLUMN,
                            ((RdbmsId) domainObject.getId()).getId());
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

                jdbcTemplate.update(query, parameters);
            }

        }
        return id;
    }

    private DomainObject createParentDO(DomainObject domainObject,
            DomainObjectTypeConfig domainObjectTypeConfig, Integer type, String initialStatus) {
        if (!isDerived(domainObjectTypeConfig)) {
            return null;
        }

        GenericDomainObject parentDO = new GenericDomainObject(domainObject);
        parentDO.setTypeName(domainObjectTypeConfig.getExtendsAttribute());
        return create(parentDO, type, initialStatus);
    }

    private DomainObject updateParentDO(DomainObjectTypeConfig domainObjectTypeConfig, DomainObject domainObject,
            boolean isUpdateStatus, List<FieldModification> changedFields) {
        RdbmsId parentId = getParentId((RdbmsId) domainObject.getId(), domainObjectTypeConfig);
        if (parentId == null) {
            return null;
        }

        GenericDomainObject parentObject = new GenericDomainObject(domainObject);
        parentObject.setId(parentId);
        parentObject.setTypeName(domainObjectTypeConfig.getExtendsAttribute());

        return update(parentObject, isUpdateStatus, changedFields);
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

    private String findInHierarchyDOTypeHavingField(String doType, String fieldName) {
        FieldConfig fieldConfig = configurationExplorer.getFieldConfig(doType, fieldName, false);
        if (fieldConfig != null) {
            return doType;
        } else {
            DomainObjectTypeConfig doTypeConfig = configurationExplorer.getConfig(DomainObjectTypeConfig.class, doType);
            if (doTypeConfig.getExtendsAttribute() != null) {
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

        String tableAlias = getSqlAlias(parentConfig.getName());

        for (FieldConfig fieldConfig : parentConfig.getFieldConfigs()) {
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

        if (parentConfig.getExtendsAttribute() != null) {
            appendParentColumns(query, parentConfig);
        } else {
            query.append(", ").append(wrap(CREATED_DATE_COLUMN));
            query.append(", ").append(wrap(UPDATED_DATE_COLUMN));
            query.append(", ").append(wrap(STATUS_FIELD_NAME));
            query.append(", ").append(wrap(STATUS_TYPE_COLUMN));
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
}
