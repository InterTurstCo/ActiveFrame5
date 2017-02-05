package ru.intertrust.cm.core.business.impl.profiling;

/**
 * @author Denis Mitavskiy
 *         Date: 04.02.2017
 *         Time: 9:57
 */
public enum SizeUnit {
    Byte {
        public long toBytes(long size) {
            return size;
        }

        @Override
        public double toBytes(double size) {
            return size;
        }

        public long toKilobytes(long size) {
            return size / KILO;
        }

        @Override
        public double toKilobytes(double size) {
            return size / KILO;
        }

        public long toMegabytes(long size) {
            return size / MEGA;
        }

        @Override
        public double toMegabytes(double size) {
            return size / MEGA;
        }

        public long toGigabytes(long size) {
            return size / GIGA;
        }

        @Override
        public double toGigabytes(double size) {
            return size / GIGA;
        }

        public long from(SizeUnit from, long size) {
            return from.toBytes(size);
        }

        @Override
        public double from(SizeUnit from, double size) {
            return from.toBytes(size);
        }

    },

    Kilobyte {
        public long toBytes(long size) {
            return size * KILO;
        }

        @Override
        public double toBytes(double size) {
            return size * KILO;
        }

        public long toKilobytes(long size) {
            return size;
        }

        @Override
        public double toKilobytes(double size) {
            return size;
        }

        public long toMegabytes(long size) {
            return size / KILO;
        }

        @Override
        public double toMegabytes(double size) {
            return size / KILO;
        }

        public long toGigabytes(long size) {
            return size / MEGA;
        }

        @Override
        public double toGigabytes(double size) {
            return size / MEGA;
        }

        public long from(SizeUnit from, long size) {
            return from.toKilobytes(size);
        }

        @Override
        public double from(SizeUnit from, double size) {
            return from.toKilobytes(size);
        }

    },

    Megabyte {
        public long toBytes(long size) {
            return size * MEGA;
        }

        @Override
        public double toBytes(double size) {
            return size * MEGA;
        }

        public long toKilobytes(long size) {
            return size * KILO;
        }

        @Override
        public double toKilobytes(double size) {
            return size * KILO;
        }

        public long toMegabytes(long size) {
            return size;
        }

        @Override
        public double toMegabytes(double size) {
            return size;
        }

        public long toGigabytes(long size) {
            return size / KILO;
        }

        @Override
        public double toGigabytes(double size) {
            return size / KILO;
        }

        public long from(SizeUnit from, long size) {
            return from.toMegabytes(size);
        }

        @Override
        public double from(SizeUnit from, double size) {
            return from.toMegabytes(size);
        }

    },

    Gigabyte {
        public long toBytes(long size) {
            return size * GIGA;
        }

        @Override
        public double toBytes(double size) {
            return size * GIGA;
        }

        public long toKilobytes(long size) {
            return size * MEGA;
        }

        @Override
        public double toKilobytes(double size) {
            return size * MEGA;
        }

        public long toMegabytes(long size) {
            return size * KILO;
        }

        @Override
        public double toMegabytes(double size) {
            return size * KILO;
        }

        public long toGigabytes(long size) {
            return size;
        }

        @Override
        public double toGigabytes(double size) {
            return size;
        }

        public long from(SizeUnit from, long size) {
            return from.toGigabytes(size);
        }

        @Override
        public double from(SizeUnit from, double size) {
            return from.toGigabytes(size);
        }
    };

    private static final long KILO = 1024;
    private static final long MEGA = KILO * KILO;
    private static final long GIGA = MEGA * KILO;

    public abstract long from(SizeUnit from, long size);

    public abstract double from(SizeUnit from, double size);

    public abstract long toBytes(long size);

    public abstract double toBytes(double size);

    public abstract long toKilobytes(long size);

    public abstract double toKilobytes(double size);

    public abstract long toMegabytes(long size);

    public abstract double toMegabytes(double size);

    public abstract long toGigabytes(long size);

    public abstract double toGigabytes(double size);

}
