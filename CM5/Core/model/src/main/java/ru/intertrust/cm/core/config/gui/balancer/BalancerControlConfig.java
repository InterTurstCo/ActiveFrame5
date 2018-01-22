package ru.intertrust.cm.core.config.gui.balancer;

import ru.intertrust.cm.core.config.gui.navigation.PluginConfig;

/**
 * Created by Ravil on 22.01.2018.
 */
public class BalancerControlConfig extends PluginConfig {
    private static final String COMPONENT_NAME = "BalancerControl.plugin";
    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BalancerControlConfig that = (BalancerControlConfig) o;
        return true;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
