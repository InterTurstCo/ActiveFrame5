package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.SearchService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.gui.action.ToolBarConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionColumnConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionDisplayConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionViewConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SearchAreaRefConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;
import ru.intertrust.cm.core.config.gui.navigation.*;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.GuiServerHelper;
import ru.intertrust.cm.core.gui.api.server.plugin.ActivePluginHandler;
import ru.intertrust.cm.core.gui.api.server.plugin.FilterBuilder;
import ru.intertrust.cm.core.gui.impl.server.plugin.DefaultImageMapperImpl;
import ru.intertrust.cm.core.gui.impl.server.util.ActionConfigBuilder;
import ru.intertrust.cm.core.gui.impl.server.util.DateUtil;
import ru.intertrust.cm.core.gui.impl.server.util.FilterBuilderUtil;
import ru.intertrust.cm.core.gui.impl.server.util.SortOrderBuilder;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.action.ToolbarContext;
import ru.intertrust.cm.core.gui.model.form.widget.CollectionRowItemList;
import ru.intertrust.cm.core.gui.model.plugin.CollectionPluginData;
import ru.intertrust.cm.core.gui.model.plugin.CollectionRowItem;
import ru.intertrust.cm.core.gui.model.plugin.CollectionRowsRequest;
import ru.intertrust.cm.core.gui.model.util.StringUtil;
import ru.intertrust.cm.core.gui.model.util.UserSettingsHelper;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */
@ComponentName(CollectionPluginHandler.COMPONENT_NAME)
public class CollectionPluginHandler extends ActivePluginHandler {
    static final String COMPONENT_NAME = "collection.plugin";

    private static final int INIT_ROWS_NUMBER = 25;

    @Autowired
    CollectionsService collectionsService;

    @Autowired
    ConfigurationService configurationService;

    @Autowired
    SearchService searchService;

    @Autowired
    DefaultImageMapperImpl defaultImageMapper;

    @Autowired
    private ActionConfigBuilder actionConfigBuilder;

    @Autowired
    private FilterBuilder filterBuilder;

