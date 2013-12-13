package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.SortCollectionState;

/**
 * Created with IntelliJ IDEA.
 * User: lvov
 * Date: 11.12.13
 * Time: 14:15
 * To change this template use File | Settings | File Templates.
 */
public class TableControllerSortEvent extends GwtEvent<TableControllerSortEventHandler> {
    public static final Type<TableControllerSortEventHandler> TYPE = new Type<TableControllerSortEventHandler>();
    private String columnName;
    private SortCollectionState sortCollectionState;


    public TableControllerSortEvent(String columnName, SortCollectionState sortCollectionState) {
        this.columnName = columnName;
        this.sortCollectionState = sortCollectionState;
    }



    @Override
    public Type<TableControllerSortEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(TableControllerSortEventHandler handler) {

        handler.setSort(this);
    }

    public static Type<TableControllerSortEventHandler> getType() {
        return TYPE;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public SortCollectionState getSortCollectionState() {
        return sortCollectionState;
    }

    public void setSortCollectionState(SortCollectionState sortCollectionState) {
        this.sortCollectionState = sortCollectionState;
    }
}
