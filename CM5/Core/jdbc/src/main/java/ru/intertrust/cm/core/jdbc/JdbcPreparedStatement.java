package ru.intertrust.cm.core.jdbc;

import java.io.FileOutputStream;
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

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;

public class JdbcPreparedStatement extends JdbcStatement implements PreparedStatement {
    private Hashtable<Integer, Object> parameters = new Hashtable<Integer, Object>();
    private CollectionsService collectionService;
    private String query;
    
    public JdbcPreparedStatement(CollectionsService collectionService, String query){
        super(collectionService);
        this.collectionService = collectionService;
        this.query = query;
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        log("executeQuery " + collectionService  + " cl " + collectionService.getClass().getClassLoader());
        Object result = collectionService.findCollectionByQuery(query);
        
        log("Result " + result + " cl " + result.getClass().getClassLoader() + " orig " + IdentifiableObjectCollection.class.getClassLoader());
        
        IdentifiableObjectCollection collection = (IdentifiableObjectCollection)result;   
        return new JdbcResultSet(collection);
    }

    private void log(String message) {
        try {
            FileOutputStream stream = new FileOutputStream("C:/temp/jdbc.log", true);
            stream.write(message.getBytes());
            stream.write("\n".getBytes());
            stream.close();
        } catch (Exception ex) {
            ex.printStackTrace();
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
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        throw new UnsupportedOperationException();

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
