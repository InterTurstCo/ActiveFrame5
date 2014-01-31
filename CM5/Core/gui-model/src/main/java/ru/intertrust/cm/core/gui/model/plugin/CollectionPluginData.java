package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.navigation.DefaultSortCriteriaConfig;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

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
    private ArrayList<CollectionRowItem> items = new ArrayList<CollectionRowItem>();
    private LinkedHashMap<String, CollectionColumnProperties> domainObjectFieldPropertiesMap;
    private ArrayList<Integer> indexesOfSelectedItems = new ArrayList<Integer>();
    private List<Id> chosenIds = new ArrayList<Id>();
    private String searchArea;

    public CollectionPluginData() {
        domainObjectFieldPropertiesMap = new LinkedHashMap<String, CollectionColumnProperties>();
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

    public ArrayList<Integer> getIndexesOfSelectedItems() {
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
}
