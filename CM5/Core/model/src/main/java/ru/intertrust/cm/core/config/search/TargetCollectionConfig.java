package ru.intertrust.cm.core.config.search;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;

public class TargetCollectionConfig implements Serializable {

    @Attribute(required = true)
    private String name;

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        return name.equals(((TargetCollectionConfig) obj).name);
    }
}
