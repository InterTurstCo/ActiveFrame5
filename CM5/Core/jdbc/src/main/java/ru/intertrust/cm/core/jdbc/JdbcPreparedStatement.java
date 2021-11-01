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
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.intertrust.cm.core.business.api.dto.BooleanValue;
import ru.intertrust.cm.core.business.api.dto.DateTimeValue;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.TimelessDate;
import ru.intertrust.cm.core.business.api.dto.TimelessDateValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.util.ListValue;

public class JdbcPreparedStatement extends JdbcStatement implements PreparedStatement {

    private final Map<Integer, Object> parameters = new HashMap<>();
    private final Map<Integer, Integer> nullParameterType = new HashMap<>();
    private final String query;

    private static final Object NULL = new Object();

    JdbcPreparedStatement(SochiClient client, String query) {
        super(client);
        this.query = query;
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        try {
            sql = query;
            // Заменяем знаки вопроса на нумерованные параметры
            int paramNum = 0;

            while (sql.contains("?")) {
                sql = sql.replaceFirst("\\?", "{" + paramNum + '}');
                paramNum++;
            }

            return new JdbcResultSet(this, getCollectionPartition(getParams()));
        } catch (Exception ex) {
            throw new SQLException("Error on execute query", ex);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Value<?>> getParams() {
        List<Value<?>> result = new ArrayList<>();

        for (int index = 1; index <= parameters.size(); index++) {
            Value<?> parameter;
            Object value = parameters.get(index);
            // на всякий случай оставил вариант value == null, хотя это больше похоже на ошибку
            if (value == NULL || value == null) {
                parameter = getNullValue(index);
            } else if (value instanceof Integer) {
                parameter = new LongValue((Integer) value);
            } else if (value instanceof Long) {
                parameter = new LongValue((Long) value);
            } else if (value instanceof Boolean) {
                parameter = new BooleanValue((Boolean) value);
            } else if (value instanceof Timestamp) {
                parameter = new DateTimeValue(new Date(((Timestamp) value).getTime()));
            } else if (value instanceof Date) {
                parameter = new DateTimeValue((Date) value);
            } else if (value instanceof TimelessDate) {
                parameter = new TimelessDateValue((TimelessDate) value);
            } else if (value instanceof Id) {
                parameter = new ReferenceValue((Id) value);
            } else if (value instanceof List) {
                List<Id> ids = (List<Id>) value;
                List<Value<?>> values = new ArrayList<>(ids.size());
                for (Id id : ids) {
                    values.add(new ReferenceValue(id));
                }
                parameter = ListValue.createListValue(values);
            } else {
                parameter = new StringValue(value.toString());
            }

            result.add(parameter);
        }

        return result;
    }

    /**
     * Получение значения null параметра с нужным типом
     */
    private Value<?> getNullValue(int index) {
        int type = nullParameterType.get(index);
        if (type == Types.INTEGER) {
            return new LongValue();
        }
        if (type == Types.BOOLEAN) {
            return new BooleanValue();
        }
        if (type == Types.DATE || type == Types.TIMESTAMP || type == Types.TIME) {
            return new DateTimeValue();
        }
        return new StringValue();
    }

    @Override
    public int executeUpdate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        addParameter(parameterIndex, NULL);
        nullParameterType.put(parameterIndex, sqlType);
    }

    @Override
    public void setBoolean(int parameterIndex, boolean value) throws SQLException {
        addParameter(parameterIndex, value);
    }

    @Override
    public void setByte(int parameterIndex, byte x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setShort(int parameterIndex, short x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLong(int parameterIndex, long value) throws SQLException {
        addParameter(parameterIndex, value);
    }

    @Override
    public void setFloat(int parameterIndex, float x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDouble(int parameterIndex, double x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setString(int parameterIndex, String value) throws SQLException {
        addParameter(parameterIndex, value);
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDate(int parameterIndex, Date value) throws SQLException {
        addParameter(parameterIndex, value);
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp value) throws SQLException {
        addParameter(parameterIndex, value);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearParameters() {
        parameters.clear();
        resetPartition();
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setObject(int parameterIndex, Object value) throws SQLException {
        addParameter(parameterIndex, value);
    }

    @Override
    public boolean execute() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addBatch() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRef(int parameterIndex, Ref x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBlob(int parameterIndex, Blob x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setClob(int parameterIndex, Clob x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResultSetMetaData getMetaData() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        addParameter(parameterIndex, NULL);
        nullParameterType.put(parameterIndex, sqlType);
    }

    @Override
    public void setURL(int parameterIndex, URL x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ParameterMetaData getParameterMetaData() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setNString(int parameterIndex, String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) {
        throw new UnsupportedOperationException();
    }

    private void addParameter(int parameterIndex, Object value) throws SQLException {
        if (value == null) {
            throw new SQLException("Use setNull method to set Null parameter value");
        }
        parameters.put(parameterIndex, value);
        resetPartition();
    }

    @Override
    public IdentifiableObjectCollection getCollectionPartition() throws Exception {
        return getCollectionPartition(getParams());
    }
}
