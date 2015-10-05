package ru.intertrust.cm.globalcache.impl.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Denis Mitavskiy
 *         Date: 16.09.2015
 *         Time: 13:09
 */
public class Size {
    private static final long SELF_SIZE = SizeEstimator.estimateSize(new Size(new Size()));

    private volatile Size sizeTotal;
    private final AtomicLong size;

    public Size() {
        this(null);
    }

    public Size(Size sizeTotal) {
        this.size = new AtomicLong();
        set(SELF_SIZE);
        setTotal(sizeTotal);
    }

    public Size setTotal(Size total) {
        if (this.sizeTotal != null) {
            throw new IllegalArgumentException("Totals already initialized");
        }
        this.sizeTotal = total;
        updateTotal(get());
        return this;
    }

    public Size set(long size) {
        final long previous = this.size.getAndSet(size);
        updateTotal(size - previous);
        return this;
    }

    public Size add(long delta) {
        size.addAndGet(delta);
        updateTotal(delta);
        return this;
    }

    public long get() {
        return size.get();
    }

    public void detachFromTotal() {
        Size total = sizeTotal;
        sizeTotal = null;
        final long previous = this.size.getAndSet(0);
        if (total != null) {
            total.add(-previous);
        }
    }

    private void updateTotal(long delta) {
        if (sizeTotal != null) {
            sizeTotal.add(delta);
        }
    }
}
