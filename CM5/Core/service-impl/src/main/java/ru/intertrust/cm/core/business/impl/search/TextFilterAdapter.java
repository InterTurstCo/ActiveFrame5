package ru.intertrust.cm.core.business.impl.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.SearchFilter;
import ru.intertrust.cm.core.business.api.dto.SearchQuery;
import ru.intertrust.cm.core.business.api.dto.TextSearchFilter;

public class TextFilterAdapter implements FilterAdapter<TextSearchFilter> {

    @Autowired private SearchConfigHelper configHelper;

    @Override
    public String getFilterString(TextSearchFilter filter, SearchQuery query) {
        if (filter.getText() == null || filter.getText().trim().isEmpty()) {
            return null;
        }
        String fieldName = filter.getFieldName();
        Set<SearchFieldType> types;
        if (SearchFilter.EVERYWHERE.equals(fieldName)) {
            types = Collections.<SearchFieldType>singleton(
                    new SpecialTextSearchFieldType(configHelper.getSupportedLanguages()));
        } else if (SearchFilter.CONTENT.equals(fieldName)) {
            types = Collections.<SearchFieldType>singleton(
                    new SpecialTextSearchFieldType(configHelper.getSupportedLanguages()));
        } else {
            types = configHelper.getFieldTypes(fieldName, query.getAreas());
            if (types.size() == 0) {
                return null;
            }
        }
        ArrayList<String> fields = new ArrayList<>(types.size());
        for (SearchFieldType type : types) {
            if (type.supportsFilter(filter)) {
                String searchString = filter.getText();
                if (type instanceof TextSearchFieldType) {
                    ((TextSearchFieldType) type).addLanguage("");
                }
                if (type instanceof SpecialTextSearchFieldType || (type instanceof TextSearchFieldType && ((TextSearchFieldType) type).isSearchBySubstring())) {
                    if (searchString.length() >=2 && searchString.startsWith("\"") && searchString.endsWith("\"")) {
                        searchString = searchString.substring(1, searchString.length() - 1);
                    }
                    searchString = new StringBuilder()
                            .append("\"")
                            .append(SolrUtils.protectSearchString(searchString, true))
                            .append("\"")
                            .toString();
                } else {
                    searchString = SolrUtils.protectSearchString(searchString);
                }
                for (String field : type.getSolrFieldNames(fieldName, false)) {
                    fields.add(new StringBuilder()
                            .append(field)
                            .append(":(")
                            .append(searchString)
                            .append(")")
                            .toString());
                }
            }
        }
        return SolrUtils.joinStrings("OR", fields);
    }

    @Override
    public boolean isCompositeFilter(TextSearchFilter filter) {
        return false;
    }

    @Override
    public List<String> getFieldNames(TextSearchFilter filter, SearchQuery query) {
        String fieldName = filter.getFieldName();
        Set<SearchFieldType> types;
        if (SearchFilter.EVERYWHERE.equals(fieldName)) {
            types = Collections.<SearchFieldType>singleton(
                    new SpecialTextSearchFieldType(configHelper.getSupportedLanguages()));
        } else if (SearchFilter.CONTENT.equals(fieldName)) {
            types = Collections.<SearchFieldType>singleton(
                    new SpecialTextSearchFieldType(configHelper.getSupportedLanguages()));
        } else {
            types = configHelper.getFieldTypes(fieldName, query.getAreas());
        }
        ArrayList<String> names = new ArrayList<>(types.size());
        for (SearchFieldType type : types) {
            if (type.supportsFilter(filter)) {
                String searchString = filter.getText();
                if (type instanceof TextSearchFieldType) {
                    ((TextSearchFieldType) type).addLanguage("");
                }
                for (String field : type.getSolrFieldNames(fieldName, false)) {
                    names.add(field);
                }
            }
        }
        return names;
    }

}
