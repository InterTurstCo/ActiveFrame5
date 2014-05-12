package ru.intertrust.cm.core.config.event;


import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.ConfigurationStorage;
import ru.intertrust.cm.core.config.ConfigurationStorageBuilder;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionViewConfig;

public class CollectionViewConfigUpdateHandler extends ConfigurationUpdateHandler<CollectionViewConfig> {

    @Override
    public void doUpdate(ConfigurationUpdateEvent configurationUpdateEvent) {
        ConfigurationStorage configStorage = configurationUpdateEvent.getConfigurationStorage();
        ConfigurationExplorer configurationExplorer = configurationUpdateEvent.getConfigurationExplorer();

        CollectionViewConfig newConfig = (CollectionViewConfig) configurationUpdateEvent.getNewConfig();
        CollectionViewConfig oldConfig = (CollectionViewConfig) configurationUpdateEvent.getOldConfig();

        ConfigurationStorageBuilder configurationStorageBuilder = new ConfigurationStorageBuilder(configurationExplorer, configStorage);
        configurationStorageBuilder.updateCollectionColumnConfigMap(oldConfig, newConfig);
    }

    @Override
    protected Class<CollectionViewConfig> getClazz() {
        return CollectionViewConfig.class;
    }
}
