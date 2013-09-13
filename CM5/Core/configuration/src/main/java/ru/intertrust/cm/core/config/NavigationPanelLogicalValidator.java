package ru.intertrust.cm.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.intertrust.cm.core.config.model.gui.navigation.ChildLinksConfig;
import ru.intertrust.cm.core.config.model.gui.navigation.LinkConfig;
import ru.intertrust.cm.core.config.model.gui.navigation.NavigationConfig;

import java.util.Collection;
import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 10/9/13
 *         Time: 12:05 PM
 */
public class NavigationPanelLogicalValidator {
    final static Logger logger = LoggerFactory.getLogger(NavigationPanelLogicalValidator.class);

    private ConfigurationExplorer configurationExplorer;

    public NavigationPanelLogicalValidator(ConfigurationExplorer configurationExplorer) {
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
            validateNavigateConfig(navigationConfig);
        }
        logger.info("Navigation Panel config has passed logical validation");
    }

    private void validateNavigateConfig(NavigationConfig navigationConfig) {
        if (navigationConfig == null) {
            return;
        }
        List<LinkConfig> linkConfigList = navigationConfig.getLinkConfigList();

        if (linkConfigList == null) {
            return;
        }
        for (LinkConfig linkConfig : linkConfigList) {
            validateLinkToOpen(linkConfig);
        }

    }

    private void validateLinkToOpen(LinkConfig linkConfig) {
        boolean isFound = false;
        String childToOpen = linkConfig.getChildToOpen();
        if (childToOpen != null) {
            List<ChildLinksConfig> linkConfigList = linkConfig.getChildLinksConfigList();
            if (linkConfigList.isEmpty()) {
                throw new ConfigurationException("Child link to open is not found for name '" +
                        linkConfig.getName() + "'");
            }
            for (ChildLinksConfig childLinksConfig : linkConfigList) {
                isFound = validateChildLinkForRequiredLink(childLinksConfig, childToOpen);
                if (isFound) {
                    return;
                } else {
                    System.out.println("Child link to open is not found for name '" +
                            linkConfig.getName() + "'");
                    throw new ConfigurationException("Child link to open is not found for name '" +
                            linkConfig.getName() + "'");
                }
            }
        }
    }

    private boolean validateChildLinkForRequiredLink(ChildLinksConfig childLinksConfig, String toCompareName) {
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
}
