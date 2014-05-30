package ru.intertrust.cm.core.business.impl.search;

import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.SearchFilter;
import ru.intertrust.cm.core.business.api.dto.SearchQuery;
import ru.intertrust.cm.core.business.api.dto.TextSearchFilter;

public class TextFilterAdapter implements FilterAdapter<TextSearchFilter> {

    @Autowired
    private SearchConfigHelper configHelper;

    @Override
    public String getFilterString(TextSearchFilter filter, SearchQuery query) {
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
            return new TextFieldNameDecorator(baseField, configHelper.getSupportedLanguages());
        }
        final HashSet<String> langIds = new HashSet<>();
        for (String area : areaNames) {
            langIds.addAll(configHelper.getSupportedLanguages(name, area));
        }
        return new TextFieldNameDecorator(langIds, name, false);
    }
}
