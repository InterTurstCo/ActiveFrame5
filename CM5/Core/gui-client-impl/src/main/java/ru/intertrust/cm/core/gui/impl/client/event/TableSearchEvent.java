package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionSearchBox;

/**
 * Created with IntelliJ IDEA.
 * User: lvov
 * Date: 23.12.13
 * Time: 13:42
 * To change this template use File | Settings | File Templates.
 */
public class TableSearchEvent extends GwtEvent<TableSearchEventHandler> {
    public static final Type<TableSearchEventHandler> TYPE = new Type<TableSearchEventHandler>();

    CollectionSearchBox box;

    public TableSearchEvent(CollectionSearchBox box) {
        this.box = box;
    }

    @Override
    public Type<TableSearchEventHandler> getAssociatedType() {
        return TYPE;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void dispatch(TableSearchEventHandler handler) {
        handler.searchByFields(this);
    }



    public CollectionSearchBox getBox() {
        return box;
    }

    public void setBox(CollectionSearchBox box) {
        this.box = box;
    }
}
