package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.UserInfo;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.action.ToolBarConfig;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.api.server.plugin.ActivePluginHandler;
import ru.intertrust.cm.core.gui.impl.server.util.ActionConfigBuilder;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ToolbarContext;
import ru.intertrust.cm.core.gui.model.form.FormDisplayData;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginData;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginState;

import java.util.HashMap;
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


    public FormPluginData initialize(Dto initialData) {
        final FormPluginConfig formPluginConfig = (FormPluginConfig) initialData;
        FormDisplayData form = getFormDisplayData(formPluginConfig) ;
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
            return domainObjectToCreate != null ? guiService.getForm(domainObjectToCreate, userInfo)
                    : guiService.getForm(config.getDomainObjectId(), userInfo);
        } else {
            Dto updaterContext = config.getUpdaterContext();
            return domainObjectToCreate != null ? guiService.getForm(domainObjectToCreate,
                    domainObjectUpdaterName, updaterContext, userInfo)
                    : guiService.getForm(config.getDomainObjectId(), domainObjectUpdaterName, updaterContext, userInfo);
        }

    }

    private ToolbarContext getActionContexts(final FormPluginConfig config, final FormDisplayData form) {
        final ToolbarContext toolbarContext = getToolbarContexts(config.getPluginState(), form);

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

    private ToolbarContext getToolbarContexts(final FormPluginState pluginState, final FormDisplayData formData) {
        final Map<String, Object> formParams = new HashMap<>();
        formParams.put("pluginIsCentralPanel", pluginState.isInCentralPanel());
        formParams.put("toggleEdit", pluginState.isToggleEdit());
        formParams.put("preview", !pluginState.isEditable());
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
