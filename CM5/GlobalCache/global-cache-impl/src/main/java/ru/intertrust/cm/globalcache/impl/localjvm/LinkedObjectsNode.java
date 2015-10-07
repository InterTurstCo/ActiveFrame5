package ru.intertrust.cm.globalcache.impl.localjvm;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.globalcache.api.util.Size;
import ru.intertrust.cm.globalcache.api.util.SizeEstimator;
import ru.intertrust.cm.globalcache.api.util.Sizeable;

import java.util.*;

/**
 * @author Denis Mitavskiy
 *         Date: 09.07.2015
 *         Time: 19:54
 */
public class LinkedObjectsNode implements Sizeable {
    public static final long SELF_SIZE = 2 * SizeEstimator.getReferenceSize() + 1;

    private final Set<Id> domainObjectsIds;
    private Size size;
    private transient boolean sortNeeded;

    public LinkedObjectsNode(Set<Id> domainObjectsIds) {
        if (!(domainObjectsIds instanceof LinkedHashSet)) {
            sortNeeded = true;
        }
        this.domainObjectsIds = Collections.synchronizedSet(domainObjectsIds);
        size = new Size(SizeEstimator.estimateSize(domainObjectsIds) + SELF_SIZE);
    }

    @Override
    public Size getSize() {
        return size;
    }

    public List<Id> getIds() {
        if (domainObjectsIds.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        ArrayList<Id> result = new ArrayList<>(domainObjectsIds.size());
        synchronized (domainObjectsIds) {
            result.addAll(domainObjectsIds);
        }
        if (sortNeeded) {
            Collections.sort(result, new Comparator<Id>() {
                @Override
                public int compare(Id o1, Id o2) {
                    return o1.toStringRepresentation().compareTo(o2.toStringRepresentation());
                }
            });
        }
        return result;
    }

    public void add(Id id) {
        final boolean wasSortNeeded = sortNeeded;
        sortNeeded = true;
        final boolean added = domainObjectsIds.add(id);
        if (!wasSortNeeded && !added) {
            sortNeeded = false;
        }
        if (added) {
            size.add(SizeEstimator.estimateSize(id));
        }
    }

    public void remove(Id id) {
        final boolean removed = domainObjectsIds.remove(id);
        if (removed) {
            size.add(-SizeEstimator.estimateSize(id));
        }
    }

    public boolean isSortNeeded() {
        return sortNeeded;
    }
}
