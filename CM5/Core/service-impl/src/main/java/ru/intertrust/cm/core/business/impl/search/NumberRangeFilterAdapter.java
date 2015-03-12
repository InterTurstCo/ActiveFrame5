package ru.intertrust.cm.core.business.impl.search;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.NumberRangeFilter;
import ru.intertrust.cm.core.business.api.dto.SearchQuery;

public class NumberRangeFilterAdapter implements FilterAdapter<NumberRangeFilter> {

    protected Logger log = LoggerFactory.getLogger(getClass());

    @Autowired private SearchConfigHelper configHelper;

    @Override
    public String getFilterString(NumberRangeFilter filter, SearchQuery query) {
        if (filter.getMin() == null && filter.getMax() == null) {
            log.warn("Empty number search filter for " + filter.getFieldName() + " ignored");
            return null;
        }

        Set<SearchFieldType> types = configHelper.getFieldTypes(filter.getFieldName(), query.getAreas());
        if (types.contains(SearchFieldType.LONG)) {
            String single = makeSolrFieldFilter(filter, SearchFieldType.LONG);
            if (!types.contains(SearchFieldType.LONG_MULTI)) {
                return single;
            }
            String multi = makeSolrFieldFilter(filter, SearchFieldType.LONG_MULTI);
            return new StringBuilder()
                    .append("(").append(single).append(" OR ").append(multi).append(")")
                    .toString();
        } else if (types.contains(SearchFieldType.LONG_MULTI)) {
            return makeSolrFieldFilter(filter, SearchFieldType.LONG_MULTI);
        }
        log.warn("Configured fields for field " + filter.getFieldName() + " not found in areas " + query.getAreas());
        return null;
    }

    private static String makeSolrFieldFilter(NumberRangeFilter filter, SearchFieldType type) {
        StringBuilder str = new StringBuilder()
                .append(SolrFields.FIELD_PREFIX)
                .append(type.getInfix())
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
