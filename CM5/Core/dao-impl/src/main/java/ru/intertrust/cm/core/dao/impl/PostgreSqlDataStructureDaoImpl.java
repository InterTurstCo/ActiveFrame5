package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.intertrust.cm.core.business.api.dto.DomainObjectTypeId;
import ru.intertrust.cm.core.config.model.DomainObjectParentConfig;
import ru.intertrust.cm.core.config.model.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.model.FieldConfig;
import ru.intertrust.cm.core.config.model.UniqueKeyConfig;
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
        String createSequenceQuery = generateSequenceQuery(config);

        jdbcTemplate.update(createSequenceQuery);

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
        DomainObjectTypeIdCache.getInstance().add(new DomainObjectTypeId(config.getName(), id));
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
    public void updateTableStructure(String domainObjectConfigName, List<FieldConfig> fieldConfigList,
                                     List<UniqueKeyConfig> uniqueKeyConfigList, DomainObjectParentConfig parentConfig) {
        if(domainObjectConfigName == null || ((fieldConfigList == null || fieldConfigList.isEmpty()) &&
                (uniqueKeyConfigList == null || uniqueKeyConfigList.isEmpty()))) {
            throw new IllegalArgumentException("Invalid (null or empty) arguments");
        }

        String query =
                generateUpdateTableQuery(domainObjectConfigName, fieldConfigList, uniqueKeyConfigList, parentConfig);
        jdbcTemplate.update(query);

        String createIndexesQuery = generateCreateIndexesQuery(domainObjectConfigName, fieldConfigList, parentConfig);
        if(createIndexesQuery != null) {
            jdbcTemplate.update(createIndexesQuery);
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
