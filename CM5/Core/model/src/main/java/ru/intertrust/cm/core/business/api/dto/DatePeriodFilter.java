package ru.intertrust.cm.core.business.api.dto;

import java.util.Date;
import java.util.TimeZone;

import ru.intertrust.cm.core.business.api.util.ThreadSafeDateFormat;

/**
 * @deprecated Устаревший фильтр, используйте вместо него {@link TimeIntervalFilter}.
 * 
 * Фильтр расширенного поиска, позволяющий искать документ по факту того, что значение определённого поля
 * этого документа попадает в заданный период дат.
 * Фильтр поддерживает открытые периоды, что задаётся значением null в поле начала или окончания периода.
 * 
 * @author apirozhkov
 */
@Deprecated
public class DatePeriodFilter extends SearchFilterBase {

    private static final String DATE_PATTERN = "yyyy-MM-dd";

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    private TimelessDate startDate;
    private TimelessDate endDate;
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
     * Создаёт экземпляр фильтра с заданным именем поля и периодом дат.
     * Либо начало, либо окончание периода (но не оба вместе) могут быть не заданы (параметр равен null).
     * 
     * @param fieldName имя поля
     * @param start начало диапазона
     * @param end конец диапазона
     */
    public DatePeriodFilter(String fieldName, Date start, Date end) {
        super(fieldName);
        if (start == null && end == null) {
            throw new IllegalArgumentException("Either start or end must be non-null");
        }
        if (start != null) {
            this.startDate = new TimelessDate(start, UTC);
        }
        if (end != null) {
            this.endDate = new TimelessDate(end, UTC);
        }
    }

    /**
     * Создаёт экземпляр фильтра с заданным именем поля и перидом дат.
     * Либо начало, либо окончание периода (но не оба вместе) могут быть не заданы (параметр равен null).
     * 
     * @param fieldName имя поля
     * @param start начало диапазона
     * @param end конец диапазона
     */
    public DatePeriodFilter(String fieldName, TimelessDate start, TimelessDate end) {
        super(fieldName);
        if (start == null && end == null) {
            throw new IllegalArgumentException("Either start or end must be non-null");
        }
        this.startDate = start;
        this.endDate = end;
    }

    /**
     * Возвращает начало периода или null, если период открыт в прошлое.
     * @return дата начала периода
     */
    public TimelessDate getStartDate() {
        return startDate;
    }

    /**
     * Задаёт начало периода.
     * @param startDate дата начала периода, может быть null
     */
    public void setStartDate(TimelessDate startDate) {
        this.startDate = startDate;
    }

    /**
     * Задаёт начало периода.
     * @param startDate дата начала периода, может быть null
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate != null ? new TimelessDate(startDate, UTC) : null;
    }

    /**
     * Возвращает конец периода или null, если период открыт в будущее.
     * @return дата окончания периода
     */
    public TimelessDate getEndDate() {
        return endDate;
    }

    /**
     * Задаёт окончание периода.
     * @param endDate дата окончания периода, может быть null
     */
    public void setEndDate(TimelessDate endDate) {
        this.endDate = endDate;
    }

    /**
     * Задаёт окончание периода.
     * @param endDate дата окончания периода, может быть null
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate != null ? new TimelessDate(endDate, UTC) : null;
    }

    @Override
    public String toString() {        
        return "from " + (startDate == null ? "antiquity" : ThreadSafeDateFormat.format(startDate.toDate(), DATE_PATTERN))
                + " to " + (endDate == null ? "eternity" : ThreadSafeDateFormat.format(endDate.toDate(), DATE_PATTERN));
    }

}
