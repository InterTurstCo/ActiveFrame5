package ru.intertrust.cm.core.config;


import ru.intertrust.cm.core.config.gui.action.ToolBarConfig;

public class ToolBarConfigObserver1 extends ConfigurationObserver<ToolBarConfig> {


    protected ToolBarConfigObserver1(ConfigurationExplorer configurationExplorer, ConfigurationStorage configStorage) {
        super(configurationExplorer, configStorage);
    }

    @Override
    public void doUpdate(ConfigurationUpdate configurationUpdate) {
        ToolBarConfig newConfig = (ToolBarConfig) configurationUpdate.getNewConfig();
        ToolBarConfig oldConfig = (ToolBarConfig) configurationUpdate.getOldConfig();

        ConfigurationStorageBuilder configurationStorageBuilder = new ConfigurationStorageBuilder(configurationExplorer, configStorage);
        configurationStorageBuilder.updateToolbarConfigByPluginMap(oldConfig, newConfig);
    }

    @Override
    protected Class<ToolBarConfig> getClazz() {
        return ToolBarConfig.class;
    }
}
