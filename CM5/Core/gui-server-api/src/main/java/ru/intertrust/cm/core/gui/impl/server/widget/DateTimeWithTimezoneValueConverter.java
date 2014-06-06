package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.util.ModelUtil;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.GuiServerHelper;
import ru.intertrust.cm.core.gui.model.DateTimeContext;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 19.05.14
 *         Time: 17:15
 */
public class DateTimeWithTimezoneValueConverter implements DateValueConverter<DateTimeWithTimeZoneValue> {
    @Override
    public DateTimeContext valueToContext(DateTimeWithTimeZoneValue value, String timeZoneId, DateFormat dateFormat) {
        final DateTimeContext result = new DateTimeContext();
        result.setOrdinalFieldType(FieldType.DATETIMEWITHTIMEZONE.ordinal());
        if (value != null && value.get() != null) {
            final Calendar calendar = GuiServerHelper.dateTimeWithTimezoneToCalendar(value.get());

            final String userTimeZoneId = GuiContext.get().getUserInfo().getTimeZoneId();
            dateFormat.setTimeZone(TimeZone.getTimeZone(userTimeZoneId));
            result.setDateTime(dateFormat.format(calendar.getTime()));
            result.setTimeZoneId(value.get().getTimeZoneContext().getTimeZoneId());
        }
        return result;
    }

    @Override
    public DateTimeWithTimeZoneValue contextToValue(final DateTimeContext context) {
        if (context.getDateTime() != null) {
            final String userTimeZoneId = GuiContext.get().getUserInfo().getTimeZoneId();
            final DateFormat dateFormat = new SimpleDateFormat(ModelUtil.DTO_PATTERN);
            dateFormat.setTimeZone(TimeZone.getTimeZone(userTimeZoneId));
            try {
                final Date date = dateFormat.parse(context.getDateTime());
                final String rawTimeZoneId = context.getTimeZoneId();
                final String timeZoneId = getTimeZoneId(rawTimeZoneId);
                final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(timeZoneId));
                calendar.setTime(date);
                final DateTimeWithTimeZone dateTimeWithTimeZone = new DateTimeWithTimeZone();
                dateTimeWithTimeZone.setYear(calendar.get(Calendar.YEAR));
                dateTimeWithTimeZone.setMonth(calendar.get(Calendar.MONTH));
                dateTimeWithTimeZone.setDayOfMonth(calendar.get(Calendar.DAY_OF_MONTH));
                dateTimeWithTimeZone.setHours(calendar.get(Calendar.HOUR_OF_DAY));
                dateTimeWithTimeZone.setMinutes(calendar.get(Calendar.MINUTE));
                dateTimeWithTimeZone.setSeconds(calendar.get(Calendar.SECOND));
                dateTimeWithTimeZone.setMilliseconds(calendar.get(Calendar.MILLISECOND));
                if (timeZoneId.startsWith("GMT")) {
                    TimeZone timeZone = TimeZone.getTimeZone(timeZoneId);
                    dateTimeWithTimeZone.setTimeZoneContext(new UTCOffsetTimeZoneContext(timeZone.getRawOffset()));
                } else {
                    dateTimeWithTimeZone.setTimeZoneContext(new OlsonTimeZoneContext(timeZoneId));
                }
                return new DateTimeWithTimeZoneValue(dateTimeWithTimeZone);
            } catch (ParseException ignored) {
                ignored.printStackTrace(); // for developers only
            }
        }
        return new DateTimeWithTimeZoneValue();
    }
    private  String getTimeZoneId(String timeZoneId) {
        switch (timeZoneId) {
            case ModelUtil.LOCAL_TIME_ZONE_ID:
            case ModelUtil.DEFAULT_TIME_ZONE_ID:
            case ModelUtil.ORIGINAL_TIME_ZONE_ID:
                return GuiContext.get().getUserInfo().getTimeZoneId();
        }
        return timeZoneId;
    }
}
