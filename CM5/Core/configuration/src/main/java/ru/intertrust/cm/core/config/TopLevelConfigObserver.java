package ru.intertrust.cm.core.config;


import ru.intertrust.cm.core.config.base.TopLevelConfig;

import java.util.Observable;

public class TopLevelConfigObserver extends ConfigurationObserver<TopLevelConfig> {


    protected TopLevelConfigObserver(ConfigurationExplorer configurationExplorer, ConfigurationStorage configStorage) {
        super(configurationExplorer, configStorage);
    }

    @Override
    public void doUpdate(Observable o, Object arg) {
        TopLevelConfig config = (TopLevelConfig) arg;

        TopLevelConfig oldConfig = configurationExplorer.getConfig(config.getClass(), config.getName());
        if (oldConfig != null) {
            configStorage.configuration.getConfigurationList().remove(oldConfig);
        }

        configStorage.configuration.getConfigurationList().add(config);
    }

    @Override
    protected Class<TopLevelConfig> getClazz() {
        return TopLevelConfig.class;
    }
}
