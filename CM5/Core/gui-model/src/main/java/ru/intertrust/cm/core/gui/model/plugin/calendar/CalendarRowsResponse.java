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
    private Map<Date, List<? extends Dto>> values;

    public CalendarRowsResponse(Map<Date, List<? extends Dto>> values) {
        this.values = values;
    }

    public Map<Date, List<? extends Dto>> getValues() {
        return values;
    }
}
