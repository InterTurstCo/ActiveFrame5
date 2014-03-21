package ru.intertrust.cm.core.gui.model.counters;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.navigation.LinkConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by andrey on 18.03.14.
 */
public class CollectionCountersRequest implements Dto {
    private Map<CounterKey, Id> links;
    private long lastUpdatedTime;
    private HashMap<String, Id> linksObjects;

    public Map<CounterKey, Id> getCounterKeys() {
        return links;
    }

    public void setCounterKeys(Map<CounterKey, Id> links) {
        this.links = links;
    }

    public long getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(long lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    public void setLinksObjects(HashMap<String, Id> linksObjects) {
        this.linksObjects = linksObjects;
    }

    public HashMap<String, Id> getLinksObjects() {
        return linksObjects;
    }
}
