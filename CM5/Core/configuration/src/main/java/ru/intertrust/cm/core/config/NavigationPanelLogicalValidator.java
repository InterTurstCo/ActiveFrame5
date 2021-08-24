package ru.intertrust.cm.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.config.gui.navigation.*;

import java.util.*;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 10/9/13
 *         Time: 12:05 PM
 */
public class NavigationPanelLogicalValidator implements ConfigurationValidator {
    private static final String PlUGIN_HANDLER_FULL_QUALIFIED_NAME =
            "ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler";
    private static final Logger logger = LoggerFactory.getLogger(NavigationPanelLogicalValidator.class);

    private List<LogicalErrors> logicalErrorsList = new ArrayList<>();
    private ConfigurationExplorer configurationExplorer;

    private ApplicationContext context;

    public NavigationPanelLogicalValidator() {
    }
    public NavigationPanelLogicalValidator(ConfigurationExplorer configurationExplorer) {
        setConfigurationExplorer(configurationExplorer);
    }

    public ConfigurationExplorer getConfigurationExplorer() {
        return configurationExplorer;
    }

    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
        this.context = ((ConfigurationExplorerImpl) configurationExplorer).getContext();
    }

    /**
     * Выполняет логическую валидацию конфигурации панели навигации
     */
    @Override
    public List<LogicalErrors> validate() {
        Collection<NavigationConfig> navigationConfigList = configurationExplorer.getConfigs(NavigationConfig.class);

        if (navigationConfigList.isEmpty()) {
            logger.info("Navigation Panel config couldn't be resolved");
            return logicalErrorsList;
        }

        for (NavigationConfig navigationConfig : navigationConfigList) {
            logger.info("Validating navigation panel with name '{}'", navigationConfig.getName());
            validateNavigateConfig(navigationConfig);
        }

        return logicalErrorsList;
    }

    private void validateNavigateConfig(NavigationConfig navigationConfig) {
        if (navigationConfig == null) {
            return;
        }
        String navigationPanelName = navigationConfig.getName();
        LogicalErrors logicalErrors = LogicalErrors.getInstance(navigationPanelName, "navigation panel");
        validatePinnedState(navigationConfig, logicalErrors);
        List<LinkConfig> linkConfigList = navigationConfig.getLinkConfigList();

        if (linkConfigList == null) {
            return;
        }
        Set<String> linkNames = new HashSet<>();
        for (LinkConfig linkConfig : linkConfigList) {
            validateLinkConfig(linkConfig, linkNames,logicalErrors);
            validatePluginHandlers(linkConfig, logicalErrors);
        }

        if (logicalErrors.getErrorCount() > 0) {
            logicalErrorsList.add(logicalErrors);
        }
    }

    private void validatePinnedState(NavigationConfig navigationConfig, LogicalErrors logicalErrors){
        if(!navigationConfig.isUnpinEnabled()
                && NavigationPanelSecondLevelDefaultState.UNPINNED_STATE.equals(navigationConfig.getSecondLevelDefaultState())){
            String error = "Default second level panel state  couldn't be 'unpinned' when unpin is disabled";
            logger.error(error);
            logicalErrors.addError(error);
        }
    }

    private void validateNameUniqueness(LinkConfig linkConfig, Set<String> linkNames, LogicalErrors logicalErrors){
        String linkName = linkConfig.getName();
        if(linkNames.contains(linkName)){
            String error =  String.
                    format("Duplicate link name '%s' was found", linkName);
            logger.error(error);
            logicalErrors.addError(error);
        }else {
            linkNames.add(linkName);
        }
    }

    private void validateLinkConfig(LinkConfig linkConfig, Set<String> linkNames, LogicalErrors logicalErrors) {
        validateNameUniqueness(linkConfig, linkNames, logicalErrors);
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
                if (isFound) break;
            }
            if (!isFound) {
                String error =  String.
                        format("Child link to open is not found for link with name '%s'", linkConfig.getName());
                logger.error(error);
                logicalErrors.addError(error);
            }
        }
        validateChildLinks(linkConfigList, linkNames, logicalErrors);
    }

    private void validateChildLinks(List<ChildLinksConfig> childLinksConfigList,Set<String> linkNames,
                                    LogicalErrors logicalErrors) {
        for (ChildLinksConfig childLinksConfig : childLinksConfigList) {
        List<LinkConfig> linkConfigList = childLinksConfig.getLinkConfigList();
        for(LinkConfig linkConfig : linkConfigList) {
            validateLinkConfig(linkConfig, linkNames, logicalErrors);
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
            logger.error(error, exception);
            logicalErrors.addError(error);
            return;
        }

        Class<?> clazz = bean.getClass();
        validatePluginHandlerExtending(clazz, componentName, logicalErrors);

    }

    private void validatePluginHandlerExtending(Class<?> clazz, String componentName, LogicalErrors logicalErrors) {
        Class<?> parentClass = clazz.getSuperclass();

        if (parentClass == null) {
            String error = String.format("Could not find plugin handler for widget with name '%s'", componentName);
            logger.error(error);
            logicalErrors.addError(error);
            return;
        }

        String parentClassFullName = parentClass.getCanonicalName();
        if (getPluginHandlerName().equalsIgnoreCase(parentClassFullName)) {
            return;
        }
        validatePluginHandlerExtending(parentClass, componentName, logicalErrors);

    }

    protected String getPluginHandlerName() {
        return PlUGIN_HANDLER_FULL_QUALIFIED_NAME;
    }
}
