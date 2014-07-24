package ru.intertrust.testmodule.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Предоставляет доступ к Spring {@link WebApplicationContext}. {@link WebApplicationContext} инициализируется при
 * загрузке приложения и кешируется для дальнейшего доступа к Spring бинам.
 * @author atsvetkov
 */
public class ApplicationContextProvider implements ApplicationContextAware {
    
    private static ApplicationContext ctx = null;

    public static ApplicationContext getApplicationContext() {
        return ctx;
    }

    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        this.ctx = ctx;
    }
}
