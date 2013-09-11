package ru.intertrust.cm.core.config.model.gui.navigation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.intertrust.cm.core.config.ConfigurationException;
import ru.intertrust.cm.core.config.ConfigurationExplorer;

import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA. User: Yaroslav Bondarchuk Date: 06.09.13 Time: 14:29 To change
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
        Collection<NavigationConfig> navigationConfigList =
                configurationExplorer.getConfigs(NavigationConfig.class);
        if (navigationConfigList.isEmpty()) {
            System.out.println("empty list");
            return;
        }
        for (NavigationConfig navigationConfig : navigationConfigList) {
            validateNavigateConfig(navigationConfig);
        }
        System.out.println("validated!");
        logger.info("Document has passed logical validation");
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

    private void validateLinkConfig(LinkConfig linkConfig) {
        if (linkConfig == null) {
            return;
        }

        List<ChildLinksConfig> childLinksConfigList = linkConfig.getChildLinksConfigList();

        if (childLinksConfigList == null) {
            return;
        }
        for (ChildLinksConfig childLinksConfig : childLinksConfigList) {
            List<LinkConfig> linkConfigList = childLinksConfig.getLinkConfigList();
            if (linkConfigList == null) {
                return;
            }
            validateLinkConfigContainsLink(linkConfigList, linkConfig);
        }
    }

    private void validateLinkConfigContainsLink(List<LinkConfig> linkConfigList,
                                                LinkConfig linkConfigPrototype) {
        for (LinkConfig linkConfigSerialized : linkConfigList) {
            if (!linkConfigSerialized.equals(linkConfigPrototype)) {
                return;
            }
        }
        throw new ConfigurationException("LinkConfig with name '" + linkConfigPrototype.getName() + "' has self link  '");

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
