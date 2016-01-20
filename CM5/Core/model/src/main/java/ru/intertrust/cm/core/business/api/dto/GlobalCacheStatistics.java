package ru.intertrust.cm.core.business.api.dto;

import ru.intertrust.cm.core.business.api.util.DecimalCounter;
import ru.intertrust.cm.core.business.api.util.LongCounter;
import ru.intertrust.cm.core.model.GwtIncompatible;

import java.util.List;
import java.util.Locale;

/**
 * @author Denis Mitavskiy
 *         Date: 22.10.2015
 *         Time: 16:59
 */
public class GlobalCacheStatistics implements Dto {
    private long size;
    private float hitCount;
    private float freeSpacePercentage;
    private List<Record> notifiersRecords;
    private Record notifiersSummary;
    private List<Record> readersRecords;
    private Record readersSummary;
    private Record allMethodsSummary;
    private CacheCleaningRecord cacheCleaningRecord;

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public float getHitCount() {
        return hitCount;
    }

    public void setHitCount(float hitCount) {
        this.hitCount = hitCount;
    }

    public float getFreeSpacePercentage() {
        return freeSpacePercentage;
    }

    public void setFreeSpacePercentage(float freeSpacePercentage) {
        this.freeSpacePercentage = freeSpacePercentage;
    }

    public List<Record> getNotifiersRecords() {
        return notifiersRecords;
    }

    public void setNotifiersRecords(List<Record> notifiersRecords) {
        this.notifiersRecords = notifiersRecords;
    }

    public Record getNotifiersSummary() {
        return notifiersSummary;
    }

    public void setNotifiersSummary(Record notifiersSummary) {
        this.notifiersSummary = notifiersSummary;
    }

    public List<Record> getReadersRecords() {
        return readersRecords;
    }

    public void setReadersRecords(List<Record> readersRecords) {
        this.readersRecords = readersRecords;
    }

    public Record getReadersSummary() {
        return readersSummary;
    }

    public void setReadersSummary(Record readersSummary) {
        this.readersSummary = readersSummary;
    }

    public Record getAllMethodsSummary() {
        return allMethodsSummary;
    }

    public void setAllMethodsSummary(Record allMethodsSummary) {
        this.allMethodsSummary = allMethodsSummary;
    }

    public CacheCleaningRecord getCacheCleaningRecord() {
        return cacheCleaningRecord;
    }

    public void setCacheCleaningRecord(CacheCleaningRecord cacheCleaningRecord) {
        this.cacheCleaningRecord = cacheCleaningRecord;
    }

    private final static String NEW_LINE = "\r\n";
    private final static double MEGABYTE = 1024 * 1024;

    @Override
    @GwtIncompatible
    public String toString() {
        final StringBuilder sb = new StringBuilder(10000);
        sb.append(NEW_LINE);
        sb.append("===================================== Cache Statistics =====================================").append(NEW_LINE);
        sb.append("Size: ").append(format(size / MEGABYTE)).append(" MB").append(NEW_LINE);
        sb.append("Hit Count, %: ").append(format(hitCount * 100)).append(NEW_LINE);
        sb.append("Free Space, %: ").append(format(freeSpacePercentage * 100)).append(NEW_LINE);
        if (notifiersRecords != null) {
            appendRecords(sb, notifiersRecords, notifiersSummary);
            appendRecords(sb, readersRecords, readersSummary);
            sb.append(allMethodsSummary).append(NEW_LINE);
            sb.append("--------------------------------------------------------------------------------------------").append(NEW_LINE);
        }
        sb.append(cacheCleaningRecord).append(NEW_LINE);
        sb.append("============================================================================================").append(NEW_LINE);
        return sb.toString();
    }

    @GwtIncompatible
    private StringBuilder appendRecords(StringBuilder sb, List<Record> records, Record summary) {
        for (Record record : records) {
            sb.append(record).append(NEW_LINE);
        }
        sb.append("-------------------------------------------------------").append(NEW_LINE);
        sb.append(summary).append(NEW_LINE);
        sb.append("-------------------------------------------------------").append(NEW_LINE);
        return sb;
    }

    @GwtIncompatible
    private static String format(double v) {
        return String.format(Locale.ENGLISH, "%1$.2f", v);
    }

    public static class Record implements Dto {
        private String methodDescription;
        private LongCounter hourlyCounter;
        private double hourlyFrequency;
        private LongCounter totalCounter;
        private double totalFrequency;

        public Record() {
        }

