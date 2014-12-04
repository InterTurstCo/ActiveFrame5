package ru.intertrust.cm.core.dao.impl;

import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getName;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getSqlName;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.ResultSetExtractor;

import ru.intertrust.cm.core.config.BaseIndexExpressionConfig;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.IndexConfig;
import ru.intertrust.cm.core.config.IndexExpressionConfig;
import ru.intertrust.cm.core.config.IndexFieldConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.SystemField;
import ru.intertrust.cm.core.config.UniqueKeyConfig;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.dao.api.DataStructureDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdDao;
import ru.intertrust.cm.core.dao.api.MD5Service;

/**
 * Реализация {@link ru.intertrust.cm.core.dao.api.DataStructureDao} для PostgreSQL
 * @author vmatsukevich Date: 5/15/13 Time: 4:27 PM
 */
public abstract class BasicDataStructureDaoImpl implements DataStructureDao {
    @Autowired
    private DomainObjectTypeIdDao domainObjectTypeIdDao;

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private JdbcOperations jdbcTemplate;

    @Autowired
    private MD5Service md5Service;

    
    private BasicQueryHelper queryHelper;

    protected abstract BasicQueryHelper createQueryHelper(DomainObjectTypeIdDao domainObjectTypeIdDao, ConfigurationExplorer configurationExplorer,
            MD5Service md5Service);

    /**
     * Смотри {@link ru.intertrust.cm.core.dao.api.DataStructureDao#createSequence(ru.intertrust.cm.core.config.DomainObjectTypeConfig)}
     */
    @Override
    public void createSequence(DomainObjectTypeConfig config) {
        if (config.getExtendsAttribute() != null) {
            return; // Для таблиц дочерхних доменных объектов индекс не создается - используется индекс родителя
        }

        String createSequenceQuery = getQueryHelper().generateSequenceQuery(config);;
        
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
     * Смотри {@link ru.intertrust.cm.core.dao.api.DataStructureDao#createTable(ru.intertrust.cm.core.config.DomainObjectTypeConfig)} Dot шаблон (с
     * isTemplate = true) не отображается в базе данных
     */
    @Override
    public void createTable(DomainObjectTypeConfig config) {
        if (config.isTemplate()) {
            return;
        }

        Integer id = domainObjectTypeIdDao.insert(config);
        config.setId(id);       
        
        jdbcTemplate.update(getQueryHelper().generateCreateTableQuery(config));

        createAutoIndices(config);
        createExplicitIndexes(config, config.getIndicesConfig().getIndices());
    }

    /**
     * Создание таблицы для хранения информации AuditLog
     */
    @Override
    public void createAuditLogTable(DomainObjectTypeConfig config) {
        if (config.isTemplate()) {
            return;
        }
        jdbcTemplate.update(getQueryHelper().generateCreateAuditTableQuery(config));

        createAutoIndices(config, config.getFieldConfigs(), true, false);
    }

    /**
     * Смотри {@link ru.intertrust.cm.core.dao.api.DataStructureDao#createAclTables(ru.intertrust.cm.core.config.DomainObjectTypeConfig)} Dot шаблон (с isTemplate = true) не отображается в
     * базе данных
     */
    public void createAclTables(DomainObjectTypeConfig config) {
        if (!config.isTemplate()) {
            jdbcTemplate.update(getQueryHelper().generateCreateAclTableQuery(config));
            jdbcTemplate.update(getQueryHelper().generateCreateAclReadTableQuery(config));
        }
    }

    @Override
    public void updateTableStructure(DomainObjectTypeConfig config, List<FieldConfig> fieldConfigList) {
        if (config == null || ((fieldConfigList == null || fieldConfigList.isEmpty()))) {
            throw new IllegalArgumentException("Invalid (null or empty) arguments");
        }

        String query = getQueryHelper().generateAddColumnsQuery(getName(config.getName(), false), fieldConfigList);
        jdbcTemplate.update(query);

        createAutoIndices(config, fieldConfigList, false, true);
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
        if (domainObjectTypeConfig.getName() == null || ((indexConfigsToDelete == null || indexConfigsToDelete.isEmpty()))) {
            throw new IllegalArgumentException("Invalid (null or empty) arguments");
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
                    indexExpressions.add(getSqlName(((IndexExpressionConfig) indexExpression).getValue()));
                }
            }
            
            String indexName = getQueryHelper().createExplicitIndexName(domainObjectTypeConfig, indexFields, indexExpressions);
            indexNamesToDelete.add(indexName);
        }
        
