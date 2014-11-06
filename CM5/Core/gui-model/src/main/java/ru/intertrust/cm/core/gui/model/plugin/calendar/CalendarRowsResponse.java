package ru.intertrust.cm.core.gui.model.plugin.calendar;

import java.util.Date;
import java.util.List;
import java.util.Map;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Sergey.Okolot
 *         Created on 16.10.2014 16:29.
 */
public class CalendarRowsResponse implements Dto {
    private Map<Date, List<CalendarItemsData>> values;
    private Date from;
    private Date to;

    public CalendarRowsResponse() {
    }

    public CalendarRowsResponse(final Map<Date, List<CalendarItemsData>> values, final Date from, final Date to) {
        this.values = values;
        this.from = from;
        this.to = to;
    }

    public Map<Date, List<CalendarItemsData>> getValues() {
        return values;
    }

    public Date getFrom() {
        return from;
    }

    public Date getTo() {
        return to;
    }
}
