package ru.intertrust.cm.core.business.impl.search;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.BooleanSearchFilter;
import ru.intertrust.cm.core.business.api.dto.SearchQuery;

public class BooleanValueFilterAdapter implements FilterAdapter<BooleanSearchFilter> {

    @Autowired
    private SearchConfigHelper configHelper;

    @Override
    public String getFilterString(BooleanSearchFilter filter, SearchQuery query) {
        String fieldName = filter.getFieldName();
        Set<SearchFieldType> types = configHelper.getFieldTypes(fieldName, query.getAreas());
        if (types.contains(SearchFieldType.BOOL)) {
            return SolrFields.FIELD_PREFIX + SearchFieldType.BOOL.getInfix() + fieldName + ":" + (filter.getValue() ? "true" : "false");
        }
        return null;
    }

}
