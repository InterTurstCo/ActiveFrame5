package ru.intertrust.cm.core.business.impl;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.intertrust.cm.core.business.api.ConfigurationControlService;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.dao.api.ConfigurationDao;
import ru.intertrust.cm.core.dao.api.DataStructureDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdDao;
import ru.intertrust.cm.core.model.UnexpectedException;
import ru.intertrust.cm.core.util.ObjectCloner;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

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

    private static final String CONFIGURATION_UPDATE_JMS_TOPIC = "topic/ConfigurationUpdateTopic";

    final static org.slf4j.Logger logger = LoggerFactory.getLogger(ConfigurationControlServiceImpl.class);

    @Autowired private DataStructureDao dataStructureDao;
    @Autowired private DomainObjectTypeIdDao domainObjectTypeIdDao;
    @Autowired private ConfigurationDao configurationDao;
    @Autowired private ConfigurationExplorer configurationExplorer;
    @Autowired private ConfigurationSerializer configurationSerializer;

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateConfiguration(String configurationString) throws ConfigurationException {
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

    /**
     * {@inheritDoc}
     */
    @Deprecated // marked for removal
    @Override
    public boolean restartRequiredForFullUpdate(String configurationString) {
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
    }

    private Configuration deserializeConfiguration(String configurationString) {
        Configuration configuration = configurationSerializer.deserializeTrustedConfiguration(configurationString);

        if (configuration == null) {
            throw new ConfigurationException("Failed to deserialize configuration");
        }

        return configuration;
    }

    private Configuration buildNewConfiguration(Configuration configuration) {
        Configuration oldConfiguration = new ObjectCloner().
                cloneObject(configurationExplorer.getConfiguration(), Configuration.class);
        for (TopLevelConfig topLevelConfig : configuration.getConfigurationList()) {
            TopLevelConfig oldTopLevelConfig = configurationExplorer.
                    getConfig(topLevelConfig.getClass(), topLevelConfig.getName());
            if (oldTopLevelConfig == null) {
                oldConfiguration.getConfigurationList().add(topLevelConfig);
            } else if (!oldTopLevelConfig.equals(topLevelConfig)){
                oldConfiguration.getConfigurationList().remove(oldTopLevelConfig);
                oldConfiguration.getConfigurationList().add(topLevelConfig);
            }
        }

        return oldConfiguration;
    }

    private void saveConfiguration(Configuration configuration) {
        String configurationString = ConfigurationSerializer.serializeConfiguration(configuration);
        configurationDao.save(configurationString);
    }

    private void updateConfiguration(Configuration configuration) {
        Configuration newConfiguration = buildNewConfiguration(configuration);
        ConfigurationExplorer newConfigurationExplorer = new ConfigurationExplorerImpl(newConfiguration);

        RecursiveConfigurationMerger recursiveMerger = new RecursiveConfigurationMerger(dataStructureDao, domainObjectTypeIdDao);
        recursiveMerger.merge(configurationExplorer, newConfigurationExplorer);

        saveConfiguration(newConfiguration);
    }

}
