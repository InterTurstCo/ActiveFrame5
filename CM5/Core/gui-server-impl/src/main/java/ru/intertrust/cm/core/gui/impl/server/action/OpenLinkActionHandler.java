package ru.intertrust.cm.core.gui.impl.server.action;

import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZone;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.impl.server.widget.ActionExecutorHandler;
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
        final String baseUrl = actionConfig.getProperty("base-url");
        if (baseUrl == null) {
            throw new IllegalArgumentException("Не задан базовый URL для OpenLinkAction"); // for developers only
        }
        final WidgetContext widgetContext =
                (WidgetContext) statusData.getParameter(ActionExecutorHandler.WIDGET_CONTEXT_ATTR);
        final StringBuilder urlBuilder = new StringBuilder(baseUrl);
        final String domainObjectFields = actionConfig.getProperty("domain-object-fields");
        if (domainObjectFields != null && !domainObjectFields.isEmpty()) {
            final String[] fields = domainObjectFields.split(",");
            for (int index = 0; index < fields.length; index++) {
                final String field = fields[index].trim();
                if (!field.isEmpty()) {
                    buildUrl(urlBuilder, field, actionConfig, widgetContext);
                }
            }
        }
        result.setOpenUrl(urlBuilder.toString());
        return result;
    }

    private void buildUrl(final StringBuilder builder, final String fieldName,
                          final ActionConfig config, final WidgetContext widgetContext) {
        final String identifier = config.getProperty(fieldName) == null ? fieldName : config.getProperty(fieldName);
        final FieldPath fieldPath = new FieldPath(fieldName);
        final Value value = widgetContext.getValue(fieldPath);
//        if (DateTimeWithTimeZone)
        builder.append(builder.indexOf("?") < 0 ? '?' : '&').append(identifier)
                .append('=').append(widgetContext.getValue(fieldPath));
    }
}
