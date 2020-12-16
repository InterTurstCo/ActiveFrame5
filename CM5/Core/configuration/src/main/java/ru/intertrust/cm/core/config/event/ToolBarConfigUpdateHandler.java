package ru.intertrust.cm.core.config.event;


import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.ConfigurationStorage;
import ru.intertrust.cm.core.config.ConfigurationStorageBuilder;
import ru.intertrust.cm.core.config.gui.action.ToolBarConfig;
import ru.intertrust.cm.core.config.module.ModuleService;

/**
 * Обработчик изменения конфигурации {@link ToolBarConfig}
 */
public class ToolBarConfigUpdateHandler extends ConfigurationUpdateHandler<ToolBarConfig> {

    @Autowired
    private ModuleService moduleService;

    @Override
    public void doUpdate(ConfigurationUpdateEvent configurationUpdateEvent) {
        ConfigurationStorage configStorage = configurationUpdateEvent.getConfigurationStorage();
        ConfigurationExplorer configurationExplorer = configurationUpdateEvent.getConfigurationExplorer();

        ToolBarConfig newConfig = (ToolBarConfig) configurationUpdateEvent.getNewConfig();
        ToolBarConfig oldConfig = (ToolBarConfig) configurationUpdateEvent.getOldConfig();

        ConfigurationStorageBuilder configurationStorageBuilder = new ConfigurationStorageBuilder(configurationExplorer, configStorage, moduleService);
        configurationStorageBuilder.updateToolbarConfigByPluginMap(oldConfig, newConfig);
    }

    @Override
    protected Class<ToolBarConfig> getClazz() {
        return ToolBarConfig.class;
    }
}
