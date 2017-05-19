package ru.intertrust.cm.core.business.impl.search;

import java.util.ArrayList;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.BooleanSearchFilter;
import ru.intertrust.cm.core.business.api.dto.SearchQuery;

public class BooleanValueFilterAdapter implements FilterAdapter<BooleanSearchFilter> {

    @Autowired private SearchConfigHelper configHelper;

    @Override
    public String getFilterString(BooleanSearchFilter filter, SearchQuery query) {
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
                            .append(":")
                            .append(filter.getValue() ? "true" : "false")
                            .toString());
                }
            }
        }
        return SolrUtils.joinStrings("OR", fields);
    }

    @Override
    public boolean isCompositeFilter(BooleanSearchFilter filter) {
        return false;
    }
}
