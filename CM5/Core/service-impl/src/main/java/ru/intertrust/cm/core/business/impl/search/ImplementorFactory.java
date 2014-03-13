package ru.intertrust.cm.core.business.impl.search;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.core.util.SpringApplicationContext;

public class ImplementorFactory<S, T> {

    protected Logger log = LoggerFactory.getLogger(getClass());

    private HashMap<Class<? extends S>, Class<? extends T>> implementors = new HashMap<>();

    @SuppressWarnings("unchecked")
    public void setImplementors(Map<String, String> implementors) {
        this.implementors = new HashMap<>(implementors.size());
        for (Map.Entry<String, String> entry : implementors.entrySet()) {
            try {
                Class<? extends S> sourceClass = (Class<? extends S>) Class.forName(entry.getKey());
                Class<? extends T> implClass = (Class<? extends T>) Class.forName(entry.getValue());
                this.implementors.put(sourceClass, implClass);
            } catch (ClassNotFoundException e) {
                log.error("Wrong configuration for source " + entry.getKey() + " / implementor " + entry.getValue(), e);
                continue;
            }
        }
    }

    public T createImplementorFor(Class<?> sourceClass) {
        return createImplementorFor(sourceClass, (Object[]) null);
    }

    public T createImplementorFor(Class<?> sourceClass, Object... parameters) {
        if (!implementors.containsKey(sourceClass)) {
            throw new IllegalArgumentException("Class " + sourceClass.getName() + " is not supported");
        }
        Class<? extends T> implClass = implementors.get(sourceClass);
        try {
            if (parameters == null) {
                T implementor = implClass.newInstance();
                SpringApplicationContext.getContext().getAutowireCapableBeanFactory().autowireBean(implementor);
                return implementor;
            }
            Class<?>[] types = new Class[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i] == null) {
                    //throw new IllegalArgumentException("Parameter can't be null - Nr." + (i+1));
                    types[i] = Object.class;
                } else {
                    types[i] = parameters[i].getClass();
                }
            }
            Constructor<? extends T> constructor = implClass.getConstructor(types);
            T implementor = constructor.newInstance(parameters);
            SpringApplicationContext.getContext().getAutowireCapableBeanFactory().autowireBean(implementor);
            return implementor;
        } catch (Exception e) {
            throw new FatalException("Error instantiating implementor for " + sourceClass, e);
        }
    }
}
