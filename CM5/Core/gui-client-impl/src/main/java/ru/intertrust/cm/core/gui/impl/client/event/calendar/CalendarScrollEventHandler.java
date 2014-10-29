package ru.intertrust.cm.core.gui.impl.client.event.calendar;

import java.util.Date;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Sergey.Okolot
 *         Created on 24.10.2014 12:32.
 */
public interface CalendarScrollEventHandler extends EventHandler {

    void scrollTo(Widget source, Date date);
}
