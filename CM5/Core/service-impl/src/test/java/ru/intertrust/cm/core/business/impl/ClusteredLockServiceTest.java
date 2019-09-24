package ru.intertrust.cm.core.business.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import ru.intertrust.cm.core.business.api.ClusteredLockService;
import ru.intertrust.cm.core.business.api.InterserverLockingService;
import ru.intertrust.cm.core.business.api.dto.ClusteredLock;
import ru.intertrust.cm.core.dao.api.clusterlock.ClusteredLockDao;
import ru.intertrust.cm.core.business.api.dto.impl.ClusteredLockImpl;
import ru.intertrust.cm.globalcacheclient.ClusterTransactionStampService;
import ru.intertrust.cm.globalcacheclient.impl.ClusterCommitStampsInfo;

import javax.ejb.SessionContext;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ClusteredLockServiceTest {

    @InjectMocks
    private ClusteredLockService lockService = new ClusteredLockServiceImpl();

    @Mock
    private InterserverLockingService interserverLockingService;

    @Mock
    private ClusterTransactionStampService clusterTransactionStampService;

    @Before
    public void init() throws SystemException {
        SessionContext sessionContext = mock(SessionContext.class);
        UserTransaction transaction = mock(UserTransaction.class);
        when(sessionContext.getUserTransaction()).thenReturn(transaction);
        when(transaction.getStatus()).thenReturn(Status.STATUS_ACTIVE);
        when(clusterTransactionStampService.getInvalidationCacheInfo()).thenReturn(new ClusterCommitStampsInfo());

        ReflectionTestUtils.setField(lockService, "sessionContext", sessionContext);
        ReflectionTestUtils.setField(lockService, "clusteredLockDao", new ClusteredLockDao() {
            private List<ClusteredLockImpl> locks = new ArrayList<ClusteredLockImpl>();

            @Override
            public void init() {
            }

            @Override
            public ClusteredLockImpl create(String category, String name, String tag, String owner, Instant lockTime, Duration duration, String stampInfo) {
                ClusteredLockImpl result = new ClusteredLockImpl(name, category, lockTime != null, owner, lockTime, duration,  tag, stampInfo);
                locks.add(result);
                return result;
            }

            @Override
            public ClusteredLockImpl update(String category, String name, String tag, String owner, Instant lockTime, Duration duration, String stampInfo) {
                ClusteredLockImpl result = null;
                for (ClusteredLockImpl lockObject : locks ) {
                    if (lockObject.getCategory().equals(category) && lockObject.getName().equals(name)) {
                        result = lockObject;
                        break;
                    }
                }

                if (result == null){
                    result = new ClusteredLockImpl();
                    locks.add(result);
                    result.setName(name);
                    result.setCategory(category);
                }

                result.setTag(tag);
                result.setOwner(owner);
                result.setLockTime(lockTime);
                result.setAutoUnlockTimeout(duration);
                result.setStampInfo(stampInfo);

                result.setLocked(lockTime != null);
                return result;
            }

            @Override
            public ClusteredLockImpl find(String category, String name, boolean lock) {
                ClusteredLockImpl result = null;
                for (ClusteredLockImpl lockObject : locks ) {
                    if (lockObject.getCategory().equals(category) && lockObject.getName().equals(name)){
                        result = lockObject;

                        if (lockObject.getLockTime().isPresent() && result.getLockTime().get().plusMillis(result.getAutoUnlockTimeout().toMillis()).compareTo(Instant.now()) > 0) {
                            result.setLocked(true);
                        }else{
                            result.setLocked(false);
                        }

                        break;
                    }
                }
                return result;
            }

            @Override
            public Set<ClusteredLockImpl> findAll(String category) {
                Set<ClusteredLockImpl> result = new HashSet<ClusteredLockImpl>();
                for (ClusteredLockImpl lockObject : locks ) {
                    if (lockObject.getCategory().equals(category)){
                        result.add(lockObject);

                        if (lockObject.getLockTime().isPresent() && lockObject.getLockTime().get().plusMillis(lockObject.getAutoUnlockTimeout().toMillis()).compareTo(Instant.now()) > 0) {
                            lockObject.setLocked(true);
                        }else{
                            lockObject.setLocked(false);
                        }

                    }
                }
                return result;
            }

            @Override
            public void delete(String category, String name) {
                for (int i=locks.size()-1; i>=0;i--) {
                    if (locks.get(i).getCategory().equals(category) && locks.get(i).getName().equals(name)){
                        locks.remove(i);
                        break;
                    }
                }
            }
        });

        ReflectionTestUtils.setField(lockService, "checkLockPeriod", 1);

    }

    @Test
    public void testLock() throws InterruptedException {
        Set<ClusteredLock> locks = lockService.list("category-1");
        assertTrue(getActiveLockCount(locks) == 0);

        ClusteredLock lock = lockService.lock("category-1", "name-1", "owner-1", Duration.ofSeconds(2));
        assertNotNull(lock);
        assertTrue(lock.isLocked());

        locks = lockService.list("category-1");
        assertTrue(getActiveLockCount(locks) == 1);

        lock = lockService.getLock("category-1", "name-1");
        assertTrue(lock.isLocked());

        lock = lockService.lock("category-1", "name-1", "owner-1", Duration.ofSeconds(2));
        assertNotNull(lock);
        assertTrue(lock.isLocked());

        lockService.unlock(lock);
        locks = lockService.list("category-1");
        assertTrue(getActiveLockCount(locks) == 0);

        lock = lockService.getLock("category-1", "name-1");
        assertFalse(lock.isLocked());

    }

    @Test
    public void testTryLock() throws InterruptedException {
        Set<ClusteredLock> locks = lockService.list("category-1");
        assertTrue(getActiveLockCount(locks) == 0);

        // Создаем блокировку
        ClusteredLock firstLock = lockService.tryLock("category-1", "name-1", "owner-1", Duration.ofSeconds(100), Duration.ofSeconds(2));
        assertNotNull(firstLock);
        assertTrue(firstLock.isLocked());

        // Пытаемся ее получить с другим owner, после таймаута блокировку получить не удается
        ClusteredLock lock = lockService.tryLock("category-1", "name-1", "owner-2", Duration.ofSeconds(100), Duration.ofSeconds(2));
        assertNotNull(lock);
        assertFalse(lock.isLocked());

        // Пытаемся ее получить с тем же owner, сразу же получаем отказ
        lock = lockService.tryLock("category-1", "name-1", "owner-1", Duration.ofSeconds(100), Duration.ofSeconds(2));
        assertNotNull(lock);
        assertFalse(lock.isLocked());

        lockService.unlock(firstLock);
    }


    private int getActiveLockCount(Set<ClusteredLock> locks){
        int result = 0;
        for (ClusteredLock lock: locks) {
            if (lock.isLocked()) {
                result++;
            }
        }
        return result;
    }
}
