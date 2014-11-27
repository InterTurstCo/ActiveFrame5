package ru.intertrust.cm.core.gui.impl.client.event.collection;

import com.google.gwt.event.shared.EventHandler;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 27.11.2014
 *         Time: 6:56
 */
public interface CollectionChangeSelectionEventHandler extends EventHandler {
    void changeCollectionSelection(CollectionChangeSelectionEvent event);
}
