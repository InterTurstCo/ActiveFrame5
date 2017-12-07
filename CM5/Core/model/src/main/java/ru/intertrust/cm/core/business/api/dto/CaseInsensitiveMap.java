package ru.intertrust.cm.core.business.api.dto;

import ru.intertrust.cm.core.model.GwtIncompatible;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Обертывает @{link HashMap} для хранения ключей объектов в нижнем регистре (регистронезависимо).
 * @author atsvetkov
 *
 */
public class CaseInsensitiveMap<T> implements Dto, Cloneable {

    protected LinkedHashMap<String, T> map = new LinkedHashMap<>();

    public CaseInsensitiveMap() {
    }

    public CaseInsensitiveMap(Map<String, T> map) {
        for (String key : map.keySet()) {
            this.put(key, map.get(key));
        }
    }

    
    public T put(String key, T value) {
        return map.put(getLowerCaseKey(key), value);
    }

    public T get(String key) {
        return map.get(getLowerCaseKey(key));
    }

    public boolean containsKey(String key) {
        return map.containsKey(getLowerCaseKey(key));
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

    public void clear() {
        map.clear();
    }

    @Override
    @GwtIncompatible
    protected Object clone() throws CloneNotSupportedException {
        final CaseInsensitiveMap clone = (CaseInsensitiveMap) super.clone();
        clone.map = new LinkedHashMap(clone.map);
        return clone;
    }

    private String getLowerCaseKey(String key) {
        String lowerCaseKey = null;
        if (key != null) {
            lowerCaseKey = Case.toLower(key);
        }
        return lowerCaseKey;
    }
}
