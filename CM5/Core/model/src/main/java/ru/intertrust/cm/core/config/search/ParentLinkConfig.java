package ru.intertrust.cm.core.config.search;

import org.simpleframework.xml.Attribute;

import ru.intertrust.cm.core.config.DoelAware;

import java.util.Objects;

public class ParentLinkConfig extends DoelAware {

    @Attribute(required = false)
    private String type;

    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ParentLinkConfig that = (ParentLinkConfig) o;
        return Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), type);
    }
}
