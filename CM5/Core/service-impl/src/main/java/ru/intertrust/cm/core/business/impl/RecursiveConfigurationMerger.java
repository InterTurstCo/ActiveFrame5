package ru.intertrust.cm.core.business.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.ColumnInfo;
import ru.intertrust.cm.core.business.api.dto.ColumnInfoConverter;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.dao.api.*;
import ru.intertrust.cm.core.dao.impl.FieldConfigChangeHandler;
import ru.intertrust.cm.core.model.FatalException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

import static ru.intertrust.cm.core.config.DomainObjectTypeUtility.getSourceDomainObjectType;
import static ru.intertrust.cm.core.config.DomainObjectTypeUtility.isParentObject;

/**
* Recursively merges configurations
* Designed as prototype, not thread-safe, instances are not reusable!!!
*/
public class RecursiveConfigurationMerger extends AbstractRecursiveConfigurationLoader {

    private static final Logger logger = LoggerFactory.getLogger(RecursiveConfigurationMerger.class);

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
    public boolean merge(ConfigurationExplorer oldConfigExplorer, ConfigurationExplorer newConfigExplorer) {
        this.oldConfigExplorer = oldConfigExplorer;
        setConfigurationExplorer(newConfigExplorer);
        setSchemaUpdateDone(false);

        Collection<DomainObjectTypeConfig> configList =
                configurationExplorer.getConfigs(DomainObjectTypeConfig.class);
        if (configList.isEmpty())  {
            return false;
        }

        schemaCache.reset();
        sqlLoggerEnforcer.forceSqlLogging();

        processDeletedConfigurations();
        processConfigs(configList);

        sqlLoggerEnforcer.cancelSqlLoggingEnforcement();

        return isSchemaUpdateDone();
    }

    @Override
    protected void doProcessConfig(DomainObjectTypeConfig domainObjectTypeConfig) {
        merge(domainObjectTypeConfig);
    }

    @Override
    protected void postProcessConfig(DomainObjectTypeConfig config) {
        DomainObjectTypeConfig oldConfig =
                oldConfigExplorer.getConfig(DomainObjectTypeConfig.class, config.getName());
        if (oldConfig == null && !schemaCache.isTableExist(config)) { // newly created type, nothing to merge, just create form scratch
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
                    if (schemaCache.getForeignKeyName(config, (ReferenceFieldConfig) fieldConfig) == null) {
                        newReferenceFieldConfigs.add((ReferenceFieldConfig) fieldConfig);
                    }
                }
            }

            if (!config.isTemplate() && isParentObject(config, configurationExplorer)) {
                for (FieldConfig fieldConfig : config.getSystemFieldConfigs()) {
                    if (!(fieldConfig instanceof ReferenceFieldConfig)
                            || ((ReferenceFieldConfig) fieldConfig).getType() == null
                            || fieldConfig.getName().equalsIgnoreCase(DomainObjectDao.ACCESS_OBJECT_ID)) {
                        continue;
                    }

                    if (schemaCache.getForeignKeyName(config, (ReferenceFieldConfig) fieldConfig) == null) {
                        newReferenceFieldConfigs.add((ReferenceFieldConfig) fieldConfig);
                    }
                }
            }

            for (UniqueKeyConfig uniqueKeyConfig : config.getUniqueKeyConfigs()) {
                if ((oldConfig != null && !oldConfig.getUniqueKeyConfigs().contains(uniqueKeyConfig)) ||
                        (oldConfig == null && schemaCache.getUniqueKeyName(config, uniqueKeyConfig) == null)) {
                    newUniqueKeyConfigs.add(uniqueKeyConfig);
                }
            }

