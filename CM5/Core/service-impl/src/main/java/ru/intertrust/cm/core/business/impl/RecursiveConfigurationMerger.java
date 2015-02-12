package ru.intertrust.cm.core.business.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.ColumnInfo;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdDao;
import ru.intertrust.cm.core.dao.api.SchemaCache;
import ru.intertrust.cm.core.dao.api.SqlLoggerEnforcer;
import ru.intertrust.cm.core.model.FatalException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
* Recursively merges configurations
*/
public class RecursiveConfigurationMerger extends AbstractRecursiveConfigurationLoader {

    private static final String COMMON_ERROR_MESSAGE = "It's only allowed to add some new configuration " +
            "but not to modify or delete the existing one.";

    @Autowired
    private DomainObjectTypeIdDao domainObjectTypeIdDao;
    @Autowired
    private SchemaCache schemaCache;
    @Autowired
    private FieldConfigChangeHandler fieldConfigChangeHandler;
    @Autowired
    private FieldConfigDbValidator fieldConfigDbValidator;
    @Autowired
    private SqlLoggerEnforcer sqlLoggerEnforcer;

    private ConfigurationExplorer oldConfigExplorer;

    /**
     * Recursively merges configurations from two instances of {@code ConfigurationExplorer}
     * @param oldConfigExplorer {@code ConfigurationExplorer} with old configuration
     * @param newConfigExplorer {@code ConfigurationExplorer} with new configuration
     */
    public void merge(ConfigurationExplorer oldConfigExplorer, ConfigurationExplorer newConfigExplorer) {
        this.oldConfigExplorer = oldConfigExplorer;
        setConfigurationExplorer(newConfigExplorer);

        Collection<DomainObjectTypeConfig> configList =
                configurationExplorer.getConfigs(DomainObjectTypeConfig.class);
        if(configList.isEmpty())  {
            return;
        }

        schemaCache.reset();
        processDeletedConfigurations();

        sqlLoggerEnforcer.forceSqlLogging();
        processConfigs(configList);
        sqlLoggerEnforcer.cancelSqlLoggingEnforcement();
    }

    @Override
    protected void doProcessConfig(DomainObjectTypeConfig domainObjectTypeConfig) {
        merge(domainObjectTypeConfig);
    }

    @Override
    protected void postProcessConfig(DomainObjectTypeConfig config) {
        DomainObjectTypeConfig oldConfig =
                oldConfigExplorer.getConfig(DomainObjectTypeConfig.class, config.getName());
        if (oldConfig == null) { // newly created type, nothing to merge, just create form scratch
            createAllConstraints(config);
            return;
        }

        if (!configurationExplorer.isAuditLogType(config.getName())) {
            List<ReferenceFieldConfig> newReferenceFieldConfigs = new ArrayList<>();
            List<UniqueKeyConfig> newUniqueKeyConfigs = new ArrayList<>();

            for (FieldConfig fieldConfig : config.getFieldConfigs()) {
                if (!(fieldConfig instanceof ReferenceFieldConfig)) {
                    continue;
                }

                FieldConfig oldFieldConfig = oldConfigExplorer.getFieldConfig(config.getName(),
                        fieldConfig.getName(), false);

                if (oldFieldConfig == null) {
                    newReferenceFieldConfigs.add((ReferenceFieldConfig) fieldConfig);
                }
            }

            for (UniqueKeyConfig uniqueKeyConfig : config.getUniqueKeyConfigs()) {
                if (!oldConfig.getUniqueKeyConfigs().contains(uniqueKeyConfig)) {
                    newUniqueKeyConfigs.add(uniqueKeyConfig);
                }
            }

            if (!newReferenceFieldConfigs.isEmpty() || !newUniqueKeyConfigs.isEmpty()) {
                dataStructureDao.createForeignKeyAndUniqueConstraints(config,
                        newReferenceFieldConfigs, newUniqueKeyConfigs);
            }
        }

    }

    protected void loadDomainObjectConfig(DomainObjectTypeConfig domainObjectTypeConfig) {
        super.loadDomainObjectConfig(domainObjectTypeConfig);
        createAclTablesFor(domainObjectTypeConfig);
    }

    private void merge(DomainObjectTypeConfig domainObjectTypeConfig) {
        DomainObjectTypeConfig oldDomainObjectTypeConfig =
                oldConfigExplorer.getConfig(DomainObjectTypeConfig.class, domainObjectTypeConfig.getName());

        if (oldDomainObjectTypeConfig == null) {
            loadDomainObjectConfig(domainObjectTypeConfig);
        } else if (!domainObjectTypeConfig.equals(oldDomainObjectTypeConfig)) {
            validateExtendsAttribute(domainObjectTypeConfig, oldDomainObjectTypeConfig);
            updateDomainObjectConfig(domainObjectTypeConfig, oldDomainObjectTypeConfig);
        }
    }

