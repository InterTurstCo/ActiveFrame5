package ru.intertrust.cm.core.config.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;

public class IndexedDomainObjectConfig implements Serializable {

    @Attribute(required = true)
    private String type;

    @ElementList(entry = "indexed-field", inline = true)
    private List<IndexedFieldConfig> fields = new ArrayList<>();

    @ElementList(entry = "indexed-content", inline = true)
    private List<IndexedContentConfig> contentObjects = new ArrayList<>();

    @ElementList(entry = "linked-domain-object", inline = true)
    private List<LinkedDomainObjectConfig> linkedObjects = new ArrayList<>();

    public String getType() {
        return type;
    }

    public List<IndexedFieldConfig> getFields() {
        return fields;
    }

    public List<IndexedContentConfig> getContentObjects() {
        return contentObjects;
    }

    public List<LinkedDomainObjectConfig> getLinkedObjects() {
        return linkedObjects;
    }

    @Override
    public int hashCode() {
        int hash = type.hashCode();
        hash = hash * 31 + fields.hashCode();
        hash = hash * 31 + contentObjects.hashCode();
        hash = hash * 31 + linkedObjects.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        IndexedDomainObjectConfig other = (IndexedDomainObjectConfig) obj;
        return type.equals(other.type)
            && fields.equals(other.fields)
            && contentObjects.equals(other.contentObjects)
            && linkedObjects.equals(other.linkedObjects);
    }
}
