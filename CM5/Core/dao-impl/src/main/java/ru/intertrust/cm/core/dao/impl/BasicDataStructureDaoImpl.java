package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import ru.intertrust.cm.core.business.api.dto.ColumnInfo;
import ru.intertrust.cm.core.business.api.dto.ForeignKeyInfo;
import ru.intertrust.cm.core.business.api.dto.IndexInfo;
import ru.intertrust.cm.core.business.api.dto.UniqueKeyInfo;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.dao.api.DataStructureDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdDao;
import ru.intertrust.cm.core.dao.api.MD5Service;
import ru.intertrust.cm.core.dao.impl.utils.ForeignKeysRowMapper;
import ru.intertrust.cm.core.dao.impl.utils.IndexesRowMapper;
import ru.intertrust.cm.core.dao.impl.utils.SchemaTablesRowMapper;
import ru.intertrust.cm.core.dao.impl.utils.UniqueKeysRowMapper;

import java.util.*;

import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.*;

/**
 * Реализация {@link ru.intertrust.cm.core.dao.api.DataStructureDao} для PostgreSQL
 * @author vmatsukevich Date: 5/15/13 Time: 4:27 PM
 */
public abstract class BasicDataStructureDaoImpl implements DataStructureDao {

    @Autowired
    protected JdbcOperations jdbcTemplate;

    @Autowired
    private DomainObjectTypeIdDao domainObjectTypeIdDao;

    @Autowired
    private MD5Service md5Service;

    
    private BasicQueryHelper queryHelper;

    protected abstract BasicQueryHelper createQueryHelper(DomainObjectTypeIdDao domainObjectTypeIdDao, MD5Service md5Service);

    /**
     * Смотри {@link ru.intertrust.cm.core.dao.api.DataStructureDao#createSequence(ru.intertrust.cm.core.config.DomainObjectTypeConfig)}
     */
    @Override
    public void createSequence(DomainObjectTypeConfig config) {
        if (config.getExtendsAttribute() != null) {
            return; // Для таблиц дочерхних доменных объектов индекс не создается - используется индекс родителя
        }

        String createSequenceQuery = getQueryHelper().generateSequenceQuery(config);
        
        jdbcTemplate.update(createSequenceQuery);
    }

    @Override
    public void createAuditSequence(DomainObjectTypeConfig config) {
        if (config.getExtendsAttribute() != null) {
            return; // Для таблиц дочерхних доменных объектов индекс не создается - используется индекс родителя
        }

        String createAuditSequenceQuery = getQueryHelper().generateAuditSequenceQuery(config);
        jdbcTemplate.update(createAuditSequenceQuery);
    }

    /**
     * Смотри {@link ru.intertrust.cm.core.dao.api.DataStructureDao#createTable(ru.intertrust.cm.core.config.DomainObjectTypeConfig, boolean)} Dot шаблон (с
     * isTemplate = true) не отображается в базе данных
     */
    @Override
    public void createTable(DomainObjectTypeConfig config, boolean isParentType) {
        if (config.isTemplate()) {
            return;
        }

        Integer id = domainObjectTypeIdDao.insert(config);
        config.setId(id);       
        
        jdbcTemplate.update(getQueryHelper().generateCreateTableQuery(config, isParentType));

        createTableIndices(config, isParentType);
    }

    @Override
    public void createTableIndices(DomainObjectTypeConfig config, boolean isParentType) {
        createAutoIndices(config, isParentType);
        createExplicitIndexes(config, config.getIndicesConfig().getIndices());
    }

    /**
     * Смотри {@link ru.intertrust.cm.core.dao.api.DataStructureDao#createAclTables(ru.intertrust.cm.core.config.DomainObjectTypeConfig)} Dot шаблон (с isTemplate = true) не отображается в
     * базе данных
     */
    public void createAclTables(DomainObjectTypeConfig config) {
        if (!config.isTemplate()) {
            jdbcTemplate.update(getQueryHelper().generateCreateAclTableQuery(config));
            jdbcTemplate.update(getQueryHelper().generateCreateAclReadTableQuery(config));
            createAclIndexes(config);    
        }
        
    }

