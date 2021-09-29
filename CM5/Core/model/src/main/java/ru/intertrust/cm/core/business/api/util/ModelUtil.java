package ru.intertrust.cm.core.business.api.util;

import ru.intertrust.cm.core.business.api.dto.*;

import java.util.*;

/**
 * Author: Denis Mitavskiy
 * Date: 23.05.13
 * Time: 17:42
 */
public final class ModelUtil {
    public static final String DTO_PATTERN = "dd.MM.yyyy HH:mm:ss:SSS";
    public static final String DEFAULT_DATE_PATTERN = "dd.MM.yyyy";
    public static final String DEFAULT_TIME_ZONE_ID = "default";
    public static final String LOCAL_TIME_ZONE_ID = "local";
    public static final String ORIGINAL_TIME_ZONE_ID = "original";

    /**
     * Don't create instance of helper class.
     */
    private ModelUtil() {}

    public static String getDetailedDescription(IdentifiableObject obj) {
        final String TABULATOR = "    ";
        List<String> fields = obj.getFields();
        StringBuilder result = new StringBuilder();
        result.append("Id = ").append(obj.getId()).append('\n');
        result.append("Fields: [").append('\n');
        for (String field : fields) {
            result.append(TABULATOR).append(field).append(" = ").append((Object)obj.getValue(field)).append('\n');
        }
        result.append(']').append('\n');
        return result.toString();
    }

    public static String getTableRowDescription(IdentifiableObject obj) {
        List<String> fields = obj.getFields();
        StringBuilder result = new StringBuilder();
        result.append(obj.getId()).append('\t');
        for (String field : fields) {
            result.append(obj.getValue(field) != null ? obj.getValue(field).toString() : "null").append('\t');
        }
        return result.toString();
    }

    /**
     * Returns time zone id in GMT format.
     * <b>GWT don't support {@link String#format(String, Object...)} method</b>
     * @param rawOffset timezone offset.
     * @return time zone id in GMT format.
     */
    public static String getUTCTimeZoneId(final int rawOffset) {
        final StringBuilder builder = new StringBuilder("GMT");
        if (rawOffset != 0) {
            if (rawOffset > 0) {
                builder.append('+');
            }
            final int offset = rawOffset / (60 * 1000);
            builder.append(offset / 60);
            final int minutes = Math.abs(offset % 60);
            if (minutes != 0) {
                builder.append(minutes < 10 ? ":0" : ':' ).append(minutes);
            }
        }
        return builder.toString();
    }

    public static void sort(List<? extends IdentifiableObject> objects, SortOrder sortOrder) {
        Collections.sort(objects, new IdentifiableObjectComparator(sortOrder));
    }

    public static Set<String> getFilterNames(Collection<? extends Filter> filterValues) {
        if (filterValues == null || filterValues.isEmpty()) {
            return null;
        }
        Set<String> filterNames = new HashSet<>((int) (filterValues.size() / 0.75f) + 1);
        for (Filter filter : filterValues) {
            filterNames.add(filter.getFilter());
        }
        return filterNames;
    }

    private static class IdentifiableObjectComparator implements Comparator<IdentifiableObject> {
        private ArrayList<SortCriterionComparator> comparators;

        IdentifiableObjectComparator(SortOrder sortOrder) {
            comparators = new ArrayList<>(sortOrder.size());
            for (SortCriterion criterion : sortOrder) {
                final boolean asc = criterion.getOrder() == SortCriterion.Order.ASCENDING;
                final Comparator<Value> comparator = Value.getComparator(asc, true);
                comparators.add(new SortCriterionComparator(criterion.getField(), comparator));
            }
        }

        @Override
        public int compare(IdentifiableObject o1, IdentifiableObject o2) {
            for (SortCriterionComparator comparator : comparators) {
                final int comparisonResult = comparator.compare(o1, o2);
                if (comparisonResult != 0) {
                    return comparisonResult;
                }
            }
            return 0;
        }
    }

    private static class SortCriterionComparator {
        private final String field;
        private final Comparator<Value> comparator;

        private SortCriterionComparator(String field, Comparator<Value> comparator) {
            this.field = field;
            this.comparator = comparator;
        }

        public int compare(IdentifiableObject o1, IdentifiableObject o2) {
            return comparator.compare(o1.getValue(field), o2.getValue(field));
        }
    }
}
