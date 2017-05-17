package ru.intertrust.cm.core.business.impl;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.intertrust.cm.core.business.api.ConfigurationControlService;
import ru.intertrust.cm.core.business.api.ImportDataService;
import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.util.ObjectCloner;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.config.base.CollectionConfig;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.config.event.ConfigChange;
import ru.intertrust.cm.core.config.event.SingletonConfigurationUpdateEvent;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.UserGroupGlobalCache;
import ru.intertrust.cm.core.dao.api.CollectionQueryCache;
import ru.intertrust.cm.core.dao.api.ConfigurationDao;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.core.model.SystemException;
import ru.intertrust.cm.core.model.UnexpectedException;
import ru.intertrust.cm.core.util.SpringApplicationContext;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.interceptor.Interceptors;
import javax.jms.JMSException;
import javax.naming.NamingException;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Смотри {@link ru.intertrust.cm.core.business.api.ConfigurationControlService}
 * @author vmatsukevich
 *         Date: 5/15/13
 *         Time: 4:32 PM
 */
@Stateless
@Local(ConfigurationControlService.class)
@Remote(ConfigurationControlService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class ConfigurationControlServiceImpl implements ConfigurationControlService {

    private enum UpdateType {CONFIGURATION, WORKFLOW, DATA_IMPORT}

    private static final String CONFIGURATION_UPDATE_JMS_TOPIC = "topic/ConfigurationUpdateTopic";

    final static org.slf4j.Logger logger = LoggerFactory.getLogger(ConfigurationControlServiceImpl.class);

    @Autowired private DomainObjectTypeIdCache domainObjectTypeIdCache;
    @Autowired private ConfigurationDao configurationDao;
    @Autowired private ConfigurationExplorer configurationExplorer;
    @Autowired private ConfigurationSerializer configurationSerializer;

    @Autowired private ProcessService processService;
    @Autowired private ImportDataService importDataService;
    @Autowired private CollectionQueryCache collectionQueryCache;
    @Autowired private AccessControlService accessControlService;
    @Autowired private ApplicationContext context;
    @Autowired private CurrentUserAccessor currentUserAccessor;
    @Autowired private UserGroupGlobalCache userGroupGlobalCache;

    @Autowired
    private ConfigurationExtensionHelper configurationExtensionHelper;

    @Resource
    private EJBContext ejbContext;

    @org.springframework.beans.factory.annotation.Value("${NEVER.USE.IN.PRODUCTION.dev.mode.configuration.update:false}")
    private boolean useDevModeConfigUpdate;

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateConfiguration(String updateContent, String fileName) throws ConfigurationException {
        try {
            UpdateType updateType = resolveUpdateType(updateContent, fileName);

            switch (updateType) {
                case CONFIGURATION: {
                    processConfigurationUpdate(updateContent);
                    break;
                } case WORKFLOW:{
                    processWorkflowUpdate(updateContent, fileName);
                    break;
                } case DATA_IMPORT: {
                    processDataImport(updateContent); // Импорт данных происходит в режиме rewrite.
                    break;
                }
            }
        } catch (SystemException e) {
            throw e;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in updateConfiguration", ex);
            throw new UnexpectedException("ConfigurationControlService", "updateConfiguration", "updateContent:" +
                    updateContent + ", fileName:" + fileName, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated // marked for removal
    @Override
    public boolean restartRequiredForFullUpdate(String configurationString) {
        try {
            Configuration configuration = deserializeConfiguration(configurationString);

            for (TopLevelConfig config : configuration.getConfigurationList()) {
                if (DomainObjectTypeConfig.class.equals(config.getClass())) {
                    DomainObjectTypeConfig newConfig = (DomainObjectTypeConfig) config;
                    DomainObjectTypeConfig currentConfig = configurationExplorer.getDomainObjectTypeConfig(newConfig.getName());
                    if (!newConfig.equals(currentConfig)) {
                        return true;
                    }
                }
            }

            return false;
        } catch (SystemException e) {
            throw e;
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in restartRequiredForFullUpdate", ex);
            throw new UnexpectedException("ConfigurationControlService", "restartRequiredForFullUpdate",
                    "configurationString:" + configurationString, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void activateDraftsById(List<Id> toolingIds) throws SummaryConfigurationException {
        final Set<ConfigChange> configChanges = extensionProcessor().activateDraftsById(toolingIds);
        notifySingletonListenersAndClusterAboutExtensionActivation(configChanges);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void activateDrafts(List<DomainObject> toolingDOs) throws SummaryConfigurationException {
        final Set<ConfigChange> configChanges = extensionProcessor().activateDrafts(toolingDOs);
        notifySingletonListenersAndClusterAboutExtensionActivation(configChanges);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void validateDrafts(List<DomainObject> toolingDOs) throws SummaryConfigurationException {
        extensionProcessor().validateDrafts(toolingDOs);
        ejbContext.setRollbackOnly();
    }

    @Override
    public List<DomainObject> saveDrafts(List<DomainObject> toolingDOs) throws ConfigurationException {
        return extensionProcessor().saveDrafts(toolingDOs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void activateDrafts() throws SummaryConfigurationException {
        final Set<ConfigChange> configChanges = extensionProcessor().activateDrafts();
        notifySingletonListenersAndClusterAboutExtensionActivation(configChanges);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void activateFromFiles(Collection<File> files) throws SummaryConfigurationException {
        final Set<ConfigChange> configChanges = extensionProcessor().activateFromFiles(files);
        notifySingletonListenersAndClusterAboutExtensionActivation(configChanges);
    }

    /**
     * {@inheritDoc}
     * @param extensionIds
     */
    @Override
    public void activateExtensionsById(List<Id> extensionIds) throws SummaryConfigurationException {
        final Set<ConfigChange> configChanges = extensionProcessor().activateExtensions(extensionIds);
        notifySingletonListenersAndClusterAboutExtensionActivation(configChanges);
    }

    /**
     * {@inheritDoc}
     * @param extensionIds
     */
    @Override
    public void deactivateExtensionsById(List<Id> extensionIds) throws SummaryConfigurationException {
        final Set<ConfigChange> configChanges = extensionProcessor().deactivateExtensions(extensionIds);
        notifySingletonListenersAndClusterAboutExtensionActivation(configChanges);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteNewExtensions(List<Id> extensionIds) throws ConfigurationException, SummaryConfigurationException {
        final Set<ConfigChange> configChanges = extensionProcessor().deleteNewExtensions(extensionIds);
        notifySingletonListenersAndClusterAboutExtensionActivation(configChanges);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TopLevelConfig getDistributiveConfig(String tagType, String tagName) {
        return configurationExtensionHelper.getDistributiveConfig(tagType, tagName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void validateInactiveExtensionsById(List<Id> extensionIds) throws SummaryConfigurationException {
        extensionProcessor().validateExtensionsById(extensionIds, true);
        ejbContext.setRollbackOnly();
    }

    @Override
    public void exportActiveExtensions(File file) throws ConfigurationException {
        extensionProcessor().exportActiveExtensions(file);
    }

    private ConfigurationExtensionProcessor extensionProcessor() {
        final ConfigurationExtensionProcessor configurationExtensionProcessor = (ConfigurationExtensionProcessor) context.getBean("configurationExtensionProcessor");
        if (!userGroupGlobalCache.isPersonSuperUser(currentUserAccessor.getCurrentUserId())) {
            throw new FatalException("User: \"" + currentUserAccessor.getCurrentUserId() + "\" has no rights to update configuration");
        }
        configurationExtensionProcessor.setAccessToken(accessControlService.createSystemAccessToken("ConfigurationLoader"));
        return configurationExtensionProcessor;
    }

    private void notifySingletonListenersAndClusterAboutExtensionActivation(Set<ConfigChange> configChanges) {
        try {
            if (!configChanges.isEmpty()) {
                ((ConfigurationExplorerImpl) configurationExplorer).getApplicationEventPublisher()
                        .publishEvent(new SingletonConfigurationUpdateEvent(configurationExplorer, configChanges));
                JmsUtils.sendTopicMessage(new ConfigurationUpdateMessage(), CONFIGURATION_UPDATE_JMS_TOPIC);
            }
        } catch (JMSException | NamingException e) {
            throw new UnexpectedException("ConfigurationControlService", "notifyClusterAboutExtensionActivation", null, e);
        }
    }

    private void processConfigurationUpdate(String configurationString) {
        if (!useDevModeConfigUpdate) {
            throw new ConfigurationException("Dev mode configuration update is not allowed");
        }
        Configuration configuration = deserializeConfiguration(configurationString);
        updateConfiguration(configuration);

        try {
            for (TopLevelConfig config : configuration.getConfigurationList()) {
                TopLevelConfig oldConfig = configurationExplorer.getConfig(config.getClass(), config.getName());
                if (oldConfig == null || !oldConfig.equals(config)) {
                    JmsUtils.sendTopicMessage(config, CONFIGURATION_UPDATE_JMS_TOPIC);
                }
            }
        } catch (Exception e) {
            logger.error("Unexpected exception caught in updateConfiguration", e);
            throw new UnexpectedException("ConfigurationControlService", "updateConfiguration", configurationString, e);
        }
    }

    private void processWorkflowUpdate(String processDataString, String fileName) {
        processService.deployProcess(processDataString.getBytes(), fileName);
    }

    private void processDataImport(String importDataString) {
        try {
            importDataService.importData(importDataString.getBytes("Windows-1251"), null, true);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace(); //should not happen
        }
    }

    private Configuration deserializeConfiguration(String configurationString) {
        Configuration configuration = configurationSerializer.deserializeLoadedConfiguration(configurationString);

        if (configuration == null) {
            throw new ConfigurationException("Failed to deserialize configuration");
        }

        return configuration;
    }

    private Configuration buildNewConfiguration(Configuration configuration) {
        Configuration oldConfigurationCopy =
                ObjectCloner.getInstance().cloneObject(configurationExplorer.getConfiguration(), Configuration.class);

        List<TopLevelConfig> newConfigs = new ArrayList<>();

        for (TopLevelConfig topLevelConfig : configuration.getConfigurationList()) {
            if (topLevelConfig instanceof CollectionConfig) {
                collectionQueryCache.clearCollectionQueryCache();
            }
            TopLevelConfig oldTopLevelConfig = configurationExplorer.
                    getConfig(topLevelConfig.getClass(), topLevelConfig.getName());

            if (oldTopLevelConfig == null) {
                newConfigs.add(topLevelConfig);
            } else if (!oldTopLevelConfig.equals(topLevelConfig)){
                int index = configurationExplorer.getConfiguration().getConfigurationList().indexOf(oldTopLevelConfig);
                oldConfigurationCopy.getConfigurationList().set(index, topLevelConfig);
            }
        }

        oldConfigurationCopy.getConfigurationList().addAll(newConfigs);

        return oldConfigurationCopy;
    }

    private void saveConfiguration(Configuration configuration) {
        String configurationString = ConfigurationSerializer.serializeConfiguration(configuration);
        configurationDao.save(configurationString);
    }

    private void updateConfiguration(Configuration configuration) {
        Configuration newConfiguration = buildNewConfiguration(configuration);
        ConfigurationExplorer newConfigurationExplorer = new ConfigurationExplorerImpl(newConfiguration, ((ConfigurationExplorerImpl) configurationExplorer).getContext());

        RecursiveConfigurationMerger recursiveMerger = createRecursiveConfigurationMerger();
        // todo insead of this - replace data in SINGLETON config explorer with new data, but first - perform logical validation
        recursiveMerger.merge(configurationExplorer, newConfigurationExplorer);

        saveConfiguration(newConfiguration);
        domainObjectTypeIdCache.build();
    }

    private UpdateType resolveUpdateType(String configurationString, String fileName) {
        if (fileName.endsWith(".csv")) {
            return UpdateType.DATA_IMPORT;
        } else if (fileName.endsWith(".bpmn") && configurationString.startsWith("<?xml") &&
                configurationString.contains("<process")) {
            return UpdateType.WORKFLOW;
        } else if (fileName.endsWith(".xml") && configurationString.startsWith("<?xml") &&
                configurationString.contains("<configuration")) {
            return UpdateType.CONFIGURATION;
        }

        throw new ConfigurationException("Unresolved configuration type for file " + fileName);
    }

    private RecursiveConfigurationMerger createRecursiveConfigurationMerger() {
        return (RecursiveConfigurationMerger) SpringApplicationContext.getContext().getBean("recursiveConfigurationMerger");
    }

}
