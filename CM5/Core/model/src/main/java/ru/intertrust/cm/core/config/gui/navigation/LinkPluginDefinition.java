package ru.intertrust.cm.core.config.gui.navigation;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementUnion;
import org.simpleframework.xml.Root;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.globalcachecontrol.GlobalCacheControlConfig;
import ru.intertrust.cm.core.config.gui.navigation.calendar.CalendarConfig;
import ru.intertrust.cm.core.config.gui.navigation.hierarchyplugin.HierarchyPluginConfig;
import ru.intertrust.cm.core.config.gui.navigation.hierarchyplugin.HierarchySurferConfig;
import ru.intertrust.cm.core.config.gui.navigation.listplugin.ListPluginConfig;
import ru.intertrust.cm.core.config.gui.navigation.listplugin.ListSurferConfig;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 10/9/13
 *         Time: 12:05 PM
 */
@Root(name = "plugin")
public class LinkPluginDefinition implements Dto {

    @ElementUnion({
            @Element(name = "custom", type = CustomPluginConfig.class),
            @Element(name = "domain-object-surfer", type = DomainObjectSurferConfig.class),
            @Element(name = "calendar", type = CalendarConfig.class),
            @Element(name = "report-plugin", type = ReportPluginConfig.class),
            @Element(name = "global-cache-control", type = GlobalCacheControlConfig.class),
            @Element(name = "hierarchy-plugin", type = HierarchyPluginConfig.class),
            @Element(name = "hierarchy-surfer", type = HierarchySurferConfig.class),
            @Element(name = "list-plugin", type = ListPluginConfig.class),
            @Element(name = "list-surfer", type = ListSurferConfig.class)

    })
    private PluginConfig pluginConfig;

    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }

    public void setPluginConfig(PluginConfig pluginConfig) {
        this.pluginConfig = pluginConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LinkPluginDefinition that = (LinkPluginDefinition) o;

        if (pluginConfig != null ? !pluginConfig.equals(that.getPluginConfig()) : that.
                getPluginConfig() != null) {
                    return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return pluginConfig != null ? pluginConfig.hashCode() : 0;
    }
}