    /**
     * Создание индексов для _ACL, _READ таблиц по полям object_id, group_id
     * @param config конфигурация доменного объяекта
     */
    private void createAclIndexes(DomainObjectTypeConfig config) {
        String aclTableName = getSqlName(config) + BasicQueryHelper.ACL_TABLE_SUFFIX;
        String readTableName = getSqlName(config) + BasicQueryHelper.READ_TABLE_SUFFIX;

        jdbcTemplate.update(getQueryHelper().generateCreateAclIndexQuery(config, aclTableName, BasicQueryHelper.OBJECT_ID_FIELD, 1));
        jdbcTemplate.update(getQueryHelper().generateCreateAclIndexQuery(config, aclTableName, BasicQueryHelper.GROUP_ID_FIELD, 2));
        jdbcTemplate.update(getQueryHelper().generateCreateAclIndexQuery(config, readTableName, BasicQueryHelper.OBJECT_ID_FIELD, 1));
        jdbcTemplate.update(getQueryHelper().generateCreateAclIndexQuery(config, readTableName, BasicQueryHelper.GROUP_ID_FIELD, 2));
    }

    @Override
    public void updateTableStructure(DomainObjectTypeConfig config, List<FieldConfig> fieldConfigList, boolean isAl, boolean isParent) {
        if (config == null || ((fieldConfigList == null || fieldConfigList.isEmpty()))) {
            throw new IllegalArgumentException("Invalid (null or empty) arguments");
        }

        String query = getQueryHelper().generateAddColumnsQuery(getName(config.getName(), isAl), fieldConfigList);
        jdbcTemplate.update(query);

        createAutoIndices(config, fieldConfigList, isAl, true, isParent);
    }

    @Override
    public void createIndices(DomainObjectTypeConfig domainObjectTypeConfig, List<IndexConfig> indexConfigsToCreate) {        
        if (domainObjectTypeConfig.getName() == null || ((indexConfigsToCreate == null || indexConfigsToCreate.isEmpty()))) {
            throw new IllegalArgumentException("Invalid (null or empty) arguments");
        }

        getQueryHelper().skipAutoIndices(domainObjectTypeConfig, indexConfigsToCreate);

        createExplicitIndexes(domainObjectTypeConfig, indexConfigsToCreate);
    }

    @Override    
    public void deleteIndices(DomainObjectTypeConfig domainObjectTypeConfig, List<IndexConfig> indexConfigsToDelete){
        if (domainObjectTypeConfig.getName() == null || indexConfigsToDelete == null) {
            throw new IllegalArgumentException("Invalid (null or empty) arguments");
        }else if (indexConfigsToDelete.isEmpty()){
            return;
        }
        getQueryHelper().skipAutoIndices(domainObjectTypeConfig, indexConfigsToDelete);

        List<String> indexNamesToDelete = new ArrayList<>(indexConfigsToDelete.size());

        for (IndexConfig indexConfigToDelete : indexConfigsToDelete) {
            List<String> indexFields = new ArrayList<>();
            List<String> indexExpressions = new ArrayList<>();

            for (BaseIndexExpressionConfig indexExpression : indexConfigToDelete.getIndexFieldConfigs()) {
                if (indexExpression instanceof IndexFieldConfig) {
                    indexFields.add(getSqlName(((IndexFieldConfig) indexExpression).getName()));
                } else if (indexExpression instanceof IndexExpressionConfig) {
                    indexExpressions.add(getSqlName((indexExpression).getValue()));
                }
            }

            // Алгоритм генерации имени изменен. Для поддержки клиентов, у которых уже создан индекс со старым имененем,
            // удаляем индексы по обоим вариантам имен
            String indexName = getQueryHelper().createExplicitIndexName(domainObjectTypeConfig, indexFields, indexExpressions);
            indexNamesToDelete.add(indexName);

            indexName = getQueryHelper().generateExplicitIndexName(domainObjectTypeConfig, indexFields, indexExpressions);
            indexNamesToDelete.add(indexName);
        }

        String deleteIndexesQuery = getQueryHelper().generateDeleteExplicitIndexesQuery(indexNamesToDelete);
        if (deleteIndexesQuery != null) {
            jdbcTemplate.update(deleteIndexesQuery);
        }
    }

    @Override
    public void deleteIndices(DomainObjectTypeConfig domainObjectTypeConfig, Set<String> indicesToDelete){
        if (domainObjectTypeConfig.getName() == null || indicesToDelete == null) {
            throw new IllegalArgumentException("Invalid (null or empty) arguments");
        }else if (indicesToDelete.isEmpty()){
            return;
        }

        String deleteIndexesQuery = getQueryHelper().generateDeleteExplicitIndexesQuery(indicesToDelete);
        if (deleteIndexesQuery != null) {
            jdbcTemplate.update(deleteIndexesQuery);
        }
    }

