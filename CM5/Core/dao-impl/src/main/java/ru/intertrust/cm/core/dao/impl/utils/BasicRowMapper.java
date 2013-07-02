package ru.intertrust.cm.core.dao.impl.utils;

import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.dao.impl.DataType;
import ru.intertrust.cm.core.model.FatalException;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Базовй класс для отображения {@link java.sql.ResultSet} на доменные объекты и коллекции.
 *
 * @author atsvetkov
 */
public class BasicRowMapper {

    protected final String domainObjectType;
    protected final String idField;

    public BasicRowMapper(String domainObjectType, String idField) {
        this.domainObjectType = domainObjectType;
        this.idField = idField;
    }

    /**
     * Отображает типы полей в базе на {@link ru.intertrust.cm.core.dao.impl.DataType}
     *
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
     *
     * @author atsvetkov
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
     *
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
