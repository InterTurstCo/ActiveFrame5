package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import ru.intertrust.cm.core.dao.api.ConfigurationDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.IdGenerator;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static ru.intertrust.cm.core.dao.impl.utils.DaoUtils.wrap;

/**
 * Реализация {@link ru.intertrust.cm.core.dao.api.ConfigurationDao}
 * @author vmatsukevich
 *         Date: 6/17/13
 *         Time: 3:45 PM
 */
public class ConfigurationDaoImpl implements ConfigurationDao {

    protected static final String SAVE_QUERY =
            "insert into " + wrap(CONFIGURATION_TABLE) + "(" + wrap(DomainObjectDao.ID_COLUMN) + ", " +
                    wrap(CONTENT_COLUMN) + ", " + wrap(LOADED_DATE_COLUMN) + ") values (:id, :content, :loadedDate)";

    protected static final String READ_LAST_SAVED_CONFIGURATION_QUERY =
            "select " + wrap(CONTENT_COLUMN) + " from " + wrap(CONFIGURATION_TABLE) +
            " where " + wrap("id") + "= (select max(" + wrap("id") + ") from " + wrap(CONFIGURATION_TABLE) + ")";

    @Autowired
    @Qualifier("masterNamedParameterJdbcTemplate")
    private NamedParameterJdbcOperations jdbcTemplate;
    @Autowired
    private IdGenerator idGenerator;

    /**
     * Смотри {@link ru.intertrust.cm.core.dao.api.ConfigurationDao#save(String)}
     * @param configuration конфигурация
     */
    @Override
    public void save(String configuration) {
        Map<String, Object> parametersMap = new HashMap();
        parametersMap.put("content", configuration);
        parametersMap.put("loadedDate", new Date());
        parametersMap.put("id", idGenerator.generateId(CONFIGURATION_TABLE));

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
                    Collections.emptyMap(), String.class);
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
