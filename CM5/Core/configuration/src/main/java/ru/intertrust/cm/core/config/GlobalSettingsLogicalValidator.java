package ru.intertrust.cm.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.intertrust.cm.core.config.model.GlobalSettingsConfig;
import ru.intertrust.cm.core.config.model.base.Configuration;
import ru.intertrust.cm.core.config.model.base.TopLevelConfig;

import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 31/10/13
 *         Time: 12:05 PM
 */
public class GlobalSettingsLogicalValidator {
    private static final Logger logger = LoggerFactory.getLogger(NavigationPanelLogicalValidator.class);
    private Configuration configuration;

    public GlobalSettingsLogicalValidator(Configuration configuration) {
        this.configuration = configuration;

    }
    public void validate(){
        int globalConfigurationCount = 0;
        List<TopLevelConfig> topLevelConfigs = configuration.getConfigurationList();
        for(TopLevelConfig topLevelConfig : topLevelConfigs) {
             if (topLevelConfig.getClass().equals(GlobalSettingsConfig.class)) {
                 globalConfigurationCount++;
             }
        }
        if (globalConfigurationCount == 0) {
            throw new ConfigurationException("There is no global settings configuration!");
        }
        if (globalConfigurationCount > 1 ) {
            throw new ConfigurationException("There are more then one global settings configurations!");
        }
        logger.info("Global settings configuration has passed logical validation");
    }
}
