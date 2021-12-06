package ru.intertrust.cm.remoteclient.stres;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.remoteclient.ClientBase;

public class TestManyThreadsSaveLock extends ClientBase {
    private static int THREAD_COUNT = 100;
    private static int OBJECT_IN_THREAD_COUNT = 10;

    public static void main(String[] args) {
        try {
            TestManyThreadsSaveLock test = new TestManyThreadsSaveLock();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void execute(String[] args) throws Exception {
        int errorThreads = 0;
        int okThreads = 0;
        
        try {
            super.execute(args);

            List<Thread> threads = new ArrayList<Thread>();
            List<RunnableThread> runnable = new ArrayList<RunnableThread>();

            CountDownLatch countDownLatch = new CountDownLatch(THREAD_COUNT);
            for (int i = 0; i < THREAD_COUNT; i++) {
                RunnableThread runnableThread = new RunnableThread(countDownLatch, String.valueOf(i));
                                
                Thread thread = new Thread(runnableThread);
                thread.start();
                threads.add(thread);
                runnable.add(runnableThread);
            }

            countDownLatch.await();

            for (RunnableThread runnableThread : runnable) {
                if (runnableThread.isError()){
                    errorThreads ++;
                } else {
                    okThreads ++ ;
                }
            }

        } finally {
            writeLog();
            System.out.println("Ok count = " + okThreads);
            System.out.println("Error count = " + errorThreads);
        }
    }

    private class RunnableThread implements Runnable {
        private String treadName;
        private InitialContext ctx;
        private volatile boolean error;
        private CountDownLatch countDownLatch;

        public RunnableThread(CountDownLatch countDownLatch, String treadName) {
            this.treadName = treadName;
            this.countDownLatch = countDownLatch;
        }

        public boolean isError(){
            return error;
        }
        
        public void run() {

            try {
                CrudService.Remote crudService = (CrudService.Remote) getServiceLocal(
                        "CrudServiceImpl", CrudService.Remote.class, "person8", "admin");
                for (int i = 0; i < OBJECT_IN_THREAD_COUNT; i++) {
                    System.out.println("[Thread " + treadName + "]Start create object " + i);

                    DomainObject attachmentNotInProcess = crudService
                            .createDomainObject("test_type_14");
                    attachmentNotInProcess.setString("name", "name-" + System.currentTimeMillis());
                    attachmentNotInProcess = crudService.save(attachmentNotInProcess);

                    System.out.println("[Thread " + treadName + "]End create object " + i);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                error = true;
            } finally {
                try {
                    ctx.close();
                } catch (NamingException e) {
                    e.printStackTrace();
                }
                countDownLatch.countDown();
            }
        }

        private Object getServiceLocal(String serviceName, Class remoteInterfaceClass, String login, String psswd) throws NamingException {
            if (ctx == null) {
                Properties jndiProperties = getJndiProperties(login, password);
                ctx = new InitialContext(jndiProperties);
            }

            Object service = ctx.lookup("ejb:" + getAppName() + "/" + getModuleName() + "//" + serviceName + "!" + remoteInterfaceClass.getName());
            return service;
        }
    }
}
