package ru.intertrust.cm.core.business.api.dto;

import java.util.Map;

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
public class LruLimitedSynchronizedMap<K, V> extends AccessOrderedSynchronizedMap<K, V> {
    private int maxSize = Integer.MAX_VALUE;

    public LruLimitedSynchronizedMap() {
        this(16, 0.75f);
    }

    public LruLimitedSynchronizedMap(int maxSize) {
        this(maxSize, 0.75f);
    }

    public LruLimitedSynchronizedMap(int maxSize, float loadFactor) {
        super((int) (maxSize / loadFactor) + 1, loadFactor);
        this.maxSize = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {
        return size() > maxSize;
    }
}
