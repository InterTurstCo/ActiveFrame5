package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SetSelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.form.widget.ExpandableObjectConfig;
import ru.intertrust.cm.core.config.gui.form.widget.TableBrowserParams;
import ru.intertrust.cm.core.config.gui.form.widget.filter.InitialParamConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.extra.CollectionExtraFiltersConfig;
import ru.intertrust.cm.core.config.gui.navigation.*;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;
import ru.intertrust.cm.core.gui.api.client.Predicate;
import ru.intertrust.cm.core.gui.api.client.history.HistoryManager;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.action.Action;
import ru.intertrust.cm.core.gui.impl.client.event.*;
import ru.intertrust.cm.core.gui.impl.client.event.collection.CollectionChangeSelectionEvent;
import ru.intertrust.cm.core.gui.impl.client.event.collection.CollectionChangeSelectionEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.collection.CollectionSelectionChangeListener;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.LabledCheckboxCell;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.ColumnHeaderBlock;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.header.CollectionColumnHeader;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.header.CollectionColumnHeaderController;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.header.widget.HeaderWidget;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.header.widget.HeaderWidgetFactory;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;
import ru.intertrust.cm.core.gui.impl.client.util.CollectionDataGridUtils;
import ru.intertrust.cm.core.gui.impl.client.util.GuiUtil;
import ru.intertrust.cm.core.gui.impl.client.util.JsonUtil;
import ru.intertrust.cm.core.gui.impl.client.util.UserSettingsUtil;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.SortedMarker;
import ru.intertrust.cm.core.gui.model.action.system.CollectionFiltersActionContext;
import ru.intertrust.cm.core.gui.model.action.system.CollectionSortOrderActionContext;
import ru.intertrust.cm.core.gui.model.form.widget.CollectionRowsResponse;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionPluginData;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRefreshRequest;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowsRequest;
import ru.intertrust.cm.core.gui.model.util.WidgetUtil;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.*;

import static ru.intertrust.cm.core.config.localization.LocalizationKeys.*;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.*;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 17/9/13
 *         Time: 12:05 PM
 */
public class CollectionPluginView extends PluginView {

    private CollectionDataGrid tableBody;
    private List<CollectionRowItem> items;
    private VerticalPanel root = new VerticalPanel();
    private int listCount;
    private int tableWidth;
    private boolean embeddedDisplayCheckBoxes = false;
    private SortCollectionState sortCollectionState;
    private ToggleButton filterButton = new ToggleButton();
    private HandlerRegistration scrollHandlerRegistration;
    private Map<String, List<String>> filtersMap = new HashMap<>();
    private String simpleSearchQuery = "";
    private String searchArea = "";
    private CollectionColumnHeaderController columnHeaderController;
    private int lastScrollPos = -1;
    // локальная шина событий
    private EventBus eventBus;
    private CollectionCsvController csvController;
    private SetSelectionModel<CollectionRowItem> selectionModel;
    private CollectionExtraFiltersConfig hierarchicalFiltersConfig;
    private List<IsWidget> breadcrumbWidgets = new ArrayList<>();
    private List<com.google.web.bindery.event.shared.HandlerRegistration> handlerRegistrations = new ArrayList<>();
    private boolean filteredByUser;

    protected CollectionPluginView(CollectionPlugin plugin) {
        super(plugin);
        this.eventBus = plugin.getLocalEventBus();
        DataGrid.Resources resources = GlobalThemesManager.getDataGridResources();
        tableBody = new CollectionDataGrid(plugin, 15, resources, eventBus);
        tableWidth = plugin.getOwner().getVisibleWidth();
        columnHeaderController =
                new CollectionColumnHeaderController(getCollectionIdentifier(), tableBody, tableWidth, eventBus);
    }

    private void updateSizes() {
        tableWidth = plugin.getOwner().getVisibleWidth();
        columnHeaderController.adjustColumnsWidth(tableWidth, tableBody);
    }

    public CollectionDataGrid getTableBody() {
        return tableBody;
    }

