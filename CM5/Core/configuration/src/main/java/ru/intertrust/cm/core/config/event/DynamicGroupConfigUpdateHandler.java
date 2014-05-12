package ru.intertrust.cm.core.config.event;


import ru.intertrust.cm.core.config.ConfigurationStorage;
import ru.intertrust.cm.core.config.DynamicGroupConfig;

public class DynamicGroupConfigUpdateHandler extends ConfigurationUpdateHandler<DynamicGroupConfig> {

    @Override
    public void doUpdate(ConfigurationUpdateEvent configurationUpdateEvent) {
        ConfigurationStorage configStorage = configurationUpdateEvent.getConfigurationStorage();
        DynamicGroupConfig oldConfig = (DynamicGroupConfig) configurationUpdateEvent.getOldConfig();

        if (oldConfig.getContext().getDomainObject() != null && oldConfig.getContext().getDomainObject() != null) {
            configStorage.dynamicGroupConfigByContextMap.remove(oldConfig.getContext().getDomainObject().getType());
        }

        configStorage.dynamicGroupConfigsByTrackDOMap.clear();
    }

    @Override
    protected Class<DynamicGroupConfig> getClazz() {
        return DynamicGroupConfig.class;
    }
}
