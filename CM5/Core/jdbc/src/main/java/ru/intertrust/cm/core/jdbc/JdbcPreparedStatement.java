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
import java.util.Calendar;
import java.util.Hashtable;

import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.jdbc.JdbcDriver.ConnectMode;

public class JdbcPreparedStatement extends JdbcStatement implements PreparedStatement {
    private Hashtable<Integer, Object> parameters = new Hashtable<Integer, Object>();
    private String query;

    public JdbcPreparedStatement(ConnectMode mode, String address, String login, String password, String query) {
        super(mode, address, login, password);
        this.query = query;
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        try {
            //TODO Пока не поддерживаем выполнение запроса с параметрами приходится лепить запрос здесь, ждем  CMFIVE-182
            sql = query;
            int paramNum = 1;

            while (sql.contains("?")) {
                String value = "";
                if (parameters.get(paramNum) instanceof Integer) {
                    value = parameters.get(paramNum).toString();
                } else if (parameters.get(paramNum) instanceof Long) {
                    value = parameters.get(paramNum).toString();
                } else {
                    value = "'" + parameters.get(paramNum) + "'";
                }

                sql = sql.replaceFirst("\\?", value);
                paramNum++;
            }

            return new JdbcResultSet(this, getCollectionPartition());
        } catch (Exception ex) {
            throw new SQLException("Error on execute query", ex);
        }
    }

    @Override
    public int executeUpdate() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
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
    public void setFloat(int parameterIndex, float x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setString(int parameterIndex, String value) throws SQLException {
        addParameter(parameterIndex, value);
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
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
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void clearParameters() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean execute() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addBatch() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
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
        throw new UnsupportedOperationException();

    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        throw new UnsupportedOperationException();

    }

    private void addParameter(int parameterIndex, Object value) throws SQLException {
        parameters.put(parameterIndex, value);
    }

}
