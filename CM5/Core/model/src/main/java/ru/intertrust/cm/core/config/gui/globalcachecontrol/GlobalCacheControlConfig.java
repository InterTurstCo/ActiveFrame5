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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GlobalCacheControlConfig that = (GlobalCacheControlConfig) o;

        if (statisticsOnly != null ? !statisticsOnly.equals(that.statisticsOnly) : that.statisticsOnly != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return statisticsOnly != null ? statisticsOnly.hashCode() : 0;
    }
}
