package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel;

import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionColumn;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.header.CollectionColumnHeader;

/**
 * Created by
 * Bondarchuk Yaroslav
 * 31.07.2014
 * 22:53
 */

public class ColumnHeaderBlock {
    private CollectionColumnHeader header;
    private CollectionColumn column;
    protected boolean shouldChangeVisibilityState;

    public ColumnHeaderBlock(CollectionColumnHeader header, CollectionColumn column) {
        this.header = header;
        this.column = column;
    }

    public CollectionColumnHeader getHeader() {
        return header;
    }

    public CollectionColumn getColumn() {
        return column;
    }

    public boolean shouldChangeVisibilityState() {
        return shouldChangeVisibilityState;
    }

    public void setShouldChangeVisibilityState(boolean shouldChangeVisibilityState) {
        this.shouldChangeVisibilityState = shouldChangeVisibilityState;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ColumnHeaderBlock block = (ColumnHeaderBlock) o;

        if (shouldChangeVisibilityState != block.shouldChangeVisibilityState) {
            return false;
        }

        if (column != null ? !column.equals(block.column) : block.column != null) {
            return false;
        }
        if (header != null ? !header.equals(block.header) : block.header != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = header != null ? header.hashCode() : 0;
        result = 31 * result + (column != null ? column.hashCode() : 0);
        result = 31 * result + (shouldChangeVisibilityState ? 1 : 0);
        return result;
    }
}
