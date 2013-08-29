package ru.intertrust.cm.core.config.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Author: Denis Mitavskiy
 * Date: 14.06.13
 * Time: 16:01
 */
@Root(name="navigation")
public class NavigationConfig implements TopLevelConfig {
    @Attribute(name = "name")
    private String name;

    @Element(name = "link", required = false)
    private LinkConfig link;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LinkConfig getLink() {
        return link;
    }

    public void setLink(LinkConfig link) {
        this.link = link;
    }
}
