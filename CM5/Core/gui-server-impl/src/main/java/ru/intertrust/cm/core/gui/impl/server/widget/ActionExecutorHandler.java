package ru.intertrust.cm.core.gui.impl.server.widget;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.action.ActionRefConfig;
import ru.intertrust.cm.core.config.gui.form.widget.ActionExecutorConfig;
import ru.intertrust.cm.core.gui.api.server.ActionService;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.form.widget.ActionExecutorState;
import ru.intertrust.cm.core.gui.model.form.widget.LabelState;

/**
 * @author Sergey.Okolot
 *         Created on 04.09.2014 15:27.
 */
@ComponentName(ActionExecutorConfig.COMPONENT_NAME)
public class ActionExecutorHandler extends LabelHandler {
    public static final String WIDGET_CONTEXT_ATTR = "widgetContext";

    @Autowired private ActionService actionService;
    @Autowired private ApplicationContext applicationContext;

    @Override
    public ActionExecutorState getInitialState(WidgetContext context) {
        final Map<String, Object> params = new HashMap<>();
        params.put("widgetContext", context);
        final LabelState labelState = super.getInitialState(context);
        final ActionExecutorState result = new ActionExecutorState();
        result.setLabelStates(labelState);
        final ActionExecutorConfig actionExecutorConfig = context.getWidgetConfig();
        final ActionRefConfig actionRefConfig = actionExecutorConfig.getActionRefConfig();
        final ActionConfig actionConfig = actionService.getActionConfig(actionRefConfig.getActionId());
        // fixme FakeHandler and check visibility of action
        // fixme copy all attributes
        actionConfig.getProperties().putAll(actionRefConfig.getProperties());
        final boolean contains = applicationContext.containsBean(actionConfig.getComponentName());
        final ActionContext actionContext;
        if (contains) {
            final ActionHandler handler = (ActionHandler) applicationContext.getBean(actionConfig.getComponentName());
            final ActionHandler.HandlerStatusData statusData = handler.getCheckStatusData();
            statusData.initialize(params);
            actionContext = handler.getActionContext(actionConfig);
        } else {
            actionContext = new ActionContext(actionConfig);
        }
        result.setActionContext(actionContext);
        return result;
    }
}
