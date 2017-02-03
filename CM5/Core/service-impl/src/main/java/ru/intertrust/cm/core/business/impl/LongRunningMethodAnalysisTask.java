package ru.intertrust.cm.core.business.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.intertrust.cm.core.business.api.dto.Pair;
import ru.intertrust.cm.core.business.api.dto.Trio;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTask;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskHandle;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskParameters;
import ru.intertrust.cm.core.business.impl.profiling.HeapState;

import javax.ejb.EJBContext;
import javax.ejb.SessionContext;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Периодическое задание, анализирующее работающие потоки и методы и информирующее о тех, которые работают долго
 */
@ScheduleTask(name = "LongRunningMethodAnalysisTask", minute = "*/1", allNodes = true)
public class LongRunningMethodAnalysisTask implements ScheduleTaskHandle {
    private static final Logger logger = LoggerFactory.getLogger(LongRunningMethodAnalysisTask.class);

    // all the code is executed in a single thread, thus static variables are safe
    private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final ConcurrentHashMap<ThreadId, Pair<StackTraceElement[], HeapHistory>> threadsInformation = new ConcurrentHashMap<>(1024);
    private final StringBuilder sb = new StringBuilder(10000); // big enough log builder
    private Set<ThreadId> aliveThreadIds = new HashSet<>();
    private HeapState prevHeapState = null;

    @org.springframework.beans.factory.annotation.Value("${long.running.method.analysis.system.packages:ru.intertrust}")
    private String[] basePackages;

    @org.springframework.beans.factory.annotation.Value("${long.running.method.analysis.log.new.threads:false}")
    private boolean logAllNewThreads;

    @Override
    public String execute(EJBContext ejbContext, SessionContext sessionContext, ScheduleTaskParameters parameters) throws InterruptedException {
        logger.info("---------------------- Long running methods info ----------------------");
        final Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
        ArrayList<Trio<ThreadId, Thread.State, StackTraceElement[]>> toLog = new ArrayList<>();
        ArrayList<ThreadId> allThreadIds = new ArrayList<>(allStackTraces.size());
        HashSet<ThreadId> currentlyAliveThreadIds = new HashSet<>(allStackTraces.size() * 2);
        logger.info("Threads total: " + allStackTraces.size());
        for (Map.Entry<Thread, StackTraceElement[]> entry : allStackTraces.entrySet()) {
            final Thread thread = entry.getKey();
            final Thread.State state = thread.getState();
            final ThreadId threadId = new ThreadId(thread);
            if (thread.isAlive()) {
                currentlyAliveThreadIds.add(threadId);
            }
            if (state != Thread.State.BLOCKED && state != Thread.State.RUNNABLE || threadId.id == Thread.currentThread().getId()) {
                threadsInformation.remove(threadId);
                continue;
            }
            allThreadIds.add(threadId);
            Pair<StackTraceElement[], HeapHistory> history = threadsInformation.get(threadId);
            final StackTraceElement[] currentStackTrace = entry.getValue();
            if (history == null) {
                addNewThreadMethodExecutionInformation(threadId, currentStackTrace);
                continue;
            }
            if (approximatelySameCodeExecuted(history.getFirst(), currentStackTrace)) {
                history.getSecond().add(new HeapState());
                toLog.add(new Trio<>(threadId, state, currentStackTrace));
            } else {
                addNewThreadMethodExecutionInformation(threadId, currentStackTrace);
            }
        }
        threadsInformation.keySet().retainAll(allThreadIds);

        HeapState heapState = new HeapState();
        logger.info("Alive threads running: " + currentlyAliveThreadIds.size());
        logger.info("System threads running: " + threadsInformation.size());
        logger.info("Long system threads running: " + toLog.size());
        logger.info("Current heap: " + heapState.toStringWithDelta(this.prevHeapState));
        this.prevHeapState = heapState;

        logLongRunningMethods(toLog);
        logNewTreadsSinceLastCheck(currentlyAliveThreadIds);
        // just in case of this class leak:
        final int systemThreadsRunning = threadsInformation.size();
        if (systemThreadsRunning > 5000) {
            logger.warn("Cleaning threads information due to huge size: " + threadsInformation.size());
            threadsInformation.clear();
        }
        return "DONE";
    }

    private void addNewThreadMethodExecutionInformation(ThreadId threadId, StackTraceElement[] currentStackTrace) {
        threadsInformation.remove(threadId); // this may be a different method inside the thread, so clean the previous entry
        if (currentStackTrace.length == 0) {
            return;
        }
        boolean logThread = false;
        for (StackTraceElement stackTraceElement : currentStackTrace) {
            if (containsLoggedPackages(stackTraceElement)) {
                logThread = true;
                break;
            }
        }
        if (logThread) {
            threadsInformation.put(threadId, new Pair<>(currentStackTrace, new HeapHistory()));
        }
    }

    private boolean containsLoggedPackages(StackTraceElement stackTraceElement) {
        for (String basePackage : basePackages) {
            if (stackTraceElement.toString().contains(basePackage)) {
                return true;
            }
        }
        return false;
    }

