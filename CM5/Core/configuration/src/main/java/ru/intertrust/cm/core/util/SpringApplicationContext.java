package ru.intertrust.cm.core.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import ru.intertrust.cm.core.model.FatalException;

public class SpringApplicationContext implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext ctx) {
        /*if (context != null) {
            throw new FatalException("Repeated set of context");
        }*/
        context = ctx;
    }

    public static ApplicationContext getContext() {
        return context;
    }
}
