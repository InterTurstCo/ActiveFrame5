package ru.intertrust.cm.core.gui.api.server;

import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.util.ThreadSafeDateFormat;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionColumnConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.InitialParamConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.ParamConfig;
import ru.intertrust.cm.core.config.gui.navigation.DefaultSortCriteriaConfig;
import ru.intertrust.cm.core.config.gui.navigation.InitialFilterConfig;
import ru.intertrust.cm.core.config.gui.navigation.InitialFiltersConfig;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;
import ru.intertrust.cm.core.gui.model.SortedMarker;
import ru.intertrust.cm.core.gui.model.util.GuiConstants;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Sergey.Okolot
 *         Created on 07.03.14 17:27.
 */
public final class GuiServerHelper {

    public static final TimeZone GMT_TIME_ZONE = TimeZone.getTimeZone("GMT");

    /**
     * Don't create instance of helper class.
     */
    private GuiServerHelper() {
    }

    public static Calendar timelessDateToCalendar(final TimelessDate timelessDate, final TimeZone timeZone) {
        final Calendar calendar = Calendar.getInstance(timeZone);
        calendar.set(Calendar.YEAR, timelessDate.getYear());
        calendar.set(Calendar.MONTH, timelessDate.getMonth());
        calendar.set(Calendar.DAY_OF_MONTH, timelessDate.getDayOfMonth());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    public static Calendar dateTimeWithTimezoneToCalendar(final DateTimeWithTimeZone dateTime) {
        final Calendar calendar =
                Calendar.getInstance(TimeZone.getTimeZone(dateTime.getTimeZoneContext().getTimeZoneId()));
        calendar.set(Calendar.YEAR, dateTime.getYear());
        calendar.set(Calendar.MONTH, dateTime.getMonth());
        calendar.set(Calendar.DAY_OF_MONTH, dateTime.getDayOfMonth());
        calendar.set(Calendar.HOUR_OF_DAY, dateTime.getHours());
        calendar.set(Calendar.MINUTE, dateTime.getMinutes());
        calendar.set(Calendar.SECOND, dateTime.getSeconds());
        calendar.set(Calendar.MILLISECOND, dateTime.getMilliseconds());
        return calendar;
    }

    public static CollectionColumnProperties collectionColumnConfigToProperties(CollectionColumnConfig config,
                                                                                DefaultSortCriteriaConfig sortCriteriaConfig, InitialFiltersConfig initialFiltersConfig) {
        CollectionColumnProperties properties = new CollectionColumnProperties();
        String sortedField = getSortedField(sortCriteriaConfig);
        String field = config.getField();
        String columnName = config.getName();
        String searchFilterName = config.getSearchFilter();
        properties.addProperty(CollectionColumnProperties.FIELD_NAME, field)
                .addProperty(CollectionColumnProperties.NAME_KEY, columnName)
                .addProperty(CollectionColumnProperties.TYPE_KEY, config.getType())
                .addProperty(CollectionColumnProperties.SEARCH_FILTER_KEY, searchFilterName)
                .addProperty(CollectionColumnProperties.DATE_PATTERN, config.getDatePattern())
                .addProperty(CollectionColumnProperties.TIME_PATTERN, config.getTimePattern())
                .addProperty(CollectionColumnProperties.TIME_ZONE_ID, config.getTimeZoneId())
                .addProperty(CollectionColumnProperties.WIDTH, config.getWidth())
                .addProperty(CollectionColumnProperties.MIN_WIDTH, config.getMinWidth())
                .addProperty(CollectionColumnProperties.MAX_WIDTH, config.getMaxWidth())
                .addProperty(CollectionColumnProperties.RESIZABLE, config.isResizable())
                .addProperty(CollectionColumnProperties.TEXT_BREAK_STYLE, config.getTextBreakStyle())
                .addProperty(CollectionColumnProperties.SORTABLE, config.isSortable())
                .addProperty(CollectionColumnProperties.DATE_RANGE, config.isDateRange())
                .addProperty(CollectionColumnProperties.HIDDEN, config.isHidden())
                .addProperty(CollectionColumnProperties.DRILL_DOWN_STYLE, config.getDrillDownStyle())
                .addProperty(CollectionColumnProperties.EXPANDABLE, config.isExpandable());
        if (field.equalsIgnoreCase(sortedField)) {
            properties.addProperty(
                    CollectionColumnProperties.SORTED_MARKER, getSortedMarker(sortCriteriaConfig));
        }
        if (initialFiltersConfig != null) {
            List<InitialFilterConfig> filterConfigs = initialFiltersConfig.getFilterConfigs();
            List<String> initialFilterValues = getInitialFilterValue(searchFilterName, filterConfigs, properties);
            properties.addProperty(CollectionColumnProperties.INITIAL_FILTER_VALUES, initialFilterValues);
        }
        properties.setAscSortCriteriaConfig(config.getAscSortCriteriaConfig());
        properties.setDescSortCriteriaConfig(config.getDescSortCriteriaConfig());
        properties.setImageMappingsConfig(config.getImageMappingsConfig());
        properties.setRendererConfig(config.getRendererConfig());
        properties.setActionRefConfig(config.getActionRefConfig());
        return properties;
    }

    private static List<InitialParamConfig> findFilterInitParams(String filterName, List<InitialFilterConfig> filterConfigs) {
        if (filterName == null || filterConfigs == null) {
            return null;
        }
        for (InitialFilterConfig filterConfig : filterConfigs) {
            if (filterName.equalsIgnoreCase(filterConfig.getName())) {
                return filterConfig.getParamConfigs();
            }
        }
        return null;
    }

    private static List<String> getInitialFilterValue(String filterName, List<InitialFilterConfig> filterConfigs,
                                                      CollectionColumnProperties properties) {
        List<InitialParamConfig> paramConfigs = findFilterInitParams(filterName, filterConfigs);
        List<String> initialFilterValues = getInitialFilterValueFromParamConfigs(paramConfigs, properties);
        return initialFilterValues;
    }

    private static List<String> getInitialFilterValueFromParamConfigs(List<InitialParamConfig> paramConfigs,
                                                                      CollectionColumnProperties properties) {
        if (paramConfigs == null || paramConfigs.isEmpty()) {
            return null;
        }
        List<String> initialFilterValues = new ArrayList<>();
        for (ParamConfig paramConfig : paramConfigs) {
            String paramValue = paramConfig.getValue();
            try {
                initialFilterValues.add(convertToUserFormat(paramValue, properties));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return initialFilterValues;
    }

    private static String convertToUserFormat(String value, CollectionColumnProperties properties) throws ParseException {
        String type = (String) properties.getProperty(CollectionColumnProperties.TYPE_KEY);
        FieldType fieldType = FieldType.forTypeName(type);

        switch (fieldType) {
            case TIMELESSDATE:
                Date timelessDate = ThreadSafeDateFormat.parse(value, GuiConstants.TIMELESS_DATE_FORMAT);
                DateFormat dateFormat = getUserDateFormatter(properties);
                return dateFormat != null ? dateFormat.format(timelessDate) : ThreadSafeDateFormat.format(timelessDate,"yyyy-MM-dd");
            case DATETIME:
            case DATETIMEWITHTIMEZONE:
                Date date = ThreadSafeDateFormat.parse(value, GuiConstants.DATE_TIME_FORMAT);
                dateFormat = getUserDateFormatter(properties);
                return dateFormat != null ? dateFormat.format(date) : ThreadSafeDateFormat.format(date,"yyyy-MM-dd HH:mm:ss");
            default:
                return value;

        }
    }
    private static DateFormat getUserDateFormatter(CollectionColumnProperties properties){
        String datePattern = (String) properties.getProperty(CollectionColumnProperties.DATE_PATTERN);
        String timePattern = (String) properties.getProperty(CollectionColumnProperties.TIME_PATTERN);
        return getDateFormat(datePattern, timePattern);

    }
    private static String getSortedField(DefaultSortCriteriaConfig sortCriteriaConfig) {
        if (sortCriteriaConfig == null) {
            return null;
        }
        String columnField = sortCriteriaConfig.getColumnField();
        return columnField;
    }

    private static SortedMarker getSortedMarker(DefaultSortCriteriaConfig sortCriteriaConfig) {
        SortCriterion.Order sortOrder = sortCriteriaConfig != null ? sortCriteriaConfig.getOrder() : SortCriterion.Order.ASCENDING;
        boolean sortedAscending = sortOrder.equals(SortCriterion.Order.ASCENDING);
        SortedMarker sortedMarker = new SortedMarker();
        sortedMarker.setAscending(sortedAscending);
        return sortedMarker;
    }
    public static DateFormat getDateFormat(String datePattern, String timePattern) {
        String formatDatePattern = prepareDatePattern(datePattern, timePattern);
        return formatDatePattern == null ? null : ThreadSafeDateFormat.getDateFormat(new Pair<String, Locale>(formatDatePattern, null), null);
    }

    public static String prepareDatePattern(String datePattern, String timePattern){
        if (datePattern == null) {
            return null;
        }
        if (timePattern == null) {
            return datePattern;
        }
        StringBuilder patternBuilder = new StringBuilder(datePattern);
        patternBuilder.append(" ");
        patternBuilder.append(timePattern);
        return patternBuilder.toString();
    }
}
