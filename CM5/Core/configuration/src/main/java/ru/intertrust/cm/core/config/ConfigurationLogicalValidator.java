package ru.intertrust.cm.core.config;

import ru.intertrust.cm.core.config.model.*;

import java.util.Collection;

/**
 * Логически Валидирует конфигурацию
 * User: atsvetkov Date: 17.05.13 Time: 13:52
 */
public class ConfigurationLogicalValidator {

    private ConfigurationExplorer configurationExplorer;

    public ConfigurationLogicalValidator(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    /**
     * Выполняет логическую валидацию конфигурации
     */
    public void validate() {
        Collection<DomainObjectConfig> configList = configurationExplorer.getDomainObjectConfigs();
        if (configList.isEmpty()) {
            return;
        }
        for (DomainObjectConfig config : configList) {
            validateDomainObjectConfig(config);
        }
        // TODO Log success information using logging API
        System.out.println("Document has passed logical validation");
    }

    private void validateDomainObjectConfig(DomainObjectConfig domainObjectConfig) {
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
                validateDomainObjectConfigContainsField(domainObjectConfig, uniqueKeyFieldConfig.getName());
            }
        }
    }

    private void validateDomainObjectConfigContainsField(DomainObjectConfig domainObjectConfig,
                                                         String fieldName) {
        for(FieldConfig fieldConfig : domainObjectConfig.getFieldConfigs()) {
            if(fieldName.equals(fieldConfig.getName())) {
                return;
            }
        }
        throw new RuntimeException("FieldConfig with name " + fieldName + " is not found in domain object '" + domainObjectConfig.getName() + "'");
    }

    private void validateReferenceFields(DomainObjectConfig domainObjectConfig) {
        for (FieldConfig fieldConfig : domainObjectConfig.getFieldConfigs()) {
            if (ReferenceFieldConfig.class.equals(fieldConfig.getClass())) {
                configurationExplorer.getDomainObjectConfig(((ReferenceFieldConfig) fieldConfig).getType());
            }
        }
    }

    private void validateParentConfig(DomainObjectConfig domainObjectConfig) {
        String parentConfig = domainObjectConfig.getParentConfig();
        if (parentConfig != null) {
            configurationExplorer.getDomainObjectConfig(parentConfig);
        }
    }

}
