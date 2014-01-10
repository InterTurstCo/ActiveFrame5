package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.business.api.dto.SearchQuery;

/**
 * User: IPetrov
 * Date: 09.01.14
 * Time: 11:05
 * Данные об условиях расширенного поиска
 */
public class ExtendedSearchData extends PluginData {

    // объект содержащий данные для поиска
    private SearchQuery searchQuery;
    // максимальное количество возвращаемых данных
    int maxResults;

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public ExtendedSearchData() {
    }

    public SearchQuery getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(SearchQuery searchQuery) {
        this.searchQuery = searchQuery;
    }

}
