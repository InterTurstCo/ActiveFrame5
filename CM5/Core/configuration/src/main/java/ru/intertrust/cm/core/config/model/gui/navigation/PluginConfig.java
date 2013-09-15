package ru.intertrust.cm.core.config.model.gui.navigation;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import org.simpleframework.xml.Root;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 10/9/13
 *         Time: 12:05 PM
 */
@SuppressWarnings("serial")
@Root(name = "plugin")
public class PluginConfig implements Serializable {

    @ElementListUnion({
            @ElementList(entry = "custom", type = CustomPluginConfig.class, inline = true),
            @ElementList(entry = "domain-object-surfer", type = DomainObjectSurferConfig.class, inline = true) })
    private List<PluginConfigParent> pluginConfigParentList = new ArrayList<PluginConfigParent>();

    public List<PluginConfigParent> getPluginConfigParentList() {
        return pluginConfigParentList;
    }

    public void setPluginConfigParentList(List<PluginConfigParent> pluginConfigParentList) {
        this.pluginConfigParentList = pluginConfigParentList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PluginConfig that = (PluginConfig) o;

        if (pluginConfigParentList != null ? !pluginConfigParentList.equals(that.getPluginConfigParentList()) : that.
                getPluginConfigParentList() != null) {
                    return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = pluginConfigParentList != null ? pluginConfigParentList.hashCode() : 0;
        return result * 23;
    }
}

