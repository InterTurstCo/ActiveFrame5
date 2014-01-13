package ru.intertrust.cm.core.dao.impl;

import static ru.intertrust.cm.core.dao.api.ConfigurationDao.CONFIGURATION_TABLE;
import static ru.intertrust.cm.core.dao.api.ConfigurationDao.CONTENT_COLUMN;
import static ru.intertrust.cm.core.dao.api.ConfigurationDao.LOADED_DATE_COLUMN;
import static ru.intertrust.cm.core.dao.api.DataStructureDao.AUTHENTICATION_INFO_TABLE;
import static ru.intertrust.cm.core.dao.api.DataStructureDao.USER_UID_COLUMN;
import static ru.intertrust.cm.core.dao.api.DomainObjectDao.COMPONENT_COLUMN;
import static ru.intertrust.cm.core.dao.api.DomainObjectDao.CREATED_DATE_COLUMN;
import static ru.intertrust.cm.core.dao.api.DomainObjectDao.DOMAIN_OBJECT_ID_COLUMN;
import static ru.intertrust.cm.core.dao.api.DomainObjectDao.ID_COLUMN;
import static ru.intertrust.cm.core.dao.api.DomainObjectDao.INFO_COLUMN;
import static ru.intertrust.cm.core.dao.api.DomainObjectDao.IP_ADDRESS_COLUMN;
import static ru.intertrust.cm.core.dao.api.DomainObjectDao.OPERATION_COLUMN;
import static ru.intertrust.cm.core.dao.api.DomainObjectDao.TYPE_COLUMN;
import static ru.intertrust.cm.core.dao.api.DomainObjectDao.UPDATED_DATE_COLUMN;
import static ru.intertrust.cm.core.dao.api.DomainObjectTypeIdDao.DOMAIN_OBJECT_TYPE_ID_TABLE;
import static ru.intertrust.cm.core.dao.api.DomainObjectTypeIdDao.NAME_COLUMN;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getReferenceTypeColumnName;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getSqlAuditSequenceName;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getSqlName;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getSqlSequenceName;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getTimeZoneIdColumnName;

import java.util.ArrayList;
import java.util.List;

import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.config.BooleanFieldConfig;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DateTimeFieldConfig;
import ru.intertrust.cm.core.config.DateTimeWithTimeZoneFieldConfig;
import ru.intertrust.cm.core.config.DecimalFieldConfig;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.IndexConfig;
import ru.intertrust.cm.core.config.IndexFieldConfig;
import ru.intertrust.cm.core.config.LongFieldConfig;
import ru.intertrust.cm.core.config.PasswordFieldConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.StringFieldConfig;
import ru.intertrust.cm.core.config.TextFieldConfig;
import ru.intertrust.cm.core.config.TimelessDateFieldConfig;
import ru.intertrust.cm.core.config.UniqueKeyConfig;
import ru.intertrust.cm.core.config.UniqueKeyFieldConfig;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;

/**
 * Класс для генерации sql запросов для {@link PostgreSqlDataStructureDaoImpl}
 * @author vmatsukevich Date: 5/20/13 Time: 2:12 PM
 */
public class PostgreSqlQueryHelper {

    public static final String ACL_TABLE_SUFFIX = "_acl";

    public static final String READ_TABLE_SUFFIX = "_read";

    private static final String GROUP_TABLE = "user_group";

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
    public static String generateCreateDomainObjectTypeIdTableQuery() {
        return "create table " + wrap(DOMAIN_OBJECT_TYPE_ID_TABLE) + " (" +
                wrap(ID_COLUMN) + " bigint not null default " +
                    "nextval('" + wrap(getSqlSequenceName(DOMAIN_OBJECT_TYPE_ID_TABLE)) +   "'), " +
                wrap(NAME_COLUMN) + " varchar(256) not null, " +
                "constraint " + wrap("pk_" + DOMAIN_OBJECT_TYPE_ID_TABLE) + " primary key (" + wrap(ID_COLUMN) + "), " +
                "constraint " + wrap("u_" + DOMAIN_OBJECT_TYPE_ID_TABLE) + " unique (" + wrap(NAME_COLUMN) + "))";
    }

    /**
     * Генерирует запрос для создания последовательности для domain_object_type_id
     * @return запрос для создания последовательности для domain_object_type_id
     */
    public static String generateCreateDomainObjectTypeIdSequenceQuery() {
        return "create sequence " + wrap(getSqlSequenceName(DOMAIN_OBJECT_TYPE_ID_TABLE)) + " start 5001";
    }

