package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.util.ModelUtil;
import ru.intertrust.cm.core.business.api.util.ThreadSafeDateFormat;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.GuiServerHelper;
import ru.intertrust.cm.core.gui.model.DateTimeContext;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 19.05.14
 *         Time: 17:15
 */
public class DateTimeWithTimezoneValueConverter extends AbstractDateValueConverter<DateTimeWithTimeZoneValue> {

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
            try {
                final Date date = ThreadSafeDateFormat.parse(context.getDateTime(), ModelUtil.DTO_PATTERN, TimeZone.getTimeZone(userTimeZoneId));
                final String rawTimeZoneId = context.getTimeZoneId();
                final String timeZoneId = getTimeZoneId(rawTimeZoneId);
                final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(timeZoneId));
                calendar.setTime(date);
                TimeZoneContext timeZoneContext;
                if (timeZoneId.startsWith("GMT")) {
                    TimeZone timeZone = TimeZone.getTimeZone(timeZoneId);
                    timeZoneContext = new UTCOffsetTimeZoneContext(timeZone.getRawOffset());
                } else {
                    timeZoneContext = new OlsonTimeZoneContext(timeZoneId);
                }
                return new DateTimeWithTimeZoneValue(new DateTimeWithTimeZone(
                    timeZoneContext,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH),
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    calendar.get(Calendar.SECOND),
                    calendar.get(Calendar.MILLISECOND)
                ));
            } catch (Exception ignored) {
                ignored.printStackTrace(); // for developers only
            }
        }
        return new DateTimeWithTimeZoneValue();
    }

    @Override
    public Date valueToDate(DateTimeWithTimeZoneValue value, String timeZoneId) {
        if (value != null && value.get() != null) {
            final Calendar calendar = GuiServerHelper.dateTimeWithTimezoneToCalendar(value.get());
            calendar.setTimeZone(TimeZone.getTimeZone(getTimeZoneId(timeZoneId)));
            return calendar.getTime();
        } else {
            return null;
        }
    }

    @Override
    public DateTimeWithTimeZoneValue dateToValue(final Date date, final String timeZoneRaw) {
        if (date != null) {
            final String timeZoneId = getTimeZoneId(timeZoneRaw == null ? ModelUtil.DEFAULT_TIME_ZONE_ID : timeZoneRaw);
            final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(timeZoneId));
            calendar.setTime(date);
            TimeZoneContext timeZoneContext;
            if (timeZoneId.startsWith("GMT")) {
                TimeZone timeZone = TimeZone.getTimeZone(timeZoneId);
                timeZoneContext = new UTCOffsetTimeZoneContext(timeZone.getRawOffset());
            } else {
                timeZoneContext = new OlsonTimeZoneContext(timeZoneId);
            }
            return new DateTimeWithTimeZoneValue(new DateTimeWithTimeZone(
                timeZoneContext,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND),
                calendar.get(Calendar.MILLISECOND)
            ));
        }
        return new DateTimeWithTimeZoneValue();
    }
}
