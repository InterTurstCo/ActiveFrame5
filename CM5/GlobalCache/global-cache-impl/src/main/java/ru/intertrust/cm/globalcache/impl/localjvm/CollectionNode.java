package ru.intertrust.cm.globalcache.impl.localjvm;

import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;

/**
 * @author Denis Mitavskiy
 *         Date: 11.08.2015
 *         Time: 16:30
 */
public class CollectionNode {
    private IdentifiableObjectCollection collection;
    private long timeRetrieved;

    public CollectionNode(IdentifiableObjectCollection collection, long timeRetrieved) {
        this.collection = collection;
        this.timeRetrieved = timeRetrieved;
    }

    public IdentifiableObjectCollection getCollection() {
        return collection;
    }

    public long getTimeRetrieved() {
        return timeRetrieved;
    }
}
