package ru.intertrust.cm.core.jdbc;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.TimestampValue;
import ru.intertrust.cm.core.business.api.dto.Value;

public class JdbcResultSetMetaData implements ResultSetMetaData {

    private IdentifiableObjectCollection collection;

    public JdbcResultSetMetaData(IdentifiableObjectCollection collection){
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
        return collection.getFields().size();

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
        return columnNullable;
    }

    @Override
    public boolean isSigned(int column) throws SQLException {
        return false;
    }

    @Override
    public int getColumnDisplaySize(int column) throws SQLException {
        return 0;

    }

    @Override
    public String getColumnLabel(int column) throws SQLException {
        return collection.getFields().get(column - 1);
    }

    @Override
    public String getColumnName(int column) throws SQLException {
        return collection.getFields().get(column - 1);
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
        int result = java.sql.Types.VARCHAR;
        if (collection.size() > 0){
            Value value = collection.get(0).getValue(getFieldName(column));
            if (value instanceof ReferenceValue){
                result = java.sql.Types.NUMERIC;
            }else if(value instanceof TimestampValue){
                result = java.sql.Types.TIMESTAMP;                
            }else if(value instanceof LongValue){
                result = java.sql.Types.NUMERIC;                
            }
        }
        return result;
    }

    private String getFieldName(int columnIndex) {
        List<String> fields = collection.getFields();
        return fields.get(columnIndex-1);
    }    
    
    @Override
    public String getColumnTypeName(int column) throws SQLException {
        return String.class.getName();

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
        if (collection.size() > 0){
            
        }else{
            
        }
        return String.class.getName();
    }

}
