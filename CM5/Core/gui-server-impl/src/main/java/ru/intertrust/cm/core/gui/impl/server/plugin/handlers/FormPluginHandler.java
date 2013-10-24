package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.model.gui.ActionConfig;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.api.server.plugin.ActivePluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.SaveActionContext;
import ru.intertrust.cm.core.gui.model.form.FormDisplayData;
import ru.intertrust.cm.core.gui.model.plugin.ActivePluginData;
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

    public ActivePluginData initialize(Dto initialData) {
        FormPluginConfig config = (FormPluginConfig) initialData;
        String domainObjectToCreate = config.getDomainObjectTypeToCreate();
        FormDisplayData form = domainObjectToCreate != null ? guiService.getForm(domainObjectToCreate)
                : guiService.getForm(config.getDomainObjectId());

        FormPluginData pluginData = new FormPluginData();
        pluginData.setFormDisplayData(form);
        pluginData.setActionContexts(getActions(initialData));
        return pluginData;
    }

    public List<ActionContext> getActions(Dto initialData)  {
        ActionConfig saveActionConfig = new ActionConfig("save.action", "save.action");
        saveActionConfig.setText("Сохранить");
        ActionContext saveActionContext = new SaveActionContext();
        saveActionContext.setActionConfig(saveActionConfig);

        ActionConfig createNewActionConfig = new ActionConfig("create.new.object.action", "create.new.object.action");
        createNewActionConfig.setText("Создать новый");
        ActionContext createNewActionContext = new ActionContext();
        createNewActionContext.setActionConfig(createNewActionConfig);

        ArrayList<ActionContext> actions = new ArrayList<>();
        actions.add(saveActionContext);
        actions.add(createNewActionContext);
        List<ActionContext> otherActions = actionService.getActions(null);
        if (otherActions != null) {
            actions.addAll(otherActions);
        }

        return actions;
    }
}
