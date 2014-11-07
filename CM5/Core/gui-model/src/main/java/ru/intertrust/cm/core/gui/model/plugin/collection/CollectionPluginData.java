package ru.intertrust.cm.core.gui.model.plugin.collection;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.TableBrowserParams;
import ru.intertrust.cm.core.config.gui.navigation.DefaultSortCriteriaConfig;
import ru.intertrust.cm.core.config.gui.navigation.FilterPanelConfig;
import ru.intertrust.cm.core.config.gui.navigation.InitialFiltersConfig;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;
import ru.intertrust.cm.core.gui.model.plugin.ActivePluginData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 17/9/13
 *         Time: 12:05 PM
 */
public class CollectionPluginData extends ActivePluginData {

    private String collectionName;
    private DefaultSortCriteriaConfig defaultSortCriteriaConfig;

    private ArrayList<CollectionRowItem> items = new ArrayList<>();
    private LinkedHashMap<String, CollectionColumnProperties> domainObjectFieldPropertiesMap;
    private Collection<Id> chosenIds;
    private String searchArea;
    private String collectionViewConfigName;
    private InitialFiltersConfig initialFiltersConfig;
    private FilterPanelConfig filterPanelConfig;
    private int rowsChunk;
    private TableBrowserParams tableBrowserParams;
    private boolean extendedSearchMarker;
    private InitialFiltersConfig hierarchicalFiltersConfig;

    public CollectionPluginData() {
        domainObjectFieldPropertiesMap = new LinkedHashMap<>();
    }

    public boolean isSingleChoice() {
        return tableBrowserParams == null ? true : tableBrowserParams.isSingleChoice();
    }

    public DefaultSortCriteriaConfig getDefaultSortCriteriaConfig() {
        return defaultSortCriteriaConfig;
    }

    public void setDefaultSortCriteriaConfig(DefaultSortCriteriaConfig defaultSortCriteriaConfig) {
        this.defaultSortCriteriaConfig = defaultSortCriteriaConfig;
    }

    public ArrayList<CollectionRowItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<CollectionRowItem> items) {
        this.items = items;
    }

    public LinkedHashMap<String, CollectionColumnProperties> getDomainObjectFieldPropertiesMap() {
        return domainObjectFieldPropertiesMap;
    }

    public Collection<Id> getChosenIds() {
        return chosenIds;
    }

    public void setChosenIds(Collection<Id> chosenIds) {
        this.chosenIds = chosenIds;
    }

    public void setDomainObjectFieldPropertiesMap(
            final LinkedHashMap<String, CollectionColumnProperties> domainObjectFieldPropertiesMap) {
        this.domainObjectFieldPropertiesMap = domainObjectFieldPropertiesMap;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public String getSearchArea() {
        return searchArea;
    }

    public void setSearchArea(String searchArea) {
        this.searchArea = searchArea;
    }

    public String getCollectionViewConfigName() {
        return collectionViewConfigName;
    }

    public void setCollectionViewConfigName(String collectionViewConfigName) {
        this.collectionViewConfigName = collectionViewConfigName;
    }

    public InitialFiltersConfig getInitialFiltersConfig() {
        return initialFiltersConfig;
    }

    public void setInitialFiltersConfig(InitialFiltersConfig initialFiltersConfig) {
        this.initialFiltersConfig = initialFiltersConfig;
    }

    public FilterPanelConfig getFilterPanelConfig() {
        return filterPanelConfig;
    }

    public void setFilterPanelConfig(FilterPanelConfig filterPanelConfig) {
        this.filterPanelConfig = filterPanelConfig;
    }

    public int getRowsChunk() {
        return tableBrowserParams == null ? rowsChunk : tableBrowserParams.getPageSize();
    }

    public void setRowsChunk(int rowsChunk) {
        this.rowsChunk = rowsChunk;
    }

    public TableBrowserParams getTableBrowserParams() {
        return tableBrowserParams;
    }

    public void setTableBrowserParams(TableBrowserParams tableBrowserParams) {
        this.tableBrowserParams = tableBrowserParams;
    }

    public boolean isExtendedSearchMarker() {
        return extendedSearchMarker;
    }

    public void setExtendedSearchMarker(boolean extendedSearchMarker){
        this.extendedSearchMarker = extendedSearchMarker;
    }

    public InitialFiltersConfig getHierarchicalFiltersConfig() {
        return hierarchicalFiltersConfig;
    }

    public void setHierarchicalFiltersConfig(InitialFiltersConfig hierarchicalFiltersConfig) {
        this.hierarchicalFiltersConfig = hierarchicalFiltersConfig;
    }
}
