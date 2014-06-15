package ru.intertrust.cm.core.gui.api.server;

import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZone;
import ru.intertrust.cm.core.business.api.dto.SortCriterion;
import ru.intertrust.cm.core.business.api.dto.TimelessDate;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionColumnConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.AbstractFilterConfig;
import ru.intertrust.cm.core.config.gui.navigation.DefaultSortCriteriaConfig;
import ru.intertrust.cm.core.config.gui.navigation.InitialFilterConfig;
import ru.intertrust.cm.core.config.gui.navigation.InitialFiltersConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.ParamConfig;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;
import ru.intertrust.cm.core.gui.model.SortedMarker;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * @author Sergey.Okolot
 *         Created on 07.03.14 17:27.
 */
public final class GuiServerHelper {

    public static final TimeZone GMT_TIME_ZONE = TimeZone.getTimeZone("GMT");

    /**
     * Don't create instance of helper class.
     */
    private GuiServerHelper() {}

    public static Calendar timelessDateToCalendar(final TimelessDate timelessDate, final TimeZone timeZone) {
        final Calendar calendar = Calendar.getInstance(timeZone);
        calendar.set(Calendar.YEAR, timelessDate.getYear());
        calendar.set(Calendar.MONTH, timelessDate.getMonth());
        calendar.set(Calendar.DAY_OF_MONTH, timelessDate.getDayOfMonth());
        calendar.set(Calendar.HOUR, 0);
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
        calendar.set(Calendar.HOUR, dateTime.getHours());
        calendar.set(Calendar.MINUTE, dateTime.getMinutes());
        calendar.set(Calendar.SECOND, dateTime.getSeconds());
        calendar.set(Calendar.MILLISECOND, dateTime.getMilliseconds());
        return calendar;
    }

    public static CollectionColumnProperties collectionColumnConfigToProperties(final CollectionColumnConfig config,
                final DefaultSortCriteriaConfig sortCriteriaConfig, InitialFiltersConfig initialFiltersConfig) {
        final CollectionColumnProperties properties = new CollectionColumnProperties();
        final String sortedField = getSortedField(sortCriteriaConfig);
        final String field = config.getField();
        final String columnName = config.getName();
        String searchFilterName = config.getSearchFilter();
        properties.addProperty(CollectionColumnProperties.FIELD_NAME, field)
                .addProperty(CollectionColumnProperties.NAME_KEY, columnName)
                .addProperty(CollectionColumnProperties.TYPE_KEY, config.getType())
                .addProperty(CollectionColumnProperties.SEARCH_FILTER_KEY,searchFilterName)
                .addProperty(CollectionColumnProperties.PATTERN_KEY, config.getPattern())
                .addProperty(CollectionColumnProperties.TIME_ZONE_ID, config.getTimeZoneId())
                .addProperty(CollectionColumnProperties.MIN_WIDTH, config.getMinWidth())
                .addProperty(CollectionColumnProperties.MAX_WIDTH, config.getMaxWidth())
                .addProperty(CollectionColumnProperties.RESIZABLE, config.isResizable())
                .addProperty(CollectionColumnProperties.TEXT_BREAK_STYLE, config.getTextBreakStyle())
                .addProperty(CollectionColumnProperties.SORTABLE, config.isSortable());
        if (field.equalsIgnoreCase(sortedField)) {
            properties.addProperty(
                    CollectionColumnProperties.SORTED_MARKER, getSortedMarker(sortCriteriaConfig));
        }
        if(initialFiltersConfig != null){
            List<AbstractFilterConfig> abstractFilterConfigs = (List<AbstractFilterConfig>) initialFiltersConfig.getAbstractFilterConfigs();
            String initialFilterValue = getInitialFilterValue(searchFilterName, abstractFilterConfigs);
            properties.addProperty(CollectionColumnProperties.INITIAL_FILTER_VALUE, initialFilterValue);
        }
        properties.setAscSortCriteriaConfig(config.getAscSortCriteriaConfig());
        properties.setDescSortCriteriaConfig(config.getDescSortCriteriaConfig());
        properties.setImageMappingsConfig(config.getImageMappingsConfig());
        properties.setRendererConfig(config.getRendererConfig());
        return properties;
    }
    private static List<ParamConfig> findFilterInitParams(String filterName, List<AbstractFilterConfig> abstractFilterConfigs) {
         if (filterName == null || abstractFilterConfigs == null) {
             return null;
         }
        for (AbstractFilterConfig abstractFilterConfig : abstractFilterConfigs) {
              if (filterName.equalsIgnoreCase(abstractFilterConfig.getName())){
                  return abstractFilterConfig.getParamConfigs();
              }
        }
        return null;
    }
    private static String getInitialFilterValueFromParamConfigs(List<ParamConfig> paramConfigs) {
        if(paramConfigs == null || paramConfigs.isEmpty()) {
            return null;
        }
        int size = paramConfigs.size();
        ParamConfig paramConfig = paramConfigs.get(size - 1);
        return paramConfig.getValue();
    }
    private static String getInitialFilterValue(String filterName, List<AbstractFilterConfig> abstractFilterConfigs){
        List<ParamConfig> paramConfigs = findFilterInitParams(filterName, abstractFilterConfigs);
        String initialFilterValue = getInitialFilterValueFromParamConfigs(paramConfigs);
        return initialFilterValue;
    }

    private static String getSortedField(DefaultSortCriteriaConfig sortCriteriaConfig) {
        if (sortCriteriaConfig == null) {
            return null;
        }
        String columnField = sortCriteriaConfig.getColumnField();
        return columnField;
    }

    private static SortedMarker getSortedMarker(DefaultSortCriteriaConfig sortCriteriaConfig) {
        SortCriterion.Order sortOrder = sortCriteriaConfig.getOrder();
        boolean sortedAscending = sortOrder.equals(SortCriterion.Order.ASCENDING);
        SortedMarker sortedMarker = new SortedMarker();
        sortedMarker.setAscending(sortedAscending);
        return sortedMarker;
    }
}
