package ru.intertrust.cm.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.config.model.gui.navigation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 10/9/13
 *         Time: 12:05 PM
 */
public class NavigationPanelLogicalValidator {
    private static final String PlUGIN_HANDLER_FULL_QUALIFIED_NAME =
            "ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler";
    private static final Logger logger = LoggerFactory.getLogger(NavigationPanelLogicalValidator.class);

    private List<LogicalErrors> validationLogicalErrors;
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    ApplicationContext context;

    public NavigationPanelLogicalValidator() {
        validationLogicalErrors = new ArrayList<LogicalErrors>();
    }
    public NavigationPanelLogicalValidator(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
        validationLogicalErrors = new ArrayList<LogicalErrors>();
    }

    public List<LogicalErrors> getValidationLogicalErrors() {
        return validationLogicalErrors;
    }

    public ConfigurationExplorer getConfigurationExplorer() {
        return configurationExplorer;
    }

    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    /**
     * Выполняет логическую валидацию конфигурации панели навигации
     */
    public void validate() {
        Collection<NavigationConfig> navigationConfigList = configurationExplorer.getConfigs(NavigationConfig.class);

        if (navigationConfigList.isEmpty()) {
            logger.info("Navigation Panel config couldn't be resolved");
            return;
        }
        for (NavigationConfig navigationConfig : navigationConfigList) {
            logger.info("Validating navigation panel with name '{}'", navigationConfig.getName());
            validateNavigateConfig(navigationConfig);
        }
        StringBuilder errorLogBuilder = new StringBuilder();
        for (LogicalErrors errors : validationLogicalErrors) {
              if(errors.getErrorCount() != 0) {
                  errorLogBuilder.append(errors.toString());
                  errorLogBuilder.append("\n");
              }
        }
        String errorLog = errorLogBuilder.toString();
        if (!errorLog.equalsIgnoreCase("")) {
            throw new ConfigurationException(errorLog);

        }
        logger.info("Navigation Panel configuration has passed logical validation without errors");
    }

    private void validateNavigateConfig(NavigationConfig navigationConfig) {
        if (navigationConfig == null) {
            return;
        }
        String navigationPanelName = navigationConfig.getName();
        LogicalErrors logicalErrors = LogicalErrors.getInstance(navigationPanelName, "navigation panel");
        List<LinkConfig> linkConfigList = navigationConfig.getLinkConfigList();

        if (linkConfigList == null) {
            return;
        }
        for (LinkConfig linkConfig : linkConfigList) {
            validateLinkConfig(linkConfig, logicalErrors);
            validatePluginHandlers(linkConfig, logicalErrors);
        }

        validationLogicalErrors.add(logicalErrors);
    }

    private void validateLinkConfig(LinkConfig linkConfig, LogicalErrors logicalErrors) {

        validatePluginHandlers(linkConfig, logicalErrors);

        String childToOpen = linkConfig.getChildToOpen();
        List<ChildLinksConfig> linkConfigList = linkConfig.getChildLinksConfigList();
        boolean isFound = false;
        if (childToOpen != null) {

            if (linkConfigList.isEmpty()) {
                String error =  String.
                        format("Child link to open is not found for link with name '%s'", linkConfig.getName());
                logger.error(error);
                logicalErrors.addError(error);

                return;

            }

            for (ChildLinksConfig childLinksConfig : linkConfigList) {
               isFound = findLinkByName(childLinksConfig, childToOpen);
            }
            if (!isFound) {
                String error =  String.
                        format("Child link to open is not found for link with name '%s'", linkConfig.getName());
                logger.error(error);
                logicalErrors.addError(error);
            }
        }
        validateChildLinks(linkConfigList, logicalErrors);
    }

    private void validateChildLinks(List<ChildLinksConfig> childLinksConfigList,
                                    LogicalErrors logicalErrors) {
        for (ChildLinksConfig childLinksConfig : childLinksConfigList) {
        List<LinkConfig> linkConfigList = childLinksConfig.getLinkConfigList();
        for(LinkConfig linkConfig : linkConfigList) {
            validateLinkConfig(linkConfig, logicalErrors);
        }
    }
    }

    private boolean findLinkByName(ChildLinksConfig childLinksConfig, String toCompareName) {
        List<LinkConfig> linkConfigList = childLinksConfig.getLinkConfigList();
        if (linkConfigList.isEmpty()) {
            return false;
        }
        for (LinkConfig linkConfig : linkConfigList) {
            String linkName = linkConfig.getName();
            if (toCompareName.equals(linkName)) {
                return true;
            }
        }
        return false;
    }

    private void validatePluginHandlers(LinkConfig linkConfig, LogicalErrors logicalErrors) {
      String linkName = linkConfig.getName();
      LinkPluginDefinition pluginDefinition = linkConfig.getPluginDefinition();

       if (pluginDefinition == null) {

            return;
        }

      PluginConfig pluginConfig = pluginDefinition.getPluginConfig();

        if (pluginConfig == null) {
            String error = String.format("Could not find plugin handler for link with name '%s'", linkName);
            logger.error(error);
            logicalErrors.addError(error);
            return;
        }

        String componentName = pluginConfig.getComponentName();

        Object bean = null;
        try{
          bean = context.getBean(componentName);
        } catch (BeansException exception) {
            String error = String.format("Could not find plugin handler for link with name '%s'", componentName);
            logger.error(error);
            logicalErrors.addError(error);
            return;
        }

        Class clazz = bean.getClass();
        validatePluginHandlerExtending(clazz, componentName, logicalErrors);

    }

    private void validatePluginHandlerExtending(Class clazz, String componentName, LogicalErrors logicalErrors) {
        Class  parentClass = clazz.getSuperclass();

        if (parentClass == null) {
            String error = String.format("Could not find plugin handler for widget with name '%s'", componentName);
            logger.error(error);
            logicalErrors.addError(error);
            return;
        }

        String parentClassFullName = parentClass.getCanonicalName();
        if (PlUGIN_HANDLER_FULL_QUALIFIED_NAME.equalsIgnoreCase(parentClassFullName)) {
            return;
        }
        validatePluginHandlerExtending(parentClass, componentName, logicalErrors);

    }
}
