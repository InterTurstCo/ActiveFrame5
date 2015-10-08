package ru.intertrust.cm.globalcache.api;

import ru.intertrust.cm.core.dao.dto.QueryCollectionTypesKey;
import ru.intertrust.cm.globalcache.api.util.Size;
import ru.intertrust.cm.globalcache.api.util.SizeEstimator;
import ru.intertrust.cm.globalcache.api.util.Sizeable;

/**
 * @author Denis Mitavskiy
 *         Date: 06.10.2015
 *         Time: 15:41
 */
public class SizeableQueryCollectionTypesKey extends QueryCollectionTypesKey implements Sizeable {
    private Size size;

    public SizeableQueryCollectionTypesKey(String query) {
        super(query);
        size = new Size(SizeEstimator.estimateSize(this));
    }

    @Override
    public Size getSize() {
        return size;
    }
}
