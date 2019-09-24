package ru.intertrust.cm.remoteclient.lock.test;

import java.time.Duration;
import ru.intertrust.cm.core.business.api.ClusteredLockService;
import ru.intertrust.cm.core.business.api.dto.ClusteredLock;
import ru.intertrust.cm.remoteclient.ClientBase;

import java.util.Set;

public class TestClusteredLock extends ClientBase {

    public static void main(String[] args) {
        try {
            TestClusteredLock test = new TestClusteredLock();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void execute(String[] args) throws Exception {
        try {
            super.execute(args);

            ClusteredLockService clusteredLockService = getService("ClusteredLockService", ClusteredLockService.Remote.class);
            Set<ClusteredLock> locks = clusteredLockService.list("test");
            assertTrue("locks is empty", getActiveLockCount(locks) == 0 );

            ClusteredLock lock = clusteredLockService.lock("test", "name", "owner", Duration.ofSeconds(2));
            assertTrue("create lock", lock.isLocked());
            locks = clusteredLockService.list("test");
            assertTrue("find one lock", getActiveLockCount(locks) == 1);

            // Должны ждать, после ожидания захватываем блокировку и количество блокировок все равно 1 штука
            lock = clusteredLockService.lock("test", "name", "owner1", Duration.ofSeconds(2));
            assertTrue("create lock", lock.isLocked());
            locks = clusteredLockService.list("test");
            assertTrue("find one lock", getActiveLockCount(locks) == 1);

            // Освобождаем блокировку
            clusteredLockService.unlock(lock);
            locks = clusteredLockService.list("test");
            assertTrue("locks is empty", getActiveLockCount(locks) == 0);

            log(hasError ? "Test FAILURE" : "Test OK");
        } finally {
            writeLog();
        }
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
