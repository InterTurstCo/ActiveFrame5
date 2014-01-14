package ru.intertrust.cm.core.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.dao.api.DataStructureDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdDao;

import java.util.ArrayList;
import java.util.List;

import static ru.intertrust.cm.core.dao.impl.PostgreSqlQueryHelper.*;

/**
 * Реализация {@link ru.intertrust.cm.core.dao.api.DataStructureDao} для PostgreSQL
 * @author vmatsukevich Date: 5/15/13 Time: 4:27 PM
 */
public class PostgreSqlDataStructureDaoImpl implements DataStructureDao {
    private static final Logger logger = LoggerFactory.getLogger(PostgreSqlDataStructureDaoImpl.class);

    protected static final String DOES_TABLE_EXISTS_QUERY =
            "select count(*) FROM information_schema.tables WHERE table_schema = 'public' and table_name = ?";

    @Autowired
    private DomainObjectTypeIdDao domainObjectTypeIdDao;

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private JdbcOperations jdbcTemplate;

    public void setDomainObjectTypeIdDao(DomainObjectTypeIdDao domainObjectTypeIdDao) {
        this.domainObjectTypeIdDao = domainObjectTypeIdDao;
    }

    /**
     * Смотри {@link ru.intertrust.cm.core.dao.api.DataStructureDao#createSequence(ru.intertrust.cm.core.config.DomainObjectTypeConfig)}
     */
    @Override
    public void createSequence(DomainObjectTypeConfig config) {
        if (config.getExtendsAttribute() != null) {
            return; // Для таблиц дочерхних доменных объектов индекс не создается - используется индекс родителя
        }

        String createSequenceQuery = generateSequenceQuery(config);
        jdbcTemplate.update(createSequenceQuery);
    }

    @Override
    public void createAuditSequence(DomainObjectTypeConfig config) {
        if (config.getExtendsAttribute() != null) {
            return; // Для таблиц дочерхних доменных объектов индекс не создается - используется индекс родителя
        }

        String createAuditSequenceQuery = generateAuditSequenceQuery(config);
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
        jdbcTemplate.update(generateCreateTableQuery(config));

        String createIndexesQuery = generateCreateAutoIndexesQuery(config);
        if (createIndexesQuery != null) {
            jdbcTemplate.update(createIndexesQuery);
        }

        String createExplicitIndexesQuery = generateCreateExplicitIndexesQuery(config);
        if (createExplicitIndexesQuery != null) {
            jdbcTemplate.update(createExplicitIndexesQuery);
        }

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
        jdbcTemplate.update(generateCreateAuditTableQuery(config));

        String createIndexesQuery = generateCreateAuditLogIndexesQuery(config);
        if (createIndexesQuery != null) {
            jdbcTemplate.update(createIndexesQuery);
        }
    }

    /**
     * Смотри {@link ru.intertrust.cm.core.dao.api.DataStructureDao#createAclTables(DomainObjectTypeConfig)} Dot шаблон (с isTemplate = true) не отображается в
     * базе данных
     */
    public void createAclTables(DomainObjectTypeConfig config) {
        if (!config.isTemplate()) {
            jdbcTemplate.update(generateCreateAclTableQuery(config));
            jdbcTemplate.update(generateCreateAclReadTableQuery(config));
        }
    }

    @Override
    public void updateTableStructure(String domainObjectConfigName, List<FieldConfig> fieldConfigList) {
        if (domainObjectConfigName == null || ((fieldConfigList == null || fieldConfigList.isEmpty()))) {
            throw new IllegalArgumentException("Invalid (null or empty) arguments");
        }

        String query =
                generateAddColumnsQuery(domainObjectConfigName, fieldConfigList);
        jdbcTemplate.update(query);

        String createIndexesQuery = generateCreateIndexesQuery(domainObjectConfigName, fieldConfigList);
        if (createIndexesQuery != null) {
            jdbcTemplate.update(createIndexesQuery);
        }
    }

    @Override
    public void createIndices(DomainObjectTypeConfig domainObjectTypeConfig, List<IndexConfig> indexConfigsToCreate) {        
        if (domainObjectTypeConfig.getName() == null || ((indexConfigsToCreate == null || indexConfigsToCreate.isEmpty()))) {
            throw new IllegalArgumentException("Invalid (null or empty) arguments");
        }

        skipAutoIndices(domainObjectTypeConfig, indexConfigsToCreate);

        String createIndexesQuery = generateCreateExplicitIndexesQuery(domainObjectTypeConfig.getName(), indexConfigsToCreate);
        if (createIndexesQuery != null) {
            jdbcTemplate.update(createIndexesQuery);
        }
    }

    @Override    
    public void deleteIndices(DomainObjectTypeConfig domainObjectTypeConfig, List<IndexConfig> indexConfigsToDelete){        
        if (domainObjectTypeConfig.getName() == null || ((indexConfigsToDelete == null || indexConfigsToDelete.isEmpty()))) {
            throw new IllegalArgumentException("Invalid (null or empty) arguments");
        }
        skipAutoIndices(domainObjectTypeConfig, indexConfigsToDelete);

        String deleteIndexesQuery = generateDeleteExplicitIndexesQuery(domainObjectTypeConfig.getName(), indexConfigsToDelete);
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

        String query = generateCreateForeignKeyAndUniqueConstraintsQuery(domainObjectConfigName, fieldConfigList,
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
        jdbcTemplate.update(generateCreateDomainObjectTypeIdSequenceQuery());
        jdbcTemplate.update(generateCreateDomainObjectTypeIdTableQuery());
        jdbcTemplate.update(generateCreateConfigurationTableQuery());
    }

    /**
     * Смотри @see ru.intertrust.cm.core.dao.api.DataStructureDao#doesTableExists(java.lang.String)
     */
    @Override
    public boolean doesTableExists(String tableName) {
        int total = jdbcTemplate.queryForObject(DOES_TABLE_EXISTS_QUERY, Integer.class, tableName);
        return total > 0;
    }
}
