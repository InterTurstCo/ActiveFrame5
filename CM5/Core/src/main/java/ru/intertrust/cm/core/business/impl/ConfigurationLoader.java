package ru.intertrust.cm.core.business.impl;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.impl.ConfigurationValidator;
import ru.intertrust.cm.core.config.Configuration;

import java.io.File;

/**
 * @author vmatsukevich
 *         Date: 5/6/13
 *         Time: 9:36 AM
 */
public class ConfigurationLoader {

    private String configurationFilePath;
    private ConfigurationService configurationService;

    private Configuration configuration;

    private ConfigurationValidator configurationValidator;

    public ConfigurationLoader() {
    }

    public String getConfigurationFilePath() {
        return configurationFilePath;
    }

    public void setConfigurationFilePath(String configurationFilePath) {
        this.configurationFilePath = configurationFilePath;
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public ConfigurationValidator getConfigurationValidator() {
        return configurationValidator;
    }

    public void setConfigurationValidator(ConfigurationValidator configurationValidator) {
        this.configurationValidator = configurationValidator;
    }

    public ConfigurationLoader(String configurationFilePath) {
        this.configurationFilePath = configurationFilePath;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void load() throws Exception {
        Serializer serializer = new Persister();
        File source = new File(configurationFilePath);
        configuration = serializer.read(Configuration.class, source);

        validateConfiguration();

        configurationService.loadConfiguration(configuration);
    }

    private void validateConfiguration() {
        ConfigurationValidator configurationValidator = getConfigurationValidator();
        configurationValidator.setConfigurationPath(configurationFilePath);
        configurationValidator.setConfiguration(configuration);

        configurationValidator.validate();

        configurationService.loadConfiguration(configuration);

    }
}
