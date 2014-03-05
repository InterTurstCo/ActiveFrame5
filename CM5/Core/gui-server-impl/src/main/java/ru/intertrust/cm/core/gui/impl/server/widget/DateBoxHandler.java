package ru.intertrust.cm.core.gui.impl.server.widget;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import ru.intertrust.cm.core.business.api.dto.DateTimeValue;
import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZone;
import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZoneValue;
import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.business.api.dto.OlsonTimeZoneContext;
import ru.intertrust.cm.core.business.api.dto.TimelessDate;
import ru.intertrust.cm.core.business.api.dto.TimelessDateValue;
import ru.intertrust.cm.core.business.api.dto.UTCOffsetTimeZoneContext;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.gui.form.widget.DateBoxConfig;
import ru.intertrust.cm.core.gui.api.server.widget.ValueEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.impl.server.GuiContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.DateTimeContext;
import ru.intertrust.cm.core.gui.model.form.widget.DateBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

/**
 * @author Denis Mitavskiy
 *         Date: 26.09.13
 *         Time: 12:41
 */
@ComponentName("date-box")
public class DateBoxHandler extends ValueEditingWidgetHandler {

    private static final String DEFAULT_TIME_ZONE_ID = "default";
    private static final String LOCAL_TIME_ZONE_ID = "local";
    private static final String ORIGINAL_TIME_ZONE_ID = "original";
    private static final TimeZone GMT_TIME_ZONE = TimeZone.getTimeZone("GMT");

    @Override
    public DateBoxState getInitialState(WidgetContext widgetContext) {
        final Value value = widgetContext.getValue();
        final DateBoxConfig config = widgetContext.getWidgetConfig();
        final DateBoxState state = new DateBoxState();
        final DateValueConverter converter = getConverter(value.getFieldType());
        state.setDateTimeContext(converter.valueToContext(value, config));
        return state;
    }

    @Override
    public Value getValue(WidgetState state) {
        final DateBoxState dateBoxState = (DateBoxState) state;
        final FieldType fieldType = FieldType.values()[dateBoxState.getDateTimeContext().getOrdinalFieldType()];
        final DateValueConverter converter = getConverter(fieldType);
        final Value value = converter.contextToValue(dateBoxState.getDateTimeContext());
        return value;
    }

    private static DateValueConverter getConverter(final FieldType fieldType) {
        switch (fieldType) {
            case DATETIME:
                return new DateTimeValueConverter();
            case DATETIMEWITHTIMEZONE:
                return new DateTimeWithTimezoneValueConverter();
            case TIMELESSDATE:
                return new TimelessDateValueConverter();
            default:
                throw new IllegalArgumentException(); // for developers only
        }
    }

    private static abstract class DateValueConverter<T extends Value> {

        public DateTimeContext valueToContext(T value, DateBoxConfig config) {
            final DateTimeContext result = new DateTimeContext();
            result.setPattern(config.getPattern());
            result.setTimeZoneId(config.getTimeZoneId());
            setContextValue(value, result);
            return result;
        }

        public abstract T contextToValue(DateTimeContext context);

        protected abstract void setContextValue(T value, DateTimeContext context);
    }

    private static class TimelessDateValueConverter extends DateValueConverter<TimelessDateValue> {

