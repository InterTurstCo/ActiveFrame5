package ru.intertrust.cm.core.dao.impl.attach;

public interface FileSystemAttachmentStorageHelper {

    String PROP_PREFIX = "attachments.storage.";

    String getProperty(String propName, String typeName);

    String getPureProperty(String propName);

}
