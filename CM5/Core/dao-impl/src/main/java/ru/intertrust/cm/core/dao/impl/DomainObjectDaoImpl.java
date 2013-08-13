package ru.intertrust.cm.core.dao.impl;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.InverseExpression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.Union;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.util.StringUtils;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.SortCriterion;
import ru.intertrust.cm.core.business.api.dto.SortCriterion.Order;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.impl.CrudServiceImpl;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.ConfigurationExplorerImpl;
import ru.intertrust.cm.core.config.ConfigurationSerializer;
import ru.intertrust.cm.core.config.TopLevelConfigurationCache;
import ru.intertrust.cm.core.config.model.Configuration;
import ru.intertrust.cm.core.config.model.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.model.FieldConfig;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.AccessType;
import ru.intertrust.cm.core.dao.access.DomainObjectAccessType;
import ru.intertrust.cm.core.dao.access.UserSubject;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.IdGenerator;
import ru.intertrust.cm.core.dao.exception.InvalidIdException;
import ru.intertrust.cm.core.dao.exception.ObjectNotFoundException;
import ru.intertrust.cm.core.dao.exception.OptimisticLockException;
import ru.intertrust.cm.core.dao.impl.access.AccessControlServiceImpl;
import ru.intertrust.cm.core.dao.impl.access.AccessControlServiceImpl.SimpleAccessToken;
import ru.intertrust.cm.core.dao.impl.access.AccessControlUtility;
import ru.intertrust.cm.core.dao.impl.access.PostgresDatabaseAccessAgent;
import ru.intertrust.cm.core.dao.impl.utils.DaoUtils;
import ru.intertrust.cm.core.dao.impl.utils.IdSorterByType;
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
        if(id == null){
            throw new IllegalArgumentException("Object id can not be null");
        }
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

        return jdbcTemplate.query(query.toString(), parameters, new SingleObjectRowMapper(rdbmsId.getTypeName(), configurationExplorer));
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

        return jdbcTemplate.query(query.toString(), parameters, new MultipleObjectRowMapper(domainObjectType, configurationExplorer));
    }

    @Override
    public List<DomainObject> findChildren(Id domainObjectId, String childType, AccessToken accessToken) {

        StringBuilder query = new StringBuilder();
        query.append("select t.* from ")
                .append(childType)
                .append(" t where parent = :parent_id");

        Map<String, Object> aclParameters = new HashMap<String, Object>();
        if (accessToken.isDeferred()) {
            String childAclReadTable = AccessControlUtility.getAclReadTableNameFor(childType);
            query.append(" and exists (select r.object_id from ").append(childAclReadTable).append(" r ");
            query.append("inner join group_member gm on r.group_id = gm.parent where gm.person_id = :user_id and r.object_id = t.id)");
            
            aclParameters = getAclParameters(accessToken);
        }

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("parent_id", ((RdbmsId) domainObjectId).getId());
        if (accessToken.isDeferred()) {
            parameters.putAll(aclParameters);
        }

        return jdbcTemplate.query(query.toString(), parameters, new MultipleObjectRowMapper(childType, configurationExplorer));
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
    
    private static String CONFIGURATION_SCHEMA_PATH = "config/configuration-test.xsd";
    private static String DOMAIN_OBJECTS_CONFIG_PATH = "config/domain-objects-test.xml";
    private static String SYSTEM_DOMAIN_OBJECTS_CONFIG_PATH = "test-config/system-domain-objects-test.xml";
    private static String COLLECTIONS_CONFIG_PATH = "config/collections-test.xml";

    private static String MODULES_CONFIG_FOLDER = "modules-configuration";
    private static String MODULES_CONFIG_PATH = "/modules-configuration-test.xml";
    private static String MODULES_CONFIG_SCHEMA_PATH = "config/modules-configuration-test.xsd";

    static ConfigurationSerializer createConfigurationSerializer(String configPath) throws Exception {
        TopLevelConfigurationCache.getInstance().build(); // Инициализируем кэш конфигурации тэг-класс

        ConfigurationSerializer configurationSerializer = new ConfigurationSerializer();
        Set<String> configPaths =
                new HashSet<>(Arrays.asList(configPath, COLLECTIONS_CONFIG_PATH));

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
        configurationExplorer.build();
        domainObjectDao.setConfigurationExplorer(configurationExplorer);
        
        domainObjectDao.setIdGenerator(idGenerator);
        
        CrudServiceImpl crudService = new CrudServiceImpl();        
        crudService.setAccessControlService(accessControlService);
        crudService.setDomainObjectDao(domainObjectDao);

        
        Id employeeId = new RdbmsId("Employee", 2);
//        employeeId = null;
        SimpleAccessToken accessToken = new AccessControlServiceImpl().new SimpleAccessToken(new UserSubject(1), employeeId, DomainObjectAccessType.READ, true);
 
       DomainObject domainObject = domainObjectDao.find(employeeId, accessToken);
        System.out.println("Find domainObject: " + domainObject);
        
        Id newEmployeeId = new RdbmsId("Employee", 5);
        
//        domainObject.setId(null);
//        crudService.save(domainObject);
        
        

        Id employee2Id = new RdbmsId("Employee", 2);
        Id departmentId = new RdbmsId("Department", 1);

        List<Id> employeeIds = new ArrayList<Id>();
        employeeIds.add(employeeId);
        employeeIds.add(employee2Id);
        employeeIds.add(departmentId);
        
        
        List<DomainObject> domainObjects = domainObjectDao.find(employeeIds, accessToken);
        System.out.println("Find list of domainObjects: " + domainObjects);
        
        Id assignmentId = new RdbmsId("assignment", 1);
        Id outDocId = new RdbmsId("outgoing_document", 1);
        
        List<DomainObject> childrenObjects = domainObjectDao.findChildren(outDocId, "Assignment", accessToken);
        System.out.println("children for outgoing_document: " + childrenObjects);
        

        CollectionsDaoImpl collectionsService = new CollectionsDaoImpl();
        collectionsService.setDataSource(dataSource);
        collectionsService.setConfigurationExplorer(configurationExplorer);
        
        SortOrder sortOrder = new SortOrder();
        sortOrder.add(new SortCriterion("id", Order.ASCENDING));
        
        List<Filter> filterValues = new ArrayList<Filter>();
        
        Filter filter = new Filter();
        filter.setFilter("byDepartment");
        filter.addCriterion(0, new StringValue("dep1"));
//        filterValues.add(filter);
        IdentifiableObjectCollection objectCollection = collectionsService.findCollection("Employees", filterValues, sortOrder, 0, 0, accessToken);
        System.out.println("Colection emploees: " + objectCollection);

         Boolean allowed = databaseAccessAgent.checkDomainObjectAccess(1, new RdbmsId("Employee", 1), DomainObjectAccessType.WRITE);
        System.out.println("Allowed: " + allowed);

        RdbmsId[] objectIds = new RdbmsId[3];
        objectIds[0] = new RdbmsId("Employee", 1);
        
        objectIds[1] = new RdbmsId("Department", 1);
        objectIds[2] = new RdbmsId("Employee", 2);
        
        Id[] allowedIds = databaseAccessAgent.checkMultiDomainObjectAccess(1, objectIds, DomainObjectAccessType.WRITE);        
        System.out.println("Allowed ids : " + Arrays.asList(allowedIds));
        
        AccessType[] types = new AccessType[2];
        types[0] = DomainObjectAccessType.WRITE;        
        types[1] = DomainObjectAccessType.DELETE;
        
        AccessType[] accessTypes = databaseAccessAgent.checkDomainObjectMultiAccess(1, new RdbmsId("Employee", 2), types);
        System.out.println("Allowed types : " + Arrays.asList(accessTypes));
        
        boolean isUserInGroup = databaseAccessAgent.checkUserGroup(1, "group2");
        System.out.println("User in group: " + isUserInGroup);        
        
        
        CCJSqlParserManager pm = new CCJSqlParserManager();
        String sql = "SELECT * FROM MY_TABLE1 t1, MY_TABLE2 t2 where 1 = 1 and t1.id = 1" ;
        Statement statement = pm.parse(new StringReader(sql));

        if (statement instanceof Select) {
            Select selectStatement = (Select) statement;
            TablesNamesFinder tablesNamesFinder = domainObjectDao.new TablesNamesFinder();
            
            
            List tableList = tablesNamesFinder.getTableList(selectStatement);
            for (Iterator iter = tableList.iterator(); iter.hasNext();) {
                System.out.println(iter.next());
            }
        }

        Select selectStatement = (Select) statement;
        Expression oldExpression = ((PlainSelect)selectStatement.getSelectBody()).getWhere();


        StringBuilder aclQuery = new StringBuilder();

        aclQuery.append(" exists (select r.object_id from ").append("employee_read").append(" r ");
        aclQuery.append("inner join group_member gm on r.group_id = gm.parent where gm.person_id = :user_id and r.object_id = ");
        aclQuery.append(" id ").append(")");

        
        
        Expression leftAclExpression = (Expression) new net.sf.jsqlparser.expression.StringValue(aclQuery.toString());
        net.sf.jsqlparser.expression.StringValue stringValue = new net.sf.jsqlparser.expression.StringValue(aclQuery.toString();

        stringValue.
        System.out.println("leftAclExpression : " + leftAclExpression);
        AndExpression newAndExpression = new AndExpression(leftAclExpression, oldExpression);
        
//        newAclAndExpression.setLeftExpression(expression);
        
        ((PlainSelect)selectStatement.getSelectBody()).setWhere(newAndExpression);

        String newSql = ((PlainSelect)selectStatement.getSelectBody()).toString();
        System.out.println("Modified query: " + newSql);
        
        
        System.out.println("Before modifuing : " + newSql);
        
        List selectItems = ((PlainSelect)selectStatement.getSelectBody()).getSelectItems();
        SelectExpressionItem selectItem = new SelectExpressionItem();
        selectItem.setAlias("TYPE");        
        selectItem.setExpression(new net.sf.jsqlparser.expression.StringValue("'EMPLOYEE'"));
                
        selectItems.add(selectItem);
        
        FromItem fromItem = ((PlainSelect)selectStatement.getSelectBody()).getFromItem();
        System.out.println("FromItem : " + fromItem.toString());
        

        System.out.println("After modifying : " + ((PlainSelect)selectStatement.getSelectBody()).toString());
        
        TablesNamesFinder tablesNamesFinder = domainObjectDao.new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(selectStatement);
                
        
    }
    
    
    private class TablesNamesFinder implements SelectVisitor, FromItemVisitor, ExpressionVisitor, ItemsListVisitor {
        private List tables;

        public List getTableList(Select select) {
            tables = new ArrayList();
            select.getSelectBody().accept(this);
            return tables;
        }

        public void visit(PlainSelect plainSelect) {
            plainSelect.getFromItem().accept(this);
            
            if (plainSelect.getJoins() != null) {
                for (Iterator joinsIt = plainSelect.getJoins().iterator(); joinsIt.hasNext();) {
                    Join join = (Join) joinsIt.next();
                    join.getRightItem().accept(this);
                }
            }
            if (plainSelect.getWhere() != null)
                plainSelect.getWhere().accept(this);

        }

        public void visit(Union union) {
            for (Iterator iter = union.getPlainSelects().iterator(); iter.hasNext();) {
                PlainSelect plainSelect = (PlainSelect) iter.next();
                visit(plainSelect);
            }
        }

        public void visit(Table tableName) {
            String tableWholeName = tableName.getWholeTableName();
            tables.add(tableWholeName);
        }

        public void visit(SubSelect subSelect) {
            subSelect.getSelectBody().accept(this);
        }

        public void visit(Addition addition) {
            visitBinaryExpression(addition);
        }

        public void visit(AndExpression andExpression) {
            visitBinaryExpression(andExpression);
        }

        public void visit(Between between) {
            between.getLeftExpression().accept(this);
            between.getBetweenExpressionStart().accept(this);
            between.getBetweenExpressionEnd().accept(this);
        }

        public void visit(Column tableColumn) {
        }

        public void visit(Division division) {
            visitBinaryExpression(division);
        }

        public void visit(DoubleValue doubleValue) {
        }

        public void visit(EqualsTo equalsTo) {
            visitBinaryExpression(equalsTo);
        }

        public void visit(Function function) {
        }

        public void visit(GreaterThan greaterThan) {
            visitBinaryExpression(greaterThan);
        }

        public void visit(GreaterThanEquals greaterThanEquals) {
            visitBinaryExpression(greaterThanEquals);
        }

        public void visit(InExpression inExpression) {
            inExpression.getLeftExpression().accept(this);
            inExpression.getItemsList().accept(this);
        }

        public void visit(InverseExpression inverseExpression) {
            inverseExpression.getExpression().accept(this);
        }

        public void visit(IsNullExpression isNullExpression) {
        }

        public void visit(JdbcParameter jdbcParameter) {
        }

        public void visit(LikeExpression likeExpression) {
            visitBinaryExpression(likeExpression);
        }

        public void visit(ExistsExpression existsExpression) {
            existsExpression.getRightExpression().accept(this);
        }

        public void visit(LongValue longValue) {
        }

        public void visit(MinorThan minorThan) {
            visitBinaryExpression(minorThan);
        }

        public void visit(MinorThanEquals minorThanEquals) {
            visitBinaryExpression(minorThanEquals);
        }

        public void visit(Multiplication multiplication) {
            visitBinaryExpression(multiplication);
        }

        public void visit(NotEqualsTo notEqualsTo) {
            visitBinaryExpression(notEqualsTo);
        }

        public void visit(NullValue nullValue) {
        }

        public void visit(OrExpression orExpression) {
            visitBinaryExpression(orExpression);
        }

        public void visit(Parenthesis parenthesis) {
            parenthesis.getExpression().accept(this);
        }
        

        public void visit(Subtraction subtraction) {
            visitBinaryExpression(subtraction);
        }

        public void visitBinaryExpression(BinaryExpression binaryExpression) {
            binaryExpression.getLeftExpression().accept(this);
            binaryExpression.getRightExpression().accept(this);
        }

        public void visit(ExpressionList expressionList) {
            for (Iterator iter = expressionList.getExpressions().iterator(); iter.hasNext();) {
                Expression expression = (Expression) iter.next();
                expression.accept(this);
            }

        }

        public void visit(DateValue dateValue) {
        }
        
        public void visit(TimestampValue timestampValue) {
        }
        
        public void visit(TimeValue timeValue) {
        }

        public void visit(CaseExpression caseExpression) {
        }

        public void visit(WhenClause whenClause) {
        }

        public void visit(AllComparisonExpression allComparisonExpression) {
            allComparisonExpression.GetSubSelect().getSelectBody().accept(this);
        }

        public void visit(AnyComparisonExpression anyComparisonExpression) {
            anyComparisonExpression.GetSubSelect().getSelectBody().accept(this);
        }

        public void visit(SubJoin subjoin) {
            subjoin.getLeft().accept(this);
            subjoin.getJoin().getRightItem().accept(this);
        }

        @Override
        public void visit(net.sf.jsqlparser.expression.StringValue arg0) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void visit(Concat arg0) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void visit(Matches arg0) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void visit(BitwiseAnd arg0) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void visit(BitwiseOr arg0) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void visit(BitwiseXor arg0) {
            // TODO Auto-generated method stub
            
        }

    }
}
