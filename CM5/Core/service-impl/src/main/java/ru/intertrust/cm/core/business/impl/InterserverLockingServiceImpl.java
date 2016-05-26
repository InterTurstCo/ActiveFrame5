package ru.intertrust.cm.core.business.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.annotation.security.RunAs;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.interceptor.Interceptors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import org.springframework.transaction.annotation.Transactional;

import ru.intertrust.cm.core.business.api.InterserverLockingService;
import ru.intertrust.cm.core.dao.api.InterserverLockingDao;

@Singleton
@RunAs("system")
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@Local(InterserverLockingService.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class InterserverLockingServiceImpl implements InterserverLockingService {

    private static final long LOCK_OVERDUE_MS = 10000;
    private static final long LOCK_REFRESH_PERIOD_MS = 3000;

    private static final Logger logger = LoggerFactory.getLogger(InterserverLockingServiceImpl.class);

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    @Autowired
    private InterserverLockingDao interserverLockingDao;

    private final HashMap<String, ScheduledFuture<?>> heldLocks = new HashMap<>();

    @Override
    public synchronized boolean lock(final String resourceId) {
        try {
            if (!transactionalLock(resourceId)) {
                return false;
            }
        } catch (DuplicateKeyException ex) {
            return false;
        }
        ScheduledFuture<?> future = getExecutorService().scheduleWithFixedDelay(new Runnable() {
            @Override
            @Transactional
            public void run() {
                try {
                    getInterserverLockingDao().updateLock(resourceId, new Date());
                } catch (Exception ex) {
                    heldLocks.remove(resourceId);
                    logger.error("Error while updating lock on resource " + resourceId + ". Lock released.", ex);
                }
            }
        }, getLockRefreshPeriod(), getLockRefreshPeriod(), TimeUnit.MILLISECONDS);
        heldLocks.put(resourceId, future);
        return true;
    }

    @Transactional
    private boolean transactionalLock(final String resourceId) {
        Date overdue = new Date(System.currentTimeMillis() - getLockMaxOverdue());
        Date lockTime = getInterserverLockingDao().getLastLockTime(resourceId);
        if (lockTime == null) {

        } else if (lockTime.before(overdue)) {
            if (!getInterserverLockingDao().unlock(resourceId, lockTime)) {
                return false;
            }
        } else {
            return false;
        }
        if (!getInterserverLockingDao().lock(resourceId, new Date())) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isLocked(String resourceId) {
        InterserverLockingDao dao = getInterserverLockingDao();
        Date overdue = new Date(System.currentTimeMillis() - getLockMaxOverdue());
        Date time = dao.getLastLockTime(resourceId);
        return time != null && time.after(overdue);
    }

    protected InterserverLockingDao getInterserverLockingDao() {
        return interserverLockingDao;
    }

    @Override
    public synchronized void unlock(String resourceId) {
        ScheduledFuture<?> future = heldLocks.get(resourceId);
        if (future != null) {
            future.cancel(true);
            getInterserverLockingDao().unlock(resourceId);
            heldLocks.remove(resourceId);
        } else {
            throw new RuntimeException("Only locker can unlock resource.");
        }
    }

    protected long getLockMaxOverdue() {
        return LOCK_OVERDUE_MS;
    }

    protected long getLockRefreshPeriod() {
        return LOCK_REFRESH_PERIOD_MS;
    }

    protected ScheduledExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public void waitUntilNotLocked(final String resourceId) {
        final Semaphore semaphore = new Semaphore(0);
        final ScheduledFuture<?> future = getExecutorService().scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                if (!isLocked(resourceId)) {
                    semaphore.release();
                }
            }
        }, getLockRefreshPeriod(), getLockRefreshPeriod(), TimeUnit.MILLISECONDS);
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        future.cancel(true);
    }

}
