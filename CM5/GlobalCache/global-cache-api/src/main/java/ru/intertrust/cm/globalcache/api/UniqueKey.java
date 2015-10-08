package ru.intertrust.cm.globalcache.api;

import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.globalcache.api.util.Size;
import ru.intertrust.cm.globalcache.api.util.SizeEstimator;
import ru.intertrust.cm.globalcache.api.util.Sizeable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Denis Mitavskiy
 *         Date: 21.08.2015
 *         Time: 14:25
 */
public class UniqueKey implements Sizeable {
    private HashMap<String, Value> map;
    private int hash;
    private Size size;

    public UniqueKey() {
    }

    public UniqueKey(Map<String, Value> map) {
        this.map = new HashMap<>(((int) (map.size() / 0.75f) + 1));
        for (String key : map.keySet()) {
            this.map.put(key.toLowerCase(), map.get(key));
        }
        this.size = new Size(SizeEstimator.estimateSize(this.map) + Integer.SIZE + SizeEstimator.getReferenceSize());
    }

    public Map<String, Value> getValues() {
        return map;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

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

    @Override
    public Size getSize() {
        return size;
    }
}
