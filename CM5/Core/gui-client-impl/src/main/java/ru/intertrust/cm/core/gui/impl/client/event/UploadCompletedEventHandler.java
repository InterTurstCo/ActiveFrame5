package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.EventHandler;

/**
 * @author Lesia Puhova
 *         Date: 17.10.14
 *         Time: 16:31
 */
public interface UploadCompletedEventHandler extends EventHandler {

    void onUploadCompleted(UploadCompletedEvent event);
}
