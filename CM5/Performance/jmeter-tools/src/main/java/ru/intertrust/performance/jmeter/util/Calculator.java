package ru.intertrust.performance.jmeter.util;

import java.text.DecimalFormat;

import org.apache.jmeter.samplers.SampleResult;

/**
 * Утилитарный класс, позволяющий посчитать суммарные значения(Исходный класс
 * org.apache.jmeter.util.Calculator) mean - среднее время отклика tps -
 * количество транзакций в секунду tps_system - количество транзакций в секунду,
 * которое может выполнить система.
 */
public class Calculator {

    private double sum = 0;

    private double sumOfSquares = 0;

    private double mean = 0.0;

    private Double tps = 0.0;

    private Double tps_system = 0.0;

    private double deviation = 0;

    private int count = 0;

    private long bytes = 0;

    private long maximum = Long.MIN_VALUE;

    private long minimum = Long.MAX_VALUE;

    private int errors = 0;

    private final String label;

    public Calculator() {
        this("");
    }

    public Calculator(String label) {
        this.label = label;
    }

    public void clear() {
        maximum = Long.MIN_VALUE;
        minimum = Long.MAX_VALUE;
        sum = 0;
        sumOfSquares = 0;
        mean = 0;
        deviation = 0;
        count = 0;
        tps = 0.0;
        tps_system = 0.0;
    }

    /**
     * Add the value for (possibly multiple) samples. Updates the count, sum,
     * min, max, sumOfSqaures, mean and deviation.
     * 
     * @param newValue
     *            the total value for all the samples.
     * @param sampleCount
     *            number of samples included in the value
     */
    private void addValue(long newValue, int sampleCount) {
        count += sampleCount;
        double currentVal = newValue;
        sum += currentVal;
        if (sampleCount > 1) {
            minimum = Math.min(newValue / sampleCount, minimum);
            maximum = Math.max(newValue / sampleCount, maximum);
            // For n values in an aggregate sample the average value = (val/n)
            // So need to add n * (val/n) * (val/n) = val * val / n
            sumOfSquares += (currentVal * currentVal) / (sampleCount);
        } else { // no point dividing by 1
            minimum = Math.min(newValue, minimum);
            maximum = Math.max(newValue, maximum);
            sumOfSquares += currentVal * currentVal;
        }
        // Calculate each time, as likely to be called for each add
        mean = sum / count;
        deviation = Math.sqrt((sumOfSquares / count) - (mean * mean));
        tps = 1 / (mean / 1000);
        tps_system = tps / count;
    }

    public void addBytes(long newValue) {
        bytes += newValue;
    }

    private long startTime = 0;
    private long elapsedTime = 0;

    /**
     * Add details for a sample result, which may consist of multiple samples.
     * Updates the number of bytes read, error count, startTime and elapsedTime
     * @param res
     *            the sample result; might represent multiple values
     * @see #addValue(long, int)
     */
    public void addSample(SampleResult res) {
        addBytes(res.getBytes());
        addValue(res.getTime(), res.getSampleCount());
        errors += res.getErrorCount(); // account for multiple samples
        if (startTime == 0) { // not yet intialised
            startTime = res.getStartTime();
        } else {
            startTime = Math.min(startTime, res.getStartTime());
        }
        elapsedTime = Math.max(elapsedTime, res.getEndTime() - startTime);
    }

    public long getTotalBytes() {
        return bytes;
    }

    public double getMean() {
        return mean;
    }

    public Double getTps() {
        return tps;
    }

    public Double getTpsSystem() {
        return tps_system;
    }

    public Number getMeanAsNumber() {
        return Long.valueOf((long) mean);
    }

    public double getStandardDeviation() {
        return deviation;
    }

    public long getMin() {
        return minimum;
    }

    public long getMax() {
        return maximum;
    }

    public int getCount() {
        return count;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Returns the raw double value of the percentage of samples with errors
     * that were recorded. (Between 0.0 and 1.0)
     * 
     * @return the raw double value of the percentage of samples with errors
     *         that were recorded.
     */
    public double getErrorPercentage() {
        double rval = 0.0;

        if (count == 0) {
            return (rval);
        }
        rval = (double) errors / (double) count;
        return (rval);
    }

    /**
     * Returns the throughput associated to this sampler in requests per second.
     * May be slightly skewed because it takes the timestamps of the first and
     * last samples as the total time passed, and the test may actually have
     * started before that start time and ended after that end time.
     */
    public double getRate() {
        if (elapsedTime == 0) {
            return 0.0;
        }

        return ((double) count / (double) elapsedTime) * 1000;
    }

    /**
     * calculates the average page size, which means divide the bytes by number
     * of samples.
     * 
     * @return average page size in bytes
     */
    public double getAvgPageBytes() {
        if (count > 0 && bytes > 0) {
            return (double) bytes / count;
        }
        return 0.0;
    }

    /**
     * Throughput in bytes / second
     * 
     * @return throughput in bytes/second
     */
    public double getBytesPerSecond() {
        if (elapsedTime > 0) {
            return bytes / ((double) elapsedTime / 1000); // 1000 =
                                                          // millisecs/sec
        }
        return 0.0;
    }

    /**
     * Throughput in kilobytes / second
     * 
     * @return Throughput in kilobytes / second
     */
    public double getKBPerSecond() {
        return getBytesPerSecond() / 1024; // 1024=bytes per kb
    }

    double roundTwoDecimals(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Double.valueOf(twoDForm.format(d));
    }
}
