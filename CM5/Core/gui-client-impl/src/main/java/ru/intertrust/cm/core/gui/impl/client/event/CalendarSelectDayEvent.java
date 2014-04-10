package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.gui.impl.client.plugins.calendar.CalendarDayView;


/**
 * Created by lvov on 04.04.14.
 */
public class CalendarSelectDayEvent extends GwtEvent<CalendarSelectDayEventHandler> {
    public static final  Type<CalendarSelectDayEventHandler> TYPE = new Type<CalendarSelectDayEventHandler>();
    private CalendarDayView calendarDayView;

    public CalendarSelectDayEvent(CalendarDayView calendarDayView) {
        this.calendarDayView = calendarDayView;
    }

    @Override
    public Type<CalendarSelectDayEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(CalendarSelectDayEventHandler handler) {
        handler.selectDay(this);

    }

    public CalendarDayView getCalendarDayView() {
        return calendarDayView;
    }

    public void setCalendarDayView(CalendarDayView calendarDayView) {
        this.calendarDayView = calendarDayView;
    }
}
