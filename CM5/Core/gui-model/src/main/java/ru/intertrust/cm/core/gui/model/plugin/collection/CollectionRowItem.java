package ru.intertrust.cm.core.gui.model.plugin.collection;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Yaroslav Bondacrhuk
 * Date: 17/9/13
 * Time: 12:05 PM
 */
public class CollectionRowItem implements Dto {

    public enum RowType {
        DATA, FILTER, BUTTON;
    }

    private Id id;
    private HashMap<String, Value> row;
    private Map<String, List<String>> filters;
    private boolean expanded;
    private boolean expandable;
    private boolean haveChild = false;
    private Id parentId;
    private RowType rowType;
    private int nestingLevel;
    /**
     * Карта данных в зависимости от поля дочерней коллекции (так как подобных полей может быть больше одного)
     */
    private Map<String, ChildCollectionColumnData> childCollectionColumnFieldDataMap;

    public CollectionRowItem() {
    }

    public int getNestingLevel() {
        return nestingLevel;
    }

    public void setNestingLevel(int nestingLevel) {
        this.nestingLevel = nestingLevel;
    }

    public Map<String, ChildCollectionColumnData> getChildCollectionColumnFieldDataMap() {
        return childCollectionColumnFieldDataMap;
    }

    public void setChildCollectionColumnFieldDataMap(Map<String, ChildCollectionColumnData> childCollectionColumnFieldDataMap) {
        this.childCollectionColumnFieldDataMap = childCollectionColumnFieldDataMap;
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

    public void setFilters(Map<String, List<String>> filters) {
        this.filters = filters;
    }

    public void putFilterValues(String field, List<String> value) {
        if (filters == null) {
            filters = new HashMap<>();
        }
        filters.put(field, value);
    }

    public String getFilterValue(String field) {
        StringBuilder sb = new StringBuilder();
        if (filters != null) {
            List<String> filterValues = filters.get(field);

            if (filters != null) {
                for (String filterValue : filterValues) {
                    sb.append(filterValue);
                    sb.append(" ");
                }
            }
        }
        return sb.toString();
    }

    public Map<String, List<String>> getFilters() {
        return filters;
    }

    public Value getRowValue(String key) {
        return row.get(key);
    }

    public void setRow(HashMap<String, Value> row) {
        this.row = row;
    }

    public HashMap<String, Value> getRow() {
        return row;
    }

    public boolean isExpanded() {
        return expanded;
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

    public RowType getRowType() {
        return rowType;
    }

    public void setRowType(RowType rowType) {
        this.rowType = rowType;
    }

    public Id getParentId() {
        return parentId;
    }

    public void setParentId(Id parentId) {
        this.parentId = parentId;
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
        if (parentId != null ? !parentId.equals(that.parentId) : that.parentId != null) {
            return false;
        }
        if (rowType != null ? !rowType.equals(that.rowType) : that.rowType != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (parentId != null ? parentId.hashCode() : 0);
        result = 31 * result + (rowType != null ? rowType.hashCode() : 0);
        return result;
    }

    public boolean isHaveChild() {
        return haveChild;
    }

    public void setHaveChild(boolean haveChild) {
        this.haveChild = haveChild;
    }

    /**
     * Кладет в карту объект данных в зависимости от поля дочерней коллекции.<br>
     * Если объекта карты еще нет - он будет создан.
     *
     * @param field                     поле дочерней коллекции
     * @param childCollectionColumnData объект данных дочерней коллекции
     */
    public void putChildCollectionColumnData(String field, ChildCollectionColumnData childCollectionColumnData) {
        if (childCollectionColumnFieldDataMap == null) {
            childCollectionColumnFieldDataMap = new HashMap<>();
        }
        childCollectionColumnFieldDataMap.put(field, childCollectionColumnData);
    }

}
