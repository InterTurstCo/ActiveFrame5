package ru.intertrust.cm.core.business.api.dto;

import ru.intertrust.cm.core.model.GwtIncompatible;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Класс представляет дату и время в виде календаря (год, месяц, день месяца, часы, минуты, секунды,
 * миллисекунды) с часовым поясом (или временнЫм смещением).
 * Основным предназначением служит поддержка клиентов, не способных произвести конвертацию даты и
 * времени с учетом часового пояса, таких как браузер.
 *
 * @author vmatsukevich
 *         Date: 10/24/13
 *         Time: 1:43 PM
 */
public class DateTimeWithTimeZone implements Dto {

    private int year;
    private int month; // нумерация начинается с 0 (0 - январь)
    private int dayOfMonth;
    private int hours;
    private int minutes;
    private int seconds;
    private int milliseconds;
    private TimeZoneContext timeZoneContext;

    public DateTimeWithTimeZone() {
    }

    @Deprecated
    public DateTimeWithTimeZone(int year, int month, int dayOfMonth) {
        this.year = year;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
    }

    @Deprecated
    public DateTimeWithTimeZone(int year, int month, int dayOfMonth, int hours, int minute, int seconds) {
        this(year, month, dayOfMonth);
        this.hours = hours;
        this.minutes = minute;
        this.seconds = seconds;
    }

    @Deprecated
    public DateTimeWithTimeZone(int year, int month, int dayOfMonth, int hours, int minute, int seconds, int milliseconds) {
        this(year, month, dayOfMonth, hours, minute, seconds);
        this.milliseconds = milliseconds;
    }

    public DateTimeWithTimeZone(int timeZoneUtcOffset, int year, int month, int dayOfMonth) {
        this.timeZoneContext = new UTCOffsetTimeZoneContext(timeZoneUtcOffset);
        this.year = year;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
    }

    public DateTimeWithTimeZone(int timeZoneUtcOffset, int year, int month, int dayOfMonth, int hours, int minutes, int seconds, int milliseconds) {
        this(timeZoneUtcOffset, year, month, dayOfMonth);
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        this.milliseconds = milliseconds;
    }

    public DateTimeWithTimeZone(String timeZoneId, int year, int month, int dayOfMonth) {
        this.timeZoneContext = new OlsonTimeZoneContext(timeZoneId);
        this.year = year;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
    }

    public DateTimeWithTimeZone(String timeZoneId, int year, int month, int dayOfMonth, int hours, int minutes, int seconds, int milliseconds) {
        this(timeZoneId, year, month, dayOfMonth);
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        this.milliseconds = milliseconds;
    }

    public DateTimeWithTimeZone(TimeZoneContext timeZoneContext, int year, int month, int dayOfMonth, int hours, int minutes, int seconds, int milliseconds) {
        this.timeZoneContext = timeZoneContext;
        this.year = year;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        this.milliseconds = milliseconds;
    }

    @GwtIncompatible
    public DateTimeWithTimeZone(Date date, int timeZoneUtcOffset) {
        this(date, new UTCOffsetTimeZoneContext(timeZoneUtcOffset));
    }

    @GwtIncompatible
    public DateTimeWithTimeZone(Date date, TimeZone timeZone) {
        this(date, new OlsonTimeZoneContext(timeZone == null ? null : timeZone.getID()));
    }

    @GwtIncompatible
    public DateTimeWithTimeZone(Date date, TimeZoneContext timeZoneContext) {
        if (date == null) {
            return;
        }
        final TimeZone timeZone = TimeZone.getTimeZone(timeZoneContext.getTimeZoneId());
        final Calendar cal = Calendar.getInstance(timeZone);
        cal.setTime(date);

        this.timeZoneContext = timeZoneContext;
        this.year = cal.get(Calendar.YEAR);
        this.month = cal.get(Calendar.MONTH);
        this.dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        this.hours = cal.get(Calendar.HOUR_OF_DAY);
        this.minutes = cal.get(Calendar.MINUTE);
        this.seconds = cal.get(Calendar.SECOND);
        this.milliseconds = cal.get(Calendar.MILLISECOND);
    }

    @GwtIncompatible
    public Date toDate() {
        final TimeZone timeZone = TimeZone.getTimeZone(getTimeZoneContext().getTimeZoneId());
        final Calendar cal = Calendar.getInstance(timeZone);
        cal.set(Calendar.YEAR, getYear());
        cal.set(Calendar.MONTH, getMonth());
        cal.set(Calendar.DAY_OF_MONTH, getDayOfMonth());
        cal.set(Calendar.HOUR_OF_DAY, getHours());
        cal.set(Calendar.MINUTE, getMinutes());
        cal.set(Calendar.SECOND, getSeconds());
        cal.set(Calendar.MILLISECOND, getMilliseconds());
        return cal.getTime();
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public int getSeconds() {
        return seconds;
    }

    public int getMilliseconds() {
        return milliseconds;
    }

    public TimeZoneContext getTimeZoneContext() {
        return timeZoneContext;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DateTimeWithTimeZone that = (DateTimeWithTimeZone) o;

        if (dayOfMonth != that.dayOfMonth) {
            return false;
        }
        if (hours != that.hours) {
            return false;
        }
        if (milliseconds != that.milliseconds) {
            return false;
        }
        if (minutes != that.minutes) {
            return false;
        }
        if (month != that.month) {
            return false;
        }
        if (seconds != that.seconds) {
            return false;
        }
        if (year != that.year) {
            return false;
        }
        if (timeZoneContext != null ? !timeZoneContext.equals(that.timeZoneContext) : that.timeZoneContext != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = year;
        result = 31 * result + month;
        result = 31 * result + dayOfMonth;
        result = 31 * result + hours;
        result = 31 * result + minutes;
        result = 31 * result + seconds;
        result = 31 * result + milliseconds;
        result = 31 * result + (timeZoneContext != null ? timeZoneContext.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return year + "-" + (month + 1) + "-" + dayOfMonth + " " + hours + ':' + minutes + ':' + seconds + '.' + milliseconds + ", " + timeZoneContext;
    }
}
