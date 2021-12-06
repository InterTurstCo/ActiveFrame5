package ru.intertrust.cm.core.business.api.dto;

import java.util.Date;

/**
 * Фильтр расширенного поиска, позволяющий искать документ по факту того, что значение определённого поля
 * этого документа типа {@link FieldType#DATETIME}, {@link FieldType#DATETIMEWITHTIMEZONE} или
 * {@link FieldType#TIMELESSDATE} попадает в заданный период времени.
 * Фильтр поддерживает открытые периоды, что задаётся значением null в поле начала или окончания периода.
 * 
 * @author apirozhkov
 *
 */
public class TimeIntervalFilter extends SearchFilterBase {

    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private Date startTime;
    private Date endTime;

    private boolean startInclusive = true;
    private boolean endInclusive = true;


    public TimeIntervalFilter() {
    }

    public TimeIntervalFilter(String fieldName) {
        super(fieldName);
    }

    public TimeIntervalFilter(String fieldName, Date start, Date end) {
        super(fieldName);
        this.startTime = start;
        this.endTime = end;
    }

    public TimeIntervalFilter(String fieldName, Date start, boolean startInclusive, Date end, boolean endInclusive) {
        super(fieldName);
        this.startTime = start;
        this.startInclusive = startInclusive;
        this.endTime = end;
        this.endInclusive = endInclusive;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public boolean isStartInclusive() {
        return startInclusive;
    }

    public boolean isEndInclusive() {
        return endInclusive;
    }

    @Override
    public String toString() {
        return "from antiquity to eternity";
    }

}
