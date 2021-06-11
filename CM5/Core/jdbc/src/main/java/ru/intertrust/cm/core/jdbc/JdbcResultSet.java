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

import ru.intertrust.cm.core.business.api.dto.BooleanValue;
import ru.intertrust.cm.core.business.api.dto.DateTimeValue;
import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZone;
import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZoneValue;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.TimelessDate;
import ru.intertrust.cm.core.business.api.dto.TimelessDateValue;
import ru.intertrust.cm.core.business.api.dto.Value;

public class JdbcResultSet implements ResultSet {

    private IdentifiableObjectCollection collection;
    private int index = -1;
    private int resultsetIndex = -1;
    private boolean closed = false;
    private JdbcStatement statement = null;
    private boolean wasNull = false;

    JdbcResultSet(Statement statement, IdentifiableObjectCollection collection) {
        this.collection = collection;
        this.statement = (JdbcStatement) statement;
    }

    JdbcResultSet(IdentifiableObjectCollection collection) {
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
    public boolean wasNull() {
        return wasNull;
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
        return getBoolean(getFieldName(columnIndex));
    }

    @Override
    public byte getByte(int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public short getShort(int columnIndex) {
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
    public float getFloat(int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getDouble(int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex, int scale) {
        return getBigDecimal(getFieldName(columnIndex));
    }

    @Override
    public byte[] getBytes(int columnIndex) {
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
    public InputStream getAsciiStream(int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getUnicodeStream(int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getBinaryStream(int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getString(String columnLabel) throws SQLException {
        String result = null;
        Value<?> value = collection.get(index).getValue(columnLabel);
        if (value != null && value.get() != null) {
            if (value instanceof ReferenceValue) {
                result = ((ReferenceValue) value).get().toStringRepresentation();
            } else {
                result = value.get().toString();
            }
            wasNull = false;
        } else {
            wasNull = true;
        }

        return result;
    }

    @Override
    public boolean getBoolean(String columnLabel) throws SQLException {
        boolean result = false;
        Value<?> value = collection.get(index).getValue(columnLabel);
        if (value == null || value.get() == null) {
            wasNull = true;
        } else if (value instanceof BooleanValue) {
            result = ((BooleanValue) value).get();
            wasNull = false;
        } else {
            throw new SQLException("Value of column " + columnLabel + " is not boolean type");
        }

        return result;
    }

    @Override
    public byte getByte(String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public short getShort(String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getInt(String columnLabel) throws SQLException {
        int result = 0;
        Value<?> value = collection.get(index).getValue(columnLabel);
        if (value == null || value.get() == null) {
            wasNull = true;
        } else if (value instanceof LongValue) {
            result = ((LongValue) value).get().intValue();
            wasNull = false;
        } else {
            throw new SQLException("Value of column " + columnLabel + " is not int type");
        }

        return result;
    }

    @Override
    public long getLong(String columnLabel) throws SQLException {
        Value<?> value = collection.get(index).getValue(columnLabel);
        long result = 0;
        if (value == null || value.get() == null) {
            wasNull = true;
        } else if (value instanceof LongValue) {
            result = ((LongValue) value).get();
            wasNull = false;
        } else {
            throw new SQLException("Value of column " + columnLabel + " is not long type");
        }
        return result;
    }

    @Override
    public float getFloat(String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getDouble(String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel, int scale) {
        return getBigDecimal(columnLabel);
    }

    @Override
    public byte[] getBytes(String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date getDate(String columnLabel) throws SQLException {
        java.util.Date result = collection.get(index).getTimestamp(columnLabel);

        if (result != null) {
            wasNull = false;
            return new Date(result.getTime());
        } else {
            wasNull = true;
            return null;
        }
    }

    @Override
    public Time getTime(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Timestamp getTimestamp(String columnLabel) throws SQLException {
        Timestamp result = null;

        Value<?> value = collection.get(index).getValue(columnLabel);
        if (value != null && value.get() != null) {
            if (value instanceof DateTimeValue) {
                java.util.Date date = collection.get(index).getTimestamp(columnLabel);
                result = new Timestamp(date.getTime());
            } else if (value instanceof TimelessDateValue) {
                TimelessDate date = collection.get(index).getTimelessDate(columnLabel);
                Calendar calendar = Calendar.getInstance();
                calendar.clear();
                calendar.set(Calendar.YEAR, date.getYear());
                calendar.set(Calendar.MONTH, date.getMonth());
                calendar.set(Calendar.DAY_OF_MONTH, date.getDayOfMonth());
                result = new Timestamp(calendar.getTime().getTime());
            } else if (value instanceof DateTimeWithTimeZoneValue) {
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
            } else {
                throw new SQLException("GetTimestamp can not get value of type " + value.getClass().getName());
            }
            wasNull = false;
        } else {
            wasNull = true;
        }
        return result;
    }

    @Override
    public InputStream getAsciiStream(String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getUnicodeStream(String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getBinaryStream(String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SQLWarning getWarnings() {
        return null;
    }

    @Override
    public void clearWarnings() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getCursorName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResultSetMetaData getMetaData() {
        return new JdbcResultSetMetaData(collection);
    }

    @Override
    public Object getObject(int columnIndex) throws SQLException {
        return getObject(getFieldName(columnIndex));
    }

    @Override
    public Object getObject(String columnLabel) throws SQLException {
        Value<?> value = collection.get(index).getValue(columnLabel);
        Object result = null;
        if (value != null && value.get() != null) {
            if (value instanceof LongValue) {
                result = ((LongValue) value).get();
            } else if (value instanceof ReferenceValue) {
                result = ((ReferenceValue) value).get();
            } else if (value instanceof BooleanValue) {
                result = ((BooleanValue) value).get();
            } else if (value instanceof DateTimeValue
                    || value instanceof TimelessDateValue
                    || value instanceof DateTimeWithTimeZoneValue) {
                result = getTimestamp(columnLabel);
            } else {
                result = value.toString();
            }
            wasNull = false;
        } else {
            wasNull = true;
        }
        return result;
    }

    @Override
    public int findColumn(String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Reader getCharacterStream(int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Reader getCharacterStream(String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel) {
        BigDecimal result = collection.get(index).getDecimal(columnLabel);
        wasNull = result == null;
        return result;
    }

    @Override
    public boolean isBeforeFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAfterLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void beforeFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void afterLast() {
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
    public int getRow() {
        return resultsetIndex + 1;
    }

    @Override
    public boolean absolute(int row) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean relative(int rows) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean previous() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFetchDirection(int direction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getFetchDirection() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFetchSize(int rows) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getFetchSize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getType() throws SQLException {
        return ResultSet.TYPE_FORWARD_ONLY;
    }

    @Override
    public int getConcurrency() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean rowUpdated() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean rowInserted() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean rowDeleted() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNull(int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBoolean(int columnIndex, boolean x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateByte(int columnIndex, byte x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateShort(int columnIndex, short x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateInt(int columnIndex, int x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateLong(int columnIndex, long x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateFloat(int columnIndex, float x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateDouble(int columnIndex, double x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBigDecimal(int columnIndex, BigDecimal x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateString(int columnIndex, String x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBytes(int columnIndex, byte[] x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateDate(int columnIndex, Date x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateTime(int columnIndex, Time x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateTimestamp(int columnIndex, Timestamp x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, int length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, int length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, int length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateObject(int columnIndex, Object x, int scaleOrLength) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateObject(int columnIndex, Object x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNull(String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBoolean(String columnLabel, boolean x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateByte(String columnLabel, byte x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateShort(String columnLabel, short x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateInt(String columnLabel, int x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateLong(String columnLabel, long x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateFloat(String columnLabel, float x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateDouble(String columnLabel, double x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBigDecimal(String columnLabel, BigDecimal x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateString(String columnLabel, String x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBytes(String columnLabel, byte[] x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateDate(String columnLabel, Date x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateTime(String columnLabel, Time x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateTimestamp(String columnLabel, Timestamp x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, int length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, int length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, int length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateObject(String columnLabel, Object x, int scaleOrLength) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateObject(String columnLabel, Object x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void insertRow() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRow() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteRow() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void refreshRow() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cancelRowUpdates() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void moveToInsertRow() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void moveToCurrentRow() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Statement getStatement() {
        return this.statement;
    }

    @Override
    public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Ref getRef(int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Blob getBlob(int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Clob getClob(int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Array getArray(int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Ref getRef(String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Blob getBlob(String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Clob getClob(String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Array getArray(String columnLabel) {
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
    public Timestamp getTimestamp(int columnIndex, Calendar cal) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Timestamp getTimestamp(String columnLabel, Calendar cal) {
        throw new UnsupportedOperationException();
    }

    @Override
    public URL getURL(int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public URL getURL(String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRef(int columnIndex, Ref x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRef(String columnLabel, Ref x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBlob(int columnIndex, Blob x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBlob(String columnLabel, Blob x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateClob(int columnIndex, Clob x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateClob(String columnLabel, Clob x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateArray(int columnIndex, Array x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateArray(String columnLabel, Array x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RowId getRowId(int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RowId getRowId(String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRowId(int columnIndex, RowId x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRowId(String columnLabel, RowId x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getHoldability() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void updateNString(int columnIndex, String nString) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNString(String columnLabel, String nString) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNClob(int columnIndex, NClob nClob) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNClob(String columnLabel, NClob nClob) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NClob getNClob(int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NClob getNClob(String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SQLXML getSQLXML(int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SQLXML getSQLXML(String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateSQLXML(int columnIndex, SQLXML xmlObject) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateSQLXML(String columnLabel, SQLXML xmlObject) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getNString(int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getNString(String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Reader getNCharacterStream(int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Reader getNCharacterStream(String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x, long length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader, long length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, long length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, long length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, long length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, long length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, long length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, long length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream, long length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream, long length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateClob(int columnIndex, Reader reader, long length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateClob(String columnLabel, Reader reader, long length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader, long length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNClob(String columnLabel, Reader reader, long length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateClob(int columnIndex, Reader reader) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateClob(String columnLabel, Reader reader) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNClob(String columnLabel, Reader reader) {
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
