package ru.intertrust.cm.core.config;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "folder")
public class FolderStorageConfig implements Serializable, AttachmentStorageTypeConfig {

    @Attribute(name = "configurable", required = false)
    private Boolean configurable;

    @Element(name = "subfolder-mask", required = false)
    private String subfolderMask;

    @Element(name = "delete-file", required = false)
    private DeleteFileConfig deleteFileConfig;

    public Boolean getConfigurable() {
        return configurable;
    }

    public void setConfigurable(Boolean configurable) {
        this.configurable = configurable;
    }

    public String getSubfolderMask() {
        return subfolderMask;
    }

    public void setSubfolderMask(String subfolderMask) {
        this.subfolderMask = subfolderMask;
    }

    public DeleteFileConfig getDeleteFileConfig() {
        return deleteFileConfig;
    }

    public void setDeleteFileConfig(DeleteFileConfig deleteFileConfig) {
        this.deleteFileConfig = deleteFileConfig;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }
        FolderStorageConfig that = (FolderStorageConfig) obj;
        return this.configurable == that.configurable
                && (this.subfolderMask == null ? that.subfolderMask == null : this.subfolderMask.equals(that.subfolderMask))
                && (this.deleteFileConfig == null ? that.deleteFileConfig == null : this.deleteFileConfig.equals(that.deleteFileConfig));
    }

    @Override
    public int hashCode() {
        int hash = 0;
        if (configurable != null) {
            hash += configurable.hashCode();
        }
        hash *= 31;
        if (subfolderMask != null) {
            hash += subfolderMask.hashCode();
        }
        hash *= 31;
        if (deleteFileConfig != null) {
            hash += deleteFileConfig.hashCode();
        }
        return hash;
    }
}
