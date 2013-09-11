package ru.intertrust.cm.core.config.model.gui.navigation;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.model.TopLevelConfig;

/**
 * Author: Denis Mitavskiy Date: 14.06.13 Time: 16:01
 */
@Root(name = "configuration", strict = false)
public class NavigationConfig implements TopLevelConfig {
    @ElementList(name = "navigation", required = false)
    private List<LinkConfig> linkConfigList = new ArrayList<LinkConfig>();

    @Attribute(name = "name")
    private String name;

    @Attribute(name = "is-default")
    private boolean isDefault;

    @Override
    public String getName() {
        return name;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NavigationConfig that = (NavigationConfig) o;

        if (linkConfigList != null ? !linkConfigList.equals(that.getLinkConfigList()) : that.getLinkConfigList() != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = linkConfigList != null ? linkConfigList.hashCode() : 0;
        return result * 23;
    }
}

