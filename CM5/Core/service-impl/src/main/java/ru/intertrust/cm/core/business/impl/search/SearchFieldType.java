package ru.intertrust.cm.core.business.impl.search;

import java.util.Collection;

import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.business.api.dto.SearchFilter;

public interface SearchFieldType {

    boolean supportsFilter(SearchFilter filter);
    Collection<String> getSolrFieldNames(String field);
    FieldType getDataFieldType();
    // Признак заключения фразмента поиска в кавычки
    boolean isQuote();
    // Признак текстового типа
    boolean isTextType();
}
