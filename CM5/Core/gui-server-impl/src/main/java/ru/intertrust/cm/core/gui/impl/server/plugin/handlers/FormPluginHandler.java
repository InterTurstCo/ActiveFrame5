package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.UserInfo;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.access.AccessVerificationService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.gui.action.*;
import ru.intertrust.cm.core.config.gui.navigation.FormViewerConfig;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.api.server.plugin.ActivePluginHandler;
import ru.intertrust.cm.core.gui.impl.server.action.FormPluginHandlerStatusData;
import ru.intertrust.cm.core.gui.impl.server.util.ActionConfigBuilder;
import ru.intertrust.cm.core.gui.impl.server.util.PluginHandlerHelper;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionContext;
import ru.intertrust.cm.core.gui.model.action.ToolbarContext;
import ru.intertrust.cm.core.gui.model.form.FormDisplayData;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginData;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginState;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Возможные состояния формы
 * full-screen
 * toggle-edit
 * preview
 *
 * @author Denis Mitavskiy
 *         Date: 23.08.13
 *         Time: 13:22
 */
@ComponentName(FormPluginHandler.COMPONENT_NAME)
public class FormPluginHandler extends ActivePluginHandler {
    static final String COMPONENT_NAME = "form.plugin";
    @Autowired
    private GuiService guiService;
    @Autowired
    private ActionConfigBuilder actionConfigBuilder;
    @Autowired
    private ConfigurationExplorer configurationExplorer;
    @Autowired
    private CrudService crudService;
    @Autowired
    private AccessVerificationService accessVerificationService;

    public FormPluginData initialize(Dto initialData) {
        final FormPluginConfig formPluginConfig = (FormPluginConfig) initialData;
        GuiContext.get().setFormPluginState(formPluginConfig.getPluginState());
        FormDisplayData form = getFormDisplayData(formPluginConfig);
        final String rootDomainObjectType = form.getFormState().getRootDomainObjectType();
        Id domainObjectId = formPluginConfig.getDomainObjectId();
        if (configurationExplorer.isAuditLogType(rootDomainObjectType)
                || (domainObjectId != null && !accessVerificationService.isWritePermitted(domainObjectId))) {
            formPluginConfig.getPluginState().setEditable(false);
        }
        FormPluginData pluginData = new FormPluginData();
        if(!configurationExplorer.isAuditLogType(rootDomainObjectType) && domainObjectId!=null){
            DomainObject rootObject = crudService.find(domainObjectId);
            if(rootObject.getStatus()!=null){
                form.setStatus(crudService.find(rootObject.getStatus()));
            }

        }
        pluginData.setFormDisplayData(form);
        pluginData.setPluginState(formPluginConfig.getPluginState());
        if (!configurationExplorer.isAuditLogType(rootDomainObjectType)){
            ToolbarContext toolbarContext = getActionContexts(formPluginConfig, form);
            pluginData.setToolbarContext(toolbarContext);
        }
        return pluginData;
    }

    private FormDisplayData getFormDisplayData(FormPluginConfig config) {
        final String domainObjectToCreate = config.getDomainObjectTypeToCreate();
        final UserInfo userInfo = GuiContext.get().getUserInfo();
        final String domainObjectUpdaterName = config.getDomainObjectUpdatorComponent();
        final Dto updaterContext = config.getUpdaterContext();
        final FormViewerConfig formViewerConfig = config.getFormViewerConfig();
        if (domainObjectToCreate != null) {
            return guiService.getForm(userInfo, config);
        } else {
            return guiService.getForm(config.getDomainObjectId(), domainObjectUpdaterName, updaterContext, userInfo, formViewerConfig);
        }
    }

