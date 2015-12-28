package ru.intertrust.cm.core.business.api.util;

import ru.intertrust.cm.core.business.api.dto.Pair;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.core.model.GwtIncompatible;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Потокобезопасный DateFormat. Кеширует по ключу {pattern, local} объекты ThreadLocal, содержащиие форматировщик даты
 * (DateFormat).
 * @author atsvetkov
 */
@GwtIncompatible
public class ThreadSafeDateFormat {
    public static final TimeZone DEFAULT_TIME_ZONE = new ImmutableTimeZone();

    private static final ThreadLocal<HashMap<Pair, SimpleDateFormat>> dateFormatsCache = new ThreadLocal<HashMap<Pair, SimpleDateFormat>>() {
        @Override
        protected HashMap<Pair, SimpleDateFormat> initialValue() {
            return new HashMap<>();
        }
    };

    public static String format(Date date, String pattern) {
        final Pair<String, Locale> pair = new Pair<>(pattern, null);

        SimpleDateFormat dateFormat = getDateFormat(pair, null);
        return dateFormat.format(date);
    }

    /**
     * Получение DateFormat из кеша по ключу {pattern, local}.
     * @param pair пара {pattern, local}
     * @param timeZone временная зона
     * @return закешированный объект DateFormat.
     */
    public static SimpleDateFormat getDateFormat(final Pair<String, Locale> pair, final TimeZone timeZone) {
        final HashMap<Pair, SimpleDateFormat> cache = dateFormatsCache.get();
        final SimpleDateFormat cachedSimpleDateFormat = cache.get(pair);
        if (cachedSimpleDateFormat != null) {
            setTimeZone(timeZone, cachedSimpleDateFormat);
            return cachedSimpleDateFormat;
        } else {
            final SimpleDateFormat dateFormat;
            if (pair.getSecond() != null) {
                dateFormat = new SimpleDateFormat(pair.getFirst(), pair.getSecond());
            } else {
                dateFormat = new SimpleDateFormat(pair.getFirst());

            }
            dateFormat.setTimeZone(timeZone == null ? DEFAULT_TIME_ZONE : timeZone);
            cache.put(pair, dateFormat);
            return dateFormat;
        }
    }

    /**
     * Устанавливает временную зону. Если тайм зона не передана, устанавливает значение по умолчанию
     * (TimeZone.getDefault()).
     * @param timeZone
     * @param dateFormat
     */
    private static void setTimeZone(final TimeZone timeZone, final SimpleDateFormat dateFormat) {
        if (timeZone != null) {
            if (!dateFormat.getTimeZone().equals(timeZone)) {
                dateFormat.setTimeZone(timeZone);
            }
        } else {
            if (!dateFormat.getTimeZone().equals(DEFAULT_TIME_ZONE)) {
                dateFormat.setTimeZone(DEFAULT_TIME_ZONE);
            }
        }
    }

    public static String format(Date date, String pattern, Locale locale) {
        final Pair<String, Locale> pair = new Pair<>(pattern, locale);
        SimpleDateFormat dateFormat = getDateFormat(pair, null);
        return dateFormat.format(date);
    }

    public static String format(Date date, String pattern, Locale locale, TimeZone timeZone) {
        final Pair<String, Locale> pair = new Pair<>(pattern, locale);
        SimpleDateFormat dateFormat = getDateFormat(pair, timeZone);
        return dateFormat.format(date);
    }

    public static String format(Date date, String pattern, TimeZone timeZone) {
        final Pair<String, Locale> pair = new Pair<>(pattern, null);
        SimpleDateFormat dateFormat = getDateFormat(pair, timeZone);
        return dateFormat.format(date);
    }

    public static Date parse(String stringDate, String pattern, TimeZone timeZone) {
        final Pair<String, Locale> pair = new Pair<>(pattern, null);
        SimpleDateFormat dateFormat = getDateFormat(pair, timeZone);
        try {
            return dateFormat.parse(stringDate);
        } catch (ParseException e) {
            throw new FatalException("Error parsing date from string: " + stringDate);
        }

    }

    public static Date parse(String stringDate, String pattern, Locale locale, TimeZone timeZone) {
        final Pair<String, Locale> pair = new Pair<>(pattern, locale);
        SimpleDateFormat dateFormat = getDateFormat(pair, timeZone);
        try {
            return dateFormat.parse(stringDate);
        } catch (ParseException e) {
            throw new FatalException("Error parsing date from string: " + stringDate);
        }

    }

    public static Date parse(String stringDate, String pattern) {
        final Pair<String, Locale> pair = new Pair<>(pattern, null);
        SimpleDateFormat dateFormat = getDateFormat(pair, null);
        try {
            return dateFormat.parse(stringDate);
        } catch (ParseException e) {
            throw new FatalException("Error parsing date from string: " + stringDate);
        }
    }

    public static Date parse(String stringDate, String pattern, Locale locale) {
        final Pair<String, Locale> pair = new Pair<>(pattern, locale);
        SimpleDateFormat dateFormat = getDateFormat(pair, null);
        try {
            return dateFormat.parse(stringDate);
        } catch (ParseException e) {
            throw new FatalException("Error parsing date from string: " + stringDate);
        }
    }

    private static final class ImmutableTimeZone extends TimeZone {
        public final TimeZone timeZone;
        public final int hashCode;

        public ImmutableTimeZone() {
            this(TimeZone.getDefault());
        }

        private ImmutableTimeZone(TimeZone timeZone) {
            this.timeZone = timeZone;
            hashCode = timeZone.hashCode();
        }

        @Override
        public int getOffset(int era, int year, int month, int day, int dayOfWeek, int milliseconds) {
            return timeZone.getOffset(era, year, month, day, dayOfWeek, milliseconds);
        }

        @Override
        public int getOffset(long date) {
            return timeZone.getOffset(date);
        }

        @Override
        public void setRawOffset(int offsetMillis) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public int getRawOffset() {
            return timeZone.getRawOffset();
        }

        @Override
        public boolean useDaylightTime() {
            return timeZone.useDaylightTime();
        }

        @Override
        public boolean inDaylightTime(Date date) {
            return timeZone.inDaylightTime(date);
        }

        @Override
        public void setID(String ID) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public String getID() {
            return timeZone.getID();
        }

        @Override
        public String getDisplayName(boolean daylight, int style, Locale locale) {
            return timeZone.getDisplayName(daylight, style, locale);
        }

        @Override
        public int getDSTSavings() {
            return timeZone.getDSTSavings();
        }

        @Override
        public boolean observesDaylightTime() {
            return timeZone.observesDaylightTime();
        }

        @Override
        public boolean hasSameRules(TimeZone other) {
            return this == other || timeZone.hasSameRules(other);
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj || timeZone.equals(obj);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public Object clone() {
            return this;
        }

        @Override
        public String toString() {
            return timeZone.toString();
        }
    }
}