    @Override
    public void createForeignKeyAndUniqueConstraints(DomainObjectTypeConfig config,
                                                     List<ReferenceFieldConfig> fieldConfigList,
                                                     List<UniqueKeyConfig> uniqueKeyConfigList) {
        if (config == null || fieldConfigList == null || uniqueKeyConfigList == null) {
            throw new IllegalArgumentException("Invalid (null or empty) arguments");
        }

        int index = getNextForeignKeyIndex(config.getName());
        for (ReferenceFieldConfig fieldConfig : fieldConfigList) {
            String query = getQueryHelper().generateCreateForeignKeyConstraintQuery(config, fieldConfig, index);
            if (query != null) {
                index ++;
                jdbcTemplate.update(query);
            }
        }

        createUniqueConstraints(config, uniqueKeyConfigList);
    }

    @Override
    public void createUniqueConstraints(DomainObjectTypeConfig config, List<UniqueKeyConfig> uniqueKeyConfigList) {
        if (config == null|| uniqueKeyConfigList == null) {
            throw new IllegalArgumentException("Invalid (null or empty) arguments");
        }

        int index = countUniqueKeys(config.getName());
        for (UniqueKeyConfig uniqueKeyConfig : uniqueKeyConfigList) {
            String query = getQueryHelper().generateCreateUniqueConstraintQuery(config, uniqueKeyConfig, index);
            if (query != null){
                index ++;
                jdbcTemplate.update(query);
            }
        }
    }

    /**
     * Смотри {@link ru.intertrust.cm.core.dao.api.DataStructureDao#createServiceTables()}
     */
    @Override
    public void createServiceTables() {
        jdbcTemplate.update(getQueryHelper().generateCreateDomainObjectTypeIdSequenceQuery());
        jdbcTemplate.update(getQueryHelper().generateCreateDomainObjectTypeIdTableQuery());

        jdbcTemplate.update(getQueryHelper().generateCreateConfigurationSequenceQuery());
        jdbcTemplate.update(getQueryHelper().generateCreateConfigurationTableQuery());
    }

    /**
     * Смотри {@link ru.intertrust.cm.core.dao.api.DataStructureDao#isTableExist(String)}
     */
    @Override
    public boolean isTableExist(String tableName) {
        int total = jdbcTemplate.queryForObject(getQueryHelper().generateIsTableExistQuery(), Integer.class, tableName);
        return total > 0;
    }

    /**
     * Смотри {@link ru.intertrust.cm.core.dao.api.DataStructureDao#getSchemaTables()}
     */
    @Override
     public Map<String, Map<String, ColumnInfo>> getSchemaTables() {
        return jdbcTemplate.query(getQueryHelper().generateGetSchemaTablesQuery(), new SchemaTablesRowMapper());
    }

    /**
     * Смотри {@link ru.intertrust.cm.core.dao.api.DataStructureDao#getForeignKeys()}
     */
    @Override
    public Map<String, Map<String, ForeignKeyInfo>> getForeignKeys() {
        return jdbcTemplate.query(getQueryHelper().generateGetForeignKeysQuery(), new ForeignKeysRowMapper());
    }

    /**
     * Смотри {@link ru.intertrust.cm.core.dao.api.DataStructureDao#getUniqueKeys()}
     */
    @Override
    public Map<String, Map<String, UniqueKeyInfo>> getUniqueKeys() {
        return jdbcTemplate.query(getQueryHelper().generateGetUniqueKeysQuery(), new UniqueKeysRowMapper());
    }

    /**
     * Смотри {@link ru.intertrust.cm.core.dao.api.DataStructureDao#getIndexes()}
     */
    @Override
    public Map<String, Map<String, IndexInfo>> getIndexes() {
        return jdbcTemplate.query(getQueryHelper().generateGetIndexesQuery(), new IndexesRowMapper());
    }

    @Override
    public Map<String, Map<String, IndexInfo>> getIndexes(DomainObjectTypeConfig config) {
        return jdbcTemplate.query(getQueryHelper().generateGetIndexesByTableQuery(), new Object[] {getSqlName(config)}, new IndexesRowMapper());
    }

    /**
     * Смотри {@link ru.intertrust.cm.core.dao.api.DataStructureDao#setColumnNotNull(ru.intertrust.cm.core.config.DomainObjectTypeConfig, ru.intertrust.cm.core.config.FieldConfig, boolean)}
     */
    @Override
    public void setColumnNotNull(DomainObjectTypeConfig config, FieldConfig fieldConfig, boolean notNull) {
        jdbcTemplate.update(getQueryHelper().generateSetColumnNotNullQuery(config, fieldConfig, notNull));
    }

