package ru.intertrust.cm.remoteclient.stres;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.remoteclient.ClientBase;

public class TestManyThreads extends ClientBase {
    private CrudService.Remote crudService;
    

    public static void main(String[] args) {
        try {
            TestManyThreads test = new TestManyThreads();
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

            for (int i = 0; i < 50; i++) {
                RunnableThread runnableThread = new RunnableThread(String.valueOf(i));
                                
                Thread thread = new Thread(runnableThread);
                thread.start();
                threads.add(thread);
                runnable.add(runnableThread);
            }

            for (Thread thread : threads) {
                thread.join();
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
                for (int i = 0; i < 2; i++) {
                    System.out.println("[Thread " + treadName + "]Start create object " + i);

                    DomainObject attachmentNotInProcess = crudService
                            .createDomainObject("test_process_attachment");
                    attachmentNotInProcess.setString("test_text", "Создание.");
                    attachmentNotInProcess.setLong("test_long", 10L);
                    attachmentNotInProcess.setDecimal("test_decimal",
                            new BigDecimal(10));
                    attachmentNotInProcess.setTimestamp("test_date", new Date());
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

                String address = getParamerer("address");
                String user = getParamerer("user");
                String password = getParamerer("password");

                Properties jndiProps = new Properties();
                jndiProps.put("endpoint.name",
                        "endpoint.name." + treadName);
                
                jndiProps.put(Context.INITIAL_CONTEXT_FACTORY,
                        "org.jboss.naming.remote.client.InitialContextFactory");
                jndiProps.put(Context.PROVIDER_URL, "remote://" + address);
                jndiProps.put("jboss.naming.client.ejb.context", "true");
                jndiProps
                        .put("jboss.naming.client.connect.options.org.xnio.Options.SASL_POLICY_NOPLAINTEXT",
                                "false");
                jndiProps.put(Context.SECURITY_PRINCIPAL, user);
                jndiProps.put(Context.SECURITY_CREDENTIALS, password);
                jndiProps.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
                //jndiProps.put("jboss.naming.client.connect.options.org.jboss.remoting3.RemotingOptions.MAX_OUTBOUND_CHANNELS", "100");
                //jndiProps.put("jboss.naming.client.connect.options.org.jboss.remoting3.RemotingOptions.MAX_INBOUND_CHANNELS", "100");
                 

                ctx = new InitialContext(jndiProps);
            }

            Object service = ctx.lookup("ejb:cm-sochi/web-app//" + serviceName + "!" + remoteInterfaceClass.getName());
            return service;

        }
    }
}
