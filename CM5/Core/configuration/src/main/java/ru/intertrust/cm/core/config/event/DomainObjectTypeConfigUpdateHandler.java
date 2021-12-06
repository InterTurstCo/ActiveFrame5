package ru.intertrust.cm.core.config.event;


import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.ConfigurationStorage;
import ru.intertrust.cm.core.config.ConfigurationStorageBuilder;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.module.ModuleService;

/**
 * Обработчик изменения конфигурации {@link ru.intertrust.cm.core.config.DomainObjectTypeConfig}
 */
public class DomainObjectTypeConfigUpdateHandler extends ConfigurationUpdateHandler<DomainObjectTypeConfig> {
    @Autowired
    private ModuleService moduleService;

    @Override
    public void doUpdate(ConfigurationUpdateEvent configurationUpdateEvent) {
        ConfigurationStorage configStorage = configurationUpdateEvent.getConfigurationStorage();
        ConfigurationExplorer configurationExplorer = configurationUpdateEvent.getConfigurationExplorer();

        DomainObjectTypeConfig newConfig = (DomainObjectTypeConfig) configurationUpdateEvent.getNewConfig();
        DomainObjectTypeConfig oldConfig = (DomainObjectTypeConfig) configurationUpdateEvent.getOldConfig();

        ConfigurationStorageBuilder configurationStorageBuilder = new ConfigurationStorageBuilder(configurationExplorer, configStorage, moduleService);

        configurationStorageBuilder.updateDomainObjectFieldConfig(oldConfig, newConfig);
        configurationStorageBuilder.updateConfigurationMapsOfAttachmentDomainObjectType(oldConfig, newConfig);
        configurationStorageBuilder.updateConfigurationMapOfChildDomainObjectType(newConfig);
        configurationStorageBuilder.updateAuditLogConfigs(oldConfig, newConfig);
        
        if (oldConfig != null) {
            configStorage.domainObjectTypesHierarchy.remove(oldConfig.getName());
            configStorage.referenceFieldsMap.remove(oldConfig.getName());
        }

        configStorage.typesDelegatingAccessCheckTo.clear();
        configStorage.typesDelegatingAccessCheckToInLowerCase.clear();
    }

    @Override
    protected void onUpdate(ConfigurationUpdateEvent event) {
        throw new UnsupportedOperationException("Domain Object Types can not be changed in runtime");
    }

    @Override
    protected Class<DomainObjectTypeConfig> getClazz() {
        return DomainObjectTypeConfig.class;
    }
}
