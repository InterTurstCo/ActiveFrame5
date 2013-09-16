package ru.intertrust.cm.core.config.model.gui.navigation;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
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
@Root(strict = false, name = "child-links")
public class ChildLinksConfig implements Serializable {

    @Attribute(name = "group-name", required = false)
    private String groupName;

    @ElementList(inline = true, required = false)
    private List<LinkConfig> linkConfigList = new ArrayList<LinkConfig>();

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<LinkConfig> getLinkConfigList() {
        return linkConfigList;
    }

    public void setLinkConfigList(List<LinkConfig> linkConfigList) {
        this.linkConfigList = linkConfigList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ChildLinksConfig that = (ChildLinksConfig) o;

        if (linkConfigList != null ? !linkConfigList.equals(that.getLinkConfigList()) :
                that.getLinkConfigList() != null) {
            return false;
        }

        if (groupName != null ? !groupName.equals(that.getGroupName()) : that.getGroupName() != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = linkConfigList != null ? linkConfigList.hashCode() : 0;
        result = 23 * result + groupName != null ? groupName.hashCode() : 0;
        return result;
    }
}

