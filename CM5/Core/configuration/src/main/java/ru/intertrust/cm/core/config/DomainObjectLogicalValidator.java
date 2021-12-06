package ru.intertrust.cm.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Логически Валидирует конфигурацию
 * User: atsvetkov Date: 17.05.13 Time: 13:52
 */
public class DomainObjectLogicalValidator implements ConfigurationValidator {

    final static Logger logger = LoggerFactory.getLogger(DomainObjectLogicalValidator.class);

    private ConfigurationExplorer configurationExplorer;
    private List<LogicalErrors> logicalErrorsList = new ArrayList<>();

    public DomainObjectLogicalValidator(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    /**
     * Выполняет логическую валидацию конфигурации
     */
    public List<LogicalErrors> validate() {
        Collection<DomainObjectTypeConfig> configList = configurationExplorer.getConfigs(DomainObjectTypeConfig.class);

        if (configList.isEmpty()) {
            return logicalErrorsList;
        }

        for (DomainObjectTypeConfig config : configList) {
            LogicalErrors logicalErrors = validateDomainObjectConfig(config);
            if (logicalErrors != null && logicalErrors.getErrorCount() > 0) {
                logicalErrorsList.add(logicalErrors);
            }
        }

        return logicalErrorsList;
    }

    private LogicalErrors validateDomainObjectConfig(DomainObjectTypeConfig domainObjectTypeConfig) {
        if (domainObjectTypeConfig == null) {
            return null;
        }

        LogicalErrors logicalErrors = LogicalErrors.getInstance(domainObjectTypeConfig.getName(), "domain-object");

        validateExtendsAttribute(domainObjectTypeConfig, logicalErrors);
        validateReferenceFields(domainObjectTypeConfig, logicalErrors);
        validateUniqueKeys(domainObjectTypeConfig, logicalErrors);
        validateIndices(domainObjectTypeConfig, logicalErrors);

        return logicalErrors;
    }

    private void validateUniqueKeys(DomainObjectTypeConfig domainObjectTypeConfig, LogicalErrors logicalErrors) {
        for (UniqueKeyConfig uniqueKeyConfig : domainObjectTypeConfig.getUniqueKeyConfigs()) {
            for (UniqueKeyFieldConfig uniqueKeyFieldConfig : uniqueKeyConfig.getUniqueKeyFieldConfigs()) {
                validateDomainObjectConfigContainsField(domainObjectTypeConfig, uniqueKeyFieldConfig.getName(), logicalErrors);
            }
        }
    }

    private void validateIndices(DomainObjectTypeConfig domainObjectTypeConfig, LogicalErrors logicalErrors) {
        if (domainObjectTypeConfig.getIndicesConfig() != null) {
            for (IndexConfig indexConfig : domainObjectTypeConfig.getIndicesConfig().getIndices()) {
                for (BaseIndexExpressionConfig indexExpression : indexConfig.getIndexFieldConfigs()) {
                    if (indexExpression instanceof IndexFieldConfig) {
                        validateDomainObjectConfigContainsField(domainObjectTypeConfig, ((IndexFieldConfig) indexExpression).getName(), logicalErrors);
                    }
                }
            }
        }
    }

    private void validateDomainObjectConfigContainsField(DomainObjectTypeConfig domainObjectTypeConfig,
                                                         String fieldName, LogicalErrors logicalErrors) {
        for (FieldConfig fieldConfig : domainObjectTypeConfig.getFieldConfigs()) {
            if (fieldName.equalsIgnoreCase(fieldConfig.getName())) {
                return;
            }
        }

        for (FieldConfig fieldConfig : domainObjectTypeConfig.getSystemFieldConfigs()) {
            if (fieldName.equalsIgnoreCase(fieldConfig.getName())) {
                return;
            }
        }

        List<IncludeFieldGroupConfig> includeFieldGroupConfigs =
                domainObjectTypeConfig.getDomainObjectFieldsConfig().getIncludeGroups();

        if (includeFieldGroupConfigs != null) {
            for (IncludeFieldGroupConfig includeFieldGroupConfig : includeFieldGroupConfigs){
                DomainObjectFieldGroupConfig fieldGroupConfig =
                        configurationExplorer.getConfig(DomainObjectFieldGroupConfig.class, includeFieldGroupConfig.getName());

                if (fieldGroupConfig.getFieldConfigs() == null) {
                    continue;
                }

                for (FieldConfig fieldConfig : fieldGroupConfig.getFieldConfigs()) {
                    if (fieldName.equalsIgnoreCase(fieldConfig.getName())) {
                        return;
                    }
                }
            }
        }

        logicalErrors.addError("FieldConfig with name '" + fieldName + "' is not found");
    }

    private void validateReferenceFields(DomainObjectTypeConfig domainObjectTypeConfig, LogicalErrors logicalErrors) {
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
                logicalErrors.addError("Definition is not found for '" + referenceFieldConfig.getType() + "'");
            }
        }
    }

    private void validateExtendsAttribute(DomainObjectTypeConfig domainObjectTypeConfig, LogicalErrors logicalErrors) {
        String extendsAttributeValue = domainObjectTypeConfig.getExtendsAttribute();
        if (extendsAttributeValue != null) {
            DomainObjectTypeConfig extendedConfig =
                    configurationExplorer.getConfig(DomainObjectTypeConfig.class, extendsAttributeValue);

            if (extendedConfig == null) {
                logicalErrors.addError("Extended DomainObject Configuration is not found for name '" +
                        extendsAttributeValue + "'");
                return;
            }

            validateForCoincidentFieldNamesInHierarchy(domainObjectTypeConfig, extendedConfig, logicalErrors);
        }
    }

    private void validateForCoincidentFieldNamesInHierarchy(DomainObjectTypeConfig config,
                                                            DomainObjectTypeConfig parentConfig, LogicalErrors logicalErrors) {
        for (FieldConfig fieldConfig : config.getFieldConfigs()) {
            FieldConfig parentFieldConfig = configurationExplorer.getFieldConfig(parentConfig.getName(),
                    fieldConfig.getName());

            if (parentFieldConfig != null) {
                logicalErrors.addError("FieldConfig with name '" + fieldConfig.getName() +
                        "' is already used in some ancestor");
            }
        }
    }
}


