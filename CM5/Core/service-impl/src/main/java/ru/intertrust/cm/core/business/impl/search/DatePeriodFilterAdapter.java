package ru.intertrust.cm.core.business.impl.search;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import ru.intertrust.cm.core.business.api.dto.DatePeriodFilter;

public class DatePeriodFilterAdapter implements FilterAdapter<DatePeriodFilter> {

    @Override
    public String getFilterValue(DatePeriodFilter filter) {
        StringBuilder str = new StringBuilder()
                .append("[")
                .append(dateToString(filter.getStartDate()))
                .append(" TO ")
                .append(dateToString(filter.getEndDate()))
                .append("]");
        return str.toString();
    }

    @Override
    public String getFieldTypeSuffix(DatePeriodFilter filter) {
        return SearchFieldType.DATE.getSuffix();
    }

    private static final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SSS'Z'");

    private static String dateToString(Date date) {
        if (date == null) {
            return "*";
        }
        return formatter.format(date);
    }
}
