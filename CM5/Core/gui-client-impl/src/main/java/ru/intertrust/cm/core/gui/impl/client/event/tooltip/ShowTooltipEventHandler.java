package ru.intertrust.cm.core.gui.impl.client.event.tooltip;

import com.google.gwt.event.shared.EventHandler;


/**
 * @author Yaroslav Bondarchuk
 *         Date: 02.10.2014
 *         Time: 7:18
 */
public interface ShowTooltipEventHandler extends EventHandler {
    void showTooltip(ShowTooltipEvent event);
}
