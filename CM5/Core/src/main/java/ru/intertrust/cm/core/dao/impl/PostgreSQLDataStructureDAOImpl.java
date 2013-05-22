package ru.intertrust.cm.core.dao.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import ru.intertrust.cm.core.config.BusinessObjectConfig;

import javax.sql.DataSource;

import static ru.intertrust.cm.core.dao.impl.PostgreSQLQueryHelper.generateCountTablesQuery;
import static ru.intertrust.cm.core.dao.impl.PostgreSQLQueryHelper.generateCreateIndexesQuery;
import static ru.intertrust.cm.core.dao.impl.PostgreSQLQueryHelper.generateCreateTableQuery;

/**
 * @author vmatsukevich
 *         Date: 5/15/13
 *         Time: 4:27 PM
 */
public class PostgreSQLDataStructureDAOImpl extends AbstractDataStructureDAOImpl {

    private JdbcTemplate jdbcTemplate;

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void createTable(BusinessObjectConfig config) {
        jdbcTemplate.update(generateCreateTableQuery(config));

        String createIndexesQuery = generateCreateIndexesQuery(config);
        if(createIndexesQuery != null) {
            jdbcTemplate.update(createIndexesQuery);
        }
    }

    @Override
    public Integer countTables() {
        return jdbcTemplate.queryForObject(generateCountTablesQuery(), Integer.class);
    }
}
