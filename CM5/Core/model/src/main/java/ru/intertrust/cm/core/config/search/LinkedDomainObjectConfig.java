package ru.intertrust.cm.core.config.search;

import org.simpleframework.xml.Element;

public class LinkedDomainObjectConfig extends IndexedDomainObjectConfig {

    @Element(name = "parent-link", required = true)
    private ParentLinkConfig parentLink;

    public ParentLinkConfig getParentLink() {
        return parentLink;
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 31 ^ parentLink.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) && parentLink.equals(((LinkedDomainObjectConfig) obj).parentLink);
    }
}
