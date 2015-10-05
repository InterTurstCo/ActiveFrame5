package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.dao.api.GlobalCacheManager;

/**
 * @author Denis Mitavskiy
 *         Date: 10.08.2015
 *         Time: 20:23
 */
public class GlobalCacheManagerImpl implements GlobalCacheManager {
    private boolean isEnabled = true;

    @Autowired
    private ApplicationContext context;

    public synchronized void setCacheEnabled(boolean enabled) {
        DelegatingGlobalCacheClientFactory factory = (DelegatingGlobalCacheClientFactory) context.getBean("delegatingGlobalCacheClientFactory");
        if (!enabled) {
            ((DelegatingGlobalCacheClientFactory) factory).setGlobalCacheClient(DisabledGlobalCacheClient.INSTANCE);
        } else {
            ((DelegatingGlobalCacheClientFactory) factory).setGlobalCacheClient(null);
        }
        isEnabled = enabled;
    }

    public synchronized boolean isCacheEnabled() {
        return isEnabled;
    }
}
