package ru.intertrust.cm.core.gui.impl.server;

import java.util.Calendar;
import java.util.TimeZone;

import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZone;
import ru.intertrust.cm.core.business.api.dto.TimelessDate;

/**
 * @author Sergey.Okolot
 *         Created on 07.03.14 17:27.
 */
public final class GuiServerHelper {

    public static final TimeZone GMT_TIME_ZONE = TimeZone.getTimeZone("GMT");

    /**
     * Don't create instance of helper class.
     */
    private GuiServerHelper() {}

    public static Calendar timelessDateToCalendar(final TimelessDate timelessDate, final TimeZone timeZone) {
        final Calendar calendar = Calendar.getInstance(timeZone);
        calendar.set(Calendar.YEAR, timelessDate.getYear());
        calendar.set(Calendar.MONTH, timelessDate.getMonth());
        calendar.set(Calendar.DAY_OF_MONTH, timelessDate.getDayOfMonth());
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    public static Calendar dateTimeWithTimezoneToCalendar(final DateTimeWithTimeZone dateTime) {
        final Calendar calendar =
                Calendar.getInstance(TimeZone.getTimeZone(dateTime.getTimeZoneContext().getTimeZoneId()));
        calendar.set(Calendar.YEAR, dateTime.getYear());
        calendar.set(Calendar.MONTH, dateTime.getMonth());
        calendar.set(Calendar.DAY_OF_MONTH, dateTime.getDayOfMonth());
        calendar.set(Calendar.HOUR, dateTime.getHours());
        calendar.set(Calendar.MINUTE, dateTime.getMinutes());
        calendar.set(Calendar.SECOND, dateTime.getSeconds());
        calendar.set(Calendar.MILLISECOND, dateTime.getMilliseconds());
        return calendar;
    }
}
