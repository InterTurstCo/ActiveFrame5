package ru.intertrust.cm.core.business.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import ru.intertrust.cm.core.business.api.InterserverLockingService;
import ru.intertrust.cm.core.dao.api.InterserverLockingDao;

public class InterserverLockingServiceImpl implements InterserverLockingService {

    private static final long LOCK_OVERDUE_MS = 10000;
    private static final long LOCK_REFRESH_PERIOD_MS = 5000;

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    @Autowired
    private InterserverLockingDao interserverLockingDao;

    private final HashMap<String, ScheduledFuture<?>> heldLocks = new HashMap<>();

    @Override
    public synchronized void lock(final String resourceId) {
        if (isLocked(resourceId)) {
            throw new RuntimeException("Resource is locked already.");
        } else {
            getInterserverLockingDao().lock(resourceId, new Date());
            ScheduledFuture<?> future = getExecutorService().scheduleWithFixedDelay(new Runnable() {
                @Override
                @Transactional
                public void run() {
                    getInterserverLockingDao().lock(resourceId, new Date());
                }
            }, LOCK_REFRESH_PERIOD_MS, LOCK_REFRESH_PERIOD_MS, TimeUnit.MILLISECONDS);
            heldLocks.put(resourceId, future);
        }
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
            future.cancel(false);
            getInterserverLockingDao().unlock(resourceId);
            heldLocks.remove(resourceId);
        } else {
            throw new RuntimeException("Only locker can unlock resource.");
        }
    }

    @Override
    public synchronized boolean tryLock(String resourceId) {
        if (isLocked(resourceId)) {
            return false;
        } else {
            lock(resourceId);
            return true;
        }
    }

    protected long getLockMaxOverdue() {
        return LOCK_OVERDUE_MS;
    }

    protected ScheduledExecutorService getExecutorService() {
        return executorService;
    }

}
