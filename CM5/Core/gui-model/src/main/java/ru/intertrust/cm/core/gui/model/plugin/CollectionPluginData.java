package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 17/9/13
 *         Time: 12:05 PM
 */
public class CollectionPluginData extends PluginData {

    private String collectionName;
    private LinkedHashMap<String, String> fieldMap = new LinkedHashMap<String, String>();
    private boolean singleChoice;
    private boolean displayChosenValues;
    private ArrayList<CollectionRowItem> items = new ArrayList<CollectionRowItem>();
    private LinkedHashMap<String, String> domainObjectFieldOnColumnNameMap = new LinkedHashMap<String, String>();
    private ArrayList<Integer> indexesOfSelectedItems = new ArrayList<Integer>();
    private List<Id> chosenIds = new ArrayList<Id>();
    public CollectionPluginData() {

    }

    public boolean isSingleChoice() {
        return singleChoice;
    }

    public void setSingleChoice(boolean singleChoice) {
        this.singleChoice = singleChoice;
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

    public LinkedHashMap<String, String> getDomainObjectFieldOnColumnNameMap() {
        return domainObjectFieldOnColumnNameMap;
    }

    public void setDomainObjectFieldOnColumnNameMap(LinkedHashMap<String, String> domainObjectFieldOnColumnNameMap) {
        this.domainObjectFieldOnColumnNameMap = domainObjectFieldOnColumnNameMap;
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

    public LinkedHashMap<String, String> getFieldMap() {
        return fieldMap;
    }

    public void setFieldMap(LinkedHashMap<String, String> fieldMap) {
        this.fieldMap = fieldMap;
    }
}
