package ru.intertrust.cm.core.business.impl;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.security.RunAs;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Local;
import javax.ejb.SessionContext;
import javax.ejb.Singleton;
import javax.interceptor.Interceptors;
import javax.transaction.UserTransaction;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import org.springframework.test.util.ReflectionTestUtils;

import ru.intertrust.cm.core.business.api.InterserverLockingService;
import ru.intertrust.cm.core.business.api.Stamp;
import ru.intertrust.cm.core.dao.api.ClusterManagerDao;
import ru.intertrust.cm.core.dao.api.InterserverLockingDao;
import ru.intertrust.cm.core.dao.impl.StampImpl;
import ru.intertrust.cm.globalcacheclient.ClusterTransactionStampService;
import ru.intertrust.cm.globalcacheclient.impl.ClusterCommitStampsInfo;
import ru.intertrust.cm.globalcacheclient.impl.ClusterTransactionStampServiceImpl;

public class InterserverLockingServiceImplTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private InterserverLockingServiceImpl first;
    private InterserverLockingServiceImpl second;

    @Before
    public void init() {
        first = testInstance();
        second = testInstance();
    }

    private InterserverLockingServiceImpl testInstance(final long overdue) {
        InterserverLockingServiceImpl result = new InterserverLockingServiceImpl() {
            private ClusterTransactionStampService clusterTransactionStampService;
            
            @Override
            protected InterserverLockingDao getInterserverLockingDao() {
                return new FakeInterserverLockingDao();
            }

            @Override
            protected long getLockMaxOverdue() {
                return overdue > 0 ? overdue : super.getLockMaxOverdue();
            }
            
            @Override
            protected ClusterTransactionStampService getClusterTransactionStampService() {
                if (clusterTransactionStampService == null) {
                    clusterTransactionStampService = new ClusterTransactionStampServiceImpl();
                    ReflectionTestUtils.setField(clusterTransactionStampService, "clusterManagerDao", new ClusterManagerDao() {

                        @Override
                        public String getNodeName() {
                            return "node-1";
                        }

                        @Override
                        public String getNodeId() {
                            return "node-1-id";
                        }
                    });
                }
                return clusterTransactionStampService;
            }
        };

        mockJ2eeObjects(result);
        return result;
    }

    private void mockJ2eeObjects(final InterserverLockingServiceImpl service) {
        UserTransaction userTransaction = mock(UserTransaction.class);
        SessionContext sessionContext = mock(SessionContext.class);
        when(sessionContext.getUserTransaction()).thenReturn(userTransaction);

        ReflectionTestUtils.setField(service, "sessionContext", sessionContext);
    }

    private InterserverLockingServiceImpl testInstance(final long overdue, final long refresh) {
        InterserverLockingServiceImpl result = new InterserverLockingServiceImpl() {
            @Override
            protected InterserverLockingDao getInterserverLockingDao() {
                return new FakeInterserverLockingDao();
            }

            @Override
            protected long getLockMaxOverdue() {
                return overdue > 0 ? overdue : super.getLockMaxOverdue();
            }

            @Override
            protected long getLockRefreshPeriod() {
                return refresh;
            }
            
            @Override
            protected ClusterTransactionStampService getClusterTransactionStampService() {
                return new ClusterTransactionStampServiceImpl();
            }            
        };

        mockJ2eeObjects(result);
        return result;
    }

    private InterserverLockingServiceImpl testInstance() {
        return testInstance(0);
    }

    public static class FakeInterserverLockingDao implements InterserverLockingDao {

        private final static ConcurrentHashMap<String, Date> locks = new ConcurrentHashMap<>();
        private final static ConcurrentHashMap<String, String> stamps = new ConcurrentHashMap<>();

        @Override
        public boolean lock(String resourceId, Date date) {
            synchronized (locks) {
                Date previous = locks.putIfAbsent(resourceId, date);
                if (previous != null) {
                    throw new DuplicateKeyException(resourceId);
                }
                return true;
            }
        }

        @Override
        public void unlock(String resourceId, String stampInfo) {
            synchronized (locks) {
                locks.remove(resourceId);
                stamps.put(resourceId, stampInfo);
                System.out.println(resourceId + " is unlocked at " + (new Date()).getTime());
            }
        }

        @Override
        public Date getLastLockTime(String resourceId) {
            synchronized (locks) {
                return locks.get(resourceId);
            }
        }

        @Override
        public void updateLock(String resourceId, Date lockTime) {
            synchronized (locks) {
                if (locks.containsKey(resourceId)) {
                    locks.put(resourceId, lockTime);
                }
            }
        }

        @Override
        public void updateLock(String resourceId, Date oldLockTime, Date lockTime) {
            synchronized (locks) {
                if (locks.containsKey(resourceId) && locks.get(resourceId).equals(oldLockTime)) {
                    locks.put(resourceId, lockTime);
                }
            }
        }

        @Override
        public boolean unlock(String resourceId, Date lockTime) {
            synchronized (locks) {
                return locks.remove(resourceId, lockTime);
            }
        }

        @Override
        public String getStampInfo(String resourceId) {
            return stamps.get(resourceId);
        }
    }

    @After
    public void tearDown() throws InterruptedException {
        first.getExecutorService().shutdownNow();
        first.getExecutorService().awaitTermination(10, TimeUnit.SECONDS);
        second.getExecutorService().shutdownNow();
        second.getExecutorService().awaitTermination(10, TimeUnit.SECONDS);
        FakeInterserverLockingDao.locks.clear();
    }

    @Test
    public void testLock() {
        assertFalse(second.isLocked("testLock"));
        first.lock("testLock");
        assertTrue(second.isLocked("testLock"));
    }

    @Test
    public void testUnlock() {
        first.lock("testUnlock");
        first.unlock("testUnlock");
        assertFalse(second.isLocked("testUnlock"));
    }

    @Test
    public void testUnlockAfterRelock() throws InterruptedException {
        first = testInstance(1000, 300);
        first.lock("testUnlockAfterRelock");
        Thread.sleep(1000);
        first.unlock("testUnlockAfterRelock");
    }

    @Test
    public void testOnlyLockerCanUnlock() {
        thrown.expect(RuntimeException.class);
        first.lock("testOnlyLockerCanUnlock");
        second.unlock("testOnlyLockerCanUnlock");
    }

    @Test
    public void testCantLockLocked() {
        first.lock("testCantLockLocked");
        assertFalse(second.lock("testCantLockLocked"));
    }

    @Test
    public void testAutoUnlock() throws InterruptedException {
        first = testInstance(200);
        second = testInstance(200);
        first.lock("testAutoUnlock");
        first.getExecutorService().shutdownNow();
        first.getExecutorService().awaitTermination(5, TimeUnit.SECONDS);
        Thread.sleep(400);
        second.lock("testAutoUnlock");
    }

    @Test
    public void testSystemEjbAnnotations() {
        assertNotNull(InterserverLockingServiceImpl.class.getAnnotation(Singleton.class));
        assertEquals(ConcurrencyManagementType.BEAN, InterserverLockingServiceImpl.class.getAnnotation(ConcurrencyManagement.class).value());
        assertArrayEquals(new Class[] { InterserverLockingService.class }, InterserverLockingServiceImpl.class.getAnnotation(Local.class).value());
        assertEquals("system", InterserverLockingServiceImpl.class.getAnnotation(RunAs.class).value());
        assertArrayEquals(new Class[] { SpringBeanAutowiringInterceptor.class }, InterserverLockingServiceImpl.class.getAnnotation(Interceptors.class).value());
    }

    @Test
    public void testWaitUntilNotLocked() {
        first = testInstance(1000, 300);
        second = testInstance(1000, 300);
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                first.unlock("testWaitUntilNotLocked");
            }
        };
        first.lock("testWaitUntilNotLocked");
        assertTrue(second.isLocked("testWaitUntilNotLocked"));
        thread.start();
        second.waitUntilNotLocked("testWaitUntilNotLocked");
        assertFalse(second.isLocked("testWaitUntilNotLocked"));
    }

    @Test
    public void testEncodeDecodeClusterCommitStampsInfo() {
        Map<String, Stamp> nodesStamp = new HashMap<String, Stamp>();
        nodesStamp.put("node1", new StampImpl(100, 100));
        nodesStamp.put("node2", new StampImpl(200, 200));
        nodesStamp.put("node3", new StampImpl(300, 300));
        
        ClusterCommitStampsInfo stamps = new ClusterCommitStampsInfo(nodesStamp);
        
        String encoded = stamps.encode();
        ClusterCommitStampsInfo decoded = ClusterCommitStampsInfo.decode(encoded);
        
        assertEquals(decoded.getNodesStamps().size(), nodesStamp.size());
        
        for (String nodeId : decoded.getNodesStamps().keySet()) {
            assertEquals(decoded.getNodesStamps().get(nodeId), nodesStamp.get(nodeId));
        }
    }

    @Test
    public void testCompareClusterCommitStampsInfo() {
        Map<String, Stamp> nodesStamp1 = new HashMap<String, Stamp>();
        nodesStamp1.put("node1", new StampImpl(100, 100));
        nodesStamp1.put("node2", new StampImpl(200, 200));
        nodesStamp1.put("node3", new StampImpl(300, 300));
        
        ClusterCommitStampsInfo stamps1 = new ClusterCommitStampsInfo(nodesStamp1);
        
        Map<String, Stamp> nodesStamp2 = new HashMap<String, Stamp>();
        nodesStamp2.put("node1", new StampImpl(100, 100));
        nodesStamp2.put("node2", new StampImpl(200, 200));
        
        ClusterCommitStampsInfo stamps2 = new ClusterCommitStampsInfo(nodesStamp2);
        
        assertTrue(stamps2.equalsOrGreater(stamps1));

        nodesStamp2.put("node3", new StampImpl(300, 300));
        assertTrue(stamps2.equalsOrGreater(stamps1));

        nodesStamp2.put("node3", new StampImpl(400, 400));
        assertTrue(stamps2.equalsOrGreater(stamps1));

        nodesStamp2.put("node3", new StampImpl(200, 200));
        assertFalse(stamps2.equalsOrGreater(stamps1));
    }

    /**
     * тест ожидания актуальных данных в критической секции
     */
    @Test
    public void testWaitActualData() {
        first = testInstance();
        // Делаем таймаут 1 сек
        ReflectionTestUtils.setField(first, "actualDataTimeout", 1000);
        // Делаем период опроса меток времени 10 ms
        ReflectionTestUtils.setField(first, "checkInvalidationCacheRefreshPeriod", 10);
        
        first.lock("xxx");
        // Имитируем коммит транзакции и запись в ClusterTransactionStampService
        ClusterTransactionStampService stampServiceFirst = 
                (ClusterTransactionStampService)ReflectionTestUtils.invokeMethod(first, "getClusterTransactionStampService");
        stampServiceFirst.setLocalInvalidationCacheInfo(new StampImpl(100, 100));
        stampServiceFirst.setInvalidationCacheInfo("node-2", new StampImpl(100, 100));
        first.unlock("xxx");

        // Искуственно отодвигаем метки времени назад
        Map nodeStamps = (Map)ReflectionTestUtils.getField(stampServiceFirst, "nodesStamp");
        nodeStamps.put("node-2", new StampImpl(100, 50));
        
        // Запускаем поток, который будет имитировать обновление временных меток
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(300);
                    // Имитируем приход данных по JMS
                    nodeStamps.put("node-2", new StampImpl(100, 150));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };        
        thread.start();
        
        // Попытка заблокировать приводит к ожиданию более 300 сек (Через 300 ms в паралельном потоке обновятся временные метки)
        long start = System.currentTimeMillis();
        first.lock("xxx");
        assertTrue(System.currentTimeMillis() - start >= 300);
        first.unlock("xxx");
        
        // Опять отодвигаем данные временных меток
        nodeStamps.put("node-2", new StampImpl(100, 50));
        // Попытка заблокировать приводит к ожиданию более 1000 сек (сработает таймаут)
        start = System.currentTimeMillis();
        first.lock("xxx");
        assertTrue(System.currentTimeMillis() - start >= 1000);
        first.unlock("xxx");
    }

}
