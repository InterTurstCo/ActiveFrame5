package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.api.server.plugin.ActivePluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.plugin.ActivePluginData;
import ru.intertrust.cm.core.gui.model.plugin.DomainObjectSurferPluginData;

import java.util.List;

@ComponentName("domain.object.surfer.plugin")
public class DomainObjectSurferHandler extends ActivePluginHandler {
    public ActivePluginData initialize(Dto params) {
        DomainObjectSurferPluginData pluginData = new DomainObjectSurferPluginData();
        pluginData.setActionContexts(getActions(params));
        return pluginData;
    }

    public List<ActionContext> getActions(Dto params)  {
        return null;
    }

}
