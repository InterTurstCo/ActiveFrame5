package ru.intertrust.cm.core.config;

import ru.intertrust.cm.core.config.model.CollectionConfig;
import ru.intertrust.cm.core.config.model.Configuration;
import ru.intertrust.cm.core.config.model.DomainObjectConfig;

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

    public ConfigurationExplorer() {
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
        DomainObjectConfig config = domainObjectConfigMap.get(name);

        if(config == null) {
            throw new RuntimeException("DomainObject Configuration is not found for name '" + name + "'");
        }

        return config;
    }

    public CollectionConfig getCollectionConfig(String name) {
        CollectionConfig config = collectionConfigMap.get(name);

        if(config == null) {
            throw new RuntimeException("DomainObject Configuration is not found for name '" + name + "'");
        }

        return config;
    }

    private void initConfigurationMaps() {
        if(configuration == null) {
            throw new RuntimeException("Failed to initialize ConfigurationExplorer because " +
                    "Configuration is null");
        }

        for (Object config : configuration.getConfigurationList()) {
            if(DomainObjectConfig.class.equals(config.getClass())) {
                DomainObjectConfig domainObjectConfig = (DomainObjectConfig) config;
                domainObjectConfigMap.put(domainObjectConfig.getName(), domainObjectConfig);
            } else if(CollectionConfig.class.equals(config.getClass())) {
                CollectionConfig collectionConfig = (CollectionConfig) config;
                collectionConfigMap.put(collectionConfig.getName(), collectionConfig);
            } else {
                throw new RuntimeException("Unknown configuration type '" + config.getClass() + "'");
            }
        }
    }
}
