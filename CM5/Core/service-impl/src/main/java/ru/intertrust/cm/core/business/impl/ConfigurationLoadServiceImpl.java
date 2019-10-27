package ru.intertrust.cm.core.business.impl;

import static ru.intertrust.cm.core.dao.api.ConfigurationDao.CONFIGURATION_TABLE;

import java.util.HashSet;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.ConfigurationLoadService;
import ru.intertrust.cm.core.config.ConfigurationException;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.ConfigurationExplorerImpl;
import ru.intertrust.cm.core.config.ConfigurationSerializer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.dao.api.ConfigurationDao;
import ru.intertrust.cm.core.dao.api.ConfigurationDbValidator;
import ru.intertrust.cm.core.dao.api.DataStructureDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.api.StatisticsGatherer;
import ru.intertrust.cm.core.model.RemoteSuitableException;
import ru.intertrust.cm.core.util.CustomSpringBeanAutowiringInterceptor;
import ru.intertrust.cm.core.util.SpringApplicationContext;

/**
 * Смотри {@link ru.intertrust.cm.core.business.api.ConfigurationLoadService}
 * @author vmatsukevich
 *         Date: 5/15/13
 *         Time: 4:32 PM
 */
@Stateless
@Local(ConfigurationLoadService.class)
@Remote(ConfigurationLoadService.Remote.class)
@Interceptors(CustomSpringBeanAutowiringInterceptor.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class ConfigurationLoadServiceImpl implements ConfigurationLoadService, ConfigurationLoadService.Remote {

    final static org.slf4j.Logger logger = LoggerFactory.getLogger(ConfigurationLoadServiceImpl.class);

    @Autowired
    private DomainObjectTypeIdCache domainObjectTypeIdCache;
    @Autowired
    private ConfigurationExplorer configurationExplorer;
    @Autowired
    private ConfigurationDao configurationDao;
    @Autowired
    private ConfigurationSerializer configurationSerializer;
    @Autowired
    private MigrationService migrationService;
    @Autowired
    private ConfigurationDbValidator configurationDbValidator;
    @Autowired
    private DataStructureDao dataStructureDao;
    @EJB
    private StatisticsGatherer statisticsGatherer;

    @org.springframework.beans.factory.annotation.Value("${force.db.consistency.check:false}")
    private boolean forceDbCheck;

    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    /**
     * Смотри {@link ru.intertrust.cm.core.business.api.ConfigurationLoadService#loadConfiguration()}
     */
    @Override
    public void loadConfiguration() throws ConfigurationException {
        try {
            RecursiveConfigurationLoader recursiveLoader = createRecursiveConfigurationLoader();
            recursiveLoader.load(configurationExplorer);
            saveConfiguration();
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public boolean isConfigurationTableExist() {
        return dataStructureDao.isTableExist(CONFIGURATION_TABLE);
    }

    /**
     * Смотри {@link ru.intertrust.cm.core.business.api.ConfigurationLoadService#loadConfiguration()}
     */
    @Override
    public void updateConfiguration() throws ConfigurationException {
        try {
            String oldConfigurationString = configurationDao.readLastSavedConfiguration();
            if (oldConfigurationString == null) {
                throw new ConfigurationException("Configuration loading aborted: configuration was previously " +
                        "loaded but wasn't saved");
            }

            Configuration oldConfiguration;
            try {
                oldConfiguration = configurationSerializer.deserializeLoadedConfiguration(oldConfigurationString);
                if (oldConfiguration == null) {
                    throw new ConfigurationException("Failed to deserialize last successfully loaded configuration");
                }
            } catch (ConfigurationException e) {
                throw new ConfigurationException("Configuration loading aborted: failed to deserialize last loaded " +
                        "configuration. This may mean that configuration structure has changed since last configuration load", e);
            }

            ConfigurationExplorer oldConfigurationExplorer = new ConfigurationExplorerImpl(oldConfiguration, true);
            boolean schemaUpdatedByScriptMigration = migrationService.executeBeforeAutoMigration(oldConfigurationExplorer);

            boolean schemaUpdatedByAutoMigration = false;

            boolean executeAutoMigration = !sameDomainObjectTypes(configurationExplorer, oldConfigurationExplorer);
            logger.warn("Auto-migration should " + (executeAutoMigration ? "" : "NOT ") + "be done");
            if (executeAutoMigration) {
                schemaUpdatedByAutoMigration =
                        createRecursiveConfigurationMerger().merge(oldConfigurationExplorer, configurationExplorer);
            }
            if (!configurationExplorer.getConfiguration().equals(oldConfiguration)) {
                saveConfiguration();
            }
            domainObjectTypeIdCache.build();
            schemaUpdatedByScriptMigration = migrationService.executeAfterAutoMigration(oldConfigurationExplorer) ||
                    schemaUpdatedByScriptMigration;

            if (schemaUpdatedByScriptMigration || forceDbCheck) {
                configurationDbValidator.validate();
            }

            if (schemaUpdatedByScriptMigration || schemaUpdatedByAutoMigration) {
                statisticsGatherer.gatherStatistics();
            }
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    private boolean sameDomainObjectTypes(ConfigurationExplorer explorer1, ConfigurationExplorer explorer2) {
        return new HashSet(explorer1.getConfigs(DomainObjectTypeConfig.class)).equals(new HashSet(explorer2.getConfigs(DomainObjectTypeConfig.class)));
    }

    private void saveConfiguration() {
        String configurationString =
                ConfigurationSerializer.serializeConfiguration(configurationExplorer.getConfiguration());
        configurationDao.save(configurationString);
    }

    private RecursiveConfigurationLoader createRecursiveConfigurationLoader() {
        return (RecursiveConfigurationLoader) SpringApplicationContext.getContext().getBean("recursiveConfigurationLoader");
    }

    private RecursiveConfigurationMerger createRecursiveConfigurationMerger() {
        return (RecursiveConfigurationMerger) SpringApplicationContext.getContext().getBean("recursiveConfigurationMerger");
    }

}
