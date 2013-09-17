package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.config.model.gui.navigation.NavigationConfig;

public class NavigationTreePluginData extends PluginData {

    public NavigationTreePluginData() {
    }

    private NavigationConfig navigationConfig;

    public NavigationConfig getNavigationConfig() {
        return navigationConfig;
    }

    public void setNavigationConfig(NavigationConfig navigationConfig) {
        this.navigationConfig = navigationConfig;
    }
}
