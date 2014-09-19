package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.UserInfo;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.action.ToolBarConfig;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.api.server.plugin.ActivePluginHandler;
import ru.intertrust.cm.core.gui.impl.server.action.FormPluginHandlerStatusData;
import ru.intertrust.cm.core.gui.impl.server.util.ActionConfigBuilder;
import ru.intertrust.cm.core.gui.impl.server.util.PluginHandlerHelper;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ToolbarContext;
import ru.intertrust.cm.core.gui.model.form.FormDisplayData;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginData;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginState;

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


    public FormPluginData initialize(Dto initialData) {
        final FormPluginConfig formPluginConfig = (FormPluginConfig) initialData;
        GuiContext.get().setFormPluginState(formPluginConfig.getPluginState());
        FormDisplayData form = getFormDisplayData(formPluginConfig);
        FormPluginData pluginData = new FormPluginData();
        pluginData.setFormDisplayData(form);
        pluginData.setPluginState(formPluginConfig.getPluginState());
        ToolbarContext toolbarContext = getActionContexts(formPluginConfig, form);
        pluginData.setToolbarContext(toolbarContext);
        return pluginData;
    }

    private FormDisplayData getFormDisplayData(FormPluginConfig config) {
        String domainObjectToCreate = config.getDomainObjectTypeToCreate();
        final UserInfo userInfo = GuiContext.get().getUserInfo();
        String domainObjectUpdaterName = config.getDomainObjectUpdatorComponent();
        if (domainObjectUpdaterName == null) {
            return domainObjectToCreate != null ? guiService.getForm(domainObjectToCreate, userInfo, config.getFormViewerConfig())
                    : guiService.getForm(config.getDomainObjectId(), userInfo, config.getFormViewerConfig());
        } else {
            Dto updaterContext = config.getUpdaterContext();
            return domainObjectToCreate != null ? guiService.getForm(domainObjectToCreate,
                    domainObjectUpdaterName, updaterContext, userInfo, config.getFormViewerConfig())
                    : guiService.getForm(config.getDomainObjectId(), domainObjectUpdaterName, updaterContext, userInfo, config.getFormViewerConfig());
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
            toolbarContext.addContexts(otherActions, ToolbarContext.FacetName.LEFT);
        }
        return toolbarContext;
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
            defaultToolbarConfig = actionService.getDefaultToolbarConfig(COMPONENT_NAME);
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
