package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.business.api.dto.TimelessDate;
import ru.intertrust.cm.core.business.api.dto.TimelessDateValue;
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
public class TimelessDateValueConverter implements DateValueConverter<TimelessDateValue> {
    @Override
    public DateTimeContext valueToContext(TimelessDateValue value, String timeZoneIdP, DateFormat dateFormat) {
        final DateTimeContext result = new DateTimeContext();
        result.setOrdinalFieldType(FieldType.TIMELESSDATE.ordinal());
        if (value != null && value.get() != null) {
            final Calendar calendar =
                    GuiServerHelper.timelessDateToCalendar(value.get(), GuiServerHelper.GMT_TIME_ZONE);

            final String timeZoneId = GuiContext.get().getUserInfo().getTimeZoneId();
            dateFormat.setTimeZone(TimeZone.getTimeZone(timeZoneId));
            final String dateTime = dateFormat.format(calendar.getTime());
            result.setDateTime(dateTime);
        }
        return result;
    }

    @Override
    public TimelessDateValue contextToValue(DateTimeContext dateTimeContext) {
        if (dateTimeContext != null) {
            final TimelessDate result = new TimelessDate();
            final String timeZoneId = GuiContext.get().getUserInfo().getTimeZoneId();
            final DateFormat dateFormat = new SimpleDateFormat(ModelUtil.DTO_PATTERN);
            dateFormat.setTimeZone(TimeZone.getTimeZone(timeZoneId));
            try {
                final Date date = dateFormat.parse(dateTimeContext.getDateTime());
                final Calendar calendar = Calendar.getInstance(GuiServerHelper.GMT_TIME_ZONE);
                calendar.setTime(date);
                result.setYear(calendar.get(Calendar.YEAR));
                result.setMonth(calendar.get(Calendar.MONTH));
                result.setDayOfMonth(calendar.get(Calendar.DAY_OF_MONTH));
                return new TimelessDateValue(result);
            } catch (ParseException ignored) {
                ignored.printStackTrace();  // for developers only
            }
        }
        return new TimelessDateValue();
    }
}