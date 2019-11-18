package ru.intertrust.cm.core.business.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.annotation.security.RunAs;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.transaction.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;

import ru.intertrust.cm.core.business.api.ClusteredLockService;
import ru.intertrust.cm.core.business.api.InterserverLockingService;
import ru.intertrust.cm.core.business.api.dto.ClusteredLock;
import ru.intertrust.cm.core.business.api.dto.impl.ClusteredLockImpl;
import ru.intertrust.cm.core.dao.api.clusterlock.ClusteredLockDao;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.core.util.SpringBeanAutowiringInterceptor;
import ru.intertrust.cm.globalcacheclient.ClusterTransactionStampService;

@Stateless(name = "ClusteredLockService")
@RunAs("system")
@Local(ClusteredLockService.class)
@Remote(ClusteredLockService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class ClusteredLockServiceImpl implements ClusteredLockService {
    private static final Logger logger = LoggerFactory.getLogger(ClusteredLockServiceImpl.class);

    @Resource
    private SessionContext sessionContext;

    @Autowired
    private ClusteredLockDao clusteredLockDao;

    @Autowired
    private InterserverLockingService interserverLockingService;

    @Autowired
    private ClusterTransactionStampService clusterTransactionStampService;

    @Value("${clustered.lock.service.check.period:10}")
    private long checkLockPeriod;

    private ScheduledExecutorService schedule = Executors.newSingleThreadScheduledExecutor();

    @Override
    public Set<ClusteredLock> list(String category) {
        return new HashSet<>(clusteredLockDao.findAll(category));
    }

    @Override
    public ClusteredLock lock(String category, String name, String owner, Duration autoUnlockTimeout) throws InterruptedException {
        // Пытаемся создать блокировку
        final CreateOrFindLockResult lockResult = createOrFindLock(category, name, owner, autoUnlockTimeout);

        // Проверяем мы ли захватили блокировку
        if (!lockResult.grabed) {
            // Создать не получилось, значит блокировка занята, ждем
            final Semaphore semaphore = new Semaphore(0);

            ScheduledFuture<?> future = schedule.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run()  {
                    // Пытаемся повторно создать блокировку
                    logger.debug("Check unlocked {} {}", category, name);
                    CreateOrFindLockResult secondLockResult = createOrFindLock(category, name, owner, autoUnlockTimeout);
                    if (secondLockResult.grabed) {
                        lockResult.lock = secondLockResult.lock;
                        // Отпускаем семафор
                        semaphore.release();
                    }
                }
            }, checkLockPeriod, checkLockPeriod, TimeUnit.SECONDS);

            logger.debug("Start wait unlock {} {}", category, name);
            semaphore.acquire();

            future.cancel(true);
            logger.debug("End wait unlock {} {}", category, name);
        }

        ClusteredLock result = lockResult.lock;

        // Блокировку установили, теперь проверяем актуальность данных. Ждем если не актуальны
        String resourceId = category + "-" + name;
        interserverLockingService.waitUntilActualData(resourceId, result.getStampInfo());

        return result;
    }

    /**
     * Метод ищет блокировку. Если находит и блокировка свободна то занимает ее, если не находи то создает.
     * Если находит и блокировка занято то возвращает ее не меняя
     * @param category
     * @param name
     * @param owner
     * @param autoUnlockTimeout
     * @return
     */
    private CreateOrFindLockResult createOrFindLock(String category, String name, String owner, Duration autoUnlockTimeout) {
        CreateOrFindLockResult result = new CreateOrFindLockResult();
        try {
            sessionContext.getUserTransaction().begin();
            // Ищем блокировку
            ClusteredLockImpl lock = clusteredLockDao.find(category, name, true);
            if (lock == null) {
                // Не нашли, создаем
                result.lock = clusteredLockDao.create(category, name, null, owner, Instant.now(), autoUnlockTimeout, null);
                result.grabed = true;
            } else if (!lock.isLocked()) {
                // Нашли блокировку и она свободна, занимаем
                result.lock = clusteredLockDao.update(category, name, null, owner, Instant.now(), autoUnlockTimeout, null);
                result.grabed = true;
            }else{
                result.lock = lock;
                result.grabed = false;
            }
            sessionContext.getUserTransaction().commit();
        } catch (DuplicateKeyException ex) {
            // Значит между find и create кто то успел создать блокировку, получаем ее
            result.lock = clusteredLockDao.find(category, name, false);
            result.grabed = false;
        } catch (Exception ex) {
            throw new FatalException("Error create lock", ex);
        } finally {
            try {
                if (sessionContext.getUserTransaction().getStatus() == Status.STATUS_ACTIVE) {
                    sessionContext.getUserTransaction().rollback();
                }
            } catch (Exception ex) {
                logger.warn("Error rollback transaction in create lock method", ex);
            }
        }
        return result;
    }

    @Override
    public ClusteredLock tryLock(String category, String name, String owner, Duration autoUnlockTimeout, Duration waitingTime) throws InterruptedException {
        ClusteredLock result = null;
        // Пытаемся создать блокировку
        final CreateOrFindLockResult lockResult = createOrFindLock(category, name, owner, autoUnlockTimeout);

        // Проверяем мы ли захватили блокировку
        if (!lockResult.grabed) {
            // Создать не получилось, значит блокировка занята
            // Проверяем владельца блокировки
            if (!owner.equals(lockResult.lock.getOwner())){
                // владелец блокировки не совпадает
                // ждем некоторе время и пытаемся опять захватить блокировку
                final Semaphore semaphore = new Semaphore(0);
                long start = System.currentTimeMillis();
                ScheduledFuture<?> future = schedule.scheduleWithFixedDelay(new Runnable() {
                    @Override
                    public void run() {
                        // Пытаемся повторно создать блокировку
                        logger.debug("Check unlocked {} {}", category, name);
                        CreateOrFindLockResult secondLockResult = createOrFindLock(category, name, owner, autoUnlockTimeout);
                        if (secondLockResult.grabed) {
                            lockResult.lock = secondLockResult.lock;
                            // Отпускаем семафор
                            semaphore.release();
                        } else {
                            // Проверяем таймаут
                            if (System.currentTimeMillis() > start + (waitingTime.getSeconds() * 1000)) {
                                logger.warn("Finish wait loc {} {} by timeout.", category, name);
                                lockResult.lock = secondLockResult.lock;
                                lockResult.lock.setLocked(false);
                                // Отпускаем семафор
                                semaphore.release();
                            }
                        }
                    }
                }, checkLockPeriod, checkLockPeriod, TimeUnit.SECONDS);

                logger.debug("Start wait unlock {} {}", category, name);
                // Ждем пока отпустят семафор
                semaphore.acquire();

                future.cancel(true);
                logger.debug("End wait unlock {} {}", category, name);
            }else{
                lockResult.lock.setLocked(false);
            }
        }

        result = lockResult.lock;

        // Блокировку установили, теперь проверяем актуальность данных. Ждем если не актуальны
        String resourceId = category + "-" + name;
        interserverLockingService.waitUntilActualData(resourceId, result.getStampInfo());

        return result;
    }

    @Override
    public void unlock(ClusteredLock lock) {
        ClusteredLock clusteredLock = clusteredLockDao.find(lock.getCategory(), lock.getName(), true);
        if (clusteredLock != null && clusteredLock.isLocked()){
            clusteredLockDao.update(lock.getCategory(), lock.getName(), lock.getTag(),
                    null, null, null,
                    clusterTransactionStampService.getInvalidationCacheInfo().encode());
        }
    }

    @Override
    public ClusteredLock getLock(String category, String name) {
        return clusteredLockDao.find(category, name, false);
    }

    public static class CreateOrFindLockResult{
        ClusteredLockImpl lock;
        boolean grabed;
    }

}
