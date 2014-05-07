package ru.intertrust.cm.core.config;


import ru.intertrust.cm.core.config.base.TopLevelConfig;

import java.util.Observable;

public class TopLevelConfigObserver extends ConfigurationObserver<TopLevelConfig> {


    protected TopLevelConfigObserver(ConfigurationExplorer configurationExplorer, ConfigurationStorage configStorage) {
        super(configurationExplorer, configStorage);
    }

    @Override
    public void doUpdate(ConfigurationUpdate configurationUpdate) {
        TopLevelConfig config = configurationUpdate.getNewConfig();
        TopLevelConfig oldConfig = configurationUpdate.getOldConfig();

        if (oldConfig != null) {
            configStorage.configuration.getConfigurationList().remove(oldConfig);
        }
        configStorage.configuration.getConfigurationList().add(config);

        ConfigurationStorageBuilder configurationStorageBuilder = new ConfigurationStorageBuilder(configurationExplorer, configStorage);
        configurationStorageBuilder.fillGlobalSettingsCache(config);
        configurationStorageBuilder.fillTopLevelConfigMap(config);
    }

    @Override
    protected Class<TopLevelConfig> getClazz() {
        return TopLevelConfig.class;
    }
}
