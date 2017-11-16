package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Created by Ravil on 16.11.2017.
 */
public class CollectionViewerUpdatedEvent extends GwtEvent<CollectionViewerUpdatedEventHandler> {
    private int rows;
    public static final Type<CollectionViewerUpdatedEventHandler> TYPE = new Type();

    public CollectionViewerUpdatedEvent(int rows){
        this.rows = rows;
    }

    @Override
    public Type<CollectionViewerUpdatedEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(CollectionViewerUpdatedEventHandler collectionViewerUpdatedEventHandler) {
        collectionViewerUpdatedEventHandler.collectionViewerUpdated(this);
    }

    public int getRows() {
        return rows;
    }
}
