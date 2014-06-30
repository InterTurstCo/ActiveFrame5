package ru.intertrust.cm.core.business.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.intertrust.cm.core.business.api.ConfigurationControlService;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.base.TopLevelConfig;

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

    @Autowired
    private ConfigurationExplorer configurationExplorer;
    @Autowired
    private ConfigurationSerializer configurationSerializer;

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateConfiguration(String configurationString) throws ConfigurationException {
        Configuration configuration = deserializeConfiguration(configurationString);

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
     * {@inheritDoc}
     */
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
        Configuration configuration;
        try {
            configuration = configurationSerializer.deserializeTrustedConfiguration(configurationString, false);
            if (configuration == null) {
                throw new ConfigurationException();
            }
        } catch (ConfigurationException e) {
            throw new ConfigurationException("Configuration loading aborted: failed to deserialize configuration", e);
        }

        return configuration;
    }

}
