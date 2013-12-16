package ru.intertrust.cm.core.config.search;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

public class IndexedFieldConfig implements Serializable {

    @Attribute(required = true)
    private String name;

    @Element(required = false)
    private String doel;

    public String getName() {
        return name;
    }

    public String getDoel() {
        return doel;
    }

    @Override
    public int hashCode() {
        int hash = name.hashCode();
        if (doel != null) {
            hash = hash * 31 ^ doel.hashCode();
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        IndexedFieldConfig other = (IndexedFieldConfig) obj;
        return name.equals(other.name)
            && (doel == null ? other.doel == null : doel.equals(other.doel));
    }
}
