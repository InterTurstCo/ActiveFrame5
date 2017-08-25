package ru.intertrust.cm.core.business.impl.profiling;

import ru.intertrust.cm.core.business.api.dto.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Denis Mitavskiy
 *         Date: 03.02.2017
 *         Time: 18:47
 */
public class ExecutionSnapshot {
    private Map<ThreadId, StackTrace> stackTracesByThread;
    private Map<ThreadId, StackTrace> initialStackTracesByThread;

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
        this.stackTracesByThread = new HashMap<>();
        this.heapState = new HeapState();
        final Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
        for (Map.Entry<Thread, StackTraceElement[]> threadStackTrace : allStackTraces.entrySet()) {
            final Thread thread = threadStackTrace.getKey();
            final Thread.State state = thread.getState();
            if (Thread.currentThread().equals(thread)) {
                continue;
            }
            ++totalThreadsCount;
            final StackTrace stackTrace = StackTrace.get(thread, threadStackTrace.getValue(), basePaths, blackListPaths);
            if (!stackTrace.ignore()) {
                stackTracesByThread.put(stackTrace.getThreadId(), stackTrace);
            }
        }
        this.stackTracesByThread = Collections.unmodifiableMap(stackTracesByThread);
        this.initialStackTracesByThread = stackTracesByThread;
        this.cpuSpeed = getCurrentCpuSpeed();
    }

    public Map<ThreadId, StackTrace> getStackTracesByThread() {
        return stackTracesByThread;
    }

    public StackTrace getStackTrace(ThreadId threadId) {
        return stackTracesByThread.get(threadId);
    }

    public StackTrace getInitialStackTrace(ThreadId threadId) {
        return stackTracesByThread.get(threadId);
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
        HashMap<ThreadId, StackTrace> stackTraces = new HashMap<>();
        HashMap<ThreadId, StackTrace> initialStackTraces = new HashMap<>();
        for (Map.Entry<ThreadId, StackTrace> entry : stackTracesByThread.entrySet()) {
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
                stackTraces.put(current.getThreadId(), current);
                initialStackTraces.put(initial.getThreadId(), initial);
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
    public static final int MICROSECONDS_IN_DURATION_UNIT = 1000;
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
        return opsCount / (MICROSECONDS_IN_DURATION_UNIT * (curTime - t1));
    }

    private Pair<ThreadId, ArrayList<StackTraceElement>> parseStackTrace(Thread thread, StackTraceElement[] stackTrace) {
        ArrayList<StackTraceElement> basePackageStackTrace = null;
        for (StackTraceElement stackTraceElement : stackTrace) {
            if (containsLoggedPackages(stackTraceElement)) {
                if (basePackageStackTrace == null) {
                    basePackageStackTrace = new ArrayList<>();
                }
                basePackageStackTrace.add(stackTraceElement);
            }
        }
        return basePackageStackTrace == null ? null : new Pair<>(new ThreadId(thread), basePackageStackTrace);
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
