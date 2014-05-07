package ru.intertrust.cm.core.business.impl;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.ConfigurationControlService;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.config.ConfigurationException;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.ConfigurationExplorerImpl;
import ru.intertrust.cm.core.config.ConfigurationSerializer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.IndexConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.UniqueKeyConfig;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.dao.api.ConfigurationDao;
import ru.intertrust.cm.core.dao.api.DataStructureDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdDao;
import ru.intertrust.cm.core.model.FatalException;

import java.util.*;

/**
 * Смотри {@link ru.intertrust.cm.core.business.api.ConfigurationControlService}
 * @author vmatsukevich
 *         Date: 5/15/13
 *         Time: 4:32 PM
 */
public class ConfigurationControlServiceImpl implements ConfigurationControlService {

    private static final String COMMON_ERROR_MESSAGE = "It's only allowed to add some new configuration " +
            "but not to modify or delete the existing one.";

    @Autowired
    private ConfigurationExplorer configurationExplorer;
    @Autowired
    private DataStructureDao dataStructureDao;
    @Autowired
    private DomainObjectTypeIdDao domainObjectTypeIdDao;
    @Autowired
    private ConfigurationDao configurationDao;
    @Autowired
    private ConfigurationSerializer configurationSerializer;

    /**
     * Устанавливает  {@link #dataStructureDao}
     * @param dataStructureDao DataStructureDao
     */
    public void setDataStructureDao(DataStructureDao dataStructureDao) {
        this.dataStructureDao = dataStructureDao;
    }

    public void setConfigurationDao(ConfigurationDao configurationDao) {
        this.configurationDao = configurationDao;
    }

    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    public void setConfigurationSerializer(ConfigurationSerializer configurationSerializer) {
        this.configurationSerializer = configurationSerializer;
    }

    public void setDomainObjectTypeIdDao(DomainObjectTypeIdDao domainObjectTypeIdDao) {
        this.domainObjectTypeIdDao = domainObjectTypeIdDao;
    }

    public void loadConfiguration(String configurationString) throws ConfigurationException {
        Configuration configuration;
        try {
            configuration = configurationSerializer.deserializeTrustedConfiguration(configurationString);
            if (configuration == null) {
                throw new ConfigurationException();
            }
        } catch (ConfigurationException e) {
            throw new ConfigurationException("Configuration loading aborted: failed to deserialize configuration", e);
        }

        for (TopLevelConfig config : configuration.getConfigurationList()) {
            if (DomainObjectTypeConfig.class.equals(config.getClass())) {
                continue;
            }

            TopLevelConfig oldConfig = configurationExplorer.getConfig(config.getClass(), config.getName());
            if (oldConfig == null || !oldConfig.equals(config)) {
                configurationExplorer.updateConfig(config);
            }
        }
    }

    /**
     * Смотри {@link ConfigurationControlService#loadConfiguration()}
     */
    @Override
    public void loadConfiguration() throws ConfigurationException {
        if (!isConfigurationLoaded()) {
            RecursiveLoader recursiveLoader = new RecursiveLoader();
            recursiveLoader.load();

            saveConfiguration();
            return;
        }

        String oldConfigurationString = configurationDao.readLastSavedConfiguration();
        if (oldConfigurationString == null) {
            throw new ConfigurationException("Configuration loading aborted: configuration was previously " +
                    "loaded but wasn't saved");
        }

        Configuration oldConfiguration;
        try {
            oldConfiguration = configurationSerializer.deserializeTrustedConfiguration(oldConfigurationString);
            if (oldConfiguration == null) {
                throw new ConfigurationException();
            }
        } catch (ConfigurationException e) {
            throw new ConfigurationException("Configuration loading aborted: failed to deserialize last loaded " +
                    "configuration. This may mean that configuration structure has changed since last configuration load", e);
        }

        if (configurationExplorer.getConfiguration().equals(oldConfiguration)) {
            return;
        }

        RecursiveMerger recursiveMerger = new RecursiveMerger(oldConfiguration);
        recursiveMerger.merge();
        saveConfiguration();
    }

    private void saveConfiguration() {
        String configurationString =
                ConfigurationSerializer.serializeConfiguration(configurationExplorer.getConfiguration());
        configurationDao.save(configurationString);
    }

    private class RecursiveLoader extends AbstractRecursiveLoader {

        private RecursiveLoader() {
        }

        public void load() {
            Collection<DomainObjectTypeConfig> configList =
                    configurationExplorer.getConfigs(DomainObjectTypeConfig.class);
            if(configList.isEmpty())  {
                return;
            }

            dataStructureDao.createServiceTables();
            processConfigs(configList);
            createAclTables(configList);
        }

