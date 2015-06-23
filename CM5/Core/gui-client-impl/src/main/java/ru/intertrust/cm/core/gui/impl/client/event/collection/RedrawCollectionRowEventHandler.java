package ru.intertrust.cm.core.gui.impl.client.event.collection;

import com.google.gwt.event.shared.EventHandler;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 15.06.2015
 *         Time: 8:37
 */
public interface RedrawCollectionRowEventHandler extends EventHandler {
    void redrawCollectionRow(RedrawCollectionRowEvent event);
}
