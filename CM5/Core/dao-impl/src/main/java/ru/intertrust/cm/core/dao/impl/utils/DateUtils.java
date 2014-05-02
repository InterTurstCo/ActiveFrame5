package ru.intertrust.cm.core.dao.impl.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZone;
import ru.intertrust.cm.core.business.api.dto.OlsonTimeZoneContext;
import ru.intertrust.cm.core.business.api.dto.TimeZoneContext;
import ru.intertrust.cm.core.business.api.dto.TimelessDate;
import ru.intertrust.cm.core.business.api.dto.UTCOffsetTimeZoneContext;

/**
 * @author vmatsukevich
 *         Date: 10/28/13
 *         Time: 11:54 AM
 */
public class DateUtils {

    private static ThreadLocal<Map<String, Calendar>>  calendarCache = new ThreadLocal<Map<String, Calendar>>();
    
    public static Calendar getCalendar(String timeZoneId) {
        if (calendarCache.get() != null) {
            Map<String, Calendar> timeZoneIdToCalendar = calendarCache.get();
            if (timeZoneIdToCalendar.get(timeZoneId) != null) {
                return timeZoneIdToCalendar.get(timeZoneId);
            } else {
                Calendar calendar = createNewCalendar(timeZoneId);
                timeZoneIdToCalendar.put(timeZoneId, calendar);
                return calendar;
            }

        } else {
            Map<String, Calendar> timezoneIdToCalendar = new HashMap<>();
            calendarCache.set(timezoneIdToCalendar);
            Calendar calendar = createNewCalendar(timeZoneId);
            timezoneIdToCalendar.put(timeZoneId, calendar);
            return calendar;
        }
    }

    private static Calendar createNewCalendar(String timeZoneId) {
        TimeZone timeZone = TimeZone.getTimeZone(timeZoneId);
        Calendar calendar = Calendar.getInstance(timeZone);
        return calendar;
    }

    public static Calendar getGMTDate(Date date) {
        Calendar calendar = getCalendar("GMT");
        calendar.setTime(date);
        return calendar;
    }

    public static Calendar getGMTDate(DateTimeWithTimeZone dateTimeWithTimeZone) {
        String timeZoneId = getTimeZoneId(dateTimeWithTimeZone);
                
        Calendar calendar = getCalendar(timeZoneId);
        calendar.set(Calendar.YEAR, dateTimeWithTimeZone.getYear());
        calendar.set(Calendar.MONTH, dateTimeWithTimeZone.getMonth());
        calendar.set(Calendar.DAY_OF_MONTH, dateTimeWithTimeZone.getDayOfMonth());
        calendar.set(Calendar.HOUR_OF_DAY, dateTimeWithTimeZone.getHours());
        calendar.set(Calendar.MINUTE, dateTimeWithTimeZone.getMinutes());
        calendar.set(Calendar.SECOND, dateTimeWithTimeZone.getSeconds());
        calendar.set(Calendar.MILLISECOND, dateTimeWithTimeZone.getMilliseconds());

        Calendar gmtCalendar = getCalendar("GMT");
        gmtCalendar.setTime(calendar.getTime());
        return gmtCalendar;
    }

    public static Calendar getGMTDate(TimelessDate timelessDate) {
        Calendar calendar = getCalendar("GMT");
        calendar.set(Calendar.YEAR, timelessDate.getYear());
        calendar.set(Calendar.MONTH, timelessDate.getMonth());
        calendar.set(Calendar.DAY_OF_MONTH, timelessDate.getDayOfMonth());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar;
    }

    @Deprecated
    public static String getTimeZoneId(DateTimeWithTimeZone dateTimeWithTimeZone) {
        String timeZoneId = null;
        TimeZoneContext context = dateTimeWithTimeZone.getTimeZoneContext();

        if (context instanceof UTCOffsetTimeZoneContext) {
            long timeZoneOffset = ((UTCOffsetTimeZoneContext) context).getOffset();
            if (timeZoneOffset == 0) {
                timeZoneId = "GMT";
            } else {
                long timeZoneOffsetInHours = timeZoneOffset/3600000;
                if (timeZoneOffsetInHours > 0) {
                    timeZoneId = "GMT+" + timeZoneOffsetInHours;
                } else {
                    timeZoneId = "GMT-" + timeZoneOffsetInHours;
                }
            }
        } else if (context instanceof OlsonTimeZoneContext) {
            timeZoneId = ((OlsonTimeZoneContext) context).getTimeZoneId();
        }

        return timeZoneId;
    }

    private static TimeZone getTimeZoneFromContext(DateTimeWithTimeZone dateTimeWithTimeZone) {
        return TimeZone.getTimeZone(getTimeZoneId(dateTimeWithTimeZone));
    }
}
