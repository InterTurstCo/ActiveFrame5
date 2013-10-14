package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.model.*;
import ru.intertrust.cm.core.dao.api.DataStructureDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdDao;

import javax.sql.DataSource;

import java.util.List;

import static ru.intertrust.cm.core.dao.impl.PostgreSqlQueryHelper.*;

/**
 * Реализация {@link ru.intertrust.cm.core.dao.api.DataStructureDao} для PostgreSQL
 * @author vmatsukevich
 *         Date: 5/15/13
 *         Time: 4:27 PM
 */
public class PostgreSqlDataStructureDaoImpl implements DataStructureDao {

    protected static final String DOES_TABLE_EXISTS_QUERY =
            "select count(*) FROM information_schema.tables WHERE table_schema = 'public' and table_name = ?";

    @Autowired
    private DomainObjectTypeIdDao domainObjectTypeIdDao;

    @Autowired
    private ConfigurationExplorer configurationExplorer;    
    
    private JdbcTemplate jdbcTemplate;

    /**
     * Устанавливает {@link #jdbcTemplate}
     * @param dataSource DataSource для инициализации {@link #jdbcTemplate}
     */
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Устанавливает {@link #jdbcTemplate}. Необходим для тестов.
     * @param jdbcTemplate {@link #jdbcTemplate}
     */
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void setDomainObjectTypeIdDao(DomainObjectTypeIdDao domainObjectTypeIdDao) {
        this.domainObjectTypeIdDao = domainObjectTypeIdDao;
    }

    /**
     * Смотри {@link ru.intertrust.cm.core.dao.api.DataStructureDao#createSequence(ru.intertrust.cm.core.config.model.DomainObjectTypeConfig)}
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
     * Смотри {@link ru.intertrust.cm.core.dao.api.DataStructureDao#createTable(ru.intertrust.cm.core.config.model.DomainObjectTypeConfig)}
     * Dot шаблон (с isTemplate = true) не отображается в базе данных
     */
    @Override
    public void createTable(DomainObjectTypeConfig config) {
        if (config.isTemplate()) {
            return;
        }
        jdbcTemplate.update(generateCreateTableQuery(config));

        String createIndexesQuery = generateCreateIndexesQuery(config);
        if(createIndexesQuery != null) {
            jdbcTemplate.update(createIndexesQuery);
        }

        Integer id = domainObjectTypeIdDao.insert(config.getName());
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
        if(createIndexesQuery != null) {
            jdbcTemplate.update(createIndexesQuery);
        }
    }
    

    /**
     * Смотри {@link ru.intertrust.cm.core.dao.api.DataStructureDao#createAclTables(DomainObjectTypeConfig)}
     * Dot шаблон (с isTemplate = true) не отображается в базе данных
     */
    public void createAclTables(DomainObjectTypeConfig config) {
        if (!config.isTemplate()) {
            jdbcTemplate.update(generateCreateAclTableQuery(config));
            jdbcTemplate.update(generateCreateAclReadTableQuery(config));
        }
    }

    @Override
    public void updateTableStructure(String domainObjectConfigName, List<FieldConfig> fieldConfigList) {
        if(domainObjectConfigName == null || ((fieldConfigList == null || fieldConfigList.isEmpty()))) {
            throw new IllegalArgumentException("Invalid (null or empty) arguments");
        }

        String query =
                generateAddColumnsQuery(domainObjectConfigName, fieldConfigList);
        jdbcTemplate.update(query);

        String createIndexesQuery = generateCreateIndexesQuery(domainObjectConfigName, fieldConfigList);
        if(createIndexesQuery != null) {
            jdbcTemplate.update(createIndexesQuery);
        }
    }

    @Override
    public void createForeignKeyAndUniqueConstraints(String domainObjectConfigName,
                                                     List<ReferenceFieldConfig> fieldConfigList,
                                                     List<UniqueKeyConfig> uniqueKeyConfigList) {
        if(domainObjectConfigName == null || ((fieldConfigList == null || fieldConfigList.isEmpty()) &&
                (uniqueKeyConfigList == null || uniqueKeyConfigList.isEmpty()))) {
            throw new IllegalArgumentException("Invalid (null or empty) arguments");
        }

        String query = generateCreateForeignKeyAndUniqueConstraintsQuery(domainObjectConfigName, fieldConfigList,
                uniqueKeyConfigList);
        jdbcTemplate.update(query);
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
        jdbcTemplate.update(generateCreateDomainObjectTableQuery());
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
