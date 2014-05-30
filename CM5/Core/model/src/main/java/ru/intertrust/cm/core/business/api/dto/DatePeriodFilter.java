package ru.intertrust.cm.core.business.api.dto;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Фильтр расширенного поиска, позволяющий искать документ по факту того, что значение определённого поля
 * этого документа попадает в заданный период времени.
 * Фильтр поддерживает открытые периоды, что задаётся значением null в поле начала или окончания периода.
 * 
 * @author apirozhkov
 */
public class DatePeriodFilter extends SearchFilterBase {

    private Date startDate;
    private Date endDate;

    /**
     * Создаёт пустой экземпляр фильтра.
     */
    public DatePeriodFilter() {
    }

    /**
     * Создаёт пустой экземпляр фильтра по заданному полю.
     * @param fieldName имя поля
     */
    public DatePeriodFilter(String fieldName) {
        super(fieldName);
    }

    /**
     * Создаёт экземпляр фильтра с заданным именем поля и периодом времени.
     * Либо начало, либо окончание периода (но не оба вместе) могут быть не заданы (параметр равен null).
     * 
     * @param fieldName имя поля
     * @param start начало диапазона
     * @param end конец диапазона
     */
    public DatePeriodFilter(String fieldName, Date start, Date end) {
        super(fieldName);
        this.startDate = start;
        this.endDate = end;
    }

    /**
     * Возвращает начало периода или null, если период открыт в прошлое.
     * @return дата начала периода
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Задаёт начало периода.
     * @param startDate дата начала периода, может быть null
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * Возвращает конец периода или null, если период открыт в будущее.
     * @return дата окончания периода
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * Задаёт окончание периода.
     * @param endDate дата окончания периода, может быть null
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @Override
    public String toString() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return "from " + (startDate == null ? "antiquity" : format.format(startDate))
                + " to " + (endDate == null ? "eternity" : format.format(endDate));
    }

}
