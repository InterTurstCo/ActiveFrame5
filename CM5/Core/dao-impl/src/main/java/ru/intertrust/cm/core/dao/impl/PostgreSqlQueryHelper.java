package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;

import java.util.Arrays;
import java.util.List;

import static ru.intertrust.cm.core.dao.api.ConfigurationDao.CONFIGURATION_TABLE;
import static ru.intertrust.cm.core.dao.api.DataStructureDao.AUTHENTICATION_INFO_TABLE;
import static ru.intertrust.cm.core.dao.api.DomainObjectDao.*;
import static ru.intertrust.cm.core.dao.api.DomainObjectTypeIdDao.DOMAIN_OBJECT_TYPE_ID_TABLE;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.*;

/**
 * Класс для генерации sql запросов для {@link PostgreSqlDataStructureDaoImpl}
 * @author vmatsukevich Date: 5/20/13 Time: 2:12 PM
 */
public class PostgreSqlQueryHelper {

    public static final String ACL_TABLE_SUFFIX = "_ACL";

    public static final String READ_TABLE_SUFFIX = "_READ";

    private static final String GROUP_TABLE = "User_Group";

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
        query.append("create sequence ");
        query.append(sequenceName);

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
        query.append("create sequence ");
        query.append(sequenceName);

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
        StringBuilder query = new StringBuilder("create table ").append(tableName).append(" ( ");

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
        String tableName = getSqlName(config) + "_LOG";
        StringBuilder query = new StringBuilder("create table ").append(tableName).append(" ( ");

