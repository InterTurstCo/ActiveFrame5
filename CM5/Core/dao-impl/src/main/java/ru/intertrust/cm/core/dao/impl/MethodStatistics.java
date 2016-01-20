package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.business.api.util.LongCounter;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Denis Mitavskiy
 *         Date: 22.10.2015
 *         Time: 13:33
 */
class MethodStatistics {
    private static final long MILLIES_IN_HOUR = 3600000;
    private final String methodDescription;
    private final boolean isGetter;

    private LongCounter hourlyCounter;
    private LongCounter totalCounter;

    public MethodStatistics(String methodDescription) {
        this(methodDescription, false);
    }

    public MethodStatistics(String methodDescription, boolean isGetter) {
        this.methodDescription = methodDescription;
        this.isGetter = isGetter;
        reset(false);
    }

    public String getMethodDescription() {
        return methodDescription;
    }

    public boolean isGetter() {
        return isGetter;
    }

    public LongCounter getHourlyCounter() {
        return hourlyCounter;
    }

    public LongCounter getTotalCounter() {
        return totalCounter;
    }

    public void log(long timeNanos) {
        log(timeNanos, false);
    }

    public void log(long timeNanos, boolean cacheHit) {
        hourlyCounter.log(timeNanos, cacheHit);
        totalCounter.log(timeNanos, cacheHit);
    }

    public void reset(boolean hourlyOnly) {
        hourlyCounter = new LongCounter(MILLIES_IN_HOUR);
        if (hourlyOnly) {
            return;
        }
        totalCounter = new LongCounter();
    }

    public static MethodStatistics summarize(Collection<MethodStatistics> methodsStatistics) {
        ArrayList<LongCounter> hourlyCounters = new ArrayList<>(methodsStatistics.size());
        ArrayList<LongCounter> totalCounters = new ArrayList<>(methodsStatistics.size());
        for (MethodStatistics methodsStatistic : methodsStatistics) {
            hourlyCounters.add(methodsStatistic.hourlyCounter);
            totalCounters.add(methodsStatistic.totalCounter);
        }
        final LongCounter hourlySummary = LongCounter.summarize(hourlyCounters);
        final LongCounter totalSummary = LongCounter.summarize(totalCounters);
        final MethodStatistics result = new MethodStatistics("");
        result.hourlyCounter = hourlySummary;
        result.totalCounter = totalSummary;
        return result;
    }
}
