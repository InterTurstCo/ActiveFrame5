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

import java.util.Locale;

/**
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 21.10.2015
 */
@ComponentName("GlobalCacheControl.plugin")
public class GlobalCacheControlPluginHandler extends PluginHandler {
    private static final String GLOBAL_CACHE_OFFLINE = "Глобальный кэш недоступен";
    private final static double MEGABYTE = 1024 * 1024;

    @Autowired
    private GlobalCacheManager globalCacheManager;

    private GlobalCacheStatistics globalCacheStatistics;

    public PluginData initialize(Dto config) {
        GlobalCachePluginData globalCachePluginData = new GlobalCachePluginData();
        extractStatistics(globalCachePluginData);
        return globalCachePluginData;
    }

    private void extractStatistics(GlobalCachePluginData globalCachePluginData){
        globalCacheStatistics = globalCacheManager.getStatistics();
        if(globalCacheStatistics==null){
            globalCachePluginData.setErrorMsg(GLOBAL_CACHE_OFFLINE);
        } else {
            globalCachePluginData.setStatPanel(new GlobalCacheStatPanel());
            globalCachePluginData.getStatPanel().setSize(format(globalCacheStatistics.getSize() / MEGABYTE));
            globalCachePluginData.getStatPanel().setFreeSpacePercentage(format(globalCacheStatistics.getFreeSpacePercentage() * 100));
            globalCachePluginData.getStatPanel().setHitCount(format(globalCacheStatistics.getHitCount() * 100));

            globalCachePluginData.getStatPanel().setFreedSpaceAvg(format(globalCacheStatistics.getCacheCleaningRecord().getFreedSpaceAvg() * 100) + "% ");
            globalCachePluginData.getStatPanel().setFreedSpaceMax(format(globalCacheStatistics.getCacheCleaningRecord().getFreedSpaceMax() * 100) + "% ");
            globalCachePluginData.getStatPanel().setFreedSpaceMin(format(globalCacheStatistics.getCacheCleaningRecord().getFreedSpaceMin() * 100) + "% ");

            globalCachePluginData.getStatPanel().setTimeAvg(format(globalCacheStatistics.getCacheCleaningRecord().getTimeAvg() / 1000));
            globalCachePluginData.getStatPanel().setTimeMin(format(globalCacheStatistics.getCacheCleaningRecord().getTimeMin() / 1000));
            globalCachePluginData.getStatPanel().setTimeMax(format(globalCacheStatistics.getCacheCleaningRecord().getTimeMax() / 1000));
            globalCachePluginData.getStatPanel().setTotalInvocations(String.valueOf(globalCacheStatistics.getCacheCleaningRecord().getInvocations()));

        }

    }

    private static String format(double v) {
        return String.format(Locale.ENGLISH, "%1$.2f", v);
    }

    public GlobalCachePluginData refreshStatistics(Dto request){
        GlobalCachePluginData globalCachePluginData = (GlobalCachePluginData)request;
        extractStatistics(globalCachePluginData);
        return globalCachePluginData;
    }

    public Dto resetAllStatistics(Dto request){
        globalCacheManager.clearStatistics(false);
        return request;
    }

    public Dto resetHourlyStatistics(Dto request){
        globalCacheManager.clearStatistics(false);
        return request;
    }
}
