package ru.intertrust.cm.core.dao.api;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Denis Mitavskiy
 *         Date: 22.10.2015
 *         Time: 11:39
 */
public interface GlobalCacheManager {
    void applySettings(Map<String, Serializable> cacheSettings);

    Map<String, Serializable> getSettings();

    boolean setEnabled(boolean enabled);

    boolean isEnabled();

    void setDebugEnabled(boolean enabled);

    boolean isDebugEnabled();

    boolean setExtendedStatisticsEnabled(boolean enabled);

    boolean isExtendedStatisticsEnabled();

    boolean isCacheAvailable();
}
