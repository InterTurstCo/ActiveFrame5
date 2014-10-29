package ru.intertrust.cm.core.gui.model.plugin.calendar;

import java.util.Date;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.navigation.calendar.CalendarConfig;

/**
 * @author Sergey.Okolot
 *         Created on 16.10.2014 16:19.
 */
public class CalendarRowsRequest implements Dto {

    private CalendarConfig calendarConfig;
    private Date fromDate;
    private Date toDate;

    public CalendarRowsRequest() {
    }

    public CalendarRowsRequest(CalendarConfig calendarConfig, Date fromDate, Date toDate) {
        this.calendarConfig = calendarConfig;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public CalendarConfig getCalendarConfig() {
        return calendarConfig;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public Date getToDate() {
        return toDate;
    }
}
