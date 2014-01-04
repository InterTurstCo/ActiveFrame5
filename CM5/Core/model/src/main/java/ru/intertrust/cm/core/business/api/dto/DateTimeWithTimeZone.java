package ru.intertrust.cm.core.business.api.dto;

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

    public DateTimeWithTimeZone(int year, int month, int dayOfMonth) {
        this.year = year;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
    }

    public DateTimeWithTimeZone(int year, int month, int dayOfMonth, int hours, int minute, int seconds) {
        this(year, month, dayOfMonth);
        this.hours = hours;
        this.minutes = minute;
        this.seconds = seconds;
    }

    public DateTimeWithTimeZone(int year, int month, int dayOfMonth, int hours, int minute, int seconds, int milliseconds) {
        this(year, month, dayOfMonth, hours, minute, seconds);
        this.milliseconds = milliseconds;
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

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public int getMilliseconds() {
        return milliseconds;
    }

    public void setMilliseconds(int milliseconds) {
        this.milliseconds = milliseconds;
    }

    public TimeZoneContext getTimeZoneContext() {
        return timeZoneContext;
    }

    public void setTimeZoneContext(TimeZoneContext timeZoneContext) {
        this.timeZoneContext = timeZoneContext;
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

}
