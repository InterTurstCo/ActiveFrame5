package ru.intertrust.cm.core.gui.impl.server.action;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import ru.intertrust.cm.core.business.api.dto.DateTimeValue;
import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZoneValue;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.TimelessDateValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.util.ModelUtil;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.impl.server.widget.ActionExecutorHandler;
import ru.intertrust.cm.core.gui.impl.server.widget.DateTimeValueConverter;
import ru.intertrust.cm.core.gui.impl.server.widget.DateTimeWithTimezoneValueConverter;
import ru.intertrust.cm.core.gui.impl.server.widget.DateValueConverter;
import ru.intertrust.cm.core.gui.impl.server.widget.TimelessDateValueConverter;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.OpenLinkActionContext;
import ru.intertrust.cm.core.gui.model.form.FieldPath;

/**
 * @author Sergey.Okolot
 *         Created on 05.09.2014 11:56.
 */
@ComponentName(OpenLinkActionContext.COMPONENT_NAME)
public class OpenLinkActionHandler extends ActionHandler<OpenLinkActionContext, ActionData> {

    private static final String DATE_TIME_FORMAT_PARAM = "date-time-format";
    private static final String DATE_FORMAT_PARAM = "date-format";
    private static final String DATE_TIME_FORMAT_TIME_ZONE_PARAM = "date-time-format-time-zone";



    private FormPluginHandlerStatusData statusData = new FormPluginHandlerStatusData();

    @Override
    public ActionData executeAction(OpenLinkActionContext context) {
        return null;
    }

    @Override
    public HandlerStatusData getCheckStatusData() {
        return statusData;
    }

    @Override
    public OpenLinkActionContext getActionContext(final ActionConfig actionConfig) {
        final OpenLinkActionContext result = new OpenLinkActionContext(actionConfig);
        result.setBaseUrl(actionConfig.getProperty("base-url"));
        final WidgetContext widgetContext =
                (WidgetContext) statusData.getParameter(ActionExecutorHandler.WIDGET_CONTEXT_ATTR);
        final StringBuilder queryStringBuilder = new StringBuilder();
        final String domainObjectFields = actionConfig.getProperty("domain-object-fields");
        if (domainObjectFields != null && !domainObjectFields.isEmpty()) {
            final String[] fields = domainObjectFields.split(",");
            for (int index = 0; index < fields.length; index++) {
                final String field = fields[index].trim();
                if (!field.isEmpty()) {
                    try {
                        buildUrl(queryStringBuilder, field, actionConfig, widgetContext);
                    } catch (UnsupportedEncodingException ignored){
                        throw new IllegalArgumentException(ignored);
                    }
                }
            }
        }
        result.setQueryString(queryStringBuilder.length() > 0 ? queryStringBuilder.toString() : null);
        return result;
    }

    private void buildUrl(final StringBuilder builder, final String fieldName, final ActionConfig config,
                          final WidgetContext widgetContext) throws UnsupportedEncodingException {
        final String identifier = config.getProperty(fieldName) == null ? fieldName : config.getProperty(fieldName);
        final String valueAsStr;
        if ("id".equals(fieldName)) {
            final Id id = widgetContext.getFormObjects().getRootDomainObject().getId();
            valueAsStr = id == null ? "" : id.toStringRepresentation();
        } else {
            final FieldPath fieldPath = new FieldPath(fieldName);
            valueAsStr = valueToString(widgetContext.getValue(fieldPath), config);
        }
        if (builder.length() > 0) {
            builder.append('&');
        }
        builder.append(identifier).append('=').append(URLEncoder.encode(valueAsStr, "UTF-8"));
    }

    private String valueToString(final Value value, final ActionConfig actionConfig) {
        if (value == null) {
            return "";
        }
        final String result;
        if (value instanceof DateTimeWithTimeZoneValue) {
            final DateValueConverter converter = new DateTimeWithTimezoneValueConverter();
            result = converter.valueToContext(value, getTimeZoneId(actionConfig), getDateFormatter(actionConfig, false))
                    .getDateTime();
        } else if (value instanceof TimelessDateValue) {
            final DateValueConverter converter = new TimelessDateValueConverter();
            result = converter.valueToContext(value, getTimeZoneId(actionConfig), getDateFormatter(actionConfig, true))
                    .getDateTime();
        } else if (value instanceof DateTimeValue) {
            final DateValueConverter converter = new DateTimeValueConverter();
            result = converter.valueToContext(value, getTimeZoneId(actionConfig), getDateFormatter(actionConfig, false))
                    .getDateTime();
        } else {
            result = value.toString();
        }
        return result;
    }

    private DateFormat getDateFormatter(final ActionConfig actionConfig, final boolean timeless) {
        final String dateTimeFormat = actionConfig.getProperty(DATE_TIME_FORMAT_PARAM);
        final String dateFormat = actionConfig.getProperty(DATE_FORMAT_PARAM) == null
                ? ModelUtil.DEFAULT_DATE_PATTERN
                : actionConfig.getProperty(DATE_FORMAT_PARAM);
        final String pattern = timeless ? dateFormat : dateTimeFormat;
        final DateFormat result = new SimpleDateFormat(pattern);
        return result;
    }

    private String getTimeZoneId(final ActionConfig actionConfig) {
        final String timeZoneId = actionConfig.getProperty(DATE_TIME_FORMAT_TIME_ZONE_PARAM) == null
                ? ModelUtil.DEFAULT_TIME_ZONE_ID
                : actionConfig.getProperty(DATE_TIME_FORMAT_TIME_ZONE_PARAM);
        return timeZoneId;
    }
}
