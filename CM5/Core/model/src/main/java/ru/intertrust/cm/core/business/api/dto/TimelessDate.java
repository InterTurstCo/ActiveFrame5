package ru.intertrust.cm.core.business.api.dto;

/**
 * Класс представляет дату без времени в виде календаря (год, месяц, день месяца).
 *
 * @author vmatsukevich
 *         Date: 10/24/13
 *         Time: 1:43 PM
 */
public class TimelessDate implements Dto {

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
        return new StringBuilder().append("TimelessDate{")
                .append(year).append("-")
                .append(month + 1).append("-")
                .append(dayOfMonth).append("}")
                .toString();
    }
}
