package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.user.cellview.client.Column;
import ru.intertrust.cm.core.gui.model.plugin.CollectionRowItem;

/**
 * @author Denis Mitavskiy
 *         Date: 21.01.14
 *         Time: 22:59
 */
public abstract class CollectionColumn extends Column<CollectionRowItem, String> {
    protected String fieldName;
    protected Boolean resizable = true;
    protected int minWidth = 120;
    protected int maxWidth = 999999;
    public CollectionColumn(AbstractCell cell) {
        super(cell);
    }

    public CollectionColumn( AbstractCell cell, String fieldName, boolean resizable) {
        super(cell);
        this.fieldName = fieldName;
        this.resizable = resizable;

    }

    public String getFieldName() {
        return fieldName;
    }

    public Boolean isResizable() {
        return resizable;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public void setResizable(Boolean resizable) {
        this.resizable = resizable;
    }

    public int getMinWidth() {
        return minWidth;
    }

    public void setMinWidth(Integer minWidth) {
        this.minWidth = minWidth;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(Integer maxWidth) {
        this.maxWidth = maxWidth;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CollectionColumn that = (CollectionColumn) o;

        if (maxWidth != that.maxWidth) return false;
        if (minWidth != that.minWidth) return false;
        if (fieldName != null ? !fieldName.equals(that.fieldName) : that.fieldName != null) return false;
        if (resizable != null ? !resizable.equals(that.resizable) : that.resizable != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = fieldName != null ? fieldName.hashCode() : 0;
        result = 31 * result + (resizable != null ? resizable.hashCode() : 0);
        result = 31 * result + minWidth;
        result = 31 * result + maxWidth;
        return result;
    }
}