    private boolean approximatelySameCodeExecuted(StackTraceElement[] initialStackTrace, StackTraceElement[] currentStackTrace) {
        ArrayList<StackTraceElement> initialBasePackagesElements = getBasePackagesStackTraceElements(initialStackTrace);
        ArrayList<StackTraceElement> currentBasePackagesElements = getBasePackagesStackTraceElements(currentStackTrace);
        if (currentBasePackagesElements.isEmpty()) {
            return false;
        }
        ArrayList<StackTraceElement> arrayToScan = initialBasePackagesElements.size() < currentBasePackagesElements.size() ? initialBasePackagesElements : currentBasePackagesElements;
        int nonMatchingIndex = -1;
        for (int i = 0; i < arrayToScan.size(); i++) {
            if (!initialBasePackagesElements.get(i).equals(currentBasePackagesElements.get(i))) {
                nonMatchingIndex = i;
                break;
            }
        }
        if (nonMatchingIndex == -1) {
            return true;
        }
        final int minEntriesInStackTrace = Math.min(initialBasePackagesElements.size(), currentBasePackagesElements.size());
        int nonMatchingEntriesQty = minEntriesInStackTrace - nonMatchingIndex;
        return nonMatchingEntriesQty < minEntriesInStackTrace / 2 && nonMatchingEntriesQty < 5 || nonMatchingIndex > 7;
    }

    private ArrayList<StackTraceElement> getBasePackagesStackTraceElements(StackTraceElement[] elements) {
        ArrayList<StackTraceElement> basePackagesElements = new ArrayList<>(elements.length / 2);
        for (StackTraceElement elt : elements) {
            if (containsLoggedPackages(elt)) {
                basePackagesElements.add(elt);
            }
        }
        Collections.reverse(basePackagesElements);
        return basePackagesElements;
    }

    private void logLongRunningMethods(ArrayList<Trio<ThreadId, Thread.State, StackTraceElement[]>> toLog) {
        Collections.sort(toLog, new Comparator<Trio<ThreadId, Thread.State, StackTraceElement[]>>() {
            @Override
            public int compare(Trio<ThreadId, Thread.State, StackTraceElement[]> o1, Trio<ThreadId, Thread.State, StackTraceElement[]> o2) {
                return o1.getFirst().compareTo(o2.getFirst());
            }
        }); // sort by ID for pretty printing

        sb.setLength(0);
        for (Trio<ThreadId, Thread.State, StackTraceElement[]> threadInfo : toLog) {
            sb.append("\nThread: ").append(threadInfo.getFirst().id).append(" ").append(threadInfo.getSecond()).append(" (").append(threadInfo.getFirst().name).append(")").append("\n");
            sb.append("Runtime info:\n");
            HeapHistory heapHistory = threadsInformation.get(threadInfo.getFirst()).getSecond();
            sb.append(heapHistory).append("\n");
            for (StackTraceElement elt : threadInfo.getThird()) {
                sb.append("\t").append(elt.toString()).append("\n");
            }
            sb.append("\n");
            logger.warn(sb.toString());
            sb.setLength(0);
        }
    }

    private void logNewTreadsSinceLastCheck(Set<ThreadId> currentlyAliveThreadIds) {
        if (!logAllNewThreads) {
            return;
        }
        if (aliveThreadIds.isEmpty()) { // first run
            aliveThreadIds = currentlyAliveThreadIds;
            return;
        }
        final HashSet<ThreadId> threadsDeltaSet = new HashSet<>(currentlyAliveThreadIds);
        threadsDeltaSet.removeAll(aliveThreadIds);
        if (threadsDeltaSet.isEmpty()) {
            return;
        }
        aliveThreadIds = currentlyAliveThreadIds;
        final ArrayList<ThreadId> threadsDelta = new ArrayList<>(threadsDeltaSet);
        Collections.sort(threadsDelta);
        StringBuilder sb = new StringBuilder("New threads appeared since last log: " + threadsDelta.size() + "\n");
        for (ThreadId threadId : threadsDelta) {
            sb.append(threadId).append("\n");
        }
        sb.append("\n");
        logger.warn(sb.toString());
    }

    public static String toMB(long l) {
        return Long.toString(l / 1024 / 1024);
    }

    private static class ThreadId implements Comparable<ThreadId> {
        public final long id;
        public final String name;

        public ThreadId(Thread thread) {
            this(thread.getId(), thread.getName());
        }

        public ThreadId(long id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ThreadId that = (ThreadId) o;

            if (id != that.id) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return (int) (id ^ (id >>> 32));
        }

        @Override
        public int compareTo(ThreadId o) {
            return Long.compare(this.id, o.id);
        }

        @Override
        public String toString() {
            return id + " (" + name + ")";
        }
    }

    private static class HeapHistory extends ArrayList<HeapState> {
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(200);
            sb.append("Time\tUsed Delta\tTotal Delta\tUsed Heap\tTotal Heap (Everything in MB)\n");
            for (HeapState entry : this) {
                sb.append(dateTimeFormat.format(new Date(entry.time)));
                final HeapState firstEntry = get(0);
                sb.append(entry.toStringWithDelta(firstEntry)).append("\n");
            }
            return sb.toString();
        }


    }

}
