package ru.intertrust.cm.core.jdbc;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.ejb.client.ContextSelector;
import org.jboss.ejb.client.EJBClientConfiguration;
import org.jboss.ejb.client.EJBClientContext;
import org.jboss.ejb.client.PropertiesBasedEJBClientConfiguration;
import org.jboss.ejb.client.remoting.ConfigBasedEJBClientContextSelector;

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
    private InitialContext ctx;
    private CollectionsService collectionService;
    private ConfigurationService configService;

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
        if (collectionService == null) {
            if (mode == ConnectMode.Remoting) {
                collectionService =
                        (CollectionsService) getRemoteService("CollectionsServiceImpl", host, port, login, password,
                                CollectionsService.Remote.class);
            } else {
                collectionService =
                        (CollectionsService) getLocalService("CollectionsServiceImpl", CollectionsService.class);
            }
        }
        return collectionService;
    }

    public ConfigurationService getConfigService() throws Exception {
        if (configService == null) {
            if (mode == ConnectMode.Remoting) {
                configService =
                        (ConfigurationService) getRemoteService("ConfigurationServiceImpl", host, port, login, password,
                                ConfigurationService.Remote.class);
            } else {
                configService =
                        (ConfigurationService) getLocalService("ConfigurationServiceImpl", ConfigurationService.class);
            }
        }
        return configService;
    }

    private Object getRemoteService(String serviceName, String host, String port, String login, String password,
            Class<?> remoteInterfaceClass) throws Exception {

        if (ctx == null) {
            Properties jndiProps = new Properties();
            /*jndiProps.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");

            jndiProps.put(Context.PROVIDER_URL, "remote://" + address);
            jndiProps.put("jboss.naming.client.ejb.context", "true");
            jndiProps.put("jboss.naming.client.connect.options.org.xnio.Options.SASL_POLICY_NOPLAINTEXT", "false");
            jndiProps.put(Context.SECURITY_PRINCIPAL, login);
            jndiProps.put(Context.SECURITY_CREDENTIALS, password);*/
            
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

            ctx = new InitialContext(jndiProps);
        }

        //Object service = ctx.lookup(appName + "/" + moduleName + "/" + serviceName + "!" + remoteInterfaceClass.getName());
        Object service = ctx.lookup("ejb:" + appName + "/" + moduleName + "//" + serviceName + "!" + remoteInterfaceClass.getName());

        return service;
    }

    private Object getLocalService(String serviceName, Class<?> localInterfaceClass) throws NamingException {
        if (ctx == null) {
            ctx = new InitialContext();
        }
        Object service = ctx.lookup("java:module/" + serviceName + "!" + localInterfaceClass.getName());
        return service;
    }

    public void close() {
        try {
            if (ctx != null){
                ctx.close();
            }
            ctx = null;
            collectionService = null;
            configService = null;
        } catch (Exception ignoreEx) {
            ignoreEx.printStackTrace();
        }
    }
}
