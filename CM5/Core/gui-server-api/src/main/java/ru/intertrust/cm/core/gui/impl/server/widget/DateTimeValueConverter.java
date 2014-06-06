package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.business.api.dto.DateTimeValue;
import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.business.api.util.ModelUtil;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.model.DateTimeContext;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 19.05.14
 *         Time: 17:15
 */
public class DateTimeValueConverter implements DateValueConverter<DateTimeValue> {
    @Override
    public DateTimeContext valueToContext(DateTimeValue value, String timeZoneIdP, DateFormat dateFormat) {
        final DateTimeContext result = new DateTimeContext();
        result.setOrdinalFieldType(FieldType.DATETIME.ordinal());
        if (value != null && value.get() != null) {
            final String timeZoneId = getTimeZoneId(result, timeZoneIdP);

            dateFormat.setTimeZone(TimeZone.getTimeZone(timeZoneId));
            result.setDateTime(dateFormat.format(value.get()));
        }

        return result;
    }

    @Override
    public DateTimeValue contextToValue(final DateTimeContext context) {
        final DateTimeValue result = new DateTimeValue();
        if (context.getDateTime() != null) {
            final String timeZoneId = getTimeZoneId(context, context.getTimeZoneId());
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

    protected String getTimeZoneId(final DateTimeContext context, final String timeZoneId) {
        switch (timeZoneId) {
            case ModelUtil.DEFAULT_TIME_ZONE_ID:
            case ModelUtil.LOCAL_TIME_ZONE_ID:
            case ModelUtil.ORIGINAL_TIME_ZONE_ID:
                return GuiContext.get().getUserInfo().getTimeZoneId();
        }
        return context.getTimeZoneId();
    }
}

