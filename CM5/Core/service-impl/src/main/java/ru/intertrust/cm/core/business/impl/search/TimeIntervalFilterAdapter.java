package ru.intertrust.cm.core.business.impl.search;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.intertrust.cm.core.business.api.dto.SearchQuery;
import ru.intertrust.cm.core.business.api.dto.TimeIntervalFilter;
import ru.intertrust.cm.core.business.api.util.ThreadSafeDateFormat;

public class TimeIntervalFilterAdapter implements FilterAdapter<TimeIntervalFilter> {

    private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss:SSS'Z'";
    protected Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public String getFilterString(TimeIntervalFilter filter, SearchQuery query) {
        if (filter.getStartTime() == null && filter.getEndTime() == null) {
            log.warn("Empty date search filter for " + filter.getFieldName() + " ignored");
            return null;
        }
        StringBuilder str = new StringBuilder()
                .append(SolrFields.FIELD_PREFIX)
                .append(SearchFieldType.DATE.getInfix())
                .append(filter.getFieldName().toLowerCase())
                .append(":[")
                .append(dateToString(filter.getStartTime()))
                .append(" TO ")
                .append(dateToString(filter.getEndTime()))
                .append("]");
        return str.toString();
    }

    private static String dateToString(Date time) {
        if (time == null) {
            return "*";
        }
        return ThreadSafeDateFormat.format(time, DATE_PATTERN);
    }
}
