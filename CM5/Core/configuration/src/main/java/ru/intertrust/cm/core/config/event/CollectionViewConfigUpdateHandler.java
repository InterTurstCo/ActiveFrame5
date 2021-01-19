package ru.intertrust.cm.core.config.event;


import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.ConfigurationStorage;
import ru.intertrust.cm.core.config.ConfigurationStorageBuilder;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionViewConfig;
import ru.intertrust.cm.core.config.module.ModuleService;

/**
 * Обработчик изменения конфигурации {@link CollectionViewConfig}
 */
public class CollectionViewConfigUpdateHandler extends ConfigurationUpdateHandler<CollectionViewConfig> {
    @Autowired
    private ModuleService moduleService;

    @Override
    public void doUpdate(ConfigurationUpdateEvent configurationUpdateEvent) {
        ConfigurationStorage configStorage = configurationUpdateEvent.getConfigurationStorage();
        ConfigurationExplorer configurationExplorer = configurationUpdateEvent.getConfigurationExplorer();

        CollectionViewConfig newConfig = (CollectionViewConfig) configurationUpdateEvent.getNewConfig();
        CollectionViewConfig oldConfig = (CollectionViewConfig) configurationUpdateEvent.getOldConfig();

        ConfigurationStorageBuilder configurationStorageBuilder = new ConfigurationStorageBuilder(configurationExplorer, configStorage, moduleService);
        configurationStorageBuilder.updateCollectionColumnConfigMap(oldConfig, newConfig);
    }

    @Override
    protected Class<CollectionViewConfig> getClazz() {
        return CollectionViewConfig.class;
    }
}
