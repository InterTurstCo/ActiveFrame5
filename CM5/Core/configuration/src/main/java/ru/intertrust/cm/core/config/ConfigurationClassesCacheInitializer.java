package ru.intertrust.cm.core.config;

import ru.intertrust.cm.core.config.converter.ConfigurationClassesCache;

/**
 * Служит для инициализации {@link ru.intertrust.cm.core.config.converter.ConfigurationClassesCache} с помощью спринг
 * @author vmatsukevich
 *         Date: 7/15/13
 *         Time: 1:22 PM
 */
public class ConfigurationClassesCacheInitializer {

    /**
     * Создает {@link ConfigurationClassesCacheInitializer}
     */
    public ConfigurationClassesCacheInitializer() {
    }

    /**
     * Инициализирует {@link ConfigurationClassesCacheInitializer}
     */
    public void init() {
        ConfigurationClassesCache.getInstance().build();
    }
}
