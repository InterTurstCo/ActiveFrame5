package ru.intertrust.cm.core.gui.impl.client.event.datechange;

import com.google.gwt.event.shared.GwtEvent;

import java.util.Date;


/**
 * @author Yaroslav Bondarchuk
 *         Date: 15.06.2014
 *         Time: 23:12
 */
public class DateSelectedEvent extends GwtEvent<DateSelectedEventHandler> {
    public static final Type<DateSelectedEventHandler> TYPE = new Type<DateSelectedEventHandler>();
    private Date date;

    public DateSelectedEvent(Date date) {
        this.date = date;
    }

    @Override
    public Type<DateSelectedEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(DateSelectedEventHandler handler) {
        handler.onDateSelected(this);

    }

    public Date getDate() {
        return date;
    }

    public void kill() {
        super.kill();
    }

    public boolean isDead(){
        return !super.isLive();
    }
}
