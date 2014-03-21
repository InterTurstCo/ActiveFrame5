package ru.intertrust.cm.core.gui.model.counters;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.Map;

/**
 * Created by andrey on 18.03.14.
 */
public class CollectionCountersResponse implements Dto {
    private Map<CounterKey, Long> counterValues;
    private long lastUpdatedTime;
    private Map<CounterKey, Id> counterServerObjectIds;

    public void setCounterValues(Map<CounterKey, Long> counterValues) {
        this.counterValues = counterValues;
    }

    public Map<CounterKey, Long> getCounterValues() {
        return counterValues;
    }

    public void setLastUpdatedTime(long lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    public long getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setCounterServerObjectIds(Map<CounterKey, Id> counterServerObjectIds) {
        this.counterServerObjectIds = counterServerObjectIds;
    }

    public Map<CounterKey, Id> getCounterServerObjectIds() {
        return counterServerObjectIds;
    }
}
