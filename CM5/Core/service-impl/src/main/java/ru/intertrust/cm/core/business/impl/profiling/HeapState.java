package ru.intertrust.cm.core.business.impl.profiling;

import static ru.intertrust.cm.core.business.impl.profiling.SizeUnit.Byte;
import static ru.intertrust.cm.core.business.impl.profiling.SizeUnit.Megabyte;

/**
 * @author Denis Mitavskiy
 *         Date: 02.02.2017
 *         Time: 21:10
 */
public class HeapState {
    public static final HeapState ZERO_STATE = new HeapState(0, 0, 0);
    public final long time;
    public final long total;
    public final long used;

    public HeapState() {
        this.time = System.currentTimeMillis();
        this.total = Runtime.getRuntime().totalMemory();
        this.used = this.total - Runtime.getRuntime().freeMemory();
    }

    public HeapState(long time, long total, long used) {
        this.time = time;
        this.total = total;
        this.used = used;
    }

    public HeapState getDelta(HeapState state) {
        if (state != null) {
            return new HeapState(this.time - state.time, this.total - state.total, this.used - state.used);
        } else {
            return ZERO_STATE;
        }
    }

    public String toString(SizeUnit sizeUnit) {
        return "\t" + sizeUnit.from(Byte, used) + "\t" + sizeUnit.from(Byte, total);
    }

    public String toStringWithDelta(HeapState compareWith, SizeUnit sizeUnit) {
        return this.getDelta(compareWith).toString(sizeUnit) + toString(Megabyte);
    }
}
