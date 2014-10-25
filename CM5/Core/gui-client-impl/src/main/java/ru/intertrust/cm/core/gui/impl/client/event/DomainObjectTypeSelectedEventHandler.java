package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.EventHandler;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 23.10.2014
 *         Time: 7:23
 */
public interface DomainObjectTypeSelectedEventHandler extends EventHandler {
    void onDomainObjectTypeSelected(DomainObjectTypeSelectedEvent event);
}
