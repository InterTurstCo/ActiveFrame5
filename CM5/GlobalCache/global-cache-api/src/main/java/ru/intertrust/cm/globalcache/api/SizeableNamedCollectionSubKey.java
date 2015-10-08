package ru.intertrust.cm.globalcache.api;

import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.dao.access.UserSubject;
import ru.intertrust.cm.globalcache.api.util.Size;
import ru.intertrust.cm.globalcache.api.util.SizeEstimator;
import ru.intertrust.cm.globalcache.api.util.Sizeable;

import java.util.Set;

/**
 * @author Denis Mitavskiy
 *         Date: 06.10.2015
 *         Time: 18:57
 */
public class SizeableNamedCollectionSubKey extends NamedCollectionSubKey implements Sizeable {
    private Size size;

    public SizeableNamedCollectionSubKey(UserSubject subject, Set<? extends Filter> filterValues, SortOrder sortOrder, int offset, int limit) {
        super(subject, filterValues, sortOrder, offset, limit);
        size = new Size(SizeEstimator.estimateSize(this));
    }

    @Override
    public Size getSize() {
        return size;
    }
}
