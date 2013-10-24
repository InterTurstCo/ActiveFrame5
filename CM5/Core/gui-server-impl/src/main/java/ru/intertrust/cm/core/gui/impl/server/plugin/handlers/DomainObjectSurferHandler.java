package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.model.gui.ActionConfig;
import ru.intertrust.cm.core.gui.api.server.ActionService;
import ru.intertrust.cm.core.gui.api.server.plugin.ActivePluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.SaveActionContext;
import ru.intertrust.cm.core.gui.model.plugin.ActivePluginData;
import ru.intertrust.cm.core.gui.model.plugin.DomainObjectSurferPluginData;

import java.util.ArrayList;
import java.util.List;

@ComponentName("domain.object.surfer.plugin")
public class DomainObjectSurferHandler extends ActivePluginHandler {
    @Autowired
    private ActionService actionService;

    public ActivePluginData initialize(Dto params) {
        DomainObjectSurferPluginData pluginData = new DomainObjectSurferPluginData();
        pluginData.setActionContexts(getActions());
        return pluginData;
    }

    public List<ActionContext> getActions()  {
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
