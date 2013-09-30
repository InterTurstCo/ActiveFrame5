package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.model.gui.ActionConfig;
import ru.intertrust.cm.core.gui.api.server.plugin.ActivePluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.ActivePluginData;
import ru.intertrust.cm.core.gui.model.plugin.DomainObjectSurferPluginData;

import java.util.ArrayList;
import java.util.List;

@ComponentName("domain.object.surfer.plugin")
public class DomainObjectSurferHandler extends ActivePluginHandler {

    public ActivePluginData initialize(Dto params) {
        DomainObjectSurferPluginData pluginData = new DomainObjectSurferPluginData();
        pluginData.setActionConfigs(getActions());
        return pluginData;
    }

    public List<ActionConfig> getActions()  {
        ActionConfig saveAction = new ActionConfig("save.action", "save.action");
        saveAction.setText("Сохранить");
        ActionConfig createNewAction = new ActionConfig("create.new.object.action", "create.new.object.action");
        createNewAction.setText("Создать новый");
        ArrayList<ActionConfig> actions = new ArrayList<>();
        actions.add(saveAction);
        actions.add(createNewAction);
        return actions;
    }

}
