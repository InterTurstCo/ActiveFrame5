package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.dao.api.GlobalCacheClient;
import ru.intertrust.cm.core.dao.api.GlobalCacheClientFactory;

/**
 * @author Denis Mitavskiy
 *         Date: 06.07.2015
 *         Time: 19:47
 */
public class DelegatingGlobalCacheClientFactory implements GlobalCacheClientFactory {
    @Autowired
    private ApplicationContext context;

    private GlobalCacheClient cacheClient;

    @Override
    public GlobalCacheClient getGlobalCacheClient() {
        if (cacheClient != null) {
            return cacheClient;
        }

        if (context.containsBean("globalCacheClientFactory")) {
            cacheClient = ((GlobalCacheClientFactory) context.getBean("globalCacheClientFactory")).getGlobalCacheClient();
            if (cacheClient == null) {
                cacheClient = DisabledGlobalCacheClient.INSTANCE;
            }
        } else {
            cacheClient = DisabledGlobalCacheClient.INSTANCE;
        }
        return cacheClient;
    }

    void setGlobalCacheClient(GlobalCacheClient client) {
        cacheClient = client;
    }
}
