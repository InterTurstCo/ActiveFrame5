package ru.intertrust.cm.core.config;

import ru.intertrust.cm.core.config.model.*;

import java.util.List;

/**
 * Логически Валидирует конфигурацию доменнных объектов
 * User: atsvetkov Date: 17.05.13 Time: 13:52
 */
public class DomainObjectsConfigurationLogicalValidator {

    private ConfigurationExplorer configurationExplorer;

    public DomainObjectsConfigurationLogicalValidator() {
    }

    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    /**
     * Выполняет валидацию конфигурации на предмет соответствия XSD схеме и логическую валидацию.
     */
    public void validate() {
        List<DomainObjectConfig> domainObjectConfigs = configurationExplorer.getDomainObjectsConfiguration().getDomainObjectConfigs();
        if (domainObjectConfigs.isEmpty()) {
            return;
        }
        for (DomainObjectConfig domainObjectConfig : domainObjectConfigs) {
            validateBusinessObjectConfig(domainObjectConfig);
        }
        // TODO Log success information using logging API
        System.out.println("Document has passed logical validation");
    }

    private void validateBusinessObjectConfig(DomainObjectConfig domainObjectConfig) {
        if (domainObjectConfig == null) {
            return;
        }

        validateParentConfig(domainObjectConfig);
        validateReferenceFields(domainObjectConfig);
        validateUniqueKeys(domainObjectConfig);
    }

    private void validateUniqueKeys(DomainObjectConfig domainObjectConfig) {
        for (UniqueKeyConfig uniqueKeyConfig : domainObjectConfig.getUniqueKeyConfigs()) {
            for (UniqueKeyFieldConfig uniqueKeyFieldConfig : uniqueKeyConfig.getUniqueKeyFieldConfigs()) {
                validateBusinessObjectConfigContainsField(domainObjectConfig, uniqueKeyFieldConfig.getName());
            }
        }
    }

    private void validateBusinessObjectConfigContainsField(DomainObjectConfig domainObjectConfig,
                                                          String fieldName) {
        for(FieldConfig fieldConfig : domainObjectConfig.getFieldConfigs()) {
            if(fieldName.equals(fieldConfig.getName())) {
                return;
            }
        }
        throw new RuntimeException("FieldConfig with name " + fieldName + " is not found in business object '" + domainObjectConfig.getName() + "'");
    }

    private void validateReferenceFields(DomainObjectConfig domainObjectConfig) {
        for (FieldConfig fieldConfig : domainObjectConfig.getFieldConfigs()) {
            if (ReferenceFieldConfig.class.equals(fieldConfig.getClass())) {
                configurationExplorer.getBusinessObjectConfig(((ReferenceFieldConfig) fieldConfig).getType());
            }
        }
    }

    private void validateParentConfig(DomainObjectConfig domainObjectConfig) {
        String parentConfig = domainObjectConfig.getParentConfig();
        if (parentConfig != null) {
            configurationExplorer.getBusinessObjectConfig(parentConfig);
        }
    }

}
