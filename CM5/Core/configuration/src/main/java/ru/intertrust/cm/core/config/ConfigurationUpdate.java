package ru.intertrust.cm.core.config;

import ru.intertrust.cm.core.config.base.TopLevelConfig;

public class ConfigurationUpdate {

    private TopLevelConfig oldConfig;
    private TopLevelConfig newConfig;

    public ConfigurationUpdate(TopLevelConfig oldConfig, TopLevelConfig newConfig) {
        this.oldConfig = oldConfig;
        this.newConfig = newConfig;
    }

    public TopLevelConfig getOldConfig() {
        return oldConfig;
    }

    public TopLevelConfig getNewConfig() {
        return newConfig;
    }
}
