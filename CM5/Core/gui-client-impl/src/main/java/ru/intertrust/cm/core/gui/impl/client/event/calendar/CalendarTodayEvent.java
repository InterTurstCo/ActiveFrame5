package ru.intertrust.cm.core.gui.impl.client.event.calendar;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author Sergey.Okolot
 *         Created on 29.10.2014 16:47.
 */
public class CalendarTodayEvent extends GwtEvent<CalendarTodayEventHandler> {
    public static final Type<CalendarTodayEventHandler> TYPE = new Type<>();

    @Override
    public Type<CalendarTodayEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(CalendarTodayEventHandler handler) {
        handler.goToToday();
    }
}
