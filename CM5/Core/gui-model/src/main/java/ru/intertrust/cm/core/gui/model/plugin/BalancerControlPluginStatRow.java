package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Created by Ravil on 24.01.2018.
 */
public class BalancerControlPluginStatRow implements Dto {
    private String dataSource;
    private ServerState state;
    private Integer delay;
    private Integer delayDbms;
    private String tXId;
    private Integer percentageHitNow;
    private Integer faultsNow;
    private Integer selectSecNow;
    private Integer percentageHitHour;
    private Integer faultsHour;
    private Integer selectSecHour;

    public BalancerControlPluginStatRow(){}

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public ServerState getState() {
        return state;
    }

    public void setState(ServerState state) {
        this.state = state;
    }

    public Integer getDelay() {
        return delay;
    }

    public void setDelay(Integer delay) {
        this.delay = delay;
    }

    public Integer getDelayDbms() {
        return delayDbms;
    }

    public void setDelayDbms(Integer delayDbms) {
        this.delayDbms = delayDbms;
    }

    public String gettXId() {
        return tXId;
    }

    public void settXId(String tXId) {
        this.tXId = tXId;
    }

    public Integer getPercentageHitNow() {
        return percentageHitNow;
    }

    public void setPercentageHitNow(Integer percentageHitNow) {
        this.percentageHitNow = percentageHitNow;
    }

    public Integer getFaultsNow() {
        return faultsNow;
    }

    public void setFaultsNow(Integer faultsNow) {
        this.faultsNow = faultsNow;
    }

    public Integer getSelectSecNow() {
        return selectSecNow;
    }

    public void setSelectSecNow(Integer selectSecNow) {
        this.selectSecNow = selectSecNow;
    }

    public Integer getPercentageHitHour() {
        return percentageHitHour;
    }

    public void setPercentageHitHour(Integer percentageHitHour) {
        this.percentageHitHour = percentageHitHour;
    }

    public Integer getFaultsHour() {
        return faultsHour;
    }

    public void setFaultsHour(Integer faultsHour) {
        this.faultsHour = faultsHour;
    }

    public Integer getSelectSecHour() {
        return selectSecHour;
    }

    public void setSelectSecHour(Integer selectSecHour) {
        this.selectSecHour = selectSecHour;
    }
}
