package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.GlobalCacheStatistics;
import ru.intertrust.cm.core.dao.api.GlobalCacheManager;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.GlobalCachePluginData;
import ru.intertrust.cm.core.gui.model.plugin.GlobalCacheStatPanel;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;

/**
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 21.10.2015
 */
@ComponentName("GlobalCacheControl.plugin")
public class GlobalCacheControlPluginHandler extends PluginHandler {
    private static final String GLOBAL_CACHE_OFFLINE = "Глобальный кэш недоступен";

    @Autowired
    private GlobalCacheManager globalCacheManager;

    private GlobalCacheStatistics globalCacheStatistics;

    public PluginData initialize(Dto config) {
        GlobalCachePluginData globalCachePluginData = new GlobalCachePluginData();
        globalCacheStatistics = globalCacheManager.getStatistics();
        if(globalCacheStatistics!=null){
            globalCachePluginData.setErrorMsg(GLOBAL_CACHE_OFFLINE);
            return globalCachePluginData;
        }
        globalCachePluginData.setStatPanel(new GlobalCacheStatPanel());
        globalCachePluginData.getStatPanel().setSize(globalCacheStatistics.getSize());
        globalCachePluginData.getStatPanel().setFreeSpacePercentage(globalCacheStatistics.getFreeSpacePercentage());
        globalCachePluginData.getStatPanel().setHitCount(globalCacheStatistics.getHitCount());
        return globalCachePluginData;
    }
}
