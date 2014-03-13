package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 12/3/14
 *         Time: 12:05 PM
 */
public class CollectionPluginResizeBySplitterEvent extends GwtEvent<CollectionPluginResizeBySplitterEventHandler> {
    public static final Type<CollectionPluginResizeBySplitterEventHandler> TYPE = new Type<CollectionPluginResizeBySplitterEventHandler>();


    public CollectionPluginResizeBySplitterEvent() {

    }

    @Override
    public Type<CollectionPluginResizeBySplitterEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(CollectionPluginResizeBySplitterEventHandler handler) {
        handler.onCollectionPluginResizeBySplitter(this);
    }
}

