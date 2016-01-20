package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.config.gui.navigation.PluginConfig;

/**
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 16.09.2015
 */
public class NavigationTreePluginConfig extends PluginConfig {

    private String applicationName;

    @Override
    public String getComponentName() {
        return "navigation.tree";
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }
}
