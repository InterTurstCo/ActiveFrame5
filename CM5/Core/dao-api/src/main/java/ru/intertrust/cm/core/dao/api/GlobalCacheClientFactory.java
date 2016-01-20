package ru.intertrust.cm.core.dao.api;

/**
 * @author Denis Mitavskiy
 *         Date: 06.07.2015
 *         Time: 19:44
 */
public interface GlobalCacheClientFactory {
    GlobalCacheClient getGlobalCacheClient();

    boolean isCacheAvailable();
}
