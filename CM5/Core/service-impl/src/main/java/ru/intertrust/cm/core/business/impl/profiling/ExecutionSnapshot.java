package ru.intertrust.cm.core.business.impl.profiling;

import ru.intertrust.cm.core.business.api.dto.Pair;
import ru.intertrust.cm.core.business.impl.LongRunningMethodAnalysisTask;

import java.util.*;

/**
 * @author Denis Mitavskiy
 *         Date: 03.02.2017
 *         Time: 18:47
 */
public class ExecutionSnapshot {
    private Map<ThreadInfo, StackTrace> stackTracesByThread;
    private Map<ThreadInfo, StackTrace> initialStackTracesByThread;
    private Map<ThreadInfo, StackTrace> fullThreadDump;

    private String[] basePaths;
    private String[] blackListPaths;
    private HeapState heapState;
    private int totalThreadsCount;
    private long cpuSpeed;

    private ExecutionSnapshot() {
    }

    public ExecutionSnapshot(String[] basePaths, String[] blackListPaths) {
        this.basePaths = basePaths;
        this.blackListPaths = blackListPaths;
        this.stackTracesByThread = new TreeMap<>();
        this.heapState = new HeapState();
        final Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
        this.fullThreadDump = new TreeMap<>();
        for (Map.Entry<Thread, StackTraceElement[]> threadStackTrace : allStackTraces.entrySet()) {
            final Thread thread = threadStackTrace.getKey();
            final String name = thread.getName();
            final StackTrace stackTrace = StackTrace.get(thread, threadStackTrace.getValue(), basePaths, blackListPaths);
            ++totalThreadsCount;
            if (LongRunningMethodAnalysisTask.AF5_DB_CHECK_DAEMON.equals(name) || LongRunningMethodAnalysisTask.AF5_MONITORING_DAEMON.equals(name) || Thread.currentThread().equals(thread)) {
                continue;
            }
            fullThreadDump.put(stackTrace.getThreadInfo(), stackTrace);
            if (!stackTrace.isInBlackList() && stackTrace.belongsToBasePath()) {
                stackTracesByThread.put(stackTrace.getThreadInfo(), stackTrace);
            }
        }
        this.stackTracesByThread = Collections.unmodifiableMap(stackTracesByThread);
        this.fullThreadDump = Collections.unmodifiableMap(fullThreadDump);
        this.initialStackTracesByThread = stackTracesByThread;
        this.cpuSpeed = getCurrentCpuSpeed();
    }

    public Map<ThreadInfo, StackTrace> getFullThreadDump() {
        return fullThreadDump;
    }

    public Map<ThreadInfo, StackTrace> getStackTracesByThread() {
        return stackTracesByThread;
    }

    public StackTrace getStackTrace(ThreadInfo threadInfo) {
        return stackTracesByThread.get(threadInfo);
    }

    public StackTrace getInitialStackTrace(ThreadInfo threadInfo) {
        return stackTracesByThread.get(threadInfo);
    }

    public int getThreadsCount() {
        return totalThreadsCount;
    }

    public int getSystemThreadsCount() {
        return stackTracesByThread.size();
    }

    public long getCpuSpeed() {
        return cpuSpeed;
    }

    public HeapState getHeapState() {
        return heapState;
    }

    public ExecutionSnapshot getIntersection(ExecutionSnapshot another) {
        HashMap<ThreadInfo, StackTrace> stackTraces = new HashMap<>();
        HashMap<ThreadInfo, StackTrace> initialStackTraces = new HashMap<>();
        for (Map.Entry<ThreadInfo, StackTrace> entry : stackTracesByThread.entrySet()) {
            final StackTrace thisStackTrace = entry.getValue();
            final StackTrace anotherStackTrace = another.getStackTrace(entry.getKey());
            if (anotherStackTrace != null && thisStackTrace.approximatelyEquals(anotherStackTrace)) {
                StackTrace initial;
                StackTrace current;
                if (this.heapState.time > another.heapState.time) {
                    initial = anotherStackTrace;
                    current = thisStackTrace;
                } else {
                    initial = thisStackTrace;
                    current = anotherStackTrace;
                }
                stackTraces.put(current.getThreadInfo(), current);
                initialStackTraces.put(initial.getThreadInfo(), initial);
            }
        }
        ExecutionSnapshot intersection = new ExecutionSnapshot();
        intersection.basePaths = this.basePaths;
        intersection.heapState = this.heapState.time > another.heapState.time ? this.heapState : another.heapState;
        intersection.stackTracesByThread = Collections.unmodifiableMap(stackTraces);
        intersection.initialStackTracesByThread = Collections.unmodifiableMap(initialStackTraces);
        return intersection;
    }

    public boolean isEmpty() {
        return this.stackTracesByThread.isEmpty();
    }

    public static final int DURATION = 10;
    public static final int DURATION_UNITS_IN_SECOND = 1000;
    public static final int MEGA = 1000000;
    public static long incL = 1; // нарочно static - для подавления оптимизаций
    private static long getCurrentCpuSpeed() {
        long opsCount = 0;
        final long t1 = System.currentTimeMillis();
        final long t2 = t1 + DURATION;
        long curTime = 0;
        do {
            for (int i = 0; i < 10000; ++i) {
                opsCount += incL;
            }
        } while ((curTime = System.currentTimeMillis()) < t2);
        return opsCount * DURATION_UNITS_IN_SECOND / (MEGA * (curTime - t1)); // to megaherz
    }

    private Pair<ThreadInfo, ArrayList<StackTraceElement>> parseStackTrace(Thread thread, StackTraceElement[] stackTrace) {
        ArrayList<StackTraceElement> basePackageStackTrace = null;
        for (StackTraceElement stackTraceElement : stackTrace) {
            if (containsLoggedPackages(stackTraceElement)) {
                if (basePackageStackTrace == null) {
                    basePackageStackTrace = new ArrayList<>();
                }
                basePackageStackTrace.add(stackTraceElement);
            }
        }
        return basePackageStackTrace == null ? null : new Pair<>(new ThreadInfo(thread), basePackageStackTrace);
    }

    private boolean containsLoggedPackages(StackTraceElement stackTraceElement) {
        for (String basePackage : basePaths) {
            if (stackTraceElement.getClassName().contains(basePackage)) {
                return true;
            }
        }
        return false;
    }
}
