package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 02.12.13
 *         Time: 11:15
 */
public class CollectionRowDeletedEvent extends GwtEvent<CollectionRowDeletedEventHandler> {

    public static Type<CollectionRowDeletedEventHandler> TYPE = new Type<CollectionRowDeletedEventHandler>();
    private List<Id> ids;

    public CollectionRowDeletedEvent(List<Id> ids) {
        this.ids = ids;
    }

    public List<Id> getIds() {
        return ids;
    }

    public void setIds(List<Id> ids) {
        this.ids = ids;
    }

    @Override
    public Type<CollectionRowDeletedEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(CollectionRowDeletedEventHandler handler) {
        handler.onCollectionRowDeleted(this);
    }


}
