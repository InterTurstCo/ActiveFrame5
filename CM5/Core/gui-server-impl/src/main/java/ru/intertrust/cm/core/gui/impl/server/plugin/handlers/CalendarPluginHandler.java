package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.calendar.CalendarData;
import ru.intertrust.cm.core.gui.model.plugin.calendar.CalendarPluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.calendar.CalendarPluginData;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;
import ru.intertrust.cm.core.gui.model.plugin.calendar.CalendarRowsRequest;
import ru.intertrust.cm.core.gui.model.plugin.calendar.CalendarRowsResponse;

/**
 * Created by lvov on 03.04.14.
 */
@ComponentName(CalendarPluginData.COMPONENT_NAME)
public class CalendarPluginHandler extends PluginHandler {

    @Override
    public PluginData initialize(Dto param) {
//        final CalendarPluginConfig pluginConfig = (CalendarPluginConfig) param;
        final CalendarPluginData result = new CalendarPluginData();
        final Calendar calendar = GregorianCalendar.getInstance();
        result.setSelectedDate(calendar.getTime());
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
        result.setToDate(calendar.getTime());
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 2);
        result.setFromDate(calendar.getTime());
        final Map<Date, List<? extends Dto>> values = new HashMap<>();
        for (; calendar.getTime().before(result.getToDate()); calendar.add(Calendar.DAY_OF_MONTH, 3)) {
            final Date date = calendar.getTime();
            final List<CalendarData> dateData = getDataByDate(date);
            values.put(date, dateData);
        }
        result.setValues(values);
        return result;
    }

    public CalendarRowsResponse requestRows(Dto dto) {
        final CalendarRowsRequest request = (CalendarRowsRequest) dto;
        final  Calendar calendar = GregorianCalendar.getInstance();
        final Map<Date, List<? extends Dto>> values = new HashMap<>();
        for (calendar.setTime(request.getFromDate()); calendar.getTime().before(request.getToDate()); calendar.add(Calendar.DAY_OF_MONTH, 3)) {
            final Date date = calendar.getTime();
            final List<CalendarData> dateData = getDataByDate(date);
            values.put(date, dateData);
        }
        return new CalendarRowsResponse(values);
    }

    private List<CalendarData> getDataByDate(final Date date) {
        final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
        final List<CalendarData> result = new ArrayList<>();
        for (int index = 1; index < 10; index++) {
            CalendarData calendarData = new CalendarData("task " + index + ": " + dateFormat.format(date),
                    "description of task " + index + ": " + dateFormat.format(date));
            result.add(calendarData);
        }
        return result;
    }
}
