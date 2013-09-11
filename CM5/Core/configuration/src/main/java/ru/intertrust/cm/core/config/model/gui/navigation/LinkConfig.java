package ru.intertrust.cm.core.config.model.gui.navigation;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Denis Mitavskiy Date: 14.06.13 Time: 16:02
 */
@SuppressWarnings("serial")
@Root(strict = false, name = "link")
public class LinkConfig implements Serializable {

    @Attribute(name = "name")
    private String name;

    @Attribute(name = "display-text", required = false)
    private String displayText;

    @Attribute(name = "image", required = false)
    private String image;

    @Attribute(name = "child-to-open", required = false)
    private String childToOpen;

    @Element(name = "plugin", required = false)
    private PluginConfig pluginConfig;

    @ElementList(inline = true, required = false)
    private List<ChildLinksConfig> childLinksConfigList = new ArrayList<ChildLinksConfig>();

    @Element(name = "decorations", required = false)
    private DecorationsConfig decorationsConfig;

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

    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }

    public void setPluginConfig(PluginConfig pluginConfig) {
        this.pluginConfig = pluginConfig;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LinkConfig that = (LinkConfig) o;

        if (childLinksConfigList != null ? !childLinksConfigList.equals(that.getChildLinksConfigList()) : that.getChildLinksConfigList() != null) {
            return false;
        }

        if (name != null ? !name.equals(that.getName()) : that.getName() != null) {
            return false;
        }

        if (displayText != null ? !displayText.equals(that.getDisplayText()) : that.getDisplayText() != null) {
            return false;
        }

        if (image != null ? !image.equals(that.getImage()) : that.getImage() != null) {
            return false;
        }

        if (childToOpen != null ? !childToOpen.equals(that.getChildToOpen()) : that.getChildToOpen() != null) {
            return false;
        }

        if (pluginConfig != null ? !pluginConfig.equals(that.getPluginConfig()) : that.getPluginConfig() != null) {
            return false;
        }

        if (decorationsConfig != null ? !decorationsConfig.equals(that.getDecorationsConfig()) : that.getDecorationsConfig() != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = childLinksConfigList != null ? childLinksConfigList.hashCode() : 0;
        result = 23 * result + (name != null ? name.hashCode() : 0);
        result = result + (displayText != null ? displayText.hashCode() : 0);
        result = result + (image != null ? image.hashCode() : 0);
        result = result + (childToOpen != null ? childToOpen.hashCode() : 0);
        result = result + (pluginConfig != null ? pluginConfig.hashCode() : 0);
        result = result + (decorationsConfig != null ? decorationsConfig.hashCode() : 0);
        return result;
    }

}
