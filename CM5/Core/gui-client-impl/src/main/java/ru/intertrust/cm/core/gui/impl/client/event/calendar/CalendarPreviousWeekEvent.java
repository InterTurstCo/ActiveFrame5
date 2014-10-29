package ru.intertrust.cm.core.gui.impl.client.event.calendar;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author Sergey.Okolot
 *         Created on 29.10.2014 16:14.
 */
public class CalendarPreviousWeekEvent extends GwtEvent<CalendarPreviousWeekEventHandler> {
    public static final Type<CalendarPreviousWeekEventHandler> TYPE = new Type<>();

    @Override
    public Type<CalendarPreviousWeekEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(CalendarPreviousWeekEventHandler handler) {
        handler.goToPreviousWeek();
    }
}
