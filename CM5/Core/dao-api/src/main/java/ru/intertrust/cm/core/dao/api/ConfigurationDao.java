package ru.intertrust.cm.core.dao.api;

/**
 * DAO конфигурации
 * @author vmatsukevich
 *         Date: 6/17/13
 *         Time: 3:42 PM
 */
public interface ConfigurationDao {

    String CONFIGURATION_TABLE = "configuration";
    String CONTENT_COLUMN = "content";
    String LOADED_DATE_COLUMN = "loaded_date";

    /**
     * Сохраняет конфигурацию
     * @param configuration конфигурация
     */
    void save(String configuration);

    /**
     * Возвращает последнюю сохраненную в базе конфигурацию
     * @return
     */
    String readLastSavedConfiguration();
}
