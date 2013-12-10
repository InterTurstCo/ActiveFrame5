package ru.intertrust.cm.core.config.search;

import org.simpleframework.xml.Attribute;

import ru.intertrust.cm.core.config.DoelAware;

public class ParentLinkConfig extends DoelAware {

    @Attribute(required = true)
    private String type;

    public String getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 31 ^ type.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) && type.equals(((ParentLinkConfig) obj).type);
    }
}
