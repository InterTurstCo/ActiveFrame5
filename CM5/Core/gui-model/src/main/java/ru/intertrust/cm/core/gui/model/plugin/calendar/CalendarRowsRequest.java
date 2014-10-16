package ru.intertrust.cm.core.gui.model.plugin.calendar;

import java.util.Date;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Sergey.Okolot
 *         Created on 16.10.2014 16:19.
 */
public class CalendarRowsRequest implements Dto {

    private Date fromDate;
    private Date toDate;

    public CalendarRowsRequest(Date fromDate, Date toDate) {
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public Date getToDate() {
        return toDate;
    }
}
