package ru.intertrust.cm.core.business.impl.search;

import java.util.Collection;

import ru.intertrust.cm.core.business.api.dto.SearchFilter;

public interface SearchFieldType {

    boolean supportsFilter(SearchFilter filter);
    Collection<String> getSolrFieldNames(String field, boolean strict);
    //String preProcessSearchString(String searchString);
}
