package ru.intertrust.cm.core.business.impl.search;

import ru.intertrust.cm.core.business.api.dto.NumberRangeFilter;

public class NumberRangeFilterAdapter implements FilterAdapter<NumberRangeFilter> {

    @Override
    public String getFilterValue(NumberRangeFilter filter) {
        StringBuilder str = new StringBuilder()
                .append("[")
                .append(numberToString(filter.getMin()))
                .append(" TO ")
                .append(numberToString(filter.getMax()))
                .append("]");
        return str.toString();
    }

    @Override
    public String getFieldTypeSuffix(NumberRangeFilter filter) {
        return SearchFieldType.LONG.getSuffix();
    }

    private static String numberToString(Number number) {
        return number == null ? "*" : number.toString();
    }
}
