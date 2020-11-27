package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.SearchService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.action.ToolBarConfig;
import ru.intertrust.cm.core.config.gui.collection.view.ChildCollectionViewerConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionColumnConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionDisplayConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionViewConfig;
import ru.intertrust.cm.core.config.gui.form.widget.ExpandableObjectConfig;
import ru.intertrust.cm.core.config.gui.form.widget.TableBrowserParams;
import ru.intertrust.cm.core.config.gui.form.widget.filter.InitialParamConfig;
import ru.intertrust.cm.core.config.gui.navigation.*;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.gui.api.server.DomainObjectMapping;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.GuiServerHelper;
import ru.intertrust.cm.core.gui.api.server.plugin.ActivePluginHandler;
import ru.intertrust.cm.core.gui.api.server.plugin.DefaultImageMapper;
import ru.intertrust.cm.core.gui.api.server.plugin.FilterBuilder;
import ru.intertrust.cm.core.gui.api.server.plugin.SortOrderHelper;
import ru.intertrust.cm.core.gui.impl.server.util.ActionConfigBuilder;
import ru.intertrust.cm.core.gui.impl.server.util.CollectionPluginHelper;
import ru.intertrust.cm.core.gui.impl.server.util.PluginHandlerHelper;
import ru.intertrust.cm.core.gui.impl.server.util.WidgetConstants;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ToolbarContext;
import ru.intertrust.cm.core.gui.model.filters.ComplexFiltersParams;
import ru.intertrust.cm.core.gui.model.filters.InitialFiltersParams;
import ru.intertrust.cm.core.gui.model.form.widget.CollectionRowsResponse;
import ru.intertrust.cm.core.gui.model.plugin.collection.*;
import ru.intertrust.cm.core.gui.model.util.UserSettingsHelper;

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
    private ConfigurationExplorer configurationService;

    @Autowired
    private SearchService searchService;

    @Autowired
    private DefaultImageMapper defaultImageMapper;

    @Autowired
    private ActionConfigBuilder actionConfigBuilder;

    @Autowired
    private FilterBuilder filterBuilder;

    @Autowired
    private CurrentUserAccessor currentUserAccessor;

    @Autowired
    private SortOrderHelper sortOrderHelper;

    @Autowired
    private CrudService crudService;

    @Autowired
    private DomainObjectMapping domainObjectMapping;

    private boolean expandable; //dummy for testing


    public CollectionPluginData initialize(Dto param) {
        CollectionViewerConfig collectionViewerConfig = (CollectionViewerConfig) param;
        final String link = collectionViewerConfig.getHistoryValue(UserSettingsHelper.LINK_KEY);
        CollectionRefConfig collectionRefConfig = collectionViewerConfig.getCollectionRefConfig();
        String collectionName = collectionRefConfig.getName();
        final CollectionViewConfig collectionViewConfig =
                getViewForCurrentCollection(collectionViewerConfig, collectionName, link);
        boolean expandable = CollectionPluginHelper.isExpandable(collectionViewConfig);
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
        pluginData.setRowsSelectionConfig(collectionViewerConfig.getRowsSelectionConfig());
        collectionViewerConfig.getSearchAreaRefConfig();
        DefaultSortCriteriaConfig sortCriteriaConfig = collectionViewerConfig.getDefaultSortCriteriaConfig();
        InitialFiltersConfig initialFiltersConfig = collectionViewerConfig.getInitialFiltersConfig();
        LinkedHashMap<String, CollectionColumnProperties> columnPropertyMap =
                CollectionPluginHelper.getFieldColumnPropertiesMap(collectionViewConfig, sortCriteriaConfig,
                        initialFiltersConfig, GuiContext.getUserLocale());
        pluginData.setDomainObjectFieldPropertiesMap(columnPropertyMap);

        for (String key : pluginData.getDomainObjectFieldPropertiesMap().keySet()) {
            if (pluginData.getDomainObjectFieldPropertiesMap().get(key).getActionRefConfig() != null) {
                ActionConfig actionConfig = actionConfigBuilder.resolveActionReference(pluginData.getDomainObjectFieldPropertiesMap().get(key).getActionRefConfig());

                pluginData.getDomainObjectFieldPropertiesMap().get(key).setActionContext(actionConfigBuilder.getContextForConfig(actionConfig,new HashMap<String, Object>()));
            }
        }

        List<Filter> filters = new ArrayList<>();
        TableBrowserParams tableBrowserParams = collectionViewerConfig.getTableBrowserParams();
        prepareTableBrowserFilters(tableBrowserParams, filters);
        pluginData.setTableBrowserParams(tableBrowserParams);
        pluginData.setInitialFiltersConfig(initialFiltersConfig);
        Map<String, CollectionColumnProperties> filterNameColumnPropertiesMap =
                CollectionPluginHelper.getFilterNameColumnPropertiesMap(columnPropertyMap, initialFiltersConfig);
        pluginData.setHasConfiguredFilters(hasConfiguredFilters(columnPropertyMap));
        pluginData.setHasColumnButtons(tableBrowserParams == null ? true : tableBrowserParams.hasColumnButtons());
        InitialFiltersParams filtersParams = new InitialFiltersParams(filterNameColumnPropertiesMap);
        filterBuilder.prepareInitialFilters(initialFiltersConfig, filtersParams, filters);
        pluginData.setDefaultSortCriteriaConfig(sortCriteriaConfig);
        pluginData.setFilterPanelConfig(collectionViewerConfig.getFilterPanelConfig());
        CollectionDisplayConfig collectionDisplayConfig = collectionViewConfig.getCollectionDisplayConfig();
        SortOrder order = sortOrderHelper.buildSortOrder(collectionName, sortCriteriaConfig, collectionDisplayConfig);

        CollectionPluginHelper.addFilterByText(collectionViewerConfig, filters);
        int initRowsNumber = collectionViewerConfig.getRowsChunk() >= 0 ? collectionViewerConfig.getRowsChunk() : WidgetConstants.UNBOUNDED_LIMIT;
        pluginData.setRowsChunk(initRowsNumber);
        if (displayChosenValues && collectionViewerConfig.getHierarchicalFiltersConfig() != null) {
            if (filterBuilder.prepareExtraFilters(collectionViewerConfig.getHierarchicalFiltersConfig(), null, filters)) {
                pluginData.setHierarchicalFiltersConfig(collectionViewerConfig.getHierarchicalFiltersConfig());
            }
        }

        List<String> expandableTypes;
        if (collectionViewerConfig.getChildCollectionConfig() != null) {
            final List<ExpandableObjectConfig> expandableObjects = collectionViewerConfig.getChildCollectionConfig().getExpandableObjectsConfig().getExpandableObjects();
            expandableTypes = new ArrayList<>(expandableObjects.size());
            for (ExpandableObjectConfig expandableObjectConfig : expandableObjects) {
                expandableTypes.add(expandableObjectConfig.getObjectName());
            }
        } else {
            expandableTypes = Collections.emptyList();
        }

        ArrayList<CollectionRowItem> items = getRows(collectionName, 0, initRowsNumber, filters, order, columnPropertyMap, expandableTypes);
        pluginData.setItems(items);
        Collection<Id> selectedIds = tableBrowserParams == null ? new ArrayList<Id>() : tableBrowserParams.getIds();
        pluginData.setChosenIds(selectedIds);

        pluginData.setCollectionName(collectionName);
        if (collectionViewerConfig.getSearchAreaRefConfig() != null) {
            pluginData.setSearchArea(collectionViewerConfig.getSearchAreaRefConfig().getName());
        } else {
            pluginData.setSearchArea("");
        }
        pluginData.setEmbedded(collectionViewerConfig.isEmbedded());
        pluginData.setToolbarContext(getToolbarContext(collectionViewerConfig));
        pluginData.setExpandable(expandable);
        return pluginData;
    }

    private void prepareTableBrowserFilters(TableBrowserParams params, List<Filter> filters) {
        if (params == null) {
            return;
        }
        ComplexFiltersParams tbFiltersParams = (ComplexFiltersParams) params.getComplexFiltersParams();
        if (params.isDisplayOnlySelectedIds()) { //main content not editable
            filterBuilder.prepareIncludedIdsFilter(params.getIds(), filters);
            filterBuilder.prepareSelectionFilters(params.getSelectionFiltersConfig(), tbFiltersParams, filters);
        } else {
            if (!params.isDisplayChosenValues()) { //selector window without chosen values
                filterBuilder.prepareExcludedIdsFilter(params.getIds(), filters);
            }
            filterBuilder.prepareExtraFilters(params.getCollectionExtraFiltersConfig(), tbFiltersParams, filters);
        }
    }

    public ExtendedSearchCollectionPluginData getExtendedCollectionPluginData(String collectionName, final String link,
                                                                ArrayList<CollectionRowItem> items) {
        CollectionRefConfig refConfig = new CollectionRefConfig();
        refConfig.setName(collectionName);

        CollectionViewerConfig collectionViewerConfig = new CollectionViewerConfig();
        collectionViewerConfig.setCollectionRefConfig(refConfig);

        ExtendedSearchCollectionPluginData pluginData = new ExtendedSearchCollectionPluginData();
        final CollectionViewConfig collectionViewConfig =
                getViewForCurrentCollection(collectionViewerConfig, collectionName, link);

        final LinkedHashMap<String, CollectionColumnProperties> map =
                CollectionPluginHelper.getFieldColumnPropertiesMap(collectionViewConfig, null, null, GuiContext.getUserLocale());
        pluginData.setDomainObjectFieldPropertiesMap(map);
        pluginData.setItems(items);
        pluginData.setCollectionName(collectionName);

        // build SearchResultPlugin data
        pluginData.setToolbarContext(getExtendedSearchToolbarContext(collectionViewerConfig));
        return pluginData;
    }

    private ToolbarContext getExtendedSearchToolbarContext(final CollectionViewerConfig viewerConfig){
        final Map<String, Object> collectionParams = new HashMap<>();
        final ToolBarConfig toolbarConfig =new ToolBarConfig();
        final ToolbarContext result = new ToolbarContext();
        actionConfigBuilder.appendConfigs(actionService.getDefaultToolbarConfig("search.result.toolbar",  GuiContext.getUserLocale()).getActions(), collectionParams);
        result.setContexts(actionConfigBuilder.getActionContexts(), ToolbarContext.FacetName.LEFT);
        return result;
    }

    private ToolbarContext getToolbarContext(final CollectionViewerConfig viewerConfig) {
        final Map<String, Object> collectionParams = new HashMap<>();
        final ToolBarConfig toolbarConfig =
                viewerConfig.getToolBarConfig() == null ? new ToolBarConfig() : viewerConfig.getToolBarConfig();
        ToolBarConfig defaultToolbarConfig;
        if (toolbarConfig.isRendered() && toolbarConfig.isUseDefault()) {
            defaultToolbarConfig = actionService.getDefaultToolbarConfig(COMPONENT_NAME, GuiContext.getUserLocale());
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
                link, configurationService, collectionsService, GuiContext.getUserLocale());
    }


    public CollectionRowItem generateCollectionRowItem(final IdentifiableObject identifiableObject,
                                                       final Map<String, CollectionColumnProperties> columnPropertiesMap,
                                                       final Map<String, Map<Value, ImagePathValue>> fieldMappings,
                                                       final boolean typeIsExpandable) {
        CollectionRowItem item = new CollectionRowItem();
        item.setExpandable(expandable); //stub for poc
        expandable = !expandable;
        LinkedHashMap<String, Value> row = CollectionPluginHelper.getRowValues(identifiableObject, columnPropertiesMap, fieldMappings);
        item.setId(identifiableObject.getId());
        item.setRow(row);
        item.setRowType(CollectionRowItem.RowType.DATA);
        item.setHaveChild(typeIsExpandable);

        setChildCollectionColumnData(item, identifiableObject, columnPropertiesMap);

        return item;
    }

    /**
     * Установка данных для колонок с дочерними коллекциями (см. {@link ru.intertrust.cm.core.config.gui.collection.view.ChildCollectionViewerConfig}),<br>
     * если они (колонки иерархии) имеются.
     *
     * @param item                объект данных строки коллекции
     * @param identifiableObject  Id записи данных
     * @param columnPropertiesMap карта свойств колонок
     */
    private void setChildCollectionColumnData(CollectionRowItem item, IdentifiableObject identifiableObject, Map<String, CollectionColumnProperties> columnPropertiesMap) {
        for (Map.Entry<String, CollectionColumnProperties> entry : columnPropertiesMap.entrySet()) {

            final CollectionColumnProperties collectionColumnProperties = entry.getValue();
            List<ChildCollectionViewerConfig> childCollectionsConfigs =
                    (List<ChildCollectionViewerConfig>) collectionColumnProperties.getProperty(CollectionColumnProperties.CHILD_COLLECTIONS_CONFIG);

            // производим действия только при наличии колонок дочерних коллекций
            if (childCollectionsConfigs != null) {
                final ChildCollectionViewerConfig childCollectionViewerConfig = childCollectionsConfigs.get(0);

                // параметры конфигурации колонки
                final boolean hideArrowIfEmpty = childCollectionViewerConfig.getHideArrowIfEmpty();
                final boolean showChildsCount = childCollectionViewerConfig.getShowChildsCount();

                // проверим не посчитано ли уже количество дочерних элементов в коллекции (отдельной колонкой с COUNT)
                final String field = entry.getKey();
                final Long calculatedChildsCount = getCalculatedChildsCount(item, field);
                boolean isChildsCountAlreadyCalculated = (calculatedChildsCount != null);

                final ChildCollectionColumnData childCollectionColumnData = new ChildCollectionColumnData();
                childCollectionColumnData.setHasChildsCountAlreadyCalculated(isChildsCountAlreadyCalculated);

                // производим действия, если установлен хотя бы один флаг в конфигурации дочерней коллекции, иначе в этом нет надобности
                if (showChildsCount) {
                    // устанавливаем количество дочерних элементов : либо уже вычисленное ранее, если нет, то вычисляем его предварительно
                    final long childCollectionItemsCount = isChildsCountAlreadyCalculated ?
                            calculatedChildsCount : calculateChildCollectionItemsCount(identifiableObject, childCollectionViewerConfig);

                    childCollectionColumnData.setChildCollectionItemsCount((int) childCollectionItemsCount);
                    childCollectionColumnData.setHasCollectionAnyChild(childCollectionItemsCount > 0);

                    // выполняется в случае, если флаг показа количества == false, а скрытия стрелки в пустых дочерних коллекциях == true
                    // потому что в противном случае количество либо уже посчитано (и вопрос с наличием/отсутствием дочерних элементов отпадает автоматически),
                    // либо это вообще не нужно когда оба флага не установлены
                } else if (hideArrowIfEmpty) {
                    // устанавливаем флаг наличия дочерних элементов : если их количество уже было посчитано ранее, то определяем по нему,
                    // если нет - делаем запрос проверки, что дочерняя коллекция не пустая
                    final boolean hasCollectionAnyChild = isChildsCountAlreadyCalculated ?
                            (calculatedChildsCount > 0) : hasCollectionAnyChild(identifiableObject, childCollectionViewerConfig);
                    childCollectionColumnData.setHasCollectionAnyChild(hasCollectionAnyChild);
                }
                item.putChildCollectionColumnData(field, childCollectionColumnData);
            }
        }
    }

    /**
     * Возвращает значение количества посчитанных дочерних элементов:<br>
     * берется значение из колонки, если она имеет числовой тип и не пустая.<br>
     *
     * @param item             объект данных строки коллекции
     * @param childColumnField имя поля колонки дочерней коллекции
     * @return значение количества посчитанных дочерних элементов;<br>
     * null - если тип значения не числовой, либо значение отсутствует
     */
    private Long getCalculatedChildsCount(CollectionRowItem item, String childColumnField) {
        final HashMap<String, Value> columnValuesMap = item.getRow();
        final Value childColumnValue = columnValuesMap.get(childColumnField);

        final Object valueObj = childColumnValue.get();
        if (valueObj != null) {

            if (childColumnValue instanceof LongValue) {
                return (Long) valueObj;
            }
        }
        return null;
    }

    /**
     * Подсчитывает количество дочерних элементов коллекции
     *
     * @param identifiableObject          Id фильтрации
     * @param childCollectionViewerConfig объект конфигурации дочерней коллекции
     * @return количество элементов дочерней коллекции
     */
    private int calculateChildCollectionItemsCount(IdentifiableObject identifiableObject, ChildCollectionViewerConfig childCollectionViewerConfig) {
        final CollectionViewerConfig collectionViewerConfig = childCollectionViewerConfig.getCollectionViewerConfig();
        final CollectionRefConfig collectionRefConfig = collectionViewerConfig.getCollectionRefConfig();

        final Id filterParamId = identifiableObject.getId();
        final String filterName = childCollectionViewerConfig.getFilter();
        final String collectionName = collectionRefConfig.getName();

        List<Filter> filtersList = new ArrayList<>();
        final ReferenceValue filterRefValue = new ReferenceValue(filterParamId);
        final Filter filter = Filter.create(filterName, 0, filterRefValue);
        filtersList.add(filter);

        final int childCollectionCount = collectionsService.findCollectionCount(collectionName, filtersList);
        return childCollectionCount;
    }

    /**
     * Определяет есть ли элементы у дочерней коллекции (не пустая ли она)
     *
     * @param identifiableObject          Id фильтрации
     * @param childCollectionViewerConfig объект конфигурации дочерней коллекции
     * @return true - дочерние элементы есть;<br>
     * false - их нет (количество == 0)
     */
    private boolean hasCollectionAnyChild(IdentifiableObject identifiableObject, ChildCollectionViewerConfig childCollectionViewerConfig) {
        final CollectionViewerConfig collectionViewerConfig = childCollectionViewerConfig.getCollectionViewerConfig();
        final CollectionRefConfig collectionRefConfig = collectionViewerConfig.getCollectionRefConfig();

        final Id filterParamId = identifiableObject.getId();
        final String filterName = childCollectionViewerConfig.getFilter();
        final String collectionName = collectionRefConfig.getName();

        List<Filter> filtersList = new ArrayList<>();
        final ReferenceValue filterRefValue = new ReferenceValue(filterParamId);
        final Filter filter = Filter.create(filterName, 0, filterRefValue);
        filtersList.add(filter);

        final boolean isCollectionEmpty = collectionsService.isCollectionEmpty(collectionName, filtersList);
        return !isCollectionEmpty;
    }

    public ArrayList<CollectionRowItem> getRows(String collectionName, int offset, int count, List<Filter> filters,
                                                SortOrder sortOrder, LinkedHashMap<String, CollectionColumnProperties> columnPropertiesMap,
                                                List<String> expandableTypes) {

        ArrayList<CollectionRowItem> items = new ArrayList<CollectionRowItem>();
        IdentifiableObjectCollection collection = collectionsService.findCollection(collectionName, sortOrder, filters, offset, count);
        Map<String, Map<Value, ImagePathValue>> fieldMappings = defaultImageMapper.getImageMaps(columnPropertiesMap);
        for (IdentifiableObject identifiableObject : collection) {
            String typeName = getTypeName(identifiableObject.getId());
            boolean typeIsExpandable = expandableTypes.contains(typeName);
            items.add(generateCollectionRowItem(identifiableObject, columnPropertiesMap, fieldMappings, typeIsExpandable));

        }
        return items;
    }

    private String getTypeName(Id domainObjectId){
        String typeName = domainObjectMapping.getTypeName(domainObjectId);
        if (typeName == null){
            typeName = crudService.getDomainObjectType(domainObjectId);
        }
        return typeName;
    }

    public ArrayList<CollectionRowItem> getSimpleSearchRows(String collectionName, int offset, int count,
                                                            List<Filter> filters, String simpleSearchQuery, String searchArea,
                                                            LinkedHashMap<String, CollectionColumnProperties> properties,
                                                            List<String> expandableTypes) {

        ArrayList<CollectionRowItem> items = new ArrayList<CollectionRowItem>();
        IdentifiableObjectCollection collection =
                searchService.search(simpleSearchQuery, searchArea, collectionName, 1000);
        Map<String, Map<Value, ImagePathValue>> fieldMappings = defaultImageMapper.getImageMaps(properties);
        for (IdentifiableObject identifiableObject : collection) {
            Boolean typeIsExpandable = expandableTypes.contains(crudService.getDomainObjectType(identifiableObject.getId()));
            items.add(generateCollectionRowItem(identifiableObject, properties, fieldMappings, typeIsExpandable));
        }
        return items;
    }

    public Dto generateCollectionRowItems(Dto dto) {
        // 21.01 12:50 (DB) -> if 21.01 selected -> it's a date between 21.01 00:00 and 22.01 00:00
        CollectionRowsRequest request = (CollectionRowsRequest) dto;
        CollectionRowsResponse collectionRowsResponse = new CollectionRowsResponse();
        TableBrowserParams tableBrowserParams = request.getTableBrowserParams();

        LinkedHashMap<String, CollectionColumnProperties> properties = request.getColumnProperties();
        int offset = request.getOffset();
        int limit = request.getLimit();
        List<Filter> filters = new ArrayList<>();
        prepareTableBrowserFilters(tableBrowserParams, filters);
        filterBuilder.prepareExtraFilters(request.getHierarchicalFiltersConfig(), new ComplexFiltersParams(), filters);
        InitialFiltersConfig initialFiltersConfig = request.getInitialFiltersConfig();

        Map<String, CollectionColumnProperties> filterNameColumnPropertiesMap = CollectionPluginHelper.
                getFilterNameColumnPropertiesMap(properties, initialFiltersConfig);
        InitialFiltersParams filtersParams = new InitialFiltersParams(filterNameColumnPropertiesMap);
        filterBuilder.prepareInitialFilters(initialFiltersConfig, filtersParams, filters);

        ArrayList<CollectionRowItem> result = generateRowItems(request, properties, filters, offset, limit, request.getExpandableTypes());

        collectionRowsResponse.setCollectionRows(result);

        if(request.getOffset()==0 && request.getLimit()==0 && request.getIncludedIds()!=null){
            for(Id objectId : request.getIncludedIds()){
               for( Iterator<CollectionRowItem> iterator = collectionRowsResponse.getCollectionRows().iterator();iterator.hasNext();){
                   CollectionRowItem newItem = iterator.next();
                   if(!newItem.getId().equals(objectId)){
                       iterator.remove();
                   }
               }
            }
        }

        return collectionRowsResponse;
    }

    public CollectionRowsResponse refreshCollection(Dto dto) {
        final CollectionRowsRequest request = ((CollectionRefreshRequest) dto).getCollectionRowsRequest();
        if (request.getExpandableTypes() == null) {
            request.setExpandableTypes(new ArrayList<String>());
        }
        LinkedHashMap<String, CollectionColumnProperties> properties = request.getColumnProperties();
        int offsetFromRequest = request.getOffset();
        int limitFromRequest = request.getLimit();
        int offset = 0;
        int limit = offsetFromRequest + limitFromRequest;
        List<Filter> filters = new ArrayList<Filter>();
        InitialFiltersConfig initialFiltersConfig = request.getInitialFiltersConfig();
        Map<String, CollectionColumnProperties> filterNameColumnPropertiesMap = CollectionPluginHelper.getFilterNameColumnPropertiesMap(properties, initialFiltersConfig);
        InitialFiltersParams initialFiltersParams = new InitialFiltersParams(filterNameColumnPropertiesMap);
        filterBuilder.prepareInitialFilters(initialFiltersConfig, initialFiltersParams, filters);
        filterBuilder.prepareExtraFilters(request.getHierarchicalFiltersConfig(), (ComplexFiltersParams)((CollectionRefreshRequest) dto).getFiltersParams(), filters);
        Set<Id> includedIds = request.getIncludedIds();
        if (!includedIds.isEmpty()) {
            filterBuilder.prepareIncludedIdsFilter(includedIds, filters);
        }
        ArrayList<CollectionRowItem> result = generateRowItems(request, properties, filters, offset, limit, request.getExpandableTypes());
        final Id idToFindIfAbsent = ((CollectionRefreshRequest) dto).getIdToFindIfAbsent();
        if (idToFindIfAbsent != null && CollectionPluginHelper.doesNotContainSelectedId(idToFindIfAbsent, result)) {
            int additionalOffset = limit;
            List<CollectionRowItem> additionalItems = generateRowItems(request, properties, filters, additionalOffset, 200, request.getExpandableTypes());
            result.addAll(additionalItems);
        }
        CollectionRowsResponse collectionRowsResponse = new CollectionRowsResponse();
        collectionRowsResponse.setCollectionRows(result);

        return collectionRowsResponse;

    }

    private ArrayList<CollectionRowItem> generateRowItems(CollectionRowsRequest request,
                                                          LinkedHashMap<String, CollectionColumnProperties> properties,
                                                          List<Filter> filters, int offset, int limit, List<String> expandableTypes) {
        ArrayList<CollectionRowItem> list;

        if(request instanceof ExtendedSearchCollectionRowsRequest) {
            ExtendedSearchCollectionRowsRequest searchRequest = (ExtendedSearchCollectionRowsRequest) request;


            list = new ArrayList<>();

            final String targetCollectionName = searchRequest.getCollectionName();

            final Collection<CollectionViewConfig> viewConfigs = configurationService.getConfigs(CollectionViewConfig.class);
            CollectionViewConfig targetViewConfig = null;
            for (CollectionViewConfig viewConfig : viewConfigs) {
                if (viewConfig.getCollection().equals(targetCollectionName) && viewConfig.isDefault()) {
                    targetViewConfig = viewConfig;
                    break;
                }
            }
            final LinkedHashMap<String, CollectionColumnProperties> columnPropertiesMap = new LinkedHashMap<>();
            if (targetViewConfig != null && targetViewConfig.getCollectionDisplayConfig() != null) {
                for (CollectionColumnConfig ccConfig : targetViewConfig.getCollectionDisplayConfig().getColumnConfig()) {
                    final CollectionColumnProperties cProperties = GuiServerHelper
                        .collectionColumnConfigToProperties(ccConfig, null, null);
                    columnPropertiesMap.put(ccConfig.getField(), cProperties);
                }
            }

            IdentifiableObjectCollection collection = searchService.search(searchRequest.getSearchQuery(),
                    targetCollectionName, limit);


            for (IdentifiableObject identifiableObject : collection) {
                final Map<String, Map<Value, ImagePathValue>> fieldMaps = defaultImageMapper.getImageMaps(columnPropertiesMap);
                list.add(generateCollectionRowItem(identifiableObject, columnPropertiesMap, fieldMaps, false));
            }

            // sort collection
            final SortOrder sortOrder = CollectionPluginHelper.getSortOrder(request);
            Collections.sort(list, new Comparator<CollectionRowItem>() {
                @Override
                public int compare(CollectionRowItem o1, CollectionRowItem o2) {
                   Value v1 = o1.getRowValue(sortOrder.get(0).getField());
                   Value v2 = o2.getRowValue(sortOrder.get(0).getField());
                   return  Value.getComparator(sortOrder.get(0).getOrder().equals(SortCriterion.Order.ASCENDING), true).compare(v1, v2);
                }
            });

        }else{
            String collectionName = request.getCollectionName();
            if (request.isSortable()) {
                SortOrder sortOrder = CollectionPluginHelper.getSortOrder(request);
                list = getRows(collectionName, offset, limit, filters, sortOrder, properties, expandableTypes);
            } else {
                if (request.getSimpleSearchQuery().length() > 0) {
                    list = getSimpleSearchRows(collectionName, offset, limit, filters,
                            request.getSimpleSearchQuery(), request.getSearchArea(),
                            properties, request.getExpandableTypes());
                } else {
                    SortOrder sortOrder = sortOrderHelper.buildSortOrderByIdField(collectionName);
                    list = getRows(collectionName, offset, limit, filters, sortOrder, properties, expandableTypes);

                }

            }
        }
        return list;
    }

    private boolean hasConfiguredFilters(Map<String, CollectionColumnProperties> columnPropertyMap) {
        boolean result = false;
        for (CollectionColumnProperties collectionColumnProperties : columnPropertyMap.values()) {
            String filterName = (String) collectionColumnProperties.getProperty(CollectionColumnProperties.SEARCH_FILTER_KEY);
            boolean hidden = (boolean) collectionColumnProperties.getProperty(CollectionColumnProperties.HIDDEN);
            if (filterName != null && !hidden) {
                result = true;
                break;
            }
        }
        return result;
    }

    public CollectionRowsResponse getChildrenForExpanding(Dto request) {
        CollectionRowsRequest rowsRequest = (CollectionRowsRequest) request;

        String collectionName = rowsRequest.getCollectionName();
        SortOrder sortOrder;
        Id parentId = rowsRequest.getParentId();
        Map<String, List<String>> filtersMap = rowsRequest.getFiltersMap();
        List<Filter> filters = new ArrayList<Filter>();

        if (rowsRequest.getDefaultSortCriteriaConfig() == null) {
            sortOrder = sortOrderHelper.buildSortOrderByIdField(collectionName);
        } else {
            sortOrder = sortOrderHelper.buildSortOrder(collectionName, rowsRequest.getDefaultSortCriteriaConfig());
        }

        filterBuilder.prepareExtraFilters(rowsRequest.getHierarchicalFiltersConfig(), new ComplexFiltersParams(rowsRequest.getParentId()), filters);

        IdentifiableObjectCollection collection = collectionsService.
                findCollection(collectionName, sortOrder, filters, rowsRequest.getOffset(), rowsRequest.getLimit());
        ArrayList<CollectionRowItem> items = new ArrayList<>();
        boolean notMoreItems = rowsRequest.getOffset() == 0;

        /* Фильтр в выпадающую коллекцию пока не нужен
        if(notMoreItems){
            CollectionRowItem filter = new CollectionRowItem();
            filter.setParentId(parentId);
            filter.setRowType(CollectionRowItem.RowType.FILTER);
            filter.setRow(new HashMap<String, Value>(0));
            filter.setFilters(filtersMap);
            items.add(filter);
        } */

        Map<String, Map<Value, ImagePathValue>> fieldMappings = defaultImageMapper.getImageMaps(rowsRequest.getColumnProperties());
        for (IdentifiableObject identifiableObject : collection) {
            Boolean typeIsExpandable = ((CollectionRowsRequest) request).getExpandableTypes().contains(crudService.getDomainObjectType(identifiableObject.getId()));
            CollectionRowItem item = generateCollectionRowItem(identifiableObject, rowsRequest.getColumnProperties(), fieldMappings, typeIsExpandable);
            item.setParentId(parentId);
            item.setNestingLevel(rowsRequest.getCurrentNestingLevel() + 1);
            items.add(item);
        }
        if (notMoreItems) {
            CollectionRowItem moreItems = new CollectionRowItem();
            moreItems.setParentId(parentId);
            moreItems.setRowType(CollectionRowItem.RowType.BUTTON);
            moreItems.setRow(new HashMap<String, Value>(0));
            moreItems.setNestingLevel(rowsRequest.getCurrentNestingLevel());
            items.add(moreItems);
        }

        CollectionRowsResponse response = new CollectionRowsResponse();
        response.setCollectionRows(items);
        return response;

    }

    private List<Filter> prepareFilters(Id parentId, LinkedHashMap<String, CollectionColumnProperties> columnPropertiesMap,
                                        Map<String, List<String>> filtersMap) {
        List<Filter> filters = new ArrayList<>();
        filterBuilder.prepareExcludedIdsFilter(Arrays.asList(parentId), filters);

        InitialFiltersConfig initialFiltersConfig = prepareInitialFiltersConfig(columnPropertiesMap, filtersMap);

        Map<String, CollectionColumnProperties> filterNameColumnPropertiesMap = CollectionPluginHelper.
                getFilterNameColumnPropertiesMap(columnPropertiesMap, initialFiltersConfig);
        InitialFiltersParams filtersParams = new InitialFiltersParams(filterNameColumnPropertiesMap);
        filterBuilder.prepareInitialFilters(initialFiltersConfig, filtersParams, filters);
        return filters;
    }

    private InitialFiltersConfig prepareInitialFiltersConfig(LinkedHashMap<String, CollectionColumnProperties> columnPropertiesMap,
                                                             Map<String, List<String>> filtersMap) {
        InitialFiltersConfig initialFiltersConfig = new InitialFiltersConfig();
        List<InitialFilterConfig> initialFilterList = new ArrayList<>();
        initialFiltersConfig.setFilterConfigs(initialFilterList);
        if (filtersMap != null) {
            for (Map.Entry<String, List<String>> entry : filtersMap.entrySet()) {
                InitialFilterConfig filterConfig = new InitialFilterConfig();
                CollectionColumnProperties properties = columnPropertiesMap.get(entry.getKey());
                String filterName = (String) properties.getProperty(CollectionColumnProperties.SEARCH_FILTER_KEY);
                String type = (String) properties.getProperty(CollectionColumnProperties.TYPE_KEY);
                filterConfig.setName(filterName);
                List<String> filterValues = entry.getValue();
                List<InitialParamConfig> paramConfigs = new ArrayList<>();
                for (int i = 0; i < filterValues.size(); i++) {
                    String s = filterValues.get(i);
                    InitialParamConfig paramConfig = new InitialParamConfig();
                    paramConfig.setName(i);
                    paramConfig.setType(type);
                    paramConfig.setValue(s);
                    paramConfigs.add(paramConfig);

                }
                filterConfig.setParamConfigs(paramConfigs);
                initialFilterList.add(filterConfig);
            }
        }
        return initialFiltersConfig;
    }

}
