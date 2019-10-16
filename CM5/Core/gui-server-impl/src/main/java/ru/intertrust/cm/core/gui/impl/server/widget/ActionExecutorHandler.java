package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.action.ActionRefConfig;
import ru.intertrust.cm.core.config.gui.form.widget.ActionExecutorConfig;
import ru.intertrust.cm.core.gui.api.server.ActionExecutorContextBuilder;
import ru.intertrust.cm.core.gui.api.server.ActionService;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.impl.server.action.FormPluginHandlerStatusData;
import ru.intertrust.cm.core.gui.impl.server.util.PluginHandlerHelper;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.form.widget.ActionExecutorState;
import ru.intertrust.cm.core.gui.model.form.widget.LabelState;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginState;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Sergey.Okolot
 *         Created on 04.09.2014 15:27.
 */
@ComponentName(ActionExecutorConfig.COMPONENT_NAME)
public class ActionExecutorHandler extends LabelHandler {
    public static final String WIDGET_CONTEXT_ATTR = "widgetContext";

    @Autowired
    private ActionService actionService;

    @Override
    public ActionExecutorState getInitialState(WidgetContext context) {
        final Map<String, Object> params = new HashMap<>();
        DomainObject rootObject = context.getFormObjects().getRootDomainObject();
        params.put(WIDGET_CONTEXT_ATTR, context);
        final LabelState labelState = super.getInitialState(context);
        final ActionExecutorState result = new ActionExecutorState();
        result.setLabelStates(labelState);
        final ActionExecutorConfig actionExecutorConfig = context.getWidgetConfig();
        final ActionRefConfig actionRefConfig = actionExecutorConfig.getActionRefConfig();
        if (actionRefConfig != null) {
            final ActionConfig actionConfig =
                    PluginHandlerHelper.cloneActionConfig(actionService.getActionConfig(actionRefConfig.getNameRef(), ActionConfig.class));
            PluginHandlerHelper.fillActionConfigFromRefConfig(actionConfig, actionRefConfig);
            final boolean contains = applicationContext.containsBean(actionConfig.getComponentName());
            final ActionContext actionContext;
            if (contains) {
                final ActionHandler handler = (ActionHandler) applicationContext.getBean(actionConfig.getComponentName());
                final ActionHandler.HandlerStatusData statusData = handler.getCheckStatusData();
                final FormPluginState pluginState = GuiContext.get().getFormPluginState();
                if (pluginState != null) {
                    params.put(FormPluginHandlerStatusData.PLUGIN_IN_CENTRAL_PANEL_ATTR, pluginState.isInCentralPanel());
                    params.put(FormPluginHandlerStatusData.TOGGLE_EDIT_ATTR, pluginState.isToggleEdit());
                    params.put(FormPluginHandlerStatusData.PREVIEW_ATTR, !pluginState.isEditable());
                }
                statusData.initialize(params);
                final ActionHandler.Status actionStatus =
                        handler.getHandlerStatus(actionConfig.getRendered(), statusData);
                if (ActionHandler.Status.APPLY == actionStatus) {
                    if (actionExecutorConfig.getActionContextBuilderConfig() == null) {
                        actionContext = handler.getActionContext(actionConfig);
                    } else {
                        /**
                         * CMFIVE-5049 Построение URL в open.link.action при помощи кода
                         */
                        ActionExecutorContextBuilder contextBuilder = (ActionExecutorContextBuilder)applicationContext.
                                getBean(actionExecutorConfig.getActionContextBuilderConfig().getBuilderComponent());
                        actionContext = contextBuilder.getActionContext(rootObject.getId(),context,actionConfig);
                    }
                } else {
                    actionContext = null;
                }
            } else {
                actionContext = new ActionContext(actionConfig);
            }
            if (actionContext != null && context.getFormObjects().getRootDomainObject() != null)
                actionContext.setRootObjectId(context.getFormObjects().getRootDomainObject().getId());
            result.setActionContext(actionContext);
        }
        return result;
    }
}
