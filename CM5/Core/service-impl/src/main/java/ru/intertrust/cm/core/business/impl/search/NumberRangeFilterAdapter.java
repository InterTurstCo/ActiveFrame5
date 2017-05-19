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
        String fieldName = filter.getFieldName();
        Set<SearchFieldType> types = configHelper.getFieldTypes(fieldName, query.getAreas());
        if (types.size() == 0) {
            return null;
        }
        ArrayList<String> fields = new ArrayList<>(types.size());
        for (SearchFieldType type : types) {
            if (type.supportsFilter(filter)) {
                for (String field : type.getSolrFieldNames(fieldName, false)) {
                    fields.add(new StringBuilder()
                            .append(field)
                            .append(":[")
                            .append(numberToString(filter.getMin()))
                            .append(" TO ")
                            .append(numberToString(filter.getMax()))
                            .append("]")
                            .toString());
                }
            }
        }
        return SolrUtils.joinStrings("OR", fields);
    }

    @Override
    public boolean isCompositeFilter(NumberRangeFilter filter) {
        return false;
    }

    private static String numberToString(Number number) {
        return number == null ? "*" : number.toString();
    }
}
