package ru.intertrust.cm.core.business.impl;

import ru.intertrust.cm.core.config.BusinessObjectConfig;
import ru.intertrust.cm.core.config.CollectionConfig;
import ru.intertrust.cm.core.config.CollectionConfiguration;
import ru.intertrust.cm.core.config.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author vmatsukevich
 *         Date: 6/12/13
 *         Time: 5:21 PM
 */
public class ConfigurationExplorer {

    private Configuration businessObjectsConfiguration;
    private CollectionConfiguration collectionsConfiguration;

    private Map<String, BusinessObjectConfig> businessObjectConfigMap;
    private Map<String, CollectionConfig> collectionsConfigMap;

    public ConfigurationExplorer() {
    }

    public Configuration getBusinessObjectsConfiguration() {
        return businessObjectsConfiguration;
    }

    public void setBusinessObjectsConfiguration(Configuration businessObjectsConfiguration) {
        this.businessObjectsConfiguration = businessObjectsConfiguration;
    }

    public CollectionConfiguration getCollectionsConfiguration() {
        return collectionsConfiguration;
    }

    public void setCollectionsConfiguration(CollectionConfiguration collectionsConfiguration) {
        this.collectionsConfiguration = collectionsConfiguration;
    }

    public void init() {
        initBusinessObjectsConfigurationMap();
        initCollectionsConfigurationMap();
    }

    /**
     * Находит конфигурацию бизнес-объекта по имени
     * @param name имя бизнес-объекта, конфигурацию которого надо найти
     * @return конфигурация бизнес-объекта
     */
    public BusinessObjectConfig getBusinessObjectConfig(String name) {
        BusinessObjectConfig result = businessObjectConfigMap.get(name);

        if(result == null) {
            throw new RuntimeException("BusinessObject Configuration is not found for name '" + name + "'");
        }

        return result;
    }

    public CollectionConfig getCollectionConfig(String name) {
        CollectionConfig result = collectionsConfigMap.get(name);

        if(result == null) {
            throw new RuntimeException("Collection Configuration is not found for name '" + name + "'");
        }

        return result;
    }

    private void initBusinessObjectsConfigurationMap() {
        if(businessObjectsConfiguration == null) {
            throw new RuntimeException("Failed to initialize ConfigurationExplorer because businessObjects " +
                    "Configuration is null");
        }

        int size = businessObjectsConfiguration.getBusinessObjectConfigs().size();
        businessObjectConfigMap = new ConcurrentHashMap<>(size);

        for (BusinessObjectConfig businessObjectConfig : businessObjectsConfiguration.getBusinessObjectConfigs()) {
            businessObjectConfigMap.put(businessObjectConfig.getName(), businessObjectConfig);
        }
    }

    private void initCollectionsConfigurationMap() {
        if(collectionsConfiguration == null) {
            throw new RuntimeException("Failed to initialize ConfigurationExplorer because collections Configuration " +
                    "is null");
        }

        int size = collectionsConfiguration.getCollectionConfigs().size();
        collectionsConfigMap = new ConcurrentHashMap<>(size);

        for (CollectionConfig collectionConfig : collectionsConfiguration.getCollectionConfigs()) {
            collectionsConfigMap.put(collectionConfig.getName(), collectionConfig);
        }
    }
}
