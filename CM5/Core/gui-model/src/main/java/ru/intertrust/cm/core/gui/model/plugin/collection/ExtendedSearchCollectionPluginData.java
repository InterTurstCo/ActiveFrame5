package ru.intertrust.cm.core.gui.model.plugin.collection;

import ru.intertrust.cm.core.business.api.dto.SearchQuery;

/**
 * Created by Vitaliy Orlov on 16.08.2016.
 */
public class ExtendedSearchCollectionPluginData extends CollectionPluginData{
    private SearchQuery searchQuery;

    public SearchQuery getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(SearchQuery searchQuery) {
        this.searchQuery = searchQuery;
    }
}
