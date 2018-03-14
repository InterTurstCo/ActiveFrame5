package ru.intertrust.cm.core.dao.impl.attach;

import ru.intertrust.cm.core.config.DeleteFileConfig;

public interface FileDeleteStrategy {

    void setConfiguration(DeleteFileConfig config);

    void deleteFile(String path);
}
