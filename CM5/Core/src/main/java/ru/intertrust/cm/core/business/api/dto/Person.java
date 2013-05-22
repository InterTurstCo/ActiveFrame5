package ru.intertrust.cm.core.business.api.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * Пользователь системы. Поля конфигурируются в конфигурации бизнес-объекта Person.
 * 
 * @author atsvetkov
 * 
 */
public class Person {

    private Map<String, Object> configuredFields;

    /**
     * Возвращает сконфигурированные поля для пользователя.
     * @return {@link Map}
     */
    public Map<String, Object> getConfiguredFields() {
        if (configuredFields == null) {
            configuredFields = new HashMap<String, Object>();
        }
        return configuredFields;
    }

    /**
     * Устанавливает сконфигурированные поля для пользователя.
     * @param configuredFields
     */
    public void setConfiguredFields(Map<String, Object> configuredFields) {
        this.configuredFields = configuredFields;
    }

}
