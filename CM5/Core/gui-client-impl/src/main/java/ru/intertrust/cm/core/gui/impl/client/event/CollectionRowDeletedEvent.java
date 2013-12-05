package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 04.12.13
 * Time: 13:12
 * To change this template use File | Settings | File Templates.
 */
public class CollectionRowDeletedEvent extends GwtEvent<CollectionRowDeletedEventHandler> {

    public static Type<CollectionRowDeletedEventHandler> TYPE = new Type<CollectionRowDeletedEventHandler>();
    private Id id;

    public CollectionRowDeletedEvent(Id id) {
        this.id = id;
    }

    @Override
    public Type<CollectionRowDeletedEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(CollectionRowDeletedEventHandler handler) {
        handler.onCollectionRowDeleted(this);
    }

    public Id getId() {
        return id;
    }
}
