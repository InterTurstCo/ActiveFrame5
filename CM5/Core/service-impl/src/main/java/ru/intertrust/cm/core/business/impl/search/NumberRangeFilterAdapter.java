package ru.intertrust.cm.core.business.impl.search;

import ru.intertrust.cm.core.business.api.dto.NumberRangeFilter;
import ru.intertrust.cm.core.business.api.dto.SearchFilter;

public class NumberRangeFilterAdapter implements FilterAdapter {

    @Override
    public String getFilterValue(SearchFilter filter) {
        NumberRangeFilter numberFilter = (NumberRangeFilter) filter;
        StringBuilder str = new StringBuilder()
                .append("[")
                .append(numberToString(numberFilter.getMin()))
                .append(" TO ")
                .append(numberToString(numberFilter.getMax()))
                .append("]");
        return str.toString();
    }

    private static String numberToString(Number number) {
        return number == null ? "*" : number.toString();
    }
}
