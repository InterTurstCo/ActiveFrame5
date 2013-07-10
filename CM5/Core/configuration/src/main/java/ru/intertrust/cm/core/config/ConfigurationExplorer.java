package ru.intertrust.cm.core.config;

import ru.intertrust.cm.core.config.model.AccessMatrixConfig;
import ru.intertrust.cm.core.config.model.CollectionConfig;
import ru.intertrust.cm.core.config.model.Configuration;
import ru.intertrust.cm.core.config.model.ContextRoleConfig;
import ru.intertrust.cm.core.config.model.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.model.DynamicGroupConfig;
import ru.intertrust.cm.core.config.model.FieldConfig;
import ru.intertrust.cm.core.config.model.StaticGroupConfig;

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
    Collection<DomainObjectTypeConfig> getDomainObjectConfigs();

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
    DomainObjectTypeConfig getDomainObjectTypeConfig(String name);

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
    
    /**
     * Находит конфигурацию статической группы
     * @param name имя статической группы, которую надо найти
     * @return конфигурация статической группы
     */
    StaticGroupConfig getStaticGroupConfig(String name);    

    /**
     * Находит конфигурацию динамической группы
     * @param name имя динамической группы, которую надо найти
     * @return конфигурация динамической группы
     */
    DynamicGroupConfig getDynamicGroupConfig(String name);    

    /**
     * Находит конфигурацию контекстной роли
     * @param name имя контекстной роли, которую надо найти
     * @return конфигурация динамической группы
     */
    ContextRoleConfig getContextRoleConfig(String name);    

    /**
     * Находит конфигурацию матрицы доступа для переданного типа доменного объекта
     * @param name имя матрицы доступа, которую надо найти
     * @return конфигурация матрицы доступа 
     */

    AccessMatrixConfig getAccessMatrixConfig(String domainObjectType);    

}
