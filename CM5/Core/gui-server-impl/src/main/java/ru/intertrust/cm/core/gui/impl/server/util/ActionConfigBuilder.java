package ru.intertrust.cm.core.gui.impl.server.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.config.gui.action.AbstractActionConfig;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.action.ActionRefConfig;
import ru.intertrust.cm.core.config.gui.action.ActionSeparatorConfig;
import ru.intertrust.cm.core.gui.api.server.ActionService;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.impl.server.action.FakeActionHandler;
import ru.intertrust.cm.core.gui.model.action.ActionContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 31.01.14
 *         Time: 13:15
 */
public class ActionConfigBuilder {

    @Autowired private ApplicationContext applicationContext;
    @Autowired private ActionService actionService;
    private final Map<String, ActionConfig> referenceMap = new HashMap<>();
    private final ActionContextList contextList = new ActionContextList();

    public void clear() {
        contextList.clear();
    }

    public List<ActionContext> getActionContexts() {
        return contextList.getContexts();
    }

    public void appendConfigs(final List<AbstractActionConfig> configs, final Map<String, Object> params) {
        fillReferenceMap(configs);
        for (AbstractActionConfig config : configs) {
            if (config instanceof ActionSeparatorConfig) {
                contextList.addContext(new ActionContext(config));
                continue;
            }
            if (config instanceof ActionRefConfig) {
                config = resolveActionReference((ActionRefConfig) config);
            }
            final ActionConfig actionConfig = (ActionConfig) config;
            final boolean contains = applicationContext.containsBean(actionConfig.getComponentName());
            final ActionHandler actionHandler;
            if (contains) {
                actionHandler = (ActionHandler) applicationContext.getBean(actionConfig.getComponentName());
            } else {
                actionHandler = new FakeActionHandler();
            }
            final ActionHandler.HandlerStatusData statusData = actionHandler.getCheckStatusData();
            final ActionHandler.Status status;
            if (statusData == null) {
                status = ActionHandler.Status.SUCCESSFUL;
            } else {
                statusData.initialize(params);
                status = actionHandler.getHandlerStatus(config.getRendered(), statusData);
            }
            if (ActionHandler.Status.SUCCESSFUL == status) {
                ActionContext actionContext = actionHandler.getActionContext();
                actionContext.setActionConfig(actionConfig);
                contextList.addContext(actionContext);
            }
        }
    }

    private ActionConfig resolveActionReference(final ActionRefConfig actionRefConfig) {
        ActionConfig config = referenceMap.get(actionRefConfig.getActionId());
        if (config == null) {
            config = actionService.getActionConfig(actionRefConfig.getActionId());
            if (config == null) {  // for developers only
                throw new IllegalArgumentException("Not defines action for action-ref " + actionRefConfig);
            }
            referenceMap.put(config.getId(), config);
        }
        final ActionConfig result = PluginHelper.cloneConfig(config);
        if (!actionRefConfig.isShowText()) {
            result.setText(null);
        }
        if (!actionRefConfig.isShowImage()) {
            result.setImageUrl(null);
        }
        if (actionRefConfig.getOrder() < Integer.MAX_VALUE) {
            result.setOrder(actionRefConfig.getOrder());
        }
        if (actionRefConfig.getRendered() != null) {
            result.setRendered(actionRefConfig.getRendered());
        }
        if (actionRefConfig.getMerged() != null) {
            result.setMerged(actionRefConfig.getMerged());
        }
        return result;
    }

    private void fillReferenceMap(final List<AbstractActionConfig> configs) {
        for (AbstractActionConfig config : configs) {
            if (config.getId() != null && !config.getId().isEmpty() && (config instanceof ActionConfig)) {
                referenceMap.put(config.getId(), (ActionConfig) config);
            }
        }
    }

    private static class ActionContextList {
        private Map<AbstractActionConfig, ActionContext> contextMap = new HashMap<>();

        public List<ActionContext> getContexts() {
            return new ArrayList<>(contextMap.values());
        }

        public void clear() {
            contextMap.clear();
        }

        public void addContext(final ActionContext context) {
            final AbstractActionConfig config = context.getActionConfig();
            final ActionContext exists = contextMap.get(config);
            if (exists == null) {
                contextMap.put(config, context);
            } else if (config.getWeight() > exists.getActionConfig().getWeight()) {
                contextMap.remove(config);
                contextMap.put(config, context);
            }
        }
    }
}
