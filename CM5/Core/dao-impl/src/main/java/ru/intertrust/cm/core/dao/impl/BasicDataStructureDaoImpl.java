package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.dao.api.DataStructureDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdDao;

import java.util.List;

import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getSqlName;

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

    protected BasicQueryHelper queryHelper = createQueryHelper();

    protected abstract BasicQueryHelper createQueryHelper();

    /**
     * Смотри {@link ru.intertrust.cm.core.dao.api.DataStructureDao#createSequence(ru.intertrust.cm.core.config.DomainObjectTypeConfig)}
     */
    @Override
    public void createSequence(DomainObjectTypeConfig config) {
        if (config.getExtendsAttribute() != null) {
            return; // Для таблиц дочерхних доменных объектов индекс не создается - используется индекс родителя
        }

        String createSequenceQuery = queryHelper.generateSequenceQuery(config);
        jdbcTemplate.update(createSequenceQuery);
    }

    @Override
    public void createAuditSequence(DomainObjectTypeConfig config) {
        if (config.getExtendsAttribute() != null) {
            return; // Для таблиц дочерхних доменных объектов индекс не создается - используется индекс родителя
        }

        String createAuditSequenceQuery = queryHelper.generateAuditSequenceQuery(config);
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
        jdbcTemplate.update(queryHelper.generateCreateTableQuery(config));

        createAutoIndices(config);
        createExplicitIndexes(config, config.getIndicesConfig().getIndices());

        Integer id = domainObjectTypeIdDao.insert(config);
        config.setId(new Long(id));
    }

    /**
     * Создание таблицы для хранения информации AuditLog
     */
    @Override
    public void createAuditLogTable(DomainObjectTypeConfig config) {
        if (config.isTemplate()) {
            return;
        }
        jdbcTemplate.update(queryHelper.generateCreateAuditTableQuery(config));

        createAutoIndices(config.getName() + "_log", config.getFieldConfigs());
    }

    /**
     * Смотри {@link ru.intertrust.cm.core.dao.api.DataStructureDao#createAclTables(ru.intertrust.cm.core.config.DomainObjectTypeConfig)} Dot шаблон (с isTemplate = true) не отображается в
     * базе данных
     */
    public void createAclTables(DomainObjectTypeConfig config) {
        if (!config.isTemplate()) {
            jdbcTemplate.update(queryHelper.generateCreateAclTableQuery(config));
            jdbcTemplate.update(queryHelper.generateCreateAclReadTableQuery(config));
        }
    }

    @Override
    public void updateTableStructure(String domainObjectConfigName, List<FieldConfig> fieldConfigList) {
        if (domainObjectConfigName == null || ((fieldConfigList == null || fieldConfigList.isEmpty()))) {
            throw new IllegalArgumentException("Invalid (null or empty) arguments");
        }

        String query = queryHelper.generateAddColumnsQuery(domainObjectConfigName, fieldConfigList);
        jdbcTemplate.update(query);

        createAutoIndices(domainObjectConfigName, fieldConfigList);
    }

    @Override
    public void createIndices(DomainObjectTypeConfig domainObjectTypeConfig, List<IndexConfig> indexConfigsToCreate) {        
        if (domainObjectTypeConfig.getName() == null || ((indexConfigsToCreate == null || indexConfigsToCreate.isEmpty()))) {
            throw new IllegalArgumentException("Invalid (null or empty) arguments");
        }

        queryHelper.skipAutoIndices(domainObjectTypeConfig, indexConfigsToCreate);
        createExplicitIndexes(domainObjectTypeConfig, indexConfigsToCreate);
    }

    @Override    
    public void deleteIndices(DomainObjectTypeConfig domainObjectTypeConfig, List<IndexConfig> indexConfigsToDelete){        
        if (domainObjectTypeConfig.getName() == null || ((indexConfigsToDelete == null || indexConfigsToDelete.isEmpty()))) {
            throw new IllegalArgumentException("Invalid (null or empty) arguments");
        }
        queryHelper.skipAutoIndices(domainObjectTypeConfig, indexConfigsToDelete);

        String deleteIndexesQuery = queryHelper.generateDeleteExplicitIndexesQuery(domainObjectTypeConfig.getName(), indexConfigsToDelete);
        if (deleteIndexesQuery != null) {
            jdbcTemplate.update(deleteIndexesQuery);
        }

    }

    
    @Override
    public void createForeignKeyAndUniqueConstraints(String domainObjectConfigName,
                                                     List<ReferenceFieldConfig> fieldConfigList,
                                                     List<UniqueKeyConfig> uniqueKeyConfigList) {
        if (domainObjectConfigName == null || fieldConfigList == null || uniqueKeyConfigList == null) {
            throw new IllegalArgumentException("Invalid (null or empty) arguments");
        }

        String query = queryHelper.generateCreateForeignKeyAndUniqueConstraintsQuery(domainObjectConfigName, fieldConfigList,
                uniqueKeyConfigList);
        if (query.length() > 0){
            jdbcTemplate.update(query);
        }
    }

    /**
     * Смотри {@link ru.intertrust.cm.core.dao.api.DataStructureDao#countTables()}
     */
    @Override
    public Integer countTables() {
        return jdbcTemplate.queryForObject(generateCountTablesQuery(), Integer.class);
    }

    /**
     * Смотри {@link ru.intertrust.cm.core.dao.api.DataStructureDao#createServiceTables()}
     */
    @Override
    public void createServiceTables() {
        jdbcTemplate.update(queryHelper.generateCreateDomainObjectTypeIdSequenceQuery());
        jdbcTemplate.update(queryHelper.generateCreateDomainObjectTypeIdTableQuery());

        jdbcTemplate.update(queryHelper.generateCreateConfigurationSequenceQuery());
        jdbcTemplate.update(queryHelper.generateCreateConfigurationTableQuery());
    }

    /**
     * Смотри @see ru.intertrust.cm.core.dao.api.DataStructureDao#doesTableExists(java.lang.String)
     */
    @Override
    public boolean doesTableExists(String tableName) {
        int total = jdbcTemplate.queryForObject(generateDoesTableExistQuery(), Integer.class, tableName);
        return total > 0;
    }

    private void createExplicitIndexes(DomainObjectTypeConfig config, List<IndexConfig> indexConfigs) {
        if (indexConfigs == null || indexConfigs.isEmpty()) {
            return;
        }

        queryHelper.skipAutoIndices(config, indexConfigs);

        String tableName = getSqlName(config.getName());
        for (IndexConfig indexConfig : indexConfigs) {
            jdbcTemplate.update(queryHelper.generateComplexIndexQuery(tableName, indexConfig));
        }
    }

    private void createAutoIndices(DomainObjectTypeConfig config) {
        createAutoIndices(config.getName(), config.getFieldConfigs());
    }

    private void createAutoIndices(String configName, List<FieldConfig> fieldConfigs) {
        int indexesNumber = 0;
        if (fieldConfigs == null || fieldConfigs.isEmpty()) {
            return;
        }

        for (FieldConfig fieldConfig : fieldConfigs) {
            if (!(fieldConfig instanceof ReferenceFieldConfig)) {
                continue;
            }
            jdbcTemplate.update(queryHelper.generateCreateIndexQuery(configName, fieldConfig.getName()));
        }
    }

    protected abstract String generateDoesTableExistQuery();

    protected abstract String generateCountTablesQuery();
}
