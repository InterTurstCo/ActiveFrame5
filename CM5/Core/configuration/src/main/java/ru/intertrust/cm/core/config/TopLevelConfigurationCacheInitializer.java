package ru.intertrust.cm.core.config;

/**
 * @author vmatsukevich
 *         Date: 7/15/13
 *         Time: 1:22 PM
 */
public class TopLevelConfigurationCacheInitializer {

    public TopLevelConfigurationCacheInitializer() {
    }

    public void init() {
        TopLevelConfigurationCache.getInstance().build();
    }
}
