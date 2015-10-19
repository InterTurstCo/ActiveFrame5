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
        GlobalCacheSettings settings = (GlobalCacheSettings) context.getBean("globalCacheSettings");
        if (!settings.isEnabled()) {
            return null;
        }

        DomainObjectCacheService transactionLevelCache = (DomainObjectCacheService) context.getBean("domainObjectCacheService");
        if (!transactionLevelCache.isCacheEnabled()) {
            return null;
        }

        boolean perPerson = true;
        String beanName = perPerson ? "perPersonGlobalCacheClient" : "perGroupGlobalCacheClient";
        localJvmCacheClient = (LocalJvmCacheClient) context.getBean(beanName);
        localJvmCacheClient.setDebugEnabled(settings.isDebugEnabled());
        localJvmCacheClient.init();
        return localJvmCacheClient;
    }
}
