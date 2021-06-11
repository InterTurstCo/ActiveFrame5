package ru.intertrust.cm.core.jdbc;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class JdbcConnection implements Connection {

    private boolean closed = false;
    private final SochiClient client;

    JdbcConnection(SochiClient client) throws SQLException {
        this.client = client;
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
    public Statement createStatement() {
        return new JdbcStatement(client);
    }

    @Override
    public PreparedStatement prepareStatement(String sql) {
        return new JdbcPreparedStatement(client, sql);
    }

    @Override
    public CallableStatement prepareCall(String sql) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String nativeSQL(String sql) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAutoCommit(boolean autoCommit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getAutoCommit() {
        return true;
    }

    @Override
    public void commit() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void rollback() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws SQLException {
        this.closed = true;
        client.close();
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public DatabaseMetaData getMetaData() {
        return new JdbcDatabaseMetaData(client);
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isReadOnly() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCatalog(String catalog) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getCatalog() {
        return "";
    }

    @Override
    public void setTransactionIsolation(int level) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getTransactionIsolation() {
        return Connection.TRANSACTION_READ_COMMITTED;
    }

    @Override
    public SQLWarning getWarnings() {
        return null;
    }

    @Override
    public void clearWarnings() {

    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Class<?>> getTypeMap() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHoldability(int holdability) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getHoldability() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Savepoint setSavepoint() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Savepoint setSavepoint(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Clob createClob() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Blob createBlob() {
        throw new UnsupportedOperationException();
    }

    @Override
    public NClob createNClob() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SQLXML createSQLXML() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isValid(int timeout) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setClientInfo(String name, String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setClientInfo(Properties properties) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getClientInfo(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Properties getClientInfo() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSchema(String schema) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getSchema() {
        return "";
    }

    @Override
    public void abort(Executor executor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getNetworkTimeout() {
        throw new UnsupportedOperationException();
    }
}
