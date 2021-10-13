package ru.intertrust.cm.core.dao.impl.attach;

import ru.intertrust.cm.core.config.DeleteFileConfig;

public interface StatefullFileDeleteStrategy extends FileDeleteStrategy {

    void setConfiguration(DeleteFileConfig config);

    void setName(String name);

    void init();

}
