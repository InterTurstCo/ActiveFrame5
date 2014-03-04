package ru.intertrust.cm.core.gui.impl.server.widget;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import ru.intertrust.cm.core.business.api.dto.DateTimeValue;
import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZoneValue;
import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.business.api.dto.TimelessDateValue;
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

        protected String getTimeZoneId(final DateTimeContext context) {
            return context.getTimeZoneId();
//            String timeZoneId = context.getTimeZoneId();
//            switch (timeZoneId) {
//                case DEFAULT_TIME_ZONE_ID:
//                case LOCAL_TIME_ZONE_ID:
//                    timeZoneId = GuiContext.get().getUserInfo().getTimeZone();
//                    break;
//                case ORIGINAL_TIME_ZONE_ID:
//                default:
//                    timeZoneId = calendar.getTimeZone().getID();
//            }
//            final TimeZone timeZone = TimeZone.getTimeZone(timeZoneId);
//            calendar.setTimeZone(timeZone);
//            final int timeZoneOffset =
//                    (calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET))/(60 * 1000);
//            return timeZoneOffset;
        }
    }

    private static class TimelessDateValueConverter extends DateValueConverter<TimelessDateValue> {

        @Override
        public TimelessDateValue contextToValue(final DateTimeContext context) {
            return null;
        }

        @Override
        protected void setContextValue(final TimelessDateValue value, final DateTimeContext context) {
            context.setOrdinalFieldType(FieldType.TIMELESSDATE.ordinal());
        }
    }

    private static class DateTimeValueConverter extends DateValueConverter<DateTimeValue> {

        @Override
        public DateTimeValue contextToValue(final DateTimeContext context) {
            final DateTimeValue result = new DateTimeValue();
            final String timeZoneId = getTimeZoneId(context);
            final DateFormat dateFormat = new SimpleDateFormat(context.getPattern());
            dateFormat.setTimeZone(TimeZone.getTimeZone(timeZoneId));
            try {
                final Date date = context.getDateTime() == null ? null : dateFormat.parse(context.getDateTime());
                result.setValue(date);
            } catch (ParseException ignored) {
                ignored.printStackTrace(); // for developers only
            }
            return result;
        }

        @Override
        protected void setContextValue(final DateTimeValue value, final DateTimeContext context) {
            context.setOrdinalFieldType(FieldType.DATETIME.ordinal());
            final String timeZoneId = getTimeZoneId(context);
            final DateFormat dateFormat = new SimpleDateFormat(context.getPattern());
            dateFormat.setTimeZone(TimeZone.getTimeZone(timeZoneId));
            context.setDateTime(value.get() == null ? null : dateFormat.format(value.get()));
        }

        @Override
        protected String getTimeZoneId(final DateTimeContext context) {
            switch (context.getTimeZoneId()) {
                case DEFAULT_TIME_ZONE_ID:
                case LOCAL_TIME_ZONE_ID:
                case ORIGINAL_TIME_ZONE_ID:
                    return GuiContext.get().getUserInfo().getTimeZone();
            }
            return context.getTimeZoneId();
        }
    }

    private static class DateTimeWithTimezoneValueConverter extends DateValueConverter<DateTimeWithTimeZoneValue> {

        @Override
        public DateTimeWithTimeZoneValue contextToValue(final DateTimeContext context) {
            return null;
        }

        @Override
        protected void setContextValue(final DateTimeWithTimeZoneValue value, final DateTimeContext context) {
            context.setOrdinalFieldType(FieldType.DATETIMEWITHTIMEZONE.ordinal());
        }
    }
}
