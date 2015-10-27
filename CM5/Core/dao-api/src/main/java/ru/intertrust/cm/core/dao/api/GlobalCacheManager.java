package ru.intertrust.cm.core.dao.api;

/**
 * @author Denis Mitavskiy
 *         Date: 22.10.2015
 *         Time: 11:39
 */
public interface GlobalCacheManager {
    boolean setEnabled(boolean enabled);

    boolean isEnabled();

    void setDebugEnabled(boolean enabled);

    boolean isDebugEnabled();

    boolean setExtendedStatisticsEnabled(boolean enabled);

    boolean isExtendedStatisticsEnabled();

    boolean isCacheAvailable();
}
