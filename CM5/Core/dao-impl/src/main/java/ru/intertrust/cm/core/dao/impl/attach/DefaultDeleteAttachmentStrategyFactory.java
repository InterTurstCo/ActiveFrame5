package ru.intertrust.cm.core.dao.impl.attach;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.intertrust.cm.core.config.DeleteFileConfig;

@Service
public class DefaultDeleteAttachmentStrategyFactory implements DeleteAttachmentStrategyFactory {

    private static final Logger logger = LoggerFactory.getLogger(DefaultDeleteAttachmentStrategyFactory.class);

    private final Map<DeleteFileConfig.Mode, DeleteStrategyCreator> CREATORS = new HashMap<>();
    private final FileSystemAttachmentStorageHelper helper;

    @Autowired
    public DefaultDeleteAttachmentStrategyFactory(FileSystemAttachmentStorageHelper helper) {
        this.helper = helper;
    }

    @Override
    public FileDeleteStrategy createDeleteStrategy(String name, DeleteFileConfig config) {
        // Кэшировать не имеет смысла, т.к. есть кэш выше
        config = updateConfigFromProps(name, config);

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
        }

        logger.trace("Strategy {} successfully found", strategy);
        return strategy;
    }

    private DeleteFileConfig updateConfigFromProps(String name, DeleteFileConfig config) {
        final String mode = helper.getProperty("mode", name);
        if (config == null && mode == null) {
            return null;
        } else if (mode != null && config == null) {
            logger.trace("Config wasn't set. Create new config. Name {}, mode {}", name, mode);
            config = getDeleteFileConfig(mode);
        } else if (mode != null) {
            if (config.getMode() != DeleteFileConfig.Mode.valueOf(mode.toUpperCase())) {
                logger.trace("The mode from settings will override the mode from DeleteFileConfig. Old mode {}, new one {}", config.getMode(), mode);
                config = getDeleteFileConfig(mode);
            }
        }
        // Случай когда mode == null и conf != null - просто обновляем конфиг

        updateConfigProps(name, config);
        return config;
    }

    private void updateConfigProps(String name, DeleteFileConfig config) {
        final DeleteFileConfig.Mode mode = config.getMode();
        mode.getProperties()
                .forEach(m -> {
                    final String property = helper.getProperty(m.getName(), name);
                    Object prop;
                    if (property == null) {
                        prop = m.getDefault();
                    } else {
                        prop = m.valueFromString(property);
                    }

                    if (prop != null) {
                        config.setProperty(m.getName(), prop);
                    }
                });
    }

    private DeleteFileConfig getDeleteFileConfig(String modeStr) {
        final DeleteFileConfig deleteFileConfig = new DeleteFileConfig();
        final DeleteFileConfig.Mode mode = DeleteFileConfig.Mode.valueOf(modeStr.toUpperCase());
        deleteFileConfig.setMode(mode);
        return deleteFileConfig;
    }

    @Autowired
    public void init(List<DeleteStrategyCreator> creatorList) {
        creatorList.forEach(it -> CREATORS.putIfAbsent(it.getType(), it));
    }

}
