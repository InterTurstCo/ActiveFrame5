package ru.intertrust.cm.core.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static ru.intertrust.cm.core.dao.api.ConfigurationDao.*;
import static ru.intertrust.cm.core.dao.api.DataStructureDao.AUTHENTICATION_INFO_TABLE;
import static ru.intertrust.cm.core.dao.api.DataStructureDao.USER_UID_COLUMN;
import static ru.intertrust.cm.core.dao.api.DomainObjectDao.*;
import static ru.intertrust.cm.core.dao.api.DomainObjectTypeIdDao.DOMAIN_OBJECT_TYPE_ID_TABLE;
import static ru.intertrust.cm.core.dao.api.DomainObjectTypeIdDao.NAME_COLUMN;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.*;
import static ru.intertrust.cm.core.dao.impl.utils.DaoUtils.*;

/**
 * Класс для генерации sql запросов для {@link ru.intertrust.cm.core.dao.impl.PostgreSqlDataStructureDaoImpl}
 * @author vmatsukevich Date: 5/20/13 Time: 2:12 PM
 */
public abstract class BasicQueryHelper {

    private static final Logger logger = LoggerFactory.getLogger(BasicQueryHelper.class);
    
    public static final String ACL_TABLE_SUFFIX = "_acl";

    public static final String READ_TABLE_SUFFIX = "_read";

    private static final String GROUP_TABLE = "user_group";

    /**
     * Генерирует запрос, возвращающий кол-во таблиц в базе данных
     * @return запрос, возвращающий кол-во таблиц в базе данных
     */
    public abstract String generateCountTablesQuery();

    /**
     * Генерирует запрос, создающий таблицу BUSINESS_OBJECT
     * @return запрос, создающий таблицу BUSINESS_OBJECT
     */
    public String generateCreateDomainObjectTypeIdTableQuery() {
        return "create table " + wrap(DOMAIN_OBJECT_TYPE_ID_TABLE) + " (" +
                wrap(ID_COLUMN) + " " + getIdType() + " not null, " +
                wrap(NAME_COLUMN) + " varchar(256) not null, " +
                "constraint " + wrap("pk_" + DOMAIN_OBJECT_TYPE_ID_TABLE) + " primary key (" + wrap(ID_COLUMN) + "), " +
                "constraint " + wrap("u_" + DOMAIN_OBJECT_TYPE_ID_TABLE) + " unique (" + wrap(NAME_COLUMN) + "))";
    }

    /**
     * Генерирует запрос для создания последовательности для domain_object_type_id
     * @return запрос для создания последовательности для domain_object_type_id
     */
    public String generateCreateDomainObjectTypeIdSequenceQuery() {
        return "create sequence " + wrap(getSqlSequenceName(DOMAIN_OBJECT_TYPE_ID_TABLE)) + " start with 5001";
    }

    public String generateCreateConfigurationSequenceQuery() {
        return "create sequence " + wrap(getSqlSequenceName(CONFIGURATION_TABLE));
    }

    /**
     * Генерирует запрос, создающий таблицу для хранения версий конфигурации
     * @return запрос, создающий таблицу для хранения версий конфигурации
     */
    public String generateCreateConfigurationTableQuery() {
        return "create table " + wrap(CONFIGURATION_TABLE) + " (" + wrap(ID_COLUMN) + " " + getIdType() + " not null, " +
                wrap(CONTENT_COLUMN) + " " + getTextType() + " not null, " +
                wrap(LOADED_DATE_COLUMN) + " timestamp not null, " +
                "constraint " + wrap("pk_" + CONFIGURATION_TABLE) + " primary key (" + wrap(ID_COLUMN) + "))";
    }

    /**
     * Генерирует запрос, создающий таблицу AUTHENTICATION_INFO
     * @return запрос, создающий таблицу AUTHENTICATION_INFO
     */
    public String generateCreateAuthenticationInfoTableQuery() {
        return "CREATE TABLE " + wrap(AUTHENTICATION_INFO_TABLE) +
                " (" + wrap(ID_COLUMN) + " " + getIdType() + " not null, " +
                wrap(USER_UID_COLUMN) + " character varying(64) NOT NULL, " +
                wrap("password") + " character varying(128), " +
                "constraint " + wrap("pk_" + AUTHENTICATION_INFO_TABLE + "_" + ID_COLUMN) + " " +
                    "primary key (" + wrap(ID_COLUMN) + "), " +
                "constraint " + wrap("u_" + AUTHENTICATION_INFO_TABLE + "_" + USER_UID_COLUMN) + " " +
                    "unique" + "(" + wrap(USER_UID_COLUMN) + "))";
    }

