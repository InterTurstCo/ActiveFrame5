package ru.intertrust.cm.core.dao.impl.attach;

import ru.intertrust.cm.core.config.DeleteFileConfig;

public interface DeleteAttachmentStrategyFactory {
    FileDeleteStrategy createDeleteStrategy(String name, DeleteFileConfig config);
}