    /**
     * Смотри {@link ru.intertrust.cm.core.dao.api.DataStructureDao#dropConstraint(ru.intertrust.cm.core.config.DomainObjectTypeConfig, String)}
     */
    @Override
    public void dropConstraint(DomainObjectTypeConfig config, String constraintName) {
        jdbcTemplate.update(getQueryHelper().generateDropConstraintQuery(config, constraintName));
    }

    /**
     * Смотри {@link ru.intertrust.cm.core.dao.api.DataStructureDao#updateColumnType(ru.intertrust.cm.core.config.DomainObjectTypeConfig, ru.intertrust.cm.core.config.FieldConfig)}
     */
    @Override
    public void updateColumnType(DomainObjectTypeConfig config, FieldConfig fieldConfig) {
        jdbcTemplate.update(getQueryHelper().generateUpdateColumnTypeQuery(config, fieldConfig));
    }

    /**
     * Смотри {@link ru.intertrust.cm.core.dao.api.DataStructureDao#deleteColumn(ru.intertrust.cm.core.config.DomainObjectTypeConfig, ru.intertrust.cm.core.config.FieldConfig)}
     */
    @Override
    public void deleteColumn(DomainObjectTypeConfig config, FieldConfig fieldConfig) {
        jdbcTemplate.update(getQueryHelper().generateDeleteColumnQuery(config, fieldConfig.getName()));

        if (fieldConfig instanceof ReferenceFieldConfig) {
            jdbcTemplate.update(getQueryHelper().generateDeleteColumnQuery(config,
                    getReferenceTypeColumnName(fieldConfig.getName())));
        } else if (fieldConfig instanceof DateTimeWithTimeZoneFieldConfig) {
            jdbcTemplate.update(getQueryHelper().generateDeleteColumnQuery(config,
                    getTimeZoneIdColumnName(fieldConfig.getName())));
        }
    }

    /**
     * Смотри {@link ru.intertrust.cm.core.dao.api.DataStructureDao#renameColumn(ru.intertrust.cm.core.config.DomainObjectTypeConfig, String, FieldConfig)}
     */
    @Override
    public void renameColumn(DomainObjectTypeConfig config, String oldName, FieldConfig newFieldConfig) {
        jdbcTemplate.update(getQueryHelper().generateRenameColumnQuery(config, oldName, newFieldConfig.getName()));

        if (newFieldConfig instanceof ReferenceFieldConfig) {
            jdbcTemplate.update(getQueryHelper().generateRenameColumnQuery(config, getReferenceTypeColumnName(oldName),
                    getReferenceTypeColumnName(newFieldConfig.getName())));
        } else if (newFieldConfig instanceof DateTimeWithTimeZoneFieldConfig) {
            jdbcTemplate.update(getQueryHelper().generateRenameColumnQuery(config, getTimeZoneIdColumnName(oldName),
                    getTimeZoneIdColumnName(newFieldConfig.getName())));
        }
    }

    /**
     * Смотри {@link ru.intertrust.cm.core.dao.api.DataStructureDao#deleteTable(ru.intertrust.cm.core.config.DomainObjectTypeConfig)}
     */
    @Override
    public void deleteTable(DomainObjectTypeConfig config) {
        jdbcTemplate.update(getQueryHelper().generateDeleteTableQuery(config));
    }

    /**
     * Смотри {@link ru.intertrust.cm.core.dao.api.DataStructureDao#executeSqlQuery(String)}
     */
    @Override
    public void executeSqlQuery(String sqlQuery) {
        jdbcTemplate.update(sqlQuery);
    }

    /**
     * Смотри {@link ru.intertrust.cm.core.dao.api.DataStructureDao#getSqlType(ru.intertrust.cm.core.config.FieldConfig)}
     */
    @Override
    public String getSqlType(FieldConfig fieldConfig) {
        return getQueryHelper().getSqlType(fieldConfig);
    }

    protected BasicQueryHelper getQueryHelper() {
        if (queryHelper == null) {
            queryHelper = createQueryHelper(domainObjectTypeIdDao, md5Service);
        }

        return queryHelper;
    }

    private int countIndexes(DomainObjectTypeConfig domainObjectTypeConfig, boolean isAl) {
        return jdbcTemplate.queryForObject(generateCountTableIndexes(),
                new Object[]{getSqlName(domainObjectTypeConfig.getName(), isAl)}, Integer.class);
    }

    private int countForeignKeys(String domainObjectConfigName) {
        return jdbcTemplate.queryForObject(generateCountTableForeignKeys(),
                new Object[] {getSqlName(domainObjectConfigName)}, Integer.class);
    }

