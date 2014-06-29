package ru.intertrust.cm.core.gui.impl.client.event.datechange;

import com.google.gwt.event.shared.EventHandler;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 15.06.2014
 *         Time: 23:12
 */
public interface DateSelectedEventHandler extends EventHandler {

    void onDateSelected(DateSelectedEvent event);
}