    private ToolbarContext getActionContexts(final FormPluginConfig config, final FormDisplayData form) {
        final ToolbarContext toolbarContext = getToolbarContexts(config, form);

        final List<ActionContext> otherActions;
        if (config.getDomainObjectId() != null) {
            otherActions = actionService.getActions(config.getDomainObjectId());
        } else {
            otherActions = actionService.getActions(config.getDomainObjectTypeToCreate());
        }

        if (otherActions != null && !otherActions.isEmpty()) {
            Iterator<ActionContext> i = otherActions.iterator();
            while(i.hasNext()){
                ActionContext currentContext = i.next();
                if(currentContext instanceof SimpleActionContext){
                    SimpleActionConfig saConfig = ((SimpleActionContext)currentContext).getActionConfig();
                    if(saConfig.getActionHandler().equals("generic.workflow.action")){
                        if(actionMovedToMenu(toolbarContext,(SimpleActionContext)currentContext))
                            i.remove();
                    }
                }
            }

            toolbarContext.addContexts(otherActions, ToolbarContext.FacetName.LEFT);
        }
        return toolbarContext;
    }

    private Boolean actionMovedToMenu(ToolbarContext toolbarContext,SimpleActionContext actionContext){
        String actionName = ((SimpleActionConfig)actionContext.getActionConfig()).getName();
        for(ActionContext aContext : toolbarContext.getContexts(ToolbarContext.FacetName.LEFT)){
            if(aContext.getActionConfig() instanceof ActionGroupConfig){
                for(AbstractActionConfig childConfig :((ActionGroupConfig) aContext.getActionConfig()).getChildren()){
                    if(childConfig instanceof WorkflowActionsConfig){
                        for(WorkflowActionConfig waConfig :((WorkflowActionsConfig) childConfig).getActions()){
                            if(waConfig.getName().equals(actionName)){
                                actionContext.getActionConfig().setOrder(waConfig.getOrder());
                                aContext.getInnerContexts().add(actionContext);
                                return true;
                            }
                        }
                    }
                }

            }
        }
        return false;
    }

    private ToolbarContext getToolbarContexts(final FormPluginConfig pluginConfig, final FormDisplayData formData) {
        final FormPluginState pluginState = pluginConfig.getPluginState();
        final Map<String, Object> formParams = new HashMap<>();
        formParams.put(FormPluginHandlerStatusData.PLUGIN_IN_CENTRAL_PANEL_ATTR, pluginState.isInCentralPanel());
        formParams.put(FormPluginHandlerStatusData.TOGGLE_EDIT_ATTR, pluginState.isToggleEdit());
        formParams.put(FormPluginHandlerStatusData.PREVIEW_ATTR, !pluginState.isEditable());
        formParams.put(PluginHandlerHelper.DOMAIN_OBJECT_KEY,
                formData.getFormState().getObjects().getRootDomainObject());
        final ToolBarConfig toolbarConfig =
                formData.getToolBarConfig() == null ? new ToolBarConfig() : formData.getToolBarConfig();
        ToolBarConfig defaultToolbarConfig;
        if (toolbarConfig.isRendered() && toolbarConfig.isUseDefault()) {
            defaultToolbarConfig = actionService.getDefaultToolbarConfig(COMPONENT_NAME, GuiContext.getUserLocale());
        } else {
            defaultToolbarConfig = null;
        }
        if (defaultToolbarConfig == null) {
            defaultToolbarConfig = new ToolBarConfig();
        }
        final ToolbarContext result = new ToolbarContext();
        actionConfigBuilder.appendConfigs(defaultToolbarConfig.getActions(), formParams);
        actionConfigBuilder.appendConfigs(toolbarConfig.getActions(), formParams);
        result.setContexts(actionConfigBuilder.getActionContexts(), ToolbarContext.FacetName.LEFT);
        actionConfigBuilder.clear();
        actionConfigBuilder.appendConfigs(defaultToolbarConfig.getRightFacetConfig().getActions(), formParams);
        actionConfigBuilder.appendConfigs(toolbarConfig.getRightFacetConfig().getActions(), formParams);
        result.setContexts(actionConfigBuilder.getActionContexts(), ToolbarContext.FacetName.RIGHT);
        return result;
    }
}
