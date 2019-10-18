package ru.intertrust.cm.core.business.api.util;

import ru.intertrust.cm.core.business.api.dto.Dto;

import java.math.BigDecimal;

/**
 * @author Denis Mitavskiy
 *         Date: 26.10.2015
 *         Time: 17:37
 */
public abstract class AbstractCounter implements Cloneable, Dto {
    protected volatile long eventCount;
    protected volatile long subEventCount;

    private long invalidationPeriodMillies;
    private long nextInvalidationTime;

    public AbstractCounter() {
        this(0);
    }

    public AbstractCounter(long invalidationPeriodMillies) {
        this.invalidationPeriodMillies = invalidationPeriodMillies;
        setInvalidationTime();
    }

    public void log(Number value) {
        log(value, false);
    }

    public synchronized void log(Number value, boolean logSubEvent) {
        invalidateIfTime();
        ++eventCount;
        if (logSubEvent) {
            ++subEventCount;
        }
        trackValue(value);
    }

    protected abstract void trackValue(Number value);

    public void clear() {
        eventCount = 0;
        subEventCount = 0;
        clearStatistics();
    }

    protected abstract void clearStatistics();

    public long getEventCount() {
        return eventCount;
    }

    public long getSubEventCount() {
        return subEventCount;
    }

    public double getSubEventPercentage() {
        return eventCount == 0 ? 0.0 : new BigDecimal(subEventCount).divide(new BigDecimal(eventCount), 15, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public abstract Number getTotal();

    public abstract Number getMin();

    public abstract Number getMax();

    public abstract double getAvg();

    private void invalidateIfTime() {
        if (invalidationPeriodMillies > 0) {
            if (System.currentTimeMillis() >= nextInvalidationTime) {
                clear();
                setInvalidationTime();
            }
        }
    }

    private void setInvalidationTime() {
        nextInvalidationTime = invalidationPeriodMillies <= 0 ? -1 : ((System.currentTimeMillis() + invalidationPeriodMillies) / invalidationPeriodMillies) * invalidationPeriodMillies;
    }

}
