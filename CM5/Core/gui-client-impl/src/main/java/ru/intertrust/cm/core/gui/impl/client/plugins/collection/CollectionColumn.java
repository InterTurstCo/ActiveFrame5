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

}
