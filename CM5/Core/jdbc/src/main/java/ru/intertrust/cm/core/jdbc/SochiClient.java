package ru.intertrust.cm.core.jdbc;

import java.util.Objects;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.jdbc.JdbcDriver.ConnectMode;

public class SochiClient {
    private final ConnectMode mode;
    private final String host;
    private final String port;
    private final String login;
    private final String password;
    private final String appName;
    private final String moduleName;
    private final ThreadLocal<InitialContext> ctx = new ThreadLocal<>();
    private final ThreadLocal<CollectionsService> collectionService = new ThreadLocal<>();
    private final ThreadLocal<ConfigurationService> configService = new ThreadLocal<>();

    SochiClient(ConnectMode mode, String host, String port, String login, String password, String appName, String moduleName) {
        this.mode = mode;
        this.host = host;
        this.port = port;
        this.login = login;
        this.password = password;
        this.appName = appName;
        this.moduleName = moduleName;
    }

    public ConnectMode getMode(){
        return mode;
    }
    
    public CollectionsService getCollectionService() throws Exception {
        if (collectionService.get() == null) {
            if (mode == ConnectMode.Remoting) {
                collectionService.set((CollectionsService) getRemoteService("CollectionsServiceImpl", host, port, login, password,
                                CollectionsService.Remote.class));
            } else {
                collectionService.set((CollectionsService) getLocalService("CollectionsServiceImpl", CollectionsService.class));
            }
        }
        return collectionService.get();
    }

    ConfigurationService getConfigService() throws Exception {
        if (configService.get() == null) {
            if (mode == ConnectMode.Remoting) {
                configService.set((ConfigurationService) getRemoteService("ConfigurationServiceImpl", host, port, login, password,
                                ConfigurationService.Remote.class));
            } else {
                configService.set((ConfigurationService) getLocalService("ConfigurationServiceImpl", ConfigurationService.class));
            }
        }
        return configService.get();
    }

    /**
     * Получение remote интерфейса используя JNDI
     */
    private Object getRemoteService(String serviceName, String host, String port, String login, String password,
            Class<?> remoteInterfaceClass) throws Exception {

        if (ctx.get() == null) {
            Properties jndiProps = new Properties();
            jndiProps.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");

            jndiProps.put(Context.PROVIDER_URL, "remote://" + host + ':' + port);
            jndiProps.put("jboss.naming.client.ejb.context", "true");
            jndiProps.put("jboss.naming.client.connect.options.org.xnio.Options.SASL_POLICY_NOPLAINTEXT", "false");
            jndiProps.put(Context.SECURITY_PRINCIPAL, login);
            jndiProps.put(Context.SECURITY_CREDENTIALS, password);
            
            ctx.set(new InitialContext(jndiProps));
        }
        return ctx.get().lookup(appName + '/' + moduleName + '/' + serviceName + '!' + remoteInterfaceClass.getName());
    }

    private Object getLocalService(String serviceName, Class<?> localInterfaceClass) throws NamingException {
        //Не получится использовать ThreadLocal ссылку на InitialContext, так как в потоке созданном не jboss не доступны ссылки на ejb
        if (ctx.get() == null) {
            ctx.set(new InitialContext());
        }
        final String envName = Objects.equals(appName, moduleName) ? moduleName : (appName + '/' + moduleName);
        //Нельзя использовать конструкцию java:module/.... так как Jasper создает потоки для подотчетов и lookup выдаст ошибку
        return ctx.get().lookup("java:global/" + envName + "/" + serviceName + "!" + localInterfaceClass.getName());
    }

    public void close() {
        try {
            if (ctx.get() != null){
                ctx.get().close();
            }
            ctx.set(null);
            collectionService.set(null);
            configService.set(null);
        } catch (Exception ignoreEx) {
            ignoreEx.printStackTrace();
        }
    }
}
