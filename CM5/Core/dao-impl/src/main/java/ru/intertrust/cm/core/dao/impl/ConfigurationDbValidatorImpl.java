package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.ColumnInfo;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.dao.api.ConfigurationDbValidator;
import ru.intertrust.cm.core.dao.api.SchemaCache;

import java.util.Collection;

/**
 * Проверяет соответсвие базы данных конфигурации
 */
public class ConfigurationDbValidatorImpl implements ConfigurationDbValidator {

    @Autowired
    private ConfigurationExplorer configExplorer;
    @Autowired
    private SchemaCache schemaCache;
    @Autowired
    private FieldConfigDbValidatorImpl fieldConfigDbValidator;

    /**
     * Проверяет соответсвие базы данных конфигурации
     */
    @Override
    public void validate() {
        Collection<DomainObjectTypeConfig> domainObjectTypeConfigs = configExplorer.getConfigs(DomainObjectTypeConfig.class);
        if (domainObjectTypeConfigs == null) {
            return;
        }

        schemaCache.reset();

        for (DomainObjectTypeConfig domainObjectTypeConfig : domainObjectTypeConfigs) {
            if (domainObjectTypeConfig.isTemplate() || configExplorer.isAuditLogType(domainObjectTypeConfig.getName())) {
                continue;
            }

            if (!schemaCache.isTableExist(domainObjectTypeConfig)) {
                throw new ConfigurationValidationException("Validation against DB failed for DO type " +
                        domainObjectTypeConfig.getName() + ". It doesn't exist");
            }

            for (FieldConfig fieldConfig : domainObjectTypeConfig.getFieldConfigs()) {
                ColumnInfo columnInfo = schemaCache.getColumnInfo(domainObjectTypeConfig, fieldConfig);
                if (columnInfo == null) {
                    throw new ConfigurationValidationException("Validation against DB failed for " +
                            domainObjectTypeConfig.getName() + "." + fieldConfig.getName() + ". It doesn't exist");
                }

                fieldConfigDbValidator.validate(fieldConfig, domainObjectTypeConfig, columnInfo);
            }
        }
    }
}
