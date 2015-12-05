package ru.intertrust.cm.globalcache.api.util;

import ru.intertrust.cm.core.dao.access.UserSubject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Denis Mitavskiy
 *         Date: 26.08.2015
 *         Time: 19:37
 */
public class SizeableConcurrentHashMap<K, V> extends ConcurrentHashMap<K, V> implements Sizeable {
    private final static int SELF_SIZE = 4 * Integer.SIZE + Float.SIZE + (int) SizeEstimator.REFERENCE_SIZE;
    public static final long USER_SUBJECT_SIZE = SizeEstimator.estimateSize(new UserSubject(1));

    private int modifications; // переменная нарочно не volatile, и доступ к ней не синхронизирован - это не важно
    private int approximateEntriesQty; // и эта тоже

    private int concurrencyLevel;
    private int capacity;
    private float loadFactor;
    private boolean includeKeySizes;
    private boolean includeValueSizes;

    private Size size;
    private Size selfSize;

    public SizeableConcurrentHashMap() {
        this(16, 0.75f, 16, null, true, true);
    }

    public SizeableConcurrentHashMap(Size totals) {
        this(16, 0.75f, 16, totals, true, true);
    }

    public SizeableConcurrentHashMap(int initialCapacity, float loadFactor, int concurrencyLevel, boolean includeKeySizes, boolean includeValueSizes) {
        super(initialCapacity, loadFactor, concurrencyLevel);
        this.concurrencyLevel = concurrencyLevel;
        this.capacity = initialCapacity;
        this.loadFactor = loadFactor;
        this.size = new Size();
        this.selfSize = new Size();
        this.includeKeySizes = includeKeySizes;
        this.includeValueSizes = includeValueSizes;
        updateSelfSize();
    }

    public SizeableConcurrentHashMap(int initialCapacity, float loadFactor, int concurrencyLevel, Size total, boolean includeKeySizes, boolean includeValueSizes) {
        this(initialCapacity, loadFactor, concurrencyLevel, includeKeySizes, includeValueSizes);
        this.size.setTotal(total);
    }

    public void setIncludeKeySizes(boolean includeKeySizes) {
        this.includeKeySizes = includeKeySizes;
    }

    public void setIncludeValueSizes(boolean includeValueSizes) {
        this.includeValueSizes = includeValueSizes;
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
        if (!includeKeySizes && !includeValueSizes) {
            return;
        }
        if (includeKeySizes) {
            if (key instanceof Sizeable) {
                ((Sizeable) key).getSize().setTotal(this.size);
            } else if (key instanceof UserSubject) {
                this.size.add(USER_SUBJECT_SIZE);
            } else {
                this.size.add(SizeEstimator.estimateSize(key));
            }
        }
        if (includeValueSizes) {
            final boolean addNewValueSize = newValue != null && (!includeKeySizes || key != newValue);
            final boolean extractPrevValueSize = prevValue != null && (!includeKeySizes || key != prevValue);
            if (prevValue instanceof Sizeable || newValue instanceof Sizeable) {
                if (extractPrevValueSize) {
                    ((Sizeable) prevValue).getSize().detachFromTotal();
                }
                if (addNewValueSize) {
                    ((Sizeable) newValue).getSize().setTotal(this.size);
                }
            } else {
                if (extractPrevValueSize) {
                    this.size.add(-SizeEstimator.estimateSize(prevValue));
                }
                if (addNewValueSize) {
                    this.size.add(SizeEstimator.estimateSize(newValue));
                }
            }
        }
    }

    private void updateSizeOnRemove(K key, V value) {
        if (!includeKeySizes && !includeValueSizes || value == null) { // nothing removed
            return;
        }
        if (includeKeySizes) {
            if (key instanceof Sizeable) {
                ((Sizeable) key).getSize().detachFromTotal();
            } else if (key instanceof UserSubject) {
                this.size.add(-USER_SUBJECT_SIZE);
            } else {
                this.size.add(-SizeEstimator.estimateSize(key));
            }
        }
        if (includeValueSizes) {
            if (value != key) {
                if (value instanceof Sizeable) {
                    ((Sizeable) value).getSize().detachFromTotal();
                } else {
                    this.size.add(-SizeEstimator.estimateSize(value));
                }
            }
        }
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
        final int ref = (int) SizeEstimator.REFERENCE_SIZE;
        final int a = (4 * ref + 8) * size + (9 * ref + 40) * concurrencyLevel + 6 * ref + 36;
        if (maxPossibleCapacity <= 1) {
            return a;
        }
        final int b = ref / 4 + 2;
        int approx = a + (int) Math.pow(2, intLog2(maxPossibleCapacity - 1) + b);
        return approx;
    }

    public static int intLog2(int x) {
        if (x == 0) {
            return 0;
        }
        return 31 - Integer.numberOfLeadingZeros(x);
    }

    @Override
    public Size getSize() {
        return includeKeySizes ? size : selfSize;
    }
}
