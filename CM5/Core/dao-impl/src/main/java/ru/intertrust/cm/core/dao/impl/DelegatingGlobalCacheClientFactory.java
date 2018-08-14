package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.dto.GlobalCacheStatistics;
import ru.intertrust.cm.core.dao.api.GlobalCacheClient;
import ru.intertrust.cm.core.dao.api.GlobalCacheClientFactory;
import ru.intertrust.cm.core.dao.api.GlobalCacheManager;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Denis Mitavskiy
 *         Date: 06.07.2015
 *         Time: 19:47
 */
public class DelegatingGlobalCacheClientFactory implements GlobalCacheClientFactory, GlobalCacheManager {
    @Value("${global.cache.enabled:false}")
    private volatile Boolean enabled;

    @Value("${global.cache.debug.enabled:false}")
    private volatile Boolean debugEnabled;

    @Value("${global.cache.extended.statistics:false}")
    private volatile Boolean globalCacheExtendedStatisticsEnabled;

    @Autowired
    private ApplicationContext context;

    private SwitchableCacheClient switchableClient;

    @Override
    public GlobalCacheClient getGlobalCacheClient() {
        if (switchableClient != null) {
            return switchableClient;
        }

        GlobalCacheClient impl = getImpl();
        switchableClient = new SwitchableCacheClient(impl);
        switchableClient.activate(true);
        return switchableClient;
    }

    public boolean isCacheAvailable() {
        if (!context.containsBean("globalCacheClientFactory")) {
            return false;
        }
        return ((GlobalCacheClientFactory) context.getBean("globalCacheClientFactory")).isCacheAvailable();
    }

    @Override
    public GlobalCacheStatistics getStatistics() {
        return switchableClient.getStatistics();
    }

    @Override
    public void clearStatistics(boolean hourlyOnly) {
        switchableClient.clearStatistics(hourlyOnly);
    }

    @Override
    public void clear() {
        switchableClient.clear();
    }

    protected GlobalCacheClient getImpl() {
        GlobalCacheClient impl;
        if (!isEnabled() || !context.containsBean("globalCacheClientFactory")) {
            return (GlobalCacheClient) context.getBean("disabledGlobalCacheClient");
        }
        impl = ((GlobalCacheClientFactory) context.getBean("globalCacheClientFactory")).getGlobalCacheClient();
        if (impl == null) {
            return DisabledGlobalCacheClient.INSTANCE;
        }
        if (globalCacheExtendedStatisticsEnabled) {
            return new ExtendedStatisticsGatherer(impl);
        }
        return impl;
    }

    @Override
    public void applySettings(Map<String, Serializable> cacheSettings) {
        switchableClient.applySettings(cacheSettings);
    }

    @Override
    public Map<String, Serializable> getSettings() {
        return switchableClient.getSettings();
    }

    @Override
    public boolean setEnabled(boolean enabled) {
        final boolean currentlyEnabled = isEnabled();
        if (currentlyEnabled == enabled) {
            return currentlyEnabled;
        }
        this.enabled = enabled;
        final GlobalCacheClient newImpl = getImpl();
        if (this.enabled && newImpl == DisabledGlobalCacheClient.INSTANCE) {
            this.enabled = false;
            return this.enabled;
        }

        final GlobalCacheClient currentImpl = switchableClient.getGlobalCacheClientImpl();
        switchableClient.setGlobalCacheClientImpl(newImpl);
        currentImpl.deactivate();
        if (enabled) {
            newImpl.activate(false);
        }
        return this.enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled == Boolean.TRUE;
    }

    @Override
    public void setDebugEnabled(boolean enabled) {
        debugEnabled = enabled;
    }

    @Override
    public boolean isDebugEnabled() {
        return debugEnabled == Boolean.TRUE;
    }

    @Override
    public boolean setExtendedStatisticsEnabled(boolean enabled) {
        final boolean currentlyEnabled = isExtendedStatisticsEnabled();
        if (currentlyEnabled == enabled) {
            return enabled;
        }
        final GlobalCacheClient currentImpl = switchableClient.getGlobalCacheClientImpl();
        if (currentImpl == DisabledGlobalCacheClient.INSTANCE) {
            globalCacheExtendedStatisticsEnabled = false;
            return globalCacheExtendedStatisticsEnabled;
        }

        globalCacheExtendedStatisticsEnabled = enabled;
        switchableClient.setGlobalCacheClientImpl(getImpl());
        return globalCacheExtendedStatisticsEnabled;
    }

    @Override
    public boolean isExtendedStatisticsEnabled() {
        return globalCacheExtendedStatisticsEnabled == Boolean.TRUE;
    }
}
