package ru.intertrust.cm.core.gui.impl.server.util;

import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZone;
import ru.intertrust.cm.core.business.api.dto.OlsonTimeZoneContext;
import ru.intertrust.cm.core.business.api.dto.TimeZoneContext;
import ru.intertrust.cm.core.business.api.dto.UTCOffsetTimeZoneContext;
import ru.intertrust.cm.core.business.api.util.ModelUtil;
import ru.intertrust.cm.core.gui.api.server.GuiContext;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by User on 03.04.2014.
 */
public class DateUtil {
    /**
     * If date changed on client side the {@link ru.intertrust.cm.core.gui.model.DateTimeContext#timeZoneId} attribute is setting to selected
     * time zone id, otherwise the {@link ru.intertrust.cm.core.gui.model.DateTimeContext#timeZoneId} has origin value.
     *
     * @return actual value of timeZoneId.
     */
    public static String getTimeZoneId(String timeZoneId) {
        switch (timeZoneId) {
            case ModelUtil.LOCAL_TIME_ZONE_ID:
            case ModelUtil.DEFAULT_TIME_ZONE_ID:
            case ModelUtil.ORIGINAL_TIME_ZONE_ID:
                return GuiContext.get().getUserInfo().getTimeZoneId();
        }
        return timeZoneId;
    }

    public static TimeZone prepareTimeZone(String rawTimeZone) {
        String timeZoneId = prepareTimeZoneId(rawTimeZone);
        return TimeZone.getTimeZone(timeZoneId);
    }

    private static String prepareTimeZoneId(String rawTimeZone) {
        String timeZone = rawTimeZone == null ? ModelUtil.DEFAULT_TIME_ZONE_ID : rawTimeZone;
        return getTimeZoneId(timeZone);
    }

    public static DateTimeWithTimeZone prepareDateTimeWithTimeZone(Date date, String rawTimeZone, TimeZone timeZone) {
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setTime(date);
        DateTimeWithTimeZone dateTimeWithTimeZone = new DateTimeWithTimeZone();
        dateTimeWithTimeZone.setYear(calendar.get(Calendar.YEAR));
        dateTimeWithTimeZone.setMonth(calendar.get(Calendar.MONTH));
        dateTimeWithTimeZone.setDayOfMonth(calendar.get(Calendar.DAY_OF_MONTH));
        dateTimeWithTimeZone.setHours(calendar.get(Calendar.HOUR_OF_DAY));
        dateTimeWithTimeZone.setMinutes(calendar.get(Calendar.MINUTE));
        dateTimeWithTimeZone.setSeconds(calendar.get(Calendar.SECOND));
        dateTimeWithTimeZone.setMilliseconds(calendar.get(Calendar.MILLISECOND));
        TimeZoneContext timeZoneContext = prepareTimeZoneContext(rawTimeZone, timeZone);
        dateTimeWithTimeZone.setTimeZoneContext(timeZoneContext);
        return dateTimeWithTimeZone;
    }

    public static TimeZoneContext prepareTimeZoneContext(String rawTimeZone, TimeZone timeZone) {
        String timeZoneId = prepareTimeZoneId(rawTimeZone);
        if (timeZoneId.startsWith("GMT")) {
            return new UTCOffsetTimeZoneContext(timeZone.getRawOffset());
        } else {
            return new OlsonTimeZoneContext(timeZoneId);
        }
    }

    public static DateFormat getDateFormat(String datePattern, String timePattern) {
        if (datePattern == null) {
            return null;
        }
        if (timePattern == null) {
            return new SimpleDateFormat(datePattern);
        }
        StringBuilder patternBuilder = new StringBuilder(datePattern);
        patternBuilder.append(" ");
        patternBuilder.append(timePattern);
        return new SimpleDateFormat(patternBuilder.toString());
    }

}
