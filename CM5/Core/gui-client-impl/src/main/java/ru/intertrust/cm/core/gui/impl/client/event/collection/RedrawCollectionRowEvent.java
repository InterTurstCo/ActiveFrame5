package ru.intertrust.cm.core.gui.impl.client.event.collection;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 15.06.2015
 *         Time: 8:36
 */
public class RedrawCollectionRowEvent  extends GwtEvent<RedrawCollectionRowEventHandler> {

    public static final Type<RedrawCollectionRowEventHandler> TYPE = new Type<RedrawCollectionRowEventHandler>();
    private CollectionRowItem rootRowItem;
    private CollectionRowItem effectedRowItem;
    private  boolean expanded;
    public RedrawCollectionRowEvent(CollectionRowItem rootRowItem, CollectionRowItem effectedRowItem, boolean expanded) {
        this.rootRowItem = rootRowItem;
        this.effectedRowItem = effectedRowItem;
        this.expanded = expanded;
    }

    @Override
    public Type<RedrawCollectionRowEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(RedrawCollectionRowEventHandler handler) {
        handler.redrawCollectionRow(this);
    }

    public CollectionRowItem getRootRowItem() {
        return rootRowItem;
    }

    public CollectionRowItem getEffectedRowItem() {
        return effectedRowItem;
    }

    public boolean isExpanded() {
        return expanded;
    }
}