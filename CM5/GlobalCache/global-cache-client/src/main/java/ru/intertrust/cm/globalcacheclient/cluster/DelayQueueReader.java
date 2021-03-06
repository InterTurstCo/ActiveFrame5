package ru.intertrust.cm.globalcacheclient.cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.dto.CacheInvalidation;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.GlobalCacheClient;
import ru.intertrust.cm.globalcacheclient.GlobalCacheSettings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Denis Mitavskiy
 *         Date: 22.04.2016
 *         Time: 16:38
 */
//@Singleton(name = "GlobalCacheDelayQueueReader") TODO: Bean is temporarily disabled
//@Interceptors(SpringBeanAutowiringInterceptor.class)
//@TransactionManagement(TransactionManagementType.BEAN)
public class DelayQueueReader {
    final static Logger logger = LoggerFactory.getLogger(DelayQueueReader.class);
    public static final int MESSAGES_PROCESS_BATCH_SIZE = 100;
    public static final int MAX_ITERATIONS = 10000;
    public static final int ID_INVALIDATION_BATCH_SIZE = 1000000;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private GlobalCacheSettings settings;

    private AtomicLong nextRun;

    private volatile boolean processing;

    //@PostConstruct
    private void init() {
        nextRun = new AtomicLong(System.currentTimeMillis() + settings.getClusterSynchronizationMillies());
    }

    //@Schedule(hour = "*", minute = "*", second = "*/1", persistent = false)
   /* public void schedule() {
        if (!settings.isInCluster() || processing || System.currentTimeMillis() < nextRun.get()) {
            return;
        }
        performSync();
        nextRun.addAndGet(settings.getClusterSynchronizationMillies());
    }*/

    /*private synchronized void performSync() {
        processing = true;
        GlobalCacheClient cacheClient = (GlobalCacheClient) context.getBean("globalCacheClient");
        try {
            final long start = System.currentTimeMillis();
            HashSet<Id> createdIdsToInvalidate = new HashSet<>();
            HashSet<Id> toInvalidate = new HashSet<>();
            HashSet<Id> usersToInvalideAccess = new HashSet<>();
            boolean clearFullAccessLog = false;
            boolean clearCache = false;

            outer:
            for (int i = 0; i < MAX_ITERATIONS; ++i) {
                final List<CacheInvalidation> messages = GlobalCacheJmsHelper.readFromDelayQueue(MESSAGES_PROCESS_BATCH_SIZE);

                if (messages.isEmpty()) {
                    break;
                }
                if (clearCache) { // ignore everything in this case, just consume as much messages as possible
                    continue;
                }
                boolean messageReceivedAfterProcessStarted = false;
                for (CacheInvalidation message : messages) {
                    if (message.isClearCache()) {
                        clearCache = true;
                        continue outer;
                    }
                    if (message.getReceiveTime() > start) {
                        messageReceivedAfterProcessStarted = true;
                    }
                    if (message.isClearFullAccessLog()) {
                        clearFullAccessLog = true;
                    }
                    createdIdsToInvalidate.addAll(message.getCreatedIdsToInvalidate());
                    toInvalidate.addAll(message.getIdsToInvalidate());
                    usersToInvalideAccess.addAll(message.getUsersAccessToInvalidate());
                }
                if (toInvalidate.size() + createdIdsToInvalidate.size() > ID_INVALIDATION_BATCH_SIZE) {
                    invalidateCacheEntries(cacheClient, createdIdsToInvalidate, toInvalidate, clearFullAccessLog, usersToInvalideAccess);
                    createdIdsToInvalidate.clear();
                    toInvalidate.clear();
                    usersToInvalideAccess.clear();
                    clearFullAccessLog = false;
                }

                if (messages.size() < MESSAGES_PROCESS_BATCH_SIZE || messageReceivedAfterProcessStarted) {
                    // we recieved the last message or a message which appeared after the processing start
                    break;
                }
            }
            if (clearCache) {
                cacheClient.clearCurrentNode();
            } else {
                invalidateCacheEntries(cacheClient, createdIdsToInvalidate, toInvalidate, clearFullAccessLog, usersToInvalideAccess);
            }
        } catch(Throwable t) {
            logger.error("Exception while processing delay queue", t);
            throw t;
        } finally {
            processing = false;
        }
    }*/

    private void invalidateCacheEntries(GlobalCacheClient cacheClient, HashSet<Id> createdIdsToInvalidate, HashSet<Id> toInvalidate, boolean clearFullAccessLog, HashSet<Id> usersToInvalideAccess) {
        if (createdIdsToInvalidate.isEmpty() && toInvalidate.isEmpty() && !clearFullAccessLog && usersToInvalideAccess.isEmpty()) {
            return;
        }
        DomainObjectDao dao = (DomainObjectDao) context.getBean("domainObjectDao");
        AccessToken accessToken = ((AccessControlService) context.getBean("accessControlService")).createSystemAccessToken(this.getClass().getName());
        final List<DomainObject> domainObjects = dao.find(new ArrayList<>(createdIdsToInvalidate), accessToken);
        cacheClient.invalidateCurrentNode(new CacheInvalidation(domainObjects, toInvalidate, clearFullAccessLog, usersToInvalideAccess));
    }
}
