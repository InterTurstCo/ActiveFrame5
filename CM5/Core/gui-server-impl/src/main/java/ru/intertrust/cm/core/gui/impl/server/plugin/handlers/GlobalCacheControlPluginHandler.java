package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.PersonManagementService;
import ru.intertrust.cm.core.business.api.PersonService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.GlobalCacheStatistics;
import ru.intertrust.cm.core.dao.api.GlobalCacheManager;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.GlobalCachePluginData;
import ru.intertrust.cm.core.gui.model.plugin.GlobalCacheStatPanel;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 21.10.2015
 */
@ComponentName("GlobalCacheControl.plugin")
public class GlobalCacheControlPluginHandler extends PluginHandler {
    private static final String GLOBAL_CACHE_OFFLINE = "Глобальный кэш недоступен";
    private final static double MEGABYTE = 1024 * 1024;
    private final static String FIELD_GROUP_NAME = "group_name";
    private final static String SUPERUSERS_GROUP = "Superusers";
    private final static String ADMINISTRATORS_GROUP = "Administrators";


    @Autowired
    private GlobalCacheManager globalCacheManager;

    @Autowired
    PersonService personService;

    @Autowired
    PersonManagementService personMamagementService;


    private GlobalCachePluginData globalCachePluginData;

    private GlobalCacheStatistics globalCacheStatistics;

    public PluginData initialize(Dto config) {
        globalCachePluginData = new GlobalCachePluginData();
        getSettings();
        extractStatistics(globalCachePluginData);
        for(DomainObject group : personMamagementService.getPersonGroups(personService.getCurrentPerson().getId())){
             if(group.getString(FIELD_GROUP_NAME).toLowerCase().equals(SUPERUSERS_GROUP.toLowerCase()) ||
                     group.getString(FIELD_GROUP_NAME).toLowerCase().equals(ADMINISTRATORS_GROUP.toLowerCase())){
                 globalCachePluginData.setSuperUser(true);
             }
        }
        return globalCachePluginData;
    }

    private void extractStatistics(GlobalCachePluginData globalCachePluginData){
        globalCacheStatistics = globalCacheManager.getStatistics();
        globalCachePluginData.setStatPanel(new GlobalCacheStatPanel());
        if(globalCacheStatistics==null){
            globalCachePluginData.setErrorMsg(GLOBAL_CACHE_OFFLINE);
        } else {
            /**
             * Краткая статистика
             */
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

            /**
             * Развернутая статистика
             */
            globalCachePluginData.getStatPanel().setNotifierRecords(globalCacheStatistics.getNotifiersRecords());
            globalCachePluginData.getStatPanel().setNotifierSummary(globalCacheStatistics.getNotifiersSummary());
            globalCachePluginData.getStatPanel().setReadersRecords(globalCacheStatistics.getReadersRecords());
            globalCachePluginData.getStatPanel().setReaderSummary(globalCacheStatistics.getReadersSummary());
            globalCachePluginData.getStatPanel().setGlobalSummary(globalCacheStatistics.getAllMethodsSummary());
        }

    }

    private void getSettings(){
        globalCachePluginData.getControlPanelModel().setCacheEnabled(globalCacheManager.isEnabled());
        globalCachePluginData.getControlPanelModel().setExpandedStatistics(globalCacheManager.isExtendedStatisticsEnabled());
        globalCachePluginData.getControlPanelModel().setDebugMode(globalCacheManager.isDebugEnabled());

        Map<String, Serializable> settings = globalCacheManager.getSettings();
        globalCachePluginData.getControlPanelModel().setMode((String)settings.get("global.cache.mode"));
        globalCachePluginData.getControlPanelModel().setMaxSize((Long)settings.get("global.cache.max.size"));
        globalCachePluginData.getControlPanelModel().setWaitLockMillies((Integer)settings.get("global.cache.wait.lock.millies"));

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
        globalCacheManager.clearStatistics(true);
        return request;
    }

    public Dto clearCache(Dto request){
        globalCacheManager.clear();
        return request;
    }

    public Dto applySettings(Dto request){
        GlobalCachePluginData globalCachePluginData = (GlobalCachePluginData)request;
        globalCacheManager.setEnabled(globalCachePluginData.getControlPanelModel().isCacheEnabled());
        globalCacheManager.setDebugEnabled(globalCachePluginData.getControlPanelModel().isDebugMode());
        globalCacheManager.setExtendedStatisticsEnabled(globalCachePluginData.getControlPanelModel().isExpandedStatistics());

        Long maxSize;
        if(globalCachePluginData.getControlPanelModel().getSizeUom().equals("M")){
            maxSize = globalCachePluginData.getControlPanelModel().getMaxSize()*1024*1024;
        }
        else {
            maxSize = globalCachePluginData.getControlPanelModel().getMaxSize()*1000*1024*1024;
        }

        Map<String, Serializable> settings = new HashMap<>(globalCacheManager.getSettings());
        if(globalCachePluginData.getControlPanelModel().getMaxSize()!=0){
            settings.put("global.cache.max.size",maxSize);
        }
        settings.put("global.cache.mode",globalCachePluginData.getControlPanelModel().getMode());
        settings.put("global.cache.wait.lock.millies", globalCachePluginData.getControlPanelModel().getWaitLockMillies());
        globalCacheManager.applySettings(settings);

        globalCachePluginData.getControlPanelModel().setCacheEnabled(globalCacheManager.isEnabled());
        globalCachePluginData.getControlPanelModel().setDebugMode(globalCacheManager.isDebugEnabled());
        globalCachePluginData.getControlPanelModel().setExpandedStatistics(globalCacheManager.isExtendedStatisticsEnabled());
        globalCachePluginData.getControlPanelModel().setMaxSize((Long)settings.get("global.cache.max.size"));
        globalCachePluginData.getControlPanelModel().setWaitLockMillies((Integer)settings.get("global.cache.wait.lock.millies"));
        return request;
    }

}
