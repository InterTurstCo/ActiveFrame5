package ru.intertrust.cm.core.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.intertrust.cm.core.business.api.dto.Case;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdDao;
import ru.intertrust.cm.core.dao.api.InitializationLockDao;
import ru.intertrust.cm.core.dao.api.MD5Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import static ru.intertrust.cm.core.dao.api.ConfigurationDao.*;
import static ru.intertrust.cm.core.dao.api.DataStructureDao.AUTHENTICATION_INFO_TABLE;
import static ru.intertrust.cm.core.dao.api.DataStructureDao.USER_UID_COLUMN;
import static ru.intertrust.cm.core.dao.api.DomainObjectDao.*;
import static ru.intertrust.cm.core.dao.api.DomainObjectDao.ID_COLUMN;
import static ru.intertrust.cm.core.dao.api.DomainObjectTypeIdDao.DOMAIN_OBJECT_TYPE_ID_TABLE;
import static ru.intertrust.cm.core.dao.api.DomainObjectTypeIdDao.NAME_COLUMN;
import static ru.intertrust.cm.core.dao.api.InitializationLockDao.*;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.*;
import static ru.intertrust.cm.core.dao.impl.utils.DaoUtils.unwrap;
import static ru.intertrust.cm.core.dao.impl.utils.DaoUtils.wrap;

/**
 * Класс для генерации sql запросов для {@link ru.intertrust.cm.core.dao.impl.PostgreSqlDataStructureDaoImpl}
 * @author vmatsukevich Date: 5/20/13 Time: 2:12 PM
 */
public abstract class BasicQueryHelper {

    private static final Logger logger = LoggerFactory.getLogger(BasicQueryHelper.class);
    
    public static final String ACL_TABLE_SUFFIX = "_acl";

    public static final String READ_TABLE_SUFFIX = "_read";

    public static final String OBJECT_ID_FIELD = "object_id";

    public static final String GROUP_ID_FIELD = "group_id";
    
    private static final String GROUP_TABLE = "user_group";

    private DomainObjectTypeIdDao domainObjectTypeIdDao;

    private MD5Service md5Service;
    
    protected BasicQueryHelper(DomainObjectTypeIdDao domainObjectTypeIdDao, MD5Service md5Service) {
        this.domainObjectTypeIdDao = domainObjectTypeIdDao;
        this.md5Service = md5Service;
    }

    /**
     * Генерирует запрос, возвращающий кол-во таблиц в базе данных
     * @return запрос, возвращающий кол-во таблиц в базе данных
     */
    public abstract String generateCountTablesQuery();

    public abstract String generateGetForeignKeysQuery();

    public abstract String generateGetUniqueKeysQuery();

    public abstract String generateGetIndexesQuery();

    public abstract String generateGetIndexesByTableQuery();

    public abstract String generateSetColumnNotNullQuery(DomainObjectTypeConfig config, FieldConfig fieldConfig, boolean notNull);

    public abstract List<String> generateUpdateColumnTypeQueries(DomainObjectTypeConfig config, FieldConfig fieldConfig);

    public abstract String generateGatherStatisticsQuery();
    
