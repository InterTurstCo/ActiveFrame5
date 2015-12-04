package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 27.10.2015
 */
public class GlobalCacheControlPanel implements Dto {
    public static final String VALUE_MODES_SYNC = "Синхронный";
    public static final String VALUE_MODES_ASYNC = "Асинхронный";
    public static final String VALUE_UOM_MB = "Мб";
    public static final String VALUE_UOM_GB = "Гб";
    public static final String VALUE_MODE_BLOCKING = "blocking";
    public static final String VALUE_MODE_NON_BLOCKING = "non-blocking";
    public static final String VALUE_UOM_MEGABYTE = "M";
    public static final String VALUE_UOM_GIGABYTE = "G";

    /**
     * Кэш включен
     */
    private Boolean cacheEnabled;
    /**
     * Расширенная статистика
     */
    private Boolean expandedStatistics;
    /**
     * Режим отладки
     */
    private Boolean debugMode;
    /**
     * Режим работы (синхронный/асинхронный)
     */
    private String mode;
    /**
     * Максимальный размер
     */
    private Long maxSize;
    /**
     * Единицы измерения размера (Мб/Гб)
     */
    private String sizeUom;
    /**
     * Режимы работы кэша
     */
    private Map<String, String> modes;
    /**
     * Единицы измерения максимального размера
     */
    private Map<String, String> uoms;

    public String getSizeUom() {
        return sizeUom;
    }

    public void setSizeUom(String sizeUom) {
        this.sizeUom = sizeUom;
    }

    public Long getMaxSize() {
        if (maxSize == null)
            return new Long(0);
        else
            return maxSize;
    }

    public void setMaxSize(Long maxSize) {
        this.maxSize = maxSize;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(Boolean debugMode) {
        this.debugMode = debugMode;
    }

    public Boolean isExpandedStatistics() {
        return expandedStatistics;
    }

    public void setExpandedStatistics(Boolean expandedStatistics) {
        this.expandedStatistics = expandedStatistics;
    }

    public Boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public void setCacheEnabled(Boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    public Map<String, String> getModes() {
        return modes;
    }

    public void setModes(Map<String, String> modes) {
        this.modes = modes;
    }

    public Map<String, String> getUoms() {
        return uoms;
    }

    public void setUoms(Map<String, String> uoms) {
        this.uoms = uoms;
    }

    public GlobalCacheControlPanel() {
        modes = new HashMap<>();
        modes.put("blocking", VALUE_MODES_SYNC);
        modes.put("non-blocking", VALUE_MODES_ASYNC);

        uoms = new HashMap<>();
        uoms.put(VALUE_UOM_MEGABYTE, VALUE_UOM_MB);
        uoms.put(VALUE_UOM_GIGABYTE, VALUE_UOM_GB);

        sizeUom = VALUE_UOM_MEGABYTE;
    }

    public int getModeIndex(){
       if(getMode().equals(VALUE_MODE_BLOCKING))
           return 0;
       else
           return 1;
    }

    public int getUomIndex(){
        if(getSizeUom().equals(VALUE_UOM_MEGABYTE))
            return 1;
        else
            return 0;
    }

}
