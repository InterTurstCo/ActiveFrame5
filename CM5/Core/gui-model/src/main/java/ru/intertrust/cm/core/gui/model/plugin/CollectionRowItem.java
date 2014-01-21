package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.Value;

import java.util.HashMap;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 17/9/13
 *         Time: 12:05 PM
 */
public class CollectionRowItem implements Dto{
    private Id id;
    private  HashMap<String, Value> row;

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

    @Override
    public int hashCode() {
        return getId() == null ? System.identityHashCode(this) : getId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }
        if(obj == null || !(getClass() == obj.getClass())) {
            return false;
        }
        CollectionRowItem other = (CollectionRowItem) obj;
        return (getId() == null ? other.getId() == null : getId().equals(other.getId()));
    }
}
