package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.navigation.SortCriteriaConfig;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;

import java.util.*;


/**
 * Created with IntelliJ IDEA.
 * User: lvov
 * Date: 21.11.13
 * Time: 12:46
 * To change this template use File | Settings | File Templates.
 */
public class CollectionRowsRequest implements Dto {
    private int offset;
    private int limit;
    private String collectionName;
    private boolean isSortAscending;
    private String columnName;
    private boolean sortable;
    private String sortedField;
    private List<Filter> filterList;
    private String simpleSearchQuery;
    private String searchArea;
    private SortCriteriaConfig sortCriteriaConfig;
    private Set<Id> includedIds;
    private LinkedHashMap<String, CollectionColumnProperties> columnProperties;
    private Map<String, String> filtersMap;
    public CollectionRowsRequest(int offset, int limit, String collectionName, LinkedHashMap<String, CollectionColumnProperties> properties,
                                 List<Filter> filterList, String simpleSearchQuery, String searchArea) {
        this.offset = offset;
        this.limit = limit;
        this.collectionName = collectionName;
        this.columnProperties = properties;
        this.filterList = filterList;
        this.simpleSearchQuery = simpleSearchQuery;
        this.searchArea = searchArea;
        includedIds = new HashSet<Id>();

    }

    public CollectionRowsRequest(int offset, int limit, String collectionName, LinkedHashMap<String, CollectionColumnProperties> properties,
                                 boolean isSortAscending, String columnName, String sortedField,  List<Filter> filterList  ) {
        this.offset = offset;
        this.limit = limit;
        this.collectionName = collectionName;
        this.columnProperties = properties;
        this.isSortAscending = isSortAscending;
        this.columnName = columnName;
        this.sortable = true;
        this.sortedField = sortedField;
        this.filterList = filterList;
        includedIds = new HashSet<Id>();
    }

    public CollectionRowsRequest() {
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public boolean isSortAscending() {
        return isSortAscending;
    }

    public void setSortAscending(boolean sortAscending) {
        this.isSortAscending = sortAscending;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public boolean isSortable() {
        return sortable;
    }

    public void setSortable(boolean sortable) {
        this.sortable = sortable;
    }

    public String getSortedField() {
        return sortedField;
    }

    public void setSortedField(String sortedField) {
        this.sortedField = sortedField;
    }

    public List<Filter> getFilterList() {
        return filterList;
    }

    public void setFilterList(List<Filter> filterList) {
        this.filterList = filterList;
    }

    public String getSimpleSearchQuery() {
        return simpleSearchQuery;
    }

    public void setSimpleSearchQuery(String simpleSearchQuery) {
        this.simpleSearchQuery = simpleSearchQuery;
    }

    public String getSearchArea() {
        return searchArea;
    }

    public void setSearchArea(String searchArea) {
        this.searchArea = searchArea;
    }

    public SortCriteriaConfig getSortCriteriaConfig() {
        return sortCriteriaConfig;
    }

    public void setSortCriteriaConfig(SortCriteriaConfig sortCriteriaConfig) {
        this.sortCriteriaConfig = sortCriteriaConfig;
    }

    public Set<Id> getIncludedIds() {
        return includedIds;
    }

    public void setIncludedIds(Set<Id> includedIds) {
        this.includedIds = includedIds;
    }

    public LinkedHashMap<String, CollectionColumnProperties> getColumnProperties() {
        return columnProperties;
    }

    public void setColumnProperties(LinkedHashMap<String, CollectionColumnProperties> columnProperties) {
        this.columnProperties = columnProperties;
    }

    public Map<String, String> getFiltersMap() {
        return filtersMap;
    }

    public void setFiltersMap(Map<String, String> filtersMap) {
        this.filtersMap = filtersMap;
    }
}
