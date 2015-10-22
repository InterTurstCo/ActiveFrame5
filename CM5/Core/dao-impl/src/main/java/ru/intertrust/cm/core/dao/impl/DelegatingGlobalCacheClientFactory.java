package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.dao.api.GlobalCacheClient;
import ru.intertrust.cm.core.dao.api.GlobalCacheClientFactory;
import ru.intertrust.cm.core.dao.api.GlobalCacheManager;

import java.io.Serializable;
import java.util.HashMap;

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

    @Autowired
    private ApplicationContext context;

    private SwitchableCacheClient cacheClient;

    @Override
    public GlobalCacheClient getGlobalCacheClient() {
        if (cacheClient != null) {
            return cacheClient;
        }

        GlobalCacheClient impl = getImpl();
        cacheClient = new SwitchableCacheClient(impl);
        cacheClient.activate();
        return cacheClient;
    }

    protected GlobalCacheClient getImpl() {
        GlobalCacheClient impl;
        if (!isEnabled() || !context.containsBean("globalCacheClientFactory")) {
            return DisabledGlobalCacheClient.INSTANCE;
        }
        impl = ((GlobalCacheClientFactory) context.getBean("globalCacheClientFactory")).getGlobalCacheClient();
        if (impl == null) {
            return DisabledGlobalCacheClient.INSTANCE;
        }
        return impl;
    }

    void applySettings(HashMap<String, Serializable> cacheSettings) {
        cacheClient.applySettings(cacheSettings);
    }

    void setEnabled(boolean enabled) {
        final boolean currentlyEnabled = isEnabled();
        if (currentlyEnabled == enabled) {
            return;
        }
        final GlobalCacheClient currentImpl = cacheClient.getGlobalCacheClientImpl();
        cacheClient.setGlobalCacheClientImpl(getImpl());
        if (!enabled) {
            currentImpl.deactivate();
        } else {
            currentImpl.activate();
        }
    }

    private boolean isEnabled() {
        return enabled == Boolean.TRUE;
    }

    void setDebugEnabled(boolean enabled) {
        debugEnabled = enabled;
    }

    public boolean isDebugEnabled() {
        return debugEnabled == Boolean.TRUE;
    }
}
