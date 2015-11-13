package ru.intertrust.cm.core.business.impl;

import java.util.HashMap;
import java.util.Map;

import ru.intertrust.cm.core.business.api.ThreadContext;

/**
 * Бин доступен в контексте потока (scope="thread"). Все переменные члены классы
 * глобальны в рамках потока
 * @author larin
 * 
 */
public class ThreadContextImpl implements ThreadContext {
    private ThreadLocal<Map<String, Object>> contextData = new ThreadLocal<Map<String, Object>>();

    @Override
    public Object get(String key) {
        if (this.contextData.get() == null){
            this.contextData.set(new HashMap<String, Object>());
        }
        return this.contextData.get().get(key);
    }

    @Override
    public void set(String key, Object value) {
        if (this.contextData.get() == null){
            this.contextData.set(new HashMap<String, Object>());
        }
        this.contextData.get().put(key, value);
    }
}
