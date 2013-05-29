package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.config.*;

import java.util.List;

import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getReferencedTypeSqlName;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getSqlName;

/**
 * Класс для генерации sql запросов для {@link PostgreSQLDataStructureDAOImpl}
 * @author vmatsukevich
 *         Date: 5/20/13
 *         Time: 2:12 PM
 */
public class PostgreSQLQueryHelper {

    public static final String AUTHENTICATION_INFO_TABLE = "AUTHENTICATION_INFO";
    public static final String BUSINESS_OBJECT_TABLE = "BUSINESS_OBJECT";

    /**
     * Генерирует запрос, возвращающий кол-во таблиц в базе данных
     * @return запрос, возвращающий кол-во таблиц в базе данных
     */
    public static String generateCountTablesQuery() {
        return "select count(table_name) FROM information_schema.tables WHERE table_schema = 'public'";
    }

    /**
     * Генерирует запрос, создающий таблицу BUSINESS_OBJECT
     * @return запрос, создающий таблицу BUSINESS_OBJECT
     */
    public static String generateCreateBusinessObjectTableQuery() {
        return "create table " + BUSINESS_OBJECT_TABLE + "(ID bigserial not null, NAME varchar(256) not null, " +
                "constraint PK_" + BUSINESS_OBJECT_TABLE + " primary key (ID), constraint U_" + BUSINESS_OBJECT_TABLE + " unique (NAME))";
    }

    /**
     * Генерирует запрос, создающий таблицу AUTHENTICATION_INFO
     * @return запрос, создающий таблицу AUTHENTICATION_INFO
     */
    public static String generateCreateAuthenticationInfoTableQuery() {
        return "CREATE TABLE " + AUTHENTICATION_INFO_TABLE + " (ID bigint not null, user_uid character varying(64) NOT NULL, password"
                + " character varying(128), constraint PK_" + AUTHENTICATION_INFO_TABLE + "_ID primary key (ID), constraint U_" + AUTHENTICATION_INFO_TABLE
                + "_USER_UID unique(user_uid))";
    }

    /**
     * Генерирует запрос, создающий талицу по конфигурации бизнес-объекта
     * @param config конфигурация бизнес-объекта
     * @return запрос, создающий талицу по конфигурации бизнес-объекта
     */
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

    /**
     * Генерирует запрос, для создания индексов по конфигурации бизнес-объекта
     * @param config конфигурация бизнес-объекта
     * @return запрос, для создания индексов по конфигурации бизнес-объекта
     */
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
        StringBuilder queryPart = new StringBuilder();
        queryPart.append("ID bigint not null, ");

        queryPart.append("CREATED_DATE timestamp not null, ");
        queryPart.append("UPDATED_DATE timestamp not null");

        for (FieldConfig fieldConfig : config.getFieldConfigs()) {
            queryPart.append(", ").append(getSqlName(fieldConfig)).append(" ").append(getSqlType(fieldConfig));
            if (fieldConfig.isNotNull()) {
                queryPart.append(" not null");
            }
        }

        return queryPart.toString();
    }

    private static String generateUniqueConstraintsQueryPart(BusinessObjectConfig config, String tableName) {
        String queryPart = "";

        for(UniqueKeyConfig uniqueKeyConfig : config.getUniqueKeyConfigs()) {
            if(!uniqueKeyConfig.getUniqueKeyFieldConfigs().isEmpty()) {
                String constraintName = "U_" + tableName + "_" +
                        getFieldsListAsSql(uniqueKeyConfig.getUniqueKeyFieldConfigs(), "_");
                String fieldsList = getFieldsListAsSql(uniqueKeyConfig.getUniqueKeyFieldConfigs(), ", ");
                queryPart += ", constraint " + constraintName + " unique (" + fieldsList + ")";
            }
        }

        return queryPart;
    }

    private static String getFieldsListAsSql(List<UniqueKeyFieldConfig> uniqueKeyFieldConfigList, String delimiter) {
        if(uniqueKeyFieldConfigList.isEmpty()) {
            throw new IllegalArgumentException("UniqueKeyFieldConfig list is empty");
        }

        String result = "";
        for(int i = 0; i < uniqueKeyFieldConfigList.size(); i++) {
            if(i > 0) {
                result += delimiter;
            }
            result += getSqlName(uniqueKeyFieldConfigList.get(i).getName());
        }

        return result;
    }

    private static String getSqlType(FieldConfig fieldConfig) {
        if(DateTimeFieldConfig.class.equals(fieldConfig.getClass())) {
            return "timestamp";
        }

        if(DecimalFieldConfig.class.equals(fieldConfig.getClass())) {
            String sqlType = "decimal";
            DecimalFieldConfig decimalFieldConfig = (DecimalFieldConfig) fieldConfig;

            if(decimalFieldConfig.getPrecision() != null && decimalFieldConfig.getScale() != null) {
                sqlType += "(" + decimalFieldConfig.getPrecision() + ", " + decimalFieldConfig.getScale() + ")";
            } else if(decimalFieldConfig.getPrecision() != null) {
                sqlType += "(" + decimalFieldConfig.getPrecision() + ")";
            }

            return sqlType;
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
