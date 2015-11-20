package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.GlobalCacheStatistics;

import java.util.Collection;
import java.util.List;

/**
 * Модель данных панели статистики
 *
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 13.11.2015
 */
public class GlobalCacheStatPanel implements Dto {

    private String size;
    private String freeSpacePercentage;
    private String hitCount;
    private String timeAvg;
    private String timeMin;
    private String timeMax;
    private String freedSpaceAvg;
    private String freedSpaceMin;
    private String freedSpaceMax;
    private String totalInvocations;
    private List<GlobalCacheStatistics.Record> notifierRecords;
    private GlobalCacheStatistics.Record notifierSummary;


    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getFreeSpacePercentage() {
        return freeSpacePercentage;
    }

    public void setFreeSpacePercentage(String freeSpacePercentage) {
        this.freeSpacePercentage = freeSpacePercentage;
    }

    public String getHitCount() {
        return hitCount;
    }

    public void setHitCount(String hitCount) {
        this.hitCount = hitCount;
    }

    public GlobalCacheStatPanel() {
        this.size = "";
        this.freeSpacePercentage = "";
        this.hitCount = "";
        this.timeAvg = "";
        this.timeMax = "";
        this.timeMin = "";
        this.freedSpaceAvg = "";
        this.freedSpaceMin = "";
        this.freedSpaceMax = "";
        this.totalInvocations = "";
    }

    public String getTimeAvg() {
        return timeAvg;
    }

    public void setTimeAvg(String timeAvg) {
        this.timeAvg = timeAvg;
    }

    public String getTimeMin() {
        return timeMin;
    }

    public void setTimeMin(String timeMin) {
        this.timeMin = timeMin;
    }

    public String getTimeMax() {
        return timeMax;
    }

    public void setTimeMax(String timeMax) {
        this.timeMax = timeMax;
    }

    public String getFreedSpaceAvg() {
        return freedSpaceAvg;
    }

    public void setFreedSpaceAvg(String freedSpaceAvg) {
        this.freedSpaceAvg = freedSpaceAvg;
    }

    public String getFreedSpaceMin() {
        return freedSpaceMin;
    }

    public void setFreedSpaceMin(String freedSpaceMin) {
        this.freedSpaceMin = freedSpaceMin;
    }

    public String getFreedSpaceMax() {
        return freedSpaceMax;
    }

    public void setFreedSpaceMax(String freedSpaceMax) {
        this.freedSpaceMax = freedSpaceMax;
    }

    public String getTotalInvocations() {
        return totalInvocations;
    }

    public void setTotalInvocations(String totalInvocations) {
        this.totalInvocations = totalInvocations;
    }

    public List<GlobalCacheStatistics.Record> getNotifierRecords() {
        return notifierRecords;
    }

    public void setNotifierRecords(List<GlobalCacheStatistics.Record> notifierRecords) {
        this.notifierRecords = notifierRecords;
    }

    public GlobalCacheStatistics.Record getNotifierSummary() {
        return notifierSummary;
    }

    public void setNotifierSummary(GlobalCacheStatistics.Record notifierSummary) {
        this.notifierSummary = notifierSummary;
    }
}
