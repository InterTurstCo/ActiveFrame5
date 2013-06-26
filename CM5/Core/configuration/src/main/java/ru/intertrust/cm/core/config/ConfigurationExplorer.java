package ru.intertrust.cm.core.config;

import ru.intertrust.cm.core.config.model.CollectionConfig;
import ru.intertrust.cm.core.config.model.Configuration;
import ru.intertrust.cm.core.config.model.DomainObjectConfig;
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

    /**
     * Возвращает конфигурации доменных объектов, содержащихся в конфигурации
     * @return коллекция конфигураций доменных объектов
     */
    Collection<DomainObjectConfig> getDomainObjectConfigs();

    /**
     * Возвращает конфигурации коллекций, содержащихся в конфигурации
     * @return конфигурации коллекций
     */
    Collection<CollectionConfig> getCollectionConfigs();

    /**
     * Находит конфигурацию доменного объекта по имени
     * @param name имя доменного объекта, конфигурацию которого надо найти
     * @return конфигурация доменного объекта
     */
    DomainObjectConfig getDomainObjectConfig(String name);

    /**
     * Находит конфигурацию коллекции
     * @param name имя коллекции, которую надо найти
     * @return конфигурация коллекции
     */
    CollectionConfig getCollectionConfig(String name);

    /**
     * Находит конфигурацию поля доменного объекта по имени доменного объекта и имени поля
     * @param domainObjectConfigName имя доменного объекта
     * @param fieldConfigName имя поля доменного объекта
     * @return конфигурация поля доменного объекта
     */
    FieldConfig getFieldConfig(String domainObjectConfigName, String fieldConfigName);
}
