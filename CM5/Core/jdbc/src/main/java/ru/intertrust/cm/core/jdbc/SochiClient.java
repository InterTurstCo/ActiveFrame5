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
    private ConnectMode mode;
    private String host;
    private String port;
    private String login;
    private String password;
    private String appName;
    private String moduleName;
    private ThreadLocal<InitialContext> ctx = new ThreadLocal<InitialContext>();
    private ThreadLocal<CollectionsService> collectionService = new ThreadLocal<CollectionsService>();
    private ThreadLocal<ConfigurationService> configService = new ThreadLocal<ConfigurationService>();

    public SochiClient(ConnectMode mode, String host, String port, String login, String password, String appName, String moduleName) {
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

    public ConfigurationService getConfigService() throws Exception {
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
     * Получение remote интерфеса используя JNDI
     * @param serviceName
     * @param host
     * @param port
     * @param login
     * @param password
     * @param remoteInterfaceClass
     * @return
     * @throws Exception
     */
    private Object getRemoteService(String serviceName, String host, String port, String login, String password,
            Class<?> remoteInterfaceClass) throws Exception {

        if (ctx.get() == null) {
            Properties jndiProps = new Properties();
            jndiProps.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");

            jndiProps.put(Context.PROVIDER_URL, "remote://" + host + ":" + port);
            jndiProps.put("jboss.naming.client.ejb.context", "true");
            jndiProps.put("jboss.naming.client.connect.options.org.xnio.Options.SASL_POLICY_NOPLAINTEXT", "false");
            jndiProps.put(Context.SECURITY_PRINCIPAL, login);
            jndiProps.put(Context.SECURITY_CREDENTIALS, password);
            
            ctx.set(new InitialContext(jndiProps));
        }

        Object service = ctx.get().lookup(appName + "/" + moduleName + "/" + serviceName + "!" + remoteInterfaceClass.getName());

        return service;
    }

    /**
     * Получение remote интерфеса используя naming, нежелательно использование так как используется jboss зависимости
     * @param serviceName
     * @param host
     * @param port
     * @param login
     * @param password
     * @param remoteInterfaceClass
     * @return
     * @throws Exception
     */
    /*private Object getRemoteService(String serviceName, String host, String port, String login, String password,
            Class<?> remoteInterfaceClass) throws Exception {

        if (ctx.get() == null) {
            Properties jndiProps = new Properties();
            
            Properties clientProperties = new Properties();
            clientProperties.put("remote.connectionprovider.create.options.org.xnio.Options.SSL_ENABLED", "false");
            clientProperties.put("remote.connections", "default");
            clientProperties.put("remote.connection.default.port", port);
            clientProperties.put("remote.connection.default.host", host);
            clientProperties.put("remote.connection.default.username", login);
            clientProperties.put("remote.connection.default.password", password);
            clientProperties.put("remote.connection.default.connect.options.org.xnio.Options.SASL_POLICY_NOANONYMOUS", "false");
            clientProperties.put("remote.connection.default.connect.options.org.xnio.Options.SASL_POLICY_NOPLAINTEXT", "false");

            EJBClientConfiguration ejbClientConfiguration = new PropertiesBasedEJBClientConfiguration(clientProperties);
            ContextSelector<EJBClientContext> contextSelector = new ConfigBasedEJBClientContextSelector(ejbClientConfiguration);
            EJBClientContext.setSelector(contextSelector);

            jndiProps.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");            

            ctx.set(new InitialContext(jndiProps));
        }

        Object service = ctx.get().lookup("ejb:" + appName + "/" + moduleName + "//" + serviceName + "!" + remoteInterfaceClass.getName());

        return service;
    }*/
    
    
    private Object getLocalService(String serviceName, Class<?> localInterfaceClass) throws NamingException {
        //Не получаеится использовать ThreadLocal ссылку на InitialContext, так как в потоке созданном не jboss не доступны ссылки на ejb
        if (ctx.get() == null) {
            ctx.set(new InitialContext());
        }
        final String envName = Objects.equals(appName, moduleName) ? moduleName : (appName + "/" + moduleName);
        //Нельзя использовать конструкцию java:module/.... так как Jasper создает потоки для подотчетов и lookup выдаст ошибку
        Object service = ctx.get().lookup("java:global/" + envName + "/" + serviceName + "!" + localInterfaceClass.getName());
        return service;
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
