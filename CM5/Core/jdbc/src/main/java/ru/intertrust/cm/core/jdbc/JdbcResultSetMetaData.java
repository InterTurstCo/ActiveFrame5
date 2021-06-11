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

    private final IdentifiableObjectCollection collection;

    JdbcResultSetMetaData(IdentifiableObjectCollection collection) {
        this.collection = collection;
    }

    @Override
    public <T> T unwrap(Class<T> iface) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getColumnCount() {
        return collection.getFieldsConfiguration().size();
    }

    @Override
    public boolean isAutoIncrement(int column) {
        return false;
    }

    @Override
    public boolean isCaseSensitive(int column) {
        return false;
    }

    @Override
    public boolean isSearchable(int column) {
        return false;
    }

    @Override
    public boolean isCurrency(int column) {
        return false;
    }

    @Override
    public int isNullable(int column) {
        return getFieldConfig(column).isNotNull() ? columnNoNulls : columnNullable;
    }

    @Override
    public boolean isSigned(int column) {
        return false;
    }

    @Override
    public int getColumnDisplaySize(int column) {
        FieldConfig fieldConfig = getFieldConfig(column);
        int result;
        if (fieldConfig.getFieldType() == FieldType.STRING) {
            result = ((StringFieldConfig) fieldConfig).getLength();
        } else if (fieldConfig.getFieldType() == FieldType.TEXT) {
            result = 1024; //Чтобы ширина поля в представлениях была не слишком узкой
        } else {
            result = 32;
        }

        return result;
    }

    @Override
    public String getColumnLabel(int column) {
        return getFieldConfig(column).getName();
    }

    @Override
    public String getColumnName(int column) {
        return getFieldConfig(column).getName();
    }

    @Override
    public String getSchemaName(int column) {
        return null;
    }

    @Override
    public int getPrecision(int column) {
        return 0;
    }

    @Override
    public int getScale(int column) {
        return 0;
    }

    @Override
    public String getTableName(int column) throws SQLException {
        return "";
    }

    @Override
    public String getCatalogName(int column) {
        return null;
    }

    @Override
    public int getColumnType(int column) {
        FieldConfig fieldConfig = getFieldConfig(column);

        int result;
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
    public String getColumnTypeName(int column) {
        FieldConfig fieldConfig = getFieldConfig(column);
        return fieldConfig.getFieldType().name();
    }

    @Override
    public boolean isReadOnly(int column) {
        return true;
    }

    @Override
    public boolean isWritable(int column) {
        return false;
    }

    @Override
    public boolean isDefinitelyWritable(int column) {
        return false;
    }

    @Override
    public String getColumnClassName(int column) {
        FieldConfig fieldConfig = getFieldConfig(column);

        Class<?> result;
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

    private FieldConfig getFieldConfig(int column) {
        return collection.getFieldsConfiguration().get(column - 1);
    }
}
