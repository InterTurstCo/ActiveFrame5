package ru.intertrust.cm.core.gui.impl.client.event.hierarchybrowser;

import com.google.gwt.event.shared.EventHandler;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.01.14
 *         Time: 13:15
 */
public interface HierarchyBrowserHyperlinkStateUpdatedEventHandler extends EventHandler {
    void onHierarchyBrowserHyperlinkStateUpdatedEvent(HierarchyBrowserHyperlinkStateUpdatedEvent event);
}
