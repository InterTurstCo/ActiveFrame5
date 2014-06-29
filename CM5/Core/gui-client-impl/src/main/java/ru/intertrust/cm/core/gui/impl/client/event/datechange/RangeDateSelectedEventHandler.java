package ru.intertrust.cm.core.gui.impl.client.event.datechange;

import com.google.gwt.event.shared.EventHandler;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 23.06.2014
 *         Time: 1:13
 */
public interface RangeDateSelectedEventHandler extends EventHandler {

    void onRangeDateSelected(RangeDateSelectedEvent event);
}