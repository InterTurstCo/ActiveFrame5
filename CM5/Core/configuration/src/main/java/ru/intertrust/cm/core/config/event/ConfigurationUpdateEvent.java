package ru.intertrust.cm.core.config.event;

import org.springframework.context.ApplicationEvent;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.ConfigurationStorage;
import ru.intertrust.cm.core.config.base.TopLevelConfig;

/**
 * Событие обновления конфигурации
 */
public class ConfigurationUpdateEvent extends ApplicationEvent {

    private ConfigurationExplorer configurationExplorer;
    private ConfigurationStorage configurationStorage;

    private TopLevelConfig oldConfig;
    private TopLevelConfig newConfig;

    public ConfigurationUpdateEvent(ConfigurationExplorer configurationExplorer, ConfigurationStorage configStorage, TopLevelConfig oldConfig, TopLevelConfig newConfig) {
        super(configurationExplorer);

        this.configurationExplorer = configurationExplorer;
        this.configurationStorage = configStorage;

        this.oldConfig = oldConfig;
        this.newConfig = newConfig;
    }

    public TopLevelConfig getOldConfig() {
        return oldConfig;
    }

    public TopLevelConfig getNewConfig() {
        return newConfig;
    }

    public ConfigurationExplorer getConfigurationExplorer() {
        return configurationExplorer;
    }

    public ConfigurationStorage getConfigurationStorage() {
        return configurationStorage;
    }
}
