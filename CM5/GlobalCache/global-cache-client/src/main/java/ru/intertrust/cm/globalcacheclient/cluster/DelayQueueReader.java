package ru.intertrust.cm.globalcacheclient.cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.intertrust.cm.globalcacheclient.GlobalCacheSettings;

import javax.annotation.PostConstruct;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Denis Mitavskiy
 *         Date: 22.04.2016
 *         Time: 16:38
 */
@Singleton(name = "GlobalCacheDelayQueueReader")
@Interceptors(SpringBeanAutowiringInterceptor.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class DelayQueueReader {
    final static Logger logger = LoggerFactory.getLogger(DelayQueueReader.class);

    @Autowired
    private GlobalCacheSettings settings;

    private AtomicLong nextRun;

    @PostConstruct
    private void init() {
        nextRun = new AtomicLong(System.currentTimeMillis() + settings.getClusterSynchronizationMillies());
    }

    @Schedule(hour = "*", minute = "*", second = "*/1", persistent = false)
    public void schedule() {
        if (!settings.isInCluster() || System.currentTimeMillis() < nextRun.get()) {
            return;
        }
        performSync();
        nextRun.addAndGet(settings.getClusterSynchronizationMillies());
    }

    private void performSync() {
        logger.warn("Sync caches");
    }
}
