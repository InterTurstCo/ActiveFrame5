package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.config.gui.navigation.LinkConfig;
import ru.intertrust.cm.core.config.gui.navigation.NavigationConfig;
import ru.intertrust.cm.core.config.gui.navigation.PluginConfig;

public class NavigationTreePluginData extends PluginData {

    public NavigationTreePluginData() {
    }

    private NavigationConfig navigationConfig;
    private LinkConfig rootLinkConfig;
    private PluginConfig pluginConfig;
    private String childToOpen;
    private String rootLinkSelectedName;
    private Integer sideBarOpenningTime;
    private boolean pinned;
    private boolean hasSecondLevelNavigationPanel = true;
    public String getChildToOpen() {
        return childToOpen;
    }

    public void setChildToOpen(String childToOpen) {
        this.childToOpen = childToOpen;
    }


    public NavigationConfig getNavigationConfig() {
        return navigationConfig;
    }

    public void setNavigationConfig(NavigationConfig navigationConfig) {
        this.navigationConfig = navigationConfig;
    }

    public String getRootLinkSelectedName() {
        return rootLinkSelectedName;
    }

    public void setRootLinkSelectedName(String rootLinkSelectedName) {
        this.rootLinkSelectedName = rootLinkSelectedName;
    }

    public LinkConfig getRootLinkConfig() {
        return rootLinkConfig;
    }

    public void setRootLinkConfig(LinkConfig rootLinkConfig) {
        this.rootLinkConfig = rootLinkConfig;
    }

    public Integer getSideBarOpenningTime() {
        return sideBarOpenningTime;
    }

    public void setSideBarOpenningTime(Integer sideBarOpenningTime) {
        this.sideBarOpenningTime = sideBarOpenningTime;
    }

    public Boolean isPinned() {
        return pinned;
    }

    public void setPinned(Boolean pinned) {
        this.pinned = pinned;
    }

    public boolean hasSecondLevelNavigationPanel() {
        return hasSecondLevelNavigationPanel;
    }

    public void setHasSecondLevelNavigationPanel(boolean hasSecondLevelNavigationPanel) {
        this.hasSecondLevelNavigationPanel = hasSecondLevelNavigationPanel;
    }
}
