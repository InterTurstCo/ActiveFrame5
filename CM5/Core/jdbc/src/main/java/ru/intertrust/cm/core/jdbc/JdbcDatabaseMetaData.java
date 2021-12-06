package ru.intertrust.cm.core.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

public class JdbcDatabaseMetaData implements DatabaseMetaData {

    private final SochiClient client;

    JdbcDatabaseMetaData(SochiClient client) {
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
    public boolean allProceduresAreCallable() {
        return true;
    }

    @Override
    public boolean allTablesAreSelectable() {
        return true;
    }

    @Override
    public String getURL() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getUserName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public boolean nullsAreSortedHigh() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean nullsAreSortedLow() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean nullsAreSortedAtStart() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean nullsAreSortedAtEnd() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDatabaseProductName() {
        return "Sochi Server";
    }

    @Override
    public String getDatabaseProductVersion() {
        return "unknown";
    }

    @Override
    public String getDriverName() {
        return "Sochi JDBC Driver";
    }

    @Override
    public String getDriverVersion() {
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
    public boolean usesLocalFiles() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean usesLocalFilePerTable() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsMixedCaseIdentifiers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean storesUpperCaseIdentifiers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean storesLowerCaseIdentifiers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean storesMixedCaseIdentifiers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsMixedCaseQuotedIdentifiers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean storesUpperCaseQuotedIdentifiers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean storesLowerCaseQuotedIdentifiers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean storesMixedCaseQuotedIdentifiers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getIdentifierQuoteString() {
        return " ";
    }

    @Override
    public String getSQLKeywords() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getNumericFunctions() {
        return null;
    }

    @Override
    public String getStringFunctions() {
        return null;
    }

    @Override
    public String getSystemFunctions() {
        return null;
    }

    @Override
    public String getTimeDateFunctions() {
        return null;
    }

    @Override
    public String getSearchStringEscape() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getExtraNameCharacters() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsAlterTableWithAddColumn() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsAlterTableWithDropColumn() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsColumnAliasing() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean nullPlusNonNullIsNull() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsConvert() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsConvert(int fromType, int toType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsTableCorrelationNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsDifferentTableCorrelationNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsExpressionsInOrderBy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsOrderByUnrelated() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsGroupBy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsGroupByUnrelated() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsGroupByBeyondSelect() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsLikeEscapeClause() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsMultipleResultSets() {
        return false;
    }

    @Override
    public boolean supportsMultipleTransactions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsNonNullableColumns() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsMinimumSQLGrammar() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsCoreSQLGrammar() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsExtendedSQLGrammar() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsANSI92EntryLevelSQL() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsANSI92IntermediateSQL() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsANSI92FullSQL() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsIntegrityEnhancementFacility() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsOuterJoins() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsFullOuterJoins() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsLimitedOuterJoins() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getSchemaTerm() {
        return null;
    }

    @Override
    public String getProcedureTerm() {
        return null;
    }

    @Override
    public String getCatalogTerm() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCatalogAtStart() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getCatalogSeparator() {
        return "";
    }

    @Override
    public boolean supportsSchemasInDataManipulation() {
        return false;
    }

    @Override
    public boolean supportsSchemasInProcedureCalls() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsSchemasInTableDefinitions() {
        return false;
    }

    @Override
    public boolean supportsSchemasInIndexDefinitions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsSchemasInPrivilegeDefinitions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsCatalogsInDataManipulation() {
        return false;
    }

    @Override
    public boolean supportsCatalogsInProcedureCalls() {
        return false;
    }

    @Override
    public boolean supportsCatalogsInTableDefinitions() {
        return false;
    }

    @Override
    public boolean supportsCatalogsInIndexDefinitions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsCatalogsInPrivilegeDefinitions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsPositionedDelete() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsPositionedUpdate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsSelectForUpdate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsStoredProcedures() {
        return false;
    }

    @Override
    public boolean supportsSubqueriesInComparisons() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsSubqueriesInExists() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsSubqueriesInIns() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsSubqueriesInQuantifieds() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsCorrelatedSubqueries() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsUnion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsUnionAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsOpenCursorsAcrossCommit() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsOpenCursorsAcrossRollback() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsOpenStatementsAcrossCommit() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsOpenStatementsAcrossRollback() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaxBinaryLiteralLength() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaxCharLiteralLength() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaxColumnNameLength() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaxColumnsInGroupBy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaxColumnsInIndex() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaxColumnsInOrderBy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaxColumnsInSelect() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaxColumnsInTable() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaxConnections() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaxCursorNameLength() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaxIndexLength() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaxSchemaNameLength() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaxProcedureNameLength() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaxCatalogNameLength() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaxRowSize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean doesMaxRowSizeIncludeBlobs() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaxStatementLength() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaxStatements() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaxTableNameLength() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaxTablesInSelect() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaxUserNameLength() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getDefaultTransactionIsolation() {
        return 0;
    }

    @Override
    public boolean supportsTransactions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsTransactionIsolationLevel(int level) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsDataDefinitionAndDataManipulationTransactions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsDataManipulationTransactionsOnly() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean dataDefinitionCausesTransactionCommit() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean dataDefinitionIgnoredInTransactions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern,
                                         String columnNamePattern) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types)
            throws SQLException {
        try {
            GenericIdentifiableObjectCollection collection = new GenericIdentifiableObjectCollection();
            List<FieldConfig> fields = new ArrayList<>();
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
            return new JdbcResultSet(collection);
        } catch (Exception ex) {
            throw new SQLException("Error get tables", ex);
        }
    }

    @Override
    public ResultSet getSchemas() {
        GenericIdentifiableObjectCollection collection = new GenericIdentifiableObjectCollection();
        List<FieldConfig> fields = new ArrayList<>();
        fields.add(getFieldConfig("TABLE_SCHEM", FieldType.STRING));
        fields.add(getFieldConfig("TABLE_CATALOG", FieldType.STRING));
        collection.setFieldsConfiguration(fields);
        collection.set("TABLE_SCHEM", 0, new StringValue(""));
        collection.set("TABLE_CATALOG", 0, new StringValue(""));

        return new JdbcResultSet(collection);
    }

    @Override
    public ResultSet getCatalogs() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResultSet getTableTypes() {
        GenericIdentifiableObjectCollection collection = new GenericIdentifiableObjectCollection();
        List<FieldConfig> fields = new ArrayList<>();
        fields.add(getFieldConfig("TABLE_TYPE", FieldType.STRING));
        collection.setFieldsConfiguration(fields);
        collection.set(0, 0, new StringValue("collection"));

        return new JdbcResultSet(collection);
    }

    private FieldConfig getFieldConfig(String name, FieldType type) {
        FieldConfig result = null;
        if (type == FieldType.STRING) {
            result = new StringFieldConfig();
            result.setName(name);
            result.setNotNull(false);
        } else if (type == FieldType.LONG) {
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

            List<FieldConfig> fields = new ArrayList<>();
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

            if (tableNamePattern != null && !tableNamePattern.isEmpty()) {

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
            return new JdbcResultSet(collection);
        } catch (Exception ex) {
            throw new SQLException("Error get columns", ex);
        }
    }

    @Override
    public ResultSet getColumnPrivileges(String catalog, String schema, String table, String columnNamePattern) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResultSet getTablePrivileges(String catalog, String schemaPattern, String tableNamePattern) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResultSet getBestRowIdentifier(String catalog, String schema, String table, int scope, boolean nullable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResultSet getVersionColumns(String catalog, String schema, String table) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResultSet getPrimaryKeys(String catalog, String schema, String table) {
        GenericIdentifiableObjectCollection collection = new GenericIdentifiableObjectCollection();

        List<FieldConfig> fields = new ArrayList<>();
        fields.add(getFieldConfig("TABLE_CAT", FieldType.STRING));
        fields.add(getFieldConfig("TABLE_SCHEM", FieldType.STRING));
        fields.add(getFieldConfig("TABLE_NAME", FieldType.STRING));
        fields.add(getFieldConfig("COLUMN_NAME", FieldType.STRING));
        fields.add(getFieldConfig("KEY_SEQ", FieldType.LONG));
        fields.add(getFieldConfig("PK_NAME", FieldType.STRING));
        collection.setFieldsConfiguration(fields);

        collection.set("TABLE_NAME", 0, new StringValue(table));
        collection.set("COLUMN_NAME", 0, new StringValue("id"));

        return new JdbcResultSet(collection);
    }

    @Override
    public ResultSet getImportedKeys(String catalog, String schema, String table) {
        GenericIdentifiableObjectCollection collection = new GenericIdentifiableObjectCollection();

        List<FieldConfig> fields = new ArrayList<>();
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
        return new JdbcResultSet(collection);
    }

    @Override
    public ResultSet getExportedKeys(String catalog, String schema, String table) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResultSet getCrossReference(String parentCatalog, String parentSchema, String parentTable,
                                       String foreignCatalog, String foreignSchema, String foreignTable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResultSet getTypeInfo() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsResultSetType(int type) {
        return type == ResultSet.TYPE_FORWARD_ONLY;
    }

    @Override
    public boolean supportsResultSetConcurrency(int type, int concurrency) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean ownUpdatesAreVisible(int type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean ownDeletesAreVisible(int type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean ownInsertsAreVisible(int type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean othersUpdatesAreVisible(int type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean othersDeletesAreVisible(int type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean othersInsertsAreVisible(int type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean updatesAreDetected(int type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean deletesAreDetected(int type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean insertsAreDetected(int type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsBatchUpdates() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern, int[] types) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Connection getConnection() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsSavepoints() {
        return false;
    }

    @Override
    public boolean supportsNamedParameters() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsMultipleOpenResults() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsGetGeneratedKeys() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResultSet getSuperTypes(String catalog, String schemaPattern, String typeNamePattern) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResultSet getSuperTables(String catalog, String schemaPattern, String tableNamePattern) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResultSet getAttributes(String catalog, String schemaPattern, String typeNamePattern,
                                   String attributeNamePattern) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsResultSetHoldability(int holdability) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getResultSetHoldability() {
        return ResultSet.CLOSE_CURSORS_AT_COMMIT;
    }

    @Override
    public int getDatabaseMajorVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getDatabaseMinorVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getJDBCMajorVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getJDBCMinorVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getSQLStateType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean locatorsUpdateCopy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsStatementPooling() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RowIdLifetime getRowIdLifetime() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResultSet getSchemas(String catalog, String schemaPattern) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsStoredFunctionsUsingCallSyntax() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean autoCommitFailureClosesAllResultSets() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResultSet getClientInfoProperties() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResultSet getFunctions(String catalog, String schemaPattern, String functionNamePattern) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResultSet getFunctionColumns(String catalog, String schemaPattern, String functionNamePattern,
                                        String columnNamePattern) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResultSet getPseudoColumns(String catalog, String schemaPattern, String tableNamePattern,
                                      String columnNamePattern) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean generatedKeyAlwaysReturned() {
        throw new UnsupportedOperationException();
    }
}
