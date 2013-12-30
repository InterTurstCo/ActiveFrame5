package ru.intertrust.cm.core.config.search;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;

public class IndexedContentConfig implements Serializable {

    @Attribute(required = true)
    private String type;

    public String getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        return type.equals(((IndexedContentConfig) obj).type);
    }
}
