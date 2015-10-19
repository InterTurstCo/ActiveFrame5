package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.ColumnInfo;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.dao.api.FieldConfigDbValidator;
import ru.intertrust.cm.core.dao.api.SchemaCache;

/**
 * Проверяет соответствие конфигурации поля типа ДО соотв. колонке в базе
 */
public class FieldConfigDbValidatorImpl implements FieldConfigDbValidator {

    @Autowired
    private SchemaCache schemaCache;

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    /**
     * Проверяет соответствие конфигурации поля типа ДО соотв. колонке в базе
     */
    @Override
    public void validate(FieldConfig fieldConfig, DomainObjectTypeConfig domainObjectTypeConfig, ColumnInfo columnInfo) {
        validateBasicAttributes(fieldConfig, domainObjectTypeConfig, columnInfo);

        if (fieldConfig instanceof StringFieldConfig) {
            validate((StringFieldConfig) fieldConfig, domainObjectTypeConfig, columnInfo);
        } else if (fieldConfig instanceof PasswordFieldConfig) {
            validate((PasswordFieldConfig) fieldConfig, domainObjectTypeConfig, columnInfo);
        } else if (fieldConfig instanceof ReferenceFieldConfig) {
            validate((ReferenceFieldConfig) fieldConfig, domainObjectTypeConfig, columnInfo);
        } else if (fieldConfig instanceof DecimalFieldConfig) {
            validate((DecimalFieldConfig) fieldConfig, domainObjectTypeConfig, columnInfo);
        }
    }

    private void validate(StringFieldConfig fieldConfig, DomainObjectTypeConfig domainObjectTypeConfig, ColumnInfo columnInfo) {
        if (fieldConfig.getLength() != columnInfo.getLength()) {
            throw new ConfigurationValidationException("Validation against DB failed for field '" +
                    domainObjectTypeConfig.getName() + "." + fieldConfig.getName() +
                    "' because such column already exists but has a different length property '" +
                    columnInfo.getLength() + "'");
        }
    }

    private void validate(PasswordFieldConfig fieldConfig, DomainObjectTypeConfig domainObjectTypeConfig, ColumnInfo columnInfo) {
        if (fieldConfig.getLength() != columnInfo.getLength()) {
            throw new ConfigurationValidationException("Validation against DB failed for field '" +
                    domainObjectTypeConfig.getName() + "." + fieldConfig.getName() +
                    "' because such column already exists but has a different length property '" +
                    columnInfo.getLength() + "'");
        }
    }

    private void validate(ReferenceFieldConfig fieldConfig, DomainObjectTypeConfig domainObjectTypeConfig, ColumnInfo columnInfo) {
        if (!schemaCache.isReferenceColumnExist(domainObjectTypeConfig, fieldConfig)) {
            throw new ConfigurationValidationException("Validation against DB failed for field '" +
                    domainObjectTypeConfig.getName() + "." + fieldConfig.getName() +
                    "' because required reference type column doesn't exist");
        }

        if (fieldConfig.getType().equals(ConfigurationExplorer.REFERENCE_TYPE_ANY) ||
                configurationExplorer.isAuditLogType(domainObjectTypeConfig.getName())) {
            return;
        }

        String foreignKeyName = schemaCache.getForeignKeyName(domainObjectTypeConfig, fieldConfig);
        if (foreignKeyName == null) {
            throw new ConfigurationValidationException("Validation against DB failed for field '" +
                    domainObjectTypeConfig.getName() + "." + fieldConfig.getName() +
                    "' because required foreign key doesn't exist");
        }
    }

    private void validate(DecimalFieldConfig fieldConfig, DomainObjectTypeConfig domainObjectTypeConfig, ColumnInfo columnInfo) {
        if ((fieldConfig.getPrecision() == null && columnInfo.getPrecision() != null) ||
                !fieldConfig.getPrecision().equals(columnInfo.getPrecision())) {
            if (fieldConfig.isNotNull() != columnInfo.isNotNull()) {
                throw new ConfigurationValidationException("Validation against DB failed for field '" +
                        domainObjectTypeConfig.getName() + "." + fieldConfig.getName() +
                        "' because such column already exists but has a different precision property '" +
                        columnInfo.isNotNull() + "'");
            }
        }

        if ((fieldConfig.getScale() == null && columnInfo.getScale() != null) ||
                (!fieldConfig.getScale().equals(columnInfo.getScale()))) {
            if (fieldConfig.isNotNull() != columnInfo.isNotNull()) {
                throw new ConfigurationValidationException("Validation against DB failed for field '" +
                        domainObjectTypeConfig.getName() + "." + fieldConfig.getName() +
                        "' because such column already exists but has a different scale property '" +
                        columnInfo.isNotNull() + "'");
            }
        }
    }

