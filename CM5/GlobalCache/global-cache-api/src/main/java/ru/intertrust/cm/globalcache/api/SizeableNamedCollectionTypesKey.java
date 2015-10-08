package ru.intertrust.cm.globalcache.api;

import ru.intertrust.cm.core.dao.dto.NamedCollectionTypesKey;
import ru.intertrust.cm.globalcache.api.util.Size;
import ru.intertrust.cm.globalcache.api.util.SizeEstimator;
import ru.intertrust.cm.globalcache.api.util.Sizeable;

import java.util.Set;

/**
 * @author Denis Mitavskiy
 *         Date: 06.10.2015
 *         Time: 15:40
 */
public class SizeableNamedCollectionTypesKey extends NamedCollectionTypesKey implements Sizeable {
    private Size size;

    public SizeableNamedCollectionTypesKey(String name, Set<String> filterNames) {
        super(name, filterNames);
        size = new Size(SizeEstimator.estimateSize(this));
    }

    @Override
    public Size getSize() {
        return size;
    }
}
