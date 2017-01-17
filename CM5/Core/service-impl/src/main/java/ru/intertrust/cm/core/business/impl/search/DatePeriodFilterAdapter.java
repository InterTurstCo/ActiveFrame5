package ru.intertrust.cm.core.business.impl.search;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.intertrust.cm.core.business.api.dto.DatePeriodFilter;
import ru.intertrust.cm.core.business.api.dto.SearchQuery;
import ru.intertrust.cm.core.business.api.dto.TimelessDate;
import ru.intertrust.cm.core.business.api.util.ThreadSafeDateFormat;

@Deprecated
public class DatePeriodFilterAdapter implements FilterAdapter<DatePeriodFilter> {

    private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss:SSS'Z'";
    protected Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public String getFilterString(DatePeriodFilter filter, SearchQuery query) {
        if (filter.getStartDate() == null && filter.getEndDate() == null) {
            log.warn("Empty date search filter for " + filter.getFieldName() + " ignored");
            return null;
        }
        StringBuilder str = new StringBuilder()
                .append(SolrFields.FIELD_PREFIX)
                .append(SearchFieldType.DATE.infix)
                .append(filter.getFieldName().toLowerCase())
                .append(":[")
                .append(dateToString(filter.getStartDate(), true))
                .append(" TO ")
                .append(dateToString(filter.getEndDate(), false))
                .append("]");
        return str.toString();
    }

    private static String dateToString(TimelessDate date, boolean start) {
        if (date == null) {
            return "*";
        }
        Calendar time = Calendar.getInstance();
        time.setTime(date.toDate());
        time.set(Calendar.HOUR_OF_DAY, start ? 0 : 23);
        time.set(Calendar.MINUTE, start ? 0 : 59);
        time.set(Calendar.SECOND, start ? 0 : 59);
        time.set(Calendar.MILLISECOND, start ? 0 : 999);
        return ThreadSafeDateFormat.format(time.getTime(), DATE_PATTERN);
    }
}
