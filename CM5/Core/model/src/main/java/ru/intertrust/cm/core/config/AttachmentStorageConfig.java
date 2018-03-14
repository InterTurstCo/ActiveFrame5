package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementUnion;
import org.simpleframework.xml.Root;

import ru.intertrust.cm.core.config.base.TopLevelConfig;

@Root(name = "attachment-storage")
public class AttachmentStorageConfig implements TopLevelConfig {

    @Attribute(name = "name", required = true)
    private String name;

    @ElementUnion({
        @Element(name = "folder", type = FolderStorageConfig.class)
    })
    private AttachmentStorageTypeConfig storageConfig;

    @Override
    public String getName() {
        return name;
    }

    public AttachmentStorageTypeConfig getStorageConfig() {
        return storageConfig;
    }

    @Override
    public ExtensionPolicy getReplacementPolicy() {
        return ExtensionPolicy.None;
    }

    @Override
    public ExtensionPolicy getCreationPolicy() {
        return ExtensionPolicy.None;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }
        AttachmentStorageConfig that = (AttachmentStorageConfig) obj;
        return this.name.equals(that.name) && this.storageConfig.equals(that.storageConfig);
    }

    @Override
    public int hashCode() {
        return name.hashCode() * 31 + storageConfig.hashCode();
    }

}
