package ru.intertrust.cm.core.business.impl;

import ru.intertrust.cm.core.config.*;

import java.util.List;

/**
 * Логически Валидирует конфигурацию бизнес-объектов
 * User: atsvetkov Date: 17.05.13 Time: 13:52
 */
public class BusinessObjectsConfigurationLogicalValidator {

    private ConfigurationExplorer configurationExplorer;

    public BusinessObjectsConfigurationLogicalValidator() {
    }

    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    /**
     * Выполняет валидацию конфигурации на предмет соответствия XSD схеме и логическую валидацию.
     */
    public void validate() {
        List<BusinessObjectConfig> businessObjectConfigs = configurationExplorer.getBusinessObjectsConfiguration().getBusinessObjectConfigs();
        if (businessObjectConfigs.isEmpty()) {
            return;
        }
        for (BusinessObjectConfig businessObjectConfig : businessObjectConfigs) {
            validateBusinessObjectConfig(businessObjectConfig);
        }
        // TODO Log success information using logging API
        System.out.println("Document has passed logical validation");
    }

    private void validateBusinessObjectConfig(BusinessObjectConfig businessObjectConfig) {
        if (businessObjectConfig == null) {
            return;
        }

        validateParentConfig(businessObjectConfig);
        validateReferenceFields(businessObjectConfig);
        validateUniqueKeys(businessObjectConfig);
    }

    private void validateUniqueKeys(BusinessObjectConfig businessObjectConfig) {
        for (UniqueKeyConfig uniqueKeyConfig : businessObjectConfig.getUniqueKeyConfigs()) {
            for (UniqueKeyFieldConfig uniqueKeyFieldConfig : uniqueKeyConfig.getUniqueKeyFieldConfigs()) {
                validateBusinessObjectConfigContainsField(businessObjectConfig, uniqueKeyFieldConfig.getName());
            }
        }
    }

    private void validateBusinessObjectConfigContainsField(BusinessObjectConfig businessObjectConfig,
                                                          String fieldName) {
        for(FieldConfig fieldConfig : businessObjectConfig.getFieldConfigs()) {
            if(fieldName.equals(fieldConfig.getName())) {
                return;
            }
        }
        throw new RuntimeException("FieldConfig with name " + fieldName + " is not found in business object '" + businessObjectConfig.getName() + "'");
    }

    private void validateReferenceFields(BusinessObjectConfig businessObjectConfig) {
        for (FieldConfig fieldConfig : businessObjectConfig.getFieldConfigs()) {
            if (ReferenceFieldConfig.class.equals(fieldConfig.getClass())) {
                configurationExplorer.getBusinessObjectConfig(((ReferenceFieldConfig) fieldConfig).getType());
            }
        }
    }

    private void validateParentConfig(BusinessObjectConfig businessObjectConfig) {
        String parentConfig = businessObjectConfig.getParentConfig();
        if (parentConfig != null) {
            configurationExplorer.getBusinessObjectConfig(parentConfig);
        }
    }

}
