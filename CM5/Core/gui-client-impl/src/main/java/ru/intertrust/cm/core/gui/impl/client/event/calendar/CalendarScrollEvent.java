package ru.intertrust.cm.core.gui.impl.client.event.calendar;

import java.util.Date;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Sergey.Okolot
 *         Created on 24.10.2014 12:32.
 */
public class CalendarScrollEvent extends GwtEvent<CalendarScrollEventHandler> {

    public static final Type<CalendarScrollEventHandler> TYPE = new Type<>();

    private final Date date;
    private final Widget source;

    public CalendarScrollEvent(final Widget source, final Date date) {
        this.source = source;
        this.date = date;
    }

    @Override
    public Type<CalendarScrollEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(CalendarScrollEventHandler handler) {
        handler.scrollTo(source, date);
    }
}
