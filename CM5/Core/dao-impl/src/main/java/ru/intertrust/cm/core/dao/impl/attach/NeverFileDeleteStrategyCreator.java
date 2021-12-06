package ru.intertrust.cm.core.dao.impl.attach;

import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Service;
import ru.intertrust.cm.core.config.DeleteFileConfig;

@Service
public class NeverFileDeleteStrategyCreator implements DeleteStrategyCreator {

    @Override
    @Lookup ("neverFileDeleteStrategy")
    public NeverFileDeleteStrategy create() {
        // Method will return the prototype of the file strategy
        return null;
    }

    @Override
    public DeleteFileConfig.Mode getType() {
        return DeleteFileConfig.Mode.NEVER;
    }
}
