package ru.intertrust.cm.core.gui.impl.server.util;

import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.gui.collection.view.ChildCollectionViewerConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionColumnConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionDisplayConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionViewConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SearchAreaRefConfig;
import ru.intertrust.cm.core.config.gui.form.widget.TableBrowserParams;
import ru.intertrust.cm.core.config.gui.navigation.*;
import ru.intertrust.cm.core.config.localization.LocalizationKeys;
import ru.intertrust.cm.core.config.localization.MessageResourceProvider;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.GuiServerHelper;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.filters.ComplexFiltersParams;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowsRequest;

import java.text.DateFormat;
import java.util.*;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 23.08.2014
 *         Time: 13:17
 */
public class CollectionPluginHelper {

    public static LinkedHashMap<String, CollectionColumnProperties> getFieldColumnPropertiesMap(
            final CollectionViewConfig collectionViewConfig, DefaultSortCriteriaConfig sortCriteriaConfig,
            InitialFiltersConfig initialFiltersConfig, String locale) {
        LinkedHashMap<String, CollectionColumnProperties> columnPropertiesMap = new LinkedHashMap<String, CollectionColumnProperties>();
        CollectionDisplayConfig collectionDisplay = collectionViewConfig.getCollectionDisplayConfig();
        if (collectionDisplay != null) {
            List<CollectionColumnConfig> columnConfigs = collectionDisplay.getColumnConfig();
            for (CollectionColumnConfig columnConfig : columnConfigs) {
                final String field = columnConfig.getField();
                final CollectionColumnProperties properties =
                        GuiServerHelper.collectionColumnConfigToProperties(columnConfig, sortCriteriaConfig, initialFiltersConfig);
                List<ChildCollectionViewerConfig> childViewerConfigs = columnConfig.getChildCollectionViewerConfigList();
                if (childViewerConfigs != null && !childViewerConfigs.isEmpty()) {
                    properties.addProperty(CollectionColumnProperties.CHILD_COLLECTIONS_CONFIG, childViewerConfigs);
                }
                columnPropertiesMap.put(field, properties);
            }
            return columnPropertiesMap;

        } else throw new GuiException(MessageResourceProvider.getMessage(LocalizationKeys.GUI_EXCEPTION_COLLECTION_VIEW,
                "Collection view config has no display tags configured", locale));
    }

    public static Map<String, CollectionColumnProperties> getFilterNameColumnPropertiesMap(
            Map<String, CollectionColumnProperties> fieldColumnPropertiesMap,
            InitialFiltersConfig initialFiltersConfig) {
        if (initialFiltersConfig == null) {
            return Collections.emptyMap();
        }
        Map<String, CollectionColumnProperties> result = new LinkedHashMap<>();
        Collection<CollectionColumnProperties> columnPropertiesList = fieldColumnPropertiesMap.values();
        List<InitialFilterConfig> filterConfigs = initialFiltersConfig.getFilterConfigs();
        for (CollectionColumnProperties columnProperties : columnPropertiesList) {
            for (InitialFilterConfig filterConfig : filterConfigs) {
                String filterName = filterConfig.getName();
                String nameOfColumnFilter = (String) columnProperties.getProperty(CollectionColumnProperties.SEARCH_FILTER_KEY);
                if (filterName.equalsIgnoreCase(nameOfColumnFilter)) {
                    result.put(filterName, columnProperties);
                }
            }


        }
        return result;
    }


    public static boolean doesNotContainSelectedId(Id id, List<CollectionRowItem> items) {
        boolean result = true;
        for (CollectionRowItem item : items) {
            if (item.getId().equals(id)) {
                result = false;
            }
        }
        return result;
    }

