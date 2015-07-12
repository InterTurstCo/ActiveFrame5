package ru.intertrust.cm.core.gui.impl.client.event.collection;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 07.07.2015
 *         Time: 22:00
 */
public class CollectionRowMoreItemsEvent extends GwtEvent<CollectionRowMoreItemsEventHandler> {

    public static final Type<CollectionRowMoreItemsEventHandler> TYPE = new Type<CollectionRowMoreItemsEventHandler>();
    private CollectionRowItem effectedRowItem;
    public CollectionRowMoreItemsEvent(CollectionRowItem effectedRowItem) {
        this.effectedRowItem = effectedRowItem;
    }

    @Override
    public Type<CollectionRowMoreItemsEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(CollectionRowMoreItemsEventHandler handler) {
        handler.onMoreItems(this);
    }

    public CollectionRowItem getEffectedRowItem() {
        return effectedRowItem;
    }

}
