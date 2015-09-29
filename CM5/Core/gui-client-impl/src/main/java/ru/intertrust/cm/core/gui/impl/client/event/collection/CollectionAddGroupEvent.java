package ru.intertrust.cm.core.gui.impl.client.event.collection;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;

/**
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 22.09.2015
 */
public class CollectionAddGroupEvent extends GwtEvent<CollectionAddGroupEventHandler> {
    public static final Type<CollectionAddGroupEventHandler> TYPE = new Type<CollectionAddGroupEventHandler>();
    private CollectionRowItem effectedRowItem;
    private  boolean expanded;
    public CollectionAddGroupEvent(CollectionRowItem effectedRowItem, boolean expanded) {
        this.effectedRowItem = effectedRowItem;
        this.expanded = expanded;
    }

    @Override
    public Type<CollectionAddGroupEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(CollectionAddGroupEventHandler handler) {
        handler.onCollectionAddGroup(this);
    }


    public CollectionRowItem getEffectedRowItem() {
        return effectedRowItem;
    }

    public boolean isExpanded() {
        return expanded;
    }
}
