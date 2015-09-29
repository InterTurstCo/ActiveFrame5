package ru.intertrust.cm.core.gui.impl.client.event.collection;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;

/**
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 22.09.2015
 */
public class CollectionAddElementEvent extends GwtEvent<CollectionAddElementEventHandler> {
    public static final Type<CollectionAddElementEventHandler> TYPE = new Type<CollectionAddElementEventHandler>();
    private CollectionRowItem effectedRowItem;
    private  boolean expanded;
    public CollectionAddElementEvent(CollectionRowItem effectedRowItem, boolean expanded) {
        this.effectedRowItem = effectedRowItem;
        this.expanded = expanded;
    }

    @Override
    public Type<CollectionAddElementEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(CollectionAddElementEventHandler handler) {
        handler.onCollectionAddElement(this);
    }


    public CollectionRowItem getEffectedRowItem() {
        return effectedRowItem;
    }

    public boolean isExpanded() {
        return expanded;
    }
}