    /**
     * Генерирует запрос, создающий таблицу для хранения версий конфигурации
     * @return запрос, создающий таблицу для хранения версий конфигурации
     */
    public static String generateCreateConfigurationTableQuery() {
        return "create table " + wrap(CONFIGURATION_TABLE) + " (" + wrap(ID_COLUMN) + " bigserial not null, " +
                wrap(CONTENT_COLUMN) + " text not null, " +
                wrap(LOADED_DATE_COLUMN) + " timestamp not null, " +
                "constraint " + wrap("pk_" + CONFIGURATION_TABLE) + " primary key (" + wrap(ID_COLUMN) + "))";
    }

    /**
     * Генерирует запрос, создающий таблицу AUTHENTICATION_INFO
     * @return запрос, создающий таблицу AUTHENTICATION_INFO
     */
    public static String generateCreateAuthenticationInfoTableQuery() {
        return "CREATE TABLE " + wrap(AUTHENTICATION_INFO_TABLE) +
                " (" + wrap(ID_COLUMN) + " bigint not null, " +
                wrap(USER_UID_COLUMN) + " character varying(64) NOT NULL, " +
                wrap("password") + " character varying(128), " +
                "constraint " + wrap("pk_" + AUTHENTICATION_INFO_TABLE + "_" + ID_COLUMN) + " " +
                    "primary key (" + wrap(ID_COLUMN) + "), " +
                "constraint " + wrap("u_" + AUTHENTICATION_INFO_TABLE + "_" + USER_UID_COLUMN) + " " +
                    "unique" + "(" + wrap(USER_UID_COLUMN) + "))";
    }

    private static String createAclTableQueryFor(String domainObjectType) {
        return "create table " + wrap(domainObjectType + "_acl") + " (" +
                wrap("object_id") + " bigint not null, " + wrap("group_id") + " bigint not null, " +
                wrap("operation") + " varchar(256) not null, " +
                "constraint " + wrap("pk_" + domainObjectType.toLowerCase() + "_acl") +
                    " primary key (" + wrap("object_id") + ", " + wrap("group_id") + ", " +
                    wrap(OPERATION_COLUMN) + ")";
    }

    private static String createAclReadTableQueryFor(String domainObjectType) {
        return "create table " + wrap(domainObjectType + "_read") + " (" +
                wrap("object_id") + " bigint not null, " + wrap("group_id") + " bigint not null, " +
                "constraint " + wrap("pk_" + domainObjectType + "_read") + " primary key (" + wrap("object_id") +
                ", " + wrap("group_id") + ")";
    }

    private static void appendFKConstraintForDO(String sourceDomainObjectType, String targetDomainObjectType, StringBuilder query) {
        query.append(", ").append("CONSTRAINT ").
                append(wrap("fk_" + sourceDomainObjectType.toLowerCase() + "_" + targetDomainObjectType.toLowerCase())).
                append(" FOREIGN KEY (").append(wrap("object_id")).append(") REFERENCES ").
                append(wrap(targetDomainObjectType)).append(" (").append(wrap(ID_COLUMN)).append(")");
}

    private static void appendFKConstraintForGroup(String domainObjectType, StringBuilder query) {
        query.append(", ").append("CONSTRAINT ").append(wrap("fk_" + domainObjectType + "_" + GROUP_TABLE)).
                append(" FOREIGN KEY (").append(wrap("group_id")).append(") REFERENCES ").append(wrap(GROUP_TABLE)).
                append(" (").append(wrap(ID_COLUMN)).append(")");
    }

    /**
     * Генерирует запрос, создающий последовательность(сиквенс) по конфигурации
     * доменного объекта
     * @param config
     *            конфигурация доменного объекта
     * @return запрос, создающий последовательность(сиквенс) по конфигурации
     *         доменного объекта
     */
    public static String generateSequenceQuery(DomainObjectTypeConfig config) {
        String sequenceName = getSqlSequenceName(config);
        StringBuilder query = new StringBuilder();
        query.append("create sequence ").append(wrap(sequenceName));

        return query.toString();
    }

