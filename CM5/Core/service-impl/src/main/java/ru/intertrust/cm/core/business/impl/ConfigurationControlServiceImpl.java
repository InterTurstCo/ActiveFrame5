package ru.intertrust.cm.core.business.impl;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.intertrust.cm.core.business.api.ConfigurationControlService;
import ru.intertrust.cm.core.business.api.ImportDataService;
import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.util.ObjectCloner;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.config.base.CollectionConfig;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.dao.api.CollectionQueryCache;
import ru.intertrust.cm.core.dao.api.ConfigurationDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.model.SystemException;
import ru.intertrust.cm.core.model.UnexpectedException;
import ru.intertrust.cm.core.util.SpringApplicationContext;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

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

    private void processConfigurationUpdate(String configurationString) {
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
