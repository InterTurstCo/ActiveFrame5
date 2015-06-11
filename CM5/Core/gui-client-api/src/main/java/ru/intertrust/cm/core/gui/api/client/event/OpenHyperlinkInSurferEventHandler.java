package ru.intertrust.cm.core.gui.api.client.event;

import com.google.gwt.event.shared.EventHandler;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 11.06.2015
 *         Time: 0:35
 */
public interface OpenHyperlinkInSurferEventHandler extends EventHandler {
    void onOpenHyperlinkInSurfer(OpenHyperlinkInSurferEvent event);
}
