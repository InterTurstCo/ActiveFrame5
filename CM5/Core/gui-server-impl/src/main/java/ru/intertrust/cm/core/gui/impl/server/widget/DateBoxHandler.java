package ru.intertrust.cm.core.gui.impl.server.widget;

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
    public DateBoxState getInitialState(WidgetContext context) {
        final Value value = context.getValue();
        final DateBoxConfig config = context.getWidgetConfig();
        final DateValueConverter converter = getConverter(value.getFieldType());
        final DateBoxState dateBoxState = converter.valueToState(value, config);
        dateBoxState.setDate(context.<Date>getFieldPlainValue());
        return dateBoxState;
    }

    @Override
    public Value getValue(WidgetState state) {
        final DateBoxState dateBoxState = (DateBoxState) state;
        final FieldType fieldType = FieldType.values()[dateBoxState.getOrdinalFieldType()];
        final DateValueConverter converter = getConverter(fieldType);
        final Value value = converter.dateToValue(dateBoxState);
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

        public abstract DateBoxState valueToState(T value, DateBoxConfig config);

        public abstract T dateToValue(DateBoxState state);

        protected int getTimeZoneOffset(final DateBoxConfig config, final Date date) {
            final Calendar calendar = GregorianCalendar.getInstance();
            calendar.setTime(date);
            String timeZoneId = config.getTimeZoneId() == null ? DEFAULT_TIME_ZONE_ID : config.getTimeZoneId();
            switch (timeZoneId) {
                case DEFAULT_TIME_ZONE_ID:
                case LOCAL_TIME_ZONE_ID:
                    timeZoneId = GuiContext.get().getUserInfo().getTimeZone();
                    break;
                case ORIGINAL_TIME_ZONE_ID:
                default:
                    timeZoneId = calendar.getTimeZone().getID();
            }
            final TimeZone timeZone = TimeZone.getTimeZone(timeZoneId);
            calendar.setTimeZone(timeZone);
            final int timeZoneOffset =
                    (calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET))/(60 * 1000);
            return timeZoneOffset;
        }
    }

    private static class TimelessDateValueConverter extends DateValueConverter<TimelessDateValue> {

        @Override
        public DateBoxState valueToState(TimelessDateValue value, final DateBoxConfig config) {
            final DateBoxState state = new DateBoxState(FieldType.TIMELESSDATE.ordinal());
            // FIXME not implements
            state.setDate(new Date());
            return state;
        }

        @Override
        public TimelessDateValue dateToValue(final DateBoxState state) {
            return null;
        }
    }

    private static class DateTimeValueConverter extends DateValueConverter<DateTimeValue> {

        public DateBoxState valueToState(DateTimeValue value, final DateBoxConfig config) {
            final DateBoxState state = new DateBoxState(FieldType.DATETIME.ordinal());
            state.setDate(value.get());
            state.setTimeZoneOffset(getTimeZoneOffset(config, value.get()));
            return state;
        }

        @Override
        public DateTimeValue dateToValue(final DateBoxState state) {
            return null;
        }
    }

    private static class DateTimeWithTimezoneValueConverter extends DateValueConverter<DateTimeWithTimeZoneValue> {

        @Override
        public DateBoxState valueToState(DateTimeWithTimeZoneValue value, final DateBoxConfig config) {
            final DateBoxState state = new DateBoxState(FieldType.DATETIMEWITHTIMEZONE.ordinal());
            // FIXME not implements
            state.setDate(new Date());
            return state;
        }

        @Override
        public DateTimeWithTimeZoneValue dateToValue(final DateBoxState state) {
            return null;
        }
    }
}
