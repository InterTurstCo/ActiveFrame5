package ru.intertrust.cm.core.dao.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import ru.intertrust.cm.core.config.BusinessObjectConfig;

import javax.sql.DataSource;

import static ru.intertrust.cm.core.dao.impl.PostgreSQLQueryHelper.*;

/**
 * @author vmatsukevich
 *         Date: 5/15/13
 *         Time: 4:27 PM
 */
public class PostgreSQLDataStructureDAOImpl extends AbstractDataStructureDAOImpl {

    private static final String DOES_TABLE_EXISTS_QUERY = "select count(*) FROM information_schema.tables WHERE table_schema = 'public' and table_name=?";    

    private JdbcTemplate jdbcTemplate;

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void createTable(BusinessObjectConfig config, boolean isBusinessObject) {
        jdbcTemplate.update(generateCreateTableQuery(config, isBusinessObject));

        String createIndexesQuery = generateCreateIndexesQuery(config);
        if(createIndexesQuery != null) {
            jdbcTemplate.update(createIndexesQuery);
        }

        jdbcTemplate.update("insert into BUSINESS_OBJECT(NAME) values (?)", config.getName());
        Long id = jdbcTemplate.queryForObject("select ID from BUSINESS_OBJECT where NAME = ?", Long.class, config.getName());
        config.setId(id);
    }

    @Override
    public Integer countTables() {
        return jdbcTemplate.queryForObject(generateCountTablesQuery(), Integer.class);
    }

    @Override
    public void createServiceTables() {
        jdbcTemplate.update(generateCreateBusinessObjectTableQuery());
    }
    
    @Override
    public boolean doesTableExists(String tableName) {
        tableName = tableName.toLowerCase();
        int total = jdbcTemplate.queryForObject(DOES_TABLE_EXISTS_QUERY, Integer.class, tableName);
        return total > 0;
    }
}
