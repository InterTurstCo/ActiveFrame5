package ru.intertrust.cm.core.business.impl;

import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.dao.api.DataStructureDao;

import java.util.*;

/**
* Created by vmatsukevich on 9/18/14.
*/
abstract class AbstractRecursiveLoader {

    protected DataStructureDao dataStructureDao;
    protected ConfigurationExplorer configurationExplorer;
    private final Set<String> processedConfigs = new HashSet<>();

    protected DataStructureDao getDataStructureDao() {
        return dataStructureDao;
    }

    protected void setDataStructureDao(DataStructureDao dataStructureDao) {
        this.dataStructureDao = dataStructureDao;
    }

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
        dataStructureDao.createAclTables(domainObjectTypeConfig);
    }

    protected abstract void doProcessConfig(DomainObjectTypeConfig domainObjectTypeConfig);

    protected abstract void postProcessConfig(DomainObjectTypeConfig domainObjectTypeConfig);

    protected void createAllConstraints(DomainObjectTypeConfig config) {
        List<ReferenceFieldConfig> referenceFieldConfigs = new ArrayList<>();
        for (FieldConfig fieldConfig : config.getFieldConfigs()) {
            if (fieldConfig instanceof ReferenceFieldConfig) {
                referenceFieldConfigs.add((ReferenceFieldConfig) fieldConfig);
            }
        }

        if (!referenceFieldConfigs.isEmpty() || !config.getUniqueKeyConfigs().isEmpty()) {
            dataStructureDao.createForeignKeyAndUniqueConstraints(config, referenceFieldConfigs,
                    config.getUniqueKeyConfigs());
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
            dataStructureDao.createTable(domainObjectTypeConfig);
            dataStructureDao.createAuditLogTable(domainObjectTypeConfig);
            dataStructureDao.createSequence(domainObjectTypeConfig);
            dataStructureDao.createAuditSequence(domainObjectTypeConfig);
        }
    }

    private boolean isProcessed(DomainObjectTypeConfig domainObjectTypeConfig) {
        return processedConfigs.contains(domainObjectTypeConfig.getName());
    }

    private void setAsProcessed(DomainObjectTypeConfig domainObjectTypeConfig) {
        processedConfigs.add(domainObjectTypeConfig.getName());
    }
}
