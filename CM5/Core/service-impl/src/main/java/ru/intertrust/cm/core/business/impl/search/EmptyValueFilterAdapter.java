package ru.intertrust.cm.core.business.impl.search;

import java.util.Collections;
import java.util.HashSet;
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
        String field = filter.getFieldName();
        if (SearchFilter.EVERYWHERE.equals(field)) {
            throw new SearchException("Поиск пустого значения может осущетсвляться только в конкретном поле");
        }
        Set<SearchFieldType> types = configHelper.getFieldTypes(field, query.getAreas());
        StringBuilder result = new StringBuilder();
        boolean multiple = false;
        for (SearchFieldType solrType : types) {
            //SearchFieldType solrType = SearchFieldType.getFieldType(type.getDataType(), type.isMultivalued());
            Set<String> infixes = new HashSet<>();
            if (solrType == null) {
                solrType = SearchFieldType.TEXT;
            }
            if (SearchFieldType.TEXT == solrType || SearchFieldType.TEXT_MULTI == solrType) {
                for (String area : query.getAreas()) {
                    for (String langId : configHelper.getSupportedLanguages(field, area)) {
                        infixes.add(langId.isEmpty() ? solrType.infix
                                : langId + (solrType == SearchFieldType.TEXT_MULTI ? "s_" : "_"));
                    }
                }
            } else {
                infixes = Collections.singleton(solrType.infix);
            }
            for (String infix : infixes) {
                if (result.length() > 0) {
                    result.append(" OR ");
                    multiple = true;
                }
                result.append(SolrFields.FIELD_PREFIX)
                      .append(infix)
                      .append(field.toLowerCase())
                      .append(":[")
                      .append(solrType == SearchFieldType.TEXT || solrType == SearchFieldType.TEXT_MULTI ? "\"\"" : "*")
                      .append(" TO *]");
            }
        }
        if (multiple) {
            result.insert(0, "(").append(")");
        }
        result.insert(0, "-");
        return result.toString();
    }

}
