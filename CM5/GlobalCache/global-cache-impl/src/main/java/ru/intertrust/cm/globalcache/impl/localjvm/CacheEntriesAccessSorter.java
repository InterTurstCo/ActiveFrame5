package ru.intertrust.cm.globalcache.impl.localjvm;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.Pair;
import ru.intertrust.cm.globalcache.api.util.Size;
import ru.intertrust.cm.globalcache.api.util.SizeEstimator;
import ru.intertrust.cm.globalcache.api.util.Sizeable;
import ru.intertrust.cm.globalcache.api.util.SizeableConcurrentHashMap;

import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * @author Denis Mitavskiy
 *         Date: 13.10.2015
 *         Time: 15:50
 */
public class CacheEntriesAccessSorter implements Sizeable {
    private Size size;
    private int maxAccessOrderElts = 0;
    private LinkedHashMap<Object, Object> accessOrder;

    public CacheEntriesAccessSorter(int initialSize, Size totalCacheSize) {
        size = new Size(totalCacheSize);
        accessOrder = new LinkedHashMap<>(initialSize, 0.75f, true);
        updateSizeOnAdd(null);
    }

    public synchronized void logAccess(Object key) {
        if (accessOrder.get(key) == null) { // contains doesn't change order of the map, use get
            final Object prev = accessOrder.put(key, key);
            if (prev != null) {
                updateSizeOnAdd(key);
            }
        }
    }

    public synchronized Object getEldest() {
        final Iterator<Object> iterator = accessOrder.keySet().iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }

    public synchronized void remove(Object key) {
        final Object removed = accessOrder.remove(key);
        if (removed != null) {
            updateSizeOnRemove(key);
        }
    }

    @Override
    public Size getSize() {
        return size;
    }

    private void updateSizeOnAdd(Object newKey) {
        final int eltsQty = accessOrder.size();
        if (eltsQty <= maxAccessOrderElts) {
            return;
        }
        maxAccessOrderElts = eltsQty;
        int size = (int) Math.pow(2, SizeableConcurrentHashMap.intLog2(maxAccessOrderElts - 1) + 1);
        if (newKey != null && !(newKey instanceof Id)) { // extra-charge for it
            size += SizeEstimator.REFERENCE_SIZE;
        }
        this.size.set(size);
    }

    private void updateSizeOnRemove(Object newKey) {
        if (newKey instanceof Pair) {
            this.size.add(-SizeEstimator.REFERENCE_SIZE);
        }
    }

    public static void main(String[] args) {
        final int size = 1000000;
        final CacheEntriesAccessSorter m = new CacheEntriesAccessSorter(100000, null);
        for (int i = 1; i < size; ++i) {
            m.logAccess(i);
        }
        m.logAccess(1);
        System.out.println(m.getEldest());
    }
}
