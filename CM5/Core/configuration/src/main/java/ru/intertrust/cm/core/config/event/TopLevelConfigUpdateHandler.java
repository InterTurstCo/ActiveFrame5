package ru.intertrust.cm.core.config.event;


import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.ConfigurationStorage;
import ru.intertrust.cm.core.config.ConfigurationStorageBuilder;
import ru.intertrust.cm.core.config.base.TopLevelConfig;

/**
 * Обработчик изменения конфигурации {@link TopLevelConfig}.
 * Выполняет действия по обновлению конфигурации, общие для всех видов конигурации.
 */
public class TopLevelConfigUpdateHandler extends ConfigurationUpdateHandler<TopLevelConfig> {

    @Override
    public void doUpdate(ConfigurationUpdateEvent configurationUpdateEvent) {
        ConfigurationStorage configStorage = configurationUpdateEvent.getConfigurationStorage();
        ConfigurationExplorer configurationExplorer = configurationUpdateEvent.getConfigurationExplorer();

        TopLevelConfig config = configurationUpdateEvent.getNewConfig();
        TopLevelConfig oldConfig = configurationUpdateEvent.getOldConfig();

        if (oldConfig != null) {
            configStorage.configuration.getConfigurationList().remove(oldConfig);
        }
        configStorage.configuration.getConfigurationList().add(config);

        ConfigurationStorageBuilder configurationStorageBuilder = new ConfigurationStorageBuilder(configurationExplorer, configStorage);
        configurationStorageBuilder.fillGlobalSettingsCache(config);
        configurationStorageBuilder.fillTopLevelConfigMap(config);
        configurationStorageBuilder.fillLocalizedConfigMaps(config);
    }

    @Override
    protected Class<TopLevelConfig> getClazz() {
        return TopLevelConfig.class;
    }
}
