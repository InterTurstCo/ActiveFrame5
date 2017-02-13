package ru.intertrust.cm.core.business.impl.profiling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import ru.intertrust.cm.core.dao.api.component.ServerComponent;
import ru.intertrust.cm.core.dao.api.component.ServerComponentHandler;

import javax.annotation.PostConstruct;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static ru.intertrust.cm.core.business.impl.profiling.RAMUsageTracker.SuspectGroups.MAIN;
import static ru.intertrust.cm.core.business.impl.profiling.SizeUnit.Byte;
import static ru.intertrust.cm.core.business.impl.profiling.SizeUnit.Megabyte;

/**
 * @author Denis Mitavskiy
 *         Date: 02.02.2017
 *         Time: 21:01
 */
@ServerComponent(name="RAMUsageTracker")
public class RAMUsageTracker implements ServerComponentHandler {
    private Logger level1 = LoggerFactory.getLogger("AF5_RAMUsageTracker_Level_1");
    private Logger level2 = LoggerFactory.getLogger("AF5_RAMUsageTracker_Level_2");
    private Logger level3 = LoggerFactory.getLogger("AF5_RAMUsageTracker_Level_3");

    @Value("${long.running.method.analysis.system.paths:ru.intertrust}")
    private String[] basePaths;

    @Value("${long.running.method.analysis.black.list.paths:org.springframework.web.client.RestTemplate,org.apache.http.impl.client.AbstractHttpClient.execute}")
    private String[] blackListPaths;

    @Value("${suspicious.heap.delta.deviations:3}")
    private double suspiciousHeapDeltaDeviations;

    @Value("${suspicious.total.heap.delta.deviations:1}")
    private double suspiciousTotalHeapDeltaDeviations;

    @Value("${suspicious.total.heap.delta.bytes.per.minute:536870912}") // 500 MB per minute is suspicious
    private long suspiciousTotalHeapDeltaBytesPerMinute;

    private SizeUnit sizeUnit = Megabyte;

    private ExecutionSnapshot prevSnapshot;
    private HeapStatistics heapStat;
    private SuspectGroups suspects;
    private DecimalFormat numberFormat = new DecimalFormat("#.#");
    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private StringBuilder sb = new StringBuilder(10000);

    public void setSuspiciousTotalHeapDeltaBytesPerMinute(long suspiciousTotalHeapDeltaBytesPerMinute) {
        this.suspiciousTotalHeapDeltaBytesPerMinute = suspiciousTotalHeapDeltaBytesPerMinute;
    }

    public void setLoggers(Logger... loggers) {
        if (loggers.length > 0) {
            this.level1 = loggers[0];
        }
        if (loggers.length > 1) {
            this.level2 = loggers[1];
        }
        if (loggers.length > 2) {
            this.level3 = loggers[2];
        }
    }

    @PostConstruct
    public void init() {
        prevSnapshot = makeSnapshot();
        suspects = new SuspectGroups();
        heapStat = new HeapStatistics(suspiciousHeapDeltaDeviations, suspiciousTotalHeapDeltaDeviations);
    }

    public void printHead() {
        if (isEnabled(level1)) {
            level1.debug("\tdUsed" + "\tdUsed_avg" + "\tdUsed_warn" + "\tUsed" + "\tdTotal" + "\tdTotal_avg" + "\tdTotal_warn" + "\tTotal" + "\tThreads" + "\tSystem Threads" + "\tSuspects");
        }
    }

    public void track() {
        if (!logEnabled()) {
            return;
        }
        long t1 = System.currentTimeMillis();
        final ExecutionSnapshot snapshot = makeSnapshot();
        suspects.retainFrom(snapshot);
        final boolean suspiciousActivity = isSuspiciousActivity(snapshot);
        if (suspiciousActivity) {
            final ExecutionSnapshot intersection = snapshot.getIntersection(prevSnapshot);
            suspects.addAllFrom(MAIN, intersection);
        }
        printLog(snapshot, suspiciousActivity, System.currentTimeMillis() - t1);

        heapStat.add(snapshot.getHeapState().getDelta(prevSnapshot.getHeapState()));
        prevSnapshot = snapshot;
    }