            if (!newReferenceFieldConfigs.isEmpty() || !newUniqueKeyConfigs.isEmpty()) {
                dataStructureDao.createForeignKeyAndUniqueConstraints(config,
                        newReferenceFieldConfigs, newUniqueKeyConfigs);
                setSchemaUpdateDone();
            }
        }
    }

    protected void loadDomainObjectConfig(DomainObjectTypeConfig domainObjectTypeConfig) {
        if (!schemaCache.isTableExist(domainObjectTypeConfig)) {
            super.loadDomainObjectConfig(domainObjectTypeConfig);
            createAclTablesFor(domainObjectTypeConfig);
        } else {
            logger.trace("The table already exists. Attempting to update...");

            List<FieldConfig> newFieldConfigs = new ArrayList<>();

            for (FieldConfig fieldConfig : domainObjectTypeConfig.getFieldConfigs()) {
                ColumnInfo columnInfo = schemaCache.getColumnInfo(domainObjectTypeConfig, fieldConfig);
                if (columnInfo == null) {
                    logger.trace("Add new field config {}", fieldConfig);
                    newFieldConfigs.add(fieldConfig);
                } else {
                    logger.trace("Attempting to update existing field config from {} to {}", columnInfo, fieldConfig);
                    fieldConfigDbValidator.validate(fieldConfig, domainObjectTypeConfig, columnInfo);
                    fieldConfigChangeHandler.handle(fieldConfig, ColumnInfoConverter.convert(columnInfo, fieldConfig),
                            domainObjectTypeConfig, configurationExplorer);
                }
            }

            if (!newFieldConfigs.isEmpty()) {
                boolean isParent = isParentObject(domainObjectTypeConfig, configurationExplorer);
                dataStructureDao.updateTableStructure(domainObjectTypeConfig, newFieldConfigs, false, isParent);
                setSchemaUpdateDone();
            }
        }
    }

    private void merge(DomainObjectTypeConfig domainObjectTypeConfig) {
        logger.trace(domainObjectTypeConfig + " will be processed");

        if (domainObjectTypeConfig.isTemplate()) {
            return;
        }

        DomainObjectTypeConfig oldDomainObjectTypeConfig =
                oldConfigExplorer.getConfig(DomainObjectTypeConfig.class, domainObjectTypeConfig.getName());

        if (oldDomainObjectTypeConfig == null) {
            logger.trace("Type config not found in previous configuration. Attempting to load domain object type config");
            loadDomainObjectConfig(domainObjectTypeConfig);
        } else if (!domainObjectTypeConfig.equals(oldDomainObjectTypeConfig)) {
            logger.trace("Type config was changed. Attempting to update domain object type config");
            validateExtendsAttribute(domainObjectTypeConfig, oldDomainObjectTypeConfig);
            updateDomainObjectConfig(domainObjectTypeConfig, oldDomainObjectTypeConfig);
        }
        logger.trace("DomainObjectTypeConfig successfully processed");
    }

    private void updateDomainObjectConfig(DomainObjectTypeConfig domainObjectTypeConfig,
                                          DomainObjectTypeConfig oldDomainObjectTypeConfig) {
        boolean isAl = configurationExplorer.isAuditLogType(domainObjectTypeConfig.getName());
        boolean isParent = isParentObject(domainObjectTypeConfig, configurationExplorer);

        Integer usedId;
        DomainObjectTypeConfig sourceDomainObjectTypeConfig = null;
        if (isAl) {
            sourceDomainObjectTypeConfig = getSourceDomainObjectType(domainObjectTypeConfig, configurationExplorer);
            Objects.requireNonNull(sourceDomainObjectTypeConfig, "Cannot update audit log type "
                    + domainObjectTypeConfig.getName() + ". Source Domain Object Type not found");

            usedId = domainObjectTypeIdDao.findIdByName(sourceDomainObjectTypeConfig.getName());
        } else {
            usedId = domainObjectTypeIdDao.findIdByName(domainObjectTypeConfig.getName());
        }

        if (!domainObjectTypeConfig.isTemplate() && usedId == null) {
            throw new FatalException("Cannot update domain object type " + domainObjectTypeConfig.getName() +
                    " because it's not found in domain_object_type_id table");
        }

        domainObjectTypeConfig.setId(usedId);

        processDependentConfigs(domainObjectTypeConfig);

        if (oldDomainObjectTypeConfig.getDbId() == null && domainObjectTypeConfig.getDbId() != null) {
            if (!domainObjectTypeConfig.getDbId().equals(usedId)) {
                throw new FatalException("Cannot update domain object type " + domainObjectTypeConfig.getName() +
                " because db-id different from specified " + domainObjectTypeConfig.getDbId() +
                        " is already in use");
            }
        }

        LinkedHashSet<FieldConfig> newFieldConfigs = new LinkedHashSet<>();

        if (isParent) {
            for (FieldConfig fieldConfig : domainObjectTypeConfig.getSystemFieldConfigs()) {
                ColumnInfo columnInfo = schemaCache.getColumnInfo(domainObjectTypeConfig, fieldConfig);
                if (columnInfo == null) {
                    if (fieldConfig.getName().equals(DomainObjectDao.ACCESS_OBJECT_ID) && fieldConfig instanceof ReferenceFieldConfig) {
                        fieldConfig = createAccessObjectIdConfig();
                    }
                    newFieldConfigs.add(fieldConfig);
                }
            }

            FieldConfig accessObjectIdConfig = createAccessObjectIdConfig();
            ColumnInfo columnInfo = schemaCache.getColumnInfo(domainObjectTypeConfig, accessObjectIdConfig);
            if (columnInfo == null) {
                newFieldConfigs.add(accessObjectIdConfig);
            }
        }

        for (FieldConfig fieldConfig : domainObjectTypeConfig.getFieldConfigs()) {
            FieldConfig oldFieldConfig = oldConfigExplorer.getFieldConfig(domainObjectTypeConfig.getName(),
                    fieldConfig.getName(), false);

            ColumnInfo columnInfo = schemaCache.getColumnInfo(domainObjectTypeConfig, fieldConfig);
            if (columnInfo == null) {
                newFieldConfigs.add(fieldConfig);
            } else {
                if (oldFieldConfig == null) {
                    fieldConfigDbValidator.validate(fieldConfig, domainObjectTypeConfig, columnInfo);
                } else if (!fieldConfig.equals(oldFieldConfig) &&
                        !configurationExplorer.isAuditLogType(domainObjectTypeConfig.getName())) {
                    fieldConfigChangeHandler.handle(fieldConfig, oldFieldConfig, domainObjectTypeConfig, configurationExplorer);
                }
            }
        }

        if (!newFieldConfigs.isEmpty()) {
            if (isAl) {
                dataStructureDao.updateTableStructure(sourceDomainObjectTypeConfig, new ArrayList<>(newFieldConfigs), true, isParent);
            } else {
                dataStructureDao.updateTableStructure(domainObjectTypeConfig, new ArrayList<>(newFieldConfigs), false, isParent);
            }
            setSchemaUpdateDone();
        }

        List<IndexConfig> newIndices = new ArrayList<>();

        for (IndexConfig indexConfig : domainObjectTypeConfig.getIndicesConfig().getIndices()) {
            if (!oldDomainObjectTypeConfig.getIndicesConfig().getIndices().contains(indexConfig)) {
                newIndices.add(indexConfig);
            }
        }

        if (!newIndices.isEmpty()) {
            dataStructureDao.createIndices(domainObjectTypeConfig, newIndices);
            setSchemaUpdateDone();
        }

        List<IndexConfig> indicesToDelete = new ArrayList<>();
        for (IndexConfig oldIndexConfig : oldDomainObjectTypeConfig.getIndicesConfig().getIndices()) {
            if (!domainObjectTypeConfig.getIndicesConfig().getIndices().contains(oldIndexConfig)) {
                indicesToDelete.add(oldIndexConfig);
            }
        }

        if (!indicesToDelete.isEmpty()) {
            dataStructureDao.deleteIndices(domainObjectTypeConfig, indicesToDelete);
            setSchemaUpdateDone();
        }

    }

    private static FieldConfig createAccessObjectIdConfig() {
        LongFieldConfig fieldConfig = new LongFieldConfig();
        fieldConfig.setName(DomainObjectDao.ACCESS_OBJECT_ID);
        return fieldConfig;
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
                            dataStructureDao.setColumnNotNull(oldDOTypeConfig, oldFieldConfig, false);
                            setSchemaUpdateDone();
                        }
                    }
                }
            }

            for (UniqueKeyConfig oldUniqueKeyConfig : oldDOTypeConfig.getUniqueKeyConfigs()) {
                if (!domainObjectTypeConfig.getUniqueKeyConfigs().contains(oldUniqueKeyConfig)) {
                    String uniqueKeyName = schemaCache.getUniqueKeyName(oldDOTypeConfig, oldUniqueKeyConfig);
                    if (uniqueKeyName != null) {
                        dataStructureDao.dropConstraint(oldDOTypeConfig, uniqueKeyName);
                        setSchemaUpdateDone();
                    }
                }
            }
        }
    }

    private void validateExtendsAttribute(DomainObjectTypeConfig domainObjectTypeConfig,
                                          DomainObjectTypeConfig oldDomainObjectTypeConfig) {
        String extendsAttributeValue = domainObjectTypeConfig.getExtendsAttribute();
        String oldExtendsAttributeValue = oldDomainObjectTypeConfig.getExtendsAttribute();

        final boolean unextendDone = (extendsAttributeValue == null || extendsAttributeValue.isEmpty()) && oldExtendsAttributeValue != null && !oldExtendsAttributeValue.isEmpty();
        if (unextendDone) { // validate that foreign key is dropped
            final String oldForeignKey = schemaCache.getParentTypeForeignKeyName(oldDomainObjectTypeConfig);
            if (oldForeignKey != null) {
                throw new ConfigurationException("Configuration loading aborted: 'extends' attribute was removed " +
                        "for '" + domainObjectTypeConfig.getName() + ", but foreign key to parent table still exists. Use <unextend> migration script to remove it ");
            } else {
                return;
            }
        }
        final boolean extendDone = extendsAttributeValue != null && !extendsAttributeValue.equals(oldExtendsAttributeValue);
        if (extendDone) {
            throw new ConfigurationException("Configuration loading aborted: 'extends' attribute was added " +
                    "for '" + domainObjectTypeConfig.getName() + ". Scenario is not supported yet");
        }

        if (extendsAttributeValue != null && !extendsAttributeValue.isEmpty()) {
            final String parentTypeFK = schemaCache.getParentTypeForeignKeyName(oldDomainObjectTypeConfig);
            if (parentTypeFK == null) {
                throw new ConfigurationException("Configuration loading aborted: " + domainObjectTypeConfig.getName()
                        + " extends " + extendsAttributeValue + ", but foreign key to parent table does not exist.");
            }
        }
    }
}
