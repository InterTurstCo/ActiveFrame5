package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.tools.Tool;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.UserInfo;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.ValidatorConfig;
import ru.intertrust.cm.core.config.gui.action.ToolBarConfig;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.api.server.plugin.ActivePluginHandler;
import ru.intertrust.cm.core.gui.impl.server.util.ActionConfigBuilder;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.PluginActionEntryContext;
import ru.intertrust.cm.core.gui.model.action.SaveActionContext;
import ru.intertrust.cm.core.gui.model.form.FormDisplayData;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginData;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginState;

/**
 * @author Denis Mitavskiy
 *         Date: 23.08.13
 *         Time: 13:22
 */
@ComponentName(FormPluginHandler.COMPONENT_NAME)
public class FormPluginHandler extends ActivePluginHandler {
    protected static final String COMPONENT_NAME = "form.plugin";
    @Autowired
    private GuiService guiService;

    public FormPluginData initialize(Dto initialData) {
        FormPluginConfig config = (FormPluginConfig) initialData;
        String domainObjectToCreate = config.getDomainObjectTypeToCreate();
        final UserInfo userInfo = GuiContext.get().getUserInfo();
        FormDisplayData form = domainObjectToCreate != null ? guiService.getForm(domainObjectToCreate, userInfo)
                : guiService.getForm(config.getDomainObjectId(), userInfo);
        FormPluginData pluginData = new FormPluginData();
        pluginData.setFormDisplayData(form);
        pluginData.setPluginState(config.getPluginState());
        pluginData.setActionContexts(getActions(config));
        PluginActionEntryContext actionEntryContext = getPluginActionEntryContext(form);
        return pluginData;
    }

    private List<ActionContext> getActions(FormPluginConfig config) {
        final List<ActionContext> actions = getActionContexts(config.getPluginState());

        final List<ActionContext> otherActions;
        if (config.getDomainObjectId() != null) {
            otherActions = actionService.getActions(config.getDomainObjectId());
        } else {
            otherActions = actionService.getActions(config.getDomainObjectTypeToCreate());
        }
        if (otherActions != null && !otherActions.isEmpty()) {
            actions.addAll(otherActions);
        }
        return actions;
    }

    private PluginActionEntryContext getPluginActionEntryContext(final FormDisplayData formData) {
        final ToolBarConfig toolbarConfig = formData.getToolBarConfig();
        if (toolbarConfig == null || toolbarConfig.isUseDefault()) {
            final ToolBarConfig defaultToolbar = actionService.getDefaultToolbarConfig(COMPONENT_NAME);
        }

        final PluginActionEntryContext ctx = new PluginActionEntryContext();


        return ctx;
    }

    private static List<ActionContext> getActionContexts(final FormPluginState pluginState) {
        final List<ActionContext> contexts = new ArrayList<>();
        boolean pluginIsCentralPanel = pluginState.isInCentralPanel();
        boolean toggleEdit = pluginState.isToggleEdit();
        boolean editable = pluginState.isEditable();
        if (pluginIsCentralPanel || (toggleEdit && editable)) {
            contexts.add(new ActionContext(ActionConfigBuilder.createActionConfig("close.in.central.panel.action",
                    "close.in.central.panel.action", "Закрыть", "icons/icon-edit-close.png")));
        }
        if (toggleEdit) {
            if (editable) {
                contexts.add(new SaveActionContext(ActionConfigBuilder.createActionConfig(
                        "save.action", "save.action", "Сохранить", "icons/icon-save.png",
                        Collections.singletonList(new ValidatorConfig(
                                "ru.intertrust.cm.core.gui.impl.server.validation.validators.custom.CapitalValidator",
                                "suggest_capital")), true)));
            } else {
                contexts.add(new ActionContext(ActionConfigBuilder.createActionConfig(
                        "create.new.object.action", "create.new.object.action",
                        "Создать новый", "icons/icon-create.png")));
                contexts.add(new ActionContext(ActionConfigBuilder.createActionConfig(
                        "toggle.edit.on.action", "toggle.edit.on.action", "Редактировать", "icons/icon-edit.png")));
                contexts.add(new SaveActionContext(ActionConfigBuilder.createActionConfig(
                        "delete.action", "delete.action", "Удалить", "icons/icon-delete.png")));
            }

        } else {
            contexts.add(new ActionContext(ActionConfigBuilder.createActionConfig("create.new.object.action",
                    "create.new.object.action", "Создать новый", "icons/icon-create.png")));
            contexts.add(new SaveActionContext(ActionConfigBuilder.createActionConfig(
                    "save.action", "save.action", "Сохранить", "icons/icon-save.png",
                    Collections.singletonList(new ValidatorConfig(
                            "ru.intertrust.cm.core.gui.impl.server.validation.validators.custom.CapitalValidator",
                            "suggest_capital")), true)));
            contexts.add(new SaveActionContext(ActionConfigBuilder.createActionConfig(
                    "delete.action", "delete.action", "Удалить", "icons/icon-delete.png")));

        }
        return contexts;
    }

}
