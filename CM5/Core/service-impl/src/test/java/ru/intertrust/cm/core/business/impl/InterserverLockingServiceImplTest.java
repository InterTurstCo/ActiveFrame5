package ru.intertrust.cm.core.business.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import ru.intertrust.cm.core.dao.api.InterserverLockingDao;

public class InterserverLockingServiceImplTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private InterserverLockingServiceImpl first = testInstance();

    private InterserverLockingServiceImpl testInstance(final long overdue) {
        return new InterserverLockingServiceImpl() {
            @Override
            protected InterserverLockingDao getInterserverLockingDao() {
                return new FakeInterserverLockingDao();
            }

            @Override
            protected long getLockMaxOverdue() {
                return overdue > 0 ? overdue : super.getLockMaxOverdue();
            }
        };
    }

    private InterserverLockingServiceImpl testInstance() {
        return testInstance(0);
    }

    private InterserverLockingServiceImpl second = testInstance();

    public static class FakeInterserverLockingDao implements InterserverLockingDao {

        private final static HashMap<String, Date> locks = new HashMap<>();

        @Override
        public void lock(String resourceId, Date date) {
            locks.put(resourceId, date);
            System.out.println(resourceId + " is locked at " + date.getTime());
        }

        @Override
        public boolean wasLockedAfter(String resourceId, Date deadline) {
            Date date = locks.get(resourceId);
            return date != null && date.after(deadline);
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
    public void testOnlyLockerCanUnlock() {
        thrown.expect(RuntimeException.class);
        first.lock("abc");
        second.unlock("abc");
    }

    @Test
    public void testCantLockLocked() {
        thrown.expect(RuntimeException.class);
        first.lock("abc");
        second.lock("abc");
    }

    @Test
    public void testTryLockFailure() {
        first.lock("abc");
        assertFalse(second.tryLock("abc"));
    }

    @Test
    public void testTryLockSuccess() {
        assertTrue(first.tryLock("abc"));
    }

    @Test
    public void testAutoUnlock() throws InterruptedException {
        first = testInstance(200);
        second = testInstance(200);
        first.lock("abc");
        first.getExecutorService().shutdownNow();
        first.getExecutorService().awaitTermination(5, TimeUnit.SECONDS);
        Thread.sleep(200);
        second.lock("abc");
    }

}
