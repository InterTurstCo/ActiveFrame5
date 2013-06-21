package ru.intertrust.cm.core.dao.impl;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import ru.intertrust.cm.core.dao.api.ConfigurationDAO;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author vmatsukevich
 *         Date: 6/17/13
 *         Time: 3:45 PM
 */
public class ConfigurationDAOImpl implements ConfigurationDAO {

    private NamedParameterJdbcTemplate jdbcTemplate;

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public void save(String configuration) {
        String query = "insert into " + PostgreSQLQueryHelper.CONFIGURATION_TABLE +
                "(CONTENT, LOADED_DATE) values (:content, :loadedDate)";

        Map<String, Object> parametersMap = new HashMap();
        parametersMap.put("content", configuration);
        parametersMap.put("loadedDate", new Date());

        jdbcTemplate.update(query, parametersMap);
    }

    @Override
    public String readLastLoadedConfiguration() {
        String query = "select CONTENT from " + PostgreSQLQueryHelper.CONFIGURATION_TABLE + " where id = " +
                "(select max(id) from " + PostgreSQLQueryHelper.CONFIGURATION_TABLE + ")";
        try {
            return jdbcTemplate.queryForObject(query, Collections.<String, Object>emptyMap(), String.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
