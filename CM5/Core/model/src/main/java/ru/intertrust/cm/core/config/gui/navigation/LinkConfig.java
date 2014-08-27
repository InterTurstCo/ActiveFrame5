package ru.intertrust.cm.core.config.gui.navigation;

import org.simpleframework.xml.*;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.navigation.counters.CounterType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 10/9/13
 *         Time: 12:05 PM
 */
@Root(name = "link")
public class  LinkConfig implements Dto {

    @Attribute(name = "name")
    private String name;

    @Attribute(name = "display-text", required = false)
    private String displayText;

    @Attribute(name = "image", required = false)
    private String image;

    @Attribute(name = "display-counter", required = false)
    private boolean displayCounter = true;

    @Attribute(name = "counter-type", required = false)
    private String counterType;

    @Attribute(name = "child-to-open", required = false)
    private String childToOpen;

    @Element(name = "plugin", required = false)
    private LinkPluginDefinition pluginDefinition;

    // Области поиска для расширенного поиска
    @Element(name = "default-search-areas", required = false)
    private DefaultSearchAreasConfig defaultSearchAreasConfig;

    @ElementList(inline = true, required = false)
    private List<ChildLinksConfig> childLinksConfigList = new ArrayList<ChildLinksConfig>();

    @Element(name = "decorations", required = false)
    private DecorationsConfig decorationsConfig;

    @Transient
    private LinkConfig parentLinkConfig;
    @Transient
    private ChildLinksConfig parentChildLinksConfig;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }

    public String getImage() {
        return image;
    }

    public String getChildToOpen() {
        return childToOpen;
    }

    public void setChildToOpen(String childToOpen) {
        this.childToOpen = childToOpen;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public LinkPluginDefinition getPluginDefinition() {
        return pluginDefinition;
    }

    public void setPluginDefinition(LinkPluginDefinition pluginDefinition) {
        this.pluginDefinition = pluginDefinition;
    }

    public DefaultSearchAreasConfig getDefaultSearchAreasConfig() {
        return defaultSearchAreasConfig;
    }

    public void setDefaultSearchAreasConfig(DefaultSearchAreasConfig defaultSearchAreasConfig) {
        this.defaultSearchAreasConfig = defaultSearchAreasConfig;
    }

    public List<ChildLinksConfig> getChildLinksConfigList() {
        return childLinksConfigList;
    }

    public void setChildLinksConfigList(List<ChildLinksConfig> childLinksConfigList) {
        this.childLinksConfigList = childLinksConfigList;
    }

    public DecorationsConfig getDecorationsConfig() {
        return decorationsConfig;
    }

    public void setDecorationsConfig(DecorationsConfig decorationsConfig) {
        this.decorationsConfig = decorationsConfig;
    }

    public boolean isDisplayCounter() {
        return displayCounter;
    }

    public void setDisplayCounter(boolean displayCounter) {
        this.displayCounter = displayCounter;
    }

    public String getCounterType() {
        return counterType == null ? CounterType.ALL.value() : counterType;
    }

    public void setCounterType(String counterType) {
        this.counterType = counterType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LinkConfig that = (LinkConfig) o;

        if (displayCounter != that.displayCounter) {
            return false;
        }
        if (childLinksConfigList != null ? !childLinksConfigList.equals(that.childLinksConfigList)
                : that.childLinksConfigList != null) {
            return false;
        }
        if (childToOpen != null ? !childToOpen.equals(that.childToOpen) : that.childToOpen != null) {
            return false;
        }
        if (counterType != that.counterType) {
            return false;
        }
        if (decorationsConfig != null ? !decorationsConfig.equals(that.decorationsConfig)
                : that.decorationsConfig != null) {
            return false;
        }
        if (defaultSearchAreasConfig != null ? !defaultSearchAreasConfig.equals(that.defaultSearchAreasConfig)
                : that.defaultSearchAreasConfig != null) {
            return false;
        }
        if (displayText != null ? !displayText.equals(that.displayText) : that.displayText != null) {
            return false;
        }
        if (image != null ? !image.equals(that.image) : that.image != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (pluginDefinition != null ? !pluginDefinition.equals(that.pluginDefinition) : that.pluginDefinition != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (displayText != null ? displayText.hashCode() : 0);
        result = 31 * result + (image != null ? image.hashCode() : 0);
        result = 31 * result + (displayCounter ? 1 : 0);
        result = 31 * result + (counterType != null ? counterType.hashCode() : 0);
        result = 31 * result + (childToOpen != null ? childToOpen.hashCode() : 0);
        result = 31 * result + (pluginDefinition != null ? pluginDefinition.hashCode() : 0);
        result = 31 * result + (defaultSearchAreasConfig != null ? defaultSearchAreasConfig.hashCode() : 0);
        result = 31 * result + (childLinksConfigList != null ? childLinksConfigList.hashCode() : 0);
        result = 31 * result + (decorationsConfig != null ? decorationsConfig.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return new StringBuilder(LinkConfig.class.getSimpleName())
                .append(": name=").append(name)
                .append(", displayText=").append(displayText)
                .toString();
    }

    public LinkConfig getParentLinkConfig() {
        return parentLinkConfig;
    }

    public void setParentLinkConfig(LinkConfig parentLinkConfig) {
        this.parentLinkConfig = parentLinkConfig;
    }

    public ChildLinksConfig getParentChildLinksConfig() {
        return parentChildLinksConfig;
    }

    public void setParentChildLinksConfig(ChildLinksConfig parentChildLinksConfig) {
        this.parentChildLinksConfig = parentChildLinksConfig;
    }
}