        @Override
        public TimelessDateValue contextToValue(final DateTimeContext context) {
            if (context.getDateTime() != null) {
                final TimelessDate result = new TimelessDate();
                final String timeZoneId = GuiContext.get().getUserInfo().getTimeZoneId();
                final DateFormat dateFormat = new SimpleDateFormat(DateTimeContext.DTO_PATTERN);
                dateFormat.setTimeZone(TimeZone.getTimeZone(timeZoneId));
                try {
                    final Date date = dateFormat.parse(context.getDateTime());
                    final Calendar calendar = Calendar.getInstance(GMT_TIME_ZONE);
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

        @Override
        protected void setContextValue(final TimelessDateValue value, final DateTimeContext context) {
            if (value.get() != null) {
                context.setOrdinalFieldType(FieldType.TIMELESSDATE.ordinal());
                final Calendar calendar = Calendar.getInstance(GMT_TIME_ZONE);
                calendar.set(Calendar.YEAR, value.get().getYear());
                calendar.set(Calendar.MONTH, value.get().getMonth());
                calendar.set(Calendar.DAY_OF_MONTH, value.get().getDayOfMonth());
                calendar.set(Calendar.HOUR, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                final DateFormat dateFormat = new SimpleDateFormat(DateTimeContext.DTO_PATTERN);
                final String timeZoneId = GuiContext.get().getUserInfo().getTimeZoneId();
                dateFormat.setTimeZone(TimeZone.getTimeZone(timeZoneId));
                final String dateTime = dateFormat.format(calendar.getTime());
                context.setDateTime(dateTime);
            }
        }
    }

    private static class DateTimeValueConverter extends DateValueConverter<DateTimeValue> {

        @Override
        public DateTimeValue contextToValue(final DateTimeContext context) {
            final DateTimeValue result = new DateTimeValue();
            if (context.getDateTime() != null) {
                final String timeZoneId = getTimeZoneId(context);
                final DateFormat dateFormat = new SimpleDateFormat(DateTimeContext.DTO_PATTERN);
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
        protected void setContextValue(final DateTimeValue value, final DateTimeContext context) {
            context.setOrdinalFieldType(FieldType.DATETIME.ordinal());
            if (value.get() != null) {
                final String timeZoneId = getTimeZoneId(context);
                final DateFormat dateFormat = new SimpleDateFormat(DateTimeContext.DTO_PATTERN);
                dateFormat.setTimeZone(TimeZone.getTimeZone(timeZoneId));
                context.setDateTime(dateFormat.format(value.get()));
            }
        }

        protected String getTimeZoneId(final DateTimeContext context) {
            switch (context.getTimeZoneId()) {
                case DEFAULT_TIME_ZONE_ID:
                case LOCAL_TIME_ZONE_ID:
                case ORIGINAL_TIME_ZONE_ID:
                    return GuiContext.get().getUserInfo().getTimeZoneId();
            }
            return context.getTimeZoneId();
        }
    }

    private static class DateTimeWithTimezoneValueConverter extends DateValueConverter<DateTimeWithTimeZoneValue> {

        @Override
        public DateTimeWithTimeZoneValue contextToValue(final DateTimeContext context) {
            if (context.getDateTime() != null) {
                final String userTimeZoneId = GuiContext.get().getUserInfo().getTimeZoneId();
                final DateFormat dateFormat = new SimpleDateFormat(DateTimeContext.DTO_PATTERN);
                dateFormat.setTimeZone(TimeZone.getTimeZone(userTimeZoneId));
                try {
                    final Date date = dateFormat.parse(context.getDateTime());
                    final String timeZoneId = getTimeZoneId(context);
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

        @Override
        protected void setContextValue(final DateTimeWithTimeZoneValue value, final DateTimeContext context) {
            context.setOrdinalFieldType(FieldType.DATETIMEWITHTIMEZONE.ordinal());
            if (value.get() != null) {
                final Calendar calendar =
                        Calendar.getInstance(TimeZone.getTimeZone(value.get().getTimeZoneContext().getTimeZoneId()));
                calendar.set(Calendar.YEAR, value.get().getYear());
                calendar.set(Calendar.MONTH, value.get().getMonth());
                calendar.set(Calendar.DAY_OF_MONTH, value.get().getDayOfMonth());
                calendar.set(Calendar.HOUR, value.get().getHours());
                calendar.set(Calendar.MINUTE, value.get().getMinutes());
                calendar.set(Calendar.SECOND, value.get().getSeconds());
                calendar.set(Calendar.MILLISECOND, value.get().getMilliseconds());
                final DateFormat dateFormat = new SimpleDateFormat(DateTimeContext.DTO_PATTERN);
                final String userTimeZoneId = GuiContext.get().getUserInfo().getTimeZoneId();
                dateFormat.setTimeZone(TimeZone.getTimeZone(userTimeZoneId));
                context.setDateTime(dateFormat.format(calendar.getTime()));
                context.setTimeZoneId(value.get().getTimeZoneContext().getTimeZoneId());
            }
        }

        /**
         * If date changed on client side the {@link DateTimeContext#timeZoneId} attribute is setting to selected
         * time zone id, otherwise the {@link DateTimeContext#timeZoneId} has origin value.
         * @param context instance of the {@link DateTimeContext}.
         * @return actual value of timeZoneId.
         */
        private String getTimeZoneId(final DateTimeContext context) {
            switch (context.getTimeZoneId()) {
                case LOCAL_TIME_ZONE_ID:
                case DEFAULT_TIME_ZONE_ID:
                case ORIGINAL_TIME_ZONE_ID:
                    return GuiContext.get().getUserInfo().getTimeZoneId();
            }
            return context.getTimeZoneId();
        }
    }
}
