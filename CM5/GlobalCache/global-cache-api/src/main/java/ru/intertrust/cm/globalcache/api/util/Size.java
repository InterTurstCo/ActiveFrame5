package ru.intertrust.cm.globalcache.api.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Denis Mitavskiy Date: 16.09.2015 Time: 13:09
 */
public class Size {
    public static final long BYTES_IN_MEGABYTE = 1024 * 1024;

    private static final long SELF_SIZE = SizeEstimator.estimateSize(new Size(new Size()));

    private volatile Size sizeTotal;
    private final AtomicLong size;

    public Size() {
        this(null);
    }

    public Size(Size sizeTotal) {
        this.size = new AtomicLong();
        set(0);
        setTotal(sizeTotal);
    }

    public Size(long size) {
        this(null);
        set(size);
    }

    public synchronized Size setTotal(Size total) {
        if (this.sizeTotal != null) {
            throw new IllegalArgumentException("Total's already initialized");
        }
        this.sizeTotal = total;
        updateTotal(get());
        return this;
    }

    public Size set(long size) {
        final long realSize = size + SELF_SIZE;
        final long previous = this.size.getAndSet(realSize);
        updateTotal(realSize - previous);
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

    public long getMB() {
        return size.get() / BYTES_IN_MEGABYTE;
    }

    public synchronized void detachFromTotal() {
        Size total = sizeTotal;
        sizeTotal = null;
        final long previous = this.size.getAndSet(0);
        if (total != null) {
            total.add(-previous);
        }
    }

    @Override
    public String toString() {
        return get() / BYTES_IN_MEGABYTE + " MB";
    }

    private synchronized void updateTotal(long delta) {
        if (sizeTotal != null) {
            sizeTotal.add(delta);
        }
    }
}
