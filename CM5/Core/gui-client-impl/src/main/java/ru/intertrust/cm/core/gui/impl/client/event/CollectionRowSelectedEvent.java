package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.business.api.dto.Id;

public class CollectionRowSelectedEvent extends GwtEvent<CollectionRowSelectedEventHandler> {

    public static Type<CollectionRowSelectedEventHandler> TYPE = new Type<CollectionRowSelectedEventHandler>();
    private Id id;
    private boolean deselected;

    public CollectionRowSelectedEvent(Id id, boolean deselected) {
        this.id = id;
        this.deselected = deselected;
    }

    @Override
    public Type<CollectionRowSelectedEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(CollectionRowSelectedEventHandler handler) {
        handler.onCollectionRowSelect(this);
    }

    public Id getId() {
        return id;
    }

    public boolean isDeselected() {
        return deselected;
    }
}
