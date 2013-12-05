package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 17/9/13
 *         Time: 12:05 PM
 */
public class CollectionPluginData extends PluginData {

    private String collectionName;
    private String textToFindInRow;
    private ArrayList<CollectionRowItem> items;
    private ArrayList<Id> excludedIds = new ArrayList<Id>();
    private LinkedHashMap<String, String> domainObjectFieldOnColumnNameMap;

    public CollectionPluginData() {

    }

    public String getTextToFindInRow() {
        return textToFindInRow;
    }

    public void setTextToFindInRow(String textToFindInRow) {
        this.textToFindInRow = textToFindInRow;
    }

    public ArrayList<Id> getExcludedIds() {
        return excludedIds;
    }

    public void setExcludedIds(ArrayList<Id> excludedIds) {
        this.excludedIds = excludedIds;
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
}
