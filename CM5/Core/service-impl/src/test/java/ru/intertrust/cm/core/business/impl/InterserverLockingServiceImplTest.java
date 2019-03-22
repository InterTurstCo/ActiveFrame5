package ru.intertrust.cm.core.business.impl;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Matchers.any;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.security.RunAs;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Local;
import javax.ejb.SessionContext;
import javax.ejb.Singleton;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.interceptor.Interceptors;
import javax.transaction.UserTransaction;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import org.springframework.test.util.ReflectionTestUtils;

import ru.intertrust.cm.core.business.api.InterserverLockingService;
import ru.intertrust.cm.core.dao.api.InterserverLockingDao;

public class InterserverLockingServiceImplTest {
    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private InterserverLockingServiceImpl first = testInstance();

    private InterserverLockingServiceImpl testInstance(final long overdue) {
        InterserverLockingServiceImpl result = new InterserverLockingServiceImpl() {
            @Override
            protected InterserverLockingDao getInterserverLockingDao() {
                return new FakeInterserverLockingDao();
            }

            @Override
            protected long getLockMaxOverdue() {
                return overdue > 0 ? overdue : super.getLockMaxOverdue();
            }
        };
        
        mockJ2eeObjects(result);
        return result;        
    }
    
    private void mockJ2eeObjects(final InterserverLockingServiceImpl service) {
        UserTransaction userTransaction = mock(UserTransaction.class);
        SessionContext sessionContext = mock(SessionContext.class);
        when(sessionContext.getUserTransaction()).thenReturn(userTransaction);
        
        TimerService timerService = mock(TimerService.class);
        when(timerService.createIntervalTimer(any(Date.class), any(Long.class), any(TimerConfig.class))).then(new Answer<Timer>() {

            @Override
            public Timer answer(InvocationOnMock invocation) throws Throwable {
                final Timer timer = mock(Timer.class);
                
                // Timer.getInfo()
                when(timer.getInfo()).thenReturn(invocation.getArgumentAt(2, TimerConfig.class).getInfo());

                // onTimeout() имитация счетчика
                final ScheduledFuture future = executorService.scheduleWithFixedDelay(
                        new Runnable() {
                            @Override
                            public void run() {
                                ReflectionTestUtils.invokeMethod(service, "onTimeout", timer);
                            }
                        },
                        invocation.getArgumentAt(0, Date.class).getTime() - System.currentTimeMillis(),
                        invocation.getArgumentAt(1, Long.class), 
                        TimeUnit.MILLISECONDS);

                // Timer.cancel()
                doAnswer(new Answer<Void>() {
                    @Override
                    public Void answer(InvocationOnMock invocation) throws Throwable {
                        future.cancel(true);
                        return null;
                    }
                }).when(timer).cancel();
                
                return timer;
            }
            
        });
        
        ReflectionTestUtils.setField(service, "sessionContext", sessionContext);
        ReflectionTestUtils.setField(service, "timerService", timerService);
        
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
        };
        
        mockJ2eeObjects(result);
        return result;
    }

    private InterserverLockingServiceImpl testInstance() {
        return testInstance(0);
    }

    private InterserverLockingServiceImpl second = testInstance();

    public static class FakeInterserverLockingDao implements InterserverLockingDao {

        private final static ConcurrentHashMap<String, Date> locks = new ConcurrentHashMap<>();

        @Override
        public boolean lock(String resourceId, Date date) {
            Date previous = locks.putIfAbsent(resourceId, date);
            if (previous != null) {
                throw new DuplicateKeyException(resourceId);
            }
            return true;
        }

        @Override
        public void unlock(String resourceId) {
            locks.remove(resourceId);
            System.out.println(resourceId + " is unlocked at " + (new Date()).getTime());
        }

        @Override
        public Date getLastLockTime(String resourceId) {
            return locks.get(resourceId);
        }

        @Override
        public void updateLock(String resourceId, Date lockTime) {
            locks.put(resourceId, lockTime);
        }

        @Override
        public void updateLock(String resourceId, Date oldLockTime, Date lockTime) {
            locks.put(resourceId, lockTime);
        }

        @Override
        public boolean unlock(String resourceId, Date lockTime) {
            return locks.remove(resourceId, lockTime);
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
        assertFalse(second.isLocked("abc"));
        first.lock("abc");
        assertTrue(second.isLocked("abc"));
    }

    @Test
    public void testUnlock() {
        first.lock("abc");
        first.unlock("abc");
        assertFalse(second.isLocked("abc"));
    }

    @Test
    public void testUnlockAfterRelock() throws InterruptedException {
        first = testInstance(1000, 300);
        first.lock("abc");
        Thread.sleep(1000);
        first.unlock("abc");
    }

    @Test
    public void testOnlyLockerCanUnlock() {
        thrown.expect(RuntimeException.class);
        first.lock("abc");
        second.unlock("abc");
    }

    @Test
    public void testCantLockLocked() {
        first.lock("abc");
        assertFalse(second.lock("abc"));
    }

    @Test
    public void testAutoUnlock() throws InterruptedException {
        first = testInstance(200);
        second = testInstance(200);
        first.lock("abc");
        first.getExecutorService().shutdownNow();
        first.getExecutorService().awaitTermination(5, TimeUnit.SECONDS);
        Thread.sleep(400);
        second.lock("abc");
    }

    @Test
    public void testSystemEjbAnnotations() {
        assertNotNull(InterserverLockingServiceImpl.class.getAnnotation(Singleton.class));
        assertEquals(ConcurrencyManagementType.BEAN, InterserverLockingServiceImpl.class.getAnnotation(ConcurrencyManagement.class).value());
        assertArrayEquals(new Class[] {InterserverLockingService.class }, InterserverLockingServiceImpl.class.getAnnotation(Local.class).value());
        assertEquals("system", InterserverLockingServiceImpl.class.getAnnotation(RunAs.class).value());
        assertArrayEquals(new Class[] {SpringBeanAutowiringInterceptor.class }, InterserverLockingServiceImpl.class.getAnnotation(Interceptors.class).value());
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
                first.unlock("abc");
            }
        };
        first.lock("abc");
        assertTrue(second.isLocked("abc"));
        thread.start();
        second.waitUntilNotLocked("abc");
        assertFalse(second.isLocked("abc"));
    }

}
