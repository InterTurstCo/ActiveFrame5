package ru.intertrust.cm.core.config.event;


import ru.intertrust.cm.core.config.AccessMatrixConfig;
import ru.intertrust.cm.core.config.ConfigurationStorage;
import ru.intertrust.cm.core.config.DynamicGroupConfig;

/**
 * Обработчик изменения конфигурации {@link AccessMatrixConfigUpdateHandler}
 */
public class AccessMatrixConfigUpdateHandler extends ConfigurationUpdateHandler<AccessMatrixConfig> {

    @Override
    public void doUpdate(ConfigurationUpdateEvent configurationUpdateEvent) {
        ConfigurationStorage configStorage = configurationUpdateEvent.getConfigurationStorage();
        configStorage.accessMatrixByObjectTypeAndStatusMap.clear();
        configStorage.matrixReferenceTypeNameMap.clear();
        configStorage.readPermittedToEverybodyMap.clear();
    }

    @Override
    protected Class<AccessMatrixConfig> getClazz() {
        return AccessMatrixConfig.class;
    }
}
