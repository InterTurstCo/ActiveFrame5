package ru.intertrust.cm.core.gui.impl.client.event.calendar;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author Sergey.Okolot
 *         Created on 29.10.2014 16:18.
 */
public class CalendarNextWeekEvent extends GwtEvent<CalendarNextWeekEventHandler> {
    public static final Type<CalendarNextWeekEventHandler> TYPE = new Type<>();

    @Override
    public Type<CalendarNextWeekEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(CalendarNextWeekEventHandler handler) {
        handler.goToNextWeek();
    }
}