    private void printLog(ExecutionSnapshot snapshot, boolean suspiciousActivity, long executionTime) {
        final String summary = getSummaryMessage(snapshot, executionTime);
        if (isEnabled(level1)) {
            level1.debug(summary);
        }
        if (!isAnythingToLog()) {
            return;
        }
        if (isEnabled(level2)) {
            level2.debug("----------------------------------------------------------------------------");
            level2.debug(summary);
            logTraces(level2, suspiciousActivity, true);
        }
        if (isEnabled(level3)) {
            level3.debug("----------------------------------------------------------------------------");
            level3.debug(summary);
            logTraces(level3, suspiciousActivity, false);
        }
    }

    private String getSummaryMessage(ExecutionSnapshot snapshot, long time) {
        final HeapState heapState = snapshot.getHeapState();
        final HeapState delta = heapState.getDelta(prevSnapshot.getHeapState());
        final String heapDesc =
                "\t" + format(delta.used) +
                        "\t" + getHeapStatDesc(heapStat.getUsed()) +
                        "\t" + format(heapState.used) +
                        "\t" + format(delta.total) +
                        "\t" + getHeapStatDesc(heapStat.getTotal()) +
                        "\t" + format(heapState.total) +
                        "\t" + snapshot.getThreadsCount() +
                        "\t" + snapshot.getSystemThreadsCount();

        return heapDesc + "\t" + shortSuspectsSummary() + "\t" + time;
    }

    private String shortSuspectsSummary() {
        final Suspects suspects = this.suspects.get(MAIN);
        if (suspects.isEmpty()) {
            return "---";
        }
        sb.setLength(0);
        sb.append(suspects.getStackTraces().size());
        for (StackTrace stackTrace : suspects.getStackTraces().values()) {
            sb.append(stackTrace.oneLineBasePackageDescription()).append(";");
        }
        return sb.toString();
    }

    public boolean isAnythingToLog() {
        return !suspects.get(MAIN).getStackTraces().isEmpty();
    }

    public void logTraces(Logger logger, boolean suspiciousActivity, boolean onlyBasePackages) {
        sb.setLength(0);
        final Map<ThreadId, Long> oldThreads = suspects.get(MAIN).getOldThreads();
        if (suspiciousActivity) {
            sb.append("SUSPICIOUS RAM ACTIVITY IS GOING ON\n");
        } else {
            sb.append("NO UNUSUAL RAM ACTIVITY AT THE MOMENT\n");
        }
        if (!oldThreads.isEmpty()) {
            sb.append("Previously logged and still running threads information:\n");
            for (Map.Entry<ThreadId, Long> entry : oldThreads.entrySet()) {
                sb.append("\t\t").append(entry.getKey()).append(": ").append(dateTimeFormat.format(new Date(entry.getValue()))).append("\n");
            }
        }
        logger.debug(sb.toString());

        sb.setLength(0);
        final Collection<StackTrace> newSuspectStackTraces = suspects.get(MAIN).getRecentThreads().values();
        if (!newSuspectStackTraces.isEmpty()) {
            logger.debug("New suspects information");
            for (StackTrace stackTrace : newSuspectStackTraces) {
                final ThreadId threadId = stackTrace.getThreadId();
                sb.append("\n").append(threadId).append("\n");
                final List<StackTraceElement> stackTraceElements = onlyBasePackages ? stackTrace.getBasePathsStackTrace() : stackTrace.getStackTrace();
                for (StackTraceElement elt : stackTraceElements) {
                    sb.append("\t").append(elt.toString()).append("\n");
                }
                sb.append("\n");
                logger.debug(sb.toString());
                sb.setLength(0);
            }
        }
    }

    public String getHeapStatDesc(Mean value) {
        return format(value.getMean())
                + "\t" + format(value.getWarnValue());
    }

    private String format(double value) {
        return Long.toString((long) toSizeUnit(value));
    }

    private double toSizeUnit(double value) {
        return sizeUnit.from(Byte, value);
    }

    private boolean isSuspiciousActivity(ExecutionSnapshot snapshot) {
        if (suspiciousTotalHeapDeltaBytesPerMinute <= 0) {
            return true;
        }

        final HeapState delta = snapshot.getHeapState().getDelta(prevSnapshot.getHeapState());
        long suspiciousHeapDeltaBytes = suspiciousTotalHeapDeltaBytesPerMinute / 60000 * delta.time;
        if (delta.total > suspiciousHeapDeltaBytes) {
            return true;
        }
        if (heapStat.getUsed().warn(delta.used)) {
            return true;
        }
        if (heapStat.getTotal().warn(delta.total)) {
            return true;
        }
        return false;
    }

