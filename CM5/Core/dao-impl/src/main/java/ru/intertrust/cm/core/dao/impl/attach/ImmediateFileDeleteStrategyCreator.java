package ru.intertrust.cm.core.dao.impl.attach;

import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Service;
import ru.intertrust.cm.core.config.DeleteFileConfig;

@Service
public class ImmediateFileDeleteStrategyCreator implements DeleteStrategyCreator {

    @Override
    @Lookup ("immediateFileDeleteStrategy")
    public ImmediateFileDeleteStrategy create() {
        // Method will return the bean of the file strategy
        return null;
    }

    @Override
    public DeleteFileConfig.Mode getType() {
        return DeleteFileConfig.Mode.IMMED;
    }
}
