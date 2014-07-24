package ru.intertrust.cm.core.gui.impl.client.event.datechange;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.config.gui.form.widget.datebox.RangeEndConfig;
import ru.intertrust.cm.core.config.gui.form.widget.datebox.RangeStartConfig;

import java.util.Date;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 24.07.2014
 *         Time: 0:44
 */
public class FormRangeDateSelectedEvent extends GwtEvent<FormRangeDateSelectedEventHandler> {
    public static final Type<FormRangeDateSelectedEventHandler> TYPE = new Type<FormRangeDateSelectedEventHandler>();
    private Date startDate;
    private Date endDate;
    private RangeStartConfig rangeStartConfig;
    private RangeEndConfig rangeEndConfig;

    public FormRangeDateSelectedEvent(Date startDate, Date endDate, RangeStartConfig rangeStartConfig,
                                      RangeEndConfig rangeEndConfig) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.rangeStartConfig = rangeStartConfig;
        this.rangeEndConfig = rangeEndConfig;
    }

    @Override
    public Type<FormRangeDateSelectedEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(FormRangeDateSelectedEventHandler handler) {
        handler.onFormRangeDateSelected(this);

    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public RangeStartConfig getRangeStartConfig() {
        return rangeStartConfig;
    }

    public RangeEndConfig getRangeEndConfig() {
        return rangeEndConfig;
    }
}
