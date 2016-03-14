package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.navigation.LinkConfig;
import ru.intertrust.cm.core.config.gui.navigation.NavigationConfig;
import ru.intertrust.cm.core.config.gui.navigation.NavigationPanelSecondLevelDefaultState;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.api.server.UserSettingsFetcher;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.NavigationTreePluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.NavigationTreePluginData;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;
import ru.intertrust.cm.core.gui.model.util.UserSettingsHelper;

import java.util.List;

@ComponentName("navigation.tree")
public class NavigationTreePluginHandler extends PluginHandler {

    @Autowired
    GuiService guiService;

    @Autowired
    private UserSettingsFetcher userSettingsFetcher;

    @org.springframework.beans.factory.annotation.Value("${base.url.1:http://localhost:8080}")
    private String baseUrlOne;

    @org.springframework.beans.factory.annotation.Value("${base.url.2:http://localhost:8080}")
    private String baseUrlTwo;

    @Override
    public PluginData initialize(Dto param) {
        NavigationConfig navigationConfig;
        NavigationTreePluginConfig navigationTreePluginConfig = (NavigationTreePluginConfig)param;
        if(navigationTreePluginConfig.getApplicationName()!=null){
             navigationConfig = guiService.getNavigationConfiguration(navigationTreePluginConfig.getApplicationName());
        } else {
             navigationConfig = guiService.getNavigationConfiguration();
        }
        navigationConfig.setBaseUrlOne(baseUrlOne);
        navigationConfig.setBaseUrlTwo(baseUrlTwo);

        NavigationTreePluginData navigationTreePluginData = new NavigationTreePluginData();
        navigationTreePluginData.setNavigationConfig(navigationConfig);

        LinkConfig rootLinkConfig = takeFirstRootLinkConfig(navigationConfig.getLinkConfigList());

        if (rootLinkConfig != null) {
            String childToOpen = rootLinkConfig.getChildToOpen();
            navigationTreePluginData.setChildToOpen(childToOpen);

            navigationTreePluginData.setRootLinkConfig(rootLinkConfig);
        }
        DomainObject domainObject = userSettingsFetcher.getUserSettingsDomainObject(true);
        Boolean pinnedByUser = domainObject.getBoolean(UserSettingsHelper.DO_NAVIGATION_PANEL_SECOND_LEVEL_PINNED_KEY);
        boolean pinned = pinnedByUser == null
                ? NavigationPanelSecondLevelDefaultState.PINNED_STATE.equals(navigationConfig.getSecondLevelDefaultState())
                : pinnedByUser;
        navigationTreePluginData.setPinned(pinned);
        boolean hasSecondLevelNavigationPanel = hasSecondLevelNavigationPanel(navigationConfig.getLinkConfigList());
        navigationTreePluginData.setHasSecondLevelNavigationPanel(hasSecondLevelNavigationPanel);
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

    private boolean hasSecondLevelNavigationPanel(List<LinkConfig> linkConfigList) {
        boolean result = false;
        for (LinkConfig linkConfig : linkConfigList) {
            if(!linkConfig.getChildLinksConfigList().isEmpty()){
                result = true;
                break;
            }
        }
        return result;
    }

}
