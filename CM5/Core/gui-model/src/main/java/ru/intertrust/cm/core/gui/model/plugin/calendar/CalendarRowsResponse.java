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
    private Map<Date, List<CalendarItemData>> values;

    public CalendarRowsResponse() {
    }

    public CalendarRowsResponse(Map<Date, List<CalendarItemData>> values) {
        this.values = values;
    }

    public Map<Date, List<CalendarItemData>> getValues() {
        return values;
    }
}
