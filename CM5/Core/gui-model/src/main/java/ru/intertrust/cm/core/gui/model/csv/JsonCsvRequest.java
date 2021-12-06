package ru.intertrust.cm.core.gui.model.csv;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 09.04.14
 *         Time: 16:15
 */
public class JsonCsvRequest {
    private String collectionName;
    private String simpleSearchQuery;
    private String simpleSearchArea;
    private String sortedFieldName;
    private boolean ascend;
    private List<JsonColumnProperties> columnProperties;
    private JsonSortCriteria sortCriteria;
    private JsonInitialFilters jsonInitialFilters;
    private JsonInitialFilters jsonHierarchicalFilters;
    private JsonSelectedIdsFilter jsonSelectedIdsFilter;

    public JsonCsvRequest() {
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public String getSimpleSearchQuery() {
        return simpleSearchQuery;
    }

    public void setSimpleSearchQuery(String simpleSearchQuery) {
        this.simpleSearchQuery = simpleSearchQuery;
    }

    public String getSimpleSearchArea() {
        return simpleSearchArea;
    }

    public void setSimpleSearchArea(String simpleSearchArea) {
        this.simpleSearchArea = simpleSearchArea;
    }

    public String getSortedFieldName() {
        return sortedFieldName;
    }

    public void setSortedFieldName(String sortedFieldName) {
        this.sortedFieldName = sortedFieldName;
    }

    public boolean isAscend() {
        return ascend;
    }

    public void setAscend(boolean ascend) {
        this.ascend = ascend;
    }

    public List<JsonColumnProperties> getColumnProperties() {
        return columnProperties;
    }

    public void setColumnProperties(List<JsonColumnProperties> columnProperties) {
        this.columnProperties = columnProperties;
    }

    public JsonSortCriteria getSortCriteria() {
        return sortCriteria;
    }

    public void setSortCriteria(JsonSortCriteria sortCriteria) {
        this.sortCriteria = sortCriteria;
    }

    public JsonInitialFilters getJsonInitialFilters() {
        return jsonInitialFilters;
    }

    public void setJsonInitialFilters(JsonInitialFilters jsonInitialFilters) {
        this.jsonInitialFilters = jsonInitialFilters;
    }

    public JsonInitialFilters getJsonHierarchicalFilters() {
        return jsonHierarchicalFilters;
    }

    public void setJsonHierarchicalFilters(JsonInitialFilters jsonHierarchicalFilters) {
        this.jsonHierarchicalFilters = jsonHierarchicalFilters;
    }

    public JsonSelectedIdsFilter getJsonSelectedIdsFilter() {
        return jsonSelectedIdsFilter;
    }

    public void setJsonSelectedIdsFilter(JsonSelectedIdsFilter jsonSelectedIdsFilter) {
        this.jsonSelectedIdsFilter = jsonSelectedIdsFilter;
    }
}