    private String createAclTableQueryFor(String domainObjectType) {
        return "create table " + wrap(domainObjectType + "_acl") + " (" +
                wrap("object_id") + " " + getIdType() + " not null, " +
                wrap("group_id") + " " + getIdType() + " not null, " +
                wrap("operation") + " varchar(256) not null, " +
                "constraint " + wrap("pk_" + domainObjectType.toLowerCase() + "_acl") +
                    " primary key (" + wrap("object_id") + ", " + wrap("group_id") + ", " +
                    wrap(OPERATION_COLUMN) + ")";
    }

    private String createAclReadTableQueryFor(String domainObjectType) {
        return "create table " + wrap(domainObjectType + "_read") + " (" +
                wrap("object_id") + " " + getIdType() + " not null, " +
                wrap("group_id") + " " + getIdType() + " not null, " +
                "constraint " + wrap("pk_" + domainObjectType + "_read") + " primary key (" + wrap("object_id") +
                ", " + wrap("group_id") + ")";
    }

    private void appendFKConstraintForDO(String sourceDomainObjectType, String targetDomainObjectType, StringBuilder query) {
        query.append(", ").append("CONSTRAINT ").
                append(wrap("fk_" + sourceDomainObjectType.toLowerCase() + "_" + targetDomainObjectType.toLowerCase())).
                append(" FOREIGN KEY (").append(wrap("object_id")).append(") REFERENCES ").
                append(wrap(targetDomainObjectType)).append(" (").append(wrap(ID_COLUMN)).append(")");
}

