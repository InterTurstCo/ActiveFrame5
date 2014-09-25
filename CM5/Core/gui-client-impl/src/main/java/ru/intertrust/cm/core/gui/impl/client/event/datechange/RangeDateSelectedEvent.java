package ru.intertrust.cm.core.gui.impl.client.event.datechange;

import com.google.gwt.event.shared.GwtEvent;

import java.util.Date;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 23.06.2014
 *         Time: 1:13
 */
public class RangeDateSelectedEvent extends GwtEvent<RangeDateSelectedEventHandler> {
    public static final  Type<RangeDateSelectedEventHandler> TYPE = new Type<RangeDateSelectedEventHandler>();
    private Date startDate;
    private Date endDate;
    public RangeDateSelectedEvent(Date startDate, Date endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public Type<RangeDateSelectedEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(RangeDateSelectedEventHandler handler) {
        handler.onRangeDateSelected(this);

    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

}
