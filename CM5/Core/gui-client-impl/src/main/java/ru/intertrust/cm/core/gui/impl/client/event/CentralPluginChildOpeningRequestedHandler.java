package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.EventHandler;

/**
 * @author Sergey.Okolot
 *         Created on 24.01.14 10:10.
 */
public interface CentralPluginChildOpeningRequestedHandler extends EventHandler {

    void openChildPlugin(CentralPluginChildOpeningRequestedEvent event);
}
