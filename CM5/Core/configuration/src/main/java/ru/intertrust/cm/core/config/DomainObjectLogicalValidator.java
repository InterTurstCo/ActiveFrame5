package ru.intertrust.cm.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Логически Валидирует конфигурацию
 * User: atsvetkov Date: 17.05.13 Time: 13:52
 */
public class DomainObjectLogicalValidator {

    final static Logger logger = LoggerFactory.getLogger(DomainObjectLogicalValidator.class);

    private ConfigurationExplorer configurationExplorer;

    public DomainObjectLogicalValidator(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    /**
     * Выполняет логическую валидацию конфигурации
     */
    public void validate() {
        Collection<DomainObjectTypeConfig> configList =
                configurationExplorer.getConfigs(DomainObjectTypeConfig.class);
        if (configList.isEmpty()) {
            return;
        }
        for (DomainObjectTypeConfig config : configList) {
            validateDomainObjectConfig(config);
        }

        logger.info("Document has passed logical validation");
    }

    private void validateDomainObjectConfig(DomainObjectTypeConfig domainObjectTypeConfig) {
        if (domainObjectTypeConfig == null) {
            return;
        }

        validateExtendsAttribute(domainObjectTypeConfig);
        validateReferenceFields(domainObjectTypeConfig);
        validateUniqueKeys(domainObjectTypeConfig);
    }

    private void validateUniqueKeys(DomainObjectTypeConfig domainObjectTypeConfig) {
        for (UniqueKeyConfig uniqueKeyConfig : domainObjectTypeConfig.getUniqueKeyConfigs()) {
            for (UniqueKeyFieldConfig uniqueKeyFieldConfig : uniqueKeyConfig.getUniqueKeyFieldConfigs()) {
                validateDomainObjectConfigContainsField(domainObjectTypeConfig, uniqueKeyFieldConfig.getName());
            }
        }
    }

    private void validateDomainObjectConfigContainsField(DomainObjectTypeConfig domainObjectTypeConfig,
                                                         String fieldName) {
        for(FieldConfig fieldConfig : domainObjectTypeConfig.getFieldConfigs()) {
            if(fieldName.equals(fieldConfig.getName())) {
                return;
            }
        }
        throw new ConfigurationException("FieldConfig with name '" + fieldName + "' is not found in domain object '" +
                domainObjectTypeConfig.getName() + "'");
    }

    private void validateReferenceFields(DomainObjectTypeConfig domainObjectTypeConfig) {
        for (FieldConfig fieldConfig : domainObjectTypeConfig.getFieldConfigs()) {
            if (!ReferenceFieldConfig.class.equals(fieldConfig.getClass())) {
                continue;
            }

            ReferenceFieldConfig referenceFieldConfig  = (ReferenceFieldConfig) fieldConfig;

            if (ConfigurationExplorer.REFERENCE_TYPE_ANY.equals(referenceFieldConfig.getType())) {
                continue;
            }

            DomainObjectTypeConfig referencedConfig =
                    configurationExplorer.getConfig(DomainObjectTypeConfig.class, referenceFieldConfig.getType());
            if (referencedConfig == null) {
                throw new ConfigurationException("Definition is not found for '" + referenceFieldConfig.getType() +
                        "' referenced from '" + domainObjectTypeConfig.getName() + "'");
            }
        }
    }

    private void validateExtendsAttribute(DomainObjectTypeConfig domainObjectTypeConfig) {
        String extendsAttributeValue = domainObjectTypeConfig.getExtendsAttribute();
        if (extendsAttributeValue != null) {
            DomainObjectTypeConfig extendedConfig =
                    configurationExplorer.getConfig(DomainObjectTypeConfig.class, extendsAttributeValue);

            if (extendedConfig == null) {
                throw new ConfigurationException("Extended DomainObject Configuration is not found for name '" +
                        extendsAttributeValue + "'");
            }

            validateForCoincidentFieldNamesInHierarchy(domainObjectTypeConfig, extendedConfig);
        }
    }

    private void validateForCoincidentFieldNamesInHierarchy(DomainObjectTypeConfig config, DomainObjectTypeConfig parentConfig) {
        for (FieldConfig fieldConfig : config.getFieldConfigs()) {
            FieldConfig parentFieldConfig = configurationExplorer.getFieldConfig(parentConfig.getName(),
                    fieldConfig.getName());

            if (parentFieldConfig != null) {
                throw new ConfigurationException("FieldConfig with name '" + fieldConfig.getName() + "' is already " +
                        "used in some inherited DomainObjectTypeConfig of '" + config.getName() + "'");
            }
        }
    }
}


