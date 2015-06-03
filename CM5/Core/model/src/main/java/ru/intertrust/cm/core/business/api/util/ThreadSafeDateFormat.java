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
    public static SimpleDateFormat getDateFormat(final Pair<String, Locale> pair, TimeZone timeZone) {
        final HashMap<Pair, SimpleDateFormat> cache = dateFormatsCache.get();
        if (cache.get(pair) != null) {
            SimpleDateFormat dateFormat = cache.get(pair);
            setTimeZone(timeZone, dateFormat);
            return dateFormat;
        } else {
            SimpleDateFormat dateFormat;
            if (pair.getSecond() != null) {
                dateFormat = new SimpleDateFormat(pair.getFirst(), pair.getSecond());
            } else {
                dateFormat = new SimpleDateFormat(pair.getFirst());

            }
            setTimeZone(timeZone, dateFormat);
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
    private static void setTimeZone(TimeZone timeZone, SimpleDateFormat dateFormat) {
        if (timeZone != null) {
            dateFormat.setTimeZone(timeZone);
        } else {
            dateFormat.setTimeZone(TimeZone.getDefault());
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

}
