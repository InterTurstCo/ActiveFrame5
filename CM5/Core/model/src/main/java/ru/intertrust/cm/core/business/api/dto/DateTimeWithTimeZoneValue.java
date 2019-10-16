package ru.intertrust.cm.core.business.api.dto;

import ru.intertrust.cm.core.model.GwtIncompatible;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Значение поля доменного объекта, определяющее дату и время в виде календаря с часовым поясом
 * или временнЫм смещением.
 * @author vmatsukevich
 *         Date: 10/24/13
 *         Time: 3:29 PM
 */
public class DateTimeWithTimeZoneValue extends Value<DateTimeWithTimeZoneValue> {
    private DateTimeWithTimeZone value;

    /**
     * Создает пустое значение даты с часовым поясом
     */
    public DateTimeWithTimeZoneValue() {
    }

    /**
     * Создает значение даты с часовым поясом
     * @param value значение даты с часовым поясом
     */
    public DateTimeWithTimeZoneValue(DateTimeWithTimeZone value) {
        this.value = value;
    }

    @GwtIncompatible
    public DateTimeWithTimeZoneValue(Date date, TimeZone timeZone) {
        this.value = new DateTimeWithTimeZone(date, timeZone);
    }

    @Override
    public DateTimeWithTimeZone get() {
        return value;
    }

    @Override
    public boolean isImmutable() {
        return true;
    }

    @Override
    public final DateTimeWithTimeZoneValue getPlatformClone() {
        final DateTimeWithTimeZone dt = get();

        final TimeZoneContext timeZoneContext = dt == null ? null : dt.getTimeZoneContext();
        final OlsonTimeZoneContext olsonContext = timeZoneContext == null || !(timeZoneContext instanceof OlsonTimeZoneContext) ? null : ((OlsonTimeZoneContext) timeZoneContext);
        final UTCOffsetTimeZoneContext utcContext = timeZoneContext == null || !(timeZoneContext instanceof UTCOffsetTimeZoneContext) ? null : ((UTCOffsetTimeZoneContext) timeZoneContext);
        if (this.getClass() != DateTimeWithTimeZoneValue.class
                || (dt != null && dt.getClass() != DateTimeWithTimeZone.class)
                || olsonContext != null && olsonContext.getClass() != OlsonTimeZoneContext.class
                || utcContext != null && utcContext.getClass() != UTCOffsetTimeZoneContext.class) {
            if (dt == null) {
                return new DateTimeWithTimeZoneValue();
            }
            final TimeZoneContext clonedContext;
            if (timeZoneContext == null) {
                clonedContext = null;
            } else if (olsonContext != null) {
                clonedContext = olsonContext.getClass() == OlsonTimeZoneContext.class ? olsonContext : new OlsonTimeZoneContext(((OlsonTimeZoneContext) timeZoneContext).getTimeZoneId());
            } else if (utcContext != null) {
                clonedContext = utcContext.getClass() == UTCOffsetTimeZoneContext.class ? utcContext : new UTCOffsetTimeZoneContext((int) ((UTCOffsetTimeZoneContext) timeZoneContext).getOffset());
            } else {
                clonedContext = null;
            }
            final DateTimeWithTimeZone clonedDateTimeWithTimeZone = new DateTimeWithTimeZone(clonedContext, dt.getYear(), dt.getMonth(), dt.getDayOfMonth(), dt.getHours(), dt.getMinutes(), dt.getSeconds(), dt.getMilliseconds());
            return new DateTimeWithTimeZoneValue(clonedDateTimeWithTimeZone);
        } else {
            return this;
        }
    }

    public DateTimeWithTimeZone getValue() {
        return value;
    }

    @GwtIncompatible
    @Override
    public int compareTo(DateTimeWithTimeZoneValue o) {
        if (o == null || o.isEmpty()) {
            return this.isEmpty() ? 0 : 1;
        } else {
            return this.isEmpty() ? -1 : compareNotNullValues(value, o.value);
        }
    }

    @GwtIncompatible
    private static int compareNotNullValues(DateTimeWithTimeZone o1, DateTimeWithTimeZone o2) {
        final Calendar cal1 = Calendar.getInstance(TimeZone.getTimeZone(o1.getTimeZoneContext().getTimeZoneId()));
        cal1.set(o1.getYear(), o1.getMonth(), o1.getDayOfMonth(), o1.getHours(), o1.getMinutes(), o1.getSeconds());
        cal1.set(Calendar.MILLISECOND, o1.getMilliseconds());

        final Calendar cal2 = Calendar.getInstance(TimeZone.getTimeZone(o2.getTimeZoneContext().getTimeZoneId()));
        cal1.set(o2.getYear(), o2.getMonth(), o2.getDayOfMonth(), o2.getHours(), o2.getMinutes(), o2.getSeconds());
        cal1.set(Calendar.MILLISECOND, o2.getMilliseconds());

        return cal1.compareTo(cal2);
    }

    public int gwtCompareTo(DateTimeWithTimeZoneValue o) {
        if (o == null || o.isEmpty()) {
            return 1;
        }
        DateTimeWithTimeZone oValue = o.value;
        final int thisYear = value.getYear();
        final int oYear = oValue.getYear();
        if (thisYear != oYear) {
            return thisYear > oYear ? 1 : -1;
        }
        final int thisMonth = value.getMonth();
        final int oMonth = oValue.getMonth();
        if (thisMonth != oMonth) {
            return thisMonth > oMonth ? 1 : -1;
        }
        final int thisDay = value.getDayOfMonth();
        final int oDay = oValue.getDayOfMonth();
        if (thisDay != oDay) {
            return thisDay > oDay ? 1 : -1;
        }
        final int thisHours = value.getHours();
        final int oHours = oValue.getHours();
        if (thisHours != oHours) {
            return thisHours > oHours ? 1 : -1;
        }
        final int thisMinutes = value.getMinutes();
        final int oMinutes = oValue.getMinutes();
        if (thisMinutes != oMinutes) {
            return thisMinutes > oMinutes ? 1 : -1;
        }
        final int thisSeconds = value.getSeconds();
        final int oSeconds = oValue.getSeconds();
        if (thisSeconds != oSeconds) {
            return thisSeconds > oSeconds ? 1 : -1;
        }
        final int thisMillies = value.getMilliseconds();
        final int oMillies = oValue.getMilliseconds();
        if (thisMillies != oMillies) {
            return thisMillies > oMillies ? 1 : -1;
        }
        final TimeZoneContext thisTimeZoneContext = value.getTimeZoneContext();
        final TimeZoneContext oTimeZoneContext = oValue.getTimeZoneContext();
        if (thisTimeZoneContext == null) {
            return -1;
        }
        if (oTimeZoneContext == null) {
            return 1;
        }
        return thisTimeZoneContext.getTimeZoneId().compareTo(oTimeZoneContext.getTimeZoneId());
    }
}