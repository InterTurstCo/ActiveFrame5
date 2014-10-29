package ru.intertrust.cm.core.gui.impl.server.widget;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import ru.intertrust.cm.core.business.api.dto.DateTimeValue;
import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.business.api.util.ModelUtil;
import ru.intertrust.cm.core.gui.model.DateTimeContext;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 19.05.14
 *         Time: 17:15
 */
public class DateTimeValueConverter extends AbstractDateValueConverter<DateTimeValue> {

    @Override
    public DateTimeContext valueToContext(DateTimeValue value, String timeZoneIdP, DateFormat dateFormat) {
        final DateTimeContext result = new DateTimeContext();
        result.setTimeZoneId(timeZoneIdP);
        result.setOrdinalFieldType(FieldType.DATETIME.ordinal());
        if (value != null && value.get() != null) {
            final String timeZoneId = getTimeZoneId(result.getTimeZoneId());
            dateFormat.setTimeZone(TimeZone.getTimeZone(timeZoneId));
            result.setDateTime(dateFormat.format(value.get()));
        }

        return result;
    }

    @Override
    public DateTimeValue contextToValue(final DateTimeContext context) {
        final DateTimeValue result = new DateTimeValue();
        if (context.getDateTime() != null) {
            final String timeZoneId = getTimeZoneId(context.getTimeZoneId());
            final DateFormat dateFormat = new SimpleDateFormat(ModelUtil.DTO_PATTERN);
            dateFormat.setTimeZone(TimeZone.getTimeZone(timeZoneId));
            try {
                final Date date = dateFormat.parse(context.getDateTime());
                result.setValue(date);
            } catch (ParseException ignored) {
                ignored.printStackTrace(); // for developers only
            }
        }
        return result;
    }

    @Override
    public Date valueToDate(DateTimeValue value, String timeZoneId) {
        if (value != null && value.get() != null) {
            final Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone(getTimeZoneId(timeZoneId)));
            calendar.setTime(value.get());
            return calendar.getTime();
        } else {
            return null;
        }
    }

    @Override
    public DateTimeValue dateToValue(Date date, String timeZoneId) {
        final DateTimeValue result = new DateTimeValue();
        if (date != null) {
            result.setValue(date);
        }
        return result;
    }
}

