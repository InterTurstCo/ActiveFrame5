package ru.intertrust.cm.core.business.impl.profiling;

/**
 * @author Denis Mitavskiy
 *         Date: 03.02.2017
 *         Time: 21:33
 */
public class HeapStatistics {
    private Mean used;
    private Mean total;

    public HeapStatistics(double usedWarnSigmas, double totalWarnSigmas) {
        this.used = new Mean(usedWarnSigmas);
        this.total = new Mean(totalWarnSigmas);
    }

    public void add(HeapState heapState) {
        if (heapState.used >= 0) {
            used.add(heapState.used);
        }
        if (heapState.total >= 0) {
            total.add(heapState.total);
        }
    }

    public Mean getUsed() {
        return used;
    }

    public Mean getTotal() {
        return total;
    }

}
