package ru.intertrust.cm.core.business.api.dto;

import java.util.*;

/**
 * Быстрая карта с поддержкой LRU-механизма, за основу использующая синхронизированный LinkedHashMap.
 * Абсолютное время выполнения в однопоточном режиме:
 * <table border="1">
 *     <tr>
 *         <th>Объём,<br/>мкс</th>
 *         <th>Метод,<br/>мкс</th>
 *         <th>LinkedHashMap,<br/>мкс</th>
 *         <th>BoundedConcurrentHashMap<br/>(класс из библиотеки Infinispan),<br/>мкс</th>
 *         <th>ConcurrentHashMap<br/>(для сравнения, хоть оно и не корректное),<br/>мкс</th>
 *     </tr>
 *     <tr>
 *         <td rowspan=2>1000000</td>
 *         <td>put</td>
 *         <td>0.18</td>
 *         <td>0.42</td>
 *         <td>0.19</td>
 *     </tr>
 *     <tr>
 *         <td>get</td>
 *         <td>0.17</td>
 *         <td>0.31</td>
 *         <td>0.12</td>
 *     </tr>
 *     <tr>
 *         <td rowspan=2>10000000</td>
 *         <td>get</td>
 *         <td>0.17</td>
 *         <td>0.41</td>
 *         <td>0.21</td>
 *     </tr>
 *     <tr>
 *         <td>get</td>
 *         <td>0.17</td>
 *         <td>0.40</td>
 *         <td>0.14</td>
 *     </tr>
 * </table>
 *
 * Таким образом, данная реализация обеспечит ~5900000 операций над кэшем в секунду (не зависимо от количества ядер). Реализация, основанная на
 * BoundedConcurrentHashMap обеспечивает ~2500000 операций в секунду на одно ядро. На неё можно будет переходить, если 5900000 операций в секунду будет не хватать.
 * При этом производительность одного потока замедлится.
 *
 * @author Denis Mitavskiy
 *         Date: 07.07.2015
 *         Time: 15:54
 */
public class AccessOrderedSynchronizedMap<K, V> extends LinkedHashMap<K, V> {
    public AccessOrderedSynchronizedMap() {
        super();
    }

    public AccessOrderedSynchronizedMap(int initialCapacity) {
        super(initialCapacity, 0.75f, true);
    }

    public AccessOrderedSynchronizedMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor, true);
    }

    public AccessOrderedSynchronizedMap(Map<? extends K, ? extends V> m) {
        super(m);
    }

    @Override
    public synchronized boolean containsValue(Object value) {
        return super.containsValue(value);
    }

    @Override
    public synchronized V get(Object key) {
        return super.get(key);
    }

    @Override
    public synchronized void clear() {
        super.clear();
    }

    @Override
    public synchronized int size() {
        return super.size();
    }

    @Override
    public synchronized boolean isEmpty() {
        return super.isEmpty();
    }

    @Override
    public synchronized boolean containsKey(Object key) {
        return super.containsKey(key);
    }

    @Override
    public synchronized V put(K key, V value) {
        return super.put(key, value);
    }

    @Override
    public synchronized void putAll(Map<? extends K, ? extends V> m) {
        super.putAll(m);
    }

    @Override
    public synchronized V remove(Object key) {
        return super.remove(key);
    }

    @Override
    public synchronized Object clone() {
        return new AccessOrderedSynchronizedMap(this);
    }

    @Override
    public synchronized Set<K> keySet() {
        return new LinkedHashSet<>(super.keySet());
    }

    @Override
    public synchronized Collection<V> values() {
        return super.values();
    }

    @Override
    public synchronized Set<Map.Entry<K, V>> entrySet() {
        return new LinkedHashSet<>(super.entrySet());
    }

    @Override
    public synchronized boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public synchronized int hashCode() {
        return super.hashCode();
    }

    @Override
    public synchronized String toString() {
        return super.toString();
    }
}