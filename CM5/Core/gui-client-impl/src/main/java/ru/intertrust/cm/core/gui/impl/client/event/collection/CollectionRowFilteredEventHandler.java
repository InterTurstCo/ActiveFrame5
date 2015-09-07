package ru.intertrust.cm.core.gui.impl.client.event.collection;

import com.google.gwt.event.shared.EventHandler;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 07.07.2015
 *         Time: 9:33
 */
public interface CollectionRowFilteredEventHandler extends EventHandler {
    void onCollectionRowFilteredEvent(CollectionRowFilteredEvent event);
}
