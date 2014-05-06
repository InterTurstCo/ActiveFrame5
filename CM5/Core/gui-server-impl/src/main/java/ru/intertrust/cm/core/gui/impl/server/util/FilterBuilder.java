package ru.intertrust.cm.core.gui.impl.server.util;

import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static ru.intertrust.cm.core.gui.impl.server.util.DateUtil.prepareDateTimeWithTimeZone;
import static ru.intertrust.cm.core.gui.impl.server.util.DateUtil.prepareTimeZone;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 16.01.14
 *         Time: 13:15
 */
public class FilterBuilder {
    public static final String EXCLUDED_IDS_FILTER = "idsExcluded";
    public static final String INCLUDED_IDS_FILTER = "idsIncluded";
    private static final String TIMELESS_DATE_TYPE = "timelessDate";
    private static final String DATE_TIME_TYPE = "datetime";
    private static final String DATE_TIME_WITH_TIME_ZONE_TYPE = "dateTimeWithTimeZone";

    public static Filter prepareFilter(Set<Id> ids, String type) {
        List<ReferenceValue> idsCriterion = new ArrayList<>();
        for (Id id : ids) {
            idsCriterion.add(new ReferenceValue(id));
        }
        Filter filter = createRequiredFilter(idsCriterion, type);
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

    public static Filter prepareReferenceFilter(Id parentId, String filterByParentName) {
        Filter referenceFilter = new Filter();
        referenceFilter.setFilter(filterByParentName);
        referenceFilter.addCriterion(0, new ReferenceValue(parentId));
        return referenceFilter;

    }

    public static Filter prepareSearchFilter(String filterValue, CollectionColumnProperties columnProperties)
            throws ParseException {
        Filter filter = new Filter();
        String filterName = (String) columnProperties.getProperty(CollectionColumnProperties.SEARCH_FILTER_KEY);
        filter.setFilter(filterName);
        String fieldType = (String) columnProperties.getProperty(CollectionColumnProperties.TYPE_KEY);
        switch (fieldType) {
            case TIMELESS_DATE_TYPE:
                prepareTimelessDateFilter(filter, filterValue, columnProperties);
                break;
            case DATE_TIME_TYPE:
                prepareDateTimeFilter(filter, filterValue, columnProperties);
                break;
            case DATE_TIME_WITH_TIME_ZONE_TYPE:
                prepareDateTimeWithTimeZoneFilter(filter, filterValue, columnProperties);
                break;
            default:
                prepareTextFilter(filter, filterValue);
                break;
        }

        return filter;
    }

    private static void prepareTimelessDateFilter(Filter filter, String filterValue,
                                                  CollectionColumnProperties columnProperties) throws ParseException {

        String datePattern = (String) columnProperties.getProperty(CollectionColumnProperties.PATTERN_KEY);
        DateFormat format = new SimpleDateFormat(datePattern);
        Date selectedDate = format.parse(filterValue);
        String rawTimeZone = (String) columnProperties.getProperty(CollectionColumnProperties.TIME_ZONE_ID);
        TimeZone timeZone = prepareTimeZone(rawTimeZone);
        Calendar selectedCalendar = Calendar.getInstance(timeZone);
        selectedCalendar.setTime(selectedDate);
        TimelessDate timelessDateSelected = new TimelessDate(selectedCalendar.get(Calendar.YEAR),
                selectedCalendar.get(Calendar.MONTH),
                selectedCalendar.get(Calendar.DAY_OF_MONTH));
        Value selectedTimeValue = new TimelessDateValue(timelessDateSelected);
        filter.addCriterion(0, selectedTimeValue);
        Calendar currentCalendar = Calendar.getInstance(timeZone);
        TimelessDate currentTimelessDate = new TimelessDate(currentCalendar.get(Calendar.YEAR),
                currentCalendar.get(Calendar.MONTH),
                currentCalendar.get(Calendar.DAY_OF_MONTH));
        Value currentTimeValue = new TimelessDateValue(currentTimelessDate);
        filter.addCriterion(1, currentTimeValue);
    }

    private static void prepareDateTimeFilter(Filter filter, String filterValue,
                                              CollectionColumnProperties columnProperties) throws ParseException {
        String datePattern = (String) columnProperties.getProperty(CollectionColumnProperties.PATTERN_KEY);
        DateFormat format = new SimpleDateFormat(datePattern);
        String rawTimeZone = (String) columnProperties.getProperty(CollectionColumnProperties.TIME_ZONE_ID);
        TimeZone timeZone = prepareTimeZone(rawTimeZone);
        format.setTimeZone(timeZone);
        DateTimeValue selectedDateTimeValue = new DateTimeValue();
        Date selectedDate = format.parse(filterValue);
        selectedDateTimeValue.setValue(selectedDate);
        filter.addCriterion(0, selectedDateTimeValue);

        Calendar currentCalendar = Calendar.getInstance(timeZone);
        Date currentDate = currentCalendar.getTime();
        DateTimeValue currentDateTimeValue = new DateTimeValue(currentDate);
        filter.addCriterion(1, currentDateTimeValue);

    }

    private static void prepareTextFilter(Filter filter, String filterValue) {
        Value value = new StringValue("%" + filterValue + "%");
        filter.addCriterion(0, value);
    }

    private static void prepareDateTimeWithTimeZoneFilter(Filter filter, String filterValue,
                                                          CollectionColumnProperties columnProperties) throws ParseException {
        String userTimeZoneId = GuiContext.get().getUserInfo().getTimeZoneId();
        String datePattern = (String) columnProperties.getProperty(CollectionColumnProperties.PATTERN_KEY);
        DateFormat dateFormat = new SimpleDateFormat(datePattern);
        TimeZone userTimeZone = TimeZone.getTimeZone(userTimeZoneId);
        dateFormat.setTimeZone(userTimeZone);
        Date selectedDate = dateFormat.parse(filterValue);
        String rawTimeZone = (String) columnProperties.getProperty(CollectionColumnProperties.TIME_ZONE_ID);
        TimeZone timeZone = prepareTimeZone(rawTimeZone);
        DateTimeWithTimeZone selectedDateTimeWithTimeZone = prepareDateTimeWithTimeZone(selectedDate, rawTimeZone, timeZone);
        Value selectedDateTimeWithTimeZoneValue = new DateTimeWithTimeZoneValue(selectedDateTimeWithTimeZone);
        filter.addCriterion(0, selectedDateTimeWithTimeZoneValue);
        Date currentDate = Calendar.getInstance(userTimeZone).getTime();
        DateTimeWithTimeZone currentDateTimeWithTimeZone = prepareDateTimeWithTimeZone(currentDate, rawTimeZone, timeZone);
        Value currentDateTimeWithTimeZoneValue = new DateTimeWithTimeZoneValue(currentDateTimeWithTimeZone);
        filter.addCriterion(0, currentDateTimeWithTimeZoneValue);
    }

}
