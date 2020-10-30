package ru.intertrust.cm.core.business.api;

/**
 * Сервис глобальных настроек сервера (кластера серверов). Получает значения из таблицы global_server_settings
 * и если там нет из server.properties, и если нигде нет из параметра defaultValue
 * @author larin
 *
 */
public interface GlobalServerSettingsService {
    
    /**
     * Получение строкового значения из глобальной конфигурации сервера 
     * @param name
     * @return
     */
    public String getString(String name);
    
    /**
     * Получение строкового значения из глобальной конфигурации сервера, и значения по умолчанию если в конфигурации не найден
     * @param name
     * @param defaultValue
     * @return
     */
    public String getString(String name, String defaultValue);
    
    /**
     * Получение boolean значения из глобальной конфигурации сервера
     * @param name
     * @return
     */
    public Boolean getBoolean(String name);
    
    /**
     * Получение boolean значения из глобальной конфигурации сервера, и значения по умолчанию если в конфигурации не найден
     * @param name
     * @param defaultValue
     * @return
     */
    public Boolean getBoolean(String name, Boolean defaultValue);
    
    /**
     * Получение числового значения из глобальной конфигурации сервера 
     * @param name
     * @return
     */
    public Long getLong(String name);
    
    /**
     * Получение числового значения из глобальной конфигурации сервера, и значения по умолчанию если в конфигурации не найден
     * @param name
     * @param defaultValue
     * @return
     */
    public Long getLong(String name, Long defaultValue);

    /**
     * Установка строкового значения глобальной конфигурации
     * @param name
     * @param value
     */
    public void setString(String name, String value);

    /**
     * Установка числового значения глобальной конфигурации
     * @param name
     * @param value
     */
    public void setLong(String name, Long value);

    /**
     * Установка логического значения глобальной конфигурации
     * @param name
     * @param value
     */
    public void setBoolean(String name, Boolean value);
}
