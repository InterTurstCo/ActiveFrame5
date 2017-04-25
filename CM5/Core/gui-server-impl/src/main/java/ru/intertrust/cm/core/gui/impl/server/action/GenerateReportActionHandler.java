package ru.intertrust.cm.core.gui.impl.server.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.util.ListValue;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfigurationConfig;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.GenerateReportActionContext;
import ru.intertrust.cm.core.gui.model.action.GenerateReportActionData;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.widget.LinkEditingWidgetState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

/**
 * @author Lesia Puhova Date: 25.03.14 Time: 15:05
 */
@ComponentName("generate-report.action")
public class GenerateReportActionHandler extends ActionHandler<GenerateReportActionContext, GenerateReportActionData> {
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Override
    public GenerateReportActionData executeAction(GenerateReportActionContext context) {
        FormState formState = context.getFormState();

        GenerateReportActionData actionData = new GenerateReportActionData();
        actionData.setReportName(context.getReportName());
        actionData.setParams(buildParamsMap(formState));
        return actionData;
    }

    @Override
    public GenerateReportActionContext getActionContext(final ActionConfig actionConfig) {
        return new GenerateReportActionContext(actionConfig);
    }

    private Map<String, Value> buildParamsMap(FormState formState) {
        Map<String, Value> params = new HashMap<String, Value>();

        List<WidgetConfig> widgetConfigs = getWidgetConfigs(formState);
        for (WidgetConfig widgetConfig : widgetConfigs) {
            FieldPath[] fieldPaths = FieldPath.createPaths(widgetConfig.getFieldPathConfig().getValue());
            FieldPath firstFieldPath = fieldPaths[0];
            if (firstFieldPath == null) {
                continue;
            }
            String paramName = firstFieldPath.toString();

            WidgetState widgetState = formState.getWidgetState(widgetConfig.getId());
            if (widgetState == null) {
                continue;
            }
            WidgetHandler handler = getWidgetHandler(widgetConfig);
            Value value;
            if (widgetState instanceof LinkEditingWidgetState &&
                    !((LinkEditingWidgetState) widgetState).isSingleChoice()) {
                List<Id> ids = ((LinkEditingWidgetState) widgetState).getIds();
                List<Value<?>> values = new ArrayList<>(ids.size());
                for (Id id : ids) {
                    values.add(new ReferenceValue(id));
                }
                value = ListValue.createListValue(values);
            } else {
                value = handler.getValue(widgetState);
            }
            params.put(paramName, value);
        }
        return params;
    }

    private List<WidgetConfig> getWidgetConfigs(FormState formState) {
        FormConfig formConfig = configurationExplorer.getLocalizedPlainFormConfig(formState.getName(),
                GuiContext.getUserLocale());
        WidgetConfigurationConfig widgetConfigurationConfig = formConfig.getWidgetConfigurationConfig();
        return widgetConfigurationConfig.getWidgetConfigList();
    }

    private WidgetHandler getWidgetHandler(WidgetConfig config) {
        String handlerName = config.getHandler();
        if (handlerName == null) {
            handlerName = config.getComponentName();
        }
        return (WidgetHandler) applicationContext.getBean(handlerName);
    }
}
