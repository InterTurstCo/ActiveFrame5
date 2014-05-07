package ru.intertrust.cm.core.config;


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
        if (arg == null || getClazz() == null || !arg.getClass().isInstance(getClazz())) {
            return;
        }

        doUpdate(o, arg);
    }

    protected abstract void doUpdate(Observable o, Object arg);

    protected abstract Class<T> getClazz();
}
