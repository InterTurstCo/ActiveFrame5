package ru.intertrust.cm.core.business.api.util;

import java.math.BigDecimal;
import java.util.Collection;

/**
 * Класс нарочно не синхронизирован, а переменные не волатильны - не страшно, если что-то будет рассинхронизировано
 * @author Denis Mitavskiy
 *         Date: 26.10.2015
 *         Time: 17:08
 */
public class LongCounter extends AbstractCounter {
    private volatile long total;
    private volatile long min;
    private volatile long max;
    private volatile boolean empty;

    public LongCounter() {
        super(0);
        empty = true;
    }

    public LongCounter(long invalidationPeriodMillies) {
        super(invalidationPeriodMillies);
        empty = true;
    }

    protected synchronized void trackValue(Number value) {
        long val = value.longValue();
        total += val;
        if (empty) {
            empty = false;
            min = val;
        }
        if (val < min) {
            min = val;
        }
        if (val > max) {
            max = val;
        }
    }

    protected void clearStatistics() {
        empty = true;
        total = 0;
        min = 0;
        max = 0;
    }

    public Long getTotal() {
        return total;
    }

    public Long getMin() {
        return min;
    }

    public Long getMax() {
        return max;
    }

    public double getAvg() {
        return eventCount == 0 ? 0.0 : new BigDecimal(total).divide(new BigDecimal(eventCount), 15, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public synchronized static LongCounter summarize(Collection<LongCounter> counters) {
        final LongCounter sum = new LongCounter(0);
        for (LongCounter counter : counters) {
            sum.eventCount += counter.eventCount;
            sum.subEventCount += counter.subEventCount;
            sum.total += counter.total;
            if (counter.min < sum.min) {
                sum.min = counter.min;
            }
            if (counter.max > sum.max) {
                sum.max = counter.max;
            }
        }
        return sum;
    }
}
