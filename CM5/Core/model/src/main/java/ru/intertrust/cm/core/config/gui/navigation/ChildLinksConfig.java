package ru.intertrust.cm.core.config.gui.navigation;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.base.Localizable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 10/9/13
 *         Time: 12:05 PM
 */

@Root(strict = false, name = "child-links")
public class ChildLinksConfig implements Dto {

    @Attribute(name = "group-name", required = false)
    @Localizable
    private String groupName;

    @Attribute(name = "auto-cut", required = false)
    private Boolean autoCut;

    @Attribute(name = "tooltip", required = false)
    @Localizable
    private String tooltip;

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

    public Boolean isAutoCut() {
        return autoCut;
    }

    public void setAutoCut(Boolean autoCut) {
        this.autoCut = autoCut;
    }

    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
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
        if (autoCut != null ? !autoCut.equals(that.autoCut) : that.autoCut != null) {
            return false;
        }
        if (tooltip != null ? !tooltip.equals(that.tooltip) : that.tooltip != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = groupName != null ? groupName.hashCode() : 0;
        result = 31 * result + (linkConfigList != null ? linkConfigList.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return new StringBuilder(ChildLinksConfig.class.getSimpleName())
                .append(": groupName=").append(groupName)
                .append(", childList=").append(linkConfigList)
                .toString();
    }
}

