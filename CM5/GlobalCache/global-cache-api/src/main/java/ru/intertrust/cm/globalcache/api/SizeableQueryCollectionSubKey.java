package ru.intertrust.cm.globalcache.api;

import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.dao.access.UserSubject;
import ru.intertrust.cm.globalcache.api.util.Size;
import ru.intertrust.cm.globalcache.api.util.SizeEstimator;
import ru.intertrust.cm.globalcache.api.util.Sizeable;

import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 06.10.2015
 *         Time: 18:57
 */
public class SizeableQueryCollectionSubKey extends QueryCollectionSubKey implements Sizeable {
    private Size size;

    public SizeableQueryCollectionSubKey(UserSubject subject, List<? extends Value> paramValues, int offset, int limit) {
        super(subject, paramValues, offset, limit);
        size = new Size(SizeEstimator.estimateSize(this));
    }

    @Override
    public Size getSize() {
        return size;
    }
}