    public static List<Filter> prepareSearchFilters(Map<String, List<String>> filtersMap, LinkedHashMap<String, CollectionColumnProperties> properties) {
        List<Filter> filters = new ArrayList<Filter>();
        if (filtersMap == null) {
            return filters;
        }
        Set<String> fieldNames = filtersMap.keySet();
        for (String fieldName : fieldNames) {
            List<String> filterValues = filtersMap.get(fieldName);
            if (filterValuesAreValid(filterValues)) {
                CollectionColumnProperties columnProperties = properties.get(fieldName);
                Filter filter = FilterBuilderUtil.prepareColumnFilter(filterValues, columnProperties, null);
                filters.add(filter);

            }

        }
        return filters;
    }

    public static boolean filterValuesAreValid(List<String> filterValues) {
        if (filterValues == null || filterValues.isEmpty()) {
            return false;
        }

        for (String filterValue : filterValues) {
            if (filterValue == null || filterValue.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static SortOrder getSortOrder(CollectionRowsRequest request) {
        SortCriteriaConfig sortCriteriaConfig = request.getSortCriteriaConfig();
        String fieldName = request.getSortedField();
        boolean ascend = request.isSortAscending();
        SortOrder sortOrder = SortOrderBuilder.getSortOrder(sortCriteriaConfig, fieldName, ascend);
        return sortOrder;

    }

    private static Filter prepareInputTextFilter(String name, String text) {
        Filter textFilter = new Filter();
        textFilter.setFilter(name);
        textFilter.addCriterion(0, new StringValue(text + "%"));
        return textFilter;
    }

    public static List<Filter> addFilterByText(CollectionViewerConfig collectionViewerConfig, List<Filter> filters) {

        SearchAreaRefConfig searchAreaRefConfig = collectionViewerConfig.getSearchAreaRefConfig();
        if (searchAreaRefConfig == null) {
            return filters;
        }
        String name = searchAreaRefConfig.getName();
        String text = searchAreaRefConfig.getValue();
        if (text == null || text.trim().isEmpty()) {
            return filters;
        }
        Filter filterByText = prepareInputTextFilter(name, text);
        filters.add(filterByText);

        return filters;
    }

    public static List<Filter> prepareFilterExcludeIds(TableBrowserParams tableBrowserParams, List<Filter> filters) {
       if(tableBrowserParams == null) {
           return filters;
       }
        Collection<Id> excludedIds = tableBrowserParams.getIds();
        Filter filterExcludeIds = FilterBuilderUtil.prepareFilter(excludedIds, FilterBuilderUtil.EXCLUDED_IDS_FILTER);
        filters.add(filterExcludeIds);
        return filters;
    }

    public static List<String> prepareExcludedInitialFilterNames(Set<String> userFilterNamesWithInputs,
                                                                 Map<String, CollectionColumnProperties> columnPropertiesMap) {
        List<String> filterNames = new ArrayList<>();
        if (userFilterNamesWithInputs == null) {
            return filterNames;
        }
        for (String fieldName : userFilterNamesWithInputs) {
            CollectionColumnProperties columnProperties = columnPropertiesMap.get(fieldName);
            String filterName = (String) columnProperties.getProperty(CollectionColumnProperties.SEARCH_FILTER_KEY);
            filterNames.add(filterName);
        }
        return filterNames;

    }

    public static LinkedHashMap<String, Value> getRowValues(final IdentifiableObject identifiableObject,
                                                            final Map<String, CollectionColumnProperties> columnPropertiesMap,
                                                            final Map<String, Map<Value, ImagePathValue>> fieldMappings) {
        LinkedHashMap<String, Value> values = new LinkedHashMap<>();
        for (String field : fieldMappings.keySet()) {
            Value value = identifiableObject.getValue(Case.toLower(field));

            if (value != null && value.get() != null) {
                switch (value.getFieldType()) {
                    case REFERENCE:
                        value = referenceToStringValue(value);
                        break;
                    case DATETIMEWITHTIMEZONE:
                        value = dateTimeWithTimeZoneToStringValue(value, getDateFormatter(field, columnPropertiesMap));
                        break;
                    case DATETIME:
                        value = dateTimeToStringValue(value, getDateFormatter(field, columnPropertiesMap));
                        break;
                    case TIMELESSDATE:
                        value = timelessDateToStringValue(value, getDateFormatter(field, columnPropertiesMap));
                }
            }
            Map<Value, ImagePathValue> imagePathValueMap = fieldMappings.get(field);
            if (imagePathValueMap != null && !imagePathValueMap.isEmpty()) {
                value = imagePathValueMap.get(value);
            }
            values.put(field, value);

        }
        return values;

    }

    private static StringValue referenceToStringValue(Value value) {
        return new StringValue(((Id) value.get()).toStringRepresentation());
    }

    private static StringValue dateTimeWithTimeZoneToStringValue(Value value, DateFormat formatter) {
        final DateTimeWithTimeZone dateTimeWithTimeZone = (DateTimeWithTimeZone) value.get();
        final Date date = GuiServerHelper.dateTimeWithTimezoneToCalendar(dateTimeWithTimeZone).getTime();
        final String timeZoneId = dateTimeWithTimeZone.getTimeZoneContext().getTimeZoneId();
        return toStringValue(date, timeZoneId, formatter);
    }

    private static StringValue dateTimeToStringValue(Value value, DateFormat formatter) {
        final Date date = ((DateTimeValue) value).get();
        final String timeZoneId = GuiContext.get().getUserInfo().getTimeZoneId();
        return toStringValue(date, timeZoneId, formatter);
    }

    private static StringValue timelessDateToStringValue(Value value, DateFormat formatter) {
        final Date date = GuiServerHelper.timelessDateToCalendar((TimelessDate) value.get(), GuiServerHelper.GMT_TIME_ZONE).getTime();
        final String timeZoneId = GuiContext.get().getUserInfo().getTimeZoneId();
        return toStringValue(date, timeZoneId, formatter);
    }

    private static StringValue toStringValue(Date date, String timeZoneId, DateFormat dateFormat) {
        if (timeZoneId != null) {
            dateFormat.setTimeZone(TimeZone.getTimeZone(timeZoneId));
        }
        return new StringValue(dateFormat.format(date));
    }

    private static DateFormat getDateFormatter(String field, Map<String, CollectionColumnProperties> columnPropertiesMap) {
        final CollectionColumnProperties columnProperties = columnPropertiesMap.get(field);
        final String datePattern = (String) columnProperties.getProperty(CollectionColumnProperties.DATE_PATTERN);
        final String timePattern = (String) columnProperties.getProperty(CollectionColumnProperties.TIME_PATTERN);
        return DateUtil.getDateFormat(datePattern, timePattern);
    }
    /*
     * use FilterBuilder  boolean prepareExtraFilters(CollectionExtraFiltersConfig config, ComplexFiltersParams params, List<Filter> filters)
     */
    @Deprecated
    public static void prepareTableBrowserFilter(TableBrowserParams tableBrowserParams, List<Filter> filters) {
        if (tableBrowserParams == null) {
            return;
        }
        ComplexFiltersParams bundle = (ComplexFiltersParams) tableBrowserParams.getComplexFiltersParams();
        String filterName = bundle.getInputFilterName();
        String filterValue = bundle.getInputFilterValue();
        if (filterName != null && filterValue.length() > 0) {
            Filter result = CollectionPluginHelper.prepareInputTextFilter(filterName, filterValue);
            filters.add(result);
        }

    }

    public static boolean isExpandable(CollectionViewConfig collectionViewConfig){
        boolean result = false;
        List<CollectionColumnConfig> columnConfigs = collectionViewConfig.getCollectionDisplayConfig().getColumnConfig();
        for (CollectionColumnConfig columnConfig : columnConfigs) {
            if(columnConfig.isExpandable()){
                result = true;
                break;
            }
        }
        return result;
    }
}
