package ru.intertrust.cm.core.config;

import ru.intertrust.cm.core.config.model.Configuration;
import ru.intertrust.cm.core.config.model.FieldConfig;

import java.util.Collection;

/**
 * Предоставляет быстрый доступ к элементам конфигурации.
 * @author vmatsukevich
 *         Date: 6/24/13
 *         Time: 1:28 PM
 */
public interface ConfigurationExplorer {

    /**
     * Инициализирует экземпляр {@link ConfigurationExplorer}
     */
    void build();

    /**
     * Возвращает конфигурацию
     * @return конфигурация
     */
    Configuration getConfiguration();

    <T> T getConfig(Class<T> type, String name);

    <T> Collection<T> getConfigs(Class<T> type);

    /**
     * Находит конфигурацию поля доменного объекта по имени доменного объекта и имени поля
     * @param domainObjectConfigName имя доменного объекта
     * @param fieldConfigName имя поля доменного объекта
     * @return конфигурация поля доменного объекта
     */
    FieldConfig getFieldConfig(String domainObjectConfigName, String fieldConfigName);

}
