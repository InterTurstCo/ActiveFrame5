package ru.intertrust.cm.core.gui.model.plugin;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.navigation.DefaultSortCriteriaConfig;
import ru.intertrust.cm.core.config.gui.navigation.FilterPanelConfig;
import ru.intertrust.cm.core.config.gui.navigation.InitialFiltersConfig;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 17/9/13
 *         Time: 12:05 PM
 */
public class CollectionPluginData extends ActivePluginData {

    private String collectionName;
    private DefaultSortCriteriaConfig defaultSortCriteriaConfig;
    private boolean singleChoice;
    private boolean displayChosenValues;
    private ArrayList<CollectionRowItem> items = new ArrayList<>();
    private LinkedHashMap<String, CollectionColumnProperties> domainObjectFieldPropertiesMap;
    private List<Integer> indexesOfSelectedItems = new ArrayList<>();
    private List<Id> chosenIds = new ArrayList<>();
    private String searchArea;
    private String collectionViewConfigName;
    private InitialFiltersConfig initialFiltersConfig;
    private FilterPanelConfig filterPanelConfig;

    public CollectionPluginData() {
        domainObjectFieldPropertiesMap = new LinkedHashMap<>();
    }

    public boolean isSingleChoice() {
        return singleChoice;
    }

    public void setSingleChoice(boolean singleChoice) {
        this.singleChoice = singleChoice;
    }

    public DefaultSortCriteriaConfig getDefaultSortCriteriaConfig() {
        return defaultSortCriteriaConfig;
    }

    public void setDefaultSortCriteriaConfig(DefaultSortCriteriaConfig defaultSortCriteriaConfig) {
        this.defaultSortCriteriaConfig = defaultSortCriteriaConfig;
    }

    public boolean isDisplayChosenValues() {
        return displayChosenValues;
    }

    public void setDisplayChosenValues(boolean displayChosenValues) {
        this.displayChosenValues = displayChosenValues;
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

    public List<Integer> getIndexesOfSelectedItems() {
        return indexesOfSelectedItems;
    }

    public void setIndexesOfSelectedItems(ArrayList<Integer> indexesOfSelectedItems) {
        this.indexesOfSelectedItems = indexesOfSelectedItems;
    }

    public List<Id> getChosenIds() {
        return chosenIds;
    }

    public void setChosenIds(List<Id> chosenIds) {
        this.chosenIds = chosenIds;
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
}
