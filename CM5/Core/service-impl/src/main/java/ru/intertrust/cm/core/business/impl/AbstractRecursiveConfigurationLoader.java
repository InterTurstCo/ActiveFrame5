package ru.intertrust.cm.core.business.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.dao.api.DataStructureDao;

import java.util.*;

/**
* Abstract class that contains common logic for recursive configuration processing
* Designed as prototype, not thread-safe, instances are not reusable!!!
*/
abstract class AbstractRecursiveConfigurationLoader {

    @Autowired
    protected DataStructureDao dataStructureDao;

    protected ConfigurationExplorer configurationExplorer;

    private boolean schemaUpdateDone = false;

    private final Set<String> processedConfigs = new HashSet<>();


    public ConfigurationExplorer getConfigurationExplorer() {
        return configurationExplorer;
    }

    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    protected final void processDependentConfigs(DomainObjectTypeConfig domainObjectTypeConfig) {
        if (domainObjectTypeConfig.getExtendsAttribute() != null) {
            DomainObjectTypeConfig parentConfig = configurationExplorer.getConfig(DomainObjectTypeConfig.class,
                            domainObjectTypeConfig.getExtendsAttribute());
            processConfig(parentConfig);
        }
    }

    protected void loadDomainObjectConfig(DomainObjectTypeConfig domainObjectTypeConfig) {
        processDependentConfigs(domainObjectTypeConfig);
        createDbStructures(domainObjectTypeConfig);
    }

    protected final void processConfigs(Collection<DomainObjectTypeConfig> configList) {
        for (DomainObjectTypeConfig config : configList) {
            processConfig(config);
        }

        for (DomainObjectTypeConfig config : configList) {
            postProcessConfig(config);
        }
    }

    protected void createAclTablesFor(DomainObjectTypeConfig domainObjectTypeConfig) {
        if (!domainObjectTypeConfig.isTemplate() && !configurationExplorer.isAuditLogType(domainObjectTypeConfig.getName())) {
            dataStructureDao.createAclTables(domainObjectTypeConfig);
            setSchemaUpdateDone();
        }
    }

    protected abstract void doProcessConfig(DomainObjectTypeConfig domainObjectTypeConfig);

    protected abstract void postProcessConfig(DomainObjectTypeConfig domainObjectTypeConfig);

    protected void createAllConstraints(DomainObjectTypeConfig config) {
        if (configurationExplorer.isAuditLogType(config.getName())) {
            return;
        }      
        List<ReferenceFieldConfig> referenceFieldConfigs = new ArrayList<>();
        for (FieldConfig fieldConfig : config.getFieldConfigs()) {
            if (fieldConfig instanceof ReferenceFieldConfig) {
                referenceFieldConfigs.add((ReferenceFieldConfig) fieldConfig);
            }
        }

        if (!referenceFieldConfigs.isEmpty() || !config.getUniqueKeyConfigs().isEmpty()) {
            dataStructureDao.createForeignKeyAndUniqueConstraints(config, referenceFieldConfigs,
                    config.getUniqueKeyConfigs());
            setSchemaUpdateDone();
        }


        if (hasSystemFields(config)) {
            createSystemFieldsForeignKey(config);
        }
    }

    private boolean hasSystemFields(DomainObjectTypeConfig config) {
        return config.getExtendsAttribute() == null
                && (!config.isTemplate());
    }

    /**
     * Создает внешние ключи для ссылочных системных полей.
     * @param config
     */
    private void createSystemFieldsForeignKey(DomainObjectTypeConfig config) {
        if (configurationExplorer.isAuditLogType(config.getName())) {
            return;
        }
        List<ReferenceFieldConfig> referenceFieldConfigs = new ArrayList<>();
        List<UniqueKeyConfig> uniqueKeyConfigs = new ArrayList<>();

        for (FieldConfig fieldConfig : config.getSystemFieldConfigs()) {
            if (fieldConfig instanceof ReferenceFieldConfig) {
                // для id создается первичный ключ
                if (SystemField.id.name().equals(fieldConfig.getName())) {
                    continue;
                }
                referenceFieldConfigs.add((ReferenceFieldConfig) fieldConfig);

            }
        }
        dataStructureDao.createForeignKeyAndUniqueConstraints(config, referenceFieldConfigs,
                uniqueKeyConfigs);
        setSchemaUpdateDone();
    }

    private void processConfig(DomainObjectTypeConfig domainObjectTypeConfig) {
        if (isProcessed(domainObjectTypeConfig)) {
            return;
        }
        doProcessConfig(domainObjectTypeConfig);
        setAsProcessed(domainObjectTypeConfig);
    }

    private void createDbStructures(DomainObjectTypeConfig domainObjectTypeConfig) {
        if (!domainObjectTypeConfig.isTemplate()) {
            boolean isParentType = isParentObject(domainObjectTypeConfig);
            dataStructureDao.createTable(domainObjectTypeConfig, isParentType);
            dataStructureDao.createSequence(domainObjectTypeConfig);
            setSchemaUpdateDone();
        }
    }

    protected boolean isParentObject(DomainObjectTypeConfig config) {
        boolean isParent = false;
        if (configurationExplorer.isAuditLogType(config.getName())) {
            DomainObjectTypeConfig parentObjectConfig = getSourceDomainObjectType(config);
            if (parentObjectConfig != null && parentObjectConfig.getExtendsAttribute() != null && (!parentObjectConfig.isTemplate())) {
                isParent = false;
            } else {
                isParent = true;
            }

        } else {
            isParent = config.getExtendsAttribute() == null;
        }
        return isParent;
    }

    protected DomainObjectTypeConfig getSourceDomainObjectType(DomainObjectTypeConfig config) {
        if (!configurationExplorer.isAuditLogType(config.getName())) {
            return null;
        }

        String name = config.getName().replace(Configuration.AUDIT_LOG_SUFFIX, "");
        return configurationExplorer.getDomainObjectTypeConfig(name);
    }

    private boolean isProcessed(DomainObjectTypeConfig domainObjectTypeConfig) {
        return processedConfigs.contains(domainObjectTypeConfig.getName());
    }

    private void setAsProcessed(DomainObjectTypeConfig domainObjectTypeConfig) {
        processedConfigs.add(domainObjectTypeConfig.getName());
    }

    protected boolean isSchemaUpdateDone() {
        return schemaUpdateDone;
    }

    protected void setSchemaUpdateDone() {
        this.schemaUpdateDone = true;
    }

    protected void setSchemaUpdateDone(boolean sheUpdateDone) {
        this.schemaUpdateDone = sheUpdateDone;
    }
}