    public CollectionPluginData initialize(Dto param) {
        CollectionViewerConfig collectionViewerConfig = (CollectionViewerConfig) param;
        CollectionRefConfig collectionRefConfig = collectionViewerConfig.getCollectionRefConfig();
        String collectionName = collectionRefConfig.getName();
        boolean singleChoice = collectionViewerConfig.isSingleChoice();
        boolean displayChosenValues = collectionViewerConfig.isDisplayChosenValues();
        CollectionPluginData pluginData = new CollectionPluginData();
        pluginData.setSingleChoice(singleChoice);
        pluginData.setDisplayChosenValues(displayChosenValues);
        CollectionViewConfig collectionViewConfig = getViewForCurrentCollection(collectionViewerConfig, collectionName);
        pluginData.setCollectionViewConfigName(collectionViewConfig.getName());
        collectionViewerConfig.getSearchAreaRefConfig();
        DefaultSortCriteriaConfig sortCriteriaConfig = collectionViewerConfig.getDefaultSortCriteriaConfig();
        final Boolean historySortDirection = StringUtil.booleanFromString(
                (String) collectionViewerConfig.getHistoryValue(UserSettingsHelper.SORT_DIRECT_KEY), null);
        if (historySortDirection != null) {
            sortCriteriaConfig.setOrder(historySortDirection
                    ? CommonSortCriterionConfig.ASCENDING
                    : CommonSortCriterionConfig.DESCENDING);
        }
        final String historySortField = collectionViewerConfig.getHistoryValue(UserSettingsHelper.SORT_FIELD_KEY);
        if (historySortField != null) {
            sortCriteriaConfig.setColumnField(historySortField);
        }
        InitialFiltersConfig initialFiltersConfig = collectionViewerConfig.getInitialFiltersConfig();
        LinkedHashMap<String, CollectionColumnProperties> columnPropertyMap =
                getDomainObjectFieldPropertiesMap(collectionViewConfig, sortCriteriaConfig, initialFiltersConfig);
        pluginData.setDomainObjectFieldPropertiesMap(columnPropertyMap);
        List<Filter> filters = new ArrayList<>();
        String filterName = collectionViewerConfig.getFilterName();
        String filterValue = collectionViewerConfig.getFilterValue();
        if (filterName != null && filterValue.length() > 0) {
            Filter inputFilter = prepareInputTextFilter(filterName, filterValue);
            filters.add(inputFilter);
        }
        pluginData.setInitialFiltersConfig(initialFiltersConfig);
        filterBuilder.prepareInitialFilters(initialFiltersConfig, null, filters);
        pluginData.setDefaultSortCriteriaConfig(sortCriteriaConfig);
        pluginData.setFilterPanelConfig(collectionViewerConfig.getFilterPanelConfig());
        CollectionDisplayConfig collectionDisplayConfig = collectionViewConfig.getCollectionDisplayConfig();
        SortOrder order = SortOrderBuilder.getInitSortOrder(sortCriteriaConfig, collectionDisplayConfig);
        // todo не совсем верная логика. а в каком режиме обычная коллекция открывается? single choice? display chosen values?
        // todo: по-моему условие singleChoice && !displayChosenValues вполне говорит само за себя :) в следующем условии тоже
        filters = addFilterByText(collectionViewerConfig, filters);
        if ((singleChoice && !displayChosenValues) || (!singleChoice && !displayChosenValues)) {
            filters = addFilterExcludeIds(collectionViewerConfig, filters);
            ArrayList<CollectionRowItem> items =
                    getRows(collectionName, 0, INIT_ROWS_NUMBER, filters, order, columnPropertyMap);
            pluginData.setItems(items);
        }

        if ((singleChoice && displayChosenValues) || (!singleChoice && displayChosenValues)) {
            SelectionFiltersConfig selectionFiltersConfig = collectionViewerConfig.getSelectionFiltersConfig();
            filterBuilder.prepareSelectionFilters(selectionFiltersConfig, null, filters);
            ArrayList<CollectionRowItem> items = getRows(collectionName,
                    0, INIT_ROWS_NUMBER, filters, order, columnPropertyMap);
            pluginData.setChosenIds(collectionViewerConfig.getExcludedIds());
            pluginData.setItems(items);
        }

        pluginData.setCollectionName(collectionName);
        if (collectionViewerConfig.getSearchAreaRefConfig() != null) {
            pluginData.setSearchArea(collectionViewerConfig.getSearchAreaRefConfig().getName());
        } else {
            pluginData.setSearchArea("");
        }
        pluginData.setToolbarContext(getToolbarContext(collectionViewerConfig));
        return pluginData;
    }

    public CollectionPluginData getExtendedCollectionPluginData(String collectionName,
                                                                ArrayList<CollectionRowItem> items) {
        CollectionRefConfig refConfig = new CollectionRefConfig();
        refConfig.setName(collectionName);

        CollectionViewerConfig collectionViewerConfig = new CollectionViewerConfig();
        collectionViewerConfig.setCollectionRefConfig(refConfig);

        boolean singleChoice = true;
        boolean displayChosenValues = false;
        CollectionPluginData pluginData = new CollectionPluginData();
        pluginData.setSingleChoice(singleChoice);
        pluginData.setDisplayChosenValues(displayChosenValues);
        CollectionViewConfig collectionViewConfig = getViewForCurrentCollection(collectionViewerConfig, collectionName);

        final LinkedHashMap<String, CollectionColumnProperties> map =
                getDomainObjectFieldPropertiesMap(collectionViewConfig, null, null);
        pluginData.setDomainObjectFieldPropertiesMap(map);
        pluginData.setItems(items);
        pluginData.setCollectionName(collectionName);
        pluginData.setToolbarContext(getToolbarContext(collectionViewerConfig));
        return pluginData;
    }

    private ToolbarContext getToolbarContext(final CollectionViewerConfig viewerConfig) {
        final Map<String, Object> collectionParams = new HashMap<>();
        final ToolBarConfig toolbarConfig =
                viewerConfig.getToolBarConfig() == null ? new ToolBarConfig() : viewerConfig.getToolBarConfig();
        ToolBarConfig defaultToolbarConfig;
        if (toolbarConfig.isRendered() && toolbarConfig.isUseDefault()) {
            defaultToolbarConfig = actionService.getDefaultToolbarConfig(COMPONENT_NAME);
        } else {
            defaultToolbarConfig = null;
        }
        if (defaultToolbarConfig == null) {
            defaultToolbarConfig = new ToolBarConfig();
        }
        final ToolbarContext result = new ToolbarContext();
        actionConfigBuilder.appendConfigs(defaultToolbarConfig.getActions(), collectionParams);
        actionConfigBuilder.appendConfigs(toolbarConfig.getActions(), collectionParams);
        result.setContexts(actionConfigBuilder.getActionContexts(), ToolbarContext.FacetName.LEFT);
        actionConfigBuilder.clear();
        actionConfigBuilder.appendConfigs(defaultToolbarConfig.getRightFacetConfig().getActions(), collectionParams);
        actionConfigBuilder.appendConfigs(toolbarConfig.getRightFacetConfig().getActions(), collectionParams);
        result.setContexts(actionConfigBuilder.getActionContexts(), ToolbarContext.FacetName.RIGHT);
        return result;
    }

