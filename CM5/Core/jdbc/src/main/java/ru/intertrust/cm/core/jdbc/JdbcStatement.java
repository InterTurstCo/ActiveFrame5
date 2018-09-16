package ru.intertrust.cm.core.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.jdbc.JdbcDriver.ConnectMode;

public class JdbcStatement implements Statement {
    public static final int COLLECTION_LIMIT = 5000;
    //public static final int COLLECTION_LIMIT = 5;
    private int collectionPartition = 0;
    private boolean closed = false;
    private int maxRows;
    private JdbcResultSet resultSet;
    protected SochiClient client;
    protected String sql = null;

    public JdbcStatement(SochiClient client) {
        this.client = client;
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
    public ResultSet executeQuery(String sql) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void close() throws SQLException {
        this.closed = true;
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getMaxRows() throws SQLException {
        return maxRows;
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
        this.maxRows = max;
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getQueryTimeout() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void cancel() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;

    }

    @Override
    public void clearWarnings() throws SQLException {
    }

    @Override
    public void setCursorName(String name) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean execute(String sql) throws SQLException {
        try {
            this.sql = sql;
            this.resultSet = new JdbcResultSet(this, getCollectionPartition());
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new SQLException(ex);
        }
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        return this.resultSet;

    }

    @Override
    public int getUpdateCount() throws SQLException {
        return 0;
    }

    @Override
    public boolean getMoreResults() throws SQLException {
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
    public int getResultSetConcurrency() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getResultSetType() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void addBatch(String sql) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void clearBatch() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int[] executeBatch() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public Connection getConnection() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean isClosed() throws SQLException {
        return closed;
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean isPoolable() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void closeOnCompletion() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        throw new UnsupportedOperationException();

    }

    public IdentifiableObjectCollection getCollectionPartition() throws Exception {
        return getCollectionPartition(null);
    }    
    
    public IdentifiableObjectCollection getCollectionPartition(List<Value> params) throws Exception {
        
        IdentifiableObjectCollection collection =
                client.getCollectionService().findCollectionByQuery(
                        sql, 
                        params != null ? params : Collections.EMPTY_LIST,
                        collectionPartition * COLLECTION_LIMIT, 
                        COLLECTION_LIMIT);
        collectionPartition++;
        return collection;
    }
    
    protected void resetPartition(){
        collectionPartition = 0;
    }
}
