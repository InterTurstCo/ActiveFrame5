package ru.intertrust.cm.core.business.impl.profiling;

import ru.intertrust.cm.core.business.impl.LongRunningMethodAnalysisTask;

/**
 * @author Denis Mitavskiy
 *         Date: 02.02.2017
 *         Time: 21:10
 */
public class HeapState {
    public static final HeapState ZERO_STATE = new HeapState(0, 0, 0);
    public final long time;
    public final long totalHeapSize;
    public final long heapSize;

    public HeapState() {
        this.time = System.currentTimeMillis();
        this.totalHeapSize = Runtime.getRuntime().totalMemory();
        this.heapSize = this.totalHeapSize - Runtime.getRuntime().freeMemory();
    }

    public HeapState(long time, long totalHeapSize, long heapSize) {
        this.time = time;
        this.totalHeapSize = totalHeapSize;
        this.heapSize = heapSize;
    }

    public HeapState getDelta(HeapState state) {
        if (state != null) {
            return new HeapState(this.time - state.time, this.totalHeapSize - state.totalHeapSize, this.heapSize - state.heapSize);
        } else {
            return ZERO_STATE;
        }
    }

    public String toStringNoTime() {
        return "\t" + LongRunningMethodAnalysisTask.toMB(heapSize) + "\t" + LongRunningMethodAnalysisTask.toMB(totalHeapSize);
    }

    public String toStringWithDelta(HeapState compareWith) {
        return this.getDelta(compareWith).toStringNoTime() + toStringNoTime();
    }
}
