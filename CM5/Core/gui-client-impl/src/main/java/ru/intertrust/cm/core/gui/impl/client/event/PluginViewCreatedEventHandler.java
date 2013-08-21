package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.EventHandler;

/**
 * @author Denis Mitavskiy
 *         Date: 20.08.13
 *         Time: 18:19
 */
public interface PluginViewCreatedEventHandler extends EventHandler {
    void onPluginViewCreated(PluginViewCreatedEvent event);
}
