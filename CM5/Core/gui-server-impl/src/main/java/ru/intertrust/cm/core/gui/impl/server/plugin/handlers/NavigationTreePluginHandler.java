package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.navigation.LinkConfig;
import ru.intertrust.cm.core.config.gui.navigation.NavigationConfig;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.NavigationTreePluginData;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;

import java.util.List;

@ComponentName("navigation.tree")
public class NavigationTreePluginHandler extends PluginHandler {

    @Autowired
    GuiService guiService;

    @Override
    public PluginData initialize(Dto param) {

        NavigationConfig navigationConfig = guiService.getNavigationConfiguration();
        NavigationTreePluginData navigationTreePluginData = new NavigationTreePluginData();
        navigationTreePluginData.setNavigationConfig(navigationConfig);

        LinkConfig rootLinkConfig = takeFirstRootLinkConfig(navigationConfig.getLinkConfigList());

        if (rootLinkConfig != null) {
            String childToOpen = rootLinkConfig.getChildToOpen();
            navigationTreePluginData.setChildToOpen(childToOpen);

            navigationTreePluginData.setRootLinkConfig(rootLinkConfig);
        }
        return navigationTreePluginData;
    }

    public PluginData rootNodeSelected(Dto param) {

        NavigationTreePluginData inputParams = (NavigationTreePluginData) param;

        NavigationConfig navigationConfig = guiService.getNavigationConfiguration();
        LinkConfig rootLinkConfig = takeSelectedRootLinkConfig(navigationConfig.getLinkConfigList(), inputParams.getRootLinkSelectedName());
        String childToOpen = rootLinkConfig.getChildToOpen();
        NavigationTreePluginData out = new NavigationTreePluginData();
        out.setChildToOpen(childToOpen);
        out.setNavigationConfig(navigationConfig);
        out.setRootLinkConfig(rootLinkConfig);
        out.setRootLinkSelectedName(inputParams.getRootLinkSelectedName());
        return out;
    }

    private LinkConfig takeSelectedRootLinkConfig(List<LinkConfig> linkConfigList, String selectedRootLinkName) {
        for (LinkConfig linkConfig : linkConfigList) {
            if (linkConfig.getName().equals(selectedRootLinkName)) {
                return linkConfig;
            }
        }
        return null;
    }

    private LinkConfig takeFirstRootLinkConfig(List<LinkConfig> linkConfigList) {
        if (!linkConfigList.isEmpty()) {
            return linkConfigList.iterator().next();
        } else {
            return null;
        }
    }

}
