package ru.intertrust.cm.core.business.impl.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.EmptyValueFilter;
import ru.intertrust.cm.core.business.api.dto.SearchFilter;
import ru.intertrust.cm.core.business.api.dto.SearchQuery;
import ru.intertrust.cm.core.model.SearchException;

public class EmptyValueFilterAdapter implements FilterAdapter<EmptyValueFilter> {

    @Autowired
    private SearchConfigHelper configHelper;

    @Override
    public String getFilterString(EmptyValueFilter filter, SearchQuery query) {
        String fieldName = filter.getFieldName();
        if (SearchFilter.EVERYWHERE.equals(fieldName)) {
            throw new SearchException("Поиск пустого значения может осущетсвляться только в конкретном поле");
        }
        Set<SearchFieldType> types = configHelper.getFieldTypes(fieldName, query.getAreas());
        ArrayList<String> fields = new ArrayList<>(types.size());
        for (SearchFieldType type : types) {
            if (type.supportsFilter(filter)) {
                for (String field : type.getSolrFieldNames(fieldName, false)) {
                    fields.add(new StringBuilder()
                            .append(field)
                            .append(":[")
                            .append(isTextType(type) ? "\"\"" : "*")
                            .append(" TO *]")
                            .toString());
                }
            }
        }
        return "-" + SolrUtils.joinStrings("OR", fields);
    }

    @Override
    public boolean isCompositeFilter(EmptyValueFilter filter) {
        return false;
    }

    @Override
    public List<String> getFieldNames(EmptyValueFilter filter, SearchQuery query) {
        String fieldName = filter.getFieldName();
        Set<SearchFieldType> types = configHelper.getFieldTypes(fieldName, query.getAreas());
        ArrayList<String> names = new ArrayList<>(types.size());
        for (SearchFieldType type : types) {
            if (type.supportsFilter(filter)) {
                for (String field : type.getSolrFieldNames(fieldName, false)) {
                    names.add(field);
                }
            }
        }
        return names;
    }

    private boolean isTextType(SearchFieldType type) {
        return type instanceof TextSearchFieldType || type instanceof SpecialTextSearchFieldType;
    }
}