    /**
     * Генерирует запрос, создающий последовательность(сиквенс) по конфигурации
     * доменного объекта
     * @param config
     *            конфигурация доменного объекта
     * @return запрос, создающий последовательность(сиквенс) по конфигурации
     *         доменного объекта
     */
    public static String generateAuditSequenceQuery(DomainObjectTypeConfig config) {
        String sequenceName = getSqlAuditSequenceName(config);
        StringBuilder query = new StringBuilder();
        query.append("create sequence ").append(wrap(sequenceName));

        return query.toString();
    }

    /**
     * Генерирует запрос, создающий таблицу по конфигурации доменного объекта
     * @param config
     *            конфигурация доменного объекта
     * @return запрос, создающий таблицу по конфигурации доменного объекта
     */
    public static String generateCreateTableQuery(DomainObjectTypeConfig config) {
        String tableName = getSqlName(config);
        StringBuilder query = new StringBuilder("create table ").append(wrap(tableName)).append(" ( ");

        appendSystemColumnsQueryPart(config, query);

        if (config.getFieldConfigs().size() > 0) {
            query.append(", ");
            appendColumnsQueryPart(query, config.getFieldConfigs(), false);
        }

        appendPKConstraintQueryPart(query, tableName);

        // Необходимо создать уникальный ключ (ID, TYPE_ID), чтобы обеспечить
        // возможность создания внешнего ключа,
        // ссылающегося на эти колонки
        appendIdTypeUniqueConstraint(query, tableName);

        appendParentFKConstraintsQueryPart(query, tableName, config);

        query.append(")");

        return query.toString();
    }

    /**
     * Генерирует запрос, создающий таблицу аудит лога по конфигурации доменного
     * объекта
     * @param config
     *            конфигурация доменного объекта
     * @return запрос, создающий таблицу по конфигурации доменного объекта
     */
    public static String generateCreateAuditTableQuery(DomainObjectTypeConfig config) {
        String tableName = getSqlName(config) + "_log";
        StringBuilder query = new StringBuilder("create table ").append(wrap(tableName)).append(" (");

        // Системные атрибуты
        query.append(wrap(ID_COLUMN)).append(" bigint not null, ");
        query.append(wrap(TYPE_COLUMN)).append(" integer not null");
        if (config.getExtendsAttribute() == null) {
            query.append(", ");
            query.append(wrap(DomainObjectDao.OPERATION_COLUMN)).append(" int not null, ");
            query.append(wrap(DomainObjectDao.UPDATED_DATE_COLUMN)).append(" timestamp not null, ");
            query.append(wrap(DOMAIN_OBJECT_ID_COLUMN)).append(" bigint not null, ");
            query.append(wrap(COMPONENT_COLUMN)).append(" varchar(512), ");
            query.append(wrap(IP_ADDRESS_COLUMN)).append(" varchar(16), ");
            query.append(wrap(INFO_COLUMN)).append(" varchar(512)");
        }

        if (config.getFieldConfigs().size() > 0) {
            query.append(", ");
            appendAuditLogColumnsQueryPart(query, config.getFieldConfigs(), false);
        }

        appendPKConstraintQueryPart(query, tableName);

        // Необходимо создать уникальный ключ (ID, TYPE_ID), чтобы обеспечить
        // возможность создания внешнего ключа,
        // ссылающегося на эти колонки
        appendIdTypeUniqueConstraint(query, tableName);

        query.append(", ");
        if (config.getExtendsAttribute() != null) {
            appendFKConstraint(query, tableName, ID_COLUMN, config.getExtendsAttribute() + "_log", ID_COLUMN);
            query.append(", ");
        }

        appendFKConstraint(query, tableName, TYPE_COLUMN, DOMAIN_OBJECT_TYPE_ID_TABLE, ID_COLUMN);

        query.append(")");

        return query.toString();
    }

    /**
     * Генерирует запрос, создающий _ACL таблицу (таблица прав доступа, кроме
     * чтения для ДО) для соответствующего доменного объекта.
     * @param config
     *            конфигурация доменного объекта
     * @return запрос, создающий _ACL таблицу для соответствующего доменного
     *         объекта
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
     * Генерирует запрос, создающий _READ таблицу (таблица разрений на чтение
     * для ДО) для соответствующего доменного объекта.
     * @param config
     *            конфигурация доменного объекта
     * @return запрос, создающий _READ талицу для соответствующего доменного
     *         объекта
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
     * Генерирует запрос для обновления структуры таблицы (добавления колонок и
     * уникальных ключей)
     * @param domainObjectConfigName
     *            название доменного объекта, таблицу которого необходимо
     *            обновить
     * @param fieldConfigList
     *            список колонок для добавления
     * @return запрос для обновления структуры таблицы (добавления колонок и
     *         уникальных ключей)
     */
    public static String generateAddColumnsQuery(String domainObjectConfigName, List<FieldConfig> fieldConfigList) {
        String tableName = getSqlName(domainObjectConfigName);
        StringBuilder query = new StringBuilder("alter table ").append(wrap(tableName)).append(" ");
        appendColumnsQueryPart(query, fieldConfigList, true);

        return query.toString();
    }

