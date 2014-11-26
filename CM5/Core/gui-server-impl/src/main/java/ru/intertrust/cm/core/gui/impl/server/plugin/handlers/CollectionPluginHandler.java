package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.SearchService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.gui.action.ToolBarConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionDisplayConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionViewConfig;
import ru.intertrust.cm.core.config.gui.form.widget.TableBrowserParams;
import ru.intertrust.cm.core.config.gui.navigation.*;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.gui.api.server.plugin.ActivePluginHandler;
import ru.intertrust.cm.core.gui.api.server.plugin.FilterBuilder;
import ru.intertrust.cm.core.gui.impl.server.plugin.DefaultImageMapperImpl;
import ru.intertrust.cm.core.gui.impl.server.util.*;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ToolbarContext;
import ru.intertrust.cm.core.gui.model.filters.ComplicatedFiltersParams;
import ru.intertrust.cm.core.gui.model.filters.InitialFiltersParams;
import ru.intertrust.cm.core.gui.model.form.widget.CollectionRowsResponse;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionPluginData;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRefreshRequest;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowsRequest;
import ru.intertrust.cm.core.gui.model.util.UserSettingsHelper;
import ru.intertrust.cm.core.gui.model.util.WidgetUtil;

import java.util.*;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */
@ComponentName(CollectionPluginHandler.COMPONENT_NAME)
public class CollectionPluginHandler extends ActivePluginHandler {
    static final String COMPONENT_NAME = "collection.plugin";

    @Autowired
    private CollectionsService collectionsService;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private SearchService searchService;

    @Autowired
    private DefaultImageMapperImpl defaultImageMapper;

    @Autowired
    private ActionConfigBuilder actionConfigBuilder;

    @Autowired
    private FilterBuilder filterBuilder;

    @Autowired
    private CurrentUserAccessor currentUserAccessor;

    public CollectionPluginData initialize(Dto param) {
        CollectionViewerConfig collectionViewerConfig = (CollectionViewerConfig) param;
        final String link = collectionViewerConfig.getHistoryValue(UserSettingsHelper.LINK_KEY);
        CollectionRefConfig collectionRefConfig = collectionViewerConfig.getCollectionRefConfig();
        String collectionName = collectionRefConfig.getName();
        final CollectionViewConfig collectionViewConfig =
                getViewForCurrentCollection(collectionViewerConfig, collectionName, link);
        final IdentifiableObject identifiableObject = PluginHandlerHelper.getCollectionSettingIdentifiableObject(
                link, collectionViewConfig.getName(), currentUserAccessor.getCurrentUser(),
                collectionsService);
        if (identifiableObject != null) {
            final CollectionViewerConfig storedConfig = PluginHandlerHelper.deserializeFromXml(CollectionViewerConfig.class,
                    identifiableObject.getString(UserSettingsHelper.DO_COLLECTION_VIEWER_FIELD_KEY));
            if (storedConfig != null) {
                collectionViewerConfig = storedConfig;
            }
        }
        boolean displayChosenValues = collectionViewerConfig.isDisplayChosenValues();
        CollectionPluginData pluginData = new CollectionPluginData();

        pluginData.setCollectionViewConfigName(collectionViewConfig.getName());
        collectionViewerConfig.getSearchAreaRefConfig();
        DefaultSortCriteriaConfig sortCriteriaConfig = collectionViewerConfig.getDefaultSortCriteriaConfig();
        InitialFiltersConfig initialFiltersConfig = collectionViewerConfig.getInitialFiltersConfig();
        LinkedHashMap<String, CollectionColumnProperties> columnPropertyMap =
                CollectionPluginHelper.getFieldColumnPropertiesMap(collectionViewConfig, sortCriteriaConfig, initialFiltersConfig);
        pluginData.setDomainObjectFieldPropertiesMap(columnPropertyMap);
        List<Filter> filters = new ArrayList<>();
        TableBrowserParams tableBrowserParams = collectionViewerConfig.getTableBrowserParams();
        prepareTableBrowserFilters(tableBrowserParams, filters);
        pluginData.setTableBrowserParams(tableBrowserParams);
        pluginData.setInitialFiltersConfig(initialFiltersConfig);
        Map<String, CollectionColumnProperties> filterNameColumnPropertiesMap =
                CollectionPluginHelper.getFilterNameColumnPropertiesMap(columnPropertyMap, initialFiltersConfig);
        InitialFiltersParams filtersParams = new InitialFiltersParams(filterNameColumnPropertiesMap);
        filterBuilder.prepareInitialFilters(initialFiltersConfig, filtersParams, filters);
        pluginData.setDefaultSortCriteriaConfig(sortCriteriaConfig);
        pluginData.setFilterPanelConfig(collectionViewerConfig.getFilterPanelConfig());
        CollectionDisplayConfig collectionDisplayConfig = collectionViewConfig.getCollectionDisplayConfig();
        SortOrder order = SortOrderBuilder.getInitSortOrder(sortCriteriaConfig, collectionDisplayConfig);

        CollectionPluginHelper.addFilterByText(collectionViewerConfig, filters);
        int initRowsNumber = collectionViewerConfig.getRowsChunk();
        pluginData.setRowsChunk(initRowsNumber);
        if (displayChosenValues && collectionViewerConfig.getHierarchicalFiltersConfig() != null) {
            if (filterBuilder.prepareExtraFilters(collectionViewerConfig.getHierarchicalFiltersConfig(), null, filters)) {
                pluginData.setHierarchicalFiltersConfig(collectionViewerConfig.getHierarchicalFiltersConfig());
            }
        }
        boolean isTableBrowserLimitation = tableBrowserParams != null && tableBrowserParams.isTooltipLimitation();
        initRowsNumber = isTableBrowserLimitation
                ? WidgetUtil.getLimit(tableBrowserParams.getSelectionFiltersConfig())
                : initRowsNumber;
        ArrayList<CollectionRowItem> items = getRows(collectionName, 0, initRowsNumber, filters, order, columnPropertyMap);
        pluginData.setItems(items);
        Collection<Id> selectedIds = tableBrowserParams == null ? new ArrayList<Id>() : tableBrowserParams.getIds();
        pluginData.setChosenIds(selectedIds);

        pluginData.setCollectionName(collectionName);
        if (collectionViewerConfig.getSearchAreaRefConfig() != null) {
            pluginData.setSearchArea(collectionViewerConfig.getSearchAreaRefConfig().getName());
        } else {
            pluginData.setSearchArea("");
        }

        pluginData.setToolbarContext(getToolbarContext(collectionViewerConfig));
        return pluginData;
    }

