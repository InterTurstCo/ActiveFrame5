package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.config.*;

import java.util.List;

import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getReferencedTypeSqlName;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getSqlName;

/**
 * @author vmatsukevich
 *         Date: 5/20/13
 *         Time: 2:12 PM
 */
public class PostgreSQLQueryHelper {

    public static String generateCountTablesQuery() {
        return "select count(table_name) FROM information_schema.tables WHERE table_schema = 'public'";
    }

    public static String generateCreateBusinessObjectTableQuery() {
        return "create table BUSINESS_OBJECT(ID bigserial not null, NAME varchar(256) not null, " +
                "constraint PK_BUSINESS_OBJECT_ID primary key (ID), constraint unique U_BUSINESS_OBJECT_NAME(NAME))";
    }

    public static String generateCreateTableQuery(BusinessObjectConfig config) {
        String tableName = getSqlName(config);

        String query = "create table " + tableName + " ( ";

        query += generateColumnsQueryPart(config);
        query += generatePKConstraintQueryPart(tableName);
        query += generateUniqueConstraintsQueryPart(config, tableName);
        query += generateFKConstraintsQueryPart(config, tableName);

        query += ")";

        return query;
    }

    public static String generateCreateIndexesQuery(BusinessObjectConfig config) {
        String query = "";
        String tableName = getSqlName(config);

        for(FieldConfig fieldConfig : config.getFieldConfigs()) {
            if(!ReferenceFieldConfig.class.equals(fieldConfig.getClass())) {
                continue;
            }

            ReferenceFieldConfig referenceFieldConfig = (ReferenceFieldConfig) fieldConfig;
            String fieldSqlName = getSqlName(referenceFieldConfig);

            String indexName = "I_" + tableName + "_" + fieldSqlName;
            query += "create index " + indexName + " on " + tableName + " (" + fieldSqlName + ") ;\n";
        }

        if(query.isEmpty()) {
            return null;
        }

        return query;
    }

    private static String generateFKConstraintsQueryPart(BusinessObjectConfig config, String tableName) {
        String queryPart = "";

        for(FieldConfig fieldConfig : config.getFieldConfigs()) {
            if(!ReferenceFieldConfig.class.equals(fieldConfig.getClass())) {
                continue;
            }

            ReferenceFieldConfig referenceFieldConfig = (ReferenceFieldConfig) fieldConfig;
            String fieldSqlName = getSqlName(referenceFieldConfig);

            String constraintName = "FK_" + tableName + "_" + fieldSqlName;
            queryPart += ", constraint " + constraintName + " foreign key (" + fieldSqlName + ") references " +
                    getReferencedTypeSqlName(referenceFieldConfig) + "(ID)";
        }

        return queryPart;
    }

    private static String generatePKConstraintQueryPart(String tableName) {
        String pkName = "PK_" + tableName + "_ID";
        return ", constraint " + pkName + " primary key (ID)";
    }

    private static String generateColumnsQueryPart(BusinessObjectConfig config) {
        String queryPart = "ID bigint not null, " +
                           "CREATED_DATE timestamp not null, " +
                           "UPDATED_DATE timestamp not null";

        for(FieldConfig fieldConfig : config.getFieldConfigs()) {
            queryPart += ", " + getSqlName(fieldConfig) + " " + getSqlType(fieldConfig);
            if(fieldConfig.isNotNull()) {
                queryPart += " not null";
            }
        }

        return queryPart;
    }

    private static String generateUniqueConstraintsQueryPart(BusinessObjectConfig config, String tableName) {
        String queryPart = "";

        for(UniqueKeyConfig uniqueKeyConfig : config.getUniqueKeyConfigs()) {
            if(!uniqueKeyConfig.getUniqueKeyFieldConfigs().isEmpty()) {
                String constraintName = "U_" + tableName + "_" +
                        getSqlName(getFieldsListAsString(uniqueKeyConfig.getUniqueKeyFieldConfigs(), "_"));
                String fieldsList = getSqlName(getFieldsListAsString(uniqueKeyConfig.getUniqueKeyFieldConfigs(), ", "));
                queryPart += ", constraint " + constraintName + " unique (" + fieldsList + ")";
            }
        }

        return queryPart;
    }

    private static String getFieldsListAsString(List<UniqueKeyFieldConfig> uniqueKeyFieldConfigList, String delimiter) {
        if(uniqueKeyFieldConfigList.isEmpty()) {
            throw new IllegalArgumentException("UniqueKeyFieldConfig list is empty");
        }

        String result = "";
        for(int i = 0; i < uniqueKeyFieldConfigList.size(); i++) {
            if(i > 0) {
                result += delimiter;
            }
            result += uniqueKeyFieldConfigList.get(i).getName();
        }

        return result;
    }

    private static String getSqlType(FieldConfig fieldConfig) {
        if(DateTimeFieldConfig.class.equals(fieldConfig.getClass())) {
            return "timestamp";
        }

        if(DecimalFieldConfig.class.equals(fieldConfig.getClass())) {
            return "decimal";
        }

        if(LongFieldConfig.class.equals(fieldConfig.getClass())) {
            return "bigint";
        }

        if(ReferenceFieldConfig.class.equals(fieldConfig.getClass())) {
            return "bigint";
        }

        if(StringFieldConfig.class.equals(fieldConfig.getClass())) {
            return "varchar(" + ((StringFieldConfig) fieldConfig).getLength() + ")";
        }

        if(PasswordFieldConfig.class.equals(fieldConfig.getClass())) {
            return "varchar(" + ((PasswordFieldConfig) fieldConfig).getLength() + ")";
        }

        throw new IllegalArgumentException("Invalid field type");
    }
}
