package ru.intertrust.cm.core.business.api;

import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.SearchQuery;

public interface SearchService {

    public interface Remote extends SearchService {
    }

    IdentifiableObjectCollection search(String query, String areaName, int maxResults);
    IdentifiableObjectCollection search(SearchQuery query, int maxResults);
}
