package ru.intertrust.cm.core.business.impl.profiling;

import java.util.*;

/**
 * @author Denis Mitavskiy
 *         Date: 03.02.2017
 *         Time: 19:40
 */
public class StackTrace {
    public static final StackTrace IGNORED = new StackTrace();
    private ThreadId threadId;
    private List<StackTraceElement> stackTrace;
    private List<StackTraceElement> basePackagesStackTrace;
    private String[] basePackages;

    private StackTrace() {
    }

    public StackTrace(ThreadId threadId, List<StackTraceElement> stackTrace, List<StackTraceElement> basePackagesStackTrace, String[] basePackages) {
        this.threadId = threadId;
        this.stackTrace = stackTrace;
        this.basePackagesStackTrace = basePackagesStackTrace;
        this.basePackages = basePackages;
    }

    public static StackTrace get(Thread thread, StackTraceElement[] stackTrace, String[] basePackages) {
        ArrayList<StackTraceElement> basePackageStackTrace = null;
        for (StackTraceElement stackTraceElement : stackTrace) {
            if (belongsToBasePackage(stackTraceElement, basePackages)) {
                if (basePackageStackTrace == null) {
                    basePackageStackTrace = new ArrayList<>();
                }
                basePackageStackTrace.add(stackTraceElement);
            }
        }
        return basePackageStackTrace == null ? IGNORED : new StackTrace(new ThreadId(thread), Arrays.asList(stackTrace), basePackageStackTrace, basePackages);
    }

    public boolean ignore() {
        return this == IGNORED;
    }

    public ThreadId getThreadId() {
        return threadId;
    }

    public List<StackTraceElement> getStackTrace() {
        return stackTrace;
    }

    public List<StackTraceElement> getBasePackagesStackTrace() {
        return basePackagesStackTrace;
    }

    public boolean approximatelyEquals(StackTrace another) {
        ArrayList<StackTraceElement> thisStackTrace = reverse(basePackagesStackTrace);
        ArrayList<StackTraceElement> anotherStackTrace = reverse(another.basePackagesStackTrace);
        if (anotherStackTrace.isEmpty()) {
            return false;
        }
        ArrayList<StackTraceElement> arrayToScan = thisStackTrace.size() < anotherStackTrace.size() ? thisStackTrace : anotherStackTrace;
        int nonMatchingIndex = -1;
        for (int i = 0; i < arrayToScan.size(); i++) {
            final StackTraceElement thisElt = thisStackTrace.get(i);
            final StackTraceElement anotherElt = anotherStackTrace.get(i);
            if (!equalStackTraceElements(thisElt, anotherElt)) {
                nonMatchingIndex = i;
                break;
            }
        }
        if (nonMatchingIndex == -1) {
            return true;
        }
        final int minEntriesInStackTrace = Math.min(thisStackTrace.size(), anotherStackTrace.size());
        int matchingEntriesQty = nonMatchingIndex;
        int nonMatchingEntriesQty = minEntriesInStackTrace - nonMatchingIndex; // 2 nonmatching, 2 vs 4 entries, 2 matching
        return (matchingEntriesQty >= minEntriesInStackTrace / 2) || (nonMatchingEntriesQty <= minEntriesInStackTrace / 2 && nonMatchingEntriesQty < 5 || nonMatchingIndex > 7);
    }

    private boolean equalStackTraceElements(StackTraceElement e1, StackTraceElement e2) {
        return e1 == e2 || e1.getClassName().equals(e2.getClassName()) && Objects.equals(e1.getMethodName(), e2.getMethodName());
    }

    private static ArrayList<StackTraceElement> reverse(List<StackTraceElement> stackTrace) {
        final ArrayList<StackTraceElement> result = new ArrayList<>(stackTrace);
        Collections.reverse(result);
        return result;
    }

    public String[] getBasePackages() {
        return basePackages;
    }

    private static boolean belongsToBasePackage(StackTraceElement stackTraceElement, String[] basePackages) {
        for (String basePackage : basePackages) {
            if (stackTraceElement.getClassName().contains(basePackage)) {
                return true;
            }
        }
        return false;
    }

    public String oneLineBasePackageDescription() {
        return "\t" + threadId.id + "->" + basePackagesStackTrace.get(0);
    }
}
