package ru.intertrust.cm.core.gui.model.plugin.collection;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.SearchQuery;
import ru.intertrust.cm.core.config.gui.form.widget.TableBrowserParams;
import ru.intertrust.cm.core.config.gui.form.widget.filter.extra.CollectionExtraFiltersConfig;
import ru.intertrust.cm.core.config.gui.navigation.DefaultSortCriteriaConfig;
import ru.intertrust.cm.core.config.gui.navigation.InitialFiltersConfig;
import ru.intertrust.cm.core.config.gui.navigation.SortCriteriaConfig;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;

import java.util.*;

/**
 * Created by Vitaliy Orlov on 16.08.2016.
 */
public class ExtendedSearchCollectionRowsRequest extends CollectionRowsRequest{
    private SearchQuery searchQuery;

    public ExtendedSearchCollectionRowsRequest() {
    }

    public ExtendedSearchCollectionRowsRequest(CollectionRowsRequest mainRequest, SearchQuery searchQuery) {
        super();
        setOffset(mainRequest.getOffset());
        setOffset(mainRequest.getOffset());
        setLimit(mainRequest.getLimit());
        setCollectionName(mainRequest.getCollectionName());
        setSortAscending(mainRequest.isSortAscending());
        setColumnName(mainRequest.getColumnName());
        setSortable(mainRequest.isSortable());
        setSortedField(mainRequest.getSortedField());
        setInitialFiltersConfig(mainRequest.getInitialFiltersConfig());
        setSimpleSearchQuery(mainRequest.getSimpleSearchQuery());
        setSearchArea(mainRequest.getSearchArea());
        setSortCriteriaConfig(mainRequest.getSortCriteriaConfig());
        setDefaultSortCriteriaConfig(mainRequest.getDefaultSortCriteriaConfig());
        setIncludedIds(mainRequest.getIncludedIds());
        setColumnProperties(mainRequest.getColumnProperties());
        setFiltersMap(mainRequest.getFiltersMap());
        setTableBrowserParams(mainRequest.getTableBrowserParams());
        setHierarchicalFiltersConfig(mainRequest.getHierarchicalFiltersConfig());
        setParentId(mainRequest.getParentId());
        setCurrentNestingLevel(mainRequest.getCurrentNestingLevel());
        setExpandableTypes(mainRequest.getExpandableTypes());
        this.searchQuery = searchQuery;
    }

    public SearchQuery getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(SearchQuery searchQuery) {
        this.searchQuery = searchQuery;
    }
}
