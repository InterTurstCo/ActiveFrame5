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
        Collection<DomainObjectTypeConfig> configList = configurationExplorer.getDomainObjectConfigs();
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
            if (ReferenceFieldConfig.class.equals(fieldConfig.getClass())) {
                String referencedDomainObjectConfigName = ((ReferenceFieldConfig) fieldConfig).getType();
                DomainObjectTypeConfig referencedConfig =
                        configurationExplorer.getDomainObjectTypeConfig(referencedDomainObjectConfigName);
                if(referencedConfig == null) {
                    throw new ConfigurationException("Referenced DomainObject Configuration is not found for name '" +
                            referencedDomainObjectConfigName + "'");
                }
            }
        }
    }

    private void validateParentConfig(DomainObjectTypeConfig domainObjectTypeConfig) {
        String parentConfigName = domainObjectTypeConfig.getExtendsAttribute();
        if (parentConfigName != null) {
            DomainObjectTypeConfig parentConfig = configurationExplorer.getDomainObjectTypeConfig(parentConfigName);
            if(parentConfig == null) {
                throw new ConfigurationException("Parent DomainObject Configuration is not found for name '" + parentConfigName + "'");
            }
        }
    }

}
