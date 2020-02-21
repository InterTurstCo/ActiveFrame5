package ru.intertrust.cm.core.business.api.dto;

import ru.intertrust.cm.core.model.GwtIncompatible;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Класс представляет дату без времени в виде календаря (год, месяц, день месяца).
 *
 * @author vmatsukevich
 *         Date: 10/24/13
 *         Time: 1:43 PM
 */
public class TimelessDate implements Dto, Comparable<TimelessDate> {

    private int year;
    private int month; // нумерация начинается с 0 (0 - январь)
    private int dayOfMonth;

    public TimelessDate() {
    }

    public TimelessDate(int year, int month, int dayOfMonth) {
        this.year = year;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
    }

    @GwtIncompatible
    public TimelessDate(Date date, TimeZone timeZone) {
        if (date == null) {
            return;
        }
        final Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setTime(date);
        this.year = calendar.get(Calendar.YEAR);
        this.month = calendar.get(Calendar.MONTH);
        this.dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
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

    /**
     * Преобразует объект в стандартный <code>java.util.Date</code>.
     * @return момент времени, соответствующий полночи содержащейся в объекте даты по UTC
     */
    @GwtIncompatible
    public Date toDate() {
        return toDate(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Преобразует объект в стандартный <code>java.util.Date</code> с учётом часового пояса.
     * @param timeZone часовой пояс
     * @return момент времени, соответствующий полночи содержащейся в объекте даты в заданном часовом поясе
     */
    @GwtIncompatible
    public Date toDate(TimeZone timeZone) {
        final Calendar calendar = Calendar.getInstance(timeZone);
        calendar.clear();
        calendar.set(getYear(), getMonth(), getDayOfMonth());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * Преобразует объект в стандартный <code>java.util.Date</code> с учётом часового пояса.
     * @param timeZoneId идентификатор часового пояса
     * @return момент времени, соответствующий полночи содержащейся в объекте даты в заданном часовом поясе
     */
    @GwtIncompatible
    public Date toDate(String timeZoneId) {
        return toDate(TimeZone.getTimeZone(timeZoneId));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TimelessDate that = (TimelessDate) o;

        if (dayOfMonth != that.dayOfMonth) {
            return false;
        }
        if (month != that.month) {
            return false;
        }
        if (year != that.year) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = year;
        result = 31 * result + month;
        result = 31 * result + dayOfMonth;
        return result;
    }

    @Override
    public String toString() {
        return year + ":" + (month + 1) + ":" + dayOfMonth;
    }

    @Override
    public int compareTo(TimelessDate o) {
        if (o == null) {
            return 1;
        }
        if (year != o.year) {
            return year > o.year ? 1 : -1;
        }
        if (month != o.month) {
            return month > o.month ? 1 : -1;
        }
        if (dayOfMonth != o.dayOfMonth) {
            return dayOfMonth > o.dayOfMonth ? 1 : -1;
        }
        return 0;
    }
}
