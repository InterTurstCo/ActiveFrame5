package ru.intertrust.cm.globalcache.api;

import ru.intertrust.cm.core.business.api.dto.Value;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Denis Mitavskiy
 *         Date: 21.08.2015
 *         Time: 14:25
 */
public class UniqueKey {
    protected HashMap<String, Value> map;
    private int hash;

    public UniqueKey() {
    }

    public UniqueKey(Map<String, Value> map) {
        this.map = new HashMap<>(((int) (map.size() / 0.75f) + 1));
        for (Map.Entry<String, Value> entry : map.entrySet()) {
            this.map.put(entry.getKey().toLowerCase(), entry.getValue());
        }
    }

    public Map<String, Value> getValues() {
        return map;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof UniqueKey)) return false;

        UniqueKey uniqueKey = (UniqueKey) o;

        if (!map.equals(uniqueKey.map)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = map.hashCode();
        }
        return hash;
    }

    @Override
    public String toString() {
        return map.toString();
    }
}
