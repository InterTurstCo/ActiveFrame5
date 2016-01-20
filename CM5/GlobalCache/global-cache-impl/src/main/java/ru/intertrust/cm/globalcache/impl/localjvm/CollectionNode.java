package ru.intertrust.cm.globalcache.impl.localjvm;

import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.globalcache.api.util.Size;
import ru.intertrust.cm.globalcache.api.util.SizeEstimator;
import ru.intertrust.cm.globalcache.api.util.Sizeable;

/**
 * @author Denis Mitavskiy
 *         Date: 11.08.2015
 *         Time: 16:30
 */
public class CollectionNode implements Sizeable {
    private IdentifiableObjectCollection collection;
    private long timeRetrieved;
    private Size size;

    public CollectionNode(IdentifiableObjectCollection collection, long timeRetrieved) {
        this.collection = collection;
        this.timeRetrieved = timeRetrieved;
        this.size = new Size(SizeEstimator.estimateSize(this));
    }

    public IdentifiableObjectCollection getCollection() {
        return collection;
    }

    public long getTimeRetrieved() {
        return timeRetrieved;
    }

    @Override
    public Size getSize() {
        return size;
    }
}