        public Record(String methodDescription, LongCounter hourlyCounter, double hourlyFrequency, LongCounter totalCounter, double totalFrequency) {
            this.methodDescription = methodDescription;
            this.hourlyCounter = hourlyCounter;
            this.hourlyFrequency = hourlyFrequency;
            this.totalCounter = totalCounter;
            this.totalFrequency = totalFrequency;
        }

        public String getMethodDescription() {
            return methodDescription;
        }

        public long getInvocationsPerHour() {
            return hourlyCounter.getEventCount();
        }

        public long getTimeMinPerHour() {
            return hourlyCounter.getMin();
        }

        public long getTimeMaxPerHour() {
            return hourlyCounter.getMax();
        }

        public long getTimeTotalPerHour() {
            return hourlyCounter.getTotal();
        }

        public double getTimeAvgPerHour() {
            return hourlyCounter.getAvg();
        }

        public long getInvocationsTotal() {
            return totalCounter.getEventCount();
        }

        public long getTimeMinTotal() {
            return totalCounter.getMin();
        }

        public long getTimeMaxTotal() {
            return totalCounter.getMax();
        }

        public long getTimeTotal() {
            return totalCounter.getTotal();
        }

        public double getTimeAvgTotal() {
            return totalCounter.getAvg();
        }

        public double getCacheHitPercentageTotal() {
            return totalCounter.getSubEventPercentage();
        }

        public double getCacheHitPercentagePerHour() {
            return hourlyCounter.getSubEventPercentage();
        }

        public double getHourlyFrequency() {
            return hourlyFrequency;
        }

        public double getTotalFrequency() {
            return totalFrequency;
        }

        @Override
        @GwtIncompatible
        public String toString() {
            return methodDescription + "{" +
                    "invocationsPerHour=" + getInvocationsPerHour() +
                    ", timeAvgPerHour=" + format(getTimeAvgPerHour() / 1000) +
                    ", timeMinPerHour=" + getTimeMinPerHour() / 1000 +
                    ", timeMaxPerHour=" + getTimeMaxPerHour() / 1000 +
                    ", timeTotalPerHour=" + getTimeTotalPerHour() / 1000 +
                    ", cacheHitPerHour=" + format(getCacheHitPercentagePerHour() * 100) + "% " +
                    ", frequencyPerHour=" + format(getHourlyFrequency() * 100) + "% " +
                    ", invocationsTotal=" + getInvocationsTotal() +
                    ", timeAvgTotal=" + format(getTimeAvgTotal() / 1000) +
                    ", timeMinTotal=" + getTimeMinTotal() / 1000 +
                    ", timeMaxTotal=" + getTimeMaxTotal() / 1000 +
                    ", timeTotal=" + getTimeTotal() +
                    ", cacheHitTotal=" + format(getCacheHitPercentageTotal() * 100) + "% " +
                    ", frequencyTotal=" + format(getTotalFrequency() * 100) + "% " +
                    '}';
        }
    }

    public static class CacheCleaningRecord implements Dto {
        private LongCounter timeCounter;
        private DecimalCounter freedSpaceCounter;

        public CacheCleaningRecord() {
        }

        public CacheCleaningRecord(LongCounter timeCounter, DecimalCounter freedSpaceCounter) {
            this.timeCounter = timeCounter;
            this.freedSpaceCounter = freedSpaceCounter;
        }

        public long getInvocations() {
            return timeCounter.getEventCount();
        }

        public long getTimeMin() {
            return timeCounter.getMin();
        }

        public long getTimeMax() {
            return timeCounter.getMax();
        }

        public double getTimeAvg() {
            return timeCounter.getAvg();
        }

        public double getFreedSpaceMin() {
            return freedSpaceCounter.getMin().doubleValue();
        }

        public double getFreedSpaceMax() {
            return freedSpaceCounter.getMax().doubleValue();
        }

        public double getFreedSpaceAvg() {
            return freedSpaceCounter.getAvg();
        }

        @Override
        @GwtIncompatible
        public String toString() {
            return "CacheClean {" +
                    "invocations=" + getInvocations() +
                    ", timeAvg=" + format(getTimeAvg()) +
                    ", timeMin=" + getTimeMin() +
                    ", timeMax=" + getTimeMax() +
                    ", freedSpaceAvg=" + format(getFreedSpaceAvg() * 100) + "% " +
                    ", freedSpaceMin=" + format(getFreedSpaceMin() * 100) + "% " +
                    ", freedSpaceMax=" + format(getFreedSpaceMax() * 100) + "% " +
                    '}';
        }
    }

}
