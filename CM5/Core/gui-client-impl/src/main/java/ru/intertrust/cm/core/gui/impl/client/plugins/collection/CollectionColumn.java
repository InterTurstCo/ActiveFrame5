package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.user.cellview.client.Column;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;

/**
 * @author Denis Mitavskiy
 *         Date: 21.01.14
 *         Time: 22:59
 */
public abstract class CollectionColumn<CollectionRowItem, T> extends Column<CollectionRowItem, T> {

    protected String fieldName;
    protected boolean resizable = true;
    private int userWidth;
    protected int minWidth = BusinessUniverseConstants.MIN_COLUMN_WIDTH;
    protected int maxWidth = BusinessUniverseConstants.MAX_COLUMN_WIDTH;
    protected boolean moveable = true;
    protected boolean visible;
    protected int drawWidth;


    public CollectionColumn(AbstractCell cell) {
        super(cell);
    }

    public CollectionColumn(AbstractCell cell, String fieldName, boolean resizable) {
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

    public int getUserWidth() {
        return userWidth;
    }

    public void setUserWidth(int userWidth) {
        this.userWidth = userWidth;
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

    public boolean isMoveable() {
        return moveable;
    }

    public void setMoveable(boolean moveable) {
        this.moveable = moveable;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getDrawWidth() {
        return drawWidth;
    }

    public void setDrawWidth(int drawWidth) {
        this.drawWidth = drawWidth;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CollectionColumn)) {
            return false;
        }

        CollectionColumn that = (CollectionColumn) o;

        if (maxWidth != that.maxWidth) {
            return false;
        }
        if (minWidth != that.minWidth) {
            return false;
        }
        if (moveable != that.moveable) {
            return false;
        }
        if (resizable != that.resizable) {
            return false;
        }
        if (fieldName != null ? !fieldName.equals(that.fieldName) : that.fieldName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = fieldName != null ? fieldName.hashCode() : 0;
        result = 31 * result + (resizable ? 1 : 0);
        result = 31 * result + minWidth;
        result = 31 * result + maxWidth;
        result = 31 * result + (moveable ? 1 : 0);
        return result;
    }
}
