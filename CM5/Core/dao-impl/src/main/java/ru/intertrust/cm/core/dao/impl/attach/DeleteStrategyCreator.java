package ru.intertrust.cm.core.dao.impl.attach;

import ru.intertrust.cm.core.config.DeleteFileConfig;

public interface DeleteStrategyCreator {
    FileDeleteStrategy create();
    DeleteFileConfig.Mode getType();
}
