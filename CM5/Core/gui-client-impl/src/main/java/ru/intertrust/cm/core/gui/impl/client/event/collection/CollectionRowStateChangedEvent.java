package ru.intertrust.cm.core.gui.impl.client.event.collection;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 15.06.2015
 *         Time: 8:36
 */
public class CollectionRowStateChangedEvent extends GwtEvent<CollectionRowStateChangedEventHandler> {

    public static final Type<CollectionRowStateChangedEventHandler> TYPE = new Type<CollectionRowStateChangedEventHandler>();
    private CollectionRowItem effectedRowItem;
    private  boolean expanded;
    public CollectionRowStateChangedEvent(CollectionRowItem effectedRowItem, boolean expanded) {
        this.effectedRowItem = effectedRowItem;
        this.expanded = expanded;
    }

    @Override
    public Type<CollectionRowStateChangedEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(CollectionRowStateChangedEventHandler handler) {
        handler.onCollectionRowStateChanged(this);
    }


    public CollectionRowItem getEffectedRowItem() {
        return effectedRowItem;
    }

    public boolean isExpanded() {
        return expanded;
    }

}