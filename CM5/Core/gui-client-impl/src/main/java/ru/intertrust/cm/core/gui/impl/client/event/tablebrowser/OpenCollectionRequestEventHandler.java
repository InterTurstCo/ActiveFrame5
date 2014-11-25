package ru.intertrust.cm.core.gui.impl.client.event.tablebrowser;

import com.google.gwt.event.shared.EventHandler;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 20.11.2014
 *         Time: 9:19
 */
public interface OpenCollectionRequestEventHandler extends EventHandler {
    void openCollectionView(OpenCollectionRequestEvent event);
}
