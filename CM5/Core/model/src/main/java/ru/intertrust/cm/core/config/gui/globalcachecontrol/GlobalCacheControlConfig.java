package ru.intertrust.cm.core.config.gui.globalcachecontrol;

import org.simpleframework.xml.Attribute;
import ru.intertrust.cm.core.config.gui.navigation.PluginConfig;

/**
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 21.10.2015
 */
public class GlobalCacheControlConfig extends PluginConfig {
    private static final String COMPONENT_NAME = "GlobalCacheControl.plugin";

    @Attribute(name="statistics-only", required = false, empty = "false")
    private Boolean statisticsOnly;

    public Boolean getStatisticsOnly() {
        return statisticsOnly;
    }

    public void setStatisticsOnly(Boolean statisticsOnly) {
        this.statisticsOnly = statisticsOnly;
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }
}
