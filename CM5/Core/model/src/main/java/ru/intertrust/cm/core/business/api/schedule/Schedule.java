package ru.intertrust.cm.core.business.api.schedule;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Класс описывающий расписание выполнения заданий. Задание активируется когда походит фильт по всем полям данного
 * класса. Фильтр может задаваться следующими значениями (синтаксис подобен linux cron сервису).
 * <ol>
 * число-фильтр проходит когда часть текущего времени совпадает с фильтром. например поле minute="1", фильтр пройдет в
 * первую минуту каждого часа.
 * <ol>
 * звезда - фильтр проходит всегда
 * <ol>
 * звезда разделить на число. Фильтр проходит когда текущая часть времени кратна числу в дроби. Например поле
 * hour="звезда/2", фильтр пройдет по четным часам 0,2,4...22
 * @author larin
 * 
 */
public class Schedule implements Dto{
    private String year;
    private String month;
    private String dayOfWeek;
    private String dayOfMonth;
    private String hour;
    private String minute;

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(String dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public String getMinute() {
        return minute;
    }

    public void setMinute(String minute) {
        this.minute = minute;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dayOfMonth == null) ? 0 : dayOfMonth.hashCode());
        result = prime * result + ((dayOfWeek == null) ? 0 : dayOfWeek.hashCode());
        result = prime * result + ((hour == null) ? 0 : hour.hashCode());
        result = prime * result + ((minute == null) ? 0 : minute.hashCode());
        result = prime * result + ((month == null) ? 0 : month.hashCode());
        result = prime * result + ((year == null) ? 0 : year.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Schedule other = (Schedule) obj;
        if (dayOfMonth == null) {
            if (other.dayOfMonth != null)
                return false;
        } else if (!dayOfMonth.equals(other.dayOfMonth))
            return false;
        if (dayOfWeek == null) {
            if (other.dayOfWeek != null)
                return false;
        } else if (!dayOfWeek.equals(other.dayOfWeek))
            return false;
        if (hour == null) {
            if (other.hour != null)
                return false;
        } else if (!hour.equals(other.hour))
            return false;
        if (minute == null) {
            if (other.minute != null)
                return false;
        } else if (!minute.equals(other.minute))
            return false;
        if (month == null) {
            if (other.month != null)
                return false;
        } else if (!month.equals(other.month))
            return false;
        if (year == null) {
            if (other.year != null)
                return false;
        } else if (!year.equals(other.year))
            return false;
        return true;
    }
}
