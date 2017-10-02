package ru.intertrust.cm.core.business.impl.schedule.execution;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskParameters;

/**
 * Параметры периодической задачи удаления логов периодических задач.<br/>
 * <br/>
 * <p>
 * Created by Myskin Sergey on 28.09.2017.
 */
@Root(name = "expiration-period")
public class CleanOldScheduleExecutionLogsParameters implements ScheduleTaskParameters {

    private static final long serialVersionUID = 4043227352170819547L;

    @Attribute(required = false)
    private Integer months;

    @Attribute(required = false)
    private Integer weeks;

    @Attribute(required = false)
    private Integer days;

    @Override
    public String toString() {
        return "CleanOldScheduleExecutionLogsParameters{" +
                "months=" + months +
                ", weeks=" + weeks +
                ", days=" + days +
                '}';
    }

    public Integer getMonths() {
        return months;
    }

    public void setMonths(Integer months) {
        this.months = months;
    }

    public Integer getWeeks() {
        return weeks;
    }

    public void setWeeks(Integer weeks) {
        this.weeks = weeks;
    }

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }

}
