package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.business.api.dto.Pair;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.util.ModelUtil;
import ru.intertrust.cm.core.business.api.util.ThreadSafeDateFormat;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.form.widget.datebox.DateBoxConfig;
import ru.intertrust.cm.core.gui.api.server.widget.ValueEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.DateTimeContext;
import ru.intertrust.cm.core.gui.model.form.widget.DateBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.text.DateFormat;
import java.util.Locale;

/**
 * @author Denis Mitavskiy
 *         Date: 26.09.13
 *         Time: 12:41
 */
@ComponentName("date-box")
public class DateBoxHandler extends ValueEditingWidgetHandler {

    @Override
    public DateBoxState getInitialState(WidgetContext widgetContext) {
        final DateBoxConfig config = widgetContext.getWidgetConfig();
        final DateBoxState state = new DateBoxState();
        state.setPattern(config.getPattern());


        FieldType fieldType = FormConfig.TYPE_REPORT.equals(widgetContext.getFormType()) ?
                getFieldTypeForReportForm(config) : getFieldType(widgetContext);

        final boolean displayTimeZoneChoice = config.isDisplayTimeZoneChoice()
                && fieldType == FieldType.DATETIMEWITHTIMEZONE;
        state.setDisplayTimeZoneChoice(displayTimeZoneChoice);
        final DateValueConverter converter = getConverter(fieldType);
        final DateFormat dateFormat = ThreadSafeDateFormat.getDateFormat(new Pair<String, Locale>(ModelUtil.DTO_PATTERN, null), null);
        Value value = FormConfig.TYPE_REPORT.equals(widgetContext.getFormType()) ? null : widgetContext.getValue();
        final DateTimeContext context = converter.valueToContext(value, config.getTimeZoneId(), dateFormat);
        context.setTimeZoneId(config.getTimeZoneId());
        state.setDateTimeContext(context);

        boolean displayTime = fieldType == FieldType.TIMELESSDATE ? false : config.isDisplayTimeBox();
        state.setDisplayTime(displayTime);
        state.setDateBoxConfig(config);
        return state;
    }

    @Override
    public Value getValue(WidgetState state) {
        final DateBoxState dateBoxState = (DateBoxState) state;
        final FieldType fieldType = FieldType.values()[dateBoxState.getDateTimeContext().getOrdinalFieldType()];
        final DateValueConverter converter = getConverter(fieldType);
        final Value value = (Value) converter.contextToValue(dateBoxState.getDateTimeContext());
        return value;
    }

    private FieldType getFieldType(final WidgetContext context) {
        final Value value = context.getValue();
        final FieldType fieldType;
        if (value == null) {
            final String domainObjectName = context.getFormObjects().getDomainObjectType(context.getFieldPaths()[0]);
            final FieldConfig fieldConfig = configurationService.getFieldConfig(
                    domainObjectName, context.getWidgetConfig().getFieldPathConfig().getValue());
            if (fieldConfig == null) {
                fieldType = FieldType.DATETIME;
            } else {
                fieldType = fieldConfig.getFieldType();
            }
        } else {
            fieldType = value.getFieldType();
        }
        return fieldType;
    }

    private FieldType getFieldTypeForReportForm(DateBoxConfig config) {
        String unmanagedType = config.getUnmanagedType();
        if (unmanagedType != null) {
            switch (unmanagedType) {
                case "date-time":
                    return FieldType.DATETIME;
                case "date-time-with-time-zone":
                    return FieldType.DATETIMEWITHTIMEZONE;
                case "timeless-date":
                    return FieldType.TIMELESSDATE;
            }
        }
        return FieldType.DATETIME;
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

   /* private interface DateValueConverter<T extends Value> {

        DateTimeContext valueToContext(T value, DateBoxConfig config);

        T contextToValue(DateTimeContext context);
    }

    private static class TimelessDateValueConverter implements DateValueConverter<TimelessDateValue> {
        @Override
        public DateTimeContext valueToContext(TimelessDateValue value, DateBoxConfig config) {
            final DateTimeContext result = new DateTimeContext();
            result.setOrdinalFieldType(FieldType.TIMELESSDATE.ordinal());
            if (value != null && value.get() != null) {
                final Calendar calendar =
                        GuiServerHelper.timelessDateToCalendar(value.get(), GuiServerHelper.GMT_TIME_ZONE);
                final DateFormat dateFormat = new SimpleDateFormat(ModelUtil.DTO_PATTERN);
                final String timeZoneId = GuiContext.get().getUserInfo().getTimeZoneId();
                dateFormat.setTimeZone(TimeZone.getTimeZone(timeZoneId));
                final String dateTime = dateFormat.format(calendar.getTime());
                result.setDateTime(dateTime);
            }
            return result;
        }

        @Override
        public TimelessDateValue contextToValue(final DateTimeContext context) {
            if (context.getDateTime() != null) {
                final TimelessDate result = new TimelessDate();
                final String timeZoneId = GuiContext.get().getUserInfo().getTimeZoneId();
                final DateFormat dateFormat = new SimpleDateFormat(ModelUtil.DTO_PATTERN);
                dateFormat.setTimeZone(TimeZone.getTimeZone(timeZoneId));
                try {
                    final Date date = dateFormat.parse(context.getDateTime());
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

    private static class DateTimeValueConverter implements DateValueConverter<DateTimeValue> {
        @Override
        public DateTimeContext valueToContext(DateTimeValue value, DateBoxConfig config) {
            final DateTimeContext result = new DateTimeContext();
            result.setOrdinalFieldType(FieldType.DATETIME.ordinal());
            if (value != null && value.get() != null) {
                final String timeZoneId = getTimeZoneId(result, config.getTimeZoneId());
                final DateFormat dateFormat = new SimpleDateFormat(ModelUtil.DTO_PATTERN);
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

    private static class DateTimeWithTimezoneValueConverter implements DateValueConverter<DateTimeWithTimeZoneValue> {
        @Override
        public DateTimeContext valueToContext(DateTimeWithTimeZoneValue value, DateBoxConfig config) {
            final DateTimeContext result = new DateTimeContext();
            result.setOrdinalFieldType(FieldType.DATETIMEWITHTIMEZONE.ordinal());
            if (value != null && value.get() != null) {
                final Calendar calendar = GuiServerHelper.dateTimeWithTimezoneToCalendar(value.get());
                final DateFormat dateFormat = new SimpleDateFormat(ModelUtil.DTO_PATTERN);
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
                    final String timeZoneId = DateUtil.getTimeZoneId(rawTimeZoneId);
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

    }*/
}
