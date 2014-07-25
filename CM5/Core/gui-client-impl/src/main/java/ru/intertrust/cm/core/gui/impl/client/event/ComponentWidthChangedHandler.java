package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.EventHandler;

/**
 * @author Sergey.Okolot
 *         Created on 24.07.2014 16:04.
 */
public interface ComponentWidthChangedHandler extends EventHandler {

    void handleEvent(ComponentWidthChangedEvent event);
}
