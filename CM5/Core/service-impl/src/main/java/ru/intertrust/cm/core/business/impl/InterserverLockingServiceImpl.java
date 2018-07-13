package ru.intertrust.cm.core.business.impl;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
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
import org.springframework.transaction.annotation.Propagation;
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

    private final ConcurrentHashMap<String, ScheduledFutureEx> heldLocks = new ConcurrentHashMap<>();

    @Override
    public synchronized boolean lock(final String resourceId) {
        try {
            if (!transactionalLock(resourceId)) {
                return false;
            }
        } catch (DuplicateKeyException ex) {
            return false;
        }
        ScheduledFutureEx future = new ScheduledFutureEx(getExecutorService().scheduleWithFixedDelay(new Runnable() {
            @Override
            @Transactional(propagation = Propagation.REQUIRES_NEW)
            public void run() {
                try {
                    getInterserverLockingDao().updateLock(resourceId, new Date());
                } catch (Exception ex) {
                    heldLocks.remove(resourceId);
                    logger.error("Error while updating lock on resource " + resourceId + ". Lock released.", ex);
                }
            }
        }, getLockRefreshPeriod(), getLockRefreshPeriod(), TimeUnit.MILLISECONDS));
        
        future = heldLocks.put(resourceId, future);
        if (future != null) {
            future.cancel(true);
        }
        return true;
    }

    @Override
    public synchronized boolean selfSharedLock(final String resourceId) {
        Date lockTime = null;
        LOCK_STATUS lockStatus = LOCK_STATUS.NO_LOCK;
        try {
            LockResult lockResult = transactionalSelfSharedLock(resourceId);
            lockTime = lockResult.getLockTime();
            lockStatus = lockResult.getLockStatus();
            if (lockStatus != LOCK_STATUS.OWN_LOCK && lockStatus != LOCK_STATUS.NEW_LOCK) {
                return false;
            }
        } catch (DuplicateKeyException ex) {
            return false;
        }
        if (lockStatus == LOCK_STATUS.OWN_LOCK) {
            return true;
        }
        ScheduledFutureEx future = new ScheduledFutureEx(getExecutorService().scheduleWithFixedDelay(new Runnable() {
            @Override
            @Transactional(propagation = Propagation.REQUIRES_NEW)
            public void run() {
                try {
                    Date newDate = new Date();
                    ScheduledFutureEx future = heldLocks.get(resourceId);
                    Date oldDate = future != null ? future.getLockTime() : null;
                    getInterserverLockingDao().updateLock(resourceId, oldDate, newDate);
                    if (future != null) {
                        future.setLockTime(newDate);
                    }
                } catch (Exception ex) {
                    heldLocks.remove(resourceId);
                    logger.error("Error while updating lock on resource " + resourceId + ". Lock released.", ex);
                }
            }
        }, getLockRefreshPeriod(), getLockRefreshPeriod(), TimeUnit.MILLISECONDS), lockTime);
        future = heldLocks.put(resourceId, future);
        if (future != null) {
            future.cancel(true);
        }
        return true;
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
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
    
    /**
     * Попытка заблокировать ресурс, с учетом того, какой объект пытается
     * наложить блокировку.
     * @param resourceId
     *            идентификатор (имя) ресурса
     * @return true, если удалось заблокировать, или если ресурс уже
     *         заблокирован объектом, накладывающим блокировку false в противном
     *         случае
     * @return результат попытки блокировки (статус + отсечка времени
     *         блокировки)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private LockResult transactionalSelfSharedLock(final String resourceId) {
        Date overdue = new Date(System.currentTimeMillis() - getLockMaxOverdue());
        ScheduledFutureEx future = heldLocks.get(resourceId);
        Date oldLockTime = future != null ? future.getLockTime() : null;
        Date lockTime = getInterserverLockingDao().getLastLockTime(resourceId);

        if (lockTime == null) {
            // do nothing
        } else if (lockTime.before(overdue)) {
            if (!getInterserverLockingDao().unlock(resourceId, lockTime)) {
                return new LockResult(LOCK_STATUS.OTHER_LOCK, lockTime);
            }
        } else {
            return lockTime.getTime() == (oldLockTime != null ? oldLockTime.getTime() : 0) ?
                    new LockResult(LOCK_STATUS.OWN_LOCK, lockTime) : 
                    new LockResult(LOCK_STATUS.OTHER_LOCK, lockTime);
        }
        lockTime = new Date();
        if (!getInterserverLockingDao().lock(resourceId, lockTime)) {
            return new LockResult(LOCK_STATUS.OTHER_LOCK, lockTime);
        }
        return new LockResult(LOCK_STATUS.NEW_LOCK, lockTime);
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
        ScheduledFutureEx future = heldLocks.get(resourceId);
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

    private class ScheduledFutureEx {
        private final ScheduledFuture<?> scheduledFuture;
        private Date lockTime;
        
        private ScheduledFutureEx(ScheduledFuture<?> scheduledFuture) {
            this(scheduledFuture, null);
        }

        private ScheduledFutureEx(ScheduledFuture<?> scheduledFuture, Date lockTime) {
            this.scheduledFuture = scheduledFuture;
            this.lockTime = lockTime;
        }
        
        private Date getLockTime() {
            return lockTime;
        }
        
        private void setLockTime(Date lockTime) {
            this.lockTime = lockTime;
        }
        
        private boolean cancel(boolean mayInterruptIfRunning) {
            return scheduledFuture.cancel(mayInterruptIfRunning);
        }
    }
    
    /**
     * Статус блокировки.
     * @author mike
     *
     */
    private enum LOCK_STATUS {
        /* Блокировка наложена */
        NEW_LOCK,
        /* Блокировка уже наложена объектом, запрашивающим блокировку */
        OWN_LOCK,
        /* Блокировка наложена другим объектом */
        OTHER_LOCK,
        /* Нет блокировки */
        NO_LOCK
    }
    
    /**
     * Результат попытки наложения блокировки.
     * @author mike
     *
     */
    private class LockResult {
        private final LOCK_STATUS lockStatus;
        private final Date lockTime;
        
        private LockResult(LOCK_STATUS lockStatus, Date lockTime) {
            this.lockStatus = lockStatus;
            this.lockTime = lockTime;
        }
        
        private Date getLockTime() {
            return lockTime;
        }
        
        private LOCK_STATUS getLockStatus() {
            return lockStatus;
        }
        
    }
}