    /**
     * Генерирует запрос для создания форен-ки и уникальных констрэйнтов
     * @param domainObjectConfigName
     *            название доменного объекта, таблицу которого необходимо
     *            обновить
     * @param fieldConfigList
     *            список колонок для создания форен-ки констрэйнтов
     * @param uniqueKeyConfigList
     *            список уникальных ключей
     * @return запрос для обновления структуры таблицы (добавления форен-ки и
     *         уникальных констрэйнтов)
     */
    public static String generateCreateForeignKeyAndUniqueConstraintsQuery(String domainObjectConfigName,
            List<ReferenceFieldConfig> fieldConfigList,
            List<UniqueKeyConfig> uniqueKeyConfigList) {
        String tableName = getSqlName(domainObjectConfigName);
        StringBuilder query = new StringBuilder("alter table ").append(wrap(tableName)).append(" ");

        boolean commaNeeded = false;
        boolean existsConstraints = false;
        for (ReferenceFieldConfig fieldConfig : fieldConfigList) {
            if (ConfigurationExplorer.REFERENCE_TYPE_ANY.equals(fieldConfig.getType())) {
                continue;
            }

            if (commaNeeded) {
                query.append(", ");
            } else {
                commaNeeded = true;
            }
            query.append("add ");

            String columnName = getSqlName(fieldConfig);
            String typeReferenceColumnName = getReferenceTypeColumnName(fieldConfig.getName());
            String referencedTableName = getSqlName(fieldConfig.getType());
            appendFKConstraint(query, tableName, new String[] { columnName,typeReferenceColumnName },
                    referencedTableName, new String[] { ID_COLUMN, TYPE_COLUMN });
            existsConstraints = true;
        }

        for (UniqueKeyConfig uniqueKeyConfig : uniqueKeyConfigList) {
            if (uniqueKeyConfig.getUniqueKeyFieldConfigs().isEmpty()) {
                continue;
            }

            DelimitedListFormatter<UniqueKeyFieldConfig> listFormatter =
                    new DelimitedListFormatter<UniqueKeyFieldConfig>() {
                        @Override
                        protected String format(UniqueKeyFieldConfig item) {
                            return getSqlName(item.getName());
                        }
                    };

            String constraintName = "u_" + tableName + "_" +
                    listFormatter.formatAsDelimitedList(uniqueKeyConfig.getUniqueKeyFieldConfigs(), "_");
            String fieldsList =
                    listFormatter.formatAsDelimitedList(uniqueKeyConfig.getUniqueKeyFieldConfigs(), ", ", "\"");

            if (commaNeeded) {
                query.append(", ");
            } else {
                commaNeeded = true;
            }

            query.append("add ");
            appendUniqueConstraint(query, constraintName, fieldsList);
            existsConstraints = true;
        }

        //Если нет констраинтов то возвращаем пустую строку
        if (!existsConstraints){
            query = new StringBuilder();
        }

        return query.toString();
    }

    /**
     * Генерирует SQL запрос по созданию автоматических индексов: индексов для ссылочных полей
     * @param config конфигурация доменного объекта
     * @return SQL запрос создания автоматических индексов
     */
    public static String generateCreateAutoIndexesQuery(DomainObjectTypeConfig config) {        
        return generateCreateIndexesQuery(config.getName(), config.getFieldConfigs());
    }

    /**
     * Генерирует SQL запрос по созданию индексов, явно указанных в конфигурации доменного объекта (настроенных вручную)
     * @param config конфигурация доменного объекта
     * @return SQL запрос создания индексов
     */
    public static String generateCreateExplicitIndexesQuery(DomainObjectTypeConfig config) {

        List<IndexConfig> indexConfigs = config.getIndicesConfig().getIndices();
        return generateCreateExplicitIndexesQuery(config.getName(), indexConfigs);

    }

