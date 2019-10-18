package ru.intertrust.cm.globalcacheclient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.dao.api.DomainObjectCacheService;
import ru.intertrust.cm.core.dao.api.GlobalCacheClient;
import ru.intertrust.cm.core.dao.api.GlobalCacheClientFactory;

/**
 * @author Denis Mitavskiy
 *         Date: 06.07.2015
 *         Time: 19:42
 */
public class GlobalCacheClientFactoryImpl implements GlobalCacheClientFactory {
    @Autowired
    private ApplicationContext context;

    private LocalJvmCacheClient localJvmCacheClient;

    @Override
    public GlobalCacheClient getGlobalCacheClient() {
        if (localJvmCacheClient != null) {
            return localJvmCacheClient;
        }
        DomainObjectCacheService transactionLevelCache = (DomainObjectCacheService) context.getBean("domainObjectCacheService");
        if (!transactionLevelCache.isCacheEnabled()) {
            return null;
        }

        String beanName = "perPersonGlobalCacheClient";
        localJvmCacheClient = (LocalJvmCacheClient) context.getBean(beanName);
        return localJvmCacheClient;
    }

    public boolean isCacheAvailable() {
        DomainObjectCacheService transactionLevelCache = (DomainObjectCacheService) context.getBean("domainObjectCacheService");
        return transactionLevelCache.isCacheEnabled();
    }
}
