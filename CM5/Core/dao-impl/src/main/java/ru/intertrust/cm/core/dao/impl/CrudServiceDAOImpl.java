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

import ru.intertrust.cm.core.business.api.dto.BooleanValue;
import ru.intertrust.cm.core.business.api.dto.BusinessObject;
import ru.intertrust.cm.core.business.api.dto.DecimalValue;
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
import ru.intertrust.cm.core.dao.api.CrudServiceDAO;
import ru.intertrust.cm.core.dao.exception.ObjectNotFoundException;
import ru.intertrust.cm.core.dao.exception.OptimisticLockException;
import ru.intertrust.cm.core.dao.impl.utils.StrUtils;

public class CrudServiceDAOImpl implements CrudServiceDAO {

    private NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * Устанавливает источник соединений
     *
     * @param dataSource
     */
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public long generateNextSequence(BusinessObjectConfig businessObjectConfig) {

        String sequenceName = DataStructureNamingHelper.getSqlSequenceName(businessObjectConfig);

        StringBuilder query = new StringBuilder();
        query.append("select nextval ('");
        query.append(sequenceName);
        query.append("')");
        Long id = jdbcTemplate.queryForObject(query.toString(), new HashMap<String, Object>(), Long.class);

        return id.longValue();

    }

    @Override
    public BusinessObject create(BusinessObject businessObject, BusinessObjectConfig businessObjectConfig) {

        StringBuilder query = new StringBuilder();
        String tableName = businessObjectConfig.getName().replace(' ', '_').toUpperCase();
        String commaSeparatedFields = StrUtils.generateCommaSeparatedList(businessObject.getFields(), true);
        String commaSeparatedParameters = StrUtils.generateCommaSeparatedList(businessObject.getFields(), ":", false);

        query.append("insert into ").append(tableName).append(" (");
        query.append("ID , CREATED_DATE, UPDATED_DATE, ").append(commaSeparatedFields);
        query.append(") values (");
        query.append(":id , :created_date, :updated_date, ");
        query.append(commaSeparatedParameters);
        query.append(")");

        RdbmsId rdbmsId = (RdbmsId) businessObject.getId();

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("id", rdbmsId.getId());
        parameters.put("created_date", businessObject.getCreatedDate());
        parameters.put("updated_date", businessObject.getModifiedDate());

        for (String field : businessObject.getFields()) {
            Value value = businessObject.getValue(field);
            if (value != null)
                parameters.put(field, value.get());
            else
                parameters.put(field, null);

        }

        jdbcTemplate.update(query.toString(), parameters);

        return null;
    }

    @Override
    public BusinessObject update(BusinessObject businessObject, BusinessObjectConfig businessObjectConfig) {

        StringBuilder query = new StringBuilder();

        String tableName = businessObjectConfig.getName().replace(' ', '_').toUpperCase();

        String fieldsWithparams = StrUtils.generateCommaSeparatedListWithParams(businessObject.getFields(),
                businessObject.getFields());

        query.append("update ").append(tableName).append(" set ");
        query.append("updated_date=:current_date, ");
        query.append(fieldsWithparams);
        query.append(" where id=:id");
        query.append(" and updated_date=:updated_date");

        RdbmsId rdbmsId = (RdbmsId) businessObject.getId();

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("id", rdbmsId.getId());
        parameters.put("current_date", new Date());
        parameters.put("updated_date", businessObject.getModifiedDate());

        for (String field : businessObject.getFields()) {
            Value value = businessObject.getValue(field);
            parameters.put(field, value.get());

        }

        int count = jdbcTemplate.update(query.toString(), parameters);

        if (count == 0)
            throw new OptimisticLockException(businessObject);

        return null;

    }

    @Override
    public void delete(Id id, BusinessObjectConfig businessObjectConfig) throws ObjectNotFoundException {

        String tableName = businessObjectConfig.getName().replace(' ', '_').toUpperCase();

        StringBuilder query = new StringBuilder();
        query.append("delete from ");
        query.append(tableName);
        query.append(" where id=:id");

        RdbmsId rdbmsId = (RdbmsId) id;

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("id", rdbmsId.getId());

        int count = jdbcTemplate.update(query.toString(), parameters);

        if (count == 0)
            throw new ObjectNotFoundException(rdbmsId);

    }




    @Override
    public boolean exists(Id id, BusinessObjectConfig businessObjectConfig) {

        RdbmsId rdbmsId = (RdbmsId)id;

        String tableName = businessObjectConfig.getName().replace(' ', '_').toUpperCase();

        StringBuilder query = new StringBuilder();
        query.append("select id from ");
        query.append(tableName);
        query.append(" where id=:id");

        Map<String, Long> parameters = new HashMap<String, Long>();
        parameters.put("id", rdbmsId.getId());


        long total = jdbcTemplate.queryForObject(query.toString(), parameters, Long.class);

        return total > 0;

    }

    @Override
    public BusinessObject read(BusinessObject businessObject, BusinessObjectConfig businessObjectConfig) {
        return null;
    }

    public IdentifiableObjectCollection findCollectionByQuery(CollectionConfig collectionConfig, List<CollectionFilterConfig> filledFilterConfigs,
            SortOrder sortOrder, int offset, int limit) {
        CollectionQueryInitializer collectionQueryInitializer = new CollectionQueryInitializer();

        String collectionQuery = collectionQueryInitializer.initializeQuery(collectionConfig.getPrototype(), filledFilterConfigs, sortOrder, offset, limit);

        IdentifiableObjectCollection collection = jdbcTemplate.query(collectionQuery, new CollectionRowMapper(collectionConfig.getBusinessObjectTypeField(),
                collectionConfig.getIdField()));

        return collection;
    } 
    
    @SuppressWarnings("rawtypes")
    private class CollectionRowMapper implements ResultSetExtractor<IdentifiableObjectCollection> {

        private String businessObjectType;

        private String idField;

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

        private DataType getColumnType(String columnTypeName) {
            DataType result = null;
            if (columnTypeName.equals("int8")) {
                result = DataType.INTEGER;
            } else if (columnTypeName.equals("timestamp")) {
                result = DataType.DATETIME;
            } else if (columnTypeName.equals("varchar") || columnTypeName.equals("unknown") || columnTypeName.equals("text")) {
                result = DataType.STRING;
            } else if (columnTypeName.equals("bool")) {
                result = DataType.BOOLEAN;
            } else if (columnTypeName.equals("numeric")) {
                result = DataType.DECIMAL;
            }
            return result;
        }
        
        /**
         * Метаданные возвращаемых значений списка. Содержит названия колонок, их типы и имя колонки - первичного ключа для бизнес-объекта.
         * @author atsvetkov
         * 
         */
        private class ColumnModel {

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
     * 
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
