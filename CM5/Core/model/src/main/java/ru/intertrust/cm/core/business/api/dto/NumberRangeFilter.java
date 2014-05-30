package ru.intertrust.cm.core.business.api.dto;

public class NumberRangeFilter extends SearchFilterBase {

    private Number min;
    private Number max;

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

    @Override
    public String toString() {
        return "from " + (min == null ? "minus infinity" : min)
                + " to " + (max == null ? "plus intinity" : max);
    }
}
