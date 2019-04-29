package ru.intertrust.cm.core.business.impl;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.annotation.security.RunAs;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Local;
import javax.ejb.SessionContext;
import javax.ejb.Singleton;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.transaction.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.intertrust.cm.core.business.api.InterserverLockingService;
import ru.intertrust.cm.core.dao.api.InterserverLockingDao;
import ru.intertrust.cm.core.model.FatalException;

@Singleton
@RunAs("system")
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@Local(InterserverLockingService.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class InterserverLockingServiceImpl implements InterserverLockingService {

    @Resource
    private SessionContext sessionContext;

    private static final long LOCK_OVERDUE_MS = 10000;
    private static final long LOCK_REFRESH_PERIOD_MS = 3000;

    private static final Logger logger = LoggerFactory.getLogger(InterserverLockingServiceImpl.class);

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    @Autowired
    private InterserverLockingDao interserverLockingDao;

    private final ConcurrentHashMap<String, ScheduledFutureEx> heldLocks = new ConcurrentHashMap<>();

    @Override
    public synchronized boolean lock(final String resourceId) {
        logger.trace("Start lock {}", resourceId);
        boolean result = transactionalLock(resourceId);

        if (result) {
            ScheduledFutureEx future = new ScheduledFutureEx(getExecutorService().scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    logger.trace("Start lock scheduler task " + resourceId);
                    try {
                        sessionContext.getUserTransaction().begin();
                        getInterserverLockingDao().updateLock(resourceId, new Date());
                        sessionContext.getUserTransaction().commit();
                    } catch (Exception ex) {
                        heldLocks.remove(resourceId);
                        logger.error("Error while updating lock on resource " + resourceId + ". Lock released.", ex);
                        try {
                            sessionContext.getUserTransaction().rollback();
                        } catch (Exception ignoreEx) {
                            logger.warn("Error rollback transaction", ignoreEx);
                        }
                    }
                    logger.trace("End lock scheduler task " + resourceId);
                }
            }, getLockRefreshPeriod(), getLockRefreshPeriod(), TimeUnit.MILLISECONDS));

            future = heldLocks.put(resourceId, future);
            if (future != null) {
                future.cancel(true);
            }
        }
        logger.trace("End lock {} return {}", resourceId, result);
        return result;
    }

    @Override
    public synchronized boolean selfSharedLock(final String resourceId) {
        logger.trace("Start selfSharedLock {}", resourceId);
        boolean result = true;
        Date lockTime = null;
        LOCK_STATUS lockStatus = LOCK_STATUS.NO_LOCK;
        try {
            LockResult lockResult = transactionalSelfSharedLock(resourceId);
            lockTime = lockResult.getLockTime();
            lockStatus = lockResult.getLockStatus();
            if (lockStatus != LOCK_STATUS.OWN_LOCK && lockStatus != LOCK_STATUS.NEW_LOCK) {
                result = false;
            }
        } catch (DuplicateKeyException ex) {
            result = false;
        }

        if (result && lockStatus != LOCK_STATUS.OWN_LOCK) {

            ScheduledFutureEx future = new ScheduledFutureEx(getExecutorService().scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    logger.trace("Start selfSharedLock scheduler task " + resourceId);
                    try {
                        sessionContext.getUserTransaction().begin();
                        Date newDate = new Date();
                        ScheduledFutureEx future = heldLocks.get(resourceId);
                        Date oldDate = future != null ? future.getLockTime() : null;
                        getInterserverLockingDao().updateLock(resourceId, oldDate, newDate);
                        if (future != null) {
                            future.setLockTime(newDate);
                        }
                        sessionContext.getUserTransaction().commit();
                    } catch (Exception ex) {
                        heldLocks.remove(resourceId);
                        logger.error("Error while updating lock on resource " + resourceId + ". Lock released.", ex);
                        try {
                            sessionContext.getUserTransaction().rollback();
                        } catch (Exception ignoreEx) {
                            logger.warn("Error rollback transaction", ignoreEx);
                        }
                    }
                    logger.trace("End selfSharedLock scheduler task " + resourceId);
                }
            }, getLockRefreshPeriod(), getLockRefreshPeriod(), TimeUnit.MILLISECONDS), lockTime);
            future = heldLocks.put(resourceId, future);
            if (future != null) {
                future.cancel(true);
            }
        }
        logger.trace("End selfSharedLock {} return {}", resourceId, result);
        return result;
    }

    private boolean transactionalLock(final String resourceId) {
        try {
            sessionContext.getUserTransaction().begin();
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
        } catch (DuplicateKeyException ex) {
            try {
                sessionContext.getUserTransaction().rollback();
            } catch (Exception ignoreEx) {
                logger.warn("Error rollback transaction", ignoreEx);
            }
            return false;
        } catch (Exception ex) {
            try {
                sessionContext.getUserTransaction().rollback();
            } catch (Exception ignoreEx) {
                logger.warn("Error rollback transaction", ignoreEx);
            }
            throw new FatalException("Error transactionalLock", ex);
        } finally {
            try {
                if (sessionContext.getUserTransaction().getStatus() == Status.STATUS_ACTIVE) {
                    sessionContext.getUserTransaction().commit();
                }
            } catch (Exception ignoreEx) {
                logger.warn("Error commit transaction", ignoreEx);
            }
        }
    }

    /**
     * Попытка заблокировать ресурс, с учетом того, какой сервер пытается
     * наложить блокировку.
     * @param resourceId
     *            идентификатор (имя) ресурса
     * @return true, если удалось заблокировать, или если ресурс уже
     *         заблокирован объектом, накладывающим блокировку false в противном
     *         случае
     * @return результат попытки блокировки (статус + отсечка времени
     *         блокировки)
     */
    LockResult transactionalSelfSharedLock(final String resourceId) {
        try {
            sessionContext.getUserTransaction().begin();
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
                return lockTime.getTime() == (oldLockTime != null ? oldLockTime.getTime() : 0) ? new LockResult(LOCK_STATUS.OWN_LOCK, lockTime)
                        : new LockResult(LOCK_STATUS.OTHER_LOCK, lockTime);
            }
            lockTime = new Date();
            if (!getInterserverLockingDao().lock(resourceId, lockTime)) {
                return new LockResult(LOCK_STATUS.OTHER_LOCK, lockTime);
            }
            return new LockResult(LOCK_STATUS.NEW_LOCK, lockTime);
        } catch (Exception ex) {
            try {
                sessionContext.getUserTransaction().rollback();
            } catch (Exception ignoreEx) {
                logger.warn("Error rollback transaction", ignoreEx);
            }
            throw new FatalException("Error transactionalLock", ex);
        } finally {
            try {
                if (sessionContext.getUserTransaction().getStatus() == Status.STATUS_ACTIVE) {
                    sessionContext.getUserTransaction().commit();
                }
            } catch (Exception ignoreEx) {
                logger.warn("Error commit transaction", ignoreEx);
            }
        }
    }

    @Override
    public boolean isLocked(String resourceId) {
        logger.trace("Start is locked {}", resourceId);
        InterserverLockingDao dao = getInterserverLockingDao();
        Date overdue = new Date(System.currentTimeMillis() - getLockMaxOverdue());
        Date time = dao.getLastLockTime(resourceId);
        boolean result = time != null && time.after(overdue);
        logger.trace("End is locked {} return {}", resourceId, result);
        return result;
    }

    protected InterserverLockingDao getInterserverLockingDao() {
        return interserverLockingDao;
    }

    @Override
    public synchronized void unlock(String resourceId) {
        logger.trace("Start unlock {}", resourceId);
        ScheduledFutureEx future = heldLocks.get(resourceId);
        if (future != null) {
            future.cancel(true);
            getInterserverLockingDao().unlock(resourceId);
            heldLocks.remove(resourceId);
            logger.trace("End unlock {}", resourceId);
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
        logger.trace("Start waitUntilNotLocked {}", resourceId);

        final Semaphore semaphore = new Semaphore(0);

        final ScheduledFuture<?> future = getExecutorService().scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                if (!isLocked(resourceId)) {
                    semaphore.release();
                    logger.debug("Resource {} unlocked", resourceId);
                }
            }
        }, getLockRefreshPeriod(), getLockRefreshPeriod(), TimeUnit.MILLISECONDS);

        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        future.cancel(true);
        logger.trace("End waitUntilNotLocked {}", resourceId);
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

        private void cancel(boolean mayInterruptIfRunning) {
            scheduledFuture.cancel(mayInterruptIfRunning);
        }
    }

    /**
     * Статус блокировки.
     * @author mike
     *
     */
    private enum LOCK_STATUS {
        /** Создана новая наложена */
        NEW_LOCK,
        /** Блокировка уже наложена сервером, запрашивающим блокировку */
        OWN_LOCK,
        /** Блокировка наложена другим сервером */
        OTHER_LOCK,
        /** Нет блокировки */
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
