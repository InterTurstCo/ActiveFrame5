package ru.intertrust.cm.core.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;

import ru.intertrust.cm.core.business.api.dto.*;

public class JdbcResultSet implements ResultSet {

    private IdentifiableObjectCollection collection;
    private int index = -1;
    private int resultsetIndex = -1;
    private boolean closed = false;
    private JdbcStatement statement = null;

    public JdbcResultSet(Statement statement, IdentifiableObjectCollection collection) {
        this.collection = collection;
        this.statement = (JdbcStatement) statement;
    }

    public JdbcResultSet(IdentifiableObjectCollection collection) {
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
    public boolean next() throws SQLException {
        try {
            index++;
            resultsetIndex++;
            //Проверка на то что коллекция закончилась и надо получить следующую партию данных
            if (collection.size() == index) {
                //Дозапрашиваем данные
                if (statement != null) {
                    collection = statement.getCollectionPartition();
                    index = 0;
                }
            }

            return collection.size() > index;
        } catch (Exception ex) {
            throw new SQLException("Error get next row", ex);
        }
    }

    @Override
    public void close() throws SQLException {
        this.closed = true;
    }

    @Override
    public boolean wasNull() throws SQLException {
        return false;
    }

    @Override
    public String getString(int columnIndex) throws SQLException {
        return getString(getFieldName(columnIndex));
    }

    private String getFieldName(int columnIndex) {
        return collection.getFieldsConfiguration().get(columnIndex - 1).getName();
    }

    @Override
    public boolean getBoolean(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public byte getByte(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public short getShort(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getInt(int columnIndex) throws SQLException {
        return getInt(getFieldName(columnIndex));
    }

    @Override
    public long getLong(int columnIndex) throws SQLException {
        return getLong(getFieldName(columnIndex));
    }

    @Override
    public float getFloat(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public double getDouble(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        return getBigDecimal(getFieldName(columnIndex));
    }

    @Override
    public byte[] getBytes(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public Date getDate(int columnIndex) throws SQLException {
        return getDate(getFieldName(columnIndex));
    }

    @Override
    public Time getTime(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        return getTimestamp(getFieldName(columnIndex));
    }

    @Override
    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public String getString(String columnLabel) throws SQLException {
        String result = null;
        Value value = collection.get(index).getValue(columnLabel);
        if (value != null && value.get() != null) {
            if (value instanceof ReferenceValue){
                result = ((ReferenceValue)value).get().toStringRepresentation();
            }else{
                result = value.get().toString();
            }
        }

        return result;
    }

    @Override
    public boolean getBoolean(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public byte getByte(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public short getShort(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getInt(String columnLabel) throws SQLException {
        Integer result = null;
        Value value = collection.get(index).getValue(columnLabel);
        if (value != null && value.get() != null) {
            if (value instanceof LongValue) {
                result = ((LongValue) value).get().intValue();
            }else {
                throw new SQLException("Value of column " + columnLabel + " is not int type");
            }
        }

        return result;
    }

    @Override
    public long getLong(String columnLabel) throws SQLException {
        Value value = collection.get(index).getValue(columnLabel);
        long result = 0;
        if (value instanceof LongValue) {
            result = ((LongValue) value).get();
        } else {
            throw new SQLException("Value of column " + columnLabel + " is not long type");
        }
        return result;
    }

    @Override
    public float getFloat(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public double getDouble(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
        return collection.get(index).getDecimal(columnLabel);
    }

    @Override
    public byte[] getBytes(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public Date getDate(String columnLabel) throws SQLException {
        java.util.Date result = collection.get(index).getTimestamp(columnLabel);

        if (result != null) {
            return new Date(result.getTime());
        } else
            return null;
    }

    @Override
    public Time getTime(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public Timestamp getTimestamp(String columnLabel) throws SQLException {
        Timestamp result = null;
        
        Value value = collection.get(index).getValue(columnLabel);
        if (value != null && value.get() != null) {
            if (value instanceof DateTimeValue){
                java.util.Date date = collection.get(index).getTimestamp(columnLabel);
                result = new Timestamp(date.getTime());
            }else if(value instanceof TimelessDateValue){
                TimelessDate date = collection.get(index).getTimelessDate(columnLabel);
                Calendar calendar = Calendar.getInstance();
                calendar.clear();
                calendar.set(Calendar.YEAR, date.getYear());
                calendar.set(Calendar.MONTH, date.getMonth());
                calendar.set(Calendar.DAY_OF_MONTH, date.getDayOfMonth());
                result = new Timestamp(calendar.getTime().getTime());
            }else if(value instanceof DateTimeWithTimeZoneValue){
                DateTimeWithTimeZone date = collection.get(index).getDateTimeWithTimeZone(columnLabel);
                Calendar calendar = Calendar.getInstance();
                calendar.clear();
                calendar.set(Calendar.YEAR, date.getYear());
                calendar.set(Calendar.MONTH, date.getMonth());
                calendar.set(Calendar.DAY_OF_MONTH, date.getDayOfMonth());
                calendar.set(Calendar.HOUR_OF_DAY, date.getHours());
                calendar.set(Calendar.MINUTE, date.getMinutes());
                calendar.set(Calendar.SECOND, date.getSeconds());
                calendar.set(Calendar.MILLISECOND, date.getMilliseconds());
                calendar.setTimeZone(TimeZone.getTimeZone(date.getTimeZoneContext().getTimeZoneId()));
                result = new Timestamp(calendar.getTime().getTime());
            }else{
                throw new SQLException("GetTimestamp can not get value of type " + value.getClass().getName());
            }
        }
        return result;
    }

    @Override
    public InputStream getAsciiStream(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public InputStream getUnicodeStream(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public InputStream getBinaryStream(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public String getCursorName() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return new JdbcResultSetMetaData(collection);
    }

    @Override
    public Object getObject(int columnIndex) throws SQLException {
        return getObject(getFieldName(columnIndex));
    }

    @Override
    public Object getObject(String columnLabel) throws SQLException {
        Value value = collection.get(index).getValue(columnLabel);
        Object result = null;
        if (value.get() != null) {
            if (value instanceof LongValue) {
                result = ((LongValue) value).get();
            } else if (value instanceof ReferenceValue) {
                result = ((ReferenceValue) value).get().toStringRepresentation();
            } else if (value instanceof DateTimeValue 
                    || value instanceof TimelessDateValue 
                    || value instanceof DateTimeWithTimeZoneValue) {
                result = getTimestamp(columnLabel);            
            }else{
                result = value.toString();
            }
        }
        return result;
    }

    @Override
    public int findColumn(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public Reader getCharacterStream(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public Reader getCharacterStream(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
        return collection.get(index).getDecimal(columnLabel);
    }

    @Override
    public boolean isBeforeFirst() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean isAfterLast() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean isFirst() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean isLast() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void beforeFirst() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void afterLast() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean first() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean last() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getRow() throws SQLException {
        return resultsetIndex + 1;

    }

    @Override
    public boolean absolute(int row) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean relative(int rows) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean previous() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getFetchDirection() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getFetchSize() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getType() throws SQLException {
        return ResultSet.TYPE_FORWARD_ONLY;
    }

    @Override
    public int getConcurrency() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean rowUpdated() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean rowInserted() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean rowDeleted() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateNull(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateByte(int columnIndex, byte x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateShort(int columnIndex, short x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateInt(int columnIndex, int x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateLong(int columnIndex, long x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateFloat(int columnIndex, float x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateDouble(int columnIndex, double x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateString(int columnIndex, String x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateBytes(int columnIndex, byte[] x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateDate(int columnIndex, Date x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateTime(int columnIndex, Time x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateObject(int columnIndex, Object x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateNull(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateBoolean(String columnLabel, boolean x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateByte(String columnLabel, byte x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateShort(String columnLabel, short x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateInt(String columnLabel, int x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateLong(String columnLabel, long x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateFloat(String columnLabel, float x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateDouble(String columnLabel, double x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateString(String columnLabel, String x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateBytes(String columnLabel, byte[] x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateDate(String columnLabel, Date x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateTime(String columnLabel, Time x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateObject(String columnLabel, Object x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void insertRow() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateRow() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void deleteRow() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void refreshRow() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void cancelRowUpdates() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void moveToInsertRow() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void moveToCurrentRow() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public Statement getStatement() throws SQLException {
        return this.statement;
    }

    @Override
    public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public Ref getRef(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public Blob getBlob(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public Clob getClob(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public Array getArray(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public Ref getRef(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public Blob getBlob(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public Clob getClob(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public Array getArray(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public Date getDate(String columnLabel, Calendar cal) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public Time getTime(String columnLabel, Calendar cal) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public URL getURL(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public URL getURL(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateRef(int columnIndex, Ref x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateRef(String columnLabel, Ref x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateBlob(int columnIndex, Blob x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateBlob(String columnLabel, Blob x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateClob(int columnIndex, Clob x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateClob(String columnLabel, Clob x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateArray(int columnIndex, Array x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateArray(String columnLabel, Array x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public RowId getRowId(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public RowId getRowId(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateRowId(int columnIndex, RowId x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateRowId(String columnLabel, RowId x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getHoldability() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean isClosed() throws SQLException {
        return closed;
    }

    @Override
    public void updateNString(int columnIndex, String nString) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateNString(String columnLabel, String nString) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public NClob getNClob(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public NClob getNClob(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public SQLXML getSQLXML(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public String getNString(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public String getNString(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public Reader getNCharacterStream(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public Reader getNCharacterStream(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateClob(int columnIndex, Reader reader) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateClob(String columnLabel, Reader reader) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateNClob(int columnIndex, Reader reader) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateNClob(String columnLabel, Reader reader) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
        throw new UnsupportedOperationException();

    }

}
