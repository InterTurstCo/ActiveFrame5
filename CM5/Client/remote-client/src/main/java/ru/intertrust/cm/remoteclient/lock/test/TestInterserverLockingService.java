package ru.intertrust.cm.remoteclient.lock.test;

import ru.intertrust.cm.core.business.api.InterserverLockingService;
import ru.intertrust.cm.remoteclient.ClientBase;

public class TestInterserverLockingService extends ClientBase{
    
    private InterserverLockingService lockingService;

    public static void main(String[] args) {
        try {
            TestInterserverLockingService test = new TestInterserverLockingService();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void execute(String[] args) throws Exception {
        try {
            super.execute(args);
            
            // Тест lock unlock
            lockingService = (InterserverLockingService.Remote) getService("InterserverLockingServiceImpl", InterserverLockingService.Remote.class);
            assertFalse("Lock", lockingService.isLocked("xxx"));

            lockingService.lock("xxx");
            assertTrue("Lock", lockingService.isLocked("xxx"));

            lockingService.unlock("xxx");
            assertFalse("Lock", lockingService.isLocked("xxx"));
            
            log("Test OK");
        } finally {
            writeLog();
        }
    }
}
