package ru.intertrust.cm.core.business.api.dto;

//import java.util.Calendar;
import java.util.Date;
//import java.util.TimeZone;

//import ru.intertrust.cm.core.business.api.util.ThreadSafeDateFormat;

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
/*
    public TimeIntervalFilterV(String fieldName, TimelessDate startDate, TimelessDate endDate) {
        super(fieldName);
        setStartTimelessDate(startDate);
        setEndTimelessDate(endDate);
    }

    public TimeIntervalFilterV(String fieldName, DateTimeWithTimeZone start, DateTimeWithTimeZone end) {
        super(fieldName);
        setStartTimeWithTimeZone(start);
        setEndTimeWithTimeZone(end);
    }*/

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }
/*
    public void setStartTimelessDate(TimelessDate startDate) {
        if (startDate != null) {
            this.startTime = startDate.toDate();
        } else {
            this.startTime = null;
        }
    }

    public void setStartTimeWithTimeZone(DateTimeWithTimeZone start) {
        if (start != null) {
            this.startTime = convert(start);
        } else {
            this.startTime = null;
        }
    }*/

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
/*

    public void setEndTimelessDate(TimelessDate endDate) {
        if (endDate != null) {
            long time = endDate.toDate().getTime();
            time += 24 * 60 * 60 * 1000 - 1;
            this.endTime = new Date(time);
        } else {
            this.endTime = null;
        }
    }

    public void setEndTimeWithTimeZone(DateTimeWithTimeZone end) {
        if (end != null) {
            this.endTime = convert(end);
        } else {
            this.endTime = null;
        }
    }

    // Maybe this conversion should be implemented in DateTimeWithTimeZone class?
    private Date convert(DateTimeWithTimeZone source) {
        TimeZone tz = TimeZone.getTimeZone(source.getTimeZoneContext().getTimeZoneId());
        Calendar cal = Calendar.getInstance(tz);
        cal.set(source.getYear(), source.getMonth(), source.getDayOfMonth(),
                source.getHours(), source.getMinutes(), source.getSeconds());
        cal.set(Calendar.MILLISECOND, source.getMilliseconds());
        return cal.getTime();
    }
*/

    @Override
    public String toString() {
        return "from antiquity to eternity";
    }
  /*  @Override
    public String toString() {
        return "from " + (startTime == null ? "antiquity" : ThreadSafeDateFormat.format(startTime, DATE_PATTERN))
                + " to " + (endTime == null ? "eternity" : ThreadSafeDateFormat.format(endTime, DATE_PATTERN));
    }*/
}
