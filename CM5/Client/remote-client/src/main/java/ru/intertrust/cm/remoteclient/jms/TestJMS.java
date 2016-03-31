package ru.intertrust.cm.remoteclient.jms;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.intertrust.cm.remoteclient.ClientBase;
import ru.intertrust.cm.test.jms.TestService;

public class TestJMS extends ClientBase {
    public static void main(String[] args) {
        try {
            TestJMS test = new TestJMS();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void execute(String[] args) throws Exception {
        try {
            super.execute(args);

            final TestService service = (TestService) getService("TestService", TestService.Remote.class);
            service.cleanMessageCounter();
            long start = System.currentTimeMillis();
            log(new Date().toString() + " Tset start");
            ExecutorService pool = Executors.newCachedThreadPool();            
            for (int i = 0; i < 10000; i++) {
                pool.submit(new Runnable() {
                    public void run() {
                        try {                            
                            service.sendMessageToTopic(0);
                        } catch (Exception ex) {
                            throw new RuntimeException("Error in run method", ex);
                        }
                    }
                });
            }            

            long time = System.currentTimeMillis() - start;
            log(new Date().toString() + " Tset OK at " + time + " ms.");
        } finally {
            writeLog();
        }
    }

}
