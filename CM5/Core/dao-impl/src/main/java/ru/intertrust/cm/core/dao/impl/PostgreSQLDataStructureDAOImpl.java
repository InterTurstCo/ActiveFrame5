package ru.intertrust.cm.core.dao.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import ru.intertrust.cm.core.config.model.DomainObjectConfig;
import ru.intertrust.cm.core.dao.api.DataStructureDAO;

import javax.sql.DataSource;

import static ru.intertrust.cm.core.dao.impl.PostgreSQLQueryHelper.*;

/**
 * Реализация {@link DataStructureDAO} для PostgreSQL
 * @author vmatsukevich
 *         Date: 5/15/13
 *         Time: 4:27 PM
 */
public class PostgreSQLDataStructureDAOImpl implements DataStructureDAO {

    private static final String DOES_TABLE_EXISTS_QUERY = "select count(*) FROM information_schema.tables WHERE table_schema = 'public' and table_name=?";

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


    /**
     * Смотри {@link DataStructureDAO#createSequence(ru.intertrust.cm.core.config.BusinessObjectConfig)}
     */
    @Override
    public void createSequence(DomainObjectConfig config) {
        String createSequenceQuery = generateSequenceQuery(config);

        jdbcTemplate.update(createSequenceQuery);

    }

    /**
     * Смотри {@link DataStructureDAO#createTable(ru.intertrust.cm.core.config.BusinessObjectConfig)}
     */
    @Override
    public void createTable(DomainObjectConfig config) {
        jdbcTemplate.update(generateCreateTableQuery(config));

        String createIndexesQuery = generateCreateIndexesQuery(config);
        if(createIndexesQuery != null) {
            jdbcTemplate.update(createIndexesQuery);
        }

        jdbcTemplate.update("insert into BUSINESS_OBJECT(NAME) values (?)", config.getName());
        Long id = jdbcTemplate.queryForObject("select ID from BUSINESS_OBJECT where NAME = ?", Long.class, config.getName());
        config.setId(id);
    }

    /**
     * Смотри {@link ru.intertrust.cm.core.dao.api.DataStructureDAO#countTables()}
     */
    @Override
    public Integer countTables() {
        return jdbcTemplate.queryForObject(generateCountTablesQuery(), Integer.class);
    }

    /**
     * Смотри {@link ru.intertrust.cm.core.dao.api.DataStructureDAO#createServiceTables()}
     */
    @Override
    public void createServiceTables() {
        jdbcTemplate.update(generateCreateBusinessObjectTableQuery());
//        jdbcTemplate.update(generateCreateAuthenticationInfoTableQuery());
    }

    /**
     * Смотри @see ru.intertrust.cm.core.dao.api.DataStructureDAO#doesTableExists(java.lang.String)
     */
    @Override
    public boolean doesTableExists(String tableName) {
        tableName = tableName.toLowerCase();
        int total = jdbcTemplate.queryForObject(DOES_TABLE_EXISTS_QUERY, Integer.class, tableName);
        return total > 0;
    }
}
