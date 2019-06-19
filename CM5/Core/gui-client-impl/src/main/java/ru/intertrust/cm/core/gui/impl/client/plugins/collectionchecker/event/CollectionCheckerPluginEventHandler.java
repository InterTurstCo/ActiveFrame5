package ru.intertrust.cm.core.gui.impl.client.plugins.collectionchecker.event;

import com.google.gwt.event.shared.EventHandler;

public interface CollectionCheckerPluginEventHandler extends EventHandler {
  void dispatch(CollectionCheckerPluginEvent event);
}
