package ru.intertrust.cm.remoteclient.permissions.test;

import ru.intertrust.cm.remoteclient.ClientBase;
import ru.intertrust.cm.test.acess.TestPermissionsServiceInAcyncMode;

public class TestPermissionsServiceInAcyncModeClient  extends ClientBase {
    public static void main(String[] args) {
        try {
            TestPermissionsServiceInAcyncModeClient test = new TestPermissionsServiceInAcyncModeClient();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void execute(String[] args) throws Exception {
        try {
            super.execute(args);
            
            TestPermissionsServiceInAcyncMode service = (TestPermissionsServiceInAcyncMode) getService("TestPermissionsServiceInAcyncMode", TestPermissionsServiceInAcyncMode.Remote.class);
            service.test();

            log("Complete");
        } finally {
            writeLog();
        }
    }    

}
