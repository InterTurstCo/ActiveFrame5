package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.config.model.*;

import java.util.List;

import static ru.intertrust.cm.core.dao.api.ConfigurationDao.CONFIGURATION_TABLE;
import static ru.intertrust.cm.core.dao.api.DataStructureDao.AUTHENTICATION_INFO_TABLE;
import static ru.intertrust.cm.core.dao.api.DomainObjectTypeIdDao.DOMAIN_OBJECT_TYPE_ID_TABLE;
import static ru.intertrust.cm.core.dao.api.DomainObjectDao.PARENT_COLUMN;
import static ru.intertrust.cm.core.dao.api.DomainObjectDao.TYPE_COLUMN;
import static ru.intertrust.cm.core.dao.api.DomainObjectDao.MASTER_COLUMN;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.*;

/**
 * Класс для генерации sql запросов для {@link PostgreSqlDataStructureDaoImpl}
 * @author vmatsukevich
 *         Date: 5/20/13
 *         Time: 2:12 PM
 */
public class PostgreSqlQueryHelper {

    public static final String ACL_TABLE_SUFFIX = "_ACL";

    public static final String READ_TABLE_SUFFIX = "_READ";

    private static final String GROUP_TABLE= "User_Group";

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
        return "create table " + DOMAIN_OBJECT_TYPE_ID_TABLE + "(ID bigserial not null, NAME varchar(256) not null, " +
                "constraint PK_" + DOMAIN_OBJECT_TYPE_ID_TABLE + " primary key (ID), constraint U_" + DOMAIN_OBJECT_TYPE_ID_TABLE + " unique (NAME))";
    }

    /**
     * Генерирует запрос, создающий таблицу для хранения версий конфигурации
     * @return запрос, создающий таблицу для хранения версий конфигурации
     */
    public static String generateCreateConfigurationTableQuery() {
        return "create table " + CONFIGURATION_TABLE + "(ID bigserial not null, CONTENT text not null, " +
                "LOADED_DATE timestamp not null, constraint PK_" + CONFIGURATION_TABLE + " primary key (ID))";
    }

    /**
     * Генерирует запрос, создающий таблицу AUTHENTICATION_INFO
     * @return запрос, создающий таблицу AUTHENTICATION_INFO
     */
    public static String generateCreateAuthenticationInfoTableQuery() {
        return "CREATE TABLE " + AUTHENTICATION_INFO_TABLE
                + " (ID bigint not null, user_uid character varying(64) NOT NULL, password"
                + " character varying(128), constraint PK_" + AUTHENTICATION_INFO_TABLE
                + "_ID primary key (ID), constraint U_" + AUTHENTICATION_INFO_TABLE
                + "_USER_UID unique(user_uid))";
    }

    private static String createAclTableQueryFor(String domainObjectType) {
        return "create table " + domainObjectType + "_ACL (object_id bigint not null, group_id bigint not null, " +
                "operation varchar(256) not null, constraint PK_" + toUpperCase(domainObjectType)
                + "_ACL primary key (object_id, group_id, operation)";

    }

    private static String createAclReadTableQueryFor(String domainObjectType) {
        return "create table " + domainObjectType + "_READ (object_id bigint not null, group_id bigint not null, " +
                "constraint PK_" + domainObjectType + "_READ primary key (object_id, group_id)";
    }

    private static void appendFKConstraintForDO(String sourceDomainObjectType, String targetDomainObjectType, StringBuilder query) {
        query.append(", ").append("CONSTRAINT FK_").append(sourceDomainObjectType).append("_")
                .append(targetDomainObjectType).append(" FOREIGN KEY (object_id) REFERENCES ")
                .append(targetDomainObjectType).append(" (id)");
    }

    private static void appendFKConstraintForGroup(String domainObjectType, StringBuilder query) {
        query.append(", ").append("CONSTRAINT FK_").append(domainObjectType).append("_")
                .append(toUpperCase(GROUP_TABLE)).append(" FOREIGN KEY (group_id) REFERENCES ").append(GROUP_TABLE)
                .append(" (id)");
    }

    private static String toUpperCase(String sourceDomainObjectType) {
        return sourceDomainObjectType.toUpperCase();
    }

    /**
     * Генерирует запрос, создающий последовательность(сиквенс) по конфигурации доменного объекта
     * @param config конфигурация доменного объекта
     * @return запрос, создающий последовательность(сиквенс) по конфигурации доменного объекта
     */
    public static String generateSequenceQuery(DomainObjectTypeConfig config) {
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
    public static String generateCreateTableQuery(DomainObjectTypeConfig config) {
        String tableName = getSqlName(config);
        StringBuilder query = new StringBuilder("create table ").append(tableName).append(" ( ");

        appendSystemColumnsQueryPart(config, query);

        if (config.getFieldConfigs().size() > 0 || config.getParentConfig() != null) {
            query.append(", ");
            appendColumnsQueryPart(query, config.getFieldConfigs(), config.getParentConfig(), false);
        }

        appendPKConstraintQueryPart(query, tableName);
        appendUniqueConstraintsQueryPart(query, tableName, config.getUniqueKeyConfigs(), false);
        appendFKConstraintsQueryPart(query, tableName, config);

        query.append(")");

        return query.toString();
    }

    /**
     * Генерирует запрос, создающий _ACL таблицу (таблица прав доступа, кроме чтения для ДО) для соответствующего доменного объекта.
     * @param config конфигурация доменного объекта
     * @return запрос, создающий _ACL талицу для соответствующего доменного объекта
     */
    public static String generateCreateAclTableQuery(DomainObjectTypeConfig config) {
        String domainObjectType = getSqlName(config);
        String aclTableName = domainObjectType + ACL_TABLE_SUFFIX;

        StringBuilder query = new StringBuilder(createAclTableQueryFor(domainObjectType));

        appendFKConstraintForDO(aclTableName, domainObjectType, query);
        appendFKConstraintForGroup(domainObjectType, query);
        query.append(")");

        return query.toString();
    }

    /**
     * Генерирует запрос, создающий _READ таблицу (таблица разрений на чтение для ДО) для соответствующего доменного объекта.
     * @param config конфигурация доменного объекта
     * @return запрос, создающий _READ талицу для соответствующего доменного объекта
     */
    public static String generateCreateAclReadTableQuery(DomainObjectTypeConfig config) {
        String domainObjectType = getSqlName(config);
        String aclReadTableName = domainObjectType + READ_TABLE_SUFFIX;

        StringBuilder query = new StringBuilder(createAclReadTableQueryFor(domainObjectType));

        appendFKConstraintForDO(aclReadTableName, domainObjectType, query);
        appendFKConstraintForGroup(domainObjectType, query);
        query.append(")");
        return query.toString();
    }

    /**
     * Генерирует запрос для обновления структуры таблицы (добавления колонок и уникальных ключей)
     * @param domainObjectConfigName название доменного объекта, таблицу которого необходимо обновить
     * @param fieldConfigList список колонок для добавления
     * @param uniqueKeyConfigList список уникальных ключей
     * @return запрос для обновления структуры таблицы (добавления колонок и уникальных ключей)
     */
    public static String generateUpdateTableQuery(String domainObjectConfigName, List<FieldConfig> fieldConfigList,
                                                  List<UniqueKeyConfig> uniqueKeyConfigList,
                                                  DomainObjectParentConfig parentConfig) {
        String tableName = getSqlName(domainObjectConfigName);
        StringBuilder query = new StringBuilder("alter table ").append(tableName).append(" ");

        appendColumnsQueryPart(query, fieldConfigList, parentConfig, true);
        appendFKConstraintsQueryPart(query, tableName, fieldConfigList, parentConfig, true);
        appendUniqueConstraintsQueryPart(query, tableName, uniqueKeyConfigList, true);

        return query.toString();
    }

    public static String generateCreateIndexesQuery(DomainObjectTypeConfig config) {
        return generateCreateIndexesQuery(config.getName(), config.getFieldConfigs(), config.getParentConfig());
    }

    /**
     * Генерирует запрос, для создания индексов по конфигурации доменного объекта
     * @param configName название конфигурации доменного объекта
     * @return запрос, для создания индексов по конфигурации доменного объекта
     */
    public static String generateCreateIndexesQuery(String configName, List<FieldConfig> fieldConfigList,
                                                    DomainObjectParentConfig parentConfig) {
        StringBuilder query = new StringBuilder();
        String tableName = getSqlName(configName);

        if(parentConfig != null) {
            String indexName = "I_" + tableName + "_" + MASTER_COLUMN;
            query.append("create index ").append(indexName).append(" on ").append(tableName).append(" (").
                    append(MASTER_COLUMN).append(");\n");
        }

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

    private static void appendFKConstraintsQueryPart(StringBuilder query, String tableName,
                                                     DomainObjectTypeConfig config) {
        appendFKConstraintsQueryPart(query, tableName, config.getFieldConfigs(), config.getParentConfig(), false);

        query.append(", ");
        if (config.getExtendsAttribute() != null) {
            appendFKConstraint(query, tableName, PARENT_COLUMN, config.getExtendsAttribute());
        } else {
            appendFKConstraint(query, tableName, TYPE_COLUMN, DOMAIN_OBJECT_TYPE_ID_TABLE);
        }
    }

    private static void appendFKConstraintsQueryPart(StringBuilder query, String tableName,
                                                     List<FieldConfig> fieldConfigList,
                                                     DomainObjectParentConfig parentConfig,
                                                     boolean isAlterQuery) {
        if(parentConfig != null) {
            query.append(", ");
            appendAlterModeQueryPart(query, isAlterQuery);
            appendFKConstraint(query, tableName, MASTER_COLUMN, parentConfig.getName());
        }

        for(FieldConfig fieldConfig : fieldConfigList) {
            if(!ReferenceFieldConfig.class.equals(fieldConfig.getClass())) {
                continue;
            }

            query.append(", ");
            appendAlterModeQueryPart(query, isAlterQuery);

            ReferenceFieldConfig referenceFieldConfig = (ReferenceFieldConfig) fieldConfig;
            appendFKConstraint(query, tableName, referenceFieldConfig.getName(), referenceFieldConfig.getType());
        }
    }

    private static void appendFKConstraint(StringBuilder query, String tableName, String fieldName,
                                           String referencedFieldName) {
        String fieldSqlName = getSqlName(fieldName);
        String constraintName = "FK_" + tableName + "_" + fieldSqlName;

        query.append("constraint ").append(constraintName).append(" foreign key (").append(fieldSqlName).
                append(")").append(" ").append("references").append(" ").
                append(getSqlName(referencedFieldName)).append("(ID)");
    }

    private static void appendAlterModeQueryPart(StringBuilder query, boolean isAlterQuery) {
        if (isAlterQuery) {
            query.append("add ");
        }
    }

    private static void appendPKConstraintQueryPart(StringBuilder query, String tableName) {
        String pkName = "PK_" + tableName + "_ID";
        query.append(", constraint ").append(pkName).append(" primary key (ID)");
    }

    private static void appendSystemColumnsQueryPart(DomainObjectTypeConfig config, StringBuilder query) {
        query.append("ID bigint not null, ");

        if (config.getExtendsAttribute() == null) {
            query.append("CREATED_DATE timestamp not null, ");
            query.append("UPDATED_DATE timestamp not null, ");
            query.append(TYPE_COLUMN + " integer");
        } else {
            query.append(PARENT_COLUMN + " bigint not null");
        }
    }

    private static void appendColumnsQueryPart(StringBuilder query, List<FieldConfig> fieldConfigList,
                                               DomainObjectParentConfig parentConfig, boolean isAlterQuery) {
        if (parentConfig != null) {
            if(isAlterQuery) {
                query.append("add column ");
            }
            query.append(MASTER_COLUMN).append(" bigint, ");
        }

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
            appendAlterModeQueryPart(query, isAlterQuery);

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
