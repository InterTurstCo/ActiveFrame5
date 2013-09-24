package ru.intertrust.cm.core.dao.impl;

import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getSqlAlias;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getSqlName;
import static ru.intertrust.cm.core.dao.impl.utils.DaoUtils.generateParameter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.StringUtils;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.model.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.model.FieldConfig;
import ru.intertrust.cm.core.config.model.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.model.ReferenceFieldTypeConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.UserSubject;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.api.IdGenerator;
import ru.intertrust.cm.core.dao.exception.InvalidIdException;
import ru.intertrust.cm.core.dao.exception.ObjectNotFoundException;
import ru.intertrust.cm.core.dao.exception.OptimisticLockException;
import ru.intertrust.cm.core.dao.impl.access.AccessControlUtility;
import ru.intertrust.cm.core.dao.impl.utils.DaoUtils;
import ru.intertrust.cm.core.dao.impl.utils.IdSorterByType;
import ru.intertrust.cm.core.dao.impl.utils.MultipleIdRowMapper;
import ru.intertrust.cm.core.dao.impl.utils.MultipleObjectRowMapper;
import ru.intertrust.cm.core.dao.impl.utils.SingleObjectRowMapper;

/**
 * @author atsvetkov
 */

public class DomainObjectDaoImpl implements DomainObjectDao {

    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private IdGenerator idGenerator;

    private DomainObjectCacheServiceImpl domainObjectCacheService;

    @Autowired
    private DomainObjectTypeIdCache domainObjectTypeIdCache;

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    public void setDomainObjectCacheService(DomainObjectCacheServiceImpl domainObjectCacheService) {
        this.domainObjectCacheService = domainObjectCacheService;
    }
    
    public void setAccessControlService(AccessControlService accessControlService) {
        this.accessControlService = accessControlService;
    }