    public static String generateCreateExplicitIndexesQuery(String domainObjectName,
            List<IndexConfig> indexConfigs) {
        StringBuilder query = new StringBuilder();
        String tableName = getSqlName(domainObjectName);
        for (IndexConfig indexConfig : indexConfigs) {
            appendComplexIndexQueryPart(query, tableName, indexConfig);
        }

        if (query.length() == 0) {
            return null;
        }

        return query.toString();
    }


    
    public static String generateDeleteExplicitIndexesQuery(String domainObjectName,
            List<IndexConfig> indexConfigs) {
        StringBuilder query = new StringBuilder();
        String tableName = getSqlName(domainObjectName);
        for (IndexConfig indexConfig : indexConfigs) {
            appendDeleteIndexQueryPart(query, tableName, indexConfig);

        }

        if (query.length() == 0) {
            return null;
        }

        return query.toString();
    }
    
    private static void appendComplexIndexQueryPart(StringBuilder query, String tableName, IndexConfig indexConfig) {
        List<String> fieldNames = new ArrayList<String>();
        for(IndexFieldConfig indexFieldConfig : indexConfig.getIndexFieldConfigs()){
            fieldNames.add(getSqlName(indexFieldConfig));
        }
        
        String indexType = getIndexType(indexConfig);

        appendIndexQueryPart(query, tableName, indexType, fieldNames);
    }

    private static void appendDeleteIndexQueryPart(StringBuilder query, String tableName, IndexConfig indexConfig) {
        List<String> fieldNames = new ArrayList<String>();
        for (IndexFieldConfig indexFieldConfig : indexConfig.getIndexFieldConfigs()) {
            fieldNames.add(getSqlName(indexFieldConfig));
        }

        appendDeleteIndexQueryPart(query, tableName, fieldNames);
    }

    private static String getIndexType(IndexConfig indexConfig) {
        String indexType = indexConfig.getType();

        if (indexType == null) {
            return IndexConfig.IndexType.BTREE.toString();
        }
        return indexType;
    }

    public static String generateCreateAuditLogIndexesQuery(DomainObjectTypeConfig config) {
        return generateCreateIndexesQuery(config.getName() + "_log", config.getFieldConfigs());
    }

    /**
     * Генерирует запрос, для создания индексов по конфигурации доменного
     * объекта
     * @param configName
     *            название конфигурации доменного объекта
     * @return запрос, для создания индексов по конфигурации доменного объекта
     */
    public static String generateCreateIndexesQuery(String configName, List<FieldConfig> fieldConfigList) {
        StringBuilder query = new StringBuilder();
        String tableName = getSqlName(configName);

        for (FieldConfig fieldConfig : fieldConfigList) {
            if (!ReferenceFieldConfig.class.equals(fieldConfig.getClass())) {
                continue;
            }
            appendIndexQueryPart(query, tableName, getSqlName(fieldConfig));
        }

        if (query.length() == 0) {
            return null;
        }

        return query.toString();
    }

    private static void appendIndexQueryPart(StringBuilder query, String tableName, String fieldName) {
        String indexName = "i_" + tableName + "_" + fieldName;
        query.append("create index ").append(wrap(indexName)).append(" on ").append(wrap(tableName)).append(" (").
                append(wrap(fieldName)).append(");\n");
    }

    private static void appendIndexQueryPart(StringBuilder query, String tableName, String indexType, List<String> fieldNames) {
        String indexFieldsPart = createIndexTableFieldsPart(fieldNames);

        String indexName = createExplicitIndexName(tableName, fieldNames);
        query.append("create index ").append(wrap(indexName)).append(" on ").append(wrap(tableName)).append(" USING ").append(indexType).append(" (").
                append(indexFieldsPart).append(");\n");
    }

    private static void appendDeleteIndexQueryPart(StringBuilder query, String tableName, List<String> fieldNames) {
        String indexName = createExplicitIndexName(tableName, fieldNames);
        query.append("drop index if exists ").append(wrap(indexName)).append(";\n");
    }

    private static String createExplicitIndexName(String tableName, List<String> fieldNames) {
        String fieldsSuffix = createIndexSuffix(fieldNames);
        String indexName = "i_" + tableName + fieldsSuffix;
        return indexName;
    }

    public static String createIndexSuffix(List<String> fieldNames) {
        StringBuilder fieldsSuffix = new StringBuilder();
        for (String fieldName : fieldNames) {
            fieldsSuffix.append("_").append(fieldName);
        }
        return fieldsSuffix.toString();
    }

