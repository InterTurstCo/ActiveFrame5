package ru.intertrust.cm.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.base.TopLevelConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 31/10/13
 *         Time: 12:05 PM
 */
public class GlobalSettingsLogicalValidator implements ConfigurationValidator {
    private Configuration configuration;

    public GlobalSettingsLogicalValidator(Configuration configuration) {
        this.configuration = configuration;

    }

    @Override
    public List<LogicalErrors> validate(){
        List<LogicalErrors> logicalErrorsList = new ArrayList<>();

        int globalConfigurationCount = 0;
        List<TopLevelConfig> topLevelConfigs = configuration.getConfigurationList();
        for(TopLevelConfig topLevelConfig : topLevelConfigs) {
             if (topLevelConfig.getClass().equals(GlobalSettingsConfig.class)) {
                 globalConfigurationCount++;
             }
        }

        LogicalErrors logicalErrors = LogicalErrors.getInstance("Default", "global-settings");

        if (globalConfigurationCount == 0) {
            logicalErrors.addError("There is no global settings configuration!");
        }
        if (globalConfigurationCount > 1 ) {
            logicalErrors.addError("There are more then one global settings configurations!");
        }

        if (logicalErrors.getErrorCount() > 0) {
            logicalErrorsList.add(logicalErrors);
        }

        return logicalErrorsList;
    }
}