    private Collection<CollectionViewConfig> getCollectionOfViewConfigs() {
        Collection<CollectionViewConfig> viewConfigs = configurationService.getConfigs(CollectionViewConfig.class);
        return viewConfigs;

    }

    private List<String> prepareExcludedInitialFilterNames(Set<String> userFilterNamesWithInputs,
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

    private List<Filter> addFilterByText(CollectionViewerConfig collectionViewerConfig, List<Filter> filters) {

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

    private List<Filter> addFilterExcludeIds(CollectionViewerConfig collectionViewerConfig, List<Filter> filters) {

        List<Id> excludedIds = collectionViewerConfig.getExcludedIds();
        Set<Id> idsExcluded = new HashSet<Id>(excludedIds);
        Filter filterExcludeIds = FilterBuilderUtil.prepareFilter(idsExcluded, FilterBuilderUtil.EXCLUDED_IDS_FILTER);
        filters.add(filterExcludeIds);
        return filters;
    }

    private CollectionViewConfig getViewForCurrentCollection(CollectionViewerConfig collectionViewerConfig,
                                                             String collectionName) {
        CollectionViewRefConfig collectionViewRefConfig = collectionViewerConfig.getCollectionViewRefConfig();
        if (collectionViewRefConfig == null) {
            return findRequiredCollectionView(collectionName);
        }
        String viewName = collectionViewRefConfig.getName();
        return findRequiredCollectionViewByName(viewName);

    }

    private CollectionViewConfig findRequiredCollectionView(String collection) {
        // todo: very slow
        Collection<CollectionViewConfig> collectionViewConfigs = getCollectionOfViewConfigs();
        for (CollectionViewConfig collectionViewConfig : collectionViewConfigs) {
            boolean isDefault = collectionViewConfig.isDefault();
            if (collectionViewConfig.getCollection().equalsIgnoreCase(collection) && isDefault) {
                return collectionViewConfig;
            }
        }
        throw new GuiException("Couldn't find view for collection with name '" + collection + "'");
    }

    private CollectionViewConfig findRequiredCollectionViewByName(String viewName) {
        Collection<CollectionViewConfig> collectionViewConfigs = getCollectionOfViewConfigs();
        for (CollectionViewConfig collectionViewConfig : collectionViewConfigs) {

            if (collectionViewConfig.getName().equalsIgnoreCase(viewName)) {
                return collectionViewConfig;
            }
        }
        throw new GuiException("Couldn't find collection view with name '" + viewName + "'");
    }

    private LinkedHashMap<String, Value> getRowValues(final IdentifiableObject identifiableObject,
                                                      final Map<String, CollectionColumnProperties> columnPropertiesMap,
                                                      final Map<String, Map<Value, ImagePathValue>> fieldMappings) {
        LinkedHashMap<String, Value> values = new LinkedHashMap<String, Value>();
        Set<String> fields = fieldMappings.keySet();
        for (String field : fields) {
            Value value;
            if ("id".equalsIgnoreCase(field)) {
                value = new StringValue(identifiableObject.getId().toStringRepresentation());
            } else {
                value = identifiableObject.getValue(field.toLowerCase());
            }
            if (value != null && value.get() != null) {
                Calendar calendar;
                String timeZoneId;
                String datePattern =
                        (String) columnPropertiesMap.get(field).getProperty(CollectionColumnProperties.DATE_PATTERN);
                String timePattern =
                        (String) columnPropertiesMap.get(field).getProperty(CollectionColumnProperties.TIME_PATTERN);
                DateFormat dateFormat = DateUtil.getDateFormat(datePattern, timePattern);
                switch (value.getFieldType()) {
                    case DATETIMEWITHTIMEZONE:
                        final DateTimeWithTimeZone dateTimeWithTimeZone = (DateTimeWithTimeZone) value.get();
                        calendar = GuiServerHelper.dateTimeWithTimezoneToCalendar(dateTimeWithTimeZone);
                        dateFormat.setTimeZone(TimeZone.getTimeZone(
                                dateTimeWithTimeZone.getTimeZoneContext().getTimeZoneId()));
                        value = new StringValue(dateFormat.format(calendar.getTime()));
                        break;
                    case DATETIME:
                        timeZoneId = GuiContext.get().getUserInfo().getTimeZoneId();
                        final DateTimeValue dateTimeValue = (DateTimeValue) value;
                        dateFormat.setTimeZone(TimeZone.getTimeZone(timeZoneId));
                        value = new StringValue(dateFormat.format(dateTimeValue.get()));
                        break;
                    case TIMELESSDATE:
                        final TimelessDate timelessDate = (TimelessDate) value.get();
                        timeZoneId = GuiContext.get().getUserInfo().getTimeZoneId();
                        calendar = GuiServerHelper.timelessDateToCalendar(timelessDate, GuiServerHelper.GMT_TIME_ZONE);
                        dateFormat.setTimeZone(TimeZone.getTimeZone(timeZoneId));
                        value = new StringValue(dateFormat.format(calendar.getTime()));
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

    private LinkedHashMap<String, CollectionColumnProperties> getDomainObjectFieldPropertiesMap(
            final CollectionViewConfig collectionViewConfig, DefaultSortCriteriaConfig sortCriteriaConfig,
            InitialFiltersConfig initialFiltersConfig) {
        LinkedHashMap<String, CollectionColumnProperties> columnPropertiesMap = new LinkedHashMap<String, CollectionColumnProperties>();
        CollectionDisplayConfig collectionDisplay = collectionViewConfig.getCollectionDisplayConfig();
        if (collectionDisplay != null) {
            List<CollectionColumnConfig> columnConfigs = collectionDisplay.getColumnConfig();
            for (CollectionColumnConfig columnConfig : columnConfigs) {
                if (!columnConfig.isHidden()) {
                    final String field = columnConfig.getField();
                    final CollectionColumnProperties properties =
                            GuiServerHelper.collectionColumnConfigToProperties(columnConfig, sortCriteriaConfig, initialFiltersConfig);
                    columnPropertiesMap.put(field, properties);
                }
            }
            return columnPropertiesMap;

        } else throw new GuiException("Collection view config has no display tags configured ");
    }

    public CollectionRowItem generateCollectionRowItem(final IdentifiableObject identifiableObject,
                                                       final Map<String, CollectionColumnProperties> columnPropertiesMap,
                                                       final Map<String, Map<Value, ImagePathValue>> fieldMappings) {
        CollectionRowItem item = new CollectionRowItem();
        LinkedHashMap<String, Value> row = getRowValues(identifiableObject, columnPropertiesMap, fieldMappings);
        item.setId(identifiableObject.getId());
        item.setRow(row);
        return item;

    }

    public ArrayList<CollectionRowItem> getRows(String collectionName, int offset, int count, List<Filter> filters,
                                                SortOrder sortOrder, LinkedHashMap<String, CollectionColumnProperties> columnPropertiesMap) {

        ArrayList<CollectionRowItem> items = new ArrayList<CollectionRowItem>();
        IdentifiableObjectCollection collection = collectionsService.
                findCollection(collectionName, sortOrder, filters, offset, count);
        Map<String, Map<Value, ImagePathValue>> fieldMappings = defaultImageMapper.getImageMaps(columnPropertiesMap);
        for (IdentifiableObject identifiableObject : collection) {
            items.add(generateCollectionRowItem(identifiableObject, columnPropertiesMap, fieldMappings));

        }
        return items;
    }

    public ArrayList<CollectionRowItem> getSimpleSearchRows(String collectionName, int offset, int count,
                                                            List<Filter> filters, String simpleSearchQuery, String searchArea,
                                                            LinkedHashMap<String, CollectionColumnProperties> properties) {

        ArrayList<CollectionRowItem> items = new ArrayList<CollectionRowItem>();
        IdentifiableObjectCollection collection =
                searchService.search(simpleSearchQuery, searchArea, collectionName, 1000);
        Map<String, Map<Value, ImagePathValue>> fieldMappings = defaultImageMapper.getImageMaps(properties);
        for (IdentifiableObject identifiableObject : collection) {
            items.add(generateCollectionRowItem(identifiableObject, properties, fieldMappings));
        }
        return items;
    }

    public Dto generateCollectionRowItems(Dto dto) {
        // 21.01 12:50 (DB) -> if 21.01 selected -> it's a date between 21.01 00:00 and 22.01 00:00
        CollectionRowsRequest collectionRowsRequest = (CollectionRowsRequest) dto;
        LinkedHashMap<String, CollectionColumnProperties> properties = collectionRowsRequest.getColumnProperties();
        ArrayList<CollectionRowItem> list;
        final String collectionName = collectionRowsRequest.getCollectionName();

        final int offset = collectionRowsRequest.getOffset();
        final int limit = collectionRowsRequest.getLimit();
        //  final List<Filter> filters = transformDateFilters(collectionRowsRequest.getFilterList());
        Map<String, List<String>> filtersMap = collectionRowsRequest.getFiltersMap();
        List<Filter> filters = prepareSearchFilters(filtersMap, properties);
        InitialFiltersConfig initialFiltersConfig = collectionRowsRequest.getInitialFiltersConfig();
        Set<String> userFilterNamesWithInputs = filtersMap == null ? null : filtersMap.keySet();
        List<String> excludedInitialFilterNames = prepareExcludedInitialFilterNames(userFilterNamesWithInputs, properties);
        filterBuilder.prepareInitialFilters(initialFiltersConfig, excludedInitialFilterNames, filters);
        Set<Id> includedIds = collectionRowsRequest.getIncludedIds();
        if (!includedIds.isEmpty()) {
            Filter includedIdsFilter = FilterBuilderUtil.prepareFilter(includedIds, FilterBuilderUtil.INCLUDED_IDS_FILTER);
            filters.add(includedIdsFilter);
        }
        if (collectionRowsRequest.isSortable()) {
            list = getRows(collectionName, offset, limit, filters, getSortOrder(collectionRowsRequest), properties);
        } else {
            if (collectionRowsRequest.getSimpleSearchQuery().length() > 0) {
                list = getSimpleSearchRows(collectionName, offset, limit, filters,
                        collectionRowsRequest.getSimpleSearchQuery(), collectionRowsRequest.getSearchArea(),
                        properties);
            } else {
                list = getRows(collectionName, offset, limit, filters, null, properties);
                CollectionRowItemList collectionRowItemList = new CollectionRowItemList();
                collectionRowItemList.setCollectionRows(list);
            }

        }
        CollectionRowItemList collectionRowItemList = new CollectionRowItemList();
        collectionRowItemList.setCollectionRows(list);

        return collectionRowItemList;
    }

    private List<Filter> prepareSearchFilters(Map<String, List<String>> filtersMap, LinkedHashMap<String, CollectionColumnProperties> properties) {
        List<Filter> filters = new ArrayList<Filter>();
        if (filtersMap == null) {
            return filters;
        }
        Set<String> fieldNames = filtersMap.keySet();
        for (String fieldName : fieldNames) {
            List<String> filterValues = filtersMap.get(fieldName);
            if (filterValuesAreValid(filterValues)) {
                CollectionColumnProperties columnProperties = properties.get(fieldName);
                try {
                    Filter filter = FilterBuilderUtil.prepareSearchFilter(filterValues, columnProperties);
                    filters.add(filter);
                } catch (ParseException e) {
                    e.printStackTrace();  //for developers only
                }

            }

        }
        return filters;
    }

    private boolean filterValuesAreValid(List<String> filterValues) {
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

    private SortOrder getSortOrder(CollectionRowsRequest request) {
        SortCriteriaConfig sortCriteriaConfig = request.getSortCriteriaConfig();
        String fieldName = request.getSortedField();
        boolean ascend = request.isSortAscending();
        SortOrder sortOrder = SortOrderBuilder.getSortOrder(sortCriteriaConfig, fieldName, ascend);
        return sortOrder;

    }

    private Filter prepareInputTextFilter(String name, String text) {
        Filter textFilter = new Filter();
        textFilter.setFilter(name);
        textFilter.addCriterion(0, new StringValue(text + "%"));
        return textFilter;
    }
}
