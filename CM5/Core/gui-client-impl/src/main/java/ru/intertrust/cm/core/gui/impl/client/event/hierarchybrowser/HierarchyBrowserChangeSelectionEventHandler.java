package ru.intertrust.cm.core.gui.impl.client.event.hierarchybrowser;

import com.google.gwt.event.shared.EventHandler;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 18.12.2014
 *         Time: 8:01
 */
public interface HierarchyBrowserChangeSelectionEventHandler extends EventHandler {
    void onChangeSelectionEvent(HierarchyBrowserChangeSelectionEvent event);
}
