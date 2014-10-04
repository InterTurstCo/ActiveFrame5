package ru.intertrust.cm.core.gui.impl.client.event.linkedtable;

import com.google.gwt.event.shared.EventHandler;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 04.10.2014
 *         Time: 18:06
 */
public interface LinkedTableRowDeletedEventHandler extends EventHandler {
    void onLinkedTableRowDeletedEvent(LinkedTableRowDeletedEvent event);
}
