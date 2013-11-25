package ru.intertrust.cm.core.config;

import ru.intertrust.cm.core.config.converter.TopLevelConfigurationCache;

/**
 * Служит для инициализации {@link ru.intertrust.cm.core.config.converter.TopLevelConfigurationCache} с помощью спринг
 * @author vmatsukevich
 *         Date: 7/15/13
 *         Time: 1:22 PM
 */
public class TopLevelConfigurationCacheInitializer {

    /**
     * Создает {@link TopLevelConfigurationCacheInitializer}
     */
    public TopLevelConfigurationCacheInitializer() {
    }

    /**
     * Инициализирует {@link TopLevelConfigurationCacheInitializer}
     */
    public void init() {
        TopLevelConfigurationCache.getInstance().build();
    }
}
