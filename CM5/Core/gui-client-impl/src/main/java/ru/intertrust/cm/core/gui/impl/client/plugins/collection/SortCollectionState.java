package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

/**
 * Created with IntelliJ IDEA.
 * User: lvov
 * Date: 11.12.13
 * Time: 16:05
 * To change this template use File | Settings | File Templates.
 */

//класс для хранения состояния отсортированной колекции
public class SortCollectionState {
    private int offset, count;
    private String columnName;
    // false = от Z до А; true = от А до Z
    private boolean sortDirection;
    private boolean resetCollection;
    private boolean sortable;
    private String field;

    public SortCollectionState(int offset, int count, String columnName, boolean sortDirection, boolean resetCollection) {
        this.offset = offset;
        this.count = count;
        this.columnName = columnName;
        this.sortDirection = sortDirection;
        this.resetCollection = resetCollection;
        this.sortable = true;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public boolean isSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(boolean sortDirection) {
        this.sortDirection = sortDirection;
    }

    public boolean isResetCollection() {
        return resetCollection;
    }

    public void setResetCollection(boolean resetCollection) {
        this.resetCollection = resetCollection;
    }

    public boolean isSortable() {
        return sortable;
    }

    public void setSortable(boolean sortable) {
        this.sortable = sortable;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }


}
