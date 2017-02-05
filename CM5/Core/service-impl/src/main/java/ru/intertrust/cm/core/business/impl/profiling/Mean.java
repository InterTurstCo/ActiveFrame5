package ru.intertrust.cm.core.business.impl.profiling;

/**
 * @author Denis Mitavskiy
 *         Date: 03.02.2017
 *         Time: 21:45
 */
public class Mean {
    private final double MAGNIFIER = SizeUnit.Megabyte.toBytes(1.0);
    private final double MAGNIFIER_SQUARE = MAGNIFIER * MAGNIFIER;
    private final double BACK_MAGNIFIER = 1 / MAGNIFIER;
    private final double BACK_MAGNIFIER_SQUARE = BACK_MAGNIFIER * BACK_MAGNIFIER;

    private double sum;
    private double sumOfSquares;
    private double n;
    private double warnSigmas;

    public Mean(double warnSigmas) {
        this.warnSigmas = warnSigmas;
    }

    public void add(double x) {
        final double y = x * BACK_MAGNIFIER;
        sum += y;
        sumOfSquares += y * y;
        ++n;
    }

    public double getMean() {
        return n == 0.0 ? 0.0 : sum * MAGNIFIER / n;
    }

    public double getSigmaSquare() {
        return n == 0.0 ? 0.0 : MAGNIFIER_SQUARE * (sumOfSquares - sum / n * sum) / n;
    }

    public double getSigma() {
        return Math.sqrt(getSigmaSquare());
    }

    public double getWarnValue() {
        return getMean() + warnSigmas * getSigma();
    }

    public boolean warn(double value) {
        return value > getWarnValue();
    }
}
