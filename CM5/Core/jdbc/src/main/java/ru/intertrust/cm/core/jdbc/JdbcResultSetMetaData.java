package ru.intertrust.cm.core.jdbc;

import java.math.BigDecimal;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;

import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.StringFieldConfig;

public class JdbcResultSetMetaData implements ResultSetMetaData {

    private IdentifiableObjectCollection collection;

    public JdbcResultSetMetaData(IdentifiableObjectCollection collection) {
        this.collection = collection;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getColumnCount() throws SQLException {
        return collection.getFieldsConfiguration().size();

    }

    @Override
    public boolean isAutoIncrement(int column) throws SQLException {
        return false;

    }

    @Override
    public boolean isCaseSensitive(int column) throws SQLException {
        return false;

    }

    @Override
    public boolean isSearchable(int column) throws SQLException {
        return false;

    }

    @Override
    public boolean isCurrency(int column) throws SQLException {
        return false;
    }

    @Override
    public int isNullable(int column) throws SQLException {
        return getFieldConfig(column).isNotNull() ? columnNoNulls : columnNullable;
    }

    @Override
    public boolean isSigned(int column) throws SQLException {
        return false;
    }

    @Override
    public int getColumnDisplaySize(int column) throws SQLException {
        FieldConfig fieldConfig = getFieldConfig(column);
        int result = 0;
        if (fieldConfig.getFieldType() == FieldType.STRING){
            result = ((StringFieldConfig)fieldConfig).getLength();
        }else if (fieldConfig.getFieldType() == FieldType.TEXT){
            result = 1024; //Чтобы ширина поля в представлениях была не слишком узкой
        }else{
            result = 32;
        }

        return result;

    }

    @Override
    public String getColumnLabel(int column) throws SQLException {
        return getFieldConfig(column).getName();
    }

    @Override
    public String getColumnName(int column) throws SQLException {
        return getFieldConfig(column).getName();
    }

    @Override
    public String getSchemaName(int column) throws SQLException {
        return null;

    }

    @Override
    public int getPrecision(int column) throws SQLException {
        return 0;

    }

    @Override
    public int getScale(int column) throws SQLException {
        return 0;

    }

    @Override
    public String getTableName(int column) throws SQLException {
        return "";
    }

    @Override
    public String getCatalogName(int column) throws SQLException {
        return null;

    }

    @Override
    public int getColumnType(int column) throws SQLException {
        FieldConfig fieldConfig = getFieldConfig(column);

        int result = java.sql.Types.VARCHAR;
        if (fieldConfig.getFieldType() == FieldType.BOOLEAN) {
            result = java.sql.Types.BIT;
        } else if (fieldConfig.getFieldType() == FieldType.DATETIME) {
            result = java.sql.Types.TIMESTAMP;
        } else if (fieldConfig.getFieldType() == FieldType.DATETIMEWITHTIMEZONE) {
            result = java.sql.Types.TIMESTAMP;
        } else if (fieldConfig.getFieldType() == FieldType.DECIMAL) {
            result = java.sql.Types.DECIMAL;
        } else if (fieldConfig.getFieldType() == FieldType.LONG) {
            result = java.sql.Types.NUMERIC;
        } else if (fieldConfig.getFieldType() == FieldType.PASSWORD) {
            result = java.sql.Types.VARCHAR;
        } else if (fieldConfig.getFieldType() == FieldType.REFERENCE) {
            result = java.sql.Types.NUMERIC;
        } else if (fieldConfig.getFieldType() == FieldType.STRING) {
            result = java.sql.Types.VARCHAR;
        } else if (fieldConfig.getFieldType() == FieldType.TEXT) {
            result = java.sql.Types.VARCHAR;
        } else if (fieldConfig.getFieldType() == FieldType.TIMELESSDATE) {
            result = java.sql.Types.TIMESTAMP;
        } else {
            result = java.sql.Types.VARCHAR;
        }
        return result;
    }

    @Override
    public String getColumnTypeName(int column) throws SQLException {
        FieldConfig fieldConfig = getFieldConfig(column);
        return fieldConfig.getFieldType().name();
    }

    @Override
    public boolean isReadOnly(int column) throws SQLException {
        return true;

    }

    @Override
    public boolean isWritable(int column) throws SQLException {
        return false;

    }

    @Override
    public boolean isDefinitelyWritable(int column) throws SQLException {
        return false;

    }

    @Override
    public String getColumnClassName(int column) throws SQLException {
        FieldConfig fieldConfig = getFieldConfig(column);

        Class<?> result = null;
        if (fieldConfig.getFieldType() == FieldType.BOOLEAN) {
            result = Boolean.class;
        } else if (fieldConfig.getFieldType() == FieldType.DATETIME) {
            result = Timestamp.class;
        } else if (fieldConfig.getFieldType() == FieldType.DATETIMEWITHTIMEZONE) {
            result = Timestamp.class;
        } else if (fieldConfig.getFieldType() == FieldType.DECIMAL) {
            result = BigDecimal.class;
        } else if (fieldConfig.getFieldType() == FieldType.LONG) {
            result = Long.class;
        } else if (fieldConfig.getFieldType() == FieldType.PASSWORD) {
            result = String.class;
        } else if (fieldConfig.getFieldType() == FieldType.REFERENCE) {
            result = Long.class;
        } else if (fieldConfig.getFieldType() == FieldType.STRING) {
            result = String.class;
        } else if (fieldConfig.getFieldType() == FieldType.TEXT) {
            result = String.class;
        } else if (fieldConfig.getFieldType() == FieldType.TIMELESSDATE) {
            result = Timestamp.class;
        } else {
            result = String.class;
        }
        return result.getName();
    }

    private FieldConfig getFieldConfig(int column){
        return collection.getFieldsConfiguration().get(column - 1);
    }
}
