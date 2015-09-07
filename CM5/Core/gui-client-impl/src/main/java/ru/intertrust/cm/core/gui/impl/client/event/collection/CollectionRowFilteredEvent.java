package ru.intertrust.cm.core.gui.impl.client.event.collection;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 07.07.2015
 *         Time: 9:32
 */
public class CollectionRowFilteredEvent extends GwtEvent<CollectionRowFilteredEventHandler> {

    public static final Type<CollectionRowFilteredEventHandler> TYPE = new Type<CollectionRowFilteredEventHandler>();
    private CollectionRowItem effectedRowItem;
    public CollectionRowFilteredEvent(CollectionRowItem effectedRowItem) {
        this.effectedRowItem = effectedRowItem;
    }

    @Override
    public Type<CollectionRowFilteredEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(CollectionRowFilteredEventHandler handler) {
        handler.onCollectionRowFilteredEvent(this);
    }

    public CollectionRowItem getEffectedRowItem() {
        return effectedRowItem;
    }

}
