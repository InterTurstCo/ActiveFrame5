package ru.intertrust.cm.core.gui.impl.server.util;

import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.util.ModelUtil;
import ru.intertrust.cm.core.business.api.util.ThreadSafeDateFormat;
import ru.intertrust.cm.core.gui.api.server.GuiContext;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 13.09.2014
 *         Time: 12:33
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
            case "По умолчанию":
            case "Локальная":
            case "Оригинальная":
                return GuiContext.get().getUserInfo().getTimeZoneId();
        }
        return TimeZone.getTimeZone(timeZoneId).getID();
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
        return new DateTimeWithTimeZone(
            prepareTimeZoneContext(rawTimeZone, timeZone),
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            calendar.get(Calendar.SECOND),
            calendar.get(Calendar.MILLISECOND)
        );
    }

    public static TimeZoneContext prepareTimeZoneContext(String rawTimeZone, TimeZone timeZone) {
        String timeZoneId = prepareTimeZoneId(rawTimeZone);
        if (timeZoneId.startsWith("GMT")) {
            return new UTCOffsetTimeZoneContext(timeZone.getRawOffset());
        } else {
            return new OlsonTimeZoneContext(timeZoneId);
        }
    }
    @Deprecated //use the same method GuiServerHelper
    public static DateFormat getDateFormat(String datePattern, String timePattern) {
        String formatDatePattern = prepareDatePattern(datePattern, timePattern);
        return formatDatePattern == null ? null : ThreadSafeDateFormat.getDateFormat(new Pair<String, Locale>(formatDatePattern, null), null);
    }
    @Deprecated //use the same method GuiServerHelper
    public static String prepareDatePattern(String datePattern, String timePattern){
        if (datePattern == null) {
            return null;
        }
        if (timePattern == null) {
            return datePattern;
        }
        StringBuilder patternBuilder = new StringBuilder(datePattern);
        patternBuilder.append(" ");
        patternBuilder.append(timePattern);
        return patternBuilder.toString();
    }
}