    private static String createIndexTableFieldsPart(List<String> fieldNames) {
        StringBuilder fieldsEnumeration = new StringBuilder();
        
        int index = 0; 
        for (String fieldName : fieldNames) {
            fieldsEnumeration.append(wrap(fieldName));
            if (index < fieldNames.size() - 1) {
                fieldsEnumeration.append(", ");
            }
            index++;
        }
        return fieldsEnumeration.toString();
    }

    private static void appendParentFKConstraintsQueryPart(StringBuilder query, String tableName,
            DomainObjectTypeConfig config) {
        query.append(", ");
        if (config.getExtendsAttribute() != null) {
            appendFKConstraint(query, tableName, ID_COLUMN, config.getExtendsAttribute(), ID_COLUMN);
            query.append(", ");
        }

        appendFKConstraint(query, tableName, TYPE_COLUMN, DOMAIN_OBJECT_TYPE_ID_TABLE, ID_COLUMN);
    }

    public static void applyOffsetAndLimit(StringBuilder query, int offset, int limit) {
        if (limit != 0) {
            query.append(" limit ").append(limit).append(" OFFSET ").append(offset);
        }
    }

    public static String wrap(String string) {
        if (string == null || string.startsWith("\"")) {
            return string;
        } else {
            return "\"" + string + "\"";
        }
    }

    public static String unwrap(String string) {
        if (string == null || !string.startsWith("\"")) {
            return string;
        } else {
            return string.substring(1, string.length() - 1);
        }
    }

    private static void appendFKConstraint(StringBuilder query, String tableName, String columnName,
            String referencedTable, String referencedFieldName) {
        appendFKConstraint(query, tableName, new String[] { columnName }, referencedTable,
                new String[] { referencedFieldName });
    }

    private static void appendFKConstraint(StringBuilder query, String tableName, String[] columnNames,
            String referencedTable, String[] referencedFieldNames) {
        DelimitedListFormatter<String> listFormatter = new DelimitedListFormatter<>();

        String constraintName = "fk_" + tableName + "_" + listFormatter.formatAsDelimitedList(columnNames, "_");

        query.append("constraint ").append(wrap(constraintName)).append(" foreign key (").
                append(listFormatter.formatAsDelimitedList(columnNames, ", ", "\"")).append(")").
                append(" ").append("references ").append(wrap(getSqlName(referencedTable))).
                append(" (").append(listFormatter.formatAsDelimitedList(referencedFieldNames, ", ", "\"")).append(")");
    }

    private static void appendPKConstraintQueryPart(StringBuilder query, String tableName) {
        String pkName = "pk_" + tableName + "_id";
        query.append(", constraint ").append(wrap(pkName)).append(" primary key (").
                append(wrap(ID_COLUMN)).append(")");
    }

    private static void appendIdTypeUniqueConstraint(StringBuilder query, String tableName) {
        DelimitedListFormatter<String> listFormatter = new DelimitedListFormatter<>();
        String[] keyFields = new String[] { ID_COLUMN, TYPE_COLUMN };

        String constraintName = "u_" + tableName + "_" + listFormatter.formatAsDelimitedList(keyFields, "_");
        String fieldsList = listFormatter.formatAsDelimitedList(keyFields, ", ", "\"");

        query.append(", ");
        appendUniqueConstraint(query, constraintName, fieldsList);
    }

    private static void appendSystemColumnsQueryPart(DomainObjectTypeConfig config, StringBuilder query) {
        query.append(wrap(ID_COLUMN)).append(" bigint not null, ");
        query.append(wrap(TYPE_COLUMN)).append(" integer");

        if (config.getExtendsAttribute() == null) {
            query.append(", ").append(wrap(CREATED_DATE_COLUMN)).append(" timestamp not null, ");
            query.append(wrap(UPDATED_DATE_COLUMN)).append(" timestamp not null, ");

            query.append(wrap(GenericDomainObject.STATUS_FIELD_NAME)).append(" bigint, ");
            query.append(wrap(DomainObjectDao.STATUS_TYPE_COLUMN)).append(" integer");
        }
    }

