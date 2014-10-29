package ru.intertrust.cm.core.gui.model.plugin.calendar;

import java.util.Date;
import java.util.List;
import java.util.Map;

import ru.intertrust.cm.core.gui.model.plugin.ActivePluginData;

/**
 * Created by lvov on 03.04.14.
 */
public class CalendarPluginData extends ActivePluginData {

    private Date fromDate;
    private Date toDate;
    private Date selectedDate;

    private Map<Date, List<CalendarItemData>> values;

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public Date getSelectedDate() {
        return selectedDate == null ? new Date() : selectedDate;
    }

    public void setSelectedDate(Date selectedDate) {
        this.selectedDate = selectedDate;
    }

    public Map<Date, List<CalendarItemData>> getValues() {
        return values;
    }

    public void setValues(Map<Date, List<CalendarItemData>> values) {
        this.values = values;
    }
}
