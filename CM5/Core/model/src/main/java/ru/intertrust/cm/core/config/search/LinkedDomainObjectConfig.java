package ru.intertrust.cm.core.config.search;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

public class LinkedDomainObjectConfig extends IndexedDomainObjectConfig {

    public static final String REINDEX_NEVER = "never";
    public static final String REINDEX_ON_CREATE = "create";
    public static final String REINDEX_ON_CHANGE = "change";

    @Attribute(name = "reindex-on-parent", required = false)
    private String reindexOnParent;

    @Attribute(name= "nested", required = false)
    private boolean nested;

    @Element(name = "parent-link", required = true)
    private ParentLinkConfig parentLink;

    public String getReindexOnParent() {
        return reindexOnParent;
    }

    public ParentLinkConfig getParentLink() {
        return parentLink;
    }

    public boolean isNested() {
        return nested;
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
