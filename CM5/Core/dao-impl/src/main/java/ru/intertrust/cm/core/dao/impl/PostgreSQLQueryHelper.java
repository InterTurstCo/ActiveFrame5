package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.config.model.*;

import java.util.List;

import static ru.intertrust.cm.core.dao.api.DataStructureDAO.DOMAIN_OBJECT_TABLE;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.*;

/**
 * Класс для генерации sql запросов для {@link PostgreSQLDataStructureDAOImpl}
 * @author vmatsukevich
 *         Date: 5/20/13
 *         Time: 2:12 PM
 */
public class PostgreSQLQueryHelper {

    public static final String AUTHENTICATION_INFO_TABLE = "AUTHENTICATION_INFO";
    public static final String CONFIGURATION_TABLE = "CONFIGURATION";

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
    public static String generateCreateDomainObjectTableQuery() {
        return "create table " + DOMAIN_OBJECT_TABLE + "(ID bigserial not null, NAME varchar(256) not null, " +
                "constraint PK_" + DOMAIN_OBJECT_TABLE + " primary key (ID), constraint U_" + DOMAIN_OBJECT_TABLE + " unique (NAME))";
    }

    public static String generateCreateConfigurationTableQuery() {
        return "create table " + CONFIGURATION_TABLE + "(ID bigserial not null, CONTENT text not null, " +
                "LOADED_DATE timestamp not null, constraint PK_" + CONFIGURATION_TABLE + " primary key (ID))";
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
     * Генерирует запрос, создающий последовательность(сиквенс) по конфигурации доменного объекта
     * @param config конфигурация доменного объекта
     * @return запрос, создающий последовательность(сиквенс) по конфигурации доменного объекта
     */
    public static String generateSequenceQuery(DomainObjectConfig config) {
        String sequenceName = getSqlSequenceName(config);
        StringBuilder query = new StringBuilder();
        query.append("create sequence ");
        query.append(sequenceName);

        return query.toString();
    }


    /**
     * Генерирует запрос, создающий талицу по конфигурации доменного объекта
     * @param config конфигурация доменного объекта
     * @return запрос, создающий талицу по конфигурации доменного объекта
     */
    public static String generateCreateTableQuery(DomainObjectConfig config) {
        String tableName = getSqlName(config);
        StringBuilder query = new StringBuilder("create table ").append(tableName).append(" ( ");

        appendSystemColumnsQueryPart(query);
        if (config.getFieldConfigs().size() > 0) {
            query.append(", ");
            appendColumnsQueryPart(query, config.getFieldConfigs(), false);
        }

        appendPKConstraintQueryPart(query, tableName);
        appendUniqueConstraintsQueryPart(query, tableName, config.getUniqueKeyConfigs(), false);
        appendFKConstraintsQueryPart(query, tableName, config.getFieldConfigs(), false);

        query.append(")");

        return query.toString();
    }

    public static String generateUpdateTableQuery(String domainObjectConfigName, List<FieldConfig> fieldConfigList,
                                                  List<UniqueKeyConfig> uniqueKeyConfigList) {
        String tableName = getSqlName(domainObjectConfigName);
        StringBuilder query = new StringBuilder("alter table ").append(tableName).append(" ");

        appendColumnsQueryPart(query, fieldConfigList, true);
        appendFKConstraintsQueryPart(query, tableName, fieldConfigList, true);
        appendUniqueConstraintsQueryPart(query, tableName, uniqueKeyConfigList, true);

        return query.toString();
    }

    /**
     * Генерирует запрос, для создания индексов по конфигурации доменного объекта
     * @param configName название конфигурации доменного объекта
     * @return запрос, для создания индексов по конфигурации доменного объекта
     */
    public static String generateCreateIndexesQuery(String configName, List<FieldConfig> fieldConfigList) {
        StringBuilder query = new StringBuilder();
        String tableName = getSqlName(configName);

        for(FieldConfig fieldConfig : fieldConfigList) {
            if(!ReferenceFieldConfig.class.equals(fieldConfig.getClass())) {
                continue;
            }

            ReferenceFieldConfig referenceFieldConfig = (ReferenceFieldConfig) fieldConfig;
            String fieldSqlName = getSqlName(referenceFieldConfig);

            String indexName = "I_" + tableName + "_" + fieldSqlName;
            query.append("create index ").append(indexName).append(" on ").append(tableName).append(" (").
                    append(fieldSqlName).append(");\n");
        }

        if(query.length() == 0) {
            return null;
        }

        return query.toString();
    }

    private static void appendFKConstraintsQueryPart(StringBuilder query, String tableName, List<FieldConfig> fieldConfigList,
                                                     boolean isAlterQuery) {
        for(FieldConfig fieldConfig : fieldConfigList) {
            if(!ReferenceFieldConfig.class.equals(fieldConfig.getClass())) {
                continue;
            }

            ReferenceFieldConfig referenceFieldConfig = (ReferenceFieldConfig) fieldConfig;
            String fieldSqlName = getSqlName(referenceFieldConfig);
            String constraintName = "FK_" + tableName + "_" + fieldSqlName;

            query.append(", ");
            if (isAlterQuery) {
                query.append("add ");
            }

            query.append("constraint ").append(constraintName).append(" foreign key (").append(fieldSqlName).
                    append(")").append(" ").append("references").append(" ").
                    append(getReferencedTypeSqlName(referenceFieldConfig)).append("(ID)");
        }
    }

    private static void appendPKConstraintQueryPart(StringBuilder query, String tableName) {
        String pkName = "PK_" + tableName + "_ID";
        query.append(", constraint ").append(pkName).append(" primary key (ID)");
    }

    private static void appendSystemColumnsQueryPart(StringBuilder query) {
        query.append("ID bigint not null, ");
        query.append("CREATED_DATE timestamp not null, ");
        query.append("UPDATED_DATE timestamp not null");
    }

    private static void appendColumnsQueryPart(StringBuilder query, List<FieldConfig> fieldConfigList,
                                               boolean isAlterQuery) {
        int size = fieldConfigList.size();
        for (int i = 0; i < size; i ++) {
            FieldConfig fieldConfig = fieldConfigList.get(i);

            if (i > 0) {
                query.append(", ");
            }

            if(isAlterQuery) {
                query.append("add column ");
            }

            query.append(getSqlName(fieldConfig)).append(" ").append(getSqlType(fieldConfig));
            if (fieldConfig.isNotNull()) {
                query.append(" not null");
            }
        }
    }

    private static void appendUniqueConstraintsQueryPart(StringBuilder query, String tableName,
                                                         List<UniqueKeyConfig> uniqueKeyConfigList,
                                                         boolean isAlterQuery) {
        for(UniqueKeyConfig uniqueKeyConfig : uniqueKeyConfigList) {
            if(uniqueKeyConfig.getUniqueKeyFieldConfigs().isEmpty()) {
                continue;
            }

            String constraintName = "U_" + tableName + "_" +
                    getFieldsListAsSql(uniqueKeyConfig.getUniqueKeyFieldConfigs(), "_");
            String fieldsList = getFieldsListAsSql(uniqueKeyConfig.getUniqueKeyFieldConfigs(), ", ");

            query.append(", ");
            if (isAlterQuery) {
                query.append("add ");
            }

            query.append("constraint ").append(constraintName).append(" unique (").
                    append(fieldsList).append(")");
        }
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
            StringBuilder sqlType = new StringBuilder("decimal");
            DecimalFieldConfig decimalFieldConfig = (DecimalFieldConfig) fieldConfig;

            if(decimalFieldConfig.getPrecision() != null && decimalFieldConfig.getScale() != null) {
                sqlType.append("(").append(decimalFieldConfig.getPrecision()).append(", ").
                        append(decimalFieldConfig.getScale()).append(")");
            } else if(decimalFieldConfig.getPrecision() != null) {
                sqlType.append("(").append(decimalFieldConfig.getPrecision()).append(")");
            }

            return sqlType.toString();
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
