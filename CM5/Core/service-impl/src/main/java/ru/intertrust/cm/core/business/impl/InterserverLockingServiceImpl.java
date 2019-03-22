package ru.intertrust.cm.core.business.impl;

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;

import javax.annotation.Resource;
import javax.annotation.security.RunAs;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Local;
import javax.ejb.SessionContext;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
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

    @Resource
    private TimerService timerService;
    
    
    private static final long LOCK_OVERDUE_MS = 10000;
    private static final long LOCK_REFRESH_PERIOD_MS = 3000;

    private static final Logger logger = LoggerFactory.getLogger(InterserverLockingServiceImpl.class);

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    @Autowired
    private InterserverLockingDao interserverLockingDao;

    private final ConcurrentHashMap<String, ScheduledFutureEx> heldLocks = new ConcurrentHashMap<>();

    public enum TimerType{
        lock,
        selfSharedLock,
        waitUntilNotLocked
    }
    
    public class TimerInfo implements Serializable{
        private TimerType timerType;
        private String resourceId;
        private Semaphore semaphore;
        
        public TimerInfo(TimerType timerType, String resourceId) {
            this.timerType = timerType;
            this.resourceId = resourceId;
        }

        public TimerInfo(TimerType timerType, String resourceId, Semaphore semaphore) {
            this.timerType = timerType;
            this.resourceId = resourceId;
            this.semaphore = semaphore;
        }

        public TimerType getTimerType() {
            return timerType;
        }

        public String getResourceId() {
            return resourceId;
        }

        public Semaphore getSemaphore() {
            return semaphore;
        }
    }
    
    /**
     * Метод обработки таймеров
     * @param timer
     */
    @Timeout
    public void onTimeout(Timer timer) {
        TimerInfo timerInfo = (TimerInfo)timer.getInfo();
        logger.trace("Start onTimeout type:{}; resourceId:{}", timerInfo.getTimerType(), timerInfo.getResourceId());
        if (timerInfo.getTimerType().equals(TimerType.lock)) {
            // Таймер блокировки lock
            logger.debug("Start lock scheduler task");
            try {
                sessionContext.getUserTransaction().begin();
                getInterserverLockingDao().updateLock(timerInfo.getResourceId(), new Date());
                sessionContext.getUserTransaction().commit();
            } catch (Exception ex) {
                heldLocks.remove(timerInfo.getResourceId());
                logger.error("Error while updating lock on resource " + timerInfo.getResourceId() + ". Lock released.", ex);
                try {
                    sessionContext.getUserTransaction().rollback();
                } catch (Exception ignoreEx) {
                    logger.warn("Error rollback transaction", ignoreEx);
                }
            }            
        }else if (timerInfo.getTimerType().equals(TimerType.selfSharedLock)) {
            // Таймер блокировки selfSharedLock
            logger.debug("Start lock scheduler task");
            try {
                sessionContext.getUserTransaction().begin();
                Date newDate = new Date();
                ScheduledFutureEx future = heldLocks.get(timerInfo.getResourceId());
                Date oldDate = future != null ? future.getLockTime() : null;
                getInterserverLockingDao().updateLock(timerInfo.getResourceId(), oldDate, newDate);
                if (future != null) {
                    future.setLockTime(newDate);
                }
                sessionContext.getUserTransaction().commit();
            } catch (Exception ex) {
                heldLocks.remove(timerInfo.getResourceId());
                logger.error("Error while updating lock on resource " + timerInfo.getResourceId() + ". Lock released.", ex);
                try {
                    sessionContext.getUserTransaction().rollback();
                } catch (Exception ignoreEx) {
                    logger.warn("Error rollback transaction", ignoreEx);
                }                    
            }
        }else if (timerInfo.getTimerType().equals(TimerType.waitUntilNotLocked)) {
            // Таймер блокировки waitUntilNotLocked
            if (!isLocked(timerInfo.getResourceId())) {
                timerInfo.getSemaphore().release();
                logger.debug("Resource {} unlocked", timerInfo.getResourceId());
            }
        }
        logger.trace("End onTimeout");
    }    
    
    @Override
    public synchronized boolean lock(final String resourceId) {
        logger.trace("Start lock {}", resourceId);
        boolean result = true;
        try {
            if (!transactionalLock(resourceId)) {
                result = false;
            }
        } catch (DuplicateKeyException ex) {
            result = false;
        }

        if (result) {
            Timer timer = timerService.createIntervalTimer(new Date(System.currentTimeMillis() + getLockRefreshPeriod()),
                    getLockRefreshPeriod(), new TimerConfig(new TimerInfo(TimerType.lock, resourceId), false));
            ScheduledFutureEx future = new ScheduledFutureEx(timer);

            future = heldLocks.put(resourceId, future);
            if (future != null) {
                future.cancel();
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
        
            Timer timer = timerService.createIntervalTimer(new Date(System.currentTimeMillis() + getLockRefreshPeriod()), 
                    getLockRefreshPeriod(), new TimerConfig(new TimerInfo(TimerType.selfSharedLock, resourceId), false));        
            ScheduledFutureEx future = new ScheduledFutureEx(timer, lockTime);        
    
            future = heldLocks.put(resourceId, future);
            if (future != null) {
                future.cancel();
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
        } catch (Exception ex) {
            try {
                sessionContext.getUserTransaction().rollback();
            } catch (Exception ignoreEx) {
                logger.warn("Error rollback transaction", ignoreEx);
            }
            throw new FatalException("Error transactionalLock", ex);
        }finally {
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
            future.cancel();
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
        
        Timer timer = timerService.createIntervalTimer(new Date(System.currentTimeMillis() + getLockRefreshPeriod()), 
                getLockRefreshPeriod(), new TimerConfig(new TimerInfo(TimerType.waitUntilNotLocked, resourceId, semaphore), false));           
        
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        timer.cancel();
        logger.trace("End waitUntilNotLocked {}", resourceId);
    }

    private class ScheduledFutureEx {
        private final Timer timer;
        private Date lockTime;
        
        private ScheduledFutureEx(Timer timer) {
            this(timer, null);
        }

        private ScheduledFutureEx(Timer timer, Date lockTime) {
            this.timer = timer;
            this.lockTime = lockTime;
        }
        
        private Date getLockTime() {
            return lockTime;
        }
        
        private void setLockTime(Date lockTime) {
            this.lockTime = lockTime;
        }
        
        private void cancel() {
            timer.cancel();
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