    private void updateDomainObjectConfig(DomainObjectTypeConfig domainObjectTypeConfig,
                                          DomainObjectTypeConfig oldDomainObjectTypeConfig) {
        Integer usedId = domainObjectTypeIdDao.findIdByName(domainObjectTypeConfig.getName());
        domainObjectTypeConfig.setId(usedId);

        processDependentConfigs(domainObjectTypeConfig);

        if (oldDomainObjectTypeConfig.getDbId() == null && domainObjectTypeConfig.getDbId() != null) {
            if (!domainObjectTypeConfig.getDbId().equals(usedId)) {
                throw new FatalException("Cannot update domain object type " + domainObjectTypeConfig.getName() +
                " because db-id different from specified " + domainObjectTypeConfig.getDbId() +
                        " is already in use");
            }
        }

        List<FieldConfig> newFieldConfigs = new ArrayList<>();

        for (FieldConfig fieldConfig : domainObjectTypeConfig.getFieldConfigs()) {
            FieldConfig oldFieldConfig = oldConfigExplorer.getFieldConfig(domainObjectTypeConfig.getName(),
                    fieldConfig.getName(), false);

            if (oldFieldConfig == null) {
                ColumnInfo columnInfo = schemaCache.getColumnInfo(domainObjectTypeConfig, fieldConfig);
                if (columnInfo == null) {
                    newFieldConfigs.add(fieldConfig);
                } else {
                    fieldConfigDbValidator.validate(fieldConfig, domainObjectTypeConfig, columnInfo);
                }
            } else if (!fieldConfig.equals(oldFieldConfig) &&
                    !configurationExplorer.isAuditLogType(domainObjectTypeConfig.getName())) {
                fieldConfigChangeHandler.handle(fieldConfig, oldFieldConfig, domainObjectTypeConfig, configurationExplorer);
            }
        }

        if (!newFieldConfigs.isEmpty()) {
            boolean isParent = isParentObject(domainObjectTypeConfig);
            dataStructureDao.updateTableStructure(domainObjectTypeConfig, newFieldConfigs, isParent);
        }

        List<IndexConfig> newIndices = new ArrayList<>();

        for (IndexConfig indexConfig : domainObjectTypeConfig.getIndicesConfig().getIndices()) {
            if (!oldDomainObjectTypeConfig.getIndicesConfig().getIndices().contains(indexConfig)) {
                newIndices.add(indexConfig);
            }
        }

        if (!newIndices.isEmpty()) {
            dataStructureDao.createIndices(domainObjectTypeConfig, newIndices);
        }

        List<IndexConfig> indicesToDelete = new ArrayList<>();
        for (IndexConfig oldIndexConfig : oldDomainObjectTypeConfig.getIndicesConfig().getIndices()) {
            if (!domainObjectTypeConfig.getIndicesConfig().getIndices().contains(oldIndexConfig)) {
                indicesToDelete.add(oldIndexConfig);
            }
        }

        if (!indicesToDelete.isEmpty()) {
            dataStructureDao.deleteIndices(domainObjectTypeConfig, indicesToDelete);
        }

    }

    private void processDeletedConfigurations() {
        for (DomainObjectTypeConfig oldDOTypeConfig : oldConfigExplorer.getConfigs(DomainObjectTypeConfig.class)) {
            DomainObjectTypeConfig domainObjectTypeConfig =
                    configurationExplorer.getConfig(DomainObjectTypeConfig.class, oldDOTypeConfig.getName());
            if (domainObjectTypeConfig == null) {
                continue;
            }

            if (!oldDOTypeConfig.getName().endsWith(Configuration.AUDIT_LOG_SUFFIX)) {
                for (FieldConfig oldFieldConfig : oldDOTypeConfig.getFieldConfigs()) {
                    FieldConfig fieldConfig = configurationExplorer.getFieldConfig(oldDOTypeConfig.getName(),
                            oldFieldConfig.getName(), false);
                    if (fieldConfig == null) {
                        ColumnInfo columnInfo = schemaCache.getColumnInfo(oldDOTypeConfig, oldFieldConfig);
                        if (columnInfo != null && columnInfo.isNotNull()) {
                            dataStructureDao.setColumnNullable(oldDOTypeConfig, oldFieldConfig);
                        }
                    }
                }
            }

            for (UniqueKeyConfig oldUniqueKeyConfig : oldDOTypeConfig.getUniqueKeyConfigs()) {
                if (!domainObjectTypeConfig.getUniqueKeyConfigs().contains(oldUniqueKeyConfig)) {
                    String uniqueKeyName = schemaCache.getUniqueKeyName(oldDOTypeConfig, oldUniqueKeyConfig);
                    if (uniqueKeyName != null) {
                        dataStructureDao.dropConstraint(oldDOTypeConfig, uniqueKeyName);
                    }
                }
            }
        }
    }

    private void validateExtendsAttribute(DomainObjectTypeConfig domainObjectTypeConfig,
                                          DomainObjectTypeConfig oldDomainObjectTypeConfig) {
        String extendsAttributeValue = domainObjectTypeConfig.getExtendsAttribute();
        String oldExtendsAttributeValue = oldDomainObjectTypeConfig.getExtendsAttribute();

        if ((extendsAttributeValue == null && oldExtendsAttributeValue != null) ||
                (extendsAttributeValue != null && !extendsAttributeValue.equals(oldExtendsAttributeValue))) {
            throw new ConfigurationException("Configuration loading aborted: 'extends' attribute was changed " +
                    "for '" + domainObjectTypeConfig.getName() + ". " + COMMON_ERROR_MESSAGE);
        }
    }
}
