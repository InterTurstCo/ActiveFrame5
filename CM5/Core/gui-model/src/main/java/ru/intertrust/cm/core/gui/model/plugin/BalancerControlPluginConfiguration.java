package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Created by Ravil on 25.01.2018.
 */
public class BalancerControlPluginConfiguration implements Dto {
    private Long faultRequest = 0L;
    private Long problemDelay = 0L;

    public Long getFaultRequest() {
        return faultRequest;
    }

    public void setFaultRequest(Long faultRequest) {
        this.faultRequest = faultRequest;
    }

    public Long getProblemDelay() {
        return problemDelay;
    }

    public void setProblemDelay(Long problemDelay) {
        this.problemDelay = problemDelay;
    }
}
