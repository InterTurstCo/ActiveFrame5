package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZone;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author vmatsukevich
 *         Date: 10/28/13
 *         Time: 11:54 AM
 */
public class DateUtils {

    public static Calendar getGMTDate(Date date) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.setTime(date);
        return calendar;
    }

    public static Calendar getGMTDate(DateTimeWithTimeZone dateTimeWithTimeZone) {
        TimeZone timeZone = getTimeZoneFromContext(dateTimeWithTimeZone);
        Calendar calendar = Calendar.getInstance(timeZone);

        calendar.set(Calendar.YEAR, dateTimeWithTimeZone.getYear());
        calendar.set(Calendar.MONTH, dateTimeWithTimeZone.getMonth());
        calendar.set(Calendar.DAY_OF_MONTH, dateTimeWithTimeZone.getDayOfMonth());
        calendar.set(Calendar.HOUR_OF_DAY, dateTimeWithTimeZone.getHour());
        calendar.set(Calendar.MINUTE, dateTimeWithTimeZone.getMinute());
        calendar.set(Calendar.SECOND, dateTimeWithTimeZone.getSecond());
        calendar.set(Calendar.MILLISECOND, dateTimeWithTimeZone.getMillisecond());

        Calendar gmtCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        gmtCalendar.setTime(calendar.getTime());
        return gmtCalendar;
    }

    public static String getTimeZoneId(DateTimeWithTimeZone dateTimeWithTimeZone) {
        String timeZoneId = null;
        DateTimeWithTimeZone.DateContext context = dateTimeWithTimeZone.getContext();

        if (context instanceof DateTimeWithTimeZone.UtcOffsetContext) {
            long timeZoneOffset = ((DateTimeWithTimeZone.UtcOffsetContext) context).getOffset();
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
        } else if (context instanceof DateTimeWithTimeZone.TimeZoneContext) {
            timeZoneId = ((DateTimeWithTimeZone.TimeZoneContext) context).getTimeZoneId();
        }

        return timeZoneId;
    }

    private static TimeZone getTimeZoneFromContext(DateTimeWithTimeZone dateTimeWithTimeZone) {
        return TimeZone.getTimeZone(getTimeZoneId(dateTimeWithTimeZone));
    }
}
