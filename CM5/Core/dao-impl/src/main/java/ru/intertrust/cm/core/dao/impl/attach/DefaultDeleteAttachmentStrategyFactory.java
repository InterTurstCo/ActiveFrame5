package ru.intertrust.cm.core.dao.impl.attach;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.intertrust.cm.core.config.DeleteFileConfig;

@Service
public class DefaultDeleteAttachmentStrategyFactory implements DeleteAttachmentStrategyFactory {

    private final Map<DeleteFileConfig.Mode, DeleteStrategyCreator> CREATORS = new HashMap<>();

    @Override
    public FileDeleteStrategy createDeleteStrategy(String name, DeleteFileConfig config) {
        FileDeleteStrategy strategy;
        if (config == null) {
            strategy = CREATORS.get(DeleteFileConfig.Mode.NEVER).create();
        } else {
            final DeleteStrategyCreator strategyCreator = CREATORS.get(config.getMode());
            if (strategyCreator == null) {
                throw new IllegalArgumentException("Unknown delete file strategy: " + config.getMode().name());
            }
            strategy = strategyCreator.create();
        }

        if (strategy instanceof StatefullFileDeleteStrategy) {
            final StatefullFileDeleteStrategy str = (StatefullFileDeleteStrategy) strategy;
            str.setConfiguration(config);
            str.setName(name);
            str.init();
        }
        return strategy;
    }

    @Autowired
    public void init(List<DeleteStrategyCreator> creatorList) {
        creatorList.forEach(it -> CREATORS.putIfAbsent(it.getType(), it));
    }

}
