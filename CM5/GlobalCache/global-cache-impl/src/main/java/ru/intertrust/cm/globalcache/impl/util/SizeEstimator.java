package ru.intertrust.cm.globalcache.impl.util;

import com.carrotsearch.sizeof.RamUsageEstimator;

/**
 * @author Denis Mitavskiy
 *         Date: 22.08.2015
 *         Time: 18:00
 */
public class SizeEstimator {
    public static long estimateSize(Object object) {
        if (object == null) {
            return 0;
        }
        return RamUsageEstimator.sizeOf(object);
    }

    public static long getReferenceSize() {
        return RamUsageEstimator.NUM_BYTES_OBJECT_REF;
    }
}
