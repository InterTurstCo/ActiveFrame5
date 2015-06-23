package ru.intertrust.cm.core.gui.model.plugin.collection;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.Value;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 17/9/13
 *         Time: 12:05 PM
 */
public class  CollectionRowItem implements Dto{
    private Id id;
    private  HashMap<String, Value> row;
    private boolean expanded;
    private boolean expandable;
    private List<CollectionRowItem> collectionRowItems;
    public CollectionRowItem() {
    }

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }

    public String getStringValue(String key) {
        Value value = row.get(key);
        return value == null || value.get() == null ? "" : value.toString();
    }

    public Value getRowValue(String key) {
        return row.get(key);
    }

    public void setRow(HashMap<String, Value> row) {
        this.row = row;
    }

    public HashMap<String, Value> getRow () {
        return row;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public List<CollectionRowItem> getCollectionRowItems() {
        return collectionRowItems == null ? Collections.<CollectionRowItem>emptyList(): collectionRowItems;
    }

    public void setCollectionRowItems(List<CollectionRowItem> collectionRowItems) {
        this.collectionRowItems = collectionRowItems;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public boolean isExpandable() {
        return expandable;
    }

    public void setExpandable(boolean expandable) {
        this.expandable = expandable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CollectionRowItem that = (CollectionRowItem) o;

        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        return result;
    }

}
