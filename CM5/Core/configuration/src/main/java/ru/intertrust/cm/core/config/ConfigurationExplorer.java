package ru.intertrust.cm.core.config;

import ru.intertrust.cm.core.config.model.CollectionConfig;
import ru.intertrust.cm.core.config.model.Configuration;
import ru.intertrust.cm.core.config.model.DomainObjectConfig;
import ru.intertrust.cm.core.config.model.FieldConfig;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author vmatsukevich
 *         Date: 6/12/13
 *         Time: 5:21 PM
 */
public class ConfigurationExplorer {

    private Configuration configuration;

    private Map<String, DomainObjectConfig> domainObjectConfigMap = new HashMap<>();
    private Map<String, CollectionConfig> collectionConfigMap = new HashMap<>();
    private Map<FieldConfigKey, FieldConfig> fieldConfigMap = new HashMap<>();

    public ConfigurationExplorer() {
    }

    public ConfigurationExplorer(Configuration configuration) {
        this.configuration = configuration;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public void init() {
        initConfigurationMaps();

        ConfigurationLogicalValidator logicalValidator = new ConfigurationLogicalValidator(this);
        logicalValidator.validate();
    }

    public Collection<DomainObjectConfig> getDomainObjectConfigs() {
        return domainObjectConfigMap.values();
    }

    public Collection<CollectionConfig> getCollectionConfigs() {
        return collectionConfigMap.values();
    }

    /**
     * Находит конфигурацию доменного объекта по имени
     * @param name имя доменного объекта, конфигурацию которого надо найти
     * @return конфигурация доменного объекта
     */
    public DomainObjectConfig getDomainObjectConfig(String name) {
        return domainObjectConfigMap.get(name);
    }

    public CollectionConfig getCollectionConfig(String name) {
        return collectionConfigMap.get(name);
    }

    public FieldConfig getFieldConfig(String domainObjectConfigName, String fieldConfigName) {
        FieldConfigKey fieldConfigKey = new FieldConfigKey(domainObjectConfigName, fieldConfigName);
        return fieldConfigMap.get(fieldConfigKey);
    }

    private void initConfigurationMaps() {
        if(configuration == null) {
            throw new RuntimeException("Failed to initialize ConfigurationExplorer because " +
                    "Configuration is null");
        }

        for (Object config : configuration.getConfigurationList()) {
            if (DomainObjectConfig.class.equals(config.getClass())) {
                DomainObjectConfig domainObjectConfig = (DomainObjectConfig) config;
                domainObjectConfigMap.put(domainObjectConfig.getName(), domainObjectConfig);

                for (FieldConfig fieldConfig : domainObjectConfig.getFieldConfigs()) {
                    FieldConfigKey fieldConfigKey = new FieldConfigKey(domainObjectConfig.getName(), fieldConfig.getName());
                    fieldConfigMap.put(fieldConfigKey, fieldConfig);
                }
            } else if(CollectionConfig.class.equals(config.getClass())) {
                CollectionConfig collectionConfig = (CollectionConfig) config;
                collectionConfigMap.put(collectionConfig.getName(), collectionConfig);
            } else {
                throw new RuntimeException("Unknown configuration type '" + config.getClass() + "'");
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
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FieldConfigKey that = (FieldConfigKey) o;

            if (domainObjectName != null ? !domainObjectName.equals(that.domainObjectName) : that.domainObjectName != null)
                return false;
            if (fieldConfigName != null ? !fieldConfigName.equals(that.fieldConfigName) : that.fieldConfigName != null)
                return false;

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
