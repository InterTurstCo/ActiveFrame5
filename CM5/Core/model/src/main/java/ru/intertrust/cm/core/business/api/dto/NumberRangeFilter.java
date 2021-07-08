package ru.intertrust.cm.core.business.api.dto;

public class NumberRangeFilter extends SearchFilterBase {

    private Number min;
    private Number max;

    private boolean minInclusive = true;
    private boolean maxInclusive = true;

    public NumberRangeFilter() {
    }

    public NumberRangeFilter(String fieldName) {
        super(fieldName);
    }

    public NumberRangeFilter(String fieldName, Integer min, Integer max) {
        super(fieldName);
        this.min = min;
        this.max = max;
    }

    public NumberRangeFilter(String fieldName, Integer min, boolean minInclusive,
                             Integer max, boolean maxInclusive) {
        super(fieldName);
        this.min = min;
        this.max = max;
    }

    public Number getMin() {
        return min;
    }

    public void setMin(Number min) {
        this.min = min;
    }

    public Number getMax() {
        return max;
    }

    public void setMax(Number max) {
        this.max = max;
    }

    public boolean isMinInclusive() {
        return minInclusive;
    }

    public void setMinInclusive(boolean minInclusive) {
        this.minInclusive = minInclusive;
    }

    public boolean isMaxInclusive() {
        return maxInclusive;
    }

    public void setMaxInclusive(boolean maxInclusive) {
        this.maxInclusive = maxInclusive;
    }

    @Override
    public String toString() {
        return "from " + (min == null ? "minus infinity" : (min + (minInclusive ? " inclusive" : "")))
                + " to " + (max == null ? "plus intinity" : (max + (maxInclusive ? " inclusive" : "")));
    }
}