    private void appendFKConstraintForGroup(String domainObjectType, StringBuilder query) {
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
    public String generateSequenceQuery(DomainObjectTypeConfig config) {
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
    public String generateAuditSequenceQuery(DomainObjectTypeConfig config) {
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
    public String generateCreateTableQuery(DomainObjectTypeConfig config) {
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
    public String generateCreateAuditTableQuery(DomainObjectTypeConfig config) {
        String tableName = getSqlName(config) + "_log";
        StringBuilder query = new StringBuilder("create table ").append(wrap(tableName)).append(" (");

        // Системные атрибуты
        query.append(wrap(ID_COLUMN)).append(" ").append(getIdType()).append(" not null, ");
        query.append(wrap(TYPE_COLUMN)).append(" integer not null");
        if (config.getExtendsAttribute() == null) {
            query.append(", ");
            query.append(wrap(DomainObjectDao.OPERATION_COLUMN)).append(" int not null, ");
            query.append(wrap(DomainObjectDao.UPDATED_DATE_COLUMN)).append(" timestamp not null, ");
            query.append(wrap(DOMAIN_OBJECT_ID_COLUMN)).append(" ").append(getIdType()).append(" not null, ");
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
    public String generateCreateAclTableQuery(DomainObjectTypeConfig config) {
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
    public String generateCreateAclReadTableQuery(DomainObjectTypeConfig config) {
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
    public String generateAddColumnsQuery(String domainObjectConfigName, List<FieldConfig> fieldConfigList) {
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
    public String generateCreateForeignKeyAndUniqueConstraintsQuery(String domainObjectConfigName,
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
     * Автоматически создаваемые индексы удаляются из списка индексов для создания/удаления.
     * @param domainObjectTypeConfig конфигурация доменного объекта
     * @param indexConfigList спсисок дескрипторов индексов
     */
    public void skipAutoIndices(DomainObjectTypeConfig domainObjectTypeConfig, List<IndexConfig> indexConfigList) {
        List<FieldConfig> referenceFieldConfigs = new ArrayList<FieldConfig>();
        for (FieldConfig fieldConfig : domainObjectTypeConfig.getFieldConfigs()) {
            if (fieldConfig instanceof ReferenceFieldConfig) {
                referenceFieldConfigs.add(fieldConfig);
            }
        }
        List<Integer> skipIndexNumbers = new ArrayList<Integer>();

        int i = 0;
        for (IndexConfig indexConfig : indexConfigList) {
            for (FieldConfig referenceFieldConfig : referenceFieldConfigs) {
                if (getIndexFields(indexConfig).equals(referenceFieldConfig.getName())) {
                    skipIndexNumbers.add(i);
                }
            }
            i++;
        }

        Collections.sort(skipIndexNumbers, Collections.reverseOrder());        
        for (int skipIndexNumber : skipIndexNumbers) {
            logger.info("Index " + indexConfigList.get(skipIndexNumber)
                    + " will not be created/deleted since it matches an existing auto index");
            indexConfigList.remove(skipIndexNumber);
        }
    }

    private String getIndexFields(IndexConfig indexConfig) {
        StringBuilder indexFields = new StringBuilder();

        int i = 0;
        for (IndexFieldConfig indexFieldConfig : indexConfig.getIndexFieldConfigs()) {
            if (i > 0) {
                indexFields.append("_");
            }
            indexFields.append(indexFieldConfig.getName());
            i++;
        }
        return indexFields.toString();
    }
    
    public String generateDeleteExplicitIndexesQuery(String domainObjectName,
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
    
    public String generateComplexIndexQuery(String tableName, IndexConfig indexConfig) {
        List<String> fieldNames = new ArrayList<String>();
        for(IndexFieldConfig indexFieldConfig : indexConfig.getIndexFieldConfigs()){
            fieldNames.add(getSqlName(indexFieldConfig));
        }
        
        String indexType = getIndexType(indexConfig);
        return generateIndexQuery(tableName, indexType, fieldNames);
    }

    private void appendDeleteIndexQueryPart(StringBuilder query, String tableName, IndexConfig indexConfig) {
        List<String> fieldNames = new ArrayList<String>();
        for (IndexFieldConfig indexFieldConfig : indexConfig.getIndexFieldConfigs()) {
            fieldNames.add(getSqlName(indexFieldConfig));
        }

        appendDeleteIndexQueryPart(query, tableName, fieldNames);
    }

    private String getIndexType(IndexConfig indexConfig) {
        String indexType = indexConfig.getType();

        if (indexType == null) {
            return IndexConfig.IndexType.BTREE.toString();
        }
        return indexType;
    }

    public String generateCreateIndexQuery(DomainObjectTypeConfig config, ReferenceFieldConfig fieldConfig) {
        return generateCreateIndexQuery(config.getName(), fieldConfig.getName());
    }

    protected abstract String generateIndexQuery(String tableName, String indexType, List<String> fieldNames);

    public String generateCreateIndexQuery(String domainObjectTypeName, String fieldName) {
        String tableName = getSqlName(domainObjectTypeName);
        String columnName = getSqlName(fieldName);

        String indexName = createExplicitIndexName(tableName, Collections.singletonList(columnName));
        return "create index " + wrap(indexName) + " on " + wrap(tableName) + " (" + wrap(columnName) + ")";
    }

    private void appendDeleteIndexQueryPart(StringBuilder query, String tableName, List<String> fieldNames) {
        String indexName = createExplicitIndexName(tableName, fieldNames);
        query.append("drop index if exists ").append(wrap(indexName)).append(";\n");
    }

    protected String createExplicitIndexName(String tableName, List<String> fieldNames) {
        return "i_" + tableName + createIndexSuffix(fieldNames);
    }

    public String createIndexSuffix(List<String> fieldNames) {
        StringBuilder fieldsSuffix = new StringBuilder();
        for (String fieldName : fieldNames) {
            fieldsSuffix.append("_").append(fieldName);
        }
        return fieldsSuffix.toString();
    }

    protected String createIndexTableFieldsPart(List<String> fieldNames) {
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

    protected void appendParentFKConstraintsQueryPart(StringBuilder query, String tableName,
            DomainObjectTypeConfig config) {
        query.append(", ");
        if (config.getExtendsAttribute() != null) {
            appendFKConstraint(query, tableName, ID_COLUMN, config.getExtendsAttribute(), ID_COLUMN);
            query.append(", ");
        }

        appendFKConstraint(query, tableName, TYPE_COLUMN, DOMAIN_OBJECT_TYPE_ID_TABLE, ID_COLUMN);
    }

    private void appendFKConstraint(StringBuilder query, String tableName, String columnName,
            String referencedTable, String referencedFieldName) {
        appendFKConstraint(query, tableName, new String[] { columnName }, referencedTable,
                new String[] { referencedFieldName });
    }

    private void appendFKConstraint(StringBuilder query, String tableName, String[] columnNames,
            String referencedTable, String[] referencedFieldNames) {
        DelimitedListFormatter<String> listFormatter = new DelimitedListFormatter<>();

        String constraintName = "fk_" + tableName + "_" + listFormatter.formatAsDelimitedList(columnNames, "_");

        query.append("constraint ").append(wrap(constraintName)).append(" foreign key (").
                append(listFormatter.formatAsDelimitedList(columnNames, ", ", "\"")).append(")").
                append(" ").append("references ").append(wrap(getSqlName(referencedTable))).
                append(" (").append(listFormatter.formatAsDelimitedList(referencedFieldNames, ", ", "\"")).append(")");
    }

    private void appendPKConstraintQueryPart(StringBuilder query, String tableName) {
        String pkName = "pk_" + tableName + "_id";
        query.append(", constraint ").append(wrap(pkName)).append(" primary key (").
                append(wrap(ID_COLUMN)).append(")");
    }

    private void appendIdTypeUniqueConstraint(StringBuilder query, String tableName) {
        DelimitedListFormatter<String> listFormatter = new DelimitedListFormatter<>();
        String[] keyFields = new String[] { ID_COLUMN, TYPE_COLUMN };

        String constraintName = "u_" + tableName + "_" + listFormatter.formatAsDelimitedList(keyFields, "_");
        String fieldsList = listFormatter.formatAsDelimitedList(keyFields, ", ", "\"");

        query.append(", ");
        appendUniqueConstraint(query, constraintName, fieldsList);
    }

    private void appendSystemColumnsQueryPart(DomainObjectTypeConfig config, StringBuilder query) {
        query.append(wrap(ID_COLUMN)).append(" ").append(getIdType()).append(" not null, ");
        query.append(wrap(TYPE_COLUMN)).append(" integer");

        if (config.getExtendsAttribute() == null) {
            query.append(", ").append(wrap(CREATED_DATE_COLUMN)).append(" timestamp not null, ");
            query.append(wrap(UPDATED_DATE_COLUMN)).append(" timestamp not null, ");

            query.append(wrap(GenericDomainObject.STATUS_FIELD_NAME)).append(" ").append(getIdType()).append(", ");
            query.append(wrap(DomainObjectDao.STATUS_TYPE_COLUMN)).append(" integer");
        }
    }

    private void appendColumnsQueryPart(StringBuilder query, List<FieldConfig> fieldConfigList,
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

    private void appendAuditLogColumnsQueryPart(StringBuilder query, List<FieldConfig> fieldConfigList,
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

    private void appendUniqueConstraint(StringBuilder query, String constraintName, String fieldsList) {
        query.append("constraint ").append(wrap(constraintName)).append(" unique (").
                append(fieldsList).append(")");
    }

    private String getTimeZoneIdSqlType() {
        return "varchar(50)";
    }

    private String getSqlType(FieldConfig fieldConfig) {
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
            return getIdType();
        }

        if (ReferenceFieldConfig.class.equals(fieldConfig.getClass())) {
            return getIdType();
        }

        if (StringFieldConfig.class.equals(fieldConfig.getClass())) {
            return "varchar(" + ((StringFieldConfig) fieldConfig).getLength() + ")";
        }

        if (TextFieldConfig.class.equals(fieldConfig.getClass())) {
            return getTextType();
        }

        if (PasswordFieldConfig.class.equals(fieldConfig.getClass())) {
            return "varchar(" + ((PasswordFieldConfig) fieldConfig).getLength() + ")";
        }

        if (BooleanFieldConfig.class.equals(fieldConfig.getClass())) {
            return "smallint check (" + getSqlName(fieldConfig) + " in (0, 1))";
        }

        throw new IllegalArgumentException("Invalid field type");
    }

    protected abstract String getIdType();

    protected abstract String getTextType();

}