    /*This method is invoked when splitter changes position and after initialization of BusinessUniverse
            so we have to check if scroll is visible. If no load more rows
         */
    public void fetchMoreItemsIfRequired() {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            public void execute() {
                /**
                 * Need to find better way to set table focus automatically
                 * causes table shift AFVTBCNT-161
                 * tableBody.setFocus(true);
                 */
                if (CollectionDataGridUtils.isTableVerticalScrollNotVisible(tableBody)) {
                    if (sortCollectionState != null) {
                        sortCollectionState.setResetCollection(false);
                    }
                    fetchData();

                }
            }
        });
    }

    @Override
    public IsWidget getViewWidget() {
        final CollectionPluginData collectionPluginData = plugin.getInitialData();
        final CollectionViewerConfig collectionViewerConfig = (CollectionViewerConfig) plugin.getConfig();
        collectionViewerConfig.setInitialFiltersConfig(collectionPluginData.getInitialFiltersConfig());
        embeddedDisplayCheckBoxes = collectionPluginData.isEmbeddedDisplayCheckBoxes();
        tableBody.setEmbeddedDisplayCheckBoxes(embeddedDisplayCheckBoxes);
        tableBody.setDisplayCheckBoxes(collectionPluginData.isDisplayCheckBoxes());
        searchArea = collectionPluginData.getSearchArea();
        items = collectionPluginData.getItems();
        hierarchicalFiltersConfig = collectionPluginData.getHierarchicalFiltersConfig();
        init();
        final Set<Id> selectedIds = prepareSelectedIds();
        if (!collectionPluginData.isEmbedded()) {
            if (WidgetUtil.isNotEmpty(selectedIds)) {
                for (CollectionRowItem item : items) {
                    if (selectedIds.contains(item.getId())) {
                        selectionModel.setSelected(item, true);
                    }
                }
            }
            Application.getInstance().getHistoryManager().setSelectedIds(selectedIds.toArray(new Id[selectedIds.size()]));
        }

        root.addStyleName("collection-plugin-view-container");
        addHandlers();
        if (!collectionPluginData.isExtendedSearchMarker()) {
            final com.google.gwt.user.client.Timer timer = new com.google.gwt.user.client.Timer() {
                @Override
                public void run() {
                    fetchMoreItemsIfRequired();
                    this.cancel();
                }
            };
            timer.scheduleRepeating(1000);
        }

        return root;
    }

    public void init() {
        buildPanel();
        createTableColumns();
        applySelectionModel();
        insertInitialRows();

        csvController = new CollectionCsvController(root);

    }

    public List<Id> getSelectedIds() {
        if (selectionModel == null) {
            applySelectionModel();
        }
        final List<Id> selectedIds = new ArrayList<>(selectionModel.getSelectedSet().size());
        for (CollectionRowItem item : selectionModel.getSelectedSet()) {
            selectedIds.add(item.getId());
        }
        return selectedIds;
    }

    public void setSelectedRow(final Id objectId) {
        CollectionRowItem item = getRowItem(objectId);
        if (item != null) {
            selectionModel.setSelected(item, true);
            tableBody.getRowElement(items.indexOf(item)).scrollIntoView();
        } else {
            final Boolean originalResetState = sortCollectionState.isResetCollection();
            sortCollectionState.setResetCollection(false);
            final CollectionRowsRequest request = createRequest();
            request.setLimit(0);
            request.setOffset(0);
            Set<Id> searchId = new HashSet<>();
            searchId.add(objectId);
            request.setIncludedIds(searchId);
            List<String> expandableTypes = new ArrayList<String>();
            if (((CollectionViewerConfig) plugin.getConfig()).getChildCollectionConfig() != null) {

                for (ExpandableObjectConfig expandableObjectConfig : ((CollectionViewerConfig) plugin.getConfig()).getChildCollectionConfig().
                        getExpandableObjectsConfig().getExpandableObjects()) {
                    expandableTypes.add(expandableObjectConfig.getObjectName());
                }
            }
            request.setExpandableTypes(expandableTypes);

            Command command = new Command("generateCollectionRowItems", "collection.plugin", request);
            BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("something was going wrong while obtaining item by ID ");
                    caught.printStackTrace();
                }

                @Override
                public void onSuccess(Dto result) {
                    CollectionRowsResponse collectionRowsResponse = (CollectionRowsResponse) result;
                    List<CollectionRowItem> collectionRowItems = collectionRowsResponse.getCollectionRows();
                    if (collectionRowItems.size() != 0) {
                        handleCollectionRowsResponse(collectionRowItems, false);
                        selectionModel.setSelected(getRowItem(request.getIncludedIds().iterator().next()), true);
                        tableBody.getRowElement(items.indexOf(getRowItem(request.getIncludedIds().iterator().next()))).scrollIntoView();
                        sortCollectionState.setResetCollection(originalResetState);
                    }
                }
            });
        }
    }

    private CollectionRowItem getRowItem(Id objectId) {
        for (CollectionRowItem item : items) {
            if (item.getId().equals(objectId)) {
                return item;
            }
        }
        return null;
    }

    public boolean restoreHistory() {
        final HistoryManager manager = Application.getInstance().getHistoryManager();
        final List<Id> selectedFromHistoryIds = manager.getSelectedIds();
        final List<Id> selectedIds = getSelectedIds();
        final Id oldSelectedFormId = selectedIds.isEmpty() ? null : selectedIds.get(0);
        Id newSelectedFormId = null;
        selectionModel.clear();
        boolean restoreSelection = !getPluginData().isEmbedded();
        if (!selectedFromHistoryIds.isEmpty() && restoreSelection) {
            for (CollectionRowItem item : items) {
                if (selectedFromHistoryIds.contains(item.getId())) {
                    selectionModel.setSelected(item, true);
                    if (newSelectedFormId == null) {
                        newSelectedFormId = item.getId();
                    }
                }
            }
        }
        if (newSelectedFormId != null && !newSelectedFormId.equals(oldSelectedFormId)) {
            eventBus.fireEvent(new CollectionRowSelectedEvent(newSelectedFormId));
            return true;
        }
        return false;
    }

    private void createTableColumns() {
        List<ColumnHeaderBlock> columnHeaderBlocks = new ArrayList<ColumnHeaderBlock>();
        CollectionPluginData pluginData = getPluginData();
        if (embeddedDisplayCheckBoxes) {
            createEmbeddedTableColumnsWithCheckBoxes(columnHeaderBlocks);
        } else if (pluginData.isDisplayCheckBoxes()) {
            createTableColumnsWithCheckBoxes(columnHeaderBlocks);
        } else {
            createTableColumnsWithoutCheckBoxes(columnHeaderBlocks);
        }
    }

    public void onPluginPanelResize() {
        updateSizes();

    }

    private void addHandlers() {

        handlerRegistrations.add(eventBus.addHandler(CollectionPluginResizeBySplitterEvent.TYPE, new
                CollectionPluginResizeBySplitterEventHandler() {
                    @Override
                    public void onCollectionPluginResizeBySplitter(CollectionPluginResizeBySplitterEvent event) {
                        columnHeaderController.saveFilterValues();
                        tableBody.redraw();
                        columnHeaderController.setFocus();
                        columnHeaderController.updateFilterValues();

                        fetchMoreItemsIfRequired();
                    }
                }));

        // обработчик обновления коллекции (строки в таблице)
        handlerRegistrations.add(eventBus.addHandler(UpdateCollectionEvent.TYPE, new UpdateCollectionEventHandler() {
            @Override
            public void updateCollection(UpdateCollectionEvent event) {
                /**
                 * Для иерархических коллекций, при добавлении дочерних строк и обновлении
                 * родительских, обновляется вся коллекция. До того как будет разработан
                 * иеррахический плагин коллекций это временный фикс. Для обычных коллекций это
                 * не происходит.
                 */
                if (((CollectionViewerConfig) plugin.getConfig()).getChildCollectionConfig() != null) {
                    getPlugin().refresh();
                }
                if (event.getId() == null) {
                    refreshCollection(event.getIdentifiableObject());
                } else {
                    refreshCollection(event.getId());
                }

            }
        }));

        final ScrollPanel scroll = tableBody.getScrollPanel();
        tableBody.addColumnSortHandler(new ColumnSortEvent.Handler() {
            @Override
            public void onColumnSort(ColumnSortEvent event) {

                CollectionColumn column = (CollectionColumn) event.getColumn();
                String dataStoreName = column.getDataStoreName();
                boolean ascending = event.isSortAscending();
                String field = column.getFieldName();
                sortCollectionState = new SortCollectionState(0, getPluginData().getRowsChunk(), dataStoreName,
                        ascending, false, field);
                clearAllTableData();
                final CollectionViewerConfig collectionViewerConfig = (CollectionViewerConfig) plugin.getConfig();
                collectionViewerConfig.getDefaultSortCriteriaConfig().setColumnField(field);
                collectionViewerConfig.getDefaultSortCriteriaConfig().setOrder(
                        ascending ? CommonSortCriterionConfig.ASCENDING : CommonSortCriterionConfig.DESCENDING);
                final CollectionSortOrderActionContext context = new CollectionSortOrderActionContext();
                context.setActionConfig(UserSettingsUtil.createActionConfig());
                context.setLink(Application.getInstance().getHistoryManager().getLink());
                context.setCollectionViewName(getCollectionIdentifier());
                context.setCollectionViewerConfig(collectionViewerConfig);
                final Action action = ComponentRegistry.instance.get(CollectionSortOrderActionContext.COMPONENT_NAME);
                action.setInitialContext(context);
                action.perform();
            }
        });

        scrollHandlerRegistration = scroll.addScrollHandler(new ScrollLazyLoadHandler());
        handlerRegistrations.add(eventBus.addHandler(SimpleSearchEvent.TYPE, new SimpleSearchEventHandler() {
            @Override
            public void collectionSimpleSearch(SimpleSearchEvent event) {
                //To change body of implemented methods use File | Settings | File Templates.
                listCount = 0;
                items.clear();
                if (!event.isTypeButton()) {
                    filtersMap.clear();
                    simpleSearchQuery = "";
                } else {
                    simpleSearchQuery = event.getText();

                }
                CollectionRowsRequest request = createCollectionRowsRequest();
                collectionRowRequestCommand(request);
            }
        }));
        //показать/спрятать панель поиска в таблицы
        filterButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                columnHeaderController.changeFiltersInputsVisibility(filterButton.getValue());

            }
        });

        //событие которое инициализирует поиск по колонкам таблицы
        handlerRegistrations.add(eventBus.addHandler(FilterEvent.TYPE, new FilterEventHandler() {
            @Override
            public void onFilterEvent(FilterEvent event) {
                boolean filterCanceled = event.isFilterCanceled();
                boolean remote = event.isRemote();
                if (filterCanceled) {
                    onKeyEscapePressed();
                } else if(!filterCanceled && !remote){
                    onKeyEnterPressed();
                }
                updateFilterConfig();
                clearAllTableData();
            }
        }));


        // экспорт в csv
        handlerRegistrations.add(eventBus.addHandler(SaveToCsvEvent.TYPE, new SaveToCsvEventHandler() {
            @Override
            public void saveToCsv(SaveToCsvEvent saveToCsvEvent) {
                final InitialFiltersConfig initialFiltersConfig =
                        ((CollectionViewerConfig) plugin.getConfig()).getInitialFiltersConfig();
                JSONObject requestObj = new JSONObject();
                JsonUtil.prepareJsonAttributes(requestObj, getPluginData().getCollectionName(), simpleSearchQuery,
                        searchArea);
                JsonUtil.prepareJsonSortCriteria(requestObj, getPluginData().getDomainObjectFieldPropertiesMap(),
                        sortCollectionState);
                JsonUtil.prepareJsonColumnProperties(requestObj, getPluginData().getDomainObjectFieldPropertiesMap(),
                        filtersMap);
                JsonUtil.prepareJsonInitialFilters(requestObj, initialFiltersConfig, "jsonInitialFilters");
                JsonUtil.prepareJsonHierarchicalFiltersConfig(requestObj, hierarchicalFiltersConfig,
                        "jsonHierarchicalFilters");
                csvController.doPostRequest(requestObj.toString());

            }
        }));

        handlerRegistrations.add(eventBus.addHandler(CollectionChangeSelectionEvent.TYPE, new
                CollectionChangeSelectionEventHandler() {
                    @Override
                    public void changeCollectionSelection(CollectionChangeSelectionEvent event) {
                        Collection<Id> ids = getPluginData().getChosenIds();
                        final List<Id> changedIds = event.getId();

                        boolean selected = event.isSelected();
                        if (selected) {
                            ids.addAll(changedIds);
                        } else {
                            ids.removeAll(changedIds);
                        }
                        TableBrowserParams tableBrowserParams = getPluginData().getTableBrowserParams();
                        if (tableBrowserParams != null && !tableBrowserParams.isDisplayCheckBoxes()) { //single choice
                            final Id id = changedIds.get(0);
                            CollectionRowItem item = GuiUtil.find(items, new Predicate<CollectionRowItem>() {
                                @Override
                                public boolean evaluate(CollectionRowItem input) {
                                    return input.getId().equals(id);
                                }
                            });
                            selectionModel.setSelected(item, selected);
                        }
                        tableBody.redraw();
                    }
                }));


    }

    private void onKeyEnterPressed() {
        filtersMap.clear();
        for (ColumnHeaderBlock block : columnHeaderController.getColumnHeaderBlocks()) {
            CollectionColumnHeader header = block.getHeader();
            List<String> filterValues = header.getHeaderWidget().getFilterValues();
            if (filterValues != null) {
                filtersMap.put(header.getHeaderWidget().getFieldName(), filterValues);

            }
        }
        filteredByUser = true;
    }

    private void onKeyEscapePressed() {
        filterButton.setValue(false);
        columnHeaderController.clearFilters();
        columnHeaderController.changeFiltersInputsVisibility(false);
        filtersMap.clear();
        InitialFiltersConfig initialFiltersConfig = getPluginData().getInitialFiltersConfig();
        if (initialFiltersConfig != null) {
            List<InitialFilterConfig> initialFilters = initialFiltersConfig.getFilterConfigs();
            if (initialFilters != null) {
                initialFilters.clear();
            }
        }
        filteredByUser = false;

    }

    private void updateFilterConfig() {
        final CollectionViewerConfig config = (CollectionViewerConfig) plugin.getConfig();
        final List<InitialFilterConfig> configs = new ArrayList<InitialFilterConfig>();
        Map<String, CollectionColumnProperties> columnPropertiesMap = getPluginData().getDomainObjectFieldPropertiesMap();
        for (Map.Entry<String, List<String>> entry : filtersMap.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                final InitialFilterConfig initialFilterConfig = new InitialFilterConfig();
                String filterName = (String) columnPropertiesMap
                        .get(entry.getKey()).getProperty(CollectionColumnProperties.SEARCH_FILTER_KEY);
                initialFilterConfig.setName(filterName);
                final List<InitialParamConfig> paramConfigs = new ArrayList<InitialParamConfig>();
                for (int index = 0; index < entry.getValue().size(); index++) {
                    final InitialParamConfig paramConfig = new InitialParamConfig();
                    paramConfig.setName(Integer.valueOf(index));
                    final String paramValue = entry.getValue().get(index).trim();
                    if (!paramValue.isEmpty()) {
                        paramConfig.setValue(entry.getValue().get(index));
                        paramConfig.setType((String) columnPropertiesMap
                                .get(entry.getKey()).getProperty(CollectionColumnProperties.TYPE_KEY));
                        paramConfig.setTimeZoneId((String) columnPropertiesMap
                                .get(entry.getKey()).getProperty(CollectionColumnProperties.TIME_ZONE_ID));
                        paramConfigs.add(paramConfig);
                    }
                }
                if (!paramConfigs.isEmpty()) {
                    initialFilterConfig.setParamConfigs(paramConfigs);
                    configs.add(initialFilterConfig);
                }
            }
        }
        List<InitialFilterConfig> previous = config.getInitialFiltersConfig() == null ? null
                : config.getInitialFiltersConfig().getFilterConfigs();
        List<InitialFilterConfig> mergedConfigs = CollectionDataGridUtils.mergeInitialFiltersConfigs(configs, previous,
                columnPropertiesMap);
        if (mergedConfigs.isEmpty()) {
            config.setInitialFiltersConfig(null);
        } else {
            final InitialFiltersConfig initialFiltersConfig = new InitialFiltersConfig();
            initialFiltersConfig.setFilterConfigs(mergedConfigs);
            config.setInitialFiltersConfig(initialFiltersConfig);
        }
        final Action action = ComponentRegistry.instance.get(CollectionFiltersActionContext.COMPONENT_NAME);
        final CollectionFiltersActionContext context = new CollectionFiltersActionContext();
        context.setLink(Application.getInstance().getHistoryManager().getLink());
        context.setCollectionViewName(getCollectionIdentifier());
        context.setCollectionViewerConfig(config);
        final ActionConfig actionConfig = new ActionConfig();
        actionConfig.setImmediate(true);
        actionConfig.setDirtySensitivity(false);
        context.setActionConfig(actionConfig);
        action.setInitialContext(context);
        action.perform();
    }

    private void clearAllTableData() {
        items.clear();
        listCount = 0;
        fetchData();

    }

    private void fetchData() {
        CollectionRowsRequest request = createRequest();
        collectionRowRequestCommand(request);
    }

    public CollectionRowsRequest createRequest() {
        return sortCollectionState == null ? createCollectionRowsRequest() : createSortedCollectionRowsRequest();
    }

    // метод для удаления из коллекции
    public void delCollectionRow(Id collectionObject) {
        if (items.isEmpty()) {
            return;  // если в коллекции не было элементов операция удаления ни к чему не приводит
        }
        // ищем какой элемент коллекции должен быть удален
        int index = getIndex(collectionObject);
        if (index < 0) {    // element of collection isn't found.
            return;
        }
        // удаляем из коллекции
        selectionModel.setSelected(items.get(index), false);
        items.remove(index);
        if (!items.isEmpty()) {
            // выделение строки - если удалили последнюю строку, выделяем предыдущую
            if (index == items.size()) {
                index--;
            }
            CollectionRowItem itemRow = items.get(index);
            selectionModel.setSelected(itemRow, true);
        }
        tableBody.setRowData(items);
    }

    /**
     * Метод для обновления коллекции
     *
     * @param collectionObject
     */
    public void refreshCollection(IdentifiableObject collectionObject) {
        refreshCollection(collectionObject.getId());

    }

    private void refreshCollection(Id id) {
        Set<Id> includedIds = new HashSet<>();
        includedIds.add(id);
        CollectionViewerConfig config = (CollectionViewerConfig) plugin.getConfig();
        final CollectionRowsRequest collectionRowsRequest =
                new CollectionRowsRequest(0, 1, getPluginData().getCollectionName(),
                        getPluginData().getDomainObjectFieldPropertiesMap(), simpleSearchQuery, searchArea);
        collectionRowsRequest.setIncludedIds(includedIds);
        InitialFiltersConfig initialFiltersConfig = config.getInitialFiltersConfig();
        collectionRowsRequest.setInitialFiltersConfig(initialFiltersConfig);
        collectionOneRowRequestCommand(new CollectionRefreshRequest(collectionRowsRequest, null));
    }

    private void buildPanel() {
        AbsolutePanel treeLinkWidget = new AbsolutePanel();
        treeLinkWidget.addStyleName("collection-plugin-view-container");
        root.add(treeLinkWidget);
        boolean columnButtonsBuilt = buildColumnButtons(treeLinkWidget);
        boolean searchAreaBuilt = buildSearchArea(treeLinkWidget);
        boolean breadCrumbsBuilt = buildBreadCrumbs();
        if (breadCrumbsBuilt || columnButtonsBuilt || searchAreaBuilt) {
            tableBody.addStyleName("collection-plugin-view"); //header margin style
        }
        root.add(tableBody);
        tableBody.setEmptyTableMessage(!getPluginData().isEmbedded());
    }

    private boolean buildSearchArea(Panel container) {
        boolean result = false;
        if (searchArea != null && !searchArea.isEmpty()) {
            FlowPanel simpleSearch = new FlowPanel();
            SimpleSearchPanel simpleSearchPanel = new SimpleSearchPanel(simpleSearch, eventBus);
            container.add(simpleSearchPanel);
            result = true;
        }
        return result;
    }

    private boolean buildBreadCrumbs() {
        boolean result = false;
        if (plugin.isShowBreadcrumbs()) {
            Panel breadCrumbsPanel = createBreadCrumbsPanel();
            if (breadCrumbsPanel != null) {
                root.add(breadCrumbsPanel);
                result = true;
            }
        }
        return result;
    }

    private boolean buildColumnButtons(Panel container) {
        boolean result = false;
        CollectionPluginData pluginData = getPluginData();
        if (pluginData.hasColumnButtons()) {

            final Button columnManagerButton = new Button();
            columnManagerButton.setTitle(LocalizeUtil.get(COLUMNS_DISPLAY_TOOLTIP_KEY, COLUMNS_DISPLAY_TOOLTIP));
            columnManagerButton.setStyleName(GlobalThemesManager.getCurrentTheme().commonCss().columnSettingsButton());
            columnManagerButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    columnHeaderController.showPopup(columnManagerButton);
                }
            });
            container.add(columnManagerButton);
            Button columnWidthRecalculateButton = new Button();
            columnWidthRecalculateButton.setTitle(LocalizeUtil.get(COLUMNS_WIDTH_TOOLTIP_KEY, COLUMNS_WIDTH_TOOLTIP));
            columnWidthRecalculateButton.setStyleName(GlobalThemesManager.getCurrentTheme().commonCss()
                    .recalculateColumnsWidthBtn());
            columnWidthRecalculateButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    columnHeaderController.changeTableWidthByCondition();
                }
            });
            container.add(columnWidthRecalculateButton);

            if (pluginData.hasConfiguredFilters()) {
                filterButton.setStyleName(GlobalThemesManager.getCurrentTheme().commonCss().filterOpenBtn());
                filterButton.setTitle(LocalizeUtil.get(FILTER_TOOLTIP_KEY, FILTER_TOOLTIP));
                container.add(filterButton);
            }
            result = true;
        }
        return result;
    }

    @Override
    protected Panel createBreadCrumbsPanel() {
        Panel breadCrumbsPanel = super.createBreadCrumbsPanel();
        Iterator<IsWidget> iterator = breadcrumbWidgets.iterator();
        while (iterator.hasNext()) {
            IsWidget next = iterator.next();
            breadCrumbsPanel.add(next);
            if (iterator.hasNext()) {
                breadCrumbsPanel.add(new Label("/"));
            }
        }
        return breadCrumbsPanel;
    }

    private void createEmbeddedTableColumnsWithCheckBoxes(List<ColumnHeaderBlock> columnHeaderBlocks) {
        final CollectionColumn<Boolean> checkBoxColumn =
                new CollectionColumn<Boolean>(new LabledCheckboxCell(true, true)) {
                    @Override
                    public Boolean getValue(CollectionRowItem object) {
                        return getPluginData().getChosenIds().contains(object.getId());

                    }
                };

        checkBoxColumn.setFieldUpdater(new FieldUpdater<CollectionRowItem, Boolean>() {
            @Override
            public void update(int index, CollectionRowItem object, Boolean value) {
                Id id = object.getId();
                eventBus.fireEvent(new CheckBoxFieldUpdateEvent(object.getId(), !value));
                Collection<Id> chosenIds = getPluginData().getChosenIds();
                if (value) {
                    chosenIds.add(id);
                } else {
                    chosenIds.remove(id);
                }
                fireCollectionRowCheckListener(object.getId(), value);
            }
        });
        createTableColumnsWithCheckBoxes(checkBoxColumn, columnHeaderBlocks);
    }

    private void fireCollectionRowCheckListener(Id objectId, Boolean value) {
        CollectionViewerConfig config = (CollectionViewerConfig) plugin.getConfig();
        if (config.getRowsSelectionConfig() != null &&
                config.getRowsSelectionConfig().getComponent() != null) {
            CollectionSelectionChangeListener checkBoxChangeListener = ComponentRegistry.instance.get(config.getRowsSelectionConfig().getComponent());
            if (checkBoxChangeListener != null) {
                checkBoxChangeListener.onSelectionChange(plugin, plugin.getOwner().asWidget().getParent(), objectId, value);
            } else {
                throw new GuiException("Component " + config.getRowsSelectionConfig().getComponent() + " not found.");
            }
        }
    }

    private void checkForMultiSelectionEnabled(Boolean selected, Id selectedId) {
        CollectionViewerConfig config = (CollectionViewerConfig) plugin.getConfig();
        if (selected && config.getRowsSelectionConfig() != null &&
                !config.getRowsSelectionConfig().isMultiSelection()) {
            int ind = 0;
            for (CollectionRowItem item : items) {
                if (!item.getId().equals(selectedId)) {
                    ((FieldUpdater<CollectionRowItem, Boolean>) (tableBody.getColumn(0).getFieldUpdater())).update(ind, item, false);
                    ind++;
                }
                tableBody.getSelectionModel().setSelected(item, false);
            }
        }
    }

    private void createTableColumnsWithCheckBoxes(List<ColumnHeaderBlock> columnHeaderBlocks) {
        final CollectionColumn<Boolean> checkBoxColumn =
                new CollectionColumn<Boolean>(new LabledCheckboxCell(false, false)) {
                    @Override
                    public Boolean getValue(CollectionRowItem object) {
                        CollectionPlugin collectionPlugin = getPlugin();
                        Boolean changedState = collectionPlugin.getChangedRowsState().get(object.getId());
                        return changedState == null ? collectionPlugin.getCheckBoxDefaultState() : changedState;
                    }
                };

        checkBoxColumn.setFieldUpdater(new FieldUpdater<CollectionRowItem, Boolean>() {
            @Override
            public void update(int index, CollectionRowItem object, Boolean value) {
                checkForMultiSelectionEnabled(value, object.getId());
                Map<Id, Boolean> changedRowsSelection = getPlugin().getChangedRowsState();
                Id id = object.getId();
                eventBus.fireEvent(new CheckBoxFieldUpdateEvent(object.getId(), !value));
                changedRowsSelection.put(id, value);
                tableBody.redraw();
                fireCollectionRowCheckListener(object.getId(), value);

            }
        });
        createTableColumnsWithCheckBoxes(checkBoxColumn, columnHeaderBlocks);
    }


    private void createTableColumnsWithCheckBoxes(CollectionColumn checkBoxColumn, List<ColumnHeaderBlock> columnHeaderBlocks) {
        ColumnFormatter.formatCheckBoxColumn(checkBoxColumn);
        tableBody.addColumn(checkBoxColumn);
        HeaderWidget headerWidget = HeaderWidgetFactory.getInstance(checkBoxColumn, null, null);
        CollectionColumnHeader collectionColumnHeader = new CollectionColumnHeader(tableBody, checkBoxColumn, headerWidget, eventBus);
        ColumnHeaderBlock columnHeaderBlock = new ColumnHeaderBlock(collectionColumnHeader, checkBoxColumn);
        columnHeaderBlocks.add(columnHeaderBlock);
        createTableColumnsWithoutCheckBoxes(columnHeaderBlocks);
    }


    private void createTableColumnsWithoutCheckBoxes(List<ColumnHeaderBlock> columnHeaderBlocks) {
        for (String field : getPluginData().getDomainObjectFieldPropertiesMap().keySet()) {
            final CollectionColumnProperties columnProperties = getPluginData().getDomainObjectFieldPropertiesMap().get(field);
            final CollectionColumn column = ColumnFormatter.createFormattedColumn(columnProperties, eventBus);
            final List<String> initialFilterValues =
                    (List) columnProperties.getProperty(CollectionColumnProperties.INITIAL_FILTER_VALUES);
            HeaderWidget headerWidget = HeaderWidgetFactory.getInstance(column, columnProperties, initialFilterValues);
            CollectionColumnHeader collectionColumnHeader = new CollectionColumnHeader(tableBody, column, headerWidget, eventBus);
            ColumnHeaderBlock block = new ColumnHeaderBlock(collectionColumnHeader, column);
            block.setShouldChangeVisibilityState(!column.isVisible());
            columnHeaderBlocks.add(block);
            tableBody.addColumn(column, collectionColumnHeader);
            SortedMarker sortedMarker = (SortedMarker) columnProperties.getProperty(CollectionColumnProperties.SORTED_MARKER);
            if (sortedMarker != null) {
                boolean ascending = sortedMarker.isAscending();
                column.setDefaultSortAscending(ascending);
                tableBody.getColumnSortList().push(new ColumnSortList.ColumnSortInfo(column, ascending));
                sortCollectionState = new SortCollectionState(0, getPluginData().getRowsChunk(),
                        column.getDataStoreName(), ascending, true, field);
            }
        }

        columnHeaderController.setColumnHeaderBlocks(columnHeaderBlocks);

        String panelStatus = getPanelState();
        if (panelStatus.equalsIgnoreCase(OPEN)) {
            columnHeaderController.changeFiltersInputsVisibility(true);
            filterButton.setValue(true);
        }
        columnHeaderController.changeVisibilityOfColumns(true);

    }

    private String getPanelState() {
        final InitialFiltersConfig initialFiltersConfig =
                ((CollectionViewerConfig) plugin.getConfig()).getInitialFiltersConfig();
        FilterPanelConfig filterPanelConfig = getPluginData().getFilterPanelConfig();
        if (filterPanelConfig == null && initialFiltersConfig == null) {
            return CLOSED;
        } else if (filterPanelConfig == null && initialFiltersConfig != null) {
            String rawPanelState = initialFiltersConfig.getPanelState();
            String panelState = rawPanelState == null ? CLOSED : rawPanelState;
            return panelState;
        }
        return filterPanelConfig.getPanelState();

    }

    public void insertRows(List<CollectionRowItem> list) {
        items.clear();
        items.addAll(list);
        tableBody.setRowData(list);
        listCount = items.size();

    }

    private void insertInitialRows() {
        tableBody.setRowData(items);
        listCount = items.size();

    }

    private void insertMoreRows(List<CollectionRowItem> list) {
        items.addAll(removeDuplicate(list));
        tableBody.setRowData(items);
        listCount = items.size();

    }

    /**
     * Этот метод нужен для того чтобы у нас была возможность произвольно
     * добавлять строки, по одной, при поиске Id через апи, без подтяжки нескольких
     * страниц, и затем когда при скролле подтянется коллекция с этим Id чтобы
     * не было дублирования
     *
     * @param list
     * @return
     */
    private List<CollectionRowItem> removeDuplicate(List<CollectionRowItem> list) {
        for (Iterator<CollectionRowItem> iterator = list.iterator(); iterator.hasNext(); ) {
            CollectionRowItem newItem = iterator.next();
            if (items.contains(newItem)) {
                iterator.remove();
            }
        }
        return list;
    }

    private void applySelectionModel() {
        if (embeddedDisplayCheckBoxes) {
            selectionModel = new MultiSelectionModel<>();
        } else {
            selectionModel = new SingleSelectionModel<>();
        }
        tableBody.setSelectionModel(selectionModel);
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    private CollectionRowsRequest createCollectionRowsRequest() {
        CollectionPluginData collectionPluginData = plugin.getInitialData();
        CollectionRowsRequest collectionRowsRequest = new CollectionRowsRequest(listCount, getPluginData().getRowsChunk(),
                collectionPluginData.getCollectionName(), collectionPluginData.getDomainObjectFieldPropertiesMap(),
                simpleSearchQuery, searchArea);
        collectionRowsRequest.setFiltersMap(filtersMap);
        CollectionViewerConfig config = (CollectionViewerConfig) plugin.getConfig();
        InitialFiltersConfig initialFiltersConfig = config.getInitialFiltersConfig();
        collectionRowsRequest.setInitialFiltersConfig(initialFiltersConfig);
        TableBrowserParams tableBrowserParams = collectionPluginData.getTableBrowserParams();
        collectionRowsRequest.setTableBrowserParams(tableBrowserParams);
        collectionRowsRequest.setHierarchicalFiltersConfig(hierarchicalFiltersConfig);
        return collectionRowsRequest;

    }

    private CollectionRowsRequest createSortedCollectionRowsRequest() {
        CollectionRowsRequest collectionRowsRequest;
        String field = sortCollectionState.getField();
        boolean ascending = sortCollectionState.isAscend();
        CollectionColumnProperties collectionColumnProperties = getPluginData().getDomainObjectFieldPropertiesMap().get(field);
        if (sortCollectionState.isResetCollection()) {
            items.clear();
            collectionRowsRequest = new CollectionRowsRequest(sortCollectionState.getOffset(), getPluginData().getRowsChunk(),
                    getPluginData().getCollectionName(), getPluginData().getDomainObjectFieldPropertiesMap(),
                    sortCollectionState.isAscend(), sortCollectionState.getColumnName(), field);
            sortCollectionState.setResetCollection(false);
            listCount = 0;

        } else {
            collectionRowsRequest = new CollectionRowsRequest(listCount, getPluginData().getRowsChunk(),
                    getPluginData().getCollectionName(), getPluginData().getDomainObjectFieldPropertiesMap(), ascending,
                    sortCollectionState.getColumnName(), sortCollectionState.getField());

        }
        sortCollectionState.setOffset(listCount);
        SortCriteriaConfig sortCriteriaConfig = ascending ? collectionColumnProperties.getAscSortCriteriaConfig()
                : collectionColumnProperties.getDescSortCriteriaConfig();
        collectionRowsRequest.setSortCriteriaConfig(sortCriteriaConfig);
        collectionRowsRequest.setFiltersMap(filtersMap);
        CollectionPluginData collectionPluginData = plugin.getInitialData();
        CollectionViewerConfig config = (CollectionViewerConfig) plugin.getConfig();
        InitialFiltersConfig initialFiltersConfig = config.getInitialFiltersConfig();
        collectionRowsRequest.setInitialFiltersConfig(initialFiltersConfig);
        TableBrowserParams tableBrowserParams = collectionPluginData.getTableBrowserParams();
        collectionRowsRequest.setTableBrowserParams(tableBrowserParams);
        collectionRowsRequest.setHierarchicalFiltersConfig(hierarchicalFiltersConfig);
        return collectionRowsRequest;

    }

    public void clearScrollHandler() {
        scrollHandlerRegistration.removeHandler();
    }

    private void collectionRowRequestCommand(final CollectionRowsRequest collectionRowsRequest) {

        clearScrollHandler();
        //(CollectionViewerConfig) plugin.getConfig();

        List<String> expandableTypes = new ArrayList<String>();
        if (((CollectionViewerConfig) plugin.getConfig()).getChildCollectionConfig() != null) {

            for (ExpandableObjectConfig expandableObjectConfig : ((CollectionViewerConfig) plugin.getConfig()).getChildCollectionConfig().
                    getExpandableObjectsConfig().getExpandableObjects()) {
                expandableTypes.add(expandableObjectConfig.getObjectName());
            }
        }
        collectionRowsRequest.setExpandableTypes(expandableTypes);

        Command command = new Command("generateCollectionRowItems", "collection.plugin", collectionRowsRequest);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining generateTableRowsForPluginInitialization for ''");
                caught.printStackTrace();
            }

            @Override
            public void onSuccess(Dto result) {
                CollectionRowsResponse collectionRowsResponse = (CollectionRowsResponse) result;
                List<CollectionRowItem> collectionRowItems = collectionRowsResponse.getCollectionRows();
                handleCollectionRowsResponse(collectionRowItems, false);
                if (collectionRowItems.size() < collectionRowsRequest.getLimit()) {
                    return;
                }
                fetchMoreItemsIfRequired();
            }
        });
    }

    public void handleCollectionRowsResponse(List<CollectionRowItem> collectionRowItems, boolean clearPreviousRows) {
        columnHeaderController.saveFilterValues();
        int tableBodyWidth = tableBody.getOffsetWidth();
        if (clearPreviousRows) {

            insertRows(collectionRowItems);
        } else {
            insertMoreRows(collectionRowItems);
        }
        tableBody.flush();
        if (listCount == 0) {
            if (getPluginData().isEmbedded()) {
                tableBody.setEmptyTableMessage(filteredByUser);
            }
            tableBody.setEmptyTableWidgetWidth(tableBodyWidth);

        }
        setUpScrollSelection();
        columnHeaderController.updateFilterValues();
        columnHeaderController.setFocus();
    }

    private void setUpScrollSelection() {
        Set<Id> selectedItems = prepareSelectedIds();
        if (WidgetUtil.containsOneElement(selectedItems)) {
            Id selectedId = selectedItems.iterator().next();
            int index = getIndex(selectedId);
            if (index != -1 && index <= items.size() - 1) {
                CollectionRowItem item = items.get(index);
                if (!selectionModel.isSelected(item)) {
                    selectionModel.setSelected(items.get(index), true); //element is visible, so should be highlighted
                }

            }
        }
        setScrollPosition(lastScrollPos);
        scrollHandlerRegistration = tableBody.getScrollPanel().addScrollHandler(new ScrollLazyLoadHandler());

    }

    public void setScrollPosition(final int position) {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                tableBody.getScrollPanel().setVerticalScrollPosition(position);
            }
        });

    }

    @Deprecated
    public void setUpScroll() {

    }


    private void collectionOneRowRequestCommand(CollectionRefreshRequest request) {
        Command command = new Command("refreshCollection", "collection.plugin", request);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining updated row");
                caught.printStackTrace();
            }

            @Override
            public void onSuccess(Dto result) {
                CollectionRowsResponse collectionRowsResponse = (CollectionRowsResponse) result;
                List<CollectionRowItem> collectionRowItems = collectionRowsResponse.getCollectionRows();
                if (!collectionRowItems.isEmpty()) {
                    CollectionRowItem item = collectionRowItems.get(0);
                    int index = getIndex(item.getId());
                    if (index < 0) {
                        items.add(item);
                    } else {
                        items.set(index, item);
                    }
                    selectionModel.clear();
                    selectionModel.setSelected(item, true);
                }
                listCount = listCount + 1;
                tableBody.setRowData(items);
                tableBody.redraw();
                tableBody.flush();
            }
        });
    }

    public void sideBarFixPositionEvent(SideBarResizeEvent event) {
        if (getPluginData().getTableBrowserParams() == null) {
            columnHeaderController.sideBarFixPositionEvent(event);
        }
    }


    private class ScrollLazyLoadHandler implements ScrollHandler {
        private ScrollPanel scroll;

        private ScrollLazyLoadHandler() {
            lastScrollPos = 0;
            this.scroll = tableBody.getScrollPanel();
        }

        @Override
        public void onScroll(ScrollEvent event) {
            int oldScrollPos = lastScrollPos;
            lastScrollPos = scroll.getVerticalScrollPosition();
            int maxScrollTop = scroll.getMaximumVerticalScrollPosition();
            if (oldScrollPos == 0) {
                return;
            }

            // If scrolling up, ignore the event.
            if (oldScrollPos == maxScrollTop) {

            }
            if (oldScrollPos >= lastScrollPos) {
                return;
            }

            if (lastScrollPos >= maxScrollTop) {
                if (sortCollectionState != null) {
                    sortCollectionState.setResetCollection(false);
                }

                fetchData();
            }
        }

    }

    private int getIndex(final Id id) {
        int index = 0;
        for (CollectionRowItem item : items) {
            if (item.getId().equals(id)) {
                return index;
            }
            ++index;
        }
        return -1;
    }

    private String getCollectionIdentifier() {
        return getPluginData().getCollectionViewConfigName();
    }

    public void setBreadcrumbWidgets(List<IsWidget> breadcrumbWidgets) {
        this.breadcrumbWidgets.clear();
        if (breadcrumbWidgets != null) {
            this.breadcrumbWidgets.addAll(breadcrumbWidgets);
        }
    }

    private CollectionPluginData getPluginData() {
        return plugin.getInitialData();
    }

    private Set<Id> prepareSelectedIds() {
        CollectionPluginData collectionPluginData = getPluginData();
        final Set<Id> selectedIds = new HashSet<Id>();
        final List<Id> selectedFromHistory = Application.getInstance().getHistoryManager().getSelectedIds();
        if (WidgetUtil.isNotEmpty(selectedFromHistory)) {
            selectedIds.addAll(selectedFromHistory);
        } else if (WidgetUtil.isNotEmpty(collectionPluginData.getChosenIds())) {
            selectedIds.addAll(collectionPluginData.getChosenIds());
        }
        return selectedIds;

    }

    public void clearHandlers() {
        for (com.google.web.bindery.event.shared.HandlerRegistration registration : handlerRegistrations) {
            registration.removeHandler();
        }
        columnHeaderController.clearHandlers();
    }

    public void resetBodyHeight() {
        tableBody.getScrollPanel().getParent().getElement().getStyle().clearHeight();
    }

    private CollectionPlugin getPlugin() {
        return (CollectionPlugin) plugin;
    }

    public Map<String, List<String>> getFiltersMap() {
        return filtersMap;
    }

    public void setFiltersMap(Map<String, List<String>> filtersMap) {
        this.filtersMap = filtersMap;
    }

    public EventBus getEventBus() {
        return eventBus;
    }
}

