package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.listplugin.ListPluginData;

/**
 * Created by Ravil on 17.04.2017.
 */
@ComponentName("list.plugin")
public class ListPluginHandler extends PluginHandler {
    public ListPluginData initialize(Dto config) {
        return new ListPluginData();
    }
}
