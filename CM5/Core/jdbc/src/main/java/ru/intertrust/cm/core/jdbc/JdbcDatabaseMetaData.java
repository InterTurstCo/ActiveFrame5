package ru.intertrust.cm.core.jdbc;

import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.business.api.dto.GenericIdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.config.DateTimeFieldConfig;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.LongFieldConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.StringFieldConfig;
import ru.intertrust.cm.core.jdbc.JdbcDriver.ConnectMode;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JdbcDatabaseMetaData implements DatabaseMetaData {

    private SochiClient client;

    public JdbcDatabaseMetaData(ConnectMode mode, String address, String login, String password) {
        client = new SochiClient(mode, address, login, password);
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
    public boolean allProceduresAreCallable() throws SQLException {
        return true;

    }

    @Override
    public boolean allTablesAreSelectable() throws SQLException {
        return true;

    }

    @Override
    public String getURL() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public String getUserName() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return true;

    }

    @Override
    public boolean nullsAreSortedHigh() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean nullsAreSortedLow() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean nullsAreSortedAtStart() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean nullsAreSortedAtEnd() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public String getDatabaseProductName() throws SQLException {
        return "Sochi Server";

    }

    @Override
    public String getDatabaseProductVersion() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDriverName() throws SQLException {
        return "Sochi JDBC Driver";
    }

    @Override
    public String getDriverVersion() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getDriverMajorVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getDriverMinorVersion() {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean usesLocalFiles() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean usesLocalFilePerTable() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsMixedCaseIdentifiers() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean storesUpperCaseIdentifiers() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean storesLowerCaseIdentifiers() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean storesMixedCaseIdentifiers() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public String getIdentifierQuoteString() throws SQLException {
        return " ";
    }

    @Override
    public String getSQLKeywords() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public String getNumericFunctions() throws SQLException {
        return null;

    }

    @Override
    public String getStringFunctions() throws SQLException {
        return null;

    }

    @Override
    public String getSystemFunctions() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public String getTimeDateFunctions() throws SQLException {
        return null;

    }

    @Override
    public String getSearchStringEscape() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public String getExtraNameCharacters() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsAlterTableWithAddColumn() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsAlterTableWithDropColumn() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsColumnAliasing() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean nullPlusNonNullIsNull() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsConvert() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsConvert(int fromType, int toType) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsTableCorrelationNames() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsDifferentTableCorrelationNames() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsExpressionsInOrderBy() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsOrderByUnrelated() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsGroupBy() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsGroupByUnrelated() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsGroupByBeyondSelect() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsLikeEscapeClause() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsMultipleResultSets() throws SQLException {
        return false;

    }

    @Override
    public boolean supportsMultipleTransactions() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsNonNullableColumns() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsMinimumSQLGrammar() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsCoreSQLGrammar() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsExtendedSQLGrammar() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsANSI92EntryLevelSQL() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsANSI92IntermediateSQL() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsANSI92FullSQL() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsIntegrityEnhancementFacility() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsOuterJoins() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsFullOuterJoins() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsLimitedOuterJoins() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public String getSchemaTerm() throws SQLException {
        return null;

    }

    @Override
    public String getProcedureTerm() throws SQLException {
        return null;

    }

    @Override
    public String getCatalogTerm() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean isCatalogAtStart() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public String getCatalogSeparator() throws SQLException {
        return "";
    }

    @Override
    public boolean supportsSchemasInDataManipulation() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSchemasInProcedureCalls() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsSchemasInTableDefinitions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSchemasInIndexDefinitions() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsCatalogsInDataManipulation() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsCatalogsInProcedureCalls() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsCatalogsInTableDefinitions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsPositionedDelete() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsPositionedUpdate() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsSelectForUpdate() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsStoredProcedures() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSubqueriesInComparisons() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsSubqueriesInExists() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsSubqueriesInIns() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsSubqueriesInQuantifieds() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsCorrelatedSubqueries() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsUnion() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsUnionAll() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getMaxBinaryLiteralLength() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getMaxCharLiteralLength() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getMaxColumnNameLength() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getMaxColumnsInGroupBy() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getMaxColumnsInIndex() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getMaxColumnsInOrderBy() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getMaxColumnsInSelect() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getMaxColumnsInTable() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getMaxConnections() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getMaxCursorNameLength() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getMaxIndexLength() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getMaxSchemaNameLength() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getMaxProcedureNameLength() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getMaxCatalogNameLength() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getMaxRowSize() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getMaxStatementLength() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getMaxStatements() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getMaxTableNameLength() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getMaxTablesInSelect() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getMaxUserNameLength() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getDefaultTransactionIsolation() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsTransactions() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsTransactionIsolationLevel(int level) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsDataManipulationTransactionsOnly() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern)
            throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern,
            String columnNamePattern) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types)
            throws SQLException {
        try {
            GenericIdentifiableObjectCollection collection = new GenericIdentifiableObjectCollection();
            List<FieldConfig> fields = new ArrayList<FieldConfig>();
            fields.add(getFieldConfig("TABLE_CAT", FieldType.STRING));
            fields.add(getFieldConfig("TABLE_SCHEM", FieldType.STRING));
            fields.add(getFieldConfig("TABLE_NAME", FieldType.STRING));
            fields.add(getFieldConfig("TABLE_TYPE", FieldType.STRING));
            fields.add(getFieldConfig("REMARKS", FieldType.STRING));
            fields.add(getFieldConfig("TYPE_CAT", FieldType.STRING));
            fields.add(getFieldConfig("TYPE_SCHEM", FieldType.STRING));
            fields.add(getFieldConfig("SELF_REFERENCING_COL_NAME", FieldType.STRING));
            fields.add(getFieldConfig("REF_GENERATION", FieldType.STRING));
            collection.setFieldsConfiguration(fields);

            Collection<DomainObjectTypeConfig> typeConfigs =
                    client.getConfigService().getConfigs(DomainObjectTypeConfig.class);
            int row = 0;
            for (DomainObjectTypeConfig domainObjectTypeConfig : typeConfigs) {
                //collection.set("TABLE_CAT", 0, new StringValue(""));
                //collection.set("TABLE_SCHEM", 0, new StringValue(""));
                collection.set("TABLE_TYPE", row, new StringValue("TABLE"));
                collection.set("TABLE_NAME", row, new StringValue(domainObjectTypeConfig.getName()));
                row++;
            }

            JdbcResultSet result = new JdbcResultSet(collection);
            return result;
        } catch (Exception ex) {
            throw new SQLException("Error get tables", ex);
        }
    }

    @Override
    public ResultSet getSchemas() throws SQLException {
        GenericIdentifiableObjectCollection collection = new GenericIdentifiableObjectCollection();
        List<FieldConfig> fields = new ArrayList<FieldConfig>();
        fields.add(getFieldConfig("TABLE_SCHEM", FieldType.STRING));
        fields.add(getFieldConfig("TABLE_CATALOG", FieldType.STRING));
        collection.setFieldsConfiguration(fields);
        collection.set("TABLE_SCHEM", 0, new StringValue(""));
        collection.set("TABLE_CATALOG", 0, new StringValue(""));

        JdbcResultSet result = new JdbcResultSet(collection);
        return result;
    }

    @Override
    public ResultSet getCatalogs() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public ResultSet getTableTypes() throws SQLException {
        GenericIdentifiableObjectCollection collection = new GenericIdentifiableObjectCollection();
        List<FieldConfig> fields = new ArrayList<FieldConfig>();
        fields.add(getFieldConfig("TABLE_TYPE", FieldType.STRING));
        collection.setFieldsConfiguration(fields);
        collection.set(0, 0, new StringValue("collection"));

        JdbcResultSet result = new JdbcResultSet(collection);
        return result;
    }

    private FieldConfig getFieldConfig(String name, FieldType type){
        FieldConfig result = null;
        if (type == FieldType.STRING){
            result = new StringFieldConfig();
            result.setName(name);
            result.setNotNull(false);
        }else if(type == FieldType.LONG){
            result = new LongFieldConfig();
            result.setName(name);
            result.setNotNull(false);
        }
        return result;
    }
    
    @Override
    public ResultSet getColumns(
            String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern)
            throws SQLException {
        try {
            GenericIdentifiableObjectCollection collection = new GenericIdentifiableObjectCollection();

            List<FieldConfig> fields = new ArrayList<FieldConfig>();
            fields.add(getFieldConfig("TABLE_CAT", FieldType.STRING));
            fields.add(getFieldConfig("TABLE_SCHEM", FieldType.STRING));
            fields.add(getFieldConfig("TABLE_NAME", FieldType.STRING));
            fields.add(getFieldConfig("COLUMN_NAME", FieldType.STRING));
            fields.add(getFieldConfig("DATA_TYPE", FieldType.LONG));
            fields.add(getFieldConfig("TYPE_NAME", FieldType.STRING));
            fields.add(getFieldConfig("COLUMN_SIZE", FieldType.LONG));
            fields.add(getFieldConfig("BUFFER_LENGTH", FieldType.STRING));
            fields.add(getFieldConfig("DECIMAL_DIGITS", FieldType.LONG));
            fields.add(getFieldConfig("NUM_PREC_RADIX", FieldType.LONG));
            fields.add(getFieldConfig("NULLABLE", FieldType.LONG));
            fields.add(getFieldConfig("REMARKS", FieldType.STRING));
            fields.add(getFieldConfig("COLUMN_DEF", FieldType.STRING));
            fields.add(getFieldConfig("SQL_DATA_TYPE", FieldType.LONG));
            fields.add(getFieldConfig("SQL_DATETIME_SUB", FieldType.LONG));
            fields.add(getFieldConfig("CHAR_OCTET_LENGTH", FieldType.LONG));
            fields.add(getFieldConfig("ORDINAL_POSITION", FieldType.LONG));
            fields.add(getFieldConfig("IS_NULLABLE", FieldType.STRING));
            fields.add(getFieldConfig("SCOPE_CATALOG", FieldType.STRING));
            fields.add(getFieldConfig("SCOPE_SCHEMA", FieldType.STRING));
            fields.add(getFieldConfig("SCOPE_TABLE", FieldType.STRING));
            fields.add(getFieldConfig("SOURCE_DATA_TYPE", FieldType.LONG));
            fields.add(getFieldConfig("IS_AUTOINCREMENT", FieldType.STRING));
            fields.add(getFieldConfig("IS_GENERATEDCOLUMN", FieldType.STRING));
            collection.setFieldsConfiguration(fields);

            if (tableNamePattern != null && tableNamePattern.length() > 0) {

                DomainObjectTypeConfig typeConfig =
                        client.getConfigService().getConfig(DomainObjectTypeConfig.class, tableNamePattern);

                //Добавляем системные атрибуты
                // id
                collection.set("TABLE_NAME", 0, new StringValue(typeConfig.getName()));
                collection.set("COLUMN_NAME", 0, new StringValue("id"));
                collection.set("COLUMN_SIZE", 0, new LongValue(0));
                collection.set("DECIMAL_DIGITS", 0, new LongValue(0));
                collection.set("NUM_PREC_RADIX", 0, new LongValue(0));
                collection.set("NULLABLE", 0, new LongValue(1));
                collection.set("DATA_TYPE", 0, new LongValue(Types.NUMERIC));

                int row = 0;
                for (FieldConfig fieldConfig : typeConfig.getFieldConfigs()) {
                    collection.set("TABLE_NAME", row, new StringValue(typeConfig.getName()));
                    collection.set("COLUMN_NAME", row, new StringValue(fieldConfig.getName()));
                    collection.set("COLUMN_SIZE", row, new LongValue(0));
                    collection.set("DECIMAL_DIGITS", row, new LongValue(0));
                    collection.set("NUM_PREC_RADIX", row, new LongValue(0));
                    collection.set("NULLABLE", row, new LongValue(1));
                    if (fieldConfig instanceof ReferenceFieldConfig) {
                        collection.set("DATA_TYPE", row, new LongValue(Types.NUMERIC));
                    } else if (fieldConfig instanceof LongFieldConfig) {
                        collection.set("DATA_TYPE", row, new LongValue(Types.NUMERIC));
                    } else if (fieldConfig instanceof DateTimeFieldConfig) {
                        collection.set("DATA_TYPE", row, new LongValue(Types.TIMESTAMP));
                    } else {
                        collection.set("DATA_TYPE", row, new LongValue(Types.VARCHAR));
                    }
                    row++;
                }
            }

            JdbcResultSet result = new JdbcResultSet(collection);
            return result;
        } catch (Exception ex) {
            throw new SQLException("Error get columns", ex);
        }
    }

    @Override
    public ResultSet getColumnPrivileges(String catalog, String schema, String table, String columnNamePattern)
            throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public ResultSet getTablePrivileges(String catalog, String schemaPattern, String tableNamePattern)
            throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public ResultSet getBestRowIdentifier(String catalog, String schema, String table, int scope, boolean nullable)
            throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public ResultSet getVersionColumns(String catalog, String schema, String table) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public ResultSet getPrimaryKeys(String catalog, String schema, String table) throws SQLException {
        GenericIdentifiableObjectCollection collection = new GenericIdentifiableObjectCollection();

        List<FieldConfig> fields = new ArrayList<FieldConfig>();
        fields.add(getFieldConfig("TABLE_CAT", FieldType.STRING));
        fields.add(getFieldConfig("TABLE_SCHEM", FieldType.STRING));
        fields.add(getFieldConfig("TABLE_NAME", FieldType.STRING));
        fields.add(getFieldConfig("COLUMN_NAME", FieldType.STRING));
        fields.add(getFieldConfig("KEY_SEQ", FieldType.LONG));
        fields.add(getFieldConfig("PK_NAME", FieldType.STRING));
        collection.setFieldsConfiguration(fields);

        collection.set("TABLE_NAME", 0, new StringValue(table));
        collection.set("COLUMN_NAME", 0, new StringValue("id"));

        JdbcResultSet result = new JdbcResultSet(collection);
        return result;
    }

    @Override
    public ResultSet getImportedKeys(String catalog, String schema, String table) throws SQLException {
        GenericIdentifiableObjectCollection collection = new GenericIdentifiableObjectCollection();

        List<FieldConfig> fields = new ArrayList<FieldConfig>();
        fields.add(getFieldConfig("PKTABLE_CAT", FieldType.STRING));
        fields.add(getFieldConfig("PKTABLE_SCHEM", FieldType.STRING));
        fields.add(getFieldConfig("PKTABLE_NAME", FieldType.STRING));
        fields.add(getFieldConfig("PKCOLUMN_NAME", FieldType.STRING));
        fields.add(getFieldConfig("FKTABLE_CAT", FieldType.STRING));
        fields.add(getFieldConfig("FKTABLE_SCHEM", FieldType.STRING));
        fields.add(getFieldConfig("FKTABLE_NAME", FieldType.STRING));
        fields.add(getFieldConfig("FKCOLUMN_NAME", FieldType.STRING));
        fields.add(getFieldConfig("KEY_SEQ", FieldType.LONG));
        fields.add(getFieldConfig("UPDATE_RULE", FieldType.LONG));
        fields.add(getFieldConfig("DELETE_RULE", FieldType.LONG));
        fields.add(getFieldConfig("FK_NAME", FieldType.STRING));
        fields.add(getFieldConfig("PK_NAME", FieldType.STRING));
        fields.add(getFieldConfig("DEFERRABILITY", FieldType.LONG));
        collection.setFieldsConfiguration(fields);

        //TODO сформировать список forenkey
        JdbcResultSet result = new JdbcResultSet(collection);
        return result;
    }

    @Override
    public ResultSet getExportedKeys(String catalog, String schema, String table) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public ResultSet getCrossReference(String parentCatalog, String parentSchema, String parentTable,
            String foreignCatalog, String foreignSchema, String foreignTable) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public ResultSet getTypeInfo() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate)
            throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsResultSetType(int type) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsResultSetConcurrency(int type, int concurrency) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean ownUpdatesAreVisible(int type) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean ownDeletesAreVisible(int type) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean ownInsertsAreVisible(int type) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean othersUpdatesAreVisible(int type) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean othersDeletesAreVisible(int type) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean othersInsertsAreVisible(int type) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean updatesAreDetected(int type) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean deletesAreDetected(int type) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean insertsAreDetected(int type) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsBatchUpdates() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern, int[] types)
            throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public Connection getConnection() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsSavepoints() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsNamedParameters() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsMultipleOpenResults() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsGetGeneratedKeys() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public ResultSet getSuperTypes(String catalog, String schemaPattern, String typeNamePattern) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public ResultSet getSuperTables(String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public ResultSet getAttributes(String catalog, String schemaPattern, String typeNamePattern,
            String attributeNamePattern) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsResultSetHoldability(int holdability) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return ResultSet.CLOSE_CURSORS_AT_COMMIT;
    }

    @Override
    public int getDatabaseMajorVersion() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getDatabaseMinorVersion() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getJDBCMajorVersion() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getJDBCMinorVersion() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getSQLStateType() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean locatorsUpdateCopy() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsStatementPooling() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public RowIdLifetime getRowIdLifetime() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public ResultSet getClientInfoProperties() throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public ResultSet getFunctions(String catalog, String schemaPattern, String functionNamePattern) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public ResultSet getFunctionColumns(String catalog, String schemaPattern, String functionNamePattern,
            String columnNamePattern) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public ResultSet getPseudoColumns(String catalog, String schemaPattern, String tableNamePattern,
            String columnNamePattern) throws SQLException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean generatedKeyAlwaysReturned() throws SQLException {
        throw new UnsupportedOperationException();

    }

}
