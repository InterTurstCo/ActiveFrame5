package ru.intertrust.cm.core.config;

import ru.intertrust.cm.core.config.model.CollectionConfig;
import ru.intertrust.cm.core.config.model.CollectionsConfiguration;
import ru.intertrust.cm.core.config.model.DomainObjectConfig;
import ru.intertrust.cm.core.config.model.DomainObjectsConfiguration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author vmatsukevich
 *         Date: 6/12/13
 *         Time: 5:21 PM
 */
public class ConfigurationExplorer {

    private DomainObjectsConfiguration domainObjectsConfiguration;
    private CollectionsConfiguration collectionsConfiguration;

    private Map<String, DomainObjectConfig> businessObjectConfigMap;
    private Map<String, CollectionConfig> collectionsConfigMap;

    public ConfigurationExplorer() {
    }

    public DomainObjectsConfiguration getDomainObjectsConfiguration() {
        return domainObjectsConfiguration;
    }

    public void setDomainObjectsConfiguration(DomainObjectsConfiguration domainObjectsConfiguration) {
        this.domainObjectsConfiguration = domainObjectsConfiguration;
    }

    public CollectionsConfiguration getCollectionsConfiguration() {
        return collectionsConfiguration;
    }

    public void setCollectionsConfiguration(CollectionsConfiguration collectionsConfiguration) {
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
    public DomainObjectConfig getBusinessObjectConfig(String name) {
        DomainObjectConfig result = businessObjectConfigMap.get(name);

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
        if(domainObjectsConfiguration == null) {
            throw new RuntimeException("Failed to initialize ConfigurationExplorer because businessObjects " +
                    "Configuration is null");
        }

        int size = domainObjectsConfiguration.getDomainObjectConfigs().size();
        businessObjectConfigMap = new ConcurrentHashMap<>(size);

        for (DomainObjectConfig domainObjectConfig : domainObjectsConfiguration.getDomainObjectConfigs()) {
            businessObjectConfigMap.put(domainObjectConfig.getName(), domainObjectConfig);
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
