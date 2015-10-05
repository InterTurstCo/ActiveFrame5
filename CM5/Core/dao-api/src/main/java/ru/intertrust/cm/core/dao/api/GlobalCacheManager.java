package ru.intertrust.cm.core.dao.api;

/**
 * @author Denis Mitavskiy
 *         Date: 10.08.2015
 *         Time: 20:15
 */
public interface GlobalCacheManager {
    void setCacheEnabled(boolean enabled);

    boolean isCacheEnabled();
}
