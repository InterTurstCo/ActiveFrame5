package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.BalancerControlPluginData;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;

/**
 * Created by Ravil on 22.01.2018.
 */
@ComponentName("BalancerControl.plugin")
public class BalancerControlPluginHandler extends PluginHandler {
    public PluginData initialize(Dto config) {
        BalancerControlPluginData pData = new BalancerControlPluginData();
        return pData;
    }
}