        @Override
        protected void doProcessConfig(DomainObjectTypeConfig domainObjectTypeConfig) {
            loadDomainObjectConfig(domainObjectTypeConfig);
        }

        @Override
        protected void postProcessConfig(DomainObjectTypeConfig config) {
            createAllConstraints(config);
        }

        private void createAclTables(Collection<DomainObjectTypeConfig> configList) {
            for (DomainObjectTypeConfig config : configList) {
                if (!config.isTemplate()) {
                    createAclTablesFor(config);
                }
            }
        }
    }

    private Boolean isConfigurationLoaded() {
        Integer tablesCount = dataStructureDao.countTables();
        if(tablesCount == null) {
            throw new FatalException("Error occurred when calling DataStructureDao for tables count");
        }

        return tablesCount > 0;
    }

    private class RecursiveMerger extends AbstractRecursiveLoader {

        private final ConfigurationExplorer oldConfigExplorer;

        private RecursiveMerger(Configuration oldConfiguration) {
            oldConfigExplorer = new ConfigurationExplorerImpl(oldConfiguration);
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

        protected void loadDomainObjectConfig(DomainObjectTypeConfig domainObjectTypeConfig) {
            super.loadDomainObjectConfig(domainObjectTypeConfig);
            createAclTablesFor(domainObjectTypeConfig);
        }

        private void merge() {
            Collection<DomainObjectTypeConfig> configList =
                    configurationExplorer.getConfigs(DomainObjectTypeConfig.class);
            if(configList.isEmpty())  {
                return;
            }

            validateForDeletedConfigurations();
            processConfigs(configList);
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
                    newFieldConfigs.add(fieldConfig);
                } else if (!fieldConfig.equals(oldFieldConfig)) {
                    throw new ConfigurationException("Configuration loading aborted: FieldConfig '" +
                            domainObjectTypeConfig.getName() + "." + fieldConfig.getName() + " was changed. " +
                            COMMON_ERROR_MESSAGE);
                }
            }

            if (!newFieldConfigs.isEmpty()) {
                dataStructureDao.updateTableStructure(domainObjectTypeConfig, newFieldConfigs, false);
                dataStructureDao.updateTableStructure(domainObjectTypeConfig, newFieldConfigs, true);
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

        private void validateForDeletedConfigurations() {
            for (DomainObjectTypeConfig oldDOTypeConfig : oldConfigExplorer.getConfigs(DomainObjectTypeConfig.class)) {
                DomainObjectTypeConfig domainObjectTypeConfig =
                        configurationExplorer.getConfig(DomainObjectTypeConfig.class, oldDOTypeConfig.getName());
                if (domainObjectTypeConfig == null) {
                    throw new ConfigurationException("Configuration loading aborted: DomainObject configuration '" +
                            oldDOTypeConfig.getName() + "' was deleted. " + COMMON_ERROR_MESSAGE);
                }

                for (FieldConfig oldFieldConfig : oldDOTypeConfig.getFieldConfigs()) {
                    FieldConfig fieldConfig = configurationExplorer.getFieldConfig(oldDOTypeConfig.getName(),
                            oldFieldConfig.getName(), false);
                    if (fieldConfig == null) {
                        throw new ConfigurationException("Configuration loading aborted: Field " +
                                "Configuration DomainObject '" + oldDOTypeConfig.getName() + "." +
                                oldFieldConfig.getName() + "' was deleted. " + COMMON_ERROR_MESSAGE);
                    }
                }

                if (!domainObjectTypeConfig.getUniqueKeyConfigs().containsAll(oldDOTypeConfig
                        .getUniqueKeyConfigs())) {
                    throw new ConfigurationException("Configuration loading aborted: some unique key " +
                            "Configuration of DomainObject '" + oldDOTypeConfig.getName() + "' was deleted. " +
                            COMMON_ERROR_MESSAGE);
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

    private abstract class AbstractRecursiveLoader {

        private final Set<String> processedConfigs = new HashSet<>();

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


            if (canHaveStatusColumn(config)) {
                createStatusForeignKey(config);
            }
        }

        private boolean canHaveStatusColumn(DomainObjectTypeConfig config) {
            return config.getExtendsAttribute() == null
                    && (!config.isTemplate());
        }

        private void createStatusForeignKey(DomainObjectTypeConfig config) {
            List<ReferenceFieldConfig> referenceFieldConfigs = new ArrayList<>();
            List<UniqueKeyConfig> uniqueKeyConfigs = new ArrayList<>();
            ReferenceFieldConfig referenceStatusField = new ReferenceFieldConfig();
            referenceStatusField.setName(GenericDomainObject.STATUS_FIELD_NAME);
            referenceStatusField.setType(GenericDomainObject.STATUS_DO);

            referenceFieldConfigs.add(referenceStatusField);

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
}
