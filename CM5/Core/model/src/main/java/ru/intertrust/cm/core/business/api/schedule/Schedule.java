package ru.intertrust.cm.core.business.api.schedule;

/**
 * Класс описывающий расписание выполнения заданий. Задание активируется когда походит фильт по всем полям данного класса.
 * Фильтр может задаваться следующими значениями (синтаксис подобен linux cron сервису).
 * <ol>число-фильтр проходит когда часть текущего времени совпадает с фильтром. например поле minute="1", фильтр пройдет в первую минуту каждого часа.
 * <ol>звезда - фильтр проходит всегда
 * <ol>звезда разделить на число. Фильтр проходит когда текущая часть времени кратна числу в дроби. Например поле hour="звезда/2", фильтр пройдет по четным часам 0,2,4...22
 * @author larin
 *
 */
public class Schedule {
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
    
}
