package ru.intertrust.cm.core.config.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.model.gui.navigation.ChildLinksConfig;

import java.io.Serializable;

/**
 * Author: Denis Mitavskiy
 * Date: 14.06.13
 * Time: 16:02
 */
@Root(name="link")
public class LinkConfig implements Serializable {
    @Attribute(name = "name")
    private String name;

    @Attribute(name = "display-text")
    private String displayText;

    @Attribute(name = "image", required = false)
    private String image;

    @Attribute(name = "child-to-open", required = false)
    private String childToOpen;

    @Element(name = "link", required = false)
    private LinkConfig linkConfig;

    @Element(name = "plugin", required = false)
    private PluginConfig pluginConfig;

    @Element(name = "child-links", required = false)
    private ChildLinksConfig childLinksConfig;

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

    public LinkConfig getLinkConfig() {
        return linkConfig;
    }

    public void setLinkConfig(LinkConfig linkConfig) {
        this.linkConfig = linkConfig;
    }

    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }

    public void setPluginConfig(PluginConfig pluginConfig) {
        this.pluginConfig = pluginConfig;
    }

    public ChildLinksConfig getChildLinksConfig() {
        return childLinksConfig;
    }

    public void setChildLinksConfig(ChildLinksConfig childLinksConfig) {
        this.childLinksConfig = childLinksConfig;
    }
}
