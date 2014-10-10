package ru.intertrust.cm.core.gui.impl.server.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.access.AccessVerificationService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.ConfigurationException;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.gui.action.AbstractActionConfig;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.action.ActionRefConfig;
import ru.intertrust.cm.core.config.gui.action.ActionSeparatorConfig;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.form.FormMappingConfig;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.gui.api.server.ActionService;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.api.server.action.ActionVisibilityChecker;
import ru.intertrust.cm.core.gui.api.server.action.ActionVisibilityContext;
import ru.intertrust.cm.core.gui.api.server.el.DomainObjectPropertyAccessor;
import ru.intertrust.cm.core.gui.api.server.el.DomainObjectTypeComparator;
import ru.intertrust.cm.core.gui.api.server.el.ReferenceValuePropertyAccessor;
import ru.intertrust.cm.core.gui.impl.server.action.FakeActionHandler;
import ru.intertrust.cm.core.gui.model.action.ActionContext;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 31.01.14
 *         Time: 13:15
 */
public class ActionConfigBuilder {

    @Autowired private ApplicationContext applicationContext;
    @Autowired private ActionService actionService;
    @Autowired private CurrentUserAccessor currentUserAccessor;
    @Autowired private CrudService crudService;
    @Autowired private ConfigurationExplorer configurationExplorer;
    @Autowired private AccessVerificationService accessVerificationService;

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
            final DomainObject domainObject = (DomainObject) params.get(PluginHandlerHelper.DOMAIN_OBJECT_KEY);
            if (!hasPermission(domainObject, (ActionConfig) config)) {
                continue;
            }
            final ActionConfig actionConfig = PluginHandlerHelper.cloneActionConfig((ActionConfig) config);
            ActionHandler actionHandler = new FakeActionHandler();
            final boolean hasHandler = applicationContext.containsBean(actionConfig.getComponentName());
            if (hasHandler) {
                actionHandler = (ActionHandler) applicationContext.getBean(actionConfig.getComponentName());
            }
            ActionHandler.Status status = ActionHandler.Status.APPLY;
            if (actionConfig.getVisibilityChecker() != null) {
                final boolean contains = applicationContext.containsBean(actionConfig.getVisibilityChecker());
                if (contains) {
                    final ActionVisibilityContext avContext = new ActionVisibilityContext()
                            .setDomainObject(domainObject);
                    ActionVisibilityChecker checker =
                            (ActionVisibilityChecker) applicationContext.getBean(actionConfig.getVisibilityChecker());
                    status = checker.isVisible(avContext) ? ActionHandler.Status.APPLY : ActionHandler.Status.SKIP;
                } else {
                    throw new ConfigurationException("VisibilityChecker with name '"
                            + actionConfig.getVisibilityChecker() + "' not found");
                }
            }
            if (ActionHandler.Status.APPLY == status) {
                final ActionHandler.HandlerStatusData statusData = actionHandler.getCheckStatusData();
                statusData.initialize(params);
                final boolean isNew = domainObject != null && domainObject.isNew();
                if (!actionConfig.isVisibleWhenNew() && isNew) {
                    status = ActionHandler.Status.SKIP;
                } else {
                    status = actionHandler.getHandlerStatus(config.getRendered(), statusData);
                }
            }
            if (ActionHandler.Status.APPLY == status && actionConfig.getVisibilityStateCondition() != null
                    && domainObject != null) {
                final StandardEvaluationContext context = new StandardEvaluationContext(domainObject);
                final List<PropertyAccessor> accessors = new ArrayList<>();
                accessors.add(new DomainObjectPropertyAccessor(currentUserAccessor.getCurrentUserId()));
                accessors.add(new ReferenceValuePropertyAccessor(crudService));
                context.setPropertyAccessors(accessors);
                context.setTypeComparator(new DomainObjectTypeComparator());
                final ExpressionParser expressionParser = new SpelExpressionParser();
                final boolean isVisible = expressionParser.parseExpression(actionConfig.getVisibilityStateCondition())
                        .getValue(context, Boolean.class);
                status = isVisible ? ActionHandler.Status.APPLY : ActionHandler.Status.SKIP;
            }
            if (ActionHandler.Status.APPLY == status) {
                final FormMappingConfig formMappingConfig = (actionConfig.getBeforeConfig() != null
                                            && actionConfig.getBeforeConfig().getLinkedDomainObjectConfig() != null)
                        ? actionConfig.getBeforeConfig().getLinkedDomainObjectConfig().getFormMappingConfig()
                        : null;
                if (formMappingConfig != null) {
                    final FormConfig formConfig =
                            configurationExplorer.getConfig(FormConfig.class, formMappingConfig.getForm());
                    formMappingConfig.setDomainObjectType(formConfig.getDomainObjectType());
                }
                final ActionContext actionContext = actionHandler.getActionContext(actionConfig);
                actionContext.setActionConfig(actionConfig);
                contextList.addContext(actionContext);
            }
        }
    }

    private ActionConfig resolveActionReference(final ActionRefConfig actionRefConfig) {
        ActionConfig config = referenceMap.get(actionRefConfig.getNameRef());
        if (config == null) {
            config = actionService.getActionConfig(actionRefConfig.getNameRef());
            if (config == null) {  // for developers only
                throw new IllegalArgumentException("Not defines action for action-ref " + actionRefConfig);
            }
            referenceMap.put(config.getName(), config);
        }
        final ActionConfig result = PluginHandlerHelper.cloneActionConfig(config);
        PluginHandlerHelper.fillActionConfigFromRefConfig(result, actionRefConfig);
        return result;
    }

    private void fillReferenceMap(final List<AbstractActionConfig> configs) {
        for (AbstractActionConfig config : configs) {
            if (config instanceof ActionConfig) {
                final ActionConfig actionConfig = (ActionConfig) config;
                if (actionConfig.getName() != null) {
                    referenceMap.put(actionConfig.getName(), actionConfig);
                }
            }
        }
    }

    private boolean hasPermission(final DomainObject domainObject, final ActionConfig config) {
        if (domainObject != null && config.getPermissions() != null && !config.getPermissions().isEmpty()) {
            final String[] permissions = config.getPermissions().split(",");
            final Id id = domainObject.getId();
            boolean result = false;
            for (String permission : permissions) {
                switch (permission.trim().toLowerCase()) {
                    case "write":
                        result = accessVerificationService.isWritePermitted(id);
                        break;
                    case "delete":
                        result = accessVerificationService.isDeletePermitted(id);
                        break;
                    case "create":
                        result = accessVerificationService.isCreatePermitted(domainObject.getTypeName());
                        break;
                    case "execute":
                        result = accessVerificationService.isExecuteActionPermitted(config.getName(), id);
                        break;
                    default:
                        result = true;
                }
                if (result) {
                    break;
                }
            }
            return result;
        } else {
            return true;
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
