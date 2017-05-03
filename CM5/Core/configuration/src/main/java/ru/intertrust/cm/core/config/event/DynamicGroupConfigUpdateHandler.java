package ru.intertrust.cm.core.config.event;


import ru.intertrust.cm.core.config.ConfigurationStorage;
import ru.intertrust.cm.core.config.DynamicGroupConfig;

/**
 * Обработчик изменения конфигурации {@link DynamicGroupConfig}
 */
public class DynamicGroupConfigUpdateHandler extends ConfigurationUpdateHandler<DynamicGroupConfig> {

    @Override
    public void doUpdate(ConfigurationUpdateEvent configurationUpdateEvent) {
        ConfigurationStorage configStorage = configurationUpdateEvent.getConfigurationStorage();
        DynamicGroupConfig oldConfig = (DynamicGroupConfig) configurationUpdateEvent.getOldConfig();

        if (oldConfig != null && oldConfig.getContext() != null && oldConfig.getContext().getDomainObject() != null) {
            configStorage.dynamicGroupConfigByContextMap.remove(oldConfig.getContext().getDomainObject().getType());
        }

        configStorage.dynamicGroupConfigsByTrackDOMap.clear();
    }

    @Override
    protected void onUpdate(ConfigurationUpdateEvent event) {
        throw new UnsupportedOperationException("Dynamic groups can not be changed in runtime");
    }

    @Override
    protected Class<DynamicGroupConfig> getClazz() {
        return DynamicGroupConfig.class;
    }
}