        // Системные атрибуты
        query.append("ID bigint not null, ");
        query.append(TYPE_COLUMN + " integer not null");
        if (config.getExtendsAttribute() == null) {
            query.append(", ");
            query.append(DomainObjectDao.OPERATION_COLUMN + " int not null, ");
            query.append(DomainObjectDao.UPDATED_DATE_COLUMN + " timestamp not null, ");
            query.append(DOMAIN_OBJECT_ID + " bigint not null, ");
            query.append(COMPONENT + " varchar(512), ");
            query.append(IP_ADDRESS + " varchar(16), ");
            query.append(INFO + " varchar(512)");
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
            appendFKConstraint(query, tableName, ID_COLUMN, config.getExtendsAttribute() + "_LOG", ID_COLUMN);
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
        StringBuilder query = new StringBuilder("alter table ").append(tableName).append(" ");
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
        StringBuilder query = new StringBuilder("alter table ").append(tableName).append(" ");

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

            String constraintName = "U_" + tableName + "_" +
                    listFormatter.formatAsDelimitedList(uniqueKeyConfig.getUniqueKeyFieldConfigs(), "_");
            String fieldsList = listFormatter.formatAsDelimitedList(uniqueKeyConfig.getUniqueKeyFieldConfigs(), ", ");

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

    public static String generateCreateIndexesQuery(DomainObjectTypeConfig config) {
        return generateCreateIndexesQuery(config.getName(), config.getFieldConfigs());
    }

    public static String generateCreateAuditLogIndexesQuery(DomainObjectTypeConfig config) {
        return generateCreateIndexesQuery(config.getName() + "_LOG", config.getFieldConfigs());
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
        String indexName = "I_" + tableName + "_" + fieldName;
        query.append("create index ").append(indexName).append(" on ").append(tableName).append(" (").
                append(fieldName).append(");\n");
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

    private static void appendFKConstraint(StringBuilder query, String tableName, String columnName,
            String referencedTable, String referencedFieldName) {
        appendFKConstraint(query, tableName, new String[] { columnName }, referencedTable,
                new String[] { referencedFieldName });
    }

    private static void appendFKConstraint(StringBuilder query, String tableName, String[] columnNames,
            String referencedTable, String[] referencedFieldNames) {
        DelimitedListFormatter<String> listFormatter = new DelimitedListFormatter<>();

        String constraintName = "FK_" + tableName + "_" + listFormatter.formatAsDelimitedList(columnNames, "_");

        query.append("constraint ").append(constraintName).append(" foreign key (").
                append(listFormatter.formatAsDelimitedList(columnNames, ", ")).append(")").
                append(" ").append("references").append(" ").append(getSqlName(referencedTable)).
                append("(").append(listFormatter.formatAsDelimitedList(referencedFieldNames, ", ")).append(")");
    }

    private static void appendPKConstraintQueryPart(StringBuilder query, String tableName) {
        String pkName = "PK_" + tableName + "_ID";
        query.append(", constraint ").append(pkName).append(" primary key (ID)");
    }

    private static void appendIdTypeUniqueConstraint(StringBuilder query, String tableName) {
        DelimitedListFormatter<String> listFormatter = new DelimitedListFormatter<>();
        String[] keyFields = new String[] { ID_COLUMN,TYPE_COLUMN };

        String constraintName = "U_" + tableName + "_" + listFormatter.formatAsDelimitedList(keyFields, "_");
        String fieldsList = listFormatter.formatAsDelimitedList(keyFields, ", ");

        query.append(", ");
        appendUniqueConstraint(query, constraintName, fieldsList);
    }

    private static void appendSystemColumnsQueryPart(DomainObjectTypeConfig config, StringBuilder query) {
        query.append("ID bigint not null, ");

        if (config.getExtendsAttribute() == null) {
            query.append("CREATED_DATE timestamp not null, ");
            query.append("UPDATED_DATE timestamp not null, ");

            query.append(GenericDomainObject.STATUS_COLUMN + " bigint, ");
            query.append(DomainObjectDao.STATUS_TYPE_COLUMN + " integer, ");

        }

        query.append(TYPE_COLUMN + " integer");
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

            query.append(getSqlName(fieldConfig)).append(" ").append(getSqlType(fieldConfig));
            if (fieldConfig.isNotNull()) {
                query.append(" not null");
            }

            if (ReferenceFieldConfig.class.equals(fieldConfig.getClass())) {
                query.append(", ");
                if (isAlterQuery) {
                    query.append("add column ");
                }
                query.append(getReferenceTypeColumnName(fieldConfig.getName())).append(" integer");
                if (fieldConfig.isNotNull()) {
                    query.append(" not null");
                }
            } else if (DateTimeWithTimeZoneFieldConfig.class.equals(fieldConfig.getClass())) {
                query.append(", ");
                if (isAlterQuery) {
                    query.append("add column ");
                }
                query.append(getTimeZoneIdColumnName(fieldConfig.getName())).append(" ").
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

            query.append(getSqlName(fieldConfig)).append(" ").append(getSqlType(fieldConfig));

            if (ReferenceFieldConfig.class.equals(fieldConfig.getClass())) {
                query.append(", ");
                query.append(getReferenceTypeColumnName(fieldConfig.getName())).append(" integer");
            } else if (DateTimeWithTimeZoneFieldConfig.class.equals(fieldConfig.getClass())) {
                query.append(", ");
                query.append(getTimeZoneIdColumnName(fieldConfig.getName())).
                        append(" ").append(getTimeZoneIdSqlType());
            }
        }
    }

    private static void appendUniqueConstraint(StringBuilder query, String constraintName, String fieldsList) {
        query.append("constraint ").append(constraintName).append(" unique (").
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

    private static class DelimitedListFormatter<T> {

        public String formatAsDelimitedList(Iterable<T> iterable, String delimiter) {
            if (!iterable.iterator().hasNext()) {
                throw new IllegalArgumentException("Iterable parameter is empty");
            }

            StringBuilder result = new StringBuilder();
            boolean delimiterNeed = false;
            for (T item : iterable) {
                if (delimiterNeed) {
                    result.append(delimiter);
                } else {
                    delimiterNeed = true;
                }
                result.append(format(item));
            }

            return result.toString();
        }

        public String formatAsDelimitedList(T[] items, String delimiter) {
            return formatAsDelimitedList(Arrays.asList(items), delimiter);
        }

        protected String format(T item) {
            return item.toString();
        }
    }
}
