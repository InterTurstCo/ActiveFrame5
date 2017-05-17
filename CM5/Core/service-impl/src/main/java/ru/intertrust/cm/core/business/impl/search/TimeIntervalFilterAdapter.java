package ru.intertrust.cm.core.business.impl.search;

import java.util.Date;
import java.util.Set;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.SearchQuery;
import ru.intertrust.cm.core.business.api.dto.TimeIntervalFilter;
import ru.intertrust.cm.core.business.api.util.ThreadSafeDateFormat;

public class TimeIntervalFilterAdapter implements FilterAdapter<TimeIntervalFilter> {

    private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss:SSS'Z'";
    private static final TimeZone UTC_TZ = TimeZone.getTimeZone("UTC");
    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired private SearchConfigHelper configHelper;

    @Override
    public String getFilterString(TimeIntervalFilter filter, SearchQuery query) {
        if (filter.getStartTime() == null && filter.getEndTime() == null) {
            log.warn("Empty date search filter for " + filter.getFieldName() + " ignored");
            return null;
        }

        Set<SearchFieldType> types = configHelper.getFieldTypes(filter.getFieldName(), query.getAreas());
        if (types.contains(null)) {
            types.add(SearchFieldType.DATE);
        }
        if (types.contains(SearchFieldType.DATE)) {
            String single = makeSolrFieldFilter(filter, SearchFieldType.DATE);
            if (!types.contains(SearchFieldType.DATE_MULTI)) {
                return single;
            }
            String multi = makeSolrFieldFilter(filter, SearchFieldType.DATE_MULTI);
            return new StringBuilder()
                    .append("(").append(single).append(" OR ").append(multi).append(")")
                    .toString();
        } else if (types.contains(SearchFieldType.DATE_MULTI)) {
            return makeSolrFieldFilter(filter, SearchFieldType.DATE_MULTI);
        }
        log.warn("Configured fields for field " + filter.getFieldName() + " not found in areas " + query.getAreas());
        return null;
    }

    @Override
    public boolean isCompositeFilter(TimeIntervalFilter filter) {
        return false;
    }

    private static String makeSolrFieldFilter(TimeIntervalFilter filter, SearchFieldType type) {
        StringBuilder str = new StringBuilder()
                .append(SolrFields.FIELD_PREFIX)
                .append(type.infix)
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
        return ThreadSafeDateFormat.format(time, DATE_PATTERN, UTC_TZ);
    }
}
