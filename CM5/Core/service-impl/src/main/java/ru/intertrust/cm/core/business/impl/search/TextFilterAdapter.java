package ru.intertrust.cm.core.business.impl.search;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.SearchFilter;
import ru.intertrust.cm.core.business.api.dto.SearchQuery;
import ru.intertrust.cm.core.business.api.dto.TextSearchFilter;

public class TextFilterAdapter implements FilterAdapter<TextSearchFilter> {

    @Autowired
    private SearchConfigHelper configHelper;

    @Override
    public String getFilterString(TextSearchFilter filter, SearchQuery query) {
        if (filter.getText() == null || filter.getText().trim().isEmpty()) {
            return null;
        }

        StringBuilder value = new StringBuilder();
        boolean multiple = false;
        for (String solrField : enumSolrFields(filter.getFieldName(), query.getAreas())) {
            multiple = value.length() > 0;
            value.append(multiple ? " OR " : "")
                 .append(solrField)
                 .append(":(")
                 .append(SolrUtils.protectSearchString(filter.getText()))
                 .append(")");
        }
        if (multiple) {
            value.insert(0, "(").append(")");
        }
        return value.toString();
    }

    private Iterable<String> enumSolrFields(final String name, List<String> areaNames) {
        String baseField = null;
        if (SearchFilter.EVERYWHERE.equals(name)) {
            baseField = SolrFields.EVERYTHING;
        } else if (SearchFilter.CONTENT.equals(name)) {
            baseField = SolrFields.CONTENT;
        }
        if (baseField != null) {
            List<String> langIds = configHelper.getSupportedLanguages();
            ArrayList<String> fields = new ArrayList<>(langIds.size());
            for (String langId : langIds) {
                fields.add(makeSolrSpecialFieldName(baseField, langId));
            }
            return fields;
        }
        final HashSet<String> langIds = new HashSet<>();
        for (String area : areaNames) {
            langIds.addAll(configHelper.getSupportedLanguages(name, area));
        }
        Set<SearchFieldType> types = configHelper.getFieldTypes(name, areaNames);
        ArrayList<String> fields = new ArrayList<>(langIds.size());
        for (String langId : langIds) {
            if (types.contains(SearchFieldType.TEXT) || types.contains(null)) {
                fields.add(makeSolrFieldName(name, langId, SearchFieldType.TEXT));
            }
            if (types.contains(SearchFieldType.TEXT_MULTI)) {
                fields.add(makeSolrFieldName(name, langId, SearchFieldType.TEXT_MULTI));
            }
        }
        return fields;
    }

    private static String makeSolrFieldName(String field, String langId, SearchFieldType type) {
        StringBuilder result = new StringBuilder(SolrFields.FIELD_PREFIX);
        if (langId == null || langId.isEmpty()) {
            result.append(type.getInfix());
        } else {
            result.append(langId);
            if (type == SearchFieldType.TEXT_MULTI) {
                result.append("s");
            }
            result.append("_");
        }
        result.append(field.toLowerCase());
        return result.toString();
    }

    private static String makeSolrSpecialFieldName(String field, String langId) {
        StringBuilder result = new StringBuilder(field);
        if (langId != null && !langId.isEmpty()) {
            result.append("_").append(langId);
        }
        return result.toString();
    }
}
