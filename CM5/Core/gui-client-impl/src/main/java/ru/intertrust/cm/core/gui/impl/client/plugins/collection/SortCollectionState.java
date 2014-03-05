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
    private boolean ascend;
    private boolean resetCollection;
    private boolean sortable;
    private String field;

    public SortCollectionState(int offset, int count, String columnName, boolean sortDirection, boolean resetCollection, String field) {
        this.offset = offset;
        this.count = count;
        this.columnName = columnName;
        this.ascend = sortDirection;
        this.resetCollection = resetCollection;
        this.sortable = true;
        this.field = field;
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

    public boolean isAscend() {
        return ascend;
    }

    public void setAscend(boolean ascend) {
        this.ascend = ascend;
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
