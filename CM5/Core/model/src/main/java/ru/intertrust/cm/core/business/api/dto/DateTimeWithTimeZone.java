package ru.intertrust.cm.core.business.api.dto;

import java.io.Serializable;

/**
 * Класс представляет дату и время в виде календаря (год, месяц, день месяца, часы, минуты, секунды,
 * миллисекунды) с часовым поясом (или временнЫм смещением).
 * Основным предназначением служит поддержка клиентов (браузеров, например), не имеющих возможности произвести конвертацию
 * универсального времени {@link java.util.Date} самостоятельно.
 *
 * @author vmatsukevich
 *         Date: 10/24/13
 *         Time: 1:43 PM
 */
public class DateTimeWithTimeZone implements Serializable {

    private int year;
    private int month; // 0 - 11, Январь - Декабрь
    private int dayOfMonth;
    private int hour;
    private int minute;
    private int second;
    private int millisecond;
    private DateContext context;

    public DateTimeWithTimeZone() {
    }

    public DateTimeWithTimeZone(int year, int month, int dayOfMonth) {
        this.year = year;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
    }

    public DateTimeWithTimeZone(int year, int month, int dayOfMonth, DateContext context) {
        this(year, month, dayOfMonth);
        this.context = context;
    }

    public DateTimeWithTimeZone(int year, int month, int dayOfMonth, int hour, int minute, int second) {
        this(year, month, dayOfMonth);
        this.hour = hour;
        this.minute = minute;
        this.second = second;
    }

    public DateTimeWithTimeZone(int year, int month, int dayOfMonth, int hour, int minute, int second,
                                DateContext context) {
        this(year, month, dayOfMonth, hour, minute, second);
        this.context = context;
    }

    public DateTimeWithTimeZone(int year, int month, int dayOfMonth, int hour, int minute, int second, int millisecond) {
        this(year, month, dayOfMonth, hour, minute, second);
        this.millisecond = millisecond;
    }

    public DateTimeWithTimeZone(int year, int month, int dayOfMonth, int hour, int minute, int second, int millisecond,
                                DateContext context) {
        this(year, month, dayOfMonth, hour, minute, second, millisecond);
        this.context = context;
        this.context = context;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public int getMillisecond() {
        return millisecond;
    }

    public void setMillisecond(int millisecond) {
        this.millisecond = millisecond;
    }

    public DateContext getContext() {
        return context;
    }

    public void setContext(DateContext context) {
        this.context = context;
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
        if (hour != that.hour) {
            return false;
        }
        if (millisecond != that.millisecond) {
            return false;
        }
        if (minute != that.minute) {
            return false;
        }
        if (month != that.month) {
            return false;
        }
        if (second != that.second) {
            return false;
        }
        if (year != that.year) {
            return false;
        }
        if (context != null ? !context.equals(that.context) : that.context != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = year;
        result = 31 * result + month;
        result = 31 * result + dayOfMonth;
        result = 31 * result + hour;
        result = 31 * result + minute;
        result = 31 * result + second;
        result = 31 * result + millisecond;
        result = 31 * result + (context != null ? context.hashCode() : 0);
        return result;
    }

    public static abstract class DateContext implements Serializable {
    }

    public static class TimeZoneContext extends DateContext {
        private String timeZoneId;

        public TimeZoneContext(String timeZoneId) {
            this.timeZoneId = timeZoneId;
        }

        public String getTimeZoneId() {
            return timeZoneId;
        }
    }

    public static class UtcOffsetContext extends DateContext {
        private long offset;

        public UtcOffsetContext(long offset) {
            this.offset = offset;
        }

        public long getOffset() {
            return offset;
        }
    }

}
