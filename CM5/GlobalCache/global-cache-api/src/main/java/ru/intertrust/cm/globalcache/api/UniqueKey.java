package ru.intertrust.cm.globalcache.api;

import ru.intertrust.cm.core.business.api.dto.Case;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.UniqueKeyConfig;
import ru.intertrust.cm.core.config.UniqueKeyFieldConfig;

import java.io.Serializable;
import java.util.*;

/**
 * @author Denis Mitavskiy
 *         Date: 21.08.2015
 *         Time: 14:25
 */
public class UniqueKey {
    protected Map<String, Value> map;
    private int hash;

    public UniqueKey() {
    }

    public UniqueKey(Map<String, Value> map) {
        final int size = map.size();
        switch (size) {
            case 2:
                final Iterator<Map.Entry<String, Value>> iter = map.entrySet().iterator();
                final Map.Entry<String, Value> e1 = iter.next();
                final Map.Entry<String, Value> e2 = iter.next();
                this.map = new PairMap<>(Case.toLower(e1.getKey()), e1.getValue(), Case.toLower(e2.getKey()), e2.getValue());
                break;
            case 1:
                final Map.Entry<String, Value> entry = map.entrySet().iterator().next();
                this.map = Collections.singletonMap(Case.toLower(entry.getKey()), entry.getValue());
                break;
            case 0:
                throw new IllegalArgumentException("Empty unique key map!");
                //this.map = Collections.emptyMap();
                //break;
            default:
                this.map = new HashMap<>(((int) (size / 0.75f) + 1));
                for (Map.Entry<String, Value> curEntry : map.entrySet()) {
                    this.map.put(Case.toLower(curEntry.getKey()), curEntry.getValue());
                }
        }
    }

    public UniqueKey(UniqueKeyConfig uniqueKeyConfig, DomainObject object) {
        final List<UniqueKeyFieldConfig> fields = uniqueKeyConfig.getUniqueKeyFieldConfigs();
        final int size = fields.size();
        switch (size) { // 0 is impossible
            case 2:
                UniqueKeyFieldConfig f1 = fields.get(0);
                UniqueKeyFieldConfig f2 = fields.get(1);
                this.map = new PairMap<>(Case.toLower(f1.getName()), object.getValue(f1.getName()), Case.toLower(f2.getName()), object.getValue(f2.getName()));
                break;
            case 1:
                UniqueKeyFieldConfig field = fields.get(0);
                this.map = Collections.singletonMap(Case.toLower(field.getName()), object.getValue(field.getName()));
                break;
            case 0:
                throw new IllegalArgumentException("Empty unique key map!");
                //this.map = Collections.emptyMap();
                //break;
            default:
                this.map = new HashMap<>(((int) (size / 0.75f) + 1));
                for (final UniqueKeyFieldConfig curField : fields) {
                    this.map.put(Case.toLower(curField.getName()), object.getValue(curField.getName()));
                }
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

    private static class PairMap<K, V> extends AbstractMap<K, V> implements Serializable {
        private final K k1;
        private final K k2;
        private final V v1;
        private final V v2;
        private transient Set<K> keySet = null;
        private transient Set<Map.Entry<K, V>> entrySet = null;
        private transient Collection<V> values = null;

        PairMap(K key1, V value1, K key2, V value2) {
            k1 = key1;
            k2 = key2;
            v1 = value1;
            v2 = value2;
        }

        public int size() {
            return 2;
        }

        public boolean isEmpty() {
            return false;
        }

        public boolean containsKey(Object key) {
            return Objects.equals(key, k1) || Objects.equals(key, k2);
        }

        public boolean containsValue(Object value) {
            return Objects.equals(value, v1) || Objects.equals(value, v2);
        }

        public V get(Object key) {
            if (Objects.equals(key, k1)) {
                return v1;
            } else if (Objects.equals(key, k2)) {
                return v2;
            } else {
                return null;
            }
        }

        public Set<K> keySet() {
            if (keySet == null) {
                keySet = new HashSet<>();
                keySet.add(k1);
                keySet.add(k2);
                keySet = Collections.unmodifiableSet(keySet);
            }
            return keySet;
        }

        public Set<Map.Entry<K, V>> entrySet() {
            if (entrySet == null) {
                entrySet = new HashSet<>();
                entrySet.add(new SimpleImmutableEntry<K, V>(k1, v1));
                entrySet.add(new SimpleImmutableEntry<K, V>(k2, v2));
                entrySet = Collections.unmodifiableSet(entrySet);
            }
            return entrySet;
        }

        public Collection<V> values() {
            if (values == null) {
                ArrayList<V> values = new ArrayList<V>(2);
                values.add(v1);
                values.add(v2);
                this.values = Collections.unmodifiableList(values);
            }
            return values;
        }

    }
}
