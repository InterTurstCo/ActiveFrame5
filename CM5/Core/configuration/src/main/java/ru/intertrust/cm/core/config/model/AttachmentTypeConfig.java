package ru.intertrust.cm.core.config.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

/**
 * User: vlad
 */
public class AttachmentTypeConfig {
    @Attribute(name = "name", required = true)
    private String name;

    @Attribute(name = "path", required = false)
    private String path;

    @Attribute(name = "mimeType", required = false)
    private String mimeType;

    @Element(name = "parent", required = false)
    private DomainObjectParentConfig parentConfig;

    public AttachmentTypeConfig() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public DomainObjectParentConfig getParentConfig() {
        return parentConfig;
    }

    public void setParentConfig(DomainObjectParentConfig parentConfig) {
        this.parentConfig = parentConfig;
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

        if (parentConfig != null ? !parentConfig.equals(that.parentConfig) : that.parentConfig != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