    private int getNextForeignKeyIndex(String domainObjectConfigName) {
        List<String> keyNames = jdbcTemplate.query(generateSelectTableForeignKeys(),
                new Object[] {getSqlName(domainObjectConfigName)}, new SingleColumnRowMapper<String>());
        if (keyNames == null || keyNames.isEmpty()) {
            return 0;
        }
        int maxIndex = 0;
        for (String keyName : keyNames) {
            int keyIndex = Integer.parseInt(keyName.substring(keyName.lastIndexOf('_') + 1));
            if (keyIndex > maxIndex) {
                maxIndex = keyIndex;
            }
        }
        return maxIndex + 1;
    }

    private int countUniqueKeys(String domainObjectConfigName) {
        return jdbcTemplate.queryForObject(generateCountTableUniqueKeys(),
                new Object[] {getSqlName(domainObjectConfigName)}, Integer.class);
    }

    private void createExplicitIndexes(DomainObjectTypeConfig config, List<IndexConfig> indexConfigs) {
        if (indexConfigs == null || indexConfigs.isEmpty()) {
            return;
        }

        getQueryHelper().skipAutoIndices(config, indexConfigs);

        for (IndexConfig indexConfig : indexConfigs) {
            jdbcTemplate.update(getQueryHelper().generateComplexIndexQuery(config, indexConfig));
        }
    }

    private void createAutoIndices(DomainObjectTypeConfig config, boolean isParentType) {
        createAutoIndices(config, config.getFieldConfigs(), false, false, isParentType);
    }

    private void createAutoIndices(DomainObjectTypeConfig config, List<FieldConfig> fieldConfigs, boolean isAl, boolean update, boolean isParentType) {

        if (fieldConfigs == null) {
            fieldConfigs = Collections.emptyList(); // we shouldn't return here as system fields' indexes should be created
        }

        int index = update ? countIndexes(config, isAl) : 0;

        HashSet<String> fieldsIndexesCreatedFor = new HashSet<>();
        for (FieldConfig fieldConfig : fieldConfigs) {
            final boolean isAccessObjectId = fieldConfig.getName().equalsIgnoreCase(DomainObjectDao.ACCESS_OBJECT_ID);
            final boolean refField = fieldConfig instanceof ReferenceFieldConfig;
            if (!isAccessObjectId && !refField) {
                continue;
            }
            final String fieldName = fieldConfig.getName().toLowerCase();
            if (fieldsIndexesCreatedFor.contains(fieldName)) {
                continue;
            }
            if (isAccessObjectId) {
                ReferenceFieldConfig stubRefField = new ReferenceFieldConfig();
                stubRefField.setName(DomainObjectDao.ACCESS_OBJECT_ID);
                jdbcTemplate.update(getQueryHelper().generateCreateAutoIndexQuery(config, stubRefField, index, isAl));
            } else if (refField) {
                jdbcTemplate.update(getQueryHelper().generateCreateAutoIndexQuery(config, (ReferenceFieldConfig) fieldConfig, index, isAl));
            }
            fieldsIndexesCreatedFor.add(fieldName);
            index++;
        }

        // Создание индексов для системных полей.
        if (isParentType) {
            Map<String, IndexInfo> indexes = getIndexes(config).get(getSqlName(config));
            if (indexes != null) {
                for (IndexInfo indexInfo : indexes.values()) {
                    final List<String> columnNames = indexInfo.getColumnNames();
                    if (columnNames != null && columnNames.size() == 1) {
                        fieldsIndexesCreatedFor.add(columnNames.get(0).toLowerCase());
                    }
                }
            }
            for (FieldConfig fieldConfig : config.getSystemFieldConfigs()) {
                if (fieldConfig instanceof ReferenceFieldConfig) {
                    final String fieldName = fieldConfig.getName().toLowerCase();
                    if (SystemField.id.name().equals(fieldName)) {
                        continue;
                    }
                    if (fieldsIndexesCreatedFor.contains(fieldName)) {
                        continue;
                    }
                    jdbcTemplate.update(getQueryHelper().generateCreateAutoIndexQuery(config, (ReferenceFieldConfig) fieldConfig, index, isAl));

                    index++;
                }
            }

        }
    }

    protected abstract String generateSelectTableIndexes();

    protected abstract String generateCountTableIndexes();

    protected abstract String generateCountTableUniqueKeys();

    protected abstract String generateCountTableForeignKeys();

    protected abstract String generateSelectTableForeignKeys();
}
