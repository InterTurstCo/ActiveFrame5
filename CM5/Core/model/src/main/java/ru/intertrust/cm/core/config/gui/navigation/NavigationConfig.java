package ru.intertrust.cm.core.config.gui.navigation;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.base.TopLevelConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 10/9/13
 *         Time: 12:05 PM
 */
@Root(name = "navigation", strict = false)
public class NavigationConfig implements TopLevelConfig {
    @ElementList(name = "link", required = false, inline = true)
    private List<LinkConfig> linkConfigList = new ArrayList<LinkConfig>();

    @Attribute(name = "name")
    private String name;

    @Attribute(name = "is-default")
    private boolean isDefault;

    @Override
    public String getName() {
        return name;
    }

    //Used for history and breadcrumbs support in hierarchical collections
    //Not shown in navigation tree.
    private List<LinkConfig> hierarchicalLinkList = new ArrayList<LinkConfig>();

    public List<LinkConfig> getLinkConfigList() {
        return linkConfigList;
    }

    public void setLinkConfigList(List<LinkConfig> linkConfigList) {
        this.linkConfigList = linkConfigList;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public List<LinkConfig> getHierarchicalLinkList() {
        return hierarchicalLinkList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NavigationConfig that = (NavigationConfig) o;

        if (linkConfigList != null ? !linkConfigList.equals(that.getLinkConfigList()) : that.
                getLinkConfigList() != null) {
            return false;
        }

        if (isDefault != that.isDefault()) {
            return false;
        }

        if (name != null ? !name.equals(that.getName()) : that.getName() != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = linkConfigList != null ? linkConfigList.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (isDefault ? 1 : 0);
        return result;
    }
}

