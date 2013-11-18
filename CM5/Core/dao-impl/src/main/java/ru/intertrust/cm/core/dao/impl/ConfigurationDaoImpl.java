package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import ru.intertrust.cm.core.dao.api.ConfigurationDao;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Реализация {@link ru.intertrust.cm.core.dao.api.ConfigurationDao}
 * @author vmatsukevich
 *         Date: 6/17/13
 *         Time: 3:45 PM
 */
public class ConfigurationDaoImpl implements ConfigurationDao {

    protected static final String SAVE_QUERY =
            "insert into " + CONFIGURATION_TABLE + "(CONTENT, LOADED_DATE) values (:content, :loadedDate)";

    protected static final String READ_LAST_SAVED_CONFIGURATION_QUERY =
            "select CONTENT from " + CONFIGURATION_TABLE +
            " where id = (select max(id) from " + CONFIGURATION_TABLE + ")";

    @Autowired
    private NamedParameterJdbcOperations jdbcTemplate;

    /**
     * Смотри {@link ru.intertrust.cm.core.dao.api.ConfigurationDao#save(String)}
     * @param configuration конфигурация
     */
    @Override
    public void save(String configuration) {
        Map<String, Object> parametersMap = new HashMap();
        parametersMap.put("content", configuration);
        parametersMap.put("loadedDate", new Date());

        jdbcTemplate.update(generateSaveQuery(), parametersMap);
    }

    /**
     * Смотри {@link ru.intertrust.cm.core.dao.api.ConfigurationDao#readLastSavedConfiguration()}
     * @return
     */
    @Override
    public String readLastSavedConfiguration() {
        try {
            return jdbcTemplate.queryForObject(generateReadLastLoadedConfiguration(),
                    Collections.<String, Object>emptyMap(), String.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Создает запрос на сохранение конфигурации в таблице CONFIGURATION
     * @return запрос на сохранение конфигурации в таблице CONFIGURATION
     */
    protected  String generateSaveQuery() {
        return SAVE_QUERY;
    }

    /**
     * Создает запрос на получение последней сохраненной конфигурации
     * @return
     */
    protected String generateReadLastLoadedConfiguration() {
        return READ_LAST_SAVED_CONFIGURATION_QUERY;
    }
}
