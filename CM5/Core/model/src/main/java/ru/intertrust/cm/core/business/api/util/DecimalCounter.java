package ru.intertrust.cm.core.business.api.util;

import java.math.BigDecimal;

/**
 * @author Denis Mitavskiy
 *         Date: 26.10.2015
 *         Time: 17:44
 */
public class DecimalCounter extends AbstractCounter {
    private volatile BigDecimal total = BigDecimal.ZERO;;
    private volatile BigDecimal min;
    private volatile BigDecimal max = BigDecimal.ZERO;

    public DecimalCounter() {
        super(0);
    }

    public DecimalCounter(long invalidationPeriodMillies) {
        super(invalidationPeriodMillies);
    }

    protected void trackValue(Number value) {
        final BigDecimal decimalValue = BigDecimal.valueOf(value.doubleValue());
        total = total.add(decimalValue);
        if (min == null) {
            min = decimalValue;
        } else if (decimalValue.compareTo(min) < 0) {
            min = decimalValue;
        }
        if (decimalValue.compareTo(max) > 0) {
            max = decimalValue;
        }
    }

    protected void clearStatistics() {
        total = BigDecimal.ZERO;
        min = null;
        max = BigDecimal.ZERO;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public BigDecimal getMin() {
        return min == null ? BigDecimal.ZERO : min;
    }

    public BigDecimal getMax() {
        return max;
    }

    public double getAvg() {
        return eventCount == 0 ? 0.0 : total.divide(new BigDecimal(eventCount), 15, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
}