    /**
     * Возвращает выражение индекса в sql-виде c учетом регистра фрагмента в
     * кавычках или апострофах
     * 
     * @param indexFieldConfig
     *            конфигурация индексного поля
     * @return выражение
     */
    public abstract String getSqlIndexExpression(BaseIndexExpressionConfig indexFieldConfig);

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
                "constraint " + wrap("pk_" + AUTHENTICATION_INFO_TABLE) + " " +
                    "primary key (" + wrap(ID_COLUMN) + "), " +
                "constraint " + wrap("u_" + AUTHENTICATION_INFO_TABLE + "_" + USER_UID_COLUMN) + " " +
                    "unique" + "(" + wrap(USER_UID_COLUMN) + "))";
    }

    private String createAclTableQueryFor(DomainObjectTypeConfig config) {
        return "create table " + wrap(getSqlName(config) + "_acl") + " (" +
                wrap("object_id") + " " + getIdType() + " not null, " +
                wrap("group_id") + " " + getIdType() + " not null, " +
                wrap("operation") + " varchar(256) not null, " +
                "constraint " + wrap("pk_" + getDOTypeConfigId(config) + "_acl") +
                    " primary key (" + wrap("object_id") + ", " + wrap("group_id") + ", " +
                    wrap(OPERATION_COLUMN) + ")";
    }

    private String createAclReadTableQueryFor(DomainObjectTypeConfig config) {
        return "create table " + wrap(getSqlName(config) + "_read") + " (" +
                wrap("object_id") + " " + getIdType() + " not null, " +
                wrap("group_id") + " " + getIdType() + " not null, " +
                "constraint " + wrap("pk_" + getDOTypeConfigId(config) + "_read") + " primary key (" + wrap("object_id") +
                ", " + wrap("group_id") + ")";
    }

    private void appendAclFKConstraintForDO(DomainObjectTypeConfig targetConfig, StringBuilder query, boolean read) {
        query.append(", ").append("CONSTRAINT ").
                append(wrap("fk_" +getDOTypeConfigId(targetConfig) + (read ? "_read" : "_acl") + "_0")).
                append(" FOREIGN KEY (").append(wrap("object_id")).append(") REFERENCES ").
                append(wrap(getSqlName(targetConfig))).append(" (").append(wrap(ID_COLUMN)).append(")");
}

    private void appendFKConstraintForGroup(DomainObjectTypeConfig config, StringBuilder query, boolean read) {
        query.append(", ").append("CONSTRAINT ").append(wrap("fk_" + getDOTypeConfigId(config) + (read ? "_read" : "_acl") + "_1")).
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
        String sequenceName = getSqlSequenceName(getDOTypeConfigId(config).toString());
        StringBuilder query = new StringBuilder();
        query.append("create sequence ").append(wrap(sequenceName));

        return query.toString();
    }

    /**
     * Генерирует запрос, создающий последовательность(сиквенс) по конфигурации
     * доменного объекта
     * @param config конфигурация доменного объекта
     * @return запрос, создающий последовательность(сиквенс) по конфигурации
     *         доменного объекта
     */
    public String generateAuditSequenceQuery(DomainObjectTypeConfig config) {
        String sequenceName = getSqlAuditSequenceName(getDOTypeConfigId(config));
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
    public String generateCreateTableQuery(DomainObjectTypeConfig config, boolean isParentType) {
        String tableName = getSqlName(config);
        StringBuilder query = new StringBuilder("create table ").append(wrap(tableName)).append(" ( ");        
        
        appendSystemColumnsQueryPart(config, query, isParentType);

        if (config.getFieldConfigs().size() > 0) {
            query.append(", ");
            appendColumnsQueryPart(query, config.getFieldConfigs(), false);
        }

        appendPKConstraintQueryPart(query, tableName, getDOTypeConfigId(config), false);

        // Необходимо создать уникальный ключ (ID, TYPE_ID), чтобы обеспечить
        // возможность создания внешнего ключа,
        // ссылающегося на эти колонки
        appendIdTypeUniqueConstraint(query, tableName, getDOTypeConfigId(config), false);

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
        String tableName = getALTableSqlName(config.getName());
        StringBuilder query = new StringBuilder("create table ").append(wrap(tableName)).append(" (");

        // Системные атрибуты
        query.append(wrap(ID_COLUMN)).append(" ").append(getIdType()).append(" not null, ");
        query.append(wrap(TYPE_COLUMN)).append(" integer not null");
        if (config.getExtendsAttribute() == null) {
            query.append(", ");
            query.append(wrap(DomainObjectDao.OPERATION_COLUMN)).append(" int not null, ");
            query.append(wrap(DomainObjectDao.UPDATED_DATE_COLUMN)).append(" timestamp not null, ");
            query.append(wrap(DomainObjectDao.UPDATED_BY)).append(" ").append(getIdType()).append(", ");
            query.append(wrap(DomainObjectDao.UPDATED_BY_TYPE_COLUMN)).append(" integer , ");            
            
            query.append(wrap(DOMAIN_OBJECT_ID_COLUMN)).append(" ").append(getIdType()).append(" not null, ");
            query.append(wrap(COMPONENT_COLUMN)).append(" varchar(512), ");
            query.append(wrap(IP_ADDRESS_COLUMN)).append(" varchar(16), ");
            query.append(wrap(INFO_COLUMN)).append(" varchar(512)");
        }

        if (config.getFieldConfigs().size() > 0) {
            query.append(", ");
            appendAuditLogColumnsQueryPart(query, config.getFieldConfigs(), false);
        }

        appendPKConstraintQueryPart(query, tableName, getDOTypeConfigId(config), true);

        // Необходимо создать уникальный ключ (ID, TYPE_ID), чтобы обеспечить
        // возможность создания внешнего ключа,
        // ссылающегося на эти колонки
        appendIdTypeUniqueConstraint(query, tableName, getDOTypeConfigId(config), true);

        query.append(", ");
        int index = 0;
        if (config.getExtendsAttribute() != null) {
            appendFKConstraint(query, tableName, getDOTypeConfigId(config), ID_COLUMN, getName(config.getExtendsAttribute(), true), ID_COLUMN, index, true);
            index ++;
            query.append(", ");
        }

        appendFKConstraint(query, tableName, getDOTypeConfigId(config), TYPE_COLUMN, DOMAIN_OBJECT_TYPE_ID_TABLE, ID_COLUMN, index, true);

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
        StringBuilder query = new StringBuilder(createAclTableQueryFor(config));

        appendAclFKConstraintForDO(config, query, false);
        appendFKConstraintForGroup(config, query, false);
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
        StringBuilder query = new StringBuilder(createAclReadTableQueryFor(config));

        appendAclFKConstraintForDO(config, query, true);
        appendFKConstraintForGroup(config, query, true);
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
     * Генерирует запрос для создания форен-ки констрэйнтов
     * @param config
     *            конфигурация доменного объекта, таблицу которого необходимо
     *            обновить
     * @param fieldConfig
     *            колонка для создания форен-ки констрэйнтов
     * @return запрос для обновления структуры таблицы (добавления форен-ки констрэйнтов)
     */
    public String generateCreateForeignKeyConstraintQuery(DomainObjectTypeConfig config,
            ReferenceFieldConfig fieldConfig, int index) {
        String tableName = getSqlName(config.getName());

        if (ConfigurationExplorer.REFERENCE_TYPE_ANY.equals(fieldConfig.getType())) {
            return null;
        }
        StringBuilder query = new StringBuilder("alter table ");
        query.append(wrap(tableName)).append(" add ");

        String columnName = getSqlName(fieldConfig);
        String typeReferenceColumnName = getReferenceTypeColumnName(fieldConfig.getName());
        String referencedTableName = getSqlName(fieldConfig.getType());
        appendFKConstraint(query, tableName, getDOTypeConfigId(config),
                new String[]{columnName, typeReferenceColumnName},
                referencedTableName, new String[]{ID_COLUMN, TYPE_COLUMN}, index, false);

        return query.toString();
    }

    /**
     * Генерирует запрос для создания уникальных констрэйнтов
     * @param config
     *            конфигурация доменного объекта, таблицу которого необходимо
     *            обновить
     * @param uniqueKeyConfig
     *            уникальный ключ
     * @return запрос для обновления структуры таблицы (добавления уникальных констрэйнтов)
     */
    public String generateCreateUniqueConstraintQuery(DomainObjectTypeConfig config,
                                                              UniqueKeyConfig uniqueKeyConfig, int index) {
        String tableName = getSqlName(config);
        StringBuilder query = new StringBuilder("alter table ").append(wrap(tableName)).append(" ");

        if (uniqueKeyConfig.getUniqueKeyFieldConfigs().isEmpty()) {
            return null;
        }

        List<String> uniqueKeyFields = getUniqueKeyFields(config, uniqueKeyConfig);


        DelimitedListFormatter<String> listFormatter =
                new DelimitedListFormatter<String>() {
                    @Override
                    protected String format(String item) {
                        return getSqlName(item);
                    }
                };

        String constraintName = "u_" + getDOTypeConfigId(config) + "_" + index;
        String fieldsList = listFormatter.formatAsDelimitedList(uniqueKeyFields, ", ", "\"");

        query.append("add ");
        appendUniqueConstraint(query, constraintName, fieldsList);

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
        for (BaseIndexExpressionConfig indexExpression : indexConfig.getIndexFieldConfigs()) {
            
            if (indexExpression instanceof IndexFieldConfig) {
                if (i > 0) {
                    indexFields.append("_");
                }
                indexFields.append(((IndexFieldConfig) indexExpression).getName());
                i++;
            }

        }
        return indexFields.toString();
    }
    
    public String generateDeleteExplicitIndexesQuery(Collection<String> indexNames) {
        StringBuilder query = new StringBuilder();
        for (String indexName : indexNames) {
            appendDeleteIndexQueryPart(query, indexName);
        }

        if (query.length() == 0) {
            return null;
        }

        return query.toString();
    }
    
    public String generateComplexIndexQuery(DomainObjectTypeConfig config, IndexConfig indexConfig) {
        List<String> indexFields = new ArrayList<>();
        List<String> indexExpressions = new ArrayList<>();

        for (BaseIndexExpressionConfig indexExpression : indexConfig.getIndexFieldConfigs()) {
            if (indexExpression instanceof IndexFieldConfig) {
                indexFields.add(getSqlName(indexExpression));
            } else if (indexExpression instanceof IndexExpressionConfig) {
                indexExpressions.add(getSqlIndexExpression(indexExpression));
            }
        }

        String indexType = getIndexType(indexConfig);
        return generateIndexQuery(config, indexType, indexFields, indexExpressions);
    }

    private String getIndexType(IndexConfig indexConfig) {
        String indexType = indexConfig.getType();

        if (indexType == null) {
            return IndexConfig.IndexType.BTREE.toString();
        }
        return indexType;
    }

    public String generateCreateAutoIndexQuery(DomainObjectTypeConfig config, ReferenceFieldConfig fieldConfig, int index) {
        return generateCreateAutoIndexQuery(config, fieldConfig, index, false);
    }

    protected abstract String generateIndexQuery(DomainObjectTypeConfig config, String indexType, List<String> expressionValues, List<String> indexExpressions);

    public String generateCreateAutoIndexQuery(DomainObjectTypeConfig config, ReferenceFieldConfig fieldConfig, int index, boolean isAl) {
        String tableName = getSqlName(config.getName(), isAl);
        StringBuilder columnNames = new StringBuilder();
        columnNames.append(wrap(getSqlName(fieldConfig.getName())));
        
        if (ConfigurationExplorer.REFERENCE_TYPE_ANY.equals(fieldConfig.getType())) {
            String referenceTypeColumn = fieldConfig.getName() + DomainObjectDao.REFERENCE_TYPE_POSTFIX;
            columnNames.append(", ").append(wrap(getSqlName(referenceTypeColumn)));
        }

        String indexName = createAutoIndexName(config, index, isAl);
        return "create index " + wrap(indexName) + " on " + wrap(tableName) + " (" + columnNames.toString() + ")";
    }

    public String generateCreateAclIndexQuery(DomainObjectTypeConfig parentConfig, String tableName, String columnName, int index) {
        tableName = getSqlName(tableName);
        String suffix = null;
        if (tableName.endsWith(ACL_TABLE_SUFFIX)) {
            suffix = ACL_TABLE_SUFFIX;
        } else {
            suffix = READ_TABLE_SUFFIX;
        }
        String indexName = createAclIndexName(parentConfig, index, suffix);
        return "create index " + wrap(indexName) + " on " + wrap(tableName) + " (" + wrap(columnName) + ")";
    }
    
    protected String createAclIndexName(DomainObjectTypeConfig config, int index, String suffix) {
        StringBuilder builder = new StringBuilder();
        builder.append("i_").append(getName(getDOTypeConfigId(config).toString(), false)).append(suffix).append("_").append(index);
        return builder.toString();
    }

    /**
     * Генерирует запрос, создающий таблицу INITIALIZATION_LOCK
     * @return запрос, создающий таблицу INITIALIZATION_LOCK
     */
    public String generateInitializationLockTableQuery() {
        return "CREATE TABLE " + wrap(INITIALIZATION_LOCK_TABLE) +
                " (" + wrap(InitializationLockDao.ID_COLUMN) + " " + getIdType() + ", " +
                wrap(SERVER_ID_COLUMN) + " " + getIdType() + ", " + wrap(START_DATE_COLUMN) + "timestamp, " +
                "constraint " + wrap("pk_" + INITIALIZATION_LOCK_TABLE) +
                " primary key (" + wrap(InitializationLockDao.ID_COLUMN) + "))";
    }

    private void appendDeleteIndexQueryPart(StringBuilder query, String indexName) {
        query.append("drop index if exists ").append(wrap(indexName)).append(";\n");
    }

    protected String createAutoIndexName(DomainObjectTypeConfig config, int index, boolean isAl) {
        return "i_" + getName(getDOTypeConfigId(config).toString(), isAl) + "_" + index;
    }

    /**
     * @deprecated use {@link #generateExplicitIndexName(ru.intertrust.cm.core.config.DomainObjectTypeConfig, java.util.List, java.util.List)}
     * Создает имя явно сконфигурированного индекса. Имя формируется по патерну "i" + код типа ДО + урезанный MD5 хеш
     * от DDL выражения для индекса.
     * @param config конфигурация ДО.
     * @param indexFields порля ДО, образующие индекс.
     * @param indexExpressions выражения, образующие индекс.
     * @return
     */
    protected String createExplicitIndexName(DomainObjectTypeConfig config, List<String> indexFields, List<String> indexExpressions) {
        String indexExpression = createIndexFieldsPart(indexFields, indexExpressions);
        String id_type = getName(getDOTypeConfigId(config).toString(), false);
        String indexMd5 = md5Service.getMD5AsHex(indexExpression);
        indexMd5 = indexMd5.substring(2, indexMd5.length() - id_type.length());
        return "i" + id_type + indexMd5;
    }

    /**
     * Создает имя явно сконфигурированного индекса. Имя формируется по патерну "i" + код типа ДО + урезанный MD5 хеш
     * от DDL выражения для индекса.
     * @param config конфигурация ДО.
     * @param indexFields поля ДО, образующие индекс.
     * @param indexExpressions выражения, образующие индекс.
     * @return
     */
    protected String generateExplicitIndexName(DomainObjectTypeConfig config, List<String> indexFields, List<String> indexExpressions) {
        String indexExpression = createIndexFieldsPart(indexFields, indexExpressions);
        // Для совместимости со старыми именами индексов
        String indexMd5 = md5Service.getMD5As32Base(Case.toLower(indexExpression));
        String id_type = getName(getDOTypeConfigId(config).toString(), false);
        indexMd5 = indexMd5.substring(2, indexMd5.length() - id_type.length());
        return "i" + id_type + indexMd5;
    }

    /**
     * Возвращает часть определения индекса, содержащую список полей (функций над полями), по которым строится индекс.
     * @param indexFields список полей
     * @param indexExpressions список выражений (функций над полями) 
    * @return
     */
    public String createIndexFieldsPart(List<String> indexFields, List<String> indexExpressions) {
        StringBuilder expression = new StringBuilder();


        int index = 0;
        for (String fieldName : indexFields) {
            expression.append(wrap(fieldName));
            if (index < indexFields.size() - 1) {
                expression.append(", ");
            }
            index++;
        }
            
        if (indexExpressions.size() > 0) {
            if (indexFields.size() > 0) {
                expression.append(", ");
            }
            index = 0;
            for (String indexExpr : indexExpressions) {
                expression.append(unwrap(indexExpr));
                if (index < indexExpressions.size() - 1) {
                    expression.append(", ");
                }
                index++;
            }

        }
        return expression.toString();
    }

    public String generateDropConstraintQuery(DomainObjectTypeConfig config, String constraintName) {
        StringBuilder query = new StringBuilder();

        query.append("alter table ").append(wrap(getSqlName(config))).append(" drop constraint ").
                append(wrap(constraintName));

        return query.toString();
    }

    public String generateDeleteColumnQuery(DomainObjectTypeConfig config, String fieldName) {
        StringBuilder query = new StringBuilder();

        query.append("alter table ").append(wrap(getSqlName(config))).append(" drop column ").
                append(wrap(getSqlName(fieldName)));

        return query.toString();
    }

    public String generateRenameColumnQuery(DomainObjectTypeConfig config, String oldName, String newName) {
        StringBuilder query = new StringBuilder();

        query.append("alter table ").append(wrap(getSqlName(config))).append(" rename column ").
                append(wrap(getSqlName(oldName))).append(" to ").append(wrap(getSqlName(newName)));

        return query.toString();
    }

    public String generateDeleteTableQuery(DomainObjectTypeConfig config) {
        StringBuilder query = new StringBuilder();

        query.append("drop table ").append(wrap(getSqlName(config)));

        return query.toString();
    }

    public String generateDeleteTableQuery(String name) {
        StringBuilder query = new StringBuilder();

        query.append("drop table ").append(wrap(getSqlName(name)));

        return query.toString();
    }

    protected abstract String generateIsTableExistQuery();

    protected abstract String generateGetSchemaTablesQuery();

    protected void appendParentFKConstraintsQueryPart(StringBuilder query, String tableName,
            DomainObjectTypeConfig config) {
        query.append(", ");
        int index = 0;

        if (config.getExtendsAttribute() != null) {
            appendFKConstraint(query, tableName, getDOTypeConfigId(config), ID_COLUMN, config.getExtendsAttribute(), ID_COLUMN, index, false);
            index ++;
            query.append(", ");
        }

        appendFKConstraint(query, tableName, getDOTypeConfigId(config), TYPE_COLUMN, DOMAIN_OBJECT_TYPE_ID_TABLE, ID_COLUMN, index, false);
    }

    private void appendFKConstraint(StringBuilder query, String tableName, Integer doTypeId, String columnName,
            String referencedTable, String referencedFieldName, int index, boolean isAl) {
        appendFKConstraint(query, tableName, doTypeId, new String[] { columnName }, referencedTable,
                new String[] { referencedFieldName }, index, isAl);
    }

    private void appendFKConstraint(StringBuilder query, String tableName, Integer doTypeId, String[] columnNames,
            String referencedTable, String[] referencedFieldNames, int index, boolean isAl) {
        DelimitedListFormatter<String> listFormatter = new DelimitedListFormatter<>();

        String constraintName = "fk_" + getName(doTypeId != null ? doTypeId.toString() : tableName, isAl) + "_" + index;

        query.append("constraint ").append(wrap(constraintName)).append(" foreign key (").
                append(listFormatter.formatAsDelimitedList(columnNames, ", ", "\"")).append(")").
                append(" ").append("references ").append(wrap(getSqlName(referencedTable))).
                append(" (").append(listFormatter.formatAsDelimitedList(referencedFieldNames, ", ", "\"")).append(")");
    }

    private void appendPKConstraintQueryPart(StringBuilder query, String tableName, Integer doTypeId, boolean isAl) {
        String pkName = "pk_" + getName(doTypeId != null ? doTypeId.toString() : tableName, isAl);
        query.append(", constraint ").append(wrap(pkName)).append(" primary key (").
                append(wrap(ID_COLUMN)).append(")");
    }

    private void appendIdTypeUniqueConstraint(StringBuilder query, String tableName, Integer doTypeId, boolean isAl) {
        DelimitedListFormatter<String> listFormatter = new DelimitedListFormatter<>();
        String[] keyFields = new String[] { ID_COLUMN, TYPE_COLUMN };

        String constraintName = "u_" + getName(doTypeId != null ? doTypeId.toString() : tableName, isAl) + "_" + 0;
        String fieldsList = listFormatter.formatAsDelimitedList(keyFields, ", ", "\"");

        query.append(", ");
        appendUniqueConstraint(query, constraintName, fieldsList);
    }

    private void appendSystemColumnsQueryPart(DomainObjectTypeConfig config, StringBuilder query, boolean isParent) {
        query.append(wrap(ID_COLUMN)).append(" ").append(getIdType()).append(" not null, ");
        query.append(wrap(TYPE_COLUMN)).append(" integer");
        
        if (isParent) {
            query.append(", ").append(wrap(CREATED_DATE_COLUMN)).append(" timestamp not null, ");
            query.append(wrap(UPDATED_DATE_COLUMN)).append(" timestamp not null, ");

            query.append(wrap(DomainObjectDao.CREATED_BY)).append(" ").append(getIdType()).append(", ");
            query.append(wrap(DomainObjectDao.CREATED_BY_TYPE_COLUMN)).append(" integer, ");

            query.append(wrap(DomainObjectDao.UPDATED_BY)).append(" ").append(getIdType()).append(", ");
            query.append(wrap(DomainObjectDao.UPDATED_BY_TYPE_COLUMN)).append(" integer, ");

            query.append(wrap(GenericDomainObject.STATUS_FIELD_NAME)).append(" ").append(getIdType()).append(", ");
            query.append(wrap(DomainObjectDao.STATUS_TYPE_COLUMN)).append(" integer, ");

            query.append(wrap(SECURITY_STAMP_COLUMN)).append(" ").append(getIdType()).append(", ");
            query.append(wrap(SECURITY_STAMP_TYPE_COLUMN)).append(" ").append(" integer, ");

            query.append(wrap(DomainObjectDao.ACCESS_OBJECT_ID)).append(" ").append(getIdType());
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
                query.append(wrap(getReferenceTypeColumnName(fieldConfig.getName()))).append(" ").
                        append(getReferenceTypeSqlType());
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

    public String generateAddTimeZoneColumnQuery(String domainObjectConfigName, FieldConfig fieldConfig) {
        String tableName = getSqlName(domainObjectConfigName);
        StringBuilder query = new StringBuilder("alter table ").append(wrap(tableName)).append(" ");
        query.append("add column ");
        query.append(wrap(getTimeZoneIdColumnName(fieldConfig.getName()))).append(" ").
        append(getTimeZoneIdSqlType());
        if (fieldConfig.isNotNull()) {
            query.append(" not null");
        }
        query.append(" default '").append(TimeZone.getDefault().getID()).append("'");
        
        return query.toString();
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

    protected String getTimeZoneIdSqlType() {
        return "varchar(50)";
    }

    protected String getReferenceTypeSqlType() {
        return "integer";
    }

    private Integer getDOTypeConfigId(DomainObjectTypeConfig config) {
        if (config.getId() != null) {
            return config.getId();
        } else {
            return domainObjectTypeIdDao.findIdByName(config.getName());
        }
    }

    protected String getSqlType(FieldConfig fieldConfig) {
        if (DateTimeFieldConfig.class.equals(fieldConfig.getClass()) ||
                DateTimeWithTimeZoneFieldConfig.class.equals(fieldConfig.getClass()) ||
                TimelessDateFieldConfig.class.equals(fieldConfig.getClass())) {
            return "timestamp";
        }

        if (DecimalFieldConfig.class.equals(fieldConfig.getClass())) {
            StringBuilder sqlType = new StringBuilder(getDecimalType());
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
            return "smallint " + (fieldConfig.isNotNull() ? "default 0 " : "") + "check (" + wrap(getSqlName(fieldConfig)) + " in (0, 1)) ";
        }

        throw new IllegalArgumentException("Invalid field type");
    }

    protected abstract String getIdType();

    protected abstract String getTextType();

    protected abstract String getDecimalType();
}
