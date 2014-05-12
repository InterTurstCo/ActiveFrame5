package ru.intertrust.cm.core.config.event;


import org.springframework.context.ApplicationListener;
import ru.intertrust.cm.core.config.base.TopLevelConfig;

public abstract class ConfigurationUpdateHandler<T> implements ApplicationListener<ConfigurationUpdateEvent> {

    @Override
    public void onApplicationEvent(ConfigurationUpdateEvent updateEvent) {
        TopLevelConfig config = updateEvent.getNewConfig();

        if (config == null || getClazz() == null || !config.getClass().isInstance(getClazz())) {
            return;
        }

        doUpdate(updateEvent);
    }

    protected abstract void doUpdate(ConfigurationUpdateEvent configurationUpdateEvent);

    protected abstract Class<T> getClazz();
}
