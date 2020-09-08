package ru.intertrust.cm.core.business.impl.search;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.DatePeriodFilter;
import ru.intertrust.cm.core.business.api.dto.SearchQuery;
import ru.intertrust.cm.core.business.api.dto.TimelessDate;
import ru.intertrust.cm.core.business.api.util.ThreadSafeDateFormat;

@Deprecated
public class DatePeriodFilterAdapter implements FilterAdapter<DatePeriodFilter> {

    @Autowired private SearchConfigHelper configHelper;

    private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    protected Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public String getFilterString(DatePeriodFilter filter, SearchQuery query) {
        if (filter.getStartDate() == null && filter.getEndDate() == null) {
            log.warn("Empty date search filter for " + filter.getFieldName() + " ignored");
            return null;
        }
        String fieldName = filter.getFieldName();
        Set<SearchFieldType> types = configHelper.getFieldTypes(fieldName, query.getAreas(), query.getTargetObjectTypes());
        if (types.size() == 0) {
            return null;
        }
        ArrayList<String> fields = new ArrayList<>(types.size());
        for (SearchFieldType type : types) {
            if (type.supportsFilter(filter)) {
                for (String field : type.getSolrFieldNames(fieldName)) {
                    fields.add(new StringBuilder()
                            .append(field)
                            .append(":")
                            .append(":[")
                            .append(dateToString(filter.getStartDate(), true))
                            .append(" TO ")
                            .append(dateToString(filter.getEndDate(), false))
                            .append("]")
                            .toString());
                }
            }
        }
        return SolrUtils.joinStrings("OR", fields);
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

    @Override
    public boolean isCompositeFilter(DatePeriodFilter filter) {
        return false;
    }

    @Override
    public List<String> getFieldNames(DatePeriodFilter filter, SearchQuery query) {
        String fieldName = filter.getFieldName();
        Set<SearchFieldType> types = configHelper.getFieldTypes(fieldName, query.getAreas(), query.getTargetObjectTypes());
        ArrayList<String> names = new ArrayList<>(types.size());
        if (types.size() == 0) {
            return names;
        }
        for (SearchFieldType type : types) {
            if (type.supportsFilter(filter)) {
                for (String field : type.getSolrFieldNames(fieldName)) {
                    names.add(field);
                }
            }
        }
        return names;
    }
}
