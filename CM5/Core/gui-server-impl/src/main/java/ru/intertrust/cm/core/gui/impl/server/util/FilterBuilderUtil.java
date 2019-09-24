package ru.intertrust.cm.core.gui.impl.server.util;

import org.springframework.util.CollectionUtils;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.util.ThreadSafeDateFormat;
import ru.intertrust.cm.core.config.gui.form.widget.filter.AbstractFilterConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.InitialParamConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.ParamConfig;
import ru.intertrust.cm.core.config.gui.navigation.InitialFilterConfig;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;
import ru.intertrust.cm.core.gui.model.filters.InitialFiltersParams;
import ru.intertrust.cm.core.gui.model.util.GuiConstants;
import ru.intertrust.cm.core.gui.model.util.WidgetUtil;

import java.math.BigDecimal;
import java.text.ParseException;
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

    public static Filter prepareFilter(Collection<Id> ids, String type) {
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
     @Deprecated // will be private, use prepareFilter(InitialFilterConfig filterConfig, InitialFiltersParams initialFiltersParams, Filter filter)
     public static Filter prepareColumnFilter(List<String> filterValues, CollectionColumnProperties columnProperties, Filter filter){
        if (columnProperties != null) {
            String fieldType = (String) columnProperties.getProperty(CollectionColumnProperties.TYPE_KEY);
            String rawTimeZone = (String) columnProperties.getProperty(CollectionColumnProperties.TIME_ZONE_ID);
            prepareFilter(filterValues, fieldType, rawTimeZone, filter);

        }
        return filter;
    }

    public static Filter prepareFilter(InitialFilterConfig filterConfig, InitialFiltersParams initialFiltersParams, Filter filter){
        Filter result = initFilter(filterConfig.getName(), filter);
        if(shouldPrepareFilterFromConfig(filterConfig)){
            prepareFilterFromConfig(filterConfig, result);
        }else {
            List<String> filterValues = prepareFilterStringValues(filterConfig);
            String filterName = filterConfig.getName();
            CollectionColumnProperties columnProperties = initialFiltersParams.getFilterNameColumnPropertiesMap().get(filterName);
            prepareColumnFilter(filterValues, columnProperties, result);
       }
        return result;

    }
    private static void prepareFilterFromConfig(InitialFilterConfig filterConfig, Filter filter){
        if(WidgetUtil.isNotEmpty(filterConfig.getParamConfigs())){
        InitialParamConfig paramConfig = filterConfig.getParamConfigs().get(0);
        List<String> filterValues = prepareFilterStringValues(filterConfig);
        prepareFilter(filterValues, paramConfig, filter);
        }
    }

    public static void prepareFilter(List<String> filterValues, InitialParamConfig paramConfig, Filter filter){
            String fieldType = paramConfig.getType();
            String rawTimeZone = paramConfig.getTimeZoneId();
            prepareFilter(filterValues, fieldType, rawTimeZone, filter);

    }

    private static void prepareFilter(List<String> filterValues, String fieldType, String rawTimeZone, Filter filter){

            switch (fieldType) {
                case TIMELESS_DATE_TYPE:
                    try {
                        prepareTimelessDateFilter(filter, filterValues, rawTimeZone);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    break;
                case DATE_TIME_TYPE:
                    try {
                        prepareDateTimeFilter(filter, filterValues, rawTimeZone);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    break;
                case DATE_TIME_WITH_TIME_ZONE_TYPE:
                    try {
                        prepareDateTimeWithTimeZoneFilter(filter, filterValues, rawTimeZone);
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

    }

    /**
     * @link Deperecated
     *  use Filter prepareColumnFilter(List<String> filterValues, CollectionColumnProperties columnProperties, Filter filter)
     * @param filterValues
     * @param columnProperties
     * @return
     */
    @Deprecated
    public static Filter prepareSearchFilter(List<String> filterValues, CollectionColumnProperties columnProperties) {
        return prepareColumnFilter(filterValues, columnProperties, null);
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
                                                 String rawTimeZone) throws ParseException {

        String rangeStartFilterValue = filterValues.get(0);
        Date rangeStartDate = ThreadSafeDateFormat.parse(rangeStartFilterValue, GuiConstants.TIMELESS_DATE_FORMAT);

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
            Date rangeEndDate = ThreadSafeDateFormat.parse(rangeEndFilterValue, GuiConstants.TIMELESS_DATE_FORMAT);
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
                                              String rawTimeZone) throws ParseException {
        TimeZone timeZone = prepareTimeZone(rawTimeZone);
        if (filterValues.size() == 1) {
            String filterValue = filterValues.get(0);
            Date selectedDate = ThreadSafeDateFormat.parse(filterValue, GuiConstants.DATE_TIME_FORMAT, timeZone);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(selectedDate);
            Date rangeStart = prepareStartOfDayForSelectedDate(calendar);
            DateTimeValue rangeStartDateTimeValue = new DateTimeValue(rangeStart);
            filter.addCriterion(0, rangeStartDateTimeValue);
            DateTimeValue rangeEndDateTimeValue;
            if (isUserWithoutTimePattern(calendar)) {
                Date rangeEnd = prepareEndOfDayForSelectedDate(calendar);
                rangeEndDateTimeValue = new DateTimeValue(rangeEnd);
            } else {
                rangeEndDateTimeValue = new DateTimeValue(selectedDate);
            }
            filter.addCriterion(1, rangeEndDateTimeValue);
        } else {
            String rangeStartFilterValue = filterValues.get(0);
            Date rangeStartDate = ThreadSafeDateFormat.parse(rangeStartFilterValue, GuiConstants.DATE_TIME_FORMAT, timeZone);
            DateTimeValue rangeStartDateTimeValue = new DateTimeValue(rangeStartDate);
            filter.addCriterion(0, rangeStartDateTimeValue);

            String rangeEndFilterValue = filterValues.get(1);
            Date rangeEndDate = ThreadSafeDateFormat.parse(rangeEndFilterValue, GuiConstants.DATE_TIME_FORMAT, timeZone);
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
                                                         String rawTimeZone) throws ParseException {
        String userTimeZoneId = GuiContext.get().getUserInfo().getTimeZoneId();
        TimeZone userTimeZone = TimeZone.getTimeZone(userTimeZoneId);
        TimeZone timeZone = prepareTimeZone(rawTimeZone);
        if (filterValues.size() == 1) {
            String filterValue = filterValues.get(0);
            Date selectedDate = ThreadSafeDateFormat.parse(filterValue, GuiConstants.DATE_TIME_FORMAT, userTimeZone);
            Calendar calendar = Calendar.getInstance(userTimeZone);
            calendar.setTime(selectedDate);
            Date startRangeDate = prepareStartOfDayForSelectedDate(calendar);
            DateTimeWithTimeZone rangeStartDateTimeWithTimeZone = prepareDateTimeWithTimeZone(startRangeDate,
                    rawTimeZone, timeZone);
            Value selectedDateTimeWithTimeZoneValue = new DateTimeWithTimeZoneValue(rangeStartDateTimeWithTimeZone);
            filter.addCriterion(0, selectedDateTimeWithTimeZoneValue);
            if (isUserWithoutTimePattern(calendar)) {
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
            Date rangeStartDate = ThreadSafeDateFormat.parse(rangeStartFilterValue, GuiConstants.DATE_TIME_FORMAT, userTimeZone);
            DateTimeWithTimeZone rangeStartDateTimeWithTimeZone = prepareDateTimeWithTimeZone(rangeStartDate,
                    rawTimeZone, timeZone);
            Value rangeStartDateTimeWithTimeZoneValue = new DateTimeWithTimeZoneValue(rangeStartDateTimeWithTimeZone);
            filter.addCriterion(0, rangeStartDateTimeWithTimeZoneValue);

            String rangeEndFilterValue = filterValues.get(1);
            Date rangeEndDate = ThreadSafeDateFormat.parse(rangeEndFilterValue, GuiConstants.DATE_TIME_FORMAT, userTimeZone);
            DateTimeWithTimeZone rangeEndDateTimeWithTimeZone = prepareDateTimeWithTimeZone(rangeEndDate,
                    rawTimeZone, timeZone);
            Value rangeEndDateTimeWithTimeZoneValue = new DateTimeWithTimeZoneValue(rangeEndDateTimeWithTimeZone);
            filter.addCriterion(1, rangeEndDateTimeWithTimeZoneValue);
        }


    }
    public static Filter initFilter(String filterName, Filter filter) {
        Filter result = null;
        if (filter == null) {
            result = new Filter();
            result.setFilter(filterName);
        } else {
            result = filter;
        }
        return result;

    }
    private static boolean isUserWithoutTimePattern(Calendar calendar){
        return calendar.get(Calendar.HOUR_OF_DAY) == 0
                && calendar.get(Calendar.MINUTE) == 0
                && calendar.get(Calendar.SECOND) == 0;
    }

    private static List<String> prepareFilterStringValues(AbstractFilterConfig abstractFilterConfig) {
        List<ParamConfig> paramConfigs = abstractFilterConfig.getParamConfigs();
        List<String> result = new ArrayList<>(paramConfigs.size());
        for (ParamConfig paramConfig : paramConfigs) {
            result.add(paramConfig.getValue());
        }
        return result;

    }
    private static boolean shouldPrepareFilterFromConfig(InitialFilterConfig initialFilterConfig){
        boolean result = true;
        List<InitialParamConfig> paramConfigs = initialFilterConfig.getParamConfigs();

        if(ru.intertrust.cm.core.gui.model.util.WidgetUtil.isEmpty(paramConfigs)){
            result = false;
        } else {
        for (InitialParamConfig paramConfig : paramConfigs) {
            result = paramConfig.getType() != null;
        }
        }
        return result;
    }

    /**
     * Возвращает объект фильтра из списка по его имени.<br>
     * Для фильтров 'idsExcluded' и 'idsIncluded' берется совпадение по началу имени.<br>
     * Для остальных возвращает только в случае полного совпадения с учетом регистра.
     *
     * @param filterName имя фильтра для поиска
     * @param filtersList       список всех фильтров
     * @return объект {@link ru.intertrust.cm.core.business.api.dto.Filter фильтра}
     */
    public static Filter getFilterByName(String filterName, List<? extends Filter> filtersList) {
        if (!CollectionUtils.isEmpty(filtersList)) {

            if (filterName.startsWith(INCLUDED_IDS_FILTER)) {
                for (Filter filter : filtersList) {
                    if (filter instanceof IdsIncludedFilter) {
                        return filter;
                    }
                }
            } else if (filterName.startsWith(EXCLUDED_IDS_FILTER)) {
                for (Filter filter : filtersList) {
                    if (filter instanceof IdsExcludedFilter) {
                        return filter;
                    }
                }
            } else {
                for (Filter filter : filtersList) {
                    if (filterName.equals(filter.getFilter())) {
                        return filter;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Убирает из строки все символы '%'.
     *
     * @param string строка для обработки.
     * @return результирующую строку с убранными символами '%'
     */
    public static String cutPercentsCharacters(String string) {
        final String stringWithNoPercentCharacters = string.replaceAll("%", "");
        return stringWithNoPercentCharacters;
    }

}