    private void prepareTableBrowserFilters(TableBrowserParams params, List<Filter> filters) {
        if (params == null) {
            return;
        }
        ComplicatedFiltersParams tbFiltersParams = (ComplicatedFiltersParams) params.getComplicatedFiltersParams();
        if (params.isMainWidgetContent()) { //main content
            filterBuilder.prepareIncludedIdsFilter(params.getIds(), filters);
            filterBuilder.prepareSelectionFilters(params.getSelectionFiltersConfig(), tbFiltersParams, filters);
        } else {
            if (!params.isDisplayChosenValues()) { //selector window without chosen values
                filterBuilder.prepareExcludedIdsFilter(params.getIds(), filters);
            }
            filterBuilder.prepareExtraFilters(params.getCollectionExtraFiltersConfig(), tbFiltersParams, filters);
        }
    }

    public CollectionPluginData getExtendedCollectionPluginData(String collectionName, final String link,
                                                                ArrayList<CollectionRowItem> items) {
        CollectionRefConfig refConfig = new CollectionRefConfig();
        refConfig.setName(collectionName);

        CollectionViewerConfig collectionViewerConfig = new CollectionViewerConfig();
        collectionViewerConfig.setCollectionRefConfig(refConfig);

        CollectionPluginData pluginData = new CollectionPluginData();
        final CollectionViewConfig collectionViewConfig =
                getViewForCurrentCollection(collectionViewerConfig, collectionName, link);

        final LinkedHashMap<String, CollectionColumnProperties> map =
                CollectionPluginHelper.getFieldColumnPropertiesMap(collectionViewConfig, null, null);
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

    private CollectionViewConfig getViewForCurrentCollection(CollectionViewerConfig collectionViewerConfig,
                                                             String collectionName, final String link) {
        final CollectionViewRefConfig collectionViewRefConfig = collectionViewerConfig.getCollectionViewRefConfig();
        final String viewName = collectionViewRefConfig == null ? null : collectionViewRefConfig.getName();

        return PluginHandlerHelper.findCollectionViewConfig(collectionName, viewName,
                currentUserAccessor.getCurrentUser(),
                link, configurationService, collectionsService);
    }


    public CollectionRowItem generateCollectionRowItem(final IdentifiableObject identifiableObject,
                                                       final Map<String, CollectionColumnProperties> columnPropertiesMap,
                                                       final Map<String, Map<Value, ImagePathValue>> fieldMappings) {
        CollectionRowItem item = new CollectionRowItem();
        LinkedHashMap<String, Value> row = CollectionPluginHelper.getRowValues(identifiableObject, columnPropertiesMap, fieldMappings);
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
        CollectionRowsRequest request = (CollectionRowsRequest) dto;
        CollectionRowsResponse collectionRowsResponse = new CollectionRowsResponse();
        TableBrowserParams tableBrowserParams = request.getTableBrowserParams();
        boolean isTableBrowserLimitation = tableBrowserParams != null && tableBrowserParams.isTooltipLimitation();
        collectionRowsResponse.setReinsertRows(isTableBrowserLimitation); //if table-browser has limit in the selections-filters, table should reinsert rows and not add more
        LinkedHashMap<String, CollectionColumnProperties> properties = request.getColumnProperties();
        int offset = isTableBrowserLimitation ? 0 : request.getOffset();
        int limit = isTableBrowserLimitation
                ? WidgetUtil.getLimit(request.getTableBrowserParams().getSelectionFiltersConfig()) : request.getLimit();
        Map<String, List<String>> filtersMap = request.getFiltersMap();
        List<Filter> filters = CollectionPluginHelper.prepareSearchFilters(filtersMap, properties);

        prepareTableBrowserFilters(tableBrowserParams, filters);
        filterBuilder.prepareExtraFilters(request.getHierarchicalFiltersConfig(), new ComplicatedFiltersParams(), filters);
        InitialFiltersConfig initialFiltersConfig = request.getInitialFiltersConfig();
        Set<String> userFilterNamesWithInputs = filtersMap == null ? null : filtersMap.keySet();
        List<String> excludedInitialFilterNames = CollectionPluginHelper.prepareExcludedInitialFilterNames(userFilterNamesWithInputs, properties);
        Map<String, CollectionColumnProperties> filterNameColumnPropertiesMap = CollectionPluginHelper.
                getFilterNameColumnPropertiesMap(properties, initialFiltersConfig);
        InitialFiltersParams filtersParams = new InitialFiltersParams(excludedInitialFilterNames, filterNameColumnPropertiesMap);
        filterBuilder.prepareInitialFilters(initialFiltersConfig, filtersParams, filters);

        ArrayList<CollectionRowItem> result = generateRowItems(request, properties, filters, offset, limit);

        collectionRowsResponse.setCollectionRows(result);

        return collectionRowsResponse;
    }

    public CollectionRowsResponse refreshCollection(Dto dto) {
        final CollectionRowsRequest request = ((CollectionRefreshRequest) dto).getCollectionRowsRequest();
        final Id id = ((CollectionRefreshRequest) dto).getId();
        LinkedHashMap<String, CollectionColumnProperties> properties = request.getColumnProperties();
        int offsetFromRequest = request.getOffset();
        int limitFromRequest = request.getLimit();
        int offset = 0;
        int limit = offsetFromRequest + limitFromRequest;
        Map<String, List<String>> filtersMap = request.getFiltersMap();
        List<Filter> filters = CollectionPluginHelper.prepareSearchFilters(filtersMap, properties);
        InitialFiltersConfig initialFiltersConfig = request.getInitialFiltersConfig();
        Set<String> userFilterNamesWithInputs = filtersMap == null ? null : filtersMap.keySet();
        List<String> excludedInitialFilterNames = CollectionPluginHelper.prepareExcludedInitialFilterNames(userFilterNamesWithInputs, properties);
        Map<String, CollectionColumnProperties> filterNameColumnPropertiesMap = CollectionPluginHelper.getFilterNameColumnPropertiesMap(properties, initialFiltersConfig);
        InitialFiltersParams initialFiltersParams = new InitialFiltersParams(excludedInitialFilterNames, filterNameColumnPropertiesMap);
        filterBuilder.prepareInitialFilters(initialFiltersConfig, initialFiltersParams, filters);
        Set<Id> includedIds = request.getIncludedIds();
        if (!includedIds.isEmpty()) {
            Filter includedIdsFilter = FilterBuilderUtil.prepareFilter(includedIds, FilterBuilderUtil.INCLUDED_IDS_FILTER);
            filters.add(includedIdsFilter);
        }
        ArrayList<CollectionRowItem> result = generateRowItems(request, properties, filters, offset, limit);
        if (CollectionPluginHelper.doesNotContainSelectedId(id, result)) {
            int additionalOffset = limit;
            List<CollectionRowItem> additionalItems = generateRowItems(request, properties, filters, additionalOffset, 200);
            result.addAll(additionalItems);
        }
        CollectionRowsResponse collectionRowsResponse = new CollectionRowsResponse();
        collectionRowsResponse.setCollectionRows(result);

        return collectionRowsResponse;

    }

    private ArrayList<CollectionRowItem> generateRowItems(CollectionRowsRequest request,
                                                          LinkedHashMap<String, CollectionColumnProperties> properties,
                                                          List<Filter> filters, int offset, int limit) {
        ArrayList<CollectionRowItem> list;
        String collectionName = request.getCollectionName();
        if (request.isSortable()) {
            list = getRows(collectionName, offset, limit, filters, CollectionPluginHelper.getSortOrder(request), properties);
        } else {
            if (request.getSimpleSearchQuery().length() > 0) {
                list = getSimpleSearchRows(collectionName, offset, limit, filters,
                        request.getSimpleSearchQuery(), request.getSearchArea(),
                        properties);
            } else {
                list = getRows(collectionName, offset, limit, filters, null, properties);

            }

        }
        return list;

    }

}
