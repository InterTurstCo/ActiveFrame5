package ru.intertrust.cm.remoteclient.stres;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.ejb.client.ContextSelector;
import org.jboss.ejb.client.EJBClientConfiguration;
import org.jboss.ejb.client.EJBClientContext;
import org.jboss.ejb.client.PropertiesBasedEJBClientConfiguration;
import org.jboss.ejb.client.remoting.ConfigBasedEJBClientContextSelector;

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

            for (int i = 0; i < THREAD_COUNT; i++) {
                RunnableThread runnableThread = new RunnableThread(String.valueOf(i));
                                
                Thread thread = new Thread(runnableThread);
                thread.start();
                threads.add(thread);
                runnable.add(runnableThread);
            }
            
            for (RunnableThread runnableThread : runnable) {
                if (runnableThread.isError()){
                    errorThreads ++;
                }else{
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
        private boolean error;

        public RunnableThread(String treadName) {
            this.treadName = treadName;
        }

        public boolean isError(){
            return error;
        }
        
        public void run() {
            try {
                CrudService.Remote crudService = (CrudService.Remote) getServiceLocal(
                        "CrudServiceImpl", CrudService.Remote.class);
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
            }
            try{
                ctx.close();
            }catch(Exception ex){
                
            }
        }

        private Object getServiceLocal(String serviceName, Class remoteInterfaceClass) throws NamingException {
            if (ctx == null) {
                Properties jndiProps = new Properties();

                
                Properties clientProperties = new Properties();
                clientProperties.put("remote.connectionprovider.create.options.org.xnio.Options.SSL_ENABLED", "false");
                clientProperties.put("remote.connections", "default");
                clientProperties.put("remote.connection.default.port", address.split(":")[1]);
                clientProperties.put("remote.connection.default.host", address.split(":")[0]);
                clientProperties.put("remote.connection.default.username", user);
                clientProperties.put("remote.connection.default.password", password);
                clientProperties.put("remote.connection.default.connect.options.org.xnio.Options.SASL_POLICY_NOANONYMOUS", "false");
                clientProperties.put("remote.connection.default.connect.options.org.xnio.Options.SASL_POLICY_NOPLAINTEXT", "false");

                EJBClientConfiguration ejbClientConfiguration = new PropertiesBasedEJBClientConfiguration(clientProperties);
                ContextSelector<EJBClientContext> contextSelector = new ConfigBasedEJBClientContextSelector(ejbClientConfiguration);
                EJBClientContext.setSelector(contextSelector);

                jndiProps.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
                
                
                ctx = new InitialContext(jndiProps);
            }

            Object service = ctx.lookup("ejb:" + getAppName() + "/" + getModuleName() + "//" + serviceName + "!" + remoteInterfaceClass.getName());
            return service;
        }
    }
}
