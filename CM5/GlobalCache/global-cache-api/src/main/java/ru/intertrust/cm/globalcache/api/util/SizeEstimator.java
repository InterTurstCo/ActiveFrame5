package ru.intertrust.cm.globalcache.api.util;

import com.carrotsearch.sizeof.RamUsageEstimator;

/**
 * @author Denis Mitavskiy
 *         Date: 22.08.2015
 *         Time: 18:00
 */
public class SizeEstimator {
    public static final long REFERENCE_SIZE = RamUsageEstimator.NUM_BYTES_OBJECT_REF;

    public static long estimateSize(Object object) {
        if (object == null) {
            return 0;
        }
        return RamUsageEstimator.sizeOf(object);
    }
}
