package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.ColumnInfo;
import ru.intertrust.cm.core.business.api.dto.IndexInfo;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.dao.api.ConfigurationDbValidator;
import ru.intertrust.cm.core.dao.api.DataStructureDao;
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
    @Autowired
    private DataStructureDao dataStructureDao;

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

            if (isIndexErrorsFound(domainObjectTypeConfig)) { // Recreate indexes if there are errors
                dataStructureDao.deleteIndices(domainObjectTypeConfig, schemaCache.getIndexNames(domainObjectTypeConfig));

                boolean isParentType = DomainObjectTypeUtility.isParentObject(domainObjectTypeConfig, configExplorer);
                dataStructureDao.createTableIndices(domainObjectTypeConfig, isParentType);
            }
        }
    }

    private boolean isIndexErrorsFound(DomainObjectTypeConfig domainObjectTypeConfig) {
        // Check missing indexes
        for (IndexConfig indexConfig : domainObjectTypeConfig.getIndicesConfig().getIndices()) {
            // Skip check for expression type indices because search by fields doesn't work for them
            if (isExpressionIndex(indexConfig)) {
                continue;
            }

            String indexName = schemaCache.getIndexName(domainObjectTypeConfig, indexConfig);
            if (indexName == null) {
                return true;
            }
        }

        // Check duplicated indexes
        Collection<IndexInfo> indices = schemaCache.getIndices(domainObjectTypeConfig);
        for (IndexInfo indexInfo :indices) {
            for (IndexInfo indexInfo2 :indices) {
                if (!indexInfo.getName().equals(indexInfo2.getName()) &&
                        indexInfo.getColumnNames().equals(indexInfo2.getColumnNames())) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isExpressionIndex(IndexConfig indexConfig) {
        if (indexConfig.getIndexFieldConfigs() == null || indexConfig.getIndexFieldConfigs().isEmpty()) {
            return false;
        }

        for (BaseIndexExpressionConfig indexExpressionConfig : indexConfig.getIndexFieldConfigs()) {
            if (indexExpressionConfig instanceof  IndexExpressionConfig) {
                return true;
            }
        }

        return false;
    }
}
