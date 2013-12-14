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

        FormPluginData pluginData = new FormPluginData();
        pluginData.setFormDisplayData(form);
        pluginData.setActionContexts(getActions(config));
        return pluginData;
    }

    public List<ActionContext> getActions(FormPluginConfig config)  {
        ActionConfig saveActionConfig = new ActionConfig("save.action", "save.action");
        saveActionConfig.setText("Сохранить");
        saveActionConfig.setImageUrl("icons/ico-save.gif");
        ActionContext saveActionContext = new SaveActionContext();
        saveActionContext.setActionConfig(saveActionConfig);

        ActionConfig createNewActionConfig = new ActionConfig("create.new.object.action", "create.new.object.action");
        createNewActionConfig.setText("Создать новый");
        createNewActionConfig.setImageUrl("icons/icon-create.png");
        ActionContext createNewActionContext = new ActionContext();
        createNewActionContext.setActionConfig(createNewActionConfig);

        ActionConfig deleteActionConfig = new ActionConfig("delete.action", "delete.action");
        deleteActionConfig.setText("Удалить");
        deleteActionConfig.setImageUrl("icons/ico-delete.gif");
        ActionContext deleteActionContext = new SaveActionContext();
        deleteActionContext.setActionConfig(deleteActionConfig);

        ArrayList<ActionContext> actions = new ArrayList<>();
        actions.add(createNewActionContext);
        actions.add(saveActionContext);
        actions.add(deleteActionContext);

        List<ActionContext> otherActions = null;
        if (config.getDomainObjectId() != null){
            otherActions = actionService.getActions(config.getDomainObjectId());
        }else{
            otherActions = actionService.getActions(config.getDomainObjectTypeToCreate());
        }
        if (otherActions != null) {
            actions.addAll(otherActions);
        }

        return actions;
    }
}
