package ru.intertrust.cm.globalcache.impl.util;

import ru.intertrust.cm.core.dao.access.UserSubject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Denis Mitavskiy
 *         Date: 26.08.2015
 *         Time: 19:37
 */
public class SizeableConcurrentHashMap<K, V extends Sizeable> extends ConcurrentHashMap<K, V> implements Sizeable {
    private final static int SELF_SIZE = 4 * Integer.SIZE + Float.SIZE + (int) SizeEstimator.getReferenceSize();
    public static final long USER_SUBJECT_SIZE = SizeEstimator.estimateSize(new UserSubject(1));

    private int modifications; // переменная нарочно не volatile, и доступ к ней не синхронизирован - это не важно
    private int approximateEntriesQty; // и эта тоже

    private int concurrencyLevel;
    private int capacity;
    private float loadFactor;

    private Size size;
    private Size selfSize;

    public SizeableConcurrentHashMap() {
        this(16, 0.75f, 16, null);
    }

    public SizeableConcurrentHashMap(Size totals) {
        this(16, 0.75f, 16, totals);
    }

    public SizeableConcurrentHashMap(int initialCapacity, float loadFactor, int concurrencyLevel, Size total) {
        super(initialCapacity, loadFactor, concurrencyLevel);
        this.concurrencyLevel = concurrencyLevel;
        this.capacity = initialCapacity;
        this.loadFactor = loadFactor;
        this.size = new Size(total);
        this.selfSize = new Size();
        updateSelfSize();
    }

    @Override
    public V remove(Object key) {
        --approximateEntriesQty;
        ++modifications;
        updateSelfSize();
        final V removed = super.remove(key);
        updateSizeOnRemove((K) key, removed);
        return removed;
    }

    @Override
    public boolean remove(Object key, Object value) {
        --approximateEntriesQty;
        ++modifications;
        updateSelfSize();
        final boolean removed = super.remove(key, value);
        if (removed) {
            updateSizeOnRemove((K) key, (V) value);
        }
        return removed;
    }

    @Override
    public V putIfAbsent(K key, V value) {
        incrementSize(1);
        ++modifications;
        updateSelfSize();
        final V result = super.putIfAbsent(key, value);
        if (result == null) {
            updateSizeOnPut(key, null, value);
        }
        return result;
    }

    @Override
    public V put(K key, V value) {
        incrementSize(1);
        ++modifications;
        updateSelfSize();
        final V previousValue = super.put(key, value);
        updateSizeOnPut(key, previousValue, value);
        return previousValue;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException("Not supported yet");
        /*incrementSize(m.size());
        ++modifications;
        updateSelfSize();
        super.putAll(m);*/
    }

    private void incrementSize(int increment) {
        int size = approximateEntriesQty;
        size += increment;
        if (size > capacity) {
            capacity = size;
        }
        approximateEntriesQty = size;
    }

    @Override
    public void clear() {
        super.clear();
        for (int i = 0; i < 5; ++i) { // to be sure other threads do not overwrite
            modifications = -1;
            approximateEntriesQty = 0;
            Thread.yield();
        }
    }

    private void updateSizeOnPut(K key, V prevValue, V newValue) {
        if (key instanceof Sizeable) {
            ((Sizeable) key).getSize().setTotal(this.size);
        } else if (key instanceof UserSubject) {
            this.size.add(USER_SUBJECT_SIZE);
        } else {
            this.size.add(SizeEstimator.estimateSize(key));
        }
        if (prevValue != null) {
            prevValue.getSize().detachFromTotal();
        }
        newValue.getSize().setTotal(this.size);
    }

    private void updateSizeOnRemove(K key, V value) {
        if (value == null) { // nothing removed
            return;
        }
        if (key instanceof Sizeable) {
            ((Sizeable) key).getSize().detachFromTotal();
        } else if (key instanceof UserSubject) {
            this.size.add(-USER_SUBJECT_SIZE);
        } else {
            this.size.add(-SizeEstimator.estimateSize(key));
        }
        value.getSize().detachFromTotal();
    }

    private void updateSelfSize() {
        if (modifications % 200 == 0) {
            final long before = this.selfSize.get();
            final long now = getSelfSizeEstimation();
            this.selfSize.set(now);
            size.add(now - before);
        }
    }

    private long getSelfSizeEstimation() {
        if (modifications > 1000 || modifications < 0) {
            modifications = 0;
            approximateEntriesQty = size();
        }
        int size = approximateEntriesQty;
        int maxCapacity = size > capacity ? (int) (size / loadFactor) : (int) (capacity / loadFactor);
        return estimateConcurrentHashMap(maxCapacity, concurrencyLevel, size) + SELF_SIZE;
    }

    private static int estimateConcurrentHashMap(int maxPossibleCapacity, int concurrencyLevel, int size) {
        if (size > maxPossibleCapacity) {
            maxPossibleCapacity = size;
        }
        final int ref = (int) SizeEstimator.getReferenceSize();
        final int a = (4 * ref + 8) * size + (9 * ref + 40) * concurrencyLevel + 6 * ref + 36;
        if (maxPossibleCapacity <= 1) {
            return a;
        }
        final int b = ref / 4 + 2;
        int approx = a + (int) Math.pow(2, intLog2(maxPossibleCapacity - 1) + b);
        return approx;
    }

    private static int intLog2(int x) {
        if (x == 0) {
            return 0;
        }
        return 31 - Integer.numberOfLeadingZeros(x);
    }

    @Override
    public void setSizeTotal(Size total) {
        this.size.setTotal(total);
    }

    @Override
    public Size getSize() {
        return this.size;
    }
}
