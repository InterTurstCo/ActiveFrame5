package ru.intertrust.cm.core.config;


import ru.intertrust.cm.core.config.base.TopLevelConfig;

import java.util.Observable;
import java.util.Observer;

public abstract class ConfigurationObserver<T> implements Observer {

    protected ConfigurationExplorer configurationExplorer;
    protected ConfigurationStorage configStorage;

    protected ConfigurationObserver(ConfigurationExplorer configurationExplorer, ConfigurationStorage configStorage) {
        this.configurationExplorer = configurationExplorer;
        this.configStorage = configStorage;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (!(arg instanceof ConfigurationUpdate)) {
            return;
        }

        ConfigurationUpdate configurationUpdate = (ConfigurationUpdate) arg;
        TopLevelConfig config = configurationUpdate.getNewConfig();

        if (config == null || getClazz() == null || !config.getClass().isInstance(getClazz())) {
            return;
        }

        doUpdate(configurationUpdate);
    }

    protected abstract void doUpdate(ConfigurationUpdate configurationUpdate);

    protected abstract Class<T> getClazz();
}
