package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.StringUtils;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.model.CollectionConfig;
import ru.intertrust.cm.core.config.model.CollectionFilterConfig;
import ru.intertrust.cm.core.config.model.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.model.FieldConfig;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.IdGenerator;
import ru.intertrust.cm.core.dao.exception.InvalidIdException;
import ru.intertrust.cm.core.dao.exception.ObjectNotFoundException;
import ru.intertrust.cm.core.dao.exception.OptimisticLockException;
import ru.intertrust.cm.core.dao.impl.utils.DaoUtils;
import ru.intertrust.cm.core.model.FatalException;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

/**
 * @author atsvetkov
 *
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
     * @param configurationExplorer {@link #configurationExplorer}
     */
    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    /**
     * Создает SQL запрос для создания доменного объекта
     * @param domainObject доменный объект
     * @param domainObjectTypeConfig конфигурация доменного объекта
     * @return строку запроса для создания доменного объекта с параметрами
     */
    protected String generateCreateQuery(DomainObject domainObject, DomainObjectTypeConfig domainObjectTypeConfig) {
        List<FieldConfig> feldConfigs = domainObjectTypeConfig.getDomainObjectFieldsConfig().getFieldConfigs();

        String tableName = DataStructureNamingHelper.getSqlName(domainObjectTypeConfig);
        List<String> columnNames = DataStructureNamingHelper.getSqlName(feldConfigs);

        String commaSeparatedColumns = StringUtils.collectionToCommaDelimitedString(columnNames);
        String commaSeparatedParameters = DaoUtils.generateCommaSeparatedParameters(columnNames);

        StringBuilder query = new StringBuilder();
        query.append("insert into ").append(tableName).append(" (");
        query.append("ID , CREATED_DATE, UPDATED_DATE, ").append(commaSeparatedColumns);
        query.append(") values (");
        query.append(":id , :created_date, :updated_date, ");
        query.append(commaSeparatedParameters);
        query.append(")");

        return query.toString();

    }

    @Override
    public DomainObject create(DomainObject domainObject) {
        Date currentDate = new Date();
        domainObject.setCreatedDate(currentDate);
        domainObject.setModifiedDate(currentDate);

        DomainObjectTypeConfig domainObjectTypeConfig =
                configurationExplorer.getDomainObjectTypeConfig(domainObject.getTypeName());

        String query = generateCreateQuery(domainObject, domainObjectTypeConfig);

        Object nextId = idGenerator.generatetId(domainObjectTypeConfig);

        RdbmsId id = new RdbmsId(domainObject.getTypeName(), (Long) nextId);

        domainObject.setId(id);

        Map<String, Object> parameters = initializeCreateParameters(domainObject, domainObjectTypeConfig);

        jdbcTemplate.update(query, parameters);

        return domainObject;
    }

    @Override
    public DomainObject save(DomainObject domainObject) {
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

    /**
     * Инициализирует параметры для для создания доменного объекта
     * @param domainObject доменный объект
     * @param domainObjectTypeConfig конфигурация доменного объекта
     * @return карту объектов содержащую имя параметра и его значение
     */
    protected Map<String, Object> initializeCreateParameters(DomainObject domainObject,
            DomainObjectTypeConfig domainObjectTypeConfig) {

        RdbmsId rdbmsId = (RdbmsId) domainObject.getId();

        Map<String, Object> parameters = initializeIdParameter(rdbmsId);
        parameters.put("created_date", domainObject.getCreatedDate());
        parameters.put("updated_date", domainObject.getModifiedDate());

        List<FieldConfig> feldConfigs = domainObjectTypeConfig.getDomainObjectFieldsConfig().getFieldConfigs();

        initializeDomainParameters(domainObject, feldConfigs, parameters);

        return parameters;
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

    /**
     * Создает SQL запрос для модификации доменного объекта
     * @param domainObject доменный объект
     * @param domainObjectTypeConfig конфигурация доменного объекта
     * @return строку запроса для модиификации доменного объекта с параметрами
     */
    protected String generateUpdateQuery(DomainObject domainObject, DomainObjectTypeConfig domainObjectTypeConfig) {

        StringBuilder query = new StringBuilder();

        String tableName = DataStructureNamingHelper.getSqlName(domainObjectTypeConfig);

        List<FieldConfig> feldConfigs = domainObjectTypeConfig.getDomainObjectFieldsConfig().getFieldConfigs();

        List<String> columnNames = DataStructureNamingHelper.getSqlName(feldConfigs);

        String fieldsWithparams = DaoUtils.generateCommaSeparatedListWithParams(columnNames);

        query.append("update ").append(tableName).append(" set ");
        query.append("UPDATED_DATE=:current_date, ");
        query.append(fieldsWithparams);
        query.append(" where ID=:id");
        query.append(" and UPDATED_DATE=:updated_date");

        return query.toString();

    }

    /**
     * Инициализирует параметры для для создания доменного объекта
     * @param domainObject доменный объект
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

        List<FieldConfig> feldConfigs = domainObjectTypeConfig.getDomainObjectFieldsConfig().getFieldConfigs();

        initializeDomainParameters(domainObject, feldConfigs, parameters);

        return parameters;

    }

    @Override
    public DomainObject update(DomainObject domainObject) throws InvalidIdException, ObjectNotFoundException,
            OptimisticLockException {
        DomainObjectTypeConfig domainObjectTypeConfig =
                configurationExplorer.getDomainObjectTypeConfig(domainObject.getTypeName());

        String query = generateUpdateQuery(domainObject, domainObjectTypeConfig);

        validateIdType(domainObject.getId());

        Date currentDate = new Date();

        Map<String, Object> parameters = initializeUpdateParameters(domainObject, domainObjectTypeConfig, currentDate);

        int count = jdbcTemplate.update(query, parameters);

        if (count == 0 && (!exists(domainObject.getId()))) {
            throw new ObjectNotFoundException(domainObject.getId());
        }

        if (count == 0)
            throw new OptimisticLockException(domainObject);

        domainObject.setModifiedDate(currentDate);

        return domainObject;

    }

    /**
     * Проверяет какого типа идентификатор
     *
     */
    private void validateIdType(Id id) {
        if (id == null) {
            throw new InvalidIdException(id);
        }
        if (!(id instanceof RdbmsId)) {
            throw new InvalidIdException(id);
        }
    }

    /**
     * Создает SQL запрос для удаления доменного объекта
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

    @Override
    public void delete(Id id) throws InvalidIdException,ObjectNotFoundException {
        validateIdType(id);

        RdbmsId rdbmsId = (RdbmsId) id;

        DomainObjectTypeConfig domainObjectTypeConfig =
                configurationExplorer.getDomainObjectTypeConfig(rdbmsId.getTypeName());
        String query = generateDeleteQuery(domainObjectTypeConfig);

        Map<String, Object> parameters = initializeIdParameter(rdbmsId);

        int count = jdbcTemplate.update(query, parameters);

        if (count == 0)
            throw new ObjectNotFoundException(rdbmsId);

    }

    @Override
    public int delete(Collection<Id> ids) {
        // TODO как обрабатывать ошибки при удалении каждого доменного объекта...
        int count = 0;
        for(Id id : ids) {
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

    /**
     * Создает SQL запрос для удаления всех доменных объектов
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
     * Инициализирует параметры для удаления доменного объекта
     * @param id идентификатор доменного объекта для удаления
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
     * @param id идентификатор доменных объектов для удаления
     * @return карту объектов содержащую имя параметра и его значение
     */
    protected Map<String, Long> initializeExistsParameters(Id id) {

        RdbmsId rdbmsId = (RdbmsId) id;
        Map<String, Long> parameters = new HashMap<String, Long>();
        parameters.put("id", rdbmsId.getId());

        return parameters;
    }

    @Override
    public boolean exists(Id id) throws InvalidIdException {
        RdbmsId rdbmsId = (RdbmsId) id;

        String query = generateExistsQuery(rdbmsId.getTypeName());

        validateIdType(id);

        Map<String, Long> parameters = initializeExistsParameters(id);

        long total = jdbcTemplate.queryForObject(query.toString(), parameters, Long.class);

        return total > 0;

    }

    @Override
    public DomainObject find(Id id) {
        RdbmsId rdbmsId = (RdbmsId) id;

        String tableName = DataStructureNamingHelper.getSqlName(rdbmsId.getTypeName());

        StringBuilder query = new StringBuilder();
        query.append("select * from ").append(tableName).append(" where ID=:id ");
        Map<String, Object> parameters = initializeIdParameter(rdbmsId);

        return jdbcTemplate.query(query.toString(), parameters, new SingleObjectRowMapper(rdbmsId.getTypeName()));
    }

    @Override
    public List<DomainObject> find(List<Id> ids) {
        if (ids == null || ids.size() == 0) {
            return new ArrayList<>();
        }
        String tableName = DataStructureNamingHelper.getSqlName(getDomainObjectType(ids));

        String idsList = convertIdsToCommaSeparatedString(ids);
        StringBuilder query = new StringBuilder();
        query.append("select * from ").append(tableName).append(" where ID in ( " + idsList + " ) ");

        return jdbcTemplate.query(query.toString(), new MultipleObjectRowMapper(tableName));
    }

    private String convertIdsToCommaSeparatedString(List<Id> ids) {
        StringBuilder builder = new StringBuilder();
        int index = 0;
        for (Id id : ids) {
            RdbmsId rdbmsId = (RdbmsId) id;
            builder.append(rdbmsId.getId());
            if (index < ids.size() - 1) {
                builder.append(", ");
            }
            index++;
        }
        return builder.toString();
    }

    private String getDomainObjectType(List<Id> ids) {
        String typeName = null;
        for (Id id : ids) {
            RdbmsId rdbmsId = (RdbmsId) id;
            typeName = rdbmsId.getTypeName();
            break;
        }

        return typeName;
    }

    /*
     * {@see
     * ru.intertrust.cm.core.dao.api.DomainObjectDao#findCollectionByQuery(ru.intertrust.cm.core.config.model.CollectionConfig,
     * java.util.List, ru.intertrust.cm.core.business.api.dto.SortOrder, int, int)}
     */
    @Override
    public IdentifiableObjectCollection findCollection(CollectionConfig collectionConfig,
            List<CollectionFilterConfig> filledFilterConfigs, List<Filter> filterValues,
            SortOrder sortOrder, int offset, int limit) {
        String collectionQuery =
                getFindCollectionQuery(collectionConfig, filledFilterConfigs, sortOrder, offset, limit);

        Map<String, Object> parameters = new HashMap<String, Object>();

        fillFilterParameters(filterValues, parameters);

        IdentifiableObjectCollection collection =
                jdbcTemplate.query(collectionQuery, parameters,
                        new CollectionRowMapper(collectionConfig.getDomainObjectType(),
                                collectionConfig.getIdField()));

        return collection;
    }

    /**
     * Заполняет параметры. Имена параметров в формате имя_фильтра + ключ парметра, указанный в конфигурации.
     * @param filterValues
     * @param parameters
     */
    private void fillFilterParameters(List<Filter> filterValues, Map<String, Object> parameters) {
        if (filterValues != null) {
            for (Filter filter : filterValues) {
                for (Integer key : filter.getCriterionKeys()) {
                    String parameterName = filter.getFilter() + key;
                    Value value = filter.getCriterion(key);
                    parameters.put(parameterName, value.get());
                }
            }
        }
    }

    /**
     * Возвращает запрос, который используется в методе поиска коллекции доменных объектов
     * @param collectionConfig
     * @param filledFilterConfigs
     * @param sortOrder
     * @param offset
     * @param limit
     * @return
     */
    protected String getFindCollectionQuery(CollectionConfig collectionConfig,
            List<CollectionFilterConfig> filledFilterConfigs, SortOrder sortOrder,
            int offset, int limit) {
        CollectionQueryInitializer collectionQueryInitializer = new CollectionQueryInitializer();

        String collectionQuery =
                collectionQueryInitializer.initializeQuery(collectionConfig.getPrototype(), filledFilterConfigs,
                        sortOrder, offset, limit);
        return collectionQuery;
    }

    /*
     * {@see ru.intertrust.cm.core.dao.api.DomainObjectDao#findCollectionCountByQuery(ru.intertrust.cm.core.config.model.
     * CollectionConfig , java.util.List, ru.intertrust.cm.core.business.api.dto.SortOrder)}
     */
    @Override
    public int findCollectionCount(CollectionConfig collectionConfig, List<CollectionFilterConfig> filledFilterConfigs,
            List<Filter> filterValues) {
        String collectionQuery = getFindCollectionCountQuery(collectionConfig, filledFilterConfigs);

        Map<String, Object> parameters = new HashMap<String, Object>();

        fillFilterParameters(filterValues, parameters);

        return jdbcTemplate.queryForObject(collectionQuery, parameters, Integer.class);
    }

    /**
     * Возвращает запрос, который используется в методе поиска количества объектов в коллекции
     * @param collectionConfig
     * @param filledFilterConfigs
     * @return
     */
    protected String getFindCollectionCountQuery(CollectionConfig collectionConfig,
            List<CollectionFilterConfig> filledFilterConfigs) {
        CollectionQueryInitializer collectionQueryInitializer = new CollectionQueryInitializer();

        String collectionQuery =
                collectionQueryInitializer.initializeCountQuery(collectionConfig.getCountingPrototype(),
                        filledFilterConfigs);
        return collectionQuery;
    }

    /**
     * Отображает {@link ResultSet} на {@link IdentifiableObjectCollection}.
     * @author atsvetkov
     */
    @SuppressWarnings("rawtypes")
    private class CollectionRowMapper extends BasicRowMapper implements
            ResultSetExtractor<IdentifiableObjectCollection> {

        public CollectionRowMapper(String domainObjectType, String idField) {
            super(domainObjectType, idField);
        }

        @Override
        public IdentifiableObjectCollection extractData(ResultSet rs) throws SQLException, DataAccessException {
            IdentifiableObjectCollection collection = new GenericIdentifiableObjectCollection();

            ColumnModel columnModel = new ColumnModel();
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                String fieldName = rs.getMetaData().getColumnName(i);
                DataType fieldType = getColumnType(rs.getMetaData().getColumnTypeName(i));
                if (fieldName.equals(idField)) {
                    columnModel.setIdField(fieldName);
                    columnModel.getColumnTypes().add(DataType.ID);
                } else {
                    columnModel.getColumnNames().add(fieldName);
                    columnModel.getColumnTypes().add(fieldType);
                }

            }

            collection.setFields(columnModel.getColumnNames());

            int row = 0;
            while (rs.next()) {
                FieldValueModel valueModel = new FieldValueModel();

                int index = 0;
                int collectionIndex = 0;

                for (DataType fieldType : columnModel.getColumnTypes()) {
                    fillValueModel(valueModel, rs, columnModel, index, fieldType);

                    collectionIndex = index;

                    if (valueModel.getId() != null) {
                        collection.setId(row, valueModel.getId() );
                        collectionIndex = index == 0 ? 0 : index - 1;
                    }
                    if (valueModel.getValue() != null) {
                        collection.set(collectionIndex, row, valueModel.getValue());
                    }
                    index++;
                }

                row++;
            }
            return collection;
        }
    }

    /**
     * Отображает {@link ResultSet} на доменный объект {@link ru.intertrust.cm.core.business.api.dto.DomainObject}.
     * @author atsvetkov
     */
   @SuppressWarnings("rawtypes")
    private class SingleObjectRowMapper extends BasicRowMapper implements ResultSetExtractor<DomainObject> {

        private static final String DEFAULT_ID_FIELD = "id";

        public SingleObjectRowMapper(String domainObjectType) {
            super(domainObjectType, DEFAULT_ID_FIELD);
        }

        @Override
        public DomainObject extractData(ResultSet rs) throws SQLException, DataAccessException {
            GenericDomainObject object = new GenericDomainObject();
            object.setTypeName(domainObjectType);

            ColumnModel columnModel = new ColumnModel();
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                String fieldName = rs.getMetaData().getColumnName(i);
                DataType fieldType = getColumnType(rs.getMetaData().getColumnTypeName(i));
                if (fieldName.equalsIgnoreCase(idField)) {
                    columnModel.setIdField(fieldName);
                    columnModel.getColumnTypes().add(DataType.ID);
                } else {
                    columnModel.getColumnNames().add(fieldName);
                    columnModel.getColumnTypes().add(fieldType);
                }

            }

            while (rs.next()) {
                FieldValueModel valueModel = new FieldValueModel();
                int index = 0;
                int fieldIndex = 0;
                for (DataType fieldType : columnModel.getColumnTypes()) {

                    fillValueModel(valueModel, rs, columnModel, index, fieldType);

                    fieldIndex = index;

                    if (valueModel.getId() != null) {
                        object.setId(valueModel.getId());
                        fieldIndex = index == 0 ? 0 : index - 1;
                    }
                    if (valueModel.getValue() != null) {
                        String columnName = columnModel.getColumnNames().get(fieldIndex);
                        object.setValue(columnName, valueModel.getValue());

                    }
                    index++;
                }

            }
            return object;
        }

    }

    /**
     * Отображает {@link ResultSet} на список доменных объектов {@link List< ru.intertrust.cm.core.business.api.dto.DomainObject >}.
     * @author atsvetkov
     */
    @SuppressWarnings("rawtypes")
    private class MultipleObjectRowMapper extends BasicRowMapper implements ResultSetExtractor<List<DomainObject>> {

        private static final String DEFAULT_ID_FIELD = "id";

        public MultipleObjectRowMapper(String domainObjectType) {
            super(domainObjectType, DEFAULT_ID_FIELD);
        }

        @Override
        public List<DomainObject> extractData(ResultSet rs) throws SQLException, DataAccessException {
            List<DomainObject> objects = new ArrayList<>();

            ColumnModel columnModel = new ColumnModel();
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                String fieldName = rs.getMetaData().getColumnName(i);
                DataType fieldType = getColumnType(rs.getMetaData().getColumnTypeName(i));
                if (fieldName.equalsIgnoreCase(idField)) {
                    columnModel.setIdField(fieldName);
                    columnModel.getColumnTypes().add(DataType.ID);
                } else {
                    columnModel.getColumnNames().add(fieldName);
                    columnModel.getColumnTypes().add(fieldType);
                }

            }

            while (rs.next()) {
                GenericDomainObject object = new GenericDomainObject();
                FieldValueModel valueModel = new FieldValueModel();

                object.setTypeName(domainObjectType);
                int index = 0;
                int fieldIndex = 0;
                for (DataType fieldType : columnModel.getColumnTypes()) {
                    fillValueModel(valueModel, rs, columnModel, index, fieldType);

                    fieldIndex = index;

                    if (valueModel.getId() != null) {
                        object.setId(valueModel.getId());
                        fieldIndex = index == 0 ? 0 : index - 1;
                    }
                    if (valueModel.getValue() != null) {
                        String columnName = columnModel.getColumnNames().get(fieldIndex);
                        object.setValue(columnName, valueModel.getValue());

                    }
                    index++;
                }
                objects.add(object);
            }

            return objects;
        }
    }

    /**
     * Базовй класс для отображения {@link ResultSet} на доменные объекты и коллекции.
     * @author atsvetkov
     */
    private class BasicRowMapper {

        protected final String domainObjectType;

        protected final String idField;

        public BasicRowMapper(String domainObjectType, String idField) {
            this.domainObjectType = domainObjectType;
            this.idField = idField;
        }

        /**
         * Отображает типы полей в базе на {@link DataType}
         * @param columnTypeName
         * @return
         */
        protected DataType getColumnType(String columnTypeName) {
            DataType result = null;
            if (columnTypeName.equals("int8")) {
                result = DataType.INTEGER;
            } else if (columnTypeName.equals("timestamp")) {
                result = DataType.DATETIME;
            } else if (columnTypeName.equals("varchar") || columnTypeName.equals("unknown")
                    || columnTypeName.equals("text")) {
                result = DataType.STRING;
            } else if (columnTypeName.equals("bool")) {
                result = DataType.BOOLEAN;
            } else if (columnTypeName.equals("numeric")) {
                result = DataType.DECIMAL;
            }
            return result;
        }

        protected void fillValueModel(FieldValueModel valueModel, ResultSet rs, ColumnModel columnModel, int index,
                DataType fieldType) throws SQLException {
            Value value = null;
            Id id = null;
            if (DataType.ID.equals(fieldType)) {

                Long longValue = rs.getLong(columnModel.getIdField());
                if (!rs.wasNull()) {
                    id = new RdbmsId(domainObjectType, longValue);
                } else {
                    throw new FatalException("Id field can not be null for object " + "domain_object");
                }

            } else if (DataType.INTEGER.equals(fieldType)) {
                value = new DecimalValue();
                Long longValue = rs.getLong(index + 1);
                if (!rs.wasNull()) {
                    value = new IntegerValue(longValue);
                } else {
                    value = new IntegerValue();
                }

            } else if (DataType.DATETIME.equals(fieldType)) {
                Timestamp timestamp = rs.getTimestamp(index + 1);
                if (!rs.wasNull()) {
                    Date date = new Date(timestamp.getTime());
                    value = new TimestampValue(date);
                } else {
                    value = new TimestampValue();
                }

            } else if (DataType.STRING.equals(fieldType)) {
                String fieldValue = rs.getString(index + 1);
                if (!rs.wasNull()) {
                    value = new StringValue(fieldValue);
                } else {
                    value = new StringValue();
                }

            } else if (DataType.BOOLEAN.equals(fieldType)) {
                Boolean fieldValue = rs.getBoolean(index + 1);
                if (!rs.wasNull()) {
                    value = new BooleanValue(fieldValue);
                } else {
                    value = new BooleanValue();
                }

            } else if (DataType.DECIMAL.equals(fieldType)) {
                BigDecimal fieldValue = rs.getBigDecimal(index + 1);
                if (!rs.wasNull()) {
                    value = new DecimalValue(fieldValue);
                } else {
                    value = new DecimalValue();
                }
            }

            if (id != null) {
                valueModel.setId(id);
            }
            valueModel.setValue(value);
        }

        /**
         * Обертывает заполненное поле или поле id в доменном объекте.
         * @author atsvetkov
         *
         */
        protected class FieldValueModel {
            private Id id = null;

            private Value value = null;

            public Id getId() {
                return id;
            }

            public void setId(Id id) {
                this.id = id;
            }

            public Value getValue() {
                return value;
            }

            public void setValue(Value value) {
                this.value = value;
            }
        }

        /**
         * Метаданные возвращаемых значений списка. Содержит названия колонок, их типы и имя колонки-первичного ключа
         * для доменного объекта.
         * @author atsvetkov
         */
        protected class ColumnModel {

            private String idField;

            private List<String> columnNames;

            private List<DataType> columnTypes;

            public List<String> getColumnNames() {
                if (columnNames == null) {
                    columnNames = new ArrayList<String>();
                }
                return columnNames;
            }

            public List<DataType> getColumnTypes() {
                if (columnTypes == null) {
                    columnTypes = new ArrayList<DataType>();
                }
                return columnTypes;
            }

            public String getIdField() {
                return idField;
            }

            public void setIdField(String idField) {
                this.idField = idField;
            }
        }
    }

    /**
     * Перечисление типов колонок в таблицах доменных объектов. Используется для удобства чтения полей доменных объектов.
     * @author atsvetkov
     */
    private enum DataType {
        STRING("string"), INTEGER("int"), DECIMAL("decimal"), DATETIME("datetime"), BOOLEAN("boolean"), ID("id");

        private final String value;

        DataType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
