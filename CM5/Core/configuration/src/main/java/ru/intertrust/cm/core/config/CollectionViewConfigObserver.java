package ru.intertrust.cm.core.config;


import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionViewConfig;

import java.util.Observable;

public class CollectionViewConfigObserver extends ConfigurationObserver<CollectionViewConfig> {


    protected CollectionViewConfigObserver(ConfigurationExplorer configurationExplorer, ConfigurationStorage configStorage) {
        super(configurationExplorer, configStorage);
    }

    @Override
    public void doUpdate(ConfigurationUpdate configurationUpdate) {
        CollectionViewConfig newConfig = (CollectionViewConfig) configurationUpdate.getNewConfig();
        CollectionViewConfig oldConfig = (CollectionViewConfig) configurationUpdate.getOldConfig();

        ConfigurationStorageBuilder configurationStorageBuilder = new ConfigurationStorageBuilder(configurationExplorer, configStorage);
        configurationStorageBuilder.updateCollectionColumnConfigMap(oldConfig, newConfig);
    }

    @Override
    protected Class<CollectionViewConfig> getClazz() {
        return CollectionViewConfig.class;
    }
}
