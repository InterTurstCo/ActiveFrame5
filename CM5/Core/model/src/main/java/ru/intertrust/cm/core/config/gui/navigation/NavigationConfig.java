package ru.intertrust.cm.core.config.gui.navigation;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.base.LocalizableConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 10/9/13
 *         Time: 12:05 PM
 */
@Root(name = "navigation", strict = false)
public class NavigationConfig implements LocalizableConfig {
    @ElementList(name = "link", required = false, inline = true)
    private List<LinkConfig> linkConfigList = new ArrayList<LinkConfig>();

    @Attribute(name = "name")
    private String name;

    @Attribute(name = "replace", required = false)
    private String replacementPolicy;

    @Attribute(name = "application",required = false)
    private String application;

    @Attribute(name = "is-default")
    private boolean isDefault;

    @Attribute(name = "side-bar-opening-time", required = false)
    private Integer sideBarOpeningTime;

    @Attribute(name = "merge", required = false)
    private boolean merge;

    @Attribute(name = "level2-panel-width", required = false)
    private String secondLevelPanelWidth;

    @Attribute(name = "level2-default-state", required = false)
    private String secondLevelDefaultState;

    @Attribute(name = "unpin-enabled", required = false)
    private boolean unpinEnabled = true;

    @Attribute(name = "margin-size", required = false)
    private String marginSize;

    @Attribute(name = "text-auto-cut", required = false)
    private boolean textAutoCut = true;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ExtensionPolicy getReplacementPolicy() {
        return ExtensionPolicy.fromString(replacementPolicy);
    }

    @Override
    public ExtensionPolicy getCreationPolicy() {
        return ExtensionPolicy.Runtime;
    }

    private String baseUrlOne;

    private String baseUrlTwo;

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

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
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

    public Integer getSideBarOpeningTime() {
        return sideBarOpeningTime;
    }

    public void setSideBarOpeningTime(Integer sideBarOpeningTime) {
        this.sideBarOpeningTime = sideBarOpeningTime;
    }

    public boolean isMerge() {
        return merge;
    }

    public String getSecondLevelPanelWidth() {
        return secondLevelPanelWidth;
    }

    public void setSecondLevelPanelWidth(String secondLevelPanelWidth) {
        this.secondLevelPanelWidth = secondLevelPanelWidth;
    }

    public NavigationPanelSecondLevelDefaultState getSecondLevelDefaultState() {
        return NavigationPanelSecondLevelDefaultState.forState(secondLevelDefaultState);
    }

    public void setSecondLevelDefaultState(String secondLevelDefaultState) {
        this.secondLevelDefaultState = secondLevelDefaultState;
    }

    public boolean isUnpinEnabled() {
        return unpinEnabled;
    }

    public void setUnpinEnabled(boolean unpinEnabled) {
        this.unpinEnabled = unpinEnabled;
    }

    public NavigationPanelSecondLevelMarginSize getMarginSize() {
        return NavigationPanelSecondLevelMarginSize.forCode(marginSize);
    }

    public void setMarginSize(String marginSize) {
        this.marginSize = marginSize;
    }

    public boolean isTextAutoCut() {
        return textAutoCut;
    }

    public void setTextAutoCut(boolean textAutoCut) {
        this.textAutoCut = textAutoCut;
    }

    public String getBaseUrlOne() {
        return baseUrlOne;
    }

    public void setBaseUrlOne(String baseUrlOne) {
        this.baseUrlOne = baseUrlOne;
    }

    public String getBaseUrlTwo() {
        return baseUrlTwo;
    }

    public void setBaseUrlTwo(String baseUrlTwo) {
        this.baseUrlTwo = baseUrlTwo;
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

        if (isDefault != that.isDefault) {
            return false;
        }
        if (hierarchicalLinkList != null ? !hierarchicalLinkList.equals(that.hierarchicalLinkList)
                : that.hierarchicalLinkList != null) {
            return false;
        }
        if (linkConfigList != null ? !linkConfigList.equals(that.linkConfigList) : that.linkConfigList != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (replacementPolicy != null ? !replacementPolicy.equals(that.replacementPolicy) : that.replacementPolicy != null) {
            return false;
        }
        if (application != null ? !application.equals(that.application) : that.application != null) {
            return false;
        }
        if (sideBarOpeningTime != null ? !sideBarOpeningTime.equals(that.sideBarOpeningTime) : that.sideBarOpeningTime != null) {
            return false;
        }
        if (merge != that.merge) {
            return false;
        }
        if (secondLevelPanelWidth != null ? !secondLevelPanelWidth.equals(that.secondLevelPanelWidth)
                : that.secondLevelPanelWidth != null) {
            return false;
        }
        if (secondLevelDefaultState != null ? !secondLevelDefaultState.equals(that.secondLevelDefaultState)
                : that.secondLevelDefaultState != null) {
            return false;
        }
        if (unpinEnabled != that.unpinEnabled) {
            return false;
        }
        if (marginSize != null ? !marginSize.equals(that.marginSize) : that.marginSize != null) {
            return false;
        }
        if (textAutoCut != that.textAutoCut) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return  31 * (name != null ? name.hashCode() : 0);

    }

}
