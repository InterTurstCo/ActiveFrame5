package ru.intertrust.cm.core.business.api.dto;

import java.util.Date;

public class DatePeriodFilter extends SearchFilterBase {

    private Date startDate;
    private Date endDate;

    public DatePeriodFilter() {
    }

    public DatePeriodFilter(String fieldName) {
        super(fieldName);
    }

    public DatePeriodFilter(String fieldName, Date start, Date end) {
        super(fieldName);
        this.startDate = start;
        this.endDate = end;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

}