    private void validateBasicAttributes(FieldConfig fieldConfig, DomainObjectTypeConfig domainObjectTypeConfig, ColumnInfo columnInfo) {
        if (fieldConfig.isNotNull() != columnInfo.isNotNull()) {
            throw new ConfigurationValidationException("Validation against DB failed for field '" +
                    domainObjectTypeConfig.getName() + "." + fieldConfig.getName() +
                    "' because such column already exists but has a different not-null property '" +
                    columnInfo.isNotNull() + "'");
        }

        for (UniqueKeyConfig uniqueKeyConfig : domainObjectTypeConfig.getUniqueKeyConfigs()) {
            for (UniqueKeyFieldConfig uniqueKeyFieldConfig : uniqueKeyConfig.getUniqueKeyFieldConfigs()) {
                if (!uniqueKeyFieldConfig.getName().equalsIgnoreCase(fieldConfig.getName())) {
                    continue;
                }

                String uniqueKeyConfigName = schemaCache.getUniqueKeyName(domainObjectTypeConfig, uniqueKeyConfig);
                if (uniqueKeyConfigName != null) {
                    continue;
                }

                StringBuilder message = generateUniqueKeyNotFoundExceptionMessage(fieldConfig, domainObjectTypeConfig, uniqueKeyConfig);
                throw new ConfigurationValidationException(message.toString());
            }
        }

        for (IndexConfig indexConfig : domainObjectTypeConfig.getIndicesConfig().getIndices()) {
            for (BaseIndexExpressionConfig baseIndexExpressionConfig : indexConfig.getIndexFieldConfigs()) {
                if (!(baseIndexExpressionConfig instanceof IndexFieldConfig)) {
                    continue;
                }

                IndexFieldConfig indexFieldConfig = (IndexFieldConfig) baseIndexExpressionConfig;

                if (!indexFieldConfig.getName().equalsIgnoreCase(fieldConfig.getName())) {
                    continue;
                }

                String indexName = schemaCache.getIndexName(domainObjectTypeConfig, indexConfig);
                if (indexName != null) {
                    continue;
                }

                StringBuilder message = generateIndexNotFoundExceptionMessage(fieldConfig, domainObjectTypeConfig, indexConfig);
                throw new ConfigurationValidationException(message.toString());
            }
        }
    }

    private StringBuilder generateUniqueKeyNotFoundExceptionMessage(FieldConfig fieldConfig, DomainObjectTypeConfig domainObjectTypeConfig, UniqueKeyConfig uniqueKeyConfig) {
        StringBuilder message = new StringBuilder();
        message.append("Validation against DB failed for field '").
                append(domainObjectTypeConfig.getName()).append(".").append(fieldConfig.getName()).
                append("' because required unique key doesn't exist. The required unique key fields: ");

        for (UniqueKeyFieldConfig uniqueKeyFieldConfig2 : uniqueKeyConfig.getUniqueKeyFieldConfigs()) {
            message.append(uniqueKeyFieldConfig2.getName()).append(", ");
        }

        message.delete(message.length() - 2, message.length() - 1);
        return message;
    }

    private StringBuilder generateIndexNotFoundExceptionMessage(FieldConfig fieldConfig, DomainObjectTypeConfig domainObjectTypeConfig, IndexConfig indexConfig) {
        StringBuilder message = new StringBuilder();

        message.append("Validation against DB failed for field '").
                append(domainObjectTypeConfig.getName()).append(".").append(fieldConfig.getName()).
                append("' because required index doesn't exist. The required index fields: ");

        for (BaseIndexExpressionConfig baseIndexExpressionConfig : indexConfig.getIndexFieldConfigs()) {
            if (!(baseIndexExpressionConfig instanceof IndexFieldConfig)) {
                continue;
            }

            IndexFieldConfig indexFieldConfig = (IndexFieldConfig) baseIndexExpressionConfig;
            message.append(indexFieldConfig.getName()).append(", ");
        }

        message.delete(message.length() - 2, message.length() - 1);
        return message;
    }
}
