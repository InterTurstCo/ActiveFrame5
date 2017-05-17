package ru.intertrust.cm.core.business.impl.search;

import java.util.ArrayList;
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

        ArrayList<String> strings = new ArrayList<>();
        Set<SearchFieldType> types = configHelper.getFieldTypes(filter.getFieldName(), query.getAreas());
        for (SearchFieldType type : new SearchFieldType[]
                { SearchFieldType.LONG, SearchFieldType.LONG_MULTI,
                  SearchFieldType.DOUBLE, SearchFieldType.DOUBLE_MULTI }) {
            if (types.contains(type)) {
                strings.add(makeSolrFieldFilter(filter, type));
            }
        }
        if (strings.size() == 0 && types.contains(null)) {
            strings.add(makeSolrFieldFilter(filter, SearchFieldType.LONG));
        }
        if (strings.size() == 1) {
            return strings.get(0);
        }
        if (strings.size() > 1) {
            StringBuilder result = new StringBuilder();
            for (String string : strings) {
                if (result.length() > 0) {
                    result.append(" OR ");
                }
                result.append(string);
            }
            result.insert(0, "(").append(")");
            return result.toString();
        }
        log.warn("Configured fields for field " + filter.getFieldName() + " not found in areas " + query.getAreas());
        return null;
    }

    @Override
    public boolean isCompositeFilter(NumberRangeFilter filter) {
        return false;
    }

    private static String makeSolrFieldFilter(NumberRangeFilter filter, SearchFieldType type) {
        StringBuilder str = new StringBuilder()
                .append(SolrFields.FIELD_PREFIX)
                .append(type.infix)
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
