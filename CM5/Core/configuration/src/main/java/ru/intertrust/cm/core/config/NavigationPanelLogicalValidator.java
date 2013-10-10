package ru.intertrust.cm.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.intertrust.cm.core.config.model.gui.navigation.ChildLinksConfig;
import ru.intertrust.cm.core.config.model.gui.navigation.LinkConfig;
import ru.intertrust.cm.core.config.model.gui.navigation.NavigationConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 10/9/13
 *         Time: 12:05 PM
 */
public class NavigationPanelLogicalValidator {

    private static final Logger logger = LoggerFactory.getLogger(NavigationPanelLogicalValidator.class);

    private List<LogicalErrors> validationLogicalErrors;
    private ConfigurationExplorer configurationExplorer;

    public NavigationPanelLogicalValidator(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
        validationLogicalErrors = new ArrayList<LogicalErrors>();
    }

    public List<LogicalErrors> getValidationLogicalErrors() {
        return validationLogicalErrors;
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
            validateExistingChild(linkConfig, logicalErrors);
            //validatePluginHandlers();
        }

        validationLogicalErrors.add(logicalErrors);
    }

    private void validateExistingChild(LinkConfig linkConfig, LogicalErrors logicalErrors) {
        boolean isFound = false;
        String childToOpen = linkConfig.getChildToOpen();
        List<ChildLinksConfig> linkConfigList = linkConfig.getChildLinksConfigList();
        if (childToOpen != null) {

            if (linkConfigList.isEmpty()) {
                logger.error("Child link to open is not found for link with name '{}'", linkConfig.getName());
                logicalErrors.addError(String.
                        format("Child link to open is not found for link with name '%s'", linkConfig.getName()));

            }
            for (ChildLinksConfig childLinksConfig : linkConfigList) {
                isFound = findLinkByName(childLinksConfig, childToOpen);
                if (!isFound) {
                    logger.error("Child link to open is not found for link with name '{}'", linkConfig.getName());
                    logicalErrors.addError(String.
                            format("Child link to open is not found for link with name '%s'", linkConfig.getName()));
                }
            }
        }
        findInsideChildLinksAttributeChildToOpen(linkConfigList, logicalErrors);
    }

    private void findInsideChildLinksAttributeChildToOpen(List<ChildLinksConfig> childLinksConfigList , LogicalErrors logicalErrors) {
        for (ChildLinksConfig childLinksConfig : childLinksConfigList) {
        List<LinkConfig> linkConfigList = childLinksConfig.getLinkConfigList();
        for(LinkConfig linkConfig : linkConfigList) {
            validateExistingChild(linkConfig, logicalErrors);
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
    /*private void validatePluginHandlers() {
        Plugin navigationTreePlugin = ComponentRegistry.instance.get("navigation.tree");
        if(navigationTreePlugin != null) {
            logger.info("Navigation tree plugin was found");
        }  else {
            logger.info("Navigation tree plugin was not found");
        }
    }*/
}
