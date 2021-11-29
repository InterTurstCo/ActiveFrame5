package ru.intertrust.cm.core.dao.impl.attach;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class FileSystemAttachmentStorageHelperImpl implements FileSystemAttachmentStorageHelper {

    private final Environment env;

    @Autowired
    public FileSystemAttachmentStorageHelperImpl(Environment env) {
        this.env = env;
    }

    @Override
    public String getProperty(String propName, String typeName) {
        String value = env.getProperty(PROP_PREFIX + typeName + "." + propName);
        return value != null ? value : env.getProperty(PROP_PREFIX + propName);
    }

    @Override
    public String getPureProperty(String propName) {
        return env.getProperty(propName);
    }
}
