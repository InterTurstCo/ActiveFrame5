package ru.intertrust.cm.core.dao.impl;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.StringUtils;

import ru.intertrust.cm.core.business.api.dto.BooleanValue;
import ru.intertrust.cm.core.business.api.dto.BusinessObject;
import ru.intertrust.cm.core.business.api.dto.DecimalValue;
import ru.intertrust.cm.core.business.api.dto.GenericBusinessObject;
import ru.intertrust.cm.core.business.api.dto.GenericIdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.IntegerValue;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.TimestampValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.BusinessObjectConfig;
import ru.intertrust.cm.core.config.CollectionConfig;
import ru.intertrust.cm.core.config.CollectionFilterConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.dao.api.CrudServiceDAO;
import ru.intertrust.cm.core.dao.api.IdGenerator;
import ru.intertrust.cm.core.dao.exception.InvalidIdException;
import ru.intertrust.cm.core.dao.exception.ObjectNotFoundException;
import ru.intertrust.cm.core.dao.exception.OptimisticLockException;
import ru.intertrust.cm.core.dao.impl.utils.DaoUtils;

/**
 * @author atsvetkov
 * 
 */

public class CrudServiceDAOImpl implements CrudServiceDAO {

    private NamedParameterJdbcTemplate jdbcTemplate;

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
     * Создает SQL запрос для создания бизнес-объекта
     * @param businessObject бизнес-объект
     * @param businessObjectConfig конфигуоация бизнес-объекта
     * @return строку запроса для создания бизнес-объекта с параметрами
     */
    protected String generateCreateQuery(BusinessObject businessObject, BusinessObjectConfig businessObjectConfig) {
        List<FieldConfig> feldConfigs = businessObjectConfig.getBusinessObjectFieldsConfig().getFieldConfigs();

        String tableName = DataStructureNamingHelper.getSqlName(businessObjectConfig);
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
    public BusinessObject create(BusinessObject businessObject, BusinessObjectConfig businessObjectConfig) {

        String query = generateCreateQuery(businessObject, businessObjectConfig);

        Object nextId = idGenerator.generatetId(businessObjectConfig);

        RdbmsId id = new RdbmsId(businessObject.getTypeName(), (Long) nextId);

        businessObject.setId(id);

        Map<String, Object> parameters = initializeCreateParameters(businessObject, businessObjectConfig);

        jdbcTemplate.update(query, parameters);

        return businessObject;
    }

    /**
     * Инициализирует параметры для для создания бизнес-объекта
     * @param businessObject бизнес-объект
     * @param businessObjectConfig конфигуоация бизнес-объекта
     * @return карту объектов содержащую имя параметра и его значение
     */
    protected Map<String, Object> initializeCreateParameters(BusinessObject businessObject,
            BusinessObjectConfig businessObjectConfig) {

        RdbmsId rdbmsId = (RdbmsId) businessObject.getId();

        Map<String, Object> parameters = initializeIdParameter(rdbmsId);
        parameters.put("created_date", businessObject.getCreatedDate());
        parameters.put("updated_date", businessObject.getModifiedDate());

        List<FieldConfig> feldConfigs = businessObjectConfig.getBusinessObjectFieldsConfig().getFieldConfigs();

        initializeBusinessParameters(businessObject, feldConfigs, parameters);

        return parameters;
    }

    private void initializeBusinessParameters(BusinessObject businessObject, List<FieldConfig> feldConfigs,
            Map<String, Object> parameters) {
        for (FieldConfig fieldConfig : feldConfigs) {
            Value value = businessObject.getValue(fieldConfig.getName());
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
     * Создает SQL запрос для модификации бизнес-объекта
     * @param businessObject бизнес-объект
     * @param businessObjectConfig конфигуоация бизнес-объекта
     * @return строку запроса для модиификации бизнес-объекта с параметрами
     */
    protected String generateUpdateQuery(BusinessObject businessObject, BusinessObjectConfig businessObjectConfig) {

        StringBuilder query = new StringBuilder();

        String tableName = DataStructureNamingHelper.getSqlName(businessObjectConfig);

        List<FieldConfig> feldConfigs = businessObjectConfig.getBusinessObjectFieldsConfig().getFieldConfigs();

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
     * Инициализирует параметры для для создания бизнес-объекта
     * @param businessObject бизнес-объект
     * @param businessObjectConfig конфигуоация бизнес-объекта
     * @return карту объектов содержащую имя параметра и его значение
     */
    protected Map<String, Object> initializeUpdateParameters(BusinessObject businessObject,
            BusinessObjectConfig businessObjectConfig, Date currentDate) {

        Map<String, Object> parameters = new HashMap<String, Object>();

        RdbmsId rdbmsId = (RdbmsId) businessObject.getId();

        parameters.put("id", rdbmsId.getId());
        parameters.put("current_date", currentDate);
        parameters.put("updated_date", businessObject.getModifiedDate());

        List<FieldConfig> feldConfigs = businessObjectConfig.getBusinessObjectFieldsConfig().getFieldConfigs();

        initializeBusinessParameters(businessObject, feldConfigs, parameters);

        return parameters;

    }

    @Override
    public BusinessObject update(BusinessObject businessObject, BusinessObjectConfig businessObjectConfig)
            throws InvalidIdException, ObjectNotFoundException,
            OptimisticLockException {

        String query = generateUpdateQuery(businessObject, businessObjectConfig);

        validateIdType(businessObject.getId());

        Date currentDate = new Date();

        Map<String, Object> parameters = initializeUpdateParameters(businessObject, businessObjectConfig, currentDate);

        int count = jdbcTemplate.update(query, parameters);

        if (count == 0 && (!exists(businessObject.getId(), businessObjectConfig))) {
            throw new ObjectNotFoundException(businessObject.getId());
        }

        if (count == 0)
            throw new OptimisticLockException(businessObject);

        businessObject.setModifiedDate(currentDate);

        return businessObject;

    }

    /**
     * Проверяет какого типа идентификатор
     * @param businessObject
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
     * Создает SQL запрос для удаления бизнес-объекта
     * @param businessObjectConfig конфигуоация бизнес-объекта
     * @return строку запроса для удаления бизнес-объекта с параметрами
     */
    protected String generateDeleteQuery(BusinessObjectConfig businessObjectConfig) {

        String tableName = DataStructureNamingHelper.getSqlName(businessObjectConfig);

        StringBuilder query = new StringBuilder();
        query.append("delete from ");
        query.append(tableName);
        query.append(" where id=:id");

        return query.toString();

    }

    @Override
    public void delete(Id id, BusinessObjectConfig businessObjectConfig) throws InvalidIdException,
            ObjectNotFoundException {

        String query = generateDeleteQuery(businessObjectConfig);

        validateIdType(id);

        RdbmsId rdbmsId = (RdbmsId) id;

        Map<String, Object> parameters = initializeIdParameter(rdbmsId);

        int count = jdbcTemplate.update(query, parameters);

        if (count == 0)
            throw new ObjectNotFoundException(rdbmsId);

    }

    /**
     * Создает SQL запрос для удаления всех бизнес-объектов
     * @param businessObjectConfig конфигуоация бизнес-объекта
     * @return строку запроса для удаления всех бизнес-объектов
     */
    protected String generateDeleteAllQuery(BusinessObjectConfig businessObjectConfig) {

        String tableName = DataStructureNamingHelper.getSqlName(businessObjectConfig);

        StringBuilder query = new StringBuilder();
        query.append("delete from ");
        query.append(tableName);

        return query.toString();

    }

    /**
     * Инициализирует параметры для удаления бизнес-объекта
     * @param id идентификатор бизнес-объектв для удаления
     * @return карту объектов содержащую имя параметра и его значение
     */
    protected Map<String, Object> initializeIdParameter(Id id) {
        RdbmsId rdbmsId = (RdbmsId) id;
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("id", rdbmsId.getId());
        return parameters;
    }

    /**
     * Создает SQL запрос для проверки существует ли бизнес-объекта
     * @param businessObjectConfig конфигуоация бизнес-объекта
     * @return строку запроса для удаления бизнес-объекта с параметрами
     */
    protected String generateExistsQuery(BusinessObjectConfig businessObjectConfig) {

        String tableName = DataStructureNamingHelper.getSqlName(businessObjectConfig);

        StringBuilder query = new StringBuilder();
        query.append("select id from ");
        query.append(tableName);
        query.append(" where id=:id");

        return query.toString();

    }

    /**
     * Инициализирует параметры для удаления бизнес-объекта
     * @param id идентификатор бизнес-объектв для удаления
     * @return карту объектов содержащую имя параметра и его значение
     */
    protected Map<String, Long> initializeExistsParameters(Id id) {

        RdbmsId rdbmsId = (RdbmsId) id;
        Map<String, Long> parameters = new HashMap<String, Long>();
        parameters.put("id", rdbmsId.getId());

        return parameters;
    }

    @Override
    public boolean exists(Id id, BusinessObjectConfig businessObjectConfig) throws InvalidIdException {

        String query = generateExistsQuery(businessObjectConfig);

        validateIdType(id);

        Map<String, Long> parameters = initializeExistsParameters(id);

        long total = jdbcTemplate.queryForObject(query.toString(), parameters, Long.class);

        return total > 0;

    }

    @Override
    public BusinessObject find(Id id) {
        RdbmsId rdbmsId = (RdbmsId) id;

        String tableName = DataStructureNamingHelper.getSqlName(((RdbmsId) id).getTypeName());

        StringBuilder query = new StringBuilder();
        query.append("select * from ").append(tableName).append(" where ID=:id ");
        Map<String, Object> parameters = initializeIdParameter(rdbmsId);

        return jdbcTemplate.query(query.toString(), parameters, new SingleObjectRowMapper(rdbmsId.getTypeName()));
    }

    @Override
    public List<BusinessObject> find(List<Id> ids) {
        if (ids == null || ids.size() == 0) {
            return new ArrayList<>();
        }
        String tableName = DataStructureNamingHelper.getSqlName(getBusinessObjectType(ids));

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

    private String getBusinessObjectType(List<Id> ids) {
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
     * ru.intertrust.cm.core.dao.api.CrudServiceDAO#findCollectionByQuery(ru.intertrust.cm.core.config.CollectionConfig,
     * java.util.List, ru.intertrust.cm.core.business.api.dto.SortOrder, int, int)}
     */
    @Override
    public IdentifiableObjectCollection findCollection(CollectionConfig collectionConfig,
            List<CollectionFilterConfig> filledFilterConfigs,
            SortOrder sortOrder, int offset, int limit) {
        String collectionQuery =
                getFindCollectionQuery(collectionConfig, filledFilterConfigs, sortOrder, offset, limit);

        IdentifiableObjectCollection collection =
                jdbcTemplate.query(collectionQuery,
                        new CollectionRowMapper(collectionConfig.getBusinessObjectTypeField(),
                                collectionConfig.getIdField()));

        return collection;
    }

    /**
     * Возвращает запрос, который используется в методе поиска коллекции бизнес-объектов
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
     * {@see
     * ru.intertrust.cm.core.dao.api.CrudServiceDAO#findCollectionCountByQuery(ru.intertrust.cm.core.config.CollectionConfig
     * , java.util.List, ru.intertrust.cm.core.business.api.dto.SortOrder)}
     */
    @Override
    public int findCollectionCount(CollectionConfig collectionConfig, List<CollectionFilterConfig> filledFilterConfigs) {
        String collectionQuery = getFindCollectionCountQuery(collectionConfig, filledFilterConfigs);

        return jdbcTemplate.getJdbcOperations().queryForObject(collectionQuery, Integer.class);
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

        private final String businessObjectType;

        private final String idField;

        public CollectionRowMapper(String businessObjectType, String idField) {
            this.businessObjectType = businessObjectType;
            this.idField = idField;
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
                int index = 0;
                int collectionIndex = 0;

                Id id = null;
                for (DataType fieldType : columnModel.getColumnTypes()) {
                    Value value = null;
                    if (DataType.ID.equals(fieldType)) {

                        Long longValue = rs.getLong(columnModel.getIdField());
                        if (!rs.wasNull()) {
                            id = new RdbmsId(businessObjectType, longValue);
                        } else {
                            throw new RuntimeException("Id field can not be null for object " + "business_object");
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
                    collectionIndex = index;

                    if (id != null) {
                        collection.setId(row, id);
                        collectionIndex = index == 0 ? 0 : index - 1;
                    }
                    if (value != null) {
                        collection.set(collectionIndex, row, value);
                    }
                    index++;
                }

                row++;
            }
            return collection;
        }
    }

    @SuppressWarnings("rawtypes")
    private class SingleObjectRowMapper extends BasicRowMapper implements ResultSetExtractor<BusinessObject> {

        private static final String DEFAULT_ID_FIELD = "id";

        private final String businessObjectType;

        private final String idField;

        public SingleObjectRowMapper(String businessObjectType) {
            this.businessObjectType = businessObjectType;
            this.idField = DEFAULT_ID_FIELD;
        }

        @Override
        public BusinessObject extractData(ResultSet rs) throws SQLException, DataAccessException {
            GenericBusinessObject object = new GenericBusinessObject();
            object.setTypeName(businessObjectType);

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
                Id id = null;
                int fieldIndex = 0;
                for (DataType fieldType : columnModel.getColumnTypes()) {
                    Value value = null;

                    if (DataType.ID.equals(fieldType)) {

                        Long longValue = rs.getLong(columnModel.getIdField());
                        if (!rs.wasNull()) {
                            id = new RdbmsId(businessObjectType, longValue);
                        } else {
                            throw new RuntimeException("Id field can not be null for object " + "business_object");
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

                    valueModel.setId(id);
                    valueModel.setValue(value);

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

        private class FieldValueModel {
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
    }

    @SuppressWarnings("rawtypes")
    private class MultipleObjectRowMapper extends BasicRowMapper implements ResultSetExtractor<List<BusinessObject>> {

        private static final String DEFAULT_ID_FIELD = "id";

        private final String businessObjectType;

        private final String idField;

        public MultipleObjectRowMapper(String businessObjectType) {
            this.businessObjectType = businessObjectType;
            this.idField = DEFAULT_ID_FIELD;
        }

        @Override
        public List<BusinessObject> extractData(ResultSet rs) throws SQLException, DataAccessException {
            List<BusinessObject> objects = new ArrayList<>();

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
                GenericBusinessObject object = new GenericBusinessObject();
                object.setTypeName(businessObjectType);
                int index = 0;
                Id id = null;
                int fieldIndex = 0;
                for (DataType fieldType : columnModel.getColumnTypes()) {
                    Value value = null;
                    if (DataType.ID.equals(fieldType)) {

                        Long longValue = rs.getLong(columnModel.getIdField());
                        if (!rs.wasNull()) {
                            id = new RdbmsId(businessObjectType, longValue);
                        } else {
                            throw new RuntimeException("Id field can not be null for object " + "business_object");
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

                    fieldIndex = index;

                    if (id != null) {
                        object.setId(id);
                        fieldIndex = index == 0 ? 0 : index - 1;
                    }
                    if (value != null) {
                        String columnName = columnModel.getColumnNames().get(fieldIndex);
                        object.setValue(columnName, value);

                    }
                    index++;
                }
                objects.add(object);
            }

            return objects;
        }
    }

    /**
     * Базовй класс для отображения {@link ResultSet} на бизнес-объекты
     * @author atsvetkov
     */
    private class BasicRowMapper {
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

        /**
         * Метаданные возвращаемых значений списка. Содержит названия колонок, их типы и имя колонки - первичного ключа
         * для бизнес-объекта.
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
     * Перечисление типов колонок в таблицах бизнес-объектов. Используется для удобства чтения полей бизнес-объектов.
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
