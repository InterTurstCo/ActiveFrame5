package ru.intertrust.cm.core.gui.impl.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

public enum JeeServerFamily {

    JBOSS,
    TOMEE,
    UNKNOWN,
    WILDFLY
    ;

    // Suppose that the server's family can't change while application runs :)
    private static JeeServerFamily cached = null;

    public static JeeServerFamily determine(ServletContext context) {
        if (cached == null) {
            String info = context.getServerInfo();
            LoggerFactory.getLogger(JeeServerFamily.class).info(info);
            if (info.contains("JBoss")) {
                cached = JBOSS;
            } else if (info.contains("TomEE")) {
                cached = TOMEE;
            } else if (info.contains("WildFly")) {
                cached = WILDFLY;
            } else {
                cached = UNKNOWN;
            }

            Logger logger = LoggerFactory.getLogger(JeeServerFamily.class);
            ClassLoader loader = JeeServerFamily.class.getClassLoader();
            while (loader != null) {
                logger.info(loader.getClass().getName() + ": " + loader.toString());
                loader = loader.getParent();
            }
        }
        return cached;
    }

    public static boolean isLogoutRequired(HttpServletRequest request) {
        return request.getUserPrincipal() != null && determine(request.getServletContext()).equals(JeeServerFamily.JBOSS);
    }
}