    private static void appendColumnsQueryPart(StringBuilder query, List<FieldConfig> fieldConfigList,
            boolean isAlterQuery) {
        int size = fieldConfigList.size();
        for (int i = 0; i < size; i++) {
            FieldConfig fieldConfig = fieldConfigList.get(i);

            if (i > 0) {
                query.append(", ");
            }

            if (isAlterQuery) {
                query.append("add column ");
            }

            query.append(wrap(getSqlName(fieldConfig))).append(" ").append(getSqlType(fieldConfig));
            if (fieldConfig.isNotNull()) {
                query.append(" not null");
            }

            if (ReferenceFieldConfig.class.equals(fieldConfig.getClass())) {
                query.append(", ");
                if (isAlterQuery) {
                    query.append("add column ");
                }
                query.append(wrap(getReferenceTypeColumnName(fieldConfig.getName()))).append(" integer");
                if (fieldConfig.isNotNull()) {
                    query.append(" not null");
                }
            } else if (DateTimeWithTimeZoneFieldConfig.class.equals(fieldConfig.getClass())) {
                query.append(", ");
                if (isAlterQuery) {
                    query.append("add column ");
                }
                query.append(wrap(getTimeZoneIdColumnName(fieldConfig.getName()))).append(" ").
                        append(getTimeZoneIdSqlType());
                if (fieldConfig.isNotNull()) {
                    query.append(" not null");
                }
            }
        }
    }

    private static void appendAuditLogColumnsQueryPart(StringBuilder query, List<FieldConfig> fieldConfigList,
            boolean isAlterQuery) {
        int size = fieldConfigList.size();
        for (int i = 0; i < size; i++) {
            FieldConfig fieldConfig = fieldConfigList.get(i);

            if (i > 0) {
                query.append(", ");
            }

            if (isAlterQuery) {
                query.append("add column ");
            }

            query.append(wrap(getSqlName(fieldConfig))).append(" ").append(getSqlType(fieldConfig));

            if (ReferenceFieldConfig.class.equals(fieldConfig.getClass())) {
                query.append(", ");
                query.append(wrap(getReferenceTypeColumnName(fieldConfig.getName()))).append(" integer");
            } else if (DateTimeWithTimeZoneFieldConfig.class.equals(fieldConfig.getClass())) {
                query.append(", ");
                query.append(wrap(getTimeZoneIdColumnName(fieldConfig.getName()))).append(" ").
                        append(getTimeZoneIdSqlType());
            }
        }
    }

    private static void appendUniqueConstraint(StringBuilder query, String constraintName, String fieldsList) {
        query.append("constraint ").append(wrap(constraintName)).append(" unique (").
                append(fieldsList).append(")");
    }

    private static String getTimeZoneIdSqlType() {
        return "varchar(50)";
    }

    private static String getSqlType(FieldConfig fieldConfig) {
        if (DateTimeFieldConfig.class.equals(fieldConfig.getClass()) ||
                DateTimeWithTimeZoneFieldConfig.class.equals(fieldConfig.getClass()) ||
                TimelessDateFieldConfig.class.equals(fieldConfig.getClass())) {
            return "timestamp";
        }

        if (DecimalFieldConfig.class.equals(fieldConfig.getClass())) {
            StringBuilder sqlType = new StringBuilder("decimal");
            DecimalFieldConfig decimalFieldConfig = (DecimalFieldConfig) fieldConfig;

            if (decimalFieldConfig.getPrecision() != null && decimalFieldConfig.getScale() != null) {
                sqlType.append("(").append(decimalFieldConfig.getPrecision()).append(", ").
                        append(decimalFieldConfig.getScale()).append(")");
            } else if (decimalFieldConfig.getPrecision() != null) {
                sqlType.append("(").append(decimalFieldConfig.getPrecision()).append(")");
            }

            return sqlType.toString();
        }

        if (LongFieldConfig.class.equals(fieldConfig.getClass())) {
            return "bigint";
        }

        if (ReferenceFieldConfig.class.equals(fieldConfig.getClass())) {
            return "bigint";
        }

        if (StringFieldConfig.class.equals(fieldConfig.getClass())) {
            return "varchar(" + ((StringFieldConfig) fieldConfig).getLength() + ")";
        }

        if (TextFieldConfig.class.equals(fieldConfig.getClass())) {
            return "text";
        }

        if (PasswordFieldConfig.class.equals(fieldConfig.getClass())) {
            return "varchar(" + ((PasswordFieldConfig) fieldConfig).getLength() + ")";
        }

        if (BooleanFieldConfig.class.equals(fieldConfig.getClass())) {
            return "smallint check (" + getSqlName(fieldConfig) + " in (0, 1))";
        }

        throw new IllegalArgumentException("Invalid field type");
    }

}
