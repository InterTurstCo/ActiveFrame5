package ru.intertrust.cm.core.business.impl.profiling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import ru.intertrust.cm.core.business.impl.LongRunningMethodAnalysisTask;
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
    public static final String LONG_LINE = "----------------------------------------------------------------------------";
    private Logger level1 = LoggerFactory.getLogger("AF5_RAMUsageTracker_Level_1");
    private Logger level2 = LoggerFactory.getLogger("AF5_RAMUsageTracker_Level_2");
    private Logger level3 = LoggerFactory.getLogger("AF5_RAMUsageTracker_Level_3");
    private Logger level4 = LoggerFactory.getLogger("AF5_RAMUsageTracker_Level_4");
    private Logger level5 = LoggerFactory.getLogger("AF5_RAMUsageTracker_Level_5");

    @Value("${long.running.method.analysis.system.paths:ru.intertrust}")
    private String[] basePaths;

    @Value("${long.running.method.analysis.black.list.paths:}")
    private String[] blackListPaths;

    @Value("${suspicious.heap.delta.deviations:3}")
    private double suspiciousHeapDeltaDeviations;

    @Value("${suspicious.total.heap.delta.deviations:1}")
    private double suspiciousTotalHeapDeltaDeviations;

    @Value("${suspicious.total.heap.delta.bytes.per.minute:536870912}") // 500 MB per minute is suspicious
    private long suspiciousTotalHeapDeltaBytesPerMinute;

    @Value("${suspicious.system.threads:100}")
    private int suspiciousSystemThreads;

    @Value("${suspicious.connection.retrieval.time.millies:100}") // 100 ms
    private long suspiciousConnectionRetrievalTime;

    @Value("${suspicious.query.time:100}") // 100 ms
    private long suspiciousQueryTime;

    private SizeUnit sizeUnit = Megabyte;

    private ExecutionSnapshot prevSnapshot;
    private HeapStatistics heapStat;
    private SuspectGroups suspects;
    private DecimalFormat numberFormat = new DecimalFormat("#.#");
    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private StringBuilder sb = new StringBuilder(10000);
    private LongRunningMethodAnalysisTask.DBCheck dbCheck;

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
        if (loggers.length > 3) {
            this.level4 = loggers[3];
        }
        if (loggers.length > 4) {
            this.level5 = loggers[4];
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
            level1.debug("\tdUsed" + "\tdUsed_avg" + "\tdUsed_warn" + "\tUsed" + "\tdTotal" + "\tdTotal_avg" + "\tdTotal_warn" + "\tTotal" + "\tCPU Speed"+ "\tGet Conn From Pool Time"+ "\tQuery Time" + "\tThreads" + "\tSystem Threads" + "\tSnapshot Time" + "\tSuspects");
        }
    }

    public void track(LongRunningMethodAnalysisTask.DBCheck dbCheck) {
        if (!logEnabled()) {
            return;
        }
        this.dbCheck = dbCheck;
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
        if (dbCheck != null && shouldPrintFullDump(snapshot)) { // dbCheck is null wh
            if (isEnabled(level4)) { // print only threads with base packages, and with base packages stack entries
                printStackTraces(level4, true, snapshot.getStackTracesByThread().values(), LONG_LINE);
            }
            if (isEnabled(level5)) { // full dump: todo: NOT WORKING. instead of 233 threads, just 2?
                printStackTraces(level5, false, snapshot.getFullThreadDump().values(), LONG_LINE);
            }
        }
        if (!isAnythingToLog()) {
            return;
        }
        if (isEnabled(level2)) {
            level2.debug(LONG_LINE);
            level2.debug(summary);
            logTraces(level2, suspiciousActivity, true);
        }
        if (isEnabled(level3)) {
            level3.debug(LONG_LINE);
            level3.debug(summary);
            logTraces(level3, suspiciousActivity, false);
        }
    }

    private String getSummaryMessage(ExecutionSnapshot snapshot, long time) {
        final HeapState heapState = snapshot.getHeapState();
        final HeapState delta = heapState.getDelta(prevSnapshot.getHeapState());
        final String connTime = dbCheck == null ? "" : dbCheck.getGetConnectionTime() == null ? "?" : dbCheck.getGetConnectionTime().toString();
        final String queryTime = dbCheck == null ? "" : dbCheck.getQueryTime() == null ? "?" : dbCheck.getQueryTime().toString();
        final String heapDesc =
                "\t" + format(delta.used) +
                        "\t" + getHeapStatDesc(heapStat.getUsed()) +
                        "\t" + format(heapState.used) +
                        "\t" + format(delta.total) +
                        "\t" + getHeapStatDesc(heapStat.getTotal()) +
                        "\t" + format(heapState.total) +
                        "\t" + snapshot.getCpuSpeed() +
                        "\t" + connTime +
                        "\t" + queryTime +
                        "\t" + snapshot.getThreadsCount() +
                        "\t" + snapshot.getSystemThreadsCount();

        return heapDesc + "\t" + time + "\t" + shortSuspectsSummary();
    }

    private String shortSuspectsSummary() {
        final Suspects suspects = this.suspects.get(MAIN);
        if (suspects.isEmpty()) {
            return "0\t";
        }
        sb.setLength(0);
        sb.append(suspects.getStackTraces().size()).append("\t");
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
        final Map<ThreadInfo, Long> oldThreads = suspects.get(MAIN).getOldThreads();
        final TreeMap<ThreadInfo, StackTrace> stackTraces = suspects.get(MAIN).getStackTraces();
        if (suspiciousActivity) {
            sb.append("SUSPICIOUS RAM ACTIVITY IS GOING ON\n");
        } else {
            sb.append("NO UNUSUAL RAM ACTIVITY AT THE MOMENT\n");
        }
        if (!oldThreads.isEmpty()) {
            sb.append("Previously logged and still running threads information:\n");
            for (Map.Entry<ThreadInfo, Long> entry : oldThreads.entrySet()) {
                final ThreadInfo threadInfo = entry.getKey();
                final StackTrace stackTrace = stackTraces.get(threadInfo);
                final String dateStr = dateTimeFormat.format(new Date(entry.getValue()));
                final String baseStackFirstLine = stackTrace.getBasePathsStackTrace().get(0).toString();
                final String stackFirstLine = stackTrace.getStackTrace().get(0).toString();
                sb.append("\t\t").append(threadInfo).append(": ").append(dateStr).append("\t-->\t").append(baseStackFirstLine).append("\t").append(stackFirstLine).append("\n");
            }
        }
        logger.debug(sb.toString());

        final Collection<StackTrace> newSuspectStackTraces = suspects.get(MAIN).getRecentThreads().values();
        if (!newSuspectStackTraces.isEmpty()) {
            printStackTraces(logger, onlyBasePackages, newSuspectStackTraces, "------ New suspects information -----");
        }
    }

    private void printStackTraces(Logger logger, boolean onlyBasePackages, Collection<StackTrace> stackTraces, String prefix) {
        sb.setLength(0);
        sb.append(prefix);
        for (StackTrace stackTrace : stackTraces) {
            final List<StackTraceElement> stackTraceElements = onlyBasePackages ? stackTrace.getBasePathsStackTrace() : stackTrace.getStackTrace();
            if (stackTraceElements.isEmpty()) {
                continue;
            }
            final ThreadInfo threadInfo = stackTrace.getThreadInfo();
            sb.append("\n").append(threadInfo).append("\n");
            for (StackTraceElement elt : stackTraceElements) {
                sb.append("\t").append(elt.toString()).append("\n");
            }
            sb.append("\n");
        }
        logger.debug(sb.toString());
        sb.setLength(0);
        if (sb.capacity() > 20000000) {
            if (level2.isWarnEnabled()) {
                level2.warn("Builder size is too large: " + sb.capacity() + ". Clearing it");
            }
            sb = new StringBuilder();
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

    private boolean shouldPrintFullDump(ExecutionSnapshot executionSnapshot) {
        if (dbCheck != null) {
            if (suspiciousConnectionRetrievalTime >= 0 && (dbCheck.getGetConnectionTime() == null || dbCheck.getGetConnectionTime() > suspiciousConnectionRetrievalTime)) {
                return true;
            }
            if (suspiciousQueryTime >= 0 && (dbCheck.getQueryTime() == null || dbCheck.getQueryTime() > suspiciousQueryTime)) {
                return true;
            }
        }
        if (suspiciousSystemThreads >= 0 && executionSnapshot.getSystemThreadsCount() > suspiciousSystemThreads) {
            return true;
        }
        return false;
    }

    private ExecutionSnapshot makeSnapshot() {
        return new ExecutionSnapshot(basePaths, blackListPaths);
    }

    private boolean logEnabled() {
        return isEnabled(level1) || isEnabled(level2) || isEnabled(level3) || isEnabled(level4) || isEnabled(level5);
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
        private TreeMap<ThreadInfo, StackTrace> stackTraces = new TreeMap<>();
        private HashMap<ThreadInfo, Long> times = new HashMap<>();
        private Long recentSnapshotTime;

        public void addAllFrom(ExecutionSnapshot snapshot) {
            recentSnapshotTime = snapshot.getHeapState().time;
            final Map<ThreadInfo, StackTrace> stackTracesByThread = snapshot.getStackTracesByThread();
            stackTraces.putAll(stackTracesByThread);
            for (StackTrace stackTrace : stackTracesByThread.values()) {
                if (!times.containsKey(stackTrace.getThreadInfo())) {
                    times.put(stackTrace.getThreadInfo(), recentSnapshotTime);
                }
            }
        }

        public void retainFrom(ExecutionSnapshot snapshot) {
            recentSnapshotTime = snapshot.getHeapState().time;
            final Map<ThreadInfo, StackTrace> executionsToStay = snapshot.getStackTracesByThread();
            final Set<ThreadInfo> threadsToStay = executionsToStay.keySet();
            stackTraces.keySet().retainAll(threadsToStay);
            times.keySet().retainAll(threadsToStay);

            // there can stay threads with same ID, but with different exeuction stack - drop them
            ArrayList<ThreadInfo> notSameStack = new ArrayList<>(stackTraces.size());
            for (Map.Entry<ThreadInfo, StackTrace> entry : stackTraces.entrySet()) {
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

        public TreeMap<ThreadInfo, StackTrace> getStackTraces() {
            return stackTraces;
        }

        public StackTrace getStackTrace(ThreadInfo threadInfo) {
            return stackTraces.get(threadInfo);
        }

        public TreeMap<ThreadInfo, StackTrace> getRecentThreads() {
            TreeMap<ThreadInfo, StackTrace> recentThreads = new TreeMap<>();
            for (Map.Entry<ThreadInfo, Long> entry : times.entrySet()) {
                if (entry.getValue().equals(recentSnapshotTime)) {
                    final ThreadInfo threadInfo = entry.getKey();
                    recentThreads.put(threadInfo, getStackTrace(threadInfo));
                }
            }
            return recentThreads;
        }

        public Map<ThreadInfo, Long> getOldThreads() {
            Map<ThreadInfo, Long> oldThreads = new TreeMap<>();
            for (Map.Entry<ThreadInfo, Long> entry : times.entrySet()) {
                if (!entry.getValue().equals(recentSnapshotTime)) {
                    oldThreads.put(entry.getKey(), entry.getValue());
                }
            }
            return oldThreads;
        }
    }
}
