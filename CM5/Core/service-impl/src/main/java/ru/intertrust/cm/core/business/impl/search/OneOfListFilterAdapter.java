package ru.intertrust.cm.core.business.impl.search;

import ru.intertrust.cm.core.business.api.dto.OneOfListFilter;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.SearchQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class OneOfListFilterAdapter implements FilterAdapter<OneOfListFilter> {

    protected Logger log = LoggerFactory.getLogger(getClass());

    @Autowired private SearchConfigHelper configHelper;

    @Override
    public String getFilterString(OneOfListFilter filter, SearchQuery query) {
        List<ReferenceValue> values = filter.getValues();
        if (values.size() == 0) {
            log.warn("Empty list search filter for " + filter.getFieldName() + " ignored");
            return null;
        }
        String fieldName = filter.getFieldName();
        Set<SearchFieldType> types = configHelper.getFieldTypes(fieldName, query.getAreas(), query.getTargetObjectTypes());
        if (types.size() == 0) {
            return null;
        }
        StringBuilder searchString = new StringBuilder();
        for (ReferenceValue value : values) {
            if (searchString.length() > 0)  {
                searchString.append(" OR ");
            }
            searchString.append(value.get().toStringRepresentation());
        }
        searchString.insert(0, "(").append(")");

        ArrayList<String> fields = new ArrayList<>(types.size());
        for (SearchFieldType type : types) {
            if (type.supportsFilter(filter)) {
                for (String field : type.getSolrFieldNames(fieldName)) {
                    fields.add(new StringBuilder()
                            .append(field)
                            .append(":")
                            .append(searchString.toString())
                            .toString());
                }
            }
        }
        return SolrUtils.joinStrings("OR", fields);
    }

    @Override
    public boolean isCompositeFilter(OneOfListFilter filter) {
        return false;
    }

    @Override
    public List<String> getFieldNames(OneOfListFilter filter, SearchQuery query) {
        String fieldName = filter.getFieldName();
        Set<SearchFieldType> types = configHelper.getFieldTypes(fieldName, query.getAreas(), query.getTargetObjectTypes());
        ArrayList<String> names = new ArrayList<>(types.size());
        if (types.size() == 0) {
            return names;
        }

        for (SearchFieldType type : types) {
            if (type.supportsFilter(filter)) {
                for (String field : type.getSolrFieldNames(fieldName)) {
                    names.add(field);
                }
            }
        }
        return names;

    }
}
