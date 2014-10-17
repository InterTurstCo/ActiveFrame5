package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.EventHandler;

/**
 * @author Lesia Puhova
 *         Date: 17.10.14
 *         Time: 13:47
 */
public interface UploadUpdatedEventHandler extends EventHandler {

    void onPercentageUpdated(UploadUpdatedEvent event);
}
