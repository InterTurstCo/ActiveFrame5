package ru.intertrust.cm.core.business.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.jms.JMSException;
import javax.naming.NamingException;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import ru.intertrust.cm.core.business.api.ConfigurationControlService;
import ru.intertrust.cm.core.business.api.ImportDataService;
import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.util.ObjectCloner;
import ru.intertrust.cm.core.config.ConfigurationException;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.ConfigurationExplorerImpl;
import ru.intertrust.cm.core.config.ConfigurationSerializer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.SummaryConfigurationException;
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
import ru.intertrust.cm.core.model.RemoteSuitableException;
import ru.intertrust.cm.core.util.SpringBeanAutowiringInterceptor;
import ru.intertrust.cm.core.util.SpringApplicationContext;

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
        } catch (Exception ex) {
            logger.error("Error update configuration", ex);
            throw RemoteSuitableException.convert(ex);
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
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
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
     */
    @Override
    public void activateFromString(String configString) throws SummaryConfigurationException {
        final Set<ConfigChange> configChanges = extensionProcessor().activateFromString(configString);
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
     * @param extensionIds
     */
    @Override
    public void cleanExtensionsById(List<Id> extensionIds) throws SummaryConfigurationException {
        final Set<ConfigChange> configChanges = extensionProcessor().cleanExtensions(extensionIds);
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
        } catch (JMSException | NamingException ex) {
            throw RemoteSuitableException.convert(ex);
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
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    private void processWorkflowUpdate(String processDataString, String fileName) {
        byte[] process = Base64.decodeBase64(processDataString);
        if (processService.isSupportTemplate(fileName)) {
            processService.saveProcess(() -> new ByteArrayInputStream(process), fileName, ProcessService.SaveType.DEPLOY);
        } else {
            throw new FatalException("Process template " + fileName + " is not support by workflow engine");
        }
    }

    private void processDataImport(String importDataString) {
        try {
            importDataService.importData(importDataString.getBytes("windows-1251"), null, true);
        } catch (UnsupportedEncodingException e) {
            throw new FatalException("Error import csv file", e);
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
        } else if (fileName.endsWith(".bpmn")) {
            return UpdateType.WORKFLOW;
        } else if (fileName.endsWith(".par")) {
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
