package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import org.springframework.util.StringUtils;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.model.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.model.FieldConfig;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.UserSubject;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.IdGenerator;
import ru.intertrust.cm.core.dao.exception.InvalidIdException;
import ru.intertrust.cm.core.dao.exception.ObjectNotFoundException;
import ru.intertrust.cm.core.dao.exception.OptimisticLockException;
import ru.intertrust.cm.core.dao.impl.access.AccessControlUtility;
import ru.intertrust.cm.core.dao.impl.utils.*;

import javax.sql.DataSource;
import java.util.*;

/**
 * @author atsvetkov
 */

public class DomainObjectDaoImpl implements DomainObjectDao {

    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private IdGenerator idGenerator;

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

    @Override
    public DomainObject create(DomainObject domainObject) {
        GenericDomainObject updatedObject = new GenericDomainObject(domainObject);

        Date currentDate = new Date();
        updatedObject.setCreatedDate(currentDate);
        updatedObject.setModifiedDate(currentDate);

        DomainObjectTypeConfig domainObjectTypeConfig =
                configurationExplorer.getConfig(DomainObjectTypeConfig.class, updatedObject.getTypeName());

        validateParentIdType(updatedObject, domainObjectTypeConfig);

        String query = generateCreateQuery(domainObjectTypeConfig);

        Object nextId = idGenerator.generatetId(domainObjectTypeConfig);

        RdbmsId id = new RdbmsId(updatedObject.getTypeName(), (Long) nextId);

        updatedObject.setId(id);

        Map<String, Object> parameters = initializeCreateParameters(updatedObject, domainObjectTypeConfig);

        jdbcTemplate.update(query, parameters);

        return updatedObject;
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
        List<DomainObject> result = new ArrayList();

        for (DomainObject domainObject : domainObjects) {
            DomainObject newDomainObject;
            try {
                newDomainObject = save(domainObject);
                result.add(newDomainObject);
            } catch (Exception e) {
                // TODO: пока ничего не делаем...разобраться как обрабатывать ошибки
            }

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
        validateParentIdType(updatedObject, domainObjectTypeConfig);

        String query = generateUpdateQuery(domainObjectTypeConfig);

        Date currentDate = new Date();

        Map<String, Object> parameters = initializeUpdateParameters(updatedObject, domainObjectTypeConfig, currentDate);

        int count = jdbcTemplate.update(query, parameters);

        if (count == 0 && (!exists(updatedObject.getId()))) {
            throw new ObjectNotFoundException(updatedObject.getId());
        
        }

        if (count == 0) {
            throw new OptimisticLockException(updatedObject);
        }

        updatedObject.setModifiedDate(currentDate);

        return updatedObject;

    }

    @Override
    public void delete(Id id) throws InvalidIdException, ObjectNotFoundException {
        validateIdType(id);

        RdbmsId rdbmsId = (RdbmsId) id;

        DomainObjectTypeConfig domainObjectTypeConfig =
                configurationExplorer.getConfig(DomainObjectTypeConfig.class, rdbmsId.getTypeName());
        String query = generateDeleteQuery(domainObjectTypeConfig);

        Map<String, Object> parameters = initializeIdParameter(rdbmsId);

        int count = jdbcTemplate.update(query, parameters);

        if (count == 0) {
            throw new ObjectNotFoundException(rdbmsId);
        }

    }

    @Override
    public int delete(Collection<Id> ids) {
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
        RdbmsId rdbmsId = (RdbmsId) id;
        validateIdType(id);

        StringBuilder query = new StringBuilder();
        query.append(generateExistsQuery(rdbmsId.getTypeName()));

        Map<String, Object> parameters = initializeExistsParameters(id);        
        long total = jdbcTemplate.queryForObject(query.toString(), parameters, Long.class);

        return total > 0;
    }

    @Override
    public DomainObject find(Id id, AccessToken accessToken) {
        RdbmsId rdbmsId = (RdbmsId) id;

        String tableName = DataStructureNamingHelper.getSqlName(rdbmsId.getTypeName());

        StringBuilder query = new StringBuilder();
        query.append("select * from ").append(tableName).append(" where ID=:id ");
        
        Map<String, Object> aclParameters = new HashMap<String, Object>();
        if (accessToken.isDeferred()) {
            String aclReadTable = AccessControlUtility.getAclReadTableName(rdbmsId);
            query.append(" and exists (select a.object_id from " + aclReadTable + " a inner join group_member gm " +
                    "on a.group_id = gm.parent where gm.person_id = :user_id and a.object_id = :id)");
            aclParameters = getAclParameters(accessToken);
        }

        Map<String, Object> parameters = initializeIdParameter(rdbmsId);
        if (accessToken.isDeferred()) {
            parameters.putAll(aclParameters);
        }        

        return jdbcTemplate.query(query.toString(), parameters, new SingleObjectRowMapper(rdbmsId.getTypeName()));
    }

    @Override
    public List<DomainObject> find(List<Id> ids, AccessToken accessToken) {
        if (ids == null || ids.size() == 0) {
            return new ArrayList<>();
        }
        List<DomainObject> allDomainObjects = new ArrayList<DomainObject>();
        
        IdSorterByType idSorterByType = new IdSorterByType(ids.toArray(new RdbmsId[ids.size()]));

        for (final String domainObjectType : idSorterByType.getDomainObjectTypes()) {
            List<RdbmsId> idsOfSingleType = idSorterByType.getIdsOfType(domainObjectType);
            allDomainObjects.addAll(findSingleTypeDomainObjects(idsOfSingleType, accessToken, domainObjectType));
        }
        
        return allDomainObjects;
    }
    
    /**
     * Поиск доменных объектов одного типа.
     * @param ids идентификаторы доменных объектов
     * @param accessToken маркер доступа
     * @param domainObjectType тип доменного объекта
     * @return список доменных объектов
     */
    private List<DomainObject> findSingleTypeDomainObjects(List<RdbmsId> ids, AccessToken accessToken,
            String domainObjectType) {
        StringBuilder query = new StringBuilder();

        Map<String, Object> aclParameters = new HashMap<String, Object>();

        if (accessToken.isDeferred()) {
            String aclReadTable = AccessControlUtility.getAclReadTableNameFor(domainObjectType);
            query.append("select distinct t.* from " + domainObjectType + " t inner join " + aclReadTable + " r " +
                    "on t.id = r.object_id inner join group_member gm on r.group_id = gm.parent " +
                    "where gm.person_id = :user_id and t.id in (:object_ids) ");

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

        return jdbcTemplate.query(query.toString(), parameters, new MultipleObjectRowMapper(domainObjectType));
    }

    @Override
    public List<DomainObject> findChildren(Id domainObjectId, String childType, AccessToken accessToken) {
        String tableNameOfChild = DataStructureNamingHelper.getSqlName(childType);

        StringBuilder query = new StringBuilder();
        query.append("select t.* from ")
                .append(tableNameOfChild)
                .append(" t where parent = :parent_id");

        Map<String, Object> aclParameters = new HashMap<String, Object>();
        if (accessToken != null && accessToken.isDeferred()) {
            String childAclReadTable = AccessControlUtility.getAclReadTableNameFor(tableNameOfChild);
            query.append(" and exists (select r.object_id from ").append(childAclReadTable).append(" r ");
            query.append("inner join group_member gm on r.group_id = gm.parent where gm.person_id = :user_id and r.object_id = t.id)");
            
            aclParameters = getAclParameters(accessToken);
        }

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("parent_id", ((RdbmsId) domainObjectId).getId());
        if (accessToken.isDeferred()) {
            parameters.putAll(aclParameters);
        }

        return jdbcTemplate.query(query.toString(), parameters, new MultipleObjectRowMapper(tableNameOfChild));
    }

    /**
     * Инициализирует параметры для для создания доменного объекта
     *
     * @param domainObject           доменный объект
     * @param domainObjectTypeConfig конфигурация доменного объекта
     * @return карту объектов содержащую имя параметра и его значение
     */
    protected Map<String, Object> initializeCreateParameters(DomainObject domainObject,
                                                             DomainObjectTypeConfig domainObjectTypeConfig) {

        RdbmsId rdbmsId = (RdbmsId) domainObject.getId();

        Map<String, Object> parameters = initializeIdParameter(rdbmsId);

        parameters.put("parent", getParentId(domainObject));
        parameters.put("created_date", domainObject.getCreatedDate());
        parameters.put("updated_date", domainObject.getModifiedDate());

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

        String tableName = DataStructureNamingHelper.getSqlName(domainObjectTypeConfig);

        List<FieldConfig> feldConfigs = domainObjectTypeConfig.getDomainObjectFieldsConfig().getFieldConfigs();

        List<String> columnNames = DataStructureNamingHelper.getSqlName(feldConfigs);

        String fieldsWithparams = DaoUtils.generateCommaSeparatedListWithParams(columnNames);

        query.append("update ").append(tableName).append(" set ");
        query.append("UPDATED_DATE=:current_date, ");

        if(domainObjectTypeConfig.getParentConfig() != null) {
            query.append(PARENT_COLUMN).append("=:parent, ");
        }

        query.append(fieldsWithparams);
        query.append(" where ID=:id");
        query.append(" and UPDATED_DATE=:updated_date");

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
        parameters.put("parent", getParentId(domainObject));

        List<FieldConfig> feldConfigs = domainObjectTypeConfig.getDomainObjectFieldsConfig().getFieldConfigs();

        initializeDomainParameters(domainObject, feldConfigs, parameters);

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

        String tableName = DataStructureNamingHelper.getSqlName(domainObjectTypeConfig);
        List<String> columnNames = DataStructureNamingHelper.getSqlName(fieldConfigs);

        String commaSeparatedColumns = StringUtils.collectionToCommaDelimitedString(columnNames);
        String commaSeparatedParameters = DaoUtils.generateCommaSeparatedParameters(columnNames);

        StringBuilder query = new StringBuilder();
        query.append("insert into ").append(tableName).append(" (");
        query.append("ID, CREATED_DATE, UPDATED_DATE, ");

        if (domainObjectTypeConfig.getParentConfig() != null) {
            query.append(PARENT_COLUMN).append(", ");
        }

        query.append(commaSeparatedColumns);
        query.append(") values (");
        query.append(":id , :created_date, :updated_date, ");

        if (domainObjectTypeConfig.getParentConfig() != null) {
            query.append(":parent, ");
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

        String tableName = DataStructureNamingHelper.getSqlName(domainObjectTypeConfig);

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

        String tableName = DataStructureNamingHelper.getSqlName(domainObjectTypeConfig);

        StringBuilder query = new StringBuilder();
        query.append("delete from ");
        query.append(tableName);

        return query.toString();

    }

    /**
     * Инициализирует параметр c id доменного объекта
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

        String tableName = DataStructureNamingHelper.getSqlName(domainObjectName);

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
     * @param accessToken
     * @return
     */
    protected Map<String, Object> getAclParameters(AccessToken accessToken) {
        long userId = ((UserSubject)accessToken.getSubject()).getUserId();        
        Map<String, Object> parameters = new HashMap<String, Object>();
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

    private void validateParentIdType(DomainObject domainObject, DomainObjectTypeConfig config) {
        if(domainObject.getParent() == null) {
            return;
        }

        RdbmsId id = (RdbmsId) domainObject.getParent();
        String idType = id.getTypeName();
        String parentName = config.getParentConfig() != null ? config.getParentConfig().getName() : null;

        if(!idType.equals(parentName)) {
            String errorMessage = "Invalid parent id type: expected '" + parentName + "' but was '" + idType + "'";
            throw new InvalidIdException(errorMessage, id);
        }
    }

    private void initializeDomainParameters(DomainObject domainObject, List<FieldConfig> fieldConfigs,
                                            Map<String, Object> parameters) {
        for (FieldConfig fieldConfig : fieldConfigs) {
            Value value = domainObject.getValue(fieldConfig.getName());
            String columnName = DataStructureNamingHelper.getSqlName(fieldConfig.getName());
            String parameterName = DaoUtils.generateParameter(columnName);
            if (value != null) {
                parameters.put(parameterName, value.get());
            } else {
                parameters.put(parameterName, null);
            }

        }
    }

    private Long getParentId(DomainObject domainObject) {
        if(domainObject.getParent() == null) {
            return null;
        }

        RdbmsId rdbmsParentId = (RdbmsId) domainObject.getParent();
        return rdbmsParentId.getId();
    }
}
