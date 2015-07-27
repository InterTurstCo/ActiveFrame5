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
        Value value = widgetContext.getValue();
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

}
