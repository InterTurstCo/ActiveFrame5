package ru.intertrust.cm.core.business.impl.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.intertrust.cm.core.business.api.dto.NumberRangeFilter;
import ru.intertrust.cm.core.business.api.dto.SearchQuery;

public class NumberRangeFilterAdapter implements FilterAdapter<NumberRangeFilter> {

    protected Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public String getFilterString(NumberRangeFilter filter, SearchQuery query) {
        if (filter.getMin() == null && filter.getMax() == null) {
            log.warn("Empty number search filter for " + filter.getFieldName() + " ignored");
            return null;
        }
        StringBuilder str = new StringBuilder()
                .append(SolrFields.FIELD_PREFIX)
                .append(SearchFieldType.LONG.getInfix())
                .append(filter.getFieldName().toLowerCase())
                .append(":[")
                .append(numberToString(filter.getMin()))
                .append(" TO ")
                .append(numberToString(filter.getMax()))
                .append("]");
        return str.toString();
    }

    private static String numberToString(Number number) {
        return number == null ? "*" : number.toString();
    }
}
