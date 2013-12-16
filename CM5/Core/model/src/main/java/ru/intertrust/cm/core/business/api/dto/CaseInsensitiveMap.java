package ru.intertrust.cm.core.business.api.dto;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Обертывает @{link HashMap} для хранения ключей объектов в нижнем регистре (регистронезависимо).
 * @author atsvetkov
 *
 */
public class CaseInsensitiveMap<T> implements Dto {

    private Map<String, T> map;

    public CaseInsensitiveMap() {
        map = new LinkedHashMap<>();
    }

    public CaseInsensitiveMap(int size) {
        map = new LinkedHashMap<>(size);
    }

    public T put(String key, T value) {
        return map.put(getLowerCaseKey(key), value);
    }

    public T get(String key) {
        return map.get(getLowerCaseKey(key));
    }

    public T remove(String key) {
        return map.remove(getLowerCaseKey(key));
    }

    public Collection<T> values() {
        return map.values();
    }

    public Set<String> keySet() {
        return map.keySet();
    }

    private String getLowerCaseKey(String key) {
        String lowerCaseKey = null;
        if (key != null) {
            lowerCaseKey = key.toLowerCase();
        }
        return lowerCaseKey;
    }

}