    /**
     * Устанавливает источник соединений
     *
     * @param dataSource
     */
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
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
     * @param configurationExplorer {@link #configurationExplorer}
     */
    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    public void setDomainObjectTypeIdCache(DomainObjectTypeIdCache domainObjectTypeIdCache) {
        this.domainObjectTypeIdCache = domainObjectTypeIdCache;
    }

    @Override
    public DomainObject create(DomainObject domainObject) {
        DomainObject createdObject =
                create(domainObject, domainObjectTypeIdCache.getId(domainObject.getTypeName()));
        domainObjectCacheService.putObjectToCache(createdObject);

        return createdObject;
    }

    @Override
    public DomainObject save(DomainObject domainObject) throws InvalidIdException, ObjectNotFoundException,
            OptimisticLockException {
        if (domainObject.isNew()) {
            return create(domainObject);
        }

        return update(domainObject);
    }

    @Override
    public List<DomainObject> save(List<DomainObject> domainObjects) {
        //todo this can be optimized with batches

        List<DomainObject> result = new ArrayList<>();

        for (DomainObject domainObject : domainObjects) {
            DomainObject newDomainObject;
            newDomainObject = save(domainObject);
            result.add(newDomainObject);
        }

        return result;

    }

    @Override
    public DomainObject update(DomainObject domainObject) throws InvalidIdException, ObjectNotFoundException,
            OptimisticLockException {
        GenericDomainObject updatedObject = new GenericDomainObject(domainObject);

        DomainObjectTypeConfig domainObjectTypeConfig =
                configurationExplorer.getConfig(DomainObjectTypeConfig.class, updatedObject.getTypeName());

        validateIdType(updatedObject.getId());
        validateMasterIdType(updatedObject, domainObjectTypeConfig);
        updateParentDO(domainObjectTypeConfig, domainObject);

        String query = generateUpdateQuery(domainObjectTypeConfig);

        Date currentDate = new Date();

        Map<String, Object> parameters = initializeUpdateParameters(updatedObject, domainObjectTypeConfig, currentDate);

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

        domainObjectCacheService.putObjectToCache(updatedObject);

        return updatedObject;

    }

    private List<String> getModifiedFieldNames(DomainObject domainObject) {
        AccessToken accessToken = accessControlService.createSystemAccessToken("DomainObjectDaoImpl");
        DomainObject originalDomainObject = find(domainObject.getId(), accessToken);
        
        List<String> modifiedFieldNames = new ArrayList<String>();
        for(String fieldName : domainObject.getFields()){
            Value originalValue = originalDomainObject.getValue(fieldName);
            Value newValue = domainObject.getValue(fieldName);
            if(!originalValue.equals(newValue)){
                modifiedFieldNames.add(fieldName);
            }
            
        }
        return modifiedFieldNames;
    }

    private Id findParentId(DomainObjectTypeConfig domainObjectTypeConfig, Id id) {
        if (!isDerived(domainObjectTypeConfig)) {
            return null;
        }

        String tableName = getSqlName(domainObjectTypeConfig);
        String query = "select " + PARENT_COLUMN + " from " + tableName + " where ID=:id";

        Map<String, Long> parametersMap = new HashMap<>();
        parametersMap.put("id", ((RdbmsId) id).getId());

        Long parentId = jdbcTemplate.queryForObject(query, parametersMap, Long.class);
        return new RdbmsId(domainObjectTypeIdCache.getId(domainObjectTypeConfig.getExtendsAttribute()), parentId);
    }

    @Override
    public void delete(Id id) throws InvalidIdException, ObjectNotFoundException {
        validateIdType(id);

        RdbmsId rdbmsId = (RdbmsId) id;

        DomainObjectTypeConfig domainObjectTypeConfig =
                configurationExplorer.getConfig(DomainObjectTypeConfig.class, getDOTypeName(rdbmsId.getTypeId()));

        if (isDerived(domainObjectTypeConfig)) {
            Id parentId = findParentId(domainObjectTypeConfig, id);
            delete(parentId);
        }

        String query = generateDeleteQuery(domainObjectTypeConfig);

        Map<String, Object> parameters = initializeIdParameter(rdbmsId);

        int count = jdbcTemplate.update(query, parameters);

        domainObjectCacheService.removeObjectFromCache(id);

        if (count == 0) {
            throw new ObjectNotFoundException(rdbmsId);
        }

    }

    @Override
    public int delete(Collection<Id> ids) {
        // todo: in a batch
        // TODO как обрабатывать ошибки при удалении каждого доменного объекта...
        int count = 0;
        for (Id id : ids) {
            try {
                delete(id);

                count++;
            } catch (ObjectNotFoundException e) {
                //ничего не делаем пока
            } catch (InvalidIdException e) {
                ////ничего не делаем пока
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
        long total = jdbcTemplate.queryForObject(query.toString(), parameters, Long.class);

        return total > 0;
    }

    @Override
    public DomainObject find(Id id, AccessToken accessToken) {
        if(id == null){
            throw new IllegalArgumentException("Object id can not be null");
        }

        DomainObject domainObject = domainObjectCacheService.getObjectToCache(id);
        if (domainObject != null) {
            return domainObject;
        }

        RdbmsId rdbmsId = (RdbmsId) id;
        String typeName = getDOTypeName(rdbmsId.getTypeId());

        String tableAlias = getSqlAlias(typeName);

        StringBuilder query = new StringBuilder();
        query.append("select ");
        appendColumnsQueryPart(query, typeName);
        query.append(" from ");
        appendTableNameQueryPart(query, typeName);
        query.append(" where ").append(tableAlias).append(".ID=:id ");

        Map<String, Object> aclParameters = new HashMap<String, Object>();
        if (accessToken.isDeferred()) {
            String aclReadTable = AccessControlUtility.getAclReadTableName(typeName);
            query.append(" and exists (select a.object_id from " + aclReadTable + " a inner join group_member gm " +
                    "on a.group_id = gm.master where gm.person_id1 = :user_id and a.object_id = :id)");
            aclParameters = getAclParameters(accessToken);
        }

        Map<String, Object> parameters = initializeIdParameter(rdbmsId);
        if (accessToken.isDeferred()) {
            parameters.putAll(aclParameters);
        }

        return jdbcTemplate.query(query.toString(), parameters,
                new SingleObjectRowMapper(typeName, configurationExplorer, domainObjectTypeIdCache));
    }

    @Override
    public List<DomainObject> find(List<Id> ids, AccessToken accessToken) {
        if (ids == null || ids.size() == 0) {
            return new ArrayList<>();
        }
        List<DomainObject> allDomainObjects = new ArrayList<DomainObject>();

        IdSorterByType idSorterByType = new IdSorterByType(ids.toArray(new RdbmsId[ids.size()]));

        for (final Integer domainObjectType : idSorterByType.getDomainObjectTypeIds()) {
            List<RdbmsId> idsOfSingleType = idSorterByType.getIdsOfType(domainObjectType);
            String doTypeName = domainObjectTypeIdCache.getName(domainObjectType);
            allDomainObjects.addAll(findSingleTypeDomainObjects(idsOfSingleType, accessToken, doTypeName));
        }

        return allDomainObjects;
    }

    /**
     * Поиск доменных объектов одного типа.
     *
     * @param ids              идентификаторы доменных объектов
     * @param accessToken      маркер доступа
     * @param domainObjectType тип доменного объекта
     * @return список доменных объектов
     */
    private List<DomainObject> findSingleTypeDomainObjects(List<RdbmsId> ids, AccessToken accessToken,
                                                           String domainObjectType) {
        List<DomainObject> domainObjects = domainObjectCacheService.getObjectToCache(ids);
        if (domainObjects != null) {
            return domainObjects;
        }

        StringBuilder query = new StringBuilder();

        Map<String, Object> aclParameters = new HashMap<String, Object>();

        if (accessToken.isDeferred()) {
            String aclReadTable = AccessControlUtility.getAclReadTableNameFor(domainObjectType);
            query.append("select distinct t.* from " + domainObjectType + " t inner join " + aclReadTable + " r " +
                    "on t.id = r.object_id inner join group_member gm on r.group_id = gm.master " +
                    "where gm.person_id1 = :user_id and t.id in (:object_ids) ");

            aclParameters = getAclParameters(accessToken);

        } else {
            query.append("select * from ").append(domainObjectType).append(" where ID in (:object_ids) ");
        }

        Map<String, Object> parameters = new HashMap<String, Object>();
        List<Long> listIds = AccessControlUtility.convertRdbmsIdsToLongIds(ids);
        parameters.put("object_ids", listIds);

        if (accessToken.isDeferred()) {
            parameters.putAll(aclParameters);
        }

        return jdbcTemplate.query(query.toString(), parameters, new MultipleObjectRowMapper(domainObjectType,
                configurationExplorer, domainObjectTypeIdCache));
    }

    @Deprecated
    @Override
    public List<DomainObject> findChildren(Id domainObjectId, String childType, AccessToken accessToken) {
        return findLinkedDomainObjects(domainObjectId, childType, "master", accessToken);
    }

    @Override
    public List<DomainObject> findLinkedDomainObjects(Id domainObjectId, String linkedType, String linkedField, AccessToken accessToken) {
        List<DomainObject> domainObjects = domainObjectCacheService.getObjectToCache(domainObjectId,
                linkedType, linkedField);
        if (domainObjects != null) {
            return domainObjects;
        }

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("domain_object_id", ((RdbmsId) domainObjectId).getId());
        String query = buildFindChildrenQuery(linkedType, linkedField, accessToken);
        if (accessToken.isDeferred()) {
            parameters.putAll(getAclParameters(accessToken));
        }
        return jdbcTemplate.query(query, parameters, new MultipleObjectRowMapper(linkedType, configurationExplorer,
                domainObjectTypeIdCache));
    }

    @Override
    public List<Id> findLinkedDomainObjectsIds(Id domainObjectId, String linkedType, String linkedField, AccessToken accessToken) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("domain_object_id", ((RdbmsId) domainObjectId).getId());
        String query = buildFindChildrenIdsQuery(linkedType, linkedField, accessToken);
        if (accessToken.isDeferred()) {
            parameters.putAll(getAclParameters(accessToken));
        }
        Integer linkedTypeId = domainObjectTypeIdCache.getId(linkedType);
        return jdbcTemplate.query(query, parameters, new MultipleIdRowMapper(linkedTypeId));
    }

    /**
     * Инициализирует параметры для для создания доменного объекта
     *
     * @param domainObject           доменный объект
     * @param domainObjectTypeConfig конфигурация доменного объекта
     * @return карту объектов содержащую имя параметра и его значение
     */
    protected Map<String, Object> initializeCreateParameters(DomainObject domainObject,
                                                             DomainObjectTypeConfig domainObjectTypeConfig,
                                                             Long parentId, Integer type) {

        RdbmsId rdbmsId = (RdbmsId) domainObject.getId();

        Map<String, Object> parameters = initializeIdParameter(rdbmsId);

        parameters.put("master", getMasterId(domainObject));

        if (!isDerived(domainObjectTypeConfig)) {
            parameters.put("created_date", domainObject.getCreatedDate());
            parameters.put("updated_date", domainObject.getModifiedDate());
            parameters.put("type", type);
        } else {
            parameters.put("parent", parentId);
        }

        List<FieldConfig> feldConfigs = domainObjectTypeConfig.getDomainObjectFieldsConfig().getFieldConfigs();

        initializeDomainParameters(domainObject, feldConfigs, parameters);

        return parameters;
    }

    /**
     * Создает SQL запрос для модификации доменного объекта
     *
     * @param domainObjectTypeConfig конфигурация доменного объекта
     * @return строку запроса для модиификации доменного объекта с параметрами
     */
    protected String generateUpdateQuery(DomainObjectTypeConfig domainObjectTypeConfig) {

        StringBuilder query = new StringBuilder();

        String tableName = getSqlName(domainObjectTypeConfig);

        List<FieldConfig> feldConfigs = domainObjectTypeConfig.getDomainObjectFieldsConfig().getFieldConfigs();

        List<String> columnNames = DataStructureNamingHelper.getColumnNames(feldConfigs);

        String fieldsWithparams = DaoUtils.generateCommaSeparatedListWithParams(columnNames);

        query.append("update ").append(tableName).append(" set ");

        if (!isDerived(domainObjectTypeConfig)) {
            query.append(UPDATED_DATE_COLUMN).append("=:current_date, ");
        }

        if(domainObjectTypeConfig.getParentConfig() != null) {
            query.append(MASTER_COLUMN).append("=:master, ");
        }

        query.append(fieldsWithparams);
        query.append(" where ").append(ID_COLUMN).append("=:id");

        if (!isDerived(domainObjectTypeConfig)) {
            query.append(" and ").append(UPDATED_DATE_COLUMN).append("=:updated_date");
        }

        return query.toString();

    }

    /**
     * Инициализирует параметры для для создания доменного объекта
     *
     * @param domainObject           доменный объект
     * @param domainObjectTypeConfig конфигурация доменного объекта
     * @return карту объектов содержащую имя параметра и его значение
     */
    protected Map<String, Object> initializeUpdateParameters(DomainObject domainObject,
                                                             DomainObjectTypeConfig domainObjectTypeConfig, Date currentDate) {

        Map<String, Object> parameters = new HashMap<String, Object>();

        RdbmsId rdbmsId = (RdbmsId) domainObject.getId();

        parameters.put("id", rdbmsId.getId());
        parameters.put("current_date", currentDate);
        parameters.put("updated_date", domainObject.getModifiedDate());
        parameters.put("master", getMasterId(domainObject));

        List<FieldConfig> fieldConfigs = domainObjectTypeConfig.getDomainObjectFieldsConfig().getFieldConfigs();

        initializeDomainParameters(domainObject, fieldConfigs, parameters);

        return parameters;

    }

    /**
     * Создает SQL запрос для создания доменного объекта
     *
     * @param domainObjectTypeConfig конфигурация доменного объекта
     * @return строку запроса для создания доменного объекта с параметрами
     */
    protected String generateCreateQuery(DomainObjectTypeConfig domainObjectTypeConfig) {
        List<FieldConfig> fieldConfigs = domainObjectTypeConfig.getFieldConfigs();

        String tableName = getSqlName(domainObjectTypeConfig);
        List<String> columnNames = DataStructureNamingHelper.getColumnNames(fieldConfigs);

        String commaSeparatedColumns = StringUtils.collectionToCommaDelimitedString(columnNames);
        String commaSeparatedParameters = DaoUtils.generateCommaSeparatedParameters(columnNames);

        StringBuilder query = new StringBuilder();
        query.append("insert into ").append(tableName).append(" (ID, ");

        if (!isDerived(domainObjectTypeConfig)) {
            query.append(CREATED_DATE_COLUMN).append(", ").append(UPDATED_DATE_COLUMN).append(", ");
            query.append(TYPE_COLUMN).append(", ");
        } else {
            query.append(PARENT_COLUMN).append(", ");
        }

        if (domainObjectTypeConfig.getParentConfig() != null) {
            query.append(MASTER_COLUMN).append(", ");
        }

        query.append(commaSeparatedColumns);
        query.append(") values (:id , ");

        if (!isDerived(domainObjectTypeConfig)) {
            query.append(":created_date, :updated_date, :type, ");
        } else {
            query.append(":parent, ");
        }

        if (domainObjectTypeConfig.getParentConfig() != null) {
            query.append(":master, ");
        }

        query.append(commaSeparatedParameters);
        query.append(")");

        return query.toString();

    }

    /**
     * Создает SQL запрос для удаления доменного объекта
     *
     * @param domainObjectTypeConfig конфигурация доменного объекта
     * @return строку запроса для удаления доменного объекта с параметрами
     */
    protected String generateDeleteQuery(DomainObjectTypeConfig domainObjectTypeConfig) {

        String tableName = getSqlName(domainObjectTypeConfig);

        StringBuilder query = new StringBuilder();
        query.append("delete from ");
        query.append(tableName);
        query.append(" where id=:id");

        return query.toString();

    }

    /**
     * Создает SQL запрос для удаления всех доменных объектов
     *
     * @param domainObjectTypeConfig конфигурация доменного объекта
     * @return строку запроса для удаления всех доменных объектов
     */
    protected String generateDeleteAllQuery(DomainObjectTypeConfig domainObjectTypeConfig) {

        String tableName = getSqlName(domainObjectTypeConfig);

        StringBuilder query = new StringBuilder();
        query.append("delete from ");
        query.append(tableName);

        return query.toString();

    }

    /**
     * Инициализирует параметр c id доменного объекта
     *
     * @param id идентификатор доменного объекта
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
     * @param domainObjectName название доменного объекта
     * @return строку запроса для удаления доменного объекта с параметрами
     */
    protected String generateExistsQuery(String domainObjectName) {

        String tableName = getSqlName(domainObjectName);

        StringBuilder query = new StringBuilder();
        query.append("select id from ");
        query.append(tableName);
        query.append(" where id=:id");

        return query.toString();

    }

    /**
     * Инициализирует параметры для удаления доменного объекта
     *
     * @param id идентификатор доменных объектов для удаления
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

    private void validateMasterIdType(DomainObject domainObject, DomainObjectTypeConfig config) {
        if (domainObject.getParent() == null) {
            return;
        }

        RdbmsId id = (RdbmsId) domainObject.getParent();
        String idType = getDOTypeName(id.getTypeId());
        String parentName = config.getParentConfig() != null ? config.getParentConfig().getName() : null;

        if (!idType.equals(parentName)) {
            String errorMessage = "Invalid parent id type: expected '" + parentName + "' but was '" + idType + "'";
            throw new InvalidIdException(errorMessage, id);
        }
    }

    private void initializeDomainParameters(DomainObject domainObject, List<FieldConfig> fieldConfigs,
                                            Map<String, Object> parameters) {
        for (FieldConfig fieldConfig : fieldConfigs) {
            Value value = domainObject.getValue(fieldConfig.getName());
            String columnName = getSqlName(fieldConfig.getName());
            String parameterName = generateParameter(columnName);

            if (value != null) {
                if (value instanceof ReferenceValue) {
                    RdbmsId rdbmsId = (RdbmsId) value.get();
                    parameterName =
                            generateParameter((ReferenceFieldConfig) fieldConfig, getDOTypeName(rdbmsId.getTypeId()));
                    parameters.put(parameterName, rdbmsId.getId());
                } else {
                    parameters.put(parameterName, value.get());
                }
            } else {
                parameters.put(parameterName, null);
            }

        }
    }

    private Long getMasterId(DomainObject domainObject) {
        if (domainObject.getParent() == null) {
            return null;
        }

        RdbmsId rdbmsParentId = (RdbmsId) domainObject.getParent();
        return rdbmsParentId.getId();
    }

    protected String buildFindChildrenQuery(String linkedType, String linkedField, AccessToken accessToken) {
        StringBuilder query = new StringBuilder();
        query.append("select t.* from ")
                .append(linkedType)
                .append(" t where t.").append(linkedField).append(" = :domain_object_id");
        if (accessToken.isDeferred()) {
            appendAccessControlLogicToQuery(query, linkedType);
        }
        return query.toString();
    }

    protected String buildFindChildrenIdsQuery(String linkedType, String linkedField, AccessToken accessToken) {
        StringBuilder query = new StringBuilder();
        query.append("select t.id from ")
                .append(linkedType)
                .append(" t where t.").append(linkedField).append(" = :domain_object_id");
        if (accessToken.isDeferred()) {
            appendAccessControlLogicToQuery(query, linkedType);
        }
        return query.toString();
    }

    private void appendAccessControlLogicToQuery(StringBuilder query, String linkedType) {
        String childAclReadTable = AccessControlUtility.getAclReadTableNameFor(linkedType);
        query.append(" and exists (select r.object_id from ").append(childAclReadTable).append(" r ");
        query.append("inner join group_member gm on r.group_id = gm.master where gm.person_id1 = :user_id and r" +
                ".object_id = t.id)");
    }

    private DomainObject create(DomainObject domainObject, Integer type) {
        DomainObjectTypeConfig domainObjectTypeConfig =
                configurationExplorer.getConfig(DomainObjectTypeConfig.class, domainObject.getTypeName());
        GenericDomainObject updatedObject = new GenericDomainObject(domainObject);
        validateMasterIdType(updatedObject, domainObjectTypeConfig);

        DomainObject parentDo = createParentDO(domainObject, domainObjectTypeConfig, type);

        Date currentDate = new Date();
        updatedObject.setCreatedDate(currentDate);
        updatedObject.setModifiedDate(currentDate);

        String query = generateCreateQuery(domainObjectTypeConfig);

        Object nextId = idGenerator.generatetId(domainObjectTypeConfig);

        RdbmsId id = new RdbmsId(domainObjectTypeIdCache.getId(updatedObject.getTypeName()), (Long) nextId);

        updatedObject.setId(id);

        Long parentDoId = parentDo != null ? ((RdbmsId) parentDo.getId()).getId() : null;
        Map<String, Object> parameters =
                initializeCreateParameters(updatedObject, domainObjectTypeConfig, parentDoId, type);

        jdbcTemplate.update(query, parameters);

        return updatedObject;
    }

    private DomainObject createParentDO(DomainObject domainObject, DomainObjectTypeConfig domainObjectTypeConfig,
                                        Integer type) {
        if (!isDerived(domainObjectTypeConfig)) {
            return null;
        }

        GenericDomainObject parentDO = new GenericDomainObject(domainObject);
        parentDO.setTypeName(domainObjectTypeConfig.getExtendsAttribute());
        return create(parentDO, type);
    }

    private void updateParentDO(DomainObjectTypeConfig domainObjectTypeConfig, DomainObject domainObject) {
        if (!isDerived(domainObjectTypeConfig)) {
            return;
        }

        GenericDomainObject parentObject = new GenericDomainObject(domainObject);
        parentObject.setId(findParentId(domainObjectTypeConfig, domainObject.getId()));
        parentObject.setTypeName(domainObjectTypeConfig.getExtendsAttribute());
        update(parentObject);
    }

    private void appendTableNameQueryPart(StringBuilder query, String typeName) {
        String tableName = getSqlName(typeName);
        query.append(tableName).append(" ").append(getSqlAlias(tableName));
        appendParentTable(query, typeName);
    }

    private void appendColumnsQueryPart(StringBuilder query, String typeName) {
        DomainObjectTypeConfig config = configurationExplorer.getConfig(DomainObjectTypeConfig.class, typeName);

        query.append(getSqlAlias(typeName)).append(".* ");

        if (config.getExtendsAttribute() != null) {
            appendParentColumns(query, config);;
        }
    }

    private void appendParentTable(StringBuilder query, String typeName) {
        DomainObjectTypeConfig config = configurationExplorer.getConfig(DomainObjectTypeConfig.class, typeName);

        if (config.getExtendsAttribute() == null) {
            return;
        }

        String tableAlias = getSqlAlias(typeName);

        String parentTableName = getSqlName(config.getExtendsAttribute());
        String parentTableAlias = getSqlAlias(config.getExtendsAttribute());

        query.append(" inner join ").append(parentTableName).append(" ").append(parentTableAlias);
        query.append(" on ").append(tableAlias).append(".").append(PARENT_COLUMN).append("=");
        query.append(parentTableAlias).append(".ID");

        appendParentTable(query, config.getExtendsAttribute());
    }

    private void appendParentColumns(StringBuilder query, DomainObjectTypeConfig config) {
        DomainObjectTypeConfig parentConfig = configurationExplorer.getConfig(DomainObjectTypeConfig.class,
                config.getExtendsAttribute());

        String tableAlias = getSqlAlias(parentConfig.getName());

        for (FieldConfig fieldConfig : parentConfig.getFieldConfigs()) {
            if ("ID".equals(fieldConfig.getName())) {
                continue;
            }

            if (fieldConfig instanceof ReferenceFieldConfig) {
                ReferenceFieldConfig referenceFieldConfig = (ReferenceFieldConfig) fieldConfig;
                for (ReferenceFieldTypeConfig typeConfig : referenceFieldConfig.getTypes()) {
                    String columnName = getSqlName(referenceFieldConfig, typeConfig);
                    query.append(", ").append(tableAlias).append(".").append(columnName);
                }
            } else {
                query.append(", ").append(tableAlias).append(".").append(getSqlName(fieldConfig));
            }
        }

        if (parentConfig.getExtendsAttribute() != null) {
            appendParentColumns(query, parentConfig);
        } else {
            query.append(", ").append(CREATED_DATE_COLUMN);
            query.append(", ").append(UPDATED_DATE_COLUMN);
            query.append(", ").append(TYPE_COLUMN);
        }
    }

    private boolean isDerived(DomainObjectTypeConfig domainObjectTypeConfig) {
        return domainObjectTypeConfig.getExtendsAttribute() != null;
    }

    private String getDOTypeName(Integer typeId) {
        return domainObjectTypeIdCache.getName(typeId);
    }

/*    private static String CONFIGURATION_SCHEMA_PATH = "config/configuration-test.xsd";
    private static String DOMAIN_OBJECTS_CONFIG_PATH = "config/domain-objects-test.xml";   
    private static String SYSTEM_DOMAIN_OBJECTS_CONFIG_PATH = "test-config/system-domain-objects-test.xml";
    private static String COLLECTIONS_CONFIG_PATH = "config/collections-test.xml";
    private static String ACCESS_CONFIG_PATH = "config/access-test.xml";
    private static String COLLECTIONS_VIEW_PATH = "config/collection-view-test.xml";

    private static String MODULES_CONFIG_FOLDER = "modules-configuration";
    private static String MODULES_CONFIG_PATH = "/modules-configuration-test.xml";
    private static String MODULES_CONFIG_SCHEMA_PATH = "config/modules-configuration-test.xsd";

    static ConfigurationSerializer createConfigurationSerializer(String configPath) throws Exception {
        TopLevelConfigurationCache.getInstance().build(); // Инициализируем кэш конфигурации тэг-класс

        ConfigurationSerializer configurationSerializer = new ConfigurationSerializer();
        Set<String> configPaths =
                new HashSet<>(Arrays.asList(configPath, COLLECTIONS_CONFIG_PATH, ACCESS_CONFIG_PATH, COLLECTIONS_VIEW_PATH));

        configurationSerializer.setCoreConfigurationFilePaths(configPaths);
        configurationSerializer.setCoreConfigurationSchemaFilePath(CONFIGURATION_SCHEMA_PATH);

        configurationSerializer.setModulesConfigurationFolder(MODULES_CONFIG_FOLDER);
        configurationSerializer.setModulesConfigurationPath(MODULES_CONFIG_PATH);
        configurationSerializer.setModulesConfigurationSchemaPath(MODULES_CONFIG_SCHEMA_PATH);

        return configurationSerializer;
    }
    
    public static void main(String[] args) throws Exception {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.xa.PGXADataSource");
        dataSource.setUrl("dbc:postgresql://localhost:5432/br4j22");
        dataSource.setUsername("br4j");
        dataSource.setPassword("welcome");
        
        
        DoelExpression expr = DoelExpression.parse("linkDoc2.fieldDoc3");
        DoelExpression.Element firstElement = expr.getElements()[0];
        
        System.out.print("Element class: " + firstElement.getClass());
        
        PostgresDatabaseAccessAgent databaseAccessAgent = new PostgresDatabaseAccessAgent();
        databaseAccessAgent.setDataSource(dataSource);
        
        AccessControlServiceImpl accessControlService = new AccessControlServiceImpl();                
        accessControlService.setDatabaseAgent(databaseAccessAgent);
                
        SequenceIdGenerator idGenerator = new SequenceIdGenerator();
        idGenerator.setDataSource(dataSource);
        
        DomainObjectDaoImpl domainObjectDao = new DomainObjectDaoImpl();
        domainObjectDao.setDataSource(dataSource);
        
        ConfigurationSerializer configurationSerializer = createConfigurationSerializer(DOMAIN_OBJECTS_CONFIG_PATH);

        Configuration configuration = configurationSerializer.deserializeConfiguration();
        ConfigurationExplorer configurationExplorer = new ConfigurationExplorerImpl(configuration);
        domainObjectDao.setConfigurationExplorer(configurationExplorer);
        
        domainObjectDao.setIdGenerator(idGenerator);
        
        CrudServiceImpl crudService = new CrudServiceImpl();        
        crudService.setAccessControlService(accessControlService);
        crudService.setDomainObjectDao(domainObjectDao);

        DomainObjectCacheServiceImpl cacheServiceImpl = new DomainObjectCacheServiceImpl(){
            @Override
            public DomainObject getObjectToCache(Id id) {
                return null;            
            }
            
            @Override
            public List<DomainObject> getObjectToCache(List<? extends Id> ids){
                return null;                
            }
            
            @Override
            public List<DomainObject> getObjectToCache(Id parentId, String ... key){
                return null;
            }
            
            @Override
            protected TransactionSynchronizationRegistry getTxReg(){
                return null;
            }
            
            @Override
            public Id putObjectToCache(DomainObject dobj) {
                return null;
            }
        };
        
        domainObjectDao.setDomainObjectCacheService(cacheServiceImpl);
        
        DomainObjectTypeIdDaoImpl domainObjectTypeIdDao = new DomainObjectTypeIdDaoImpl();
        domainObjectTypeIdDao.setDataSource(dataSource);
        
        DomainObjectTypeIdCacheImpl domainObjectTypeIdCache = new DomainObjectTypeIdCacheImpl();
        domainObjectTypeIdCache.setDomainObjectTypeIdDao(domainObjectTypeIdDao);        
        domainObjectTypeIdCache.build();
        
        domainObjectDao.setDomainObjectTypeIdCache(domainObjectTypeIdCache);
        
        Id employeeId = new RdbmsId(domainObjectTypeIdCache.getId("Employee"), 2);
//        employeeId = null;
        AccessToken accessToken = new AccessControlServiceImpl().createAccessToken(1, employeeId, DomainObjectAccessType.READ);
 
//       DomainObject domainObject = domainObjectDao.find(employeeId, accessToken);
//        System.out.println("Find domainObject: " + domainObject);
        
        Id newEmployeeId = new RdbmsId(domainObjectTypeIdCache.getId("Employee"), 5);
        
//        domainObject.setId(null);
//        crudService.save(domainObject);
        
        

        Id employee2Id = new RdbmsId(domainObjectTypeIdCache.getId("Employee"), 2);
        Id departmentId = new RdbmsId(domainObjectTypeIdCache.getId("Department"), 1);

        List<Id> employeeIds = new ArrayList<Id>();
        employeeIds.add(employeeId);
        employeeIds.add(employee2Id);
        employeeIds.add(departmentId);
        
        DoelResolver doelResolver = new DoelResolver();        
        doelResolver.setConfigurationExplorer(configurationExplorer);
        doelResolver.setDataSource(dataSource);

        DoelExpression doelExpr = DoelExpression.parse("Delegation^person");
//        List<Value> result = doelResolver.evaluate(doelExpr, employee2Id);
        
//        List<DomainObject> domainObjects = domainObjectDao.find(employeeIds, accessToken);
//        System.out.println("Find list of domainObjects: " + domainObjects);
        
//        Id assignmentId = new RdbmsId(domainObjectTypeIdCache.getId("assignment"), 1);
//        Id outDocId = new RdbmsId(domainObjectTypeIdCache.getId("outgoing_document"), 1);
        
//        List<DomainObject> childrenObjects = domainObjectDao.findChildren(outDocId, "Assignment", accessToken);
//        System.out.println("children for outgoing_document: " + childrenObjects);
        

        CollectionsDaoImpl collectionsService = new CollectionsDaoImpl();
        collectionsService.setDataSource(dataSource);
        collectionsService.setConfigurationExplorer(configurationExplorer);
        collectionsService.setDomainObjectTypeIdCache(domainObjectTypeIdCache);
        
        SortOrder sortOrder = new SortOrder();
        sortOrder.add(new SortCriterion("id", Order.ASCENDING));
        
        List<Filter> filterValues = new ArrayList<Filter>();
        
        Filter filter = new Filter();
        filter.setFilter("byDepartment");
        filter.addCriterion(0, new StringValue("dep1"));
//        filterValues.add(filter);
        IdentifiableObjectCollection objectCollection = collectionsService.findCollection("Employees", filterValues, sortOrder, 0, 0, accessToken);
        System.out.println("Colection emploees: " + objectCollection);

        Boolean allowed = databaseAccessAgent.checkDomainObjectAccess(1, new RdbmsId(domainObjectTypeIdCache.getId("Employee"), 1), DomainObjectAccessType.WRITE);
        System.out.println("Allowed: " + allowed);

        RdbmsId[] objectIds = new RdbmsId[3];
        objectIds[0] = new RdbmsId(domainObjectTypeIdCache.getId("Employee"), 1);
        
        objectIds[1] = new RdbmsId(domainObjectTypeIdCache.getId("Department"), 1);
        objectIds[2] = new RdbmsId(domainObjectTypeIdCache.getId("Employee"), 2);
        
        Id[] allowedIds = databaseAccessAgent.checkMultiDomainObjectAccess(1, objectIds, DomainObjectAccessType.WRITE);        
        System.out.println("Allowed ids : " + Arrays.asList(allowedIds));
        
        AccessType[] types = new AccessType[2];
        types[0] = DomainObjectAccessType.WRITE;        
        types[1] = DomainObjectAccessType.DELETE;
        
        AccessType[] accessTypes = databaseAccessAgent.checkDomainObjectMultiAccess(1, new RdbmsId(domainObjectTypeIdCache.getId("Employee"), 2), types);
        System.out.println("Allowed types : " + Arrays.asList(accessTypes));
        
        boolean isUserInGroup = databaseAccessAgent.checkUserGroup(1, "group2");
        System.out.println("User in group: " + isUserInGroup);
        
        AccessMatrixConfig accessMatrixConfig = configurationExplorer.getConfig(AccessMatrixConfig.class, "Outgoing document");
        System.out.println("accessMatrixConfig: " + accessMatrixConfig);
        
        DynamicGroupServiceImpl dynamicGroupService = new DynamicGroupServiceImpl();
        
        dynamicGroupService.setConfigurationExplorer(configurationExplorer);
        
        dynamicGroupService.setDomainObjectDao(domainObjectDao);
        dynamicGroupService.setDataSource(dataSource);
        
        dynamicGroupService.setDomainObjectTypeIdCache(domainObjectTypeIdCache);
        
        Id objectId = new RdbmsId(domainObjectTypeIdCache.getId("Delegation"), 1);
        List<String> modifiedFields = new ArrayList<String>();
        modifiedFields.add("person");
//        dynamicGroupService.notifyDomainObjectChanged(objectId, modifiedFields);
                
//        domainObjectDao.delete(objectId);
//        dynamicGroupService.cleanDynamicGroupsFor(objectId);               
                
        
    }*/
}
