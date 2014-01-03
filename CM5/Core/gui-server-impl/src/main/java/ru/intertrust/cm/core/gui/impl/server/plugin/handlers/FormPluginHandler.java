package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.ActionConfig;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.api.server.plugin.ActivePluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.SaveActionContext;
import ru.intertrust.cm.core.gui.model.form.FormDisplayData;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginData;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginMode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 23.08.13
 *         Time: 13:22
 */
@ComponentName("form.plugin")
public class FormPluginHandler extends ActivePluginHandler {
    @Autowired
    private GuiService guiService;

    public FormPluginData initialize(Dto initialData) {
        FormPluginConfig config = (FormPluginConfig) initialData;
        String domainObjectToCreate = config.getDomainObjectTypeToCreate();
        FormDisplayData form = domainObjectToCreate != null ? guiService.getForm(domainObjectToCreate)
                : guiService.getForm(config.getDomainObjectId());
        form.setEditable(config.isEditable());
        FormPluginData pluginData = new FormPluginData();
        pluginData.setFormDisplayData(form);
        pluginData.setMode(config.getMode());
        pluginData.setActionContexts(getActions(config));
        return pluginData;
    }

    public List<ActionContext> getActions(FormPluginConfig config)  {
        final List<ActionContext> actions = getActionContexts(config.getMode(), config.isEditable());

        final List<ActionContext> otherActions;
        if (config.getDomainObjectId() != null){
            otherActions = actionService.getActions(config.getDomainObjectId());
        }else{
            otherActions = actionService.getActions(config.getDomainObjectTypeToCreate());
        }
        if (otherActions != null && !otherActions.isEmpty()) {
            actions.addAll(otherActions);
        }

        return actions;
    }

    private static List<ActionContext> getActionContexts(final FormPluginMode mode, final boolean isEditable) {
        final List<ActionContext> contexts = new ArrayList<>();
        switch (mode) {
            case EDITABLE:
                contexts.add(new ActionContext(createActionConfig("create.new.object.action",
                        "create.new.object.action", "Создать новый", "icons/icon-create.png")));
                contexts.add(new SaveActionContext(createActionConfig(
                        "save.action", "save.action", "Сохранить", "icons/ico-save.gif")));
                contexts.add(new SaveActionContext(createActionConfig(
                        "delete.action", "delete.action", "Удалить", "icons/ico-delete.gif")));
                break;
            case MANUAL_EDIT:
                if (isEditable) {
                    contexts.add(new SaveActionContext(createActionConfig(
                            "save.action", "save.action", "Сохранить", "icons/ico-save.gif")));
                    contexts.add(new ActionContext(createActionConfig("cancel.edit.action",
                            "cancel.edit.action", "Завершить редактирование", "icons/ico-edit-close.png")));
                } else {
                    contexts.add(new ActionContext(createActionConfig(
                            "create.new.object.action", "create.new.object.action",
                            "Создать новый", "icons/icon-create.png")));
                    contexts.add(new ActionContext(createActionConfig(
                            "edit.action", "edit.action", "Редактировать", "icons/icon-edit.png")));
                    contexts.add(new SaveActionContext(createActionConfig(
                            "delete.action", "delete.action", "Удалить", "icons/ico-delete.gif")));
                }
                break;
        }
        return contexts;
    }

    private static ActionConfig createActionConfig(final String name, final String component,
                                                     final String label, final String imageUrl) {
        final ActionConfig config = new ActionConfig(name, component);
        config.setText(label);
        config.setImageUrl(imageUrl);
        return config;
    }
}
