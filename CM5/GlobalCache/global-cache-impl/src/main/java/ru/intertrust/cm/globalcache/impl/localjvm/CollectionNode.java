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
    private int count;
    private long timeRetrieved;
    private Size size;

    public CollectionNode(IdentifiableObjectCollection collection, long timeRetrieved) {
        this(collection, collection.size(), timeRetrieved);
    }

    public CollectionNode(int count, long timeRetrieved) {
        this(null, count, timeRetrieved);
    }

    private CollectionNode(IdentifiableObjectCollection collection, int count, long timeRetrieved) {
        this.collection = collection;
        this.count = count;
        this.timeRetrieved = timeRetrieved;
        this.size = new Size(SizeEstimator.estimateSize(this));
    }

    public IdentifiableObjectCollection getCollection() {
        return collection;
    }

    public long getTimeRetrieved() {
        return timeRetrieved;
    }

    public int getCount() {
        return count;
    }

    @Override
    public Size getSize() {
        return size;
    }
}
