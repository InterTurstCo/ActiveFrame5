package ru.intertrust.cm.core.gui.impl.server.util;

import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static ru.intertrust.cm.core.business.api.dto.util.ModelConstants.*;
import static ru.intertrust.cm.core.gui.impl.server.util.DateUtil.prepareDateTimeWithTimeZone;
import static ru.intertrust.cm.core.gui.impl.server.util.DateUtil.prepareTimeZone;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 16.01.14
 *         Time: 13:15
 */
public class FilterBuilderUtil {
    public static final String EXCLUDED_IDS_FILTER = "idsExcluded";
    public static final String INCLUDED_IDS_FILTER = "idsIncluded";

    public static Filter prepareFilter(Set<Id> ids, String type) {
        List<ReferenceValue> idsCriterion = new ArrayList<>();
        for (Id id : ids) {
            idsCriterion.add(new ReferenceValue(id));
        }
        Filter filter = createRequiredFilter(idsCriterion, type);
        return filter;
    }

    public static Filter prepareReferenceFilter(Id parentId, String filterByParentName) {
        Filter referenceFilter = new Filter();
        referenceFilter.setFilter(filterByParentName);
        referenceFilter.addCriterion(0, new ReferenceValue(parentId));
        return referenceFilter;

    }

    public static Filter prepareSearchFilter(List<String> filterValues, CollectionColumnProperties columnProperties) {
        Filter filter = new Filter();
        String filterName = (String) columnProperties.getProperty(CollectionColumnProperties.SEARCH_FILTER_KEY);
        filter.setFilter(filterName);
        String fieldType = (String) columnProperties.getProperty(CollectionColumnProperties.TYPE_KEY);
        switch (fieldType) {
            case TIMELESS_DATE_TYPE:
                try {
                    prepareTimelessDateFilter(filter, filterValues, columnProperties);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;
            case DATE_TIME_TYPE:
                try {
                    prepareDateTimeFilter(filter, filterValues, columnProperties);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;
            case DATE_TIME_WITH_TIME_ZONE_TYPE:
                try {
                    prepareDateTimeWithTimeZoneFilter(filter, filterValues, columnProperties);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;
            case LONG_TYPE:
                prepareLongFilter(filter, filterValues);
                break;
            case DECIMAL_TYPE:
                prepareDecimalFilter(filter, filterValues);
                break;
            case BOOLEAN_TYPE:
                prepareBooleanFilter(filter, filterValues);
                break;
            default:
                prepareStringFilter(filter, filterValues);
                break;
        }

        return filter;
    }


    private static Filter createRequiredFilter(List<ReferenceValue> idsCriterion, String type) {
        if (EXCLUDED_IDS_FILTER.equalsIgnoreCase(type)) {
            Filter filter = new IdsExcludedFilter(idsCriterion);
            filter.setFilter(EXCLUDED_IDS_FILTER + (int) (65 * Math.random()));
            return filter;
        }
        if (INCLUDED_IDS_FILTER.equalsIgnoreCase(type)) {
            Filter filter = new IdsIncludedFilter(idsCriterion);
            filter.setFilter(INCLUDED_IDS_FILTER + (int) (65 * Math.random()));
            return filter;
        }
        return null;
    }

    private static void prepareTimelessDateFilter(Filter filter, List<String> filterValues,
                                                  CollectionColumnProperties columnProperties) throws ParseException {

        String datePattern = (String) columnProperties.getProperty(CollectionColumnProperties.DATE_PATTERN);
        DateFormat format = new SimpleDateFormat(datePattern);
        String rangeStartFilterValue = filterValues.get(0);
        Date rangeStartDate = format.parse(rangeStartFilterValue);
        String rawTimeZone = (String) columnProperties.getProperty(CollectionColumnProperties.TIME_ZONE_ID);
        TimeZone timeZone = prepareTimeZone(rawTimeZone);
        Calendar rangeStartCalendar = Calendar.getInstance(timeZone);
        rangeStartCalendar.setTime(rangeStartDate);
        TimelessDate rangeStartTimelessDate = new TimelessDate(rangeStartCalendar.get(Calendar.YEAR),
                rangeStartCalendar.get(Calendar.MONTH),
                rangeStartCalendar.get(Calendar.DAY_OF_MONTH));
        Value rangeStartTimeValue = new TimelessDateValue(rangeStartTimelessDate);
        filter.addCriterion(0, rangeStartTimeValue);
        if (filterValues.size() == 1) {
            filter.addCriterion(1, rangeStartTimeValue);
        } else {
            String rangeEndFilterValue = filterValues.get(0);
            Date rangeEndDate = format.parse(rangeEndFilterValue);
            Calendar rangeEndCalendar = Calendar.getInstance(timeZone);
            rangeEndCalendar.setTime(rangeEndDate);
            TimelessDate rangeEndTimelessDate = new TimelessDate(rangeEndCalendar.get(Calendar.YEAR),
                    rangeEndCalendar.get(Calendar.MONTH),
                    rangeEndCalendar.get(Calendar.DAY_OF_MONTH));
            Value rangeEndTimeValue = new TimelessDateValue(rangeEndTimelessDate);
            filter.addCriterion(1, rangeEndTimeValue);
        }

    }

    private static void prepareDateTimeFilter(Filter filter, List<String> filterValues,
                                              CollectionColumnProperties columnProperties) throws ParseException {
        String datePattern = (String) columnProperties.getProperty(CollectionColumnProperties.DATE_PATTERN);
        String rawTimeZone = (String) columnProperties.getProperty(CollectionColumnProperties.TIME_ZONE_ID);
        TimeZone timeZone = prepareTimeZone(rawTimeZone);
        String timePattern = (String) columnProperties.getProperty(CollectionColumnProperties.TIME_PATTERN);
        String pattern = DateUtil.prepareDatePattern(datePattern, timePattern);
        DateFormat format = new SimpleDateFormat(pattern);
        format.setTimeZone(timeZone);
        if (filterValues.size() == 1) {
            String filterValue = filterValues.get(0);
            Date selectedDate = format.parse(filterValue);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(selectedDate);
            Date rangeStart = prepareStartOfDayForSelectedDate(calendar);
            DateTimeValue rangeStartDateTimeValue = new DateTimeValue();
            rangeStartDateTimeValue.setValue(rangeStart);
            filter.addCriterion(0, rangeStartDateTimeValue);
            DateTimeValue rangeEndDateTimeValue = new DateTimeValue();
            if (timePattern == null) {
                Date rangeEnd = prepareEndOfDayForSelectedDate(calendar);
                rangeEndDateTimeValue.setValue(rangeEnd);
            } else {
                rangeEndDateTimeValue.setValue(selectedDate);
            }
            filter.addCriterion(1, rangeEndDateTimeValue);
        } else {
            String rangeStartFilterValue = filterValues.get(0);
            Date rangeStartDate = format.parse(rangeStartFilterValue);
            DateTimeValue rangeStartDateTimeValue = new DateTimeValue(rangeStartDate);
            filter.addCriterion(0, rangeStartDateTimeValue);

            String rangeEndFilterValue = filterValues.get(1);
            Date rangeEndDate = format.parse(rangeEndFilterValue);
            DateTimeValue rangeEndDateTimeValue = new DateTimeValue(rangeEndDate);
            filter.addCriterion(1, rangeEndDateTimeValue);
        }
    }

    private static Date prepareStartOfDayForSelectedDate(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private static Date prepareEndOfDayForSelectedDate(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    private static void prepareStringFilter(Filter filter, List<String> filterValues) {
        for (int i = 0; i < filterValues.size(); i++) {
            String filterValue = filterValues.get(i);
            if (!"".equalsIgnoreCase(filterValue)) {
                Value value = new StringValue("%" + filterValue + "%");
                filter.addCriterion(i, value);
            }

        }

    }

    private static void prepareLongFilter(Filter filter, List<String> filterValues) {
        for (int i = 0; i < filterValues.size(); i++) {
            String filterValue = filterValues.get(i);
            if (!"".equalsIgnoreCase(filterValue)) {
                Value value = new LongValue(Long.valueOf(filterValue));
                filter.addCriterion(i, value);
            }

        }

    }

    private static void prepareDecimalFilter(Filter filter, List<String> filterValues) {
        for (int i = 0; i < filterValues.size(); i++) {
            String filterValue = filterValues.get(i);
            if (!"".equalsIgnoreCase(filterValue)) {
                Value value = new DecimalValue(new BigDecimal(filterValue));
                filter.addCriterion(i, value);
            }

        }

    }

    private static void prepareBooleanFilter(Filter filter, List<String> filterValues) {
        for (int i = 0; i < filterValues.size(); i++) {
            String filterValue = filterValues.get(i);
            if (!"".equalsIgnoreCase(filterValue)) {
                Value value = new BooleanValue(filterValue == null || filterValue.isEmpty() ? null
                        : "true".equals(filterValue));
                filter.addCriterion(i, value);
            }

        }

    }

    public static void prepareDateTimeWithTimeZoneFilter(Filter filter, List<String> filterValues,
                                                          CollectionColumnProperties columnProperties) throws ParseException {
        String userTimeZoneId = GuiContext.get().getUserInfo().getTimeZoneId();
        String datePattern = (String) columnProperties.getProperty(CollectionColumnProperties.DATE_PATTERN);
        String timePattern = (String) columnProperties.getProperty(CollectionColumnProperties.TIME_PATTERN);
        String pattern = DateUtil.prepareDatePattern(datePattern, timePattern);
        DateFormat dateFormat = new SimpleDateFormat(pattern);
        TimeZone userTimeZone = TimeZone.getTimeZone(userTimeZoneId);
        dateFormat.setTimeZone(userTimeZone);
        String rawTimeZone = (String) columnProperties.getProperty(CollectionColumnProperties.TIME_ZONE_ID);
        TimeZone timeZone = prepareTimeZone(rawTimeZone);
        if (filterValues.size() == 1) {
            String filterValue = filterValues.get(0);
            Date selectedDate = dateFormat.parse(filterValue);
            Calendar calendar = Calendar.getInstance(userTimeZone);
            calendar.setTime(selectedDate);
            Date startRangeDate = prepareStartOfDayForSelectedDate(calendar);
            DateTimeWithTimeZone rangeStartDateTimeWithTimeZone = prepareDateTimeWithTimeZone(startRangeDate,
                    rawTimeZone, timeZone);
            Value selectedDateTimeWithTimeZoneValue = new DateTimeWithTimeZoneValue(rangeStartDateTimeWithTimeZone);
            filter.addCriterion(0, selectedDateTimeWithTimeZoneValue);
            if (timePattern == null) {
                Date rangeEnd = prepareEndOfDayForSelectedDate(calendar);
                DateTimeWithTimeZone rangeEndDateTimeWithTimeZone = prepareDateTimeWithTimeZone(rangeEnd, rawTimeZone,
                        timeZone);
                Value rangeEndDateTimeWithTimeZoneValue = new DateTimeWithTimeZoneValue(rangeEndDateTimeWithTimeZone);
                filter.addCriterion(1, rangeEndDateTimeWithTimeZoneValue);
            } else {
                DateTimeWithTimeZone rangeEndDateTimeWithTimeZone = prepareDateTimeWithTimeZone(selectedDate,
                        rawTimeZone, timeZone);
                Value rangeEndDateTimeWithTimeZoneValue = new DateTimeWithTimeZoneValue(rangeEndDateTimeWithTimeZone);
                filter.addCriterion(1, rangeEndDateTimeWithTimeZoneValue);
            }
        } else {
            String rangeStartFilterValue = filterValues.get(0);
            Date rangeStartDate = dateFormat.parse(rangeStartFilterValue);
            DateTimeWithTimeZone rangeStartDateTimeWithTimeZone = prepareDateTimeWithTimeZone(rangeStartDate,
                    rawTimeZone, timeZone);
            Value rangeStartDateTimeWithTimeZoneValue = new DateTimeWithTimeZoneValue(rangeStartDateTimeWithTimeZone);
            filter.addCriterion(0, rangeStartDateTimeWithTimeZoneValue);

            String rangeEndFilterValue = filterValues.get(1);
            Date rangeEndDate = dateFormat.parse(rangeEndFilterValue);
            DateTimeWithTimeZone rangeEndDateTimeWithTimeZone = prepareDateTimeWithTimeZone(rangeEndDate,
                    rawTimeZone, timeZone);
            Value rangeEndDateTimeWithTimeZoneValue = new DateTimeWithTimeZoneValue(rangeEndDateTimeWithTimeZone);
            filter.addCriterion(1, rangeEndDateTimeWithTimeZoneValue);
        }


    }

}
