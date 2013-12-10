package ru.intertrust.cm.core.jdbc;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.jdbc.JdbcDriver.ConnectMode;

public class SochiClient {
    private ConnectMode mode;
    private String address;
    private String login;
    private String password;
    private InitialContext ctx;
    private CollectionsService collectionService;
    private ConfigurationService configService;

    public SochiClient(ConnectMode mode, String address, String login, String password) {
        this.mode = mode;
        this.address = address;
        this.login = login;
        this.password = password;
    }

    public CollectionsService getCollectionService() throws Exception {
        if (collectionService == null) {
            if (mode == ConnectMode.Remoting) {
                collectionService =
                        (CollectionsService) getRemoteService("CollectionsServiceImpl", address, login, password,
                                CollectionsService.Remote.class);
            } else {
                collectionService =
                        (CollectionsService) getLocalService("CollectionsServiceImpl", CollectionsService.Remote.class);
            }
        }
        return collectionService;
    }

    public ConfigurationService getConfigService() throws Exception {
        if (configService == null) {
            if (mode == ConnectMode.Remoting) {
                configService =
                        (ConfigurationService) getRemoteService("ConfigurationServiceImpl", address, login, password,
                                ConfigurationService.Remote.class);
            } else {
                configService =
                        (ConfigurationService) getLocalService("ConfigurationServiceImpl",
                                ConfigurationService.Remote.class);
            }
        }
        return configService;
    }

    private Object getRemoteService(String serviceName, String address, String login, String password,
            Class<?> remoteInterfaceClass) throws Exception {

        if (ctx == null) {
            Properties jndiProps = new Properties();
            jndiProps.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");

            jndiProps.put(Context.PROVIDER_URL, "remote://" + address);
            jndiProps.put("jboss.naming.client.ejb.context", "true");
            //jndiProps.put("org.jboss.ejb.client.scoped.context","true");
            jndiProps.put("jboss.naming.client.connect.options.org.xnio.Options.SASL_POLICY_NOPLAINTEXT", "false");
            jndiProps.put("remote.connectionprovider.create.options.org.xnio.Options.SSL_ENABLED", "false");
            jndiProps.put(Context.SECURITY_PRINCIPAL, login);
            jndiProps.put(Context.SECURITY_CREDENTIALS, password);
            jndiProps.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");

            ctx = new InitialContext(jndiProps);
        }

        Object service = ctx.lookup("ejb:cm-sochi/web-app//" + serviceName + "!" + remoteInterfaceClass.getName());

        return service;
    }

    private Object getLocalService(String serviceName, Class<?> remoteInterfaceClass) throws NamingException {
        if (ctx == null) {
            ctx = new InitialContext();
        }

        Object service = ctx.lookup("ejb:cm-sochi/web-app//" + serviceName + "!" + remoteInterfaceClass.getName());

        return service;
    }

    public void close() {
        try {
            ctx.close();
            collectionService = null;
            configService = null;
        } catch (Exception ignoreEx) {
            ignoreEx.printStackTrace();
        }
    }

}
