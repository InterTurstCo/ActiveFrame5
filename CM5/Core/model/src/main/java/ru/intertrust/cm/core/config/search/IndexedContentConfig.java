package ru.intertrust.cm.core.config.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;

public class IndexedContentConfig implements Serializable {

    @Attribute(required = true)
    private String type;

    @Attribute(name = "parent-fk-field", required = false)
    private String parentFkField;

    @ElementList(entry = "content-field", inline = true, required = false)
    private List<ContentFieldConfig> fields = new ArrayList<>();

    public String getType() {
        return type;
    }

    public String getParentFkField() {
        return parentFkField;
    }

    @Override
    public int hashCode() {
        int hash = type.hashCode();
        hash = hash * 31 ^ (parentFkField != null ? parentFkField.hashCode() : 0);
        hash = hash * 31 ^ (fields!= null ? fields.hashCode() : 0);
        return hash;
    }

    public List<ContentFieldConfig> getFields() {
        return fields;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        IndexedContentConfig other = (IndexedContentConfig) obj;

        return type.equals(other.type)
                && (getFields() == null ? other.fields == null : getFields().equals(other.fields))
                && (parentFkField == null ? other.parentFkField == null : parentFkField.equals(other.parentFkField));
    }
}
