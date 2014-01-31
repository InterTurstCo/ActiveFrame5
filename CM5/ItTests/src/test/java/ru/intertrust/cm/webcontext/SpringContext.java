package ru.intertrust.cm.webcontext;

import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

//import ru.intertrust.cm_sochi.srv.connector.api.security.ConnectorSession;

public class SpringContext {
    private static final String SECURITY_DOMAIN = "CM5";
    private static ApplicationContext ctx;
    private static ThreadLocal<Boolean> tlsIsLoginCtxInit = new ThreadLocal<>();

    public static void setContext(ApplicationContext context) {
        ctx = context;
    }

    public static Object getBean(String name) {
        return getCtx().getBean(name);
    }

    public static <T> T getBean(Class<T> requiredType) throws BeansException {
        return getCtx().getBean(requiredType);
    }

    public static <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        return getCtx().getBean(name, requiredType);
    }

    public static Object getBean(String name, Object... args) throws BeansException {
        return getCtx().getBean(name, args);
    }

    private static ApplicationContext getCtx() {
        if (ctx == null) {
            throw new IllegalStateException("ApplicationContext is not set");
        }

        Boolean isCtcInit = tlsIsLoginCtxInit.get();
        if (isCtcInit == null) {
            // Аутентификация для Sochi-Platform средствами Sochi-Server
/*            if (ctx.containsBean("connectorSession")) {
                ConnectorSession connectorSession = ctx.getBean("connectorSession", ConnectorSession.class);
                connectorSession.makeSystemLoginContext();
            } else {
                // Аутентификация для Sochi-Platform для конфигурации без Sochi-Server
                makeSystemLoginContext();
            }
*/            makeSystemLoginContext();
            tlsIsLoginCtxInit.set(true);
        }

        return ctx;
    }

    private static void makeSystemLoginContext() {
        CallbackHandler cbh = new CallbackHandler() {
            @Override
            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                for (int i = 0; i < callbacks.length; i++) {
                    if (callbacks[i] instanceof NameCallback) {
                        NameCallback nc = (NameCallback) callbacks[i];
                        nc.setName("admin");

                    } else if (callbacks[i] instanceof PasswordCallback) {
                        PasswordCallback pc = (PasswordCallback) callbacks[i];
                        pc.setPassword("admin".toCharArray());

                    } else {
                        throw new UnsupportedCallbackException
                                (callbacks[i], "Unrecognized Callback");
                    }
                }
            }
        };

        try {
            LoginContext lc = new LoginContext(SECURITY_DOMAIN, cbh);
            lc.login();
        } catch (LoginException e) {
            throw new RuntimeException("System login is failed", e);
        }
    }
}
