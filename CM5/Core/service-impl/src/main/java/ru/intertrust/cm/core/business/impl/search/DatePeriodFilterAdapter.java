package ru.intertrust.cm.core.business.impl.search;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.intertrust.cm.core.business.api.dto.DatePeriodFilter;
import ru.intertrust.cm.core.business.api.dto.SearchQuery;

public class DatePeriodFilterAdapter implements FilterAdapter<DatePeriodFilter> {

    protected Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public String getFilterString(DatePeriodFilter filter, SearchQuery query) {
        if (filter.getStartDate() == null && filter.getEndDate() == null) {
            log.warn("Empty date search filter for " + filter.getFieldName() + " ignored");
            return null;
        }
        StringBuilder str = new StringBuilder()
                .append(SolrFields.FIELD_PREFIX)
                .append(SearchFieldType.DATE.getInfix())
                .append(filter.getFieldName().toLowerCase())
                .append(":[")
                .append(dateToString(filter.getStartDate()))
                .append(" TO ")
                .append(dateToString(filter.getEndDate()))
                .append("]");
        return str.toString();
    }

    private static final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SSS'Z'");

    private static String dateToString(Date date) {
        if (date == null) {
            return "*";
        }
        return formatter.format(date);
    }
}
