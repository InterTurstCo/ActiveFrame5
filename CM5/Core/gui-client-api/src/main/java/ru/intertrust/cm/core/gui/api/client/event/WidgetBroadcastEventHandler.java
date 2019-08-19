package ru.intertrust.cm.core.gui.api.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface WidgetBroadcastEventHandler extends EventHandler {
  void onEventReceived(WidgetBroadcastEvent e);
}
