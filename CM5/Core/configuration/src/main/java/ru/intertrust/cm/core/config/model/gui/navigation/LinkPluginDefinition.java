package ru.intertrust.cm.core.config.model.gui.navigation;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 10/9/13
 *         Time: 12:05 PM
 */
@Root(name = "plugin")
public class LinkPluginDefinition implements Dto {

    @ElementListUnion({
            @ElementList(entry = "custom", type = CustomPluginConfig.class, inline = true),
            @ElementList(entry = "domain-object-surfer", type = DomainObjectSurferConfig.class, inline = true) })
    private List<PluginConfig> pluginConfigList = new ArrayList<PluginConfig>();

    public List<PluginConfig> getPluginConfigList() {
        return pluginConfigList;
    }

    public void setPluginConfigList(List<PluginConfig> pluginConfigList) {
        this.pluginConfigList = pluginConfigList;
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

        if (pluginConfigList != null ? !pluginConfigList.equals(that.getPluginConfigList()) : that.
                getPluginConfigList() != null) {
                    return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return pluginConfigList != null ? pluginConfigList.hashCode() : 0;
    }
}

