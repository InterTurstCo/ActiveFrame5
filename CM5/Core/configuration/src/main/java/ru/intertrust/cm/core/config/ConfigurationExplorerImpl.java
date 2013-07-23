package ru.intertrust.cm.core.config;

import ru.intertrust.cm.core.config.model.*;
import ru.intertrust.cm.core.model.FatalException;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Предоставляет быстрый доступ к элементам конфигурации.
 * После создания объекта данного класса требуется выполнить инициализацию через вызов метода {@link #build()}.
 * @author vmatsukevich
 *         Date: 6/12/13
 *         Time: 5:21 PM
 */
public class ConfigurationExplorerImpl implements ConfigurationExplorer {

    private Configuration configuration;

    private Map<Class, Map<String, TopLevelConfig>> topLevelConfigMap = new HashMap<>();
    private Map<FieldConfigKey, FieldConfig> fieldConfigMap = new HashMap<>();

    public ConfigurationExplorerImpl() {
    }

    public ConfigurationExplorerImpl(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Возвращает конфигурацию
     * @return конфигурация
     */
    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Устанавливает конфигурацию
     * @param configuration конфигурация
     */
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Выполняет инициализацию объекта данного класса: создает внутреннюю структуру,
     * обеспечивающую быстрый доступ к элементам конфигурации и выполняет логическую валидацию конфигурации
     */
    @Override
    public void build() {
        initConfigurationMaps();

        ConfigurationLogicalValidator logicalValidator = new ConfigurationLogicalValidator(this);
        logicalValidator.validate();
    }

    public <T> T getConfig(Class<T> type, String name) {
        Map<String, TopLevelConfig> typeMap = topLevelConfigMap.get(type);
        if(typeMap == null) {
            return null;
        }

        return (T) typeMap.get(name);
    }

    public <T> Collection<T> getConfigs(Class<T> type) {
        Map<String, TopLevelConfig> typeMap = topLevelConfigMap.get(type);
        if(typeMap == null) {
            return Collections.EMPTY_LIST;
        }

        return (Collection<T>) typeMap.values();
    }

    /**
     * Смотри {@link ConfigurationExplorer#getFieldConfig(String, String)}
     */
    @Override
    public FieldConfig getFieldConfig(String domainObjectConfigName, String fieldConfigName) {
        FieldConfigKey fieldConfigKey = new FieldConfigKey(domainObjectConfigName, fieldConfigName);
        return fieldConfigMap.get(fieldConfigKey);
    }

    private void initConfigurationMaps() {
        if(configuration == null) {
            throw new FatalException("Failed to initialize ConfigurationExplorerImpl because " +
                    "Configuration is null");
        }

        topLevelConfigMap.clear();
        fieldConfigMap.clear();

        for (TopLevelConfig config : configuration.getConfigurationList()) {
            Map<String, TopLevelConfig> typeMap = topLevelConfigMap.get(config.getClass());
            if(typeMap == null) {
                typeMap = new HashMap<>();
                topLevelConfigMap.put(config.getClass(), typeMap);
            }
            typeMap.put(config.getName(), config);

            if (DomainObjectTypeConfig.class.equals(config.getClass())) {
                DomainObjectTypeConfig domainObjectTypeConfig = (DomainObjectTypeConfig) config;
                for (FieldConfig fieldConfig : domainObjectTypeConfig.getFieldConfigs()) {
                    FieldConfigKey fieldConfigKey =
                            new FieldConfigKey(domainObjectTypeConfig.getName(), fieldConfig.getName());
                    fieldConfigMap.put(fieldConfigKey, fieldConfig);
                }
            }
        }
    }

    private class FieldConfigKey {

        private String domainObjectName;
        private String fieldConfigName;

        private FieldConfigKey(String domainObjectName, String fieldConfigName) {
            this.domainObjectName = domainObjectName;
            this.fieldConfigName = fieldConfigName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            FieldConfigKey that = (FieldConfigKey) o;

            if (domainObjectName != null ? !domainObjectName.equals(that.domainObjectName) : that.domainObjectName != null) {
                return false;
            }
            if (fieldConfigName != null ? !fieldConfigName.equals(that.fieldConfigName) : that.fieldConfigName != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = domainObjectName != null ? domainObjectName.hashCode() : 0;
            result = 31 * result + (fieldConfigName != null ? fieldConfigName.hashCode() : 0);
            return result;
        }
    }

}
