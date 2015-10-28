package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.GlobalCachePluginData;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;

/**
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 21.10.2015
 */
@ComponentName("GlobalCacheControl.plugin")
public class GlobalCacheControlPluginHandler extends PluginHandler {

    public PluginData initialize(Dto config) {
        GlobalCachePluginData globalCachePluginData = new GlobalCachePluginData();
        globalCachePluginData.setCacheData("Cache data");
        return globalCachePluginData;
    }
}