        String deleteIndexesQuery = getQueryHelper().generateDeleteExplicitIndexesQuery(indexNamesToDelete);
        if (deleteIndexesQuery != null) {
            jdbcTemplate.update(deleteIndexesQuery);
        }
    }

    private String createIndexFieldsExpression(List<String> indexFields, List<String> indexExpressions) {
        StringBuilder result = new StringBuilder();
        result.append("(").append(BasicQueryHelper.createIndexFieldsPart(indexFields, indexExpressions)).append(")");
        return result.toString();
    }

    @Override
    public void createForeignKeyAndUniqueConstraints(DomainObjectTypeConfig config,
                                                     List<ReferenceFieldConfig> fieldConfigList,
                                                     List<UniqueKeyConfig> uniqueKeyConfigList) {
        if (config == null || fieldConfigList == null || uniqueKeyConfigList == null) {
            throw new IllegalArgumentException("Invalid (null or empty) arguments");
        }

        int index = countForeignKeys(config.getName());
        for (ReferenceFieldConfig fieldConfig : fieldConfigList) {
            String query = getQueryHelper().generateCreateForeignKeyConstraintQuery(config, fieldConfig, index);
            if (query != null) {
                index ++;
                jdbcTemplate.update(query);
            }
        }

        index = countUniqueKeys(config.getName());
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
     * Смотри @see ru.intertrust.cm.core.dao.api.DataStructureDao#doesTableExists(java.lang.String)
     */
    @Override
    public boolean doesTableExists(String tableName) {
        int total = jdbcTemplate.queryForObject(generateDoesTableExistQuery(), Integer.class, tableName);
        return total > 0;
    }

    protected BasicQueryHelper getQueryHelper() {
        if (queryHelper == null) {
            queryHelper = createQueryHelper(domainObjectTypeIdDao, configurationExplorer, md5Service);
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

    private void createAutoIndices(DomainObjectTypeConfig config) {
        createAutoIndices(config, config.getFieldConfigs(), false, false);
    }

    private void createAutoIndices(DomainObjectTypeConfig config, List<FieldConfig> fieldConfigs, boolean isAl, boolean update) {

        if (fieldConfigs == null || fieldConfigs.isEmpty()) {
            return;
        }

        int index = update ? countIndexes(config, isAl) : 0;

        for (FieldConfig fieldConfig : fieldConfigs) {
            if (!(fieldConfig instanceof ReferenceFieldConfig)) {
                continue;
            }
            jdbcTemplate.update(getQueryHelper().generateCreateAutoIndexQuery(config, (ReferenceFieldConfig)fieldConfig, index, isAl));
            index++;
        }

        // Создание индексов для системных полей.
        if (hasSystemFields(config)) {
            for (FieldConfig fieldConfig : config.getSystemFieldConfigs()) {
                if (fieldConfig instanceof ReferenceFieldConfig) {
                    if (SystemField.id.name().equals(fieldConfig.getName())) {
                        continue;
                    }
                    jdbcTemplate.update(getQueryHelper().generateCreateAutoIndexQuery(config, (ReferenceFieldConfig) fieldConfig, index, isAl));
                    index++;
                }
            }
        }
    }

    private boolean hasSystemFields(DomainObjectTypeConfig config) {
        // Для аудит объектов наличие системных полей определяется по соотв. родительским ДО. Если родительский ДО
        // является самым верхним в иерархии - то системные колонки содержатся в аудит лог объекте и индексы для них создаются.
        String relevantType = getRelevantType(config.getName());
        config = configurationExplorer.getConfig(DomainObjectTypeConfig.class, relevantType);
        return config.getExtendsAttribute() == null
                && (!config.isTemplate());
    }

    private String getRelevantType(String typeName) {
        if (configurationExplorer.isAuditLogType(typeName)) {
            typeName = typeName.replace(Configuration.AUDIT_LOG_SUFFIX, "");
        }
        return typeName;
    }

    protected abstract String generateDoesTableExistQuery();

    protected abstract String generateSelectTableIndexes();

    protected abstract String generateCountTableIndexes();

    protected abstract String generateCountTableUniqueKeys();

    protected abstract String generateCountTableForeignKeys();
}
