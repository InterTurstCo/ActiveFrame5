package ru.intertrust.cm.core.gui.impl.server.plugin;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.plugin.PluginService;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.action.SimpleActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionData;

@ComponentName("plugin-execute")
public class PluginkExecuteAction extends ActionHandler<SimpleActionContext, SimpleActionData> {

    @Autowired
    private PluginService pluginService;

    @Override
    public SimpleActionData executeAction(SimpleActionContext context) {
        Id pluginId = context.getRootObjectId();
        if (pluginId == null) {
            throw new GuiException("Плагин для запуска не выбран");
        }
        pluginService.executePlugin(pluginId.toStringRepresentation(), null);
        return new SimpleActionData();
    }

}