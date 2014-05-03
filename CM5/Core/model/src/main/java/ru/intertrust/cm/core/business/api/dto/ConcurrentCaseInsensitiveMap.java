package ru.intertrust.cm.core.business.api.dto;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Concurrent implementation of CaseInsensitiveMap
 */
public class ConcurrentCaseInsensitiveMap<T> extends CaseInsensitiveMap<T> {

    protected ConcurrentHashMap<String, T> map = new ConcurrentHashMap<>();

    public void putIfAbsent(String key, T value) {
        map.putIfAbsent(key, value);
    }
}
