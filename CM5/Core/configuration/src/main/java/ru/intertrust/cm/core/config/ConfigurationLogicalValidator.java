package ru.intertrust.cm.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.intertrust.cm.core.config.model.*;

import java.util.Collection;

/**
 * Логически Валидирует конфигурацию
 * User: atsvetkov Date: 17.05.13 Time: 13:52
 */
public class ConfigurationLogicalValidator {

    final static Logger logger = LoggerFactory.getLogger(ConfigurationLogicalValidator.class);

    private ConfigurationExplorer configurationExplorer;

    public ConfigurationLogicalValidator(ConfigurationExplorer configurationExplorer) {
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
        validateParentConfig(domainObjectTypeConfig);
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

            for (ReferenceFieldTypeConfig typeConfig : referenceFieldConfig.getTypes()) {
                String referencedDomainObjectConfigName = typeConfig.getName();
                DomainObjectTypeConfig referencedConfig =
                        configurationExplorer.getConfig(DomainObjectTypeConfig.class,
                                referencedDomainObjectConfigName);
                if (referencedConfig == null) {
                    throw new ConfigurationException("Definition is not found for '" + referencedDomainObjectConfigName +
                            "' referenced from '" + domainObjectTypeConfig.getName() + "'");
                }
            }
        }
    }

    private void validateExtendsAttribute(DomainObjectTypeConfig domainObjectTypeConfig) {
        String extendsAttributeValue = domainObjectTypeConfig.getExtendsAttribute();

        if (extendsAttributeValue == null) {
            return;
        }

        DomainObjectTypeConfig parentConfig =
                configurationExplorer.getConfig(DomainObjectTypeConfig.class, extendsAttributeValue);

        if (parentConfig == null) {
            throw new ConfigurationException("Extended DomainObject Configuration is not found for name '" +
                    extendsAttributeValue + "'");
        }

        validateForCoincidentFieldNamesInHierarchy(domainObjectTypeConfig, parentConfig);
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

    private void validateParentConfig(DomainObjectTypeConfig domainObjectTypeConfig) {
        DomainObjectParentConfig parentConfig = domainObjectTypeConfig.getParentConfig();
        if (parentConfig == null) {
            return;
        }

        String parentConfigName = parentConfig.getName();
        DomainObjectTypeConfig parentDomainObjectTypeConfig =
                configurationExplorer.getConfig(DomainObjectTypeConfig.class, parentConfigName);
        if (parentDomainObjectTypeConfig == null) {
            throw new ConfigurationException("Parent DomainObject Configuration is not found for name '" +
                    parentConfigName + "'");
        }
    }

}
