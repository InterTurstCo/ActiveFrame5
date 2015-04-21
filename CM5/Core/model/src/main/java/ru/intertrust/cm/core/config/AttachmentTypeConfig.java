package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * User: vlad
 */
public class AttachmentTypeConfig implements Dto{
    @Attribute(name = "name", required = true)
    private String name;

    @Attribute(name = "template", required = false)
    private String template;
    
    @Attribute(name = "path", required = false)
    private String path;

    @Attribute(name = "mimeType", required = false)
    private String mimeType;

    @Element(name = "parent", required = false)
    private ReferenceFieldConfig parentReference;

    public AttachmentTypeConfig() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public ReferenceFieldConfig getParentReference() {
        return parentReference;
    }

    public void setParentReference(ReferenceFieldConfig parentReference) {
        this.parentReference = parentReference;
    }

    public AttachmentTypeConfig clone() throws CloneNotSupportedException {
        return (AttachmentTypeConfig) super.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AttachmentTypeConfig that = (AttachmentTypeConfig) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }

        if (path != null ? !path.equals(that.path) : that.path != null) {
            return false;
        }

        if (mimeType != null ? !mimeType.equals(that.mimeType) : that.mimeType != null) {
            return false;
        }

        if (parentReference != null ? !parentReference.equals(that.parentReference) : that.parentReference != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
