package ru.intertrust.cm.core.gui.impl.client.plugins.navigation;

import com.google.gwt.event.shared.EventHandler;

interface RootNodeSelectedEventHandler extends EventHandler {
    void onRootNodeSelected(RootLinkSelectedEvent event);
}