    private ExecutionSnapshot makeSnapshot() {
        return new ExecutionSnapshot(basePaths, blackListPaths);
    }

    private boolean logEnabled() {
        return isEnabled(level1) || isEnabled(level2) || isEnabled(level3);
    }

    private static boolean isEnabled(Logger logger) {
        return logger != null && logger.isDebugEnabled();
    }

    static class SuspectGroups {
        public static int MAIN = 0;

        private Suspects[] suspects;

        public SuspectGroups() {
            suspects = new Suspects[3];
            for (int i = 0; i < 3; ++i) {
                suspects[i] = new Suspects();
            }
        }

        public void addAllFrom(int group, ExecutionSnapshot snapshot) {
            suspects[group].addAllFrom(snapshot);
        }

        public boolean isEmpty(int group) {
            return suspects[group].isEmpty();
        }

        public void retainFrom(ExecutionSnapshot snapshot) {
            for (Suspects suspect : suspects) {
                suspect.retainFrom(snapshot);
            }
        }

        public Suspects get(int group) {
            return suspects[group];
        }
    }

    private static class Suspects {
        private TreeMap<ThreadId, StackTrace> stackTraces = new TreeMap<>();
        private HashMap<ThreadId, Long> times = new HashMap<>();
        private Long recentSnapshotTime;

        public void addAllFrom(ExecutionSnapshot snapshot) {
            recentSnapshotTime = snapshot.getHeapState().time;
            final Map<ThreadId, StackTrace> stackTracesByThread = snapshot.getStackTracesByThread();
            stackTraces.putAll(stackTracesByThread);
            for (StackTrace stackTrace : stackTracesByThread.values()) {
                if (!times.containsKey(stackTrace.getThreadId())) {
                    times.put(stackTrace.getThreadId(), recentSnapshotTime);
                }
            }
        }

        public void retainFrom(ExecutionSnapshot snapshot) {
            recentSnapshotTime = snapshot.getHeapState().time;
            final Map<ThreadId, StackTrace> executionsToStay = snapshot.getStackTracesByThread();
            final Set<ThreadId> threadsToStay = executionsToStay.keySet();
            stackTraces.keySet().retainAll(threadsToStay);
            times.keySet().retainAll(threadsToStay);

            // there can stay threads with same ID, but with different exeuction stack - drop them
            ArrayList<ThreadId> notSameStack = new ArrayList<>(stackTraces.size());
            for (Map.Entry<ThreadId, StackTrace> entry : stackTraces.entrySet()) {
                if (!executionsToStay.get(entry.getKey()).approximatelyEquals(entry.getValue())) {
                    notSameStack.add(entry.getKey());
                }
            }
            stackTraces.keySet().removeAll(notSameStack);
            times.keySet().removeAll(notSameStack);
        }

        public boolean isEmpty() {
            return stackTraces.isEmpty();
        }

        public TreeMap<ThreadId, StackTrace> getStackTraces() {
            return stackTraces;
        }

        public StackTrace getStackTrace(ThreadId threadId) {
            return stackTraces.get(threadId);
        }

        public TreeMap<ThreadId, StackTrace> getRecentThreads() {
            TreeMap<ThreadId, StackTrace> recentThreads = new TreeMap<>();
            for (Map.Entry<ThreadId, Long> entry : times.entrySet()) {
                if (entry.getValue().equals(recentSnapshotTime)) {
                    final ThreadId threadId = entry.getKey();
                    recentThreads.put(threadId, getStackTrace(threadId));
                }
            }
            return recentThreads;
        }

        public Map<ThreadId, Long> getOldThreads() {
            Map<ThreadId, Long> oldThreads = new TreeMap<>();
            for (Map.Entry<ThreadId, Long> entry : times.entrySet()) {
                if (!entry.getValue().equals(recentSnapshotTime)) {
                    oldThreads.put(entry.getKey(), entry.getValue());
                }
            }
            return oldThreads;
        }
    }
}
