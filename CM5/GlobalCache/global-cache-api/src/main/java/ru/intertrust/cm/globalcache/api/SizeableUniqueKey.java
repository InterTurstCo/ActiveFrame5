package ru.intertrust.cm.globalcache.api;

import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.globalcache.api.util.Size;
import ru.intertrust.cm.globalcache.api.util.SizeEstimator;
import ru.intertrust.cm.globalcache.api.util.Sizeable;

import java.util.Map;

/**
 * @author Denis Mitavskiy
 *         Date: 05.11.2015
 *         Time: 19:57
 */
public class SizeableUniqueKey extends UniqueKey implements Sizeable {
    private Size size;

    public SizeableUniqueKey() {
    }

    public SizeableUniqueKey(Map<String, Value> map) {
        super(map);
        this.size = new Size(SizeEstimator.estimateSize(this.map) + Integer.SIZE + SizeEstimator.REFERENCE_SIZE);
    }


    @Override
    public Size getSize() {
        return size;
    }
}
