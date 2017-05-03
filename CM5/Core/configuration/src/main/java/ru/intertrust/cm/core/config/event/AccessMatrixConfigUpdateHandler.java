package ru.intertrust.cm.core.config.event;


import ru.intertrust.cm.core.config.AccessMatrixConfig;
import ru.intertrust.cm.core.config.ConfigurationStorage;

/**
 * Обработчик изменения конфигурации {@link AccessMatrixConfigUpdateHandler}
 */
public class AccessMatrixConfigUpdateHandler extends ConfigurationUpdateHandler<AccessMatrixConfig> {

    @Override
    public void doUpdate(ConfigurationUpdateEvent configurationUpdateEvent) {
        ConfigurationStorage configStorage = configurationUpdateEvent.getConfigurationStorage();
        configStorage.accessMatrixByObjectTypeAndStatusMap.clear();
        configStorage.matrixReferenceTypeNameMap.clear();
        configStorage.typesDelegatingAccessCheckTo.clear();
        configStorage.typesDelegatingAccessCheckToInLowerCase.clear();
        configStorage.readPermittedToEverybodyMap.clear();
    }

    @Override
    protected void onUpdate(ConfigurationUpdateEvent event) {
        throw new UnsupportedOperationException("Access matrixes can not be changed in runtime");
    }

    @Override
    protected Class<AccessMatrixConfig> getClazz() {
        return AccessMatrixConfig.class;
    }
}
