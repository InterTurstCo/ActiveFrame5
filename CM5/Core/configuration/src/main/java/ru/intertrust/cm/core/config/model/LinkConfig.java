package ru.intertrust.cm.core.config.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

import java.io.Serializable;

/**
 * Author: Denis Mitavskiy
 * Date: 14.06.13
 * Time: 16:02
 */
public class LinkConfig implements Serializable {
    @Attribute(name = "name")
    private String name;

    @Element(name = "link", required = false)
    private LinkConfig linkConfig;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LinkConfig getLinkConfig() {
        return linkConfig;
    }

    public void setLinkConfig(LinkConfig linkConfig) {
        this.linkConfig = linkConfig;
    }
}
