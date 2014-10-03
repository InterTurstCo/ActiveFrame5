package ru.intertrust.cm.core.gui.impl.client.util;

import ru.intertrust.cm.core.config.gui.navigation.ChildLinksConfig;
import ru.intertrust.cm.core.config.gui.navigation.LinkConfig;
import ru.intertrust.cm.core.config.gui.navigation.NavigationConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.history.HistoryManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 24.09.14
 *         Time: 20:02
 */
public class LinkUtil {

    private LinkUtil(){} // non-instantiable

    public static void markNavigationHierarchy(List<LinkConfig> linkConfigList, LinkConfig parent, ChildLinksConfig parentChildLinksConfig) {
        for (LinkConfig linkConfig : linkConfigList) {
            linkConfig.setParentLinkConfig(parent);
            linkConfig.setParentChildLinksConfig(parentChildLinksConfig);
            for (ChildLinksConfig childLinksConfig : linkConfig.getChildLinksConfigList()) {
                markNavigationHierarchy(childLinksConfig.getLinkConfigList(), linkConfig, childLinksConfig);
            }
        }
    }


    public static void findLink(String link, List<LinkConfig> linkConfigList, List<LinkConfig> results) {
        for (LinkConfig linkConfig : linkConfigList) {
            if (linkConfig.getName().equals(link)) {
                results.add(linkConfig);
                return;
            }
            for (ChildLinksConfig childLinksConfig : linkConfig.getChildLinksConfigList()) {
                findLink(link, childLinksConfig.getLinkConfigList(), results);
            }
        }
    }

    public static void addHierarchicalLinkToNavigationConfig(NavigationConfig navigationConfig, LinkConfig link) {
        LinkConfig parentLink = findParentLink(navigationConfig, link);
        link.setParentLinkConfig(parentLink);
        if (!navigationConfig.getHierarchicalLinkList().contains(link)) {
            navigationConfig.getHierarchicalLinkList().add(link);
        }
    }

    private static LinkConfig findParentLink(NavigationConfig navigationConfig, LinkConfig link) {
        LinkConfig parentLinkConfig = null;
        HistoryManager manager = Application.getInstance().getHistoryManager();
        String parentLinkName = manager.getLink();
        List<LinkConfig> results = new ArrayList<>();
        LinkUtil.findLink(parentLinkName, navigationConfig.getLinkConfigList(), results);
        if (results.isEmpty()) {
            LinkUtil.findLink(parentLinkName, navigationConfig.getHierarchicalLinkList(), results);
        }
        if (!results.isEmpty()) {
            parentLinkConfig = results.get(0);
        }
        return parentLinkConfig;
    }
}
