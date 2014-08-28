package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SetSelectionModel;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.form.widget.TableBrowserParams;
import ru.intertrust.cm.core.config.gui.form.widget.filter.AbstractFilterConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.ParamConfig;
import ru.intertrust.cm.core.config.gui.navigation.*;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.api.client.history.HistoryManager;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.action.Action;
import ru.intertrust.cm.core.gui.impl.client.action.system.CollectionColumnWidthAction;
import ru.intertrust.cm.core.gui.impl.client.event.*;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.CheckedSelectionModel;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.ColumnHeaderBlock;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.header.CollectionColumnHeader;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.header.CollectionColumnHeaderController;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.header.widget.HeaderWidget;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.header.widget.HeaderWidgetFactory;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;
import ru.intertrust.cm.core.gui.impl.client.util.CollectionDataGridUtils;
import ru.intertrust.cm.core.gui.impl.client.util.JsonUtil;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.SortedMarker;
import ru.intertrust.cm.core.gui.model.action.system.CollectionColumnOrderActionContext;
import ru.intertrust.cm.core.gui.model.action.system.CollectionColumnWidthActionContext;
import ru.intertrust.cm.core.gui.model.action.system.CollectionFiltersActionContext;
import ru.intertrust.cm.core.gui.model.action.system.CollectionSortOrderActionContext;
import ru.intertrust.cm.core.gui.model.form.widget.CollectionRowsResponse;
import ru.intertrust.cm.core.gui.model.plugin.CollectionPluginData;
import ru.intertrust.cm.core.gui.model.plugin.CollectionRowItem;
import ru.intertrust.cm.core.gui.model.plugin.CollectionRowsRequest;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.*;

import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.*;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 17/9/13
 *         Time: 12:05 PM
 */
public class CollectionPluginView extends PluginView {

    private CollectionDataGrid tableBody;
    private List<CollectionRowItem> items;
    private LinkedHashMap<String, CollectionColumnProperties> fieldPropertiesMap;
    private VerticalPanel root = new VerticalPanel();
    private String collectionName;
    private int listCount;
    private int tableWidth;
    private boolean singleChoice = true;
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
    private FilterPanelConfig filterPanelConfig;
    private int rowsChunk;

    protected CollectionPluginView(CollectionPlugin plugin) {
        super(plugin);
        this.eventBus = plugin.getLocalEventBus();
        DataGrid.Resources resources = GlobalThemesManager.getDataGridResources();
        tableBody = new CollectionDataGrid(15, resources, eventBus);
        tableWidth = plugin.getOwner().getVisibleWidth();
        final CollectionColumnWidthChangedHandler changedWidthHandler = new CollectionColumnWidthChangedHandler();
        eventBus.addHandler(ComponentWidthChangedEvent.TYPE, changedWidthHandler);
        final CollectionColumnOrderChangedHandler orderChangedHandler = new CollectionColumnOrderChangedHandler();
        columnHeaderController =
                new CollectionColumnHeaderController(getCollectionIdentifier(), tableBody, tableWidth, eventBus);
        eventBus.addHandler(ComponentOrderChangedEvent.TYPE, orderChangedHandler);
    }

    private void updateSizes() {
        tableWidth = plugin.getOwner().getVisibleWidth();
        CollectionDataGridUtils.adjustColumnsWidth(tableWidth, tableBody);
        columnHeaderController.setDisplayedWidth(tableWidth);
    }

    /*This method is invoked when splitter changes position and after initialization of BusinessUniverse
        so we have to check if scroll is visible. If no load more rows
     */
    public void fetchMoreItemsIfRequired() {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            public void execute() {
                int scrollMinVertical = tableBody.getScrollPanel().getMinimumVerticalScrollPosition();
                int scrollMaxVertical = tableBody.getScrollPanel().getMaximumVerticalScrollPosition();
                if (scrollMinVertical == scrollMaxVertical) {
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
        rowsChunk = collectionPluginData.getRowsChunk();
        collectionName = collectionPluginData.getCollectionName();
        fieldPropertiesMap = collectionPluginData.getDomainObjectFieldPropertiesMap();
        singleChoice = collectionPluginData.isSingleChoice();
        searchArea = collectionPluginData.getSearchArea();
        items = collectionPluginData.getItems();
        filterPanelConfig = collectionPluginData.getFilterPanelConfig();
        init();

        final List<Id> selectedIds = new ArrayList<>();
        final List<Id> selectedFromHistory = Application.getInstance().getHistoryManager().getSelectedIds();
        if (!selectedFromHistory.isEmpty()) {
            selectedIds.addAll(selectedFromHistory);
        } else {
            selectedIds.addAll(collectionPluginData.getChosenIds());
        }
        if (!selectedIds.isEmpty()) {
            for (CollectionRowItem item : items) {
                if (selectedIds.contains(item.getId())) {
                    selectionModel.setSelected(item, true);
                }
            }
        }
        Application.getInstance().getHistoryManager().setSelectedIds(selectedIds.toArray(new Id[selectedIds.size()]));
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
        insertRows(items);
        applyBodyTableStyle();
        csvController = new CollectionCsvController(root);

    }

    public List<Id> getSelectedIds() {
        final List<Id> selectedIds = new ArrayList<>(selectionModel.getSelectedSet().size());
        for (CollectionRowItem item : selectionModel.getSelectedSet()) {
            selectedIds.add(item.getId());
        }
        return selectedIds;
    }


    public boolean restoreHistory() {
        final HistoryManager manager = Application.getInstance().getHistoryManager();
        final List<Id> selectedFromHistoryIds = manager.getSelectedIds();
        final List<Id> selectedIds = getSelectedIds();
        final Id oldSelectedFormId = selectedIds.isEmpty() ? null : selectedIds.get(0);
        Id newSelectedFormId = null;
        selectionModel.clear();
        if (!selectedFromHistoryIds.isEmpty()) {
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
        if (singleChoice) {
            createTableColumnsWithoutCheckBoxes(fieldPropertiesMap, columnHeaderBlocks);
        } else {
            createTableColumnsWithCheckBoxes(fieldPropertiesMap, columnHeaderBlocks);
        }
    }

    public void onPluginPanelResize() {
        updateSizes();

    }

    private void addHandlers() {

        eventBus.addHandler(CollectionPluginResizeBySplitterEvent.TYPE, new CollectionPluginResizeBySplitterEventHandler() {
            @Override
            public void onCollectionPluginResizeBySplitter(CollectionPluginResizeBySplitterEvent event) {
                tableBody.redraw();
                columnHeaderController.setFocus();
                columnHeaderController.updateFilterValues();
                tableBody.flush();
                fetchMoreItemsIfRequired();
            }
        });

        // обработчик обновления коллекции (строки в таблице)
        eventBus.addHandler(UpdateCollectionEvent.TYPE, new UpdateCollectionEventHandler() {
            @Override
            public void updateCollection(UpdateCollectionEvent event) {
                refreshCollection(event.getIdentifiableObject());
            }
        });

        // обработчик удаления элемента коллекции (строки в таблице)
        eventBus.addHandler(DeleteCollectionRowEvent.TYPE, new DeleteCollectionRowEventHandler() {
            @Override
            public void deleteCollectionRow(DeleteCollectionRowEvent event) {
                delCollectionRow(event.getId());
            }
        });
        final ScrollPanel scroll = tableBody.getScrollPanel();
        tableBody.addColumnSortHandler(new ColumnSortEvent.Handler() {
            @Override
            public void onColumnSort(ColumnSortEvent event) {

                CollectionColumn column = (CollectionColumn) event.getColumn();
                String dataStoreName = column.getDataStoreName();
                boolean ascending = event.isSortAscending();
                String field = column.getFieldName();
                sortCollectionState = new SortCollectionState(0, rowsChunk, dataStoreName, ascending, false, field);
                clearAllTableData();
                final CollectionViewerConfig collectionViewerConfig = (CollectionViewerConfig) plugin.getConfig();
                collectionViewerConfig.getDefaultSortCriteriaConfig().setColumnField(field);
                collectionViewerConfig.getDefaultSortCriteriaConfig().setOrder(
                        ascending ? CommonSortCriterionConfig.ASCENDING : CommonSortCriterionConfig.DESCENDING);
                final CollectionSortOrderActionContext context = new CollectionSortOrderActionContext();
                context.setActionConfig(createActionConfig());
                context.setLink(Application.getInstance().getHistoryManager().getLink());
                context.setCollectionViewName(getCollectionIdentifier());
                context.setCollectionViewerConfig(collectionViewerConfig);
                final Action action = ComponentRegistry.instance.get(CollectionSortOrderActionContext.COMPONENT_NAME);
                action.setInitialContext(context);
                action.perform();
            }
        });

        scrollHandlerRegistration = scroll.addScrollHandler(new ScrollLazyLoadHandler());
        eventBus.addHandler(SimpleSearchEvent.TYPE, new SimpleSearchEventHandler() {
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
        });
        //показать/спрятать панель поиска в таблицы
        filterButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                columnHeaderController.changeFiltersInputsVisibility(filterButton.getValue());

            }
        });

        //событие которое инициализирует поиск по колонкам таблицы
        eventBus.addHandler(FilterEvent.TYPE, new FilterEventHandler() {
            @Override
            public void onFilterEvent(FilterEvent event) {
                boolean filterCanceled = event.isFilterCanceled();
                if (filterCanceled) {
                    onKeyEscapePressed();
                } else {
                    onKeyEnterPressed();
                }
                updateFilterConfig();
                clearAllTableData();
            }
        });


        // экспорт в csv
        eventBus.addHandler(SaveToCsvEvent.TYPE, new SaveToCsvEventHandler() {
            @Override
            public void saveToCsv(SaveToCsvEvent saveToCsvEvent) {
                final InitialFiltersConfig initialFiltersConfig =
                        ((CollectionViewerConfig) plugin.getConfig()).getInitialFiltersConfig();
                JSONObject requestObj = new JSONObject();
                JsonUtil.prepareJsonAttributes(requestObj, collectionName, simpleSearchQuery, searchArea);
                JsonUtil.prepareJsonSortCriteria(requestObj, fieldPropertiesMap, sortCollectionState);
                JsonUtil.prepareJsonColumnProperties(requestObj, fieldPropertiesMap, filtersMap);
                JsonUtil.prepareJsonInitialFilters(requestObj, initialFiltersConfig);
                csvController.doPostRequest(requestObj.toString());

            }
        });

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
    }

    private void onKeyEscapePressed() {
        filterButton.setValue(false);
        columnHeaderController.clearFilters();
        columnHeaderController.changeFiltersInputsVisibility(false);
        lastScrollPos = 0;
        filtersMap.clear();
    }

    private void updateFilterConfig() {
        final CollectionViewerConfig config = (CollectionViewerConfig) plugin.getConfig();
        final List<AbstractFilterConfig> configs = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : filtersMap.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                final InitialFilterConfig initialFilterConfig = new InitialFilterConfig();
                initialFilterConfig.setName((String) fieldPropertiesMap.get(
                        entry.getKey()).getProperty(CollectionColumnProperties.SEARCH_FILTER_KEY));
                final List<ParamConfig> paramConfigs = new ArrayList<>();
                for (int index = 0; index < entry.getValue().size(); index++) {
                    final ParamConfig paramConfig = new ParamConfig();
                    paramConfig.setName(Integer.valueOf(index));
                    final String paramValue = entry.getValue().get(index).trim();
                    if (!paramValue.isEmpty()) {
                        paramConfig.setValue(entry.getValue().get(index));
                        paramConfig.setType((String) fieldPropertiesMap.get(
                                entry.getKey()).getProperty(CollectionColumnProperties.TYPE_KEY));
                        paramConfigs.add(paramConfig);
                    }
                }
                if (!paramConfigs.isEmpty()) {
                    initialFilterConfig.setParamConfigs(paramConfigs);
                    configs.add(initialFilterConfig);
                }
            }
        }
        if (configs.isEmpty()) {
            config.setInitialFiltersConfig(null);
        } else {
            final InitialFiltersConfig initialFiltersConfig = new InitialFiltersConfig();
            initialFiltersConfig.setAbstractFilterConfigs(configs);
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
        lastScrollPos = 0;
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
    private void delCollectionRow(Id collectionObject) {
        if (items.isEmpty()) {
            return;  // если в коллекции не было элементов операция удаления ни к чему не приводит
        }
        // ищем какой элемент коллекции должен быть удален
        int index = getSelectedIndex(collectionObject);
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
        Set<Id> includedIds = new HashSet<>();
        includedIds.add(collectionObject.getId());

        final CollectionRowsRequest collectionRowsRequest =
                new CollectionRowsRequest(0, 1, collectionName, fieldPropertiesMap, simpleSearchQuery, searchArea);
        collectionRowsRequest.setIncludedIds(includedIds);
        collectionOneRowRequestCommand(collectionRowsRequest);

    }

    private void buildPanel() {
        AbsolutePanel treeLinkWidget = new AbsolutePanel();
        treeLinkWidget.addStyleName("collection-plugin-view-container");
        treeLinkWidget.add(filterButton);
        final Button columnManagerButton = new Button();
        columnManagerButton.setStyleName(GlobalThemesManager.getCurrentTheme().commonCss().columnSettingsButton());
        columnManagerButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                columnHeaderController.showPopup(columnManagerButton);
            }
        });
        treeLinkWidget.add(columnManagerButton);
        AbsolutePanel containerForToolbar = new AbsolutePanel();
        containerForToolbar.addStyleName("search-header");
        if (searchArea != null && !searchArea.isEmpty()) {
            FlowPanel simpleSearch = new FlowPanel();
            SimpleSearchPanel simpleSearchPanel = new SimpleSearchPanel(simpleSearch, eventBus);
            treeLinkWidget.add(simpleSearchPanel);
        }
        filterButton.setStyleName("show-filter-button");
        root.add(treeLinkWidget);
        if (plugin.isShowBreadcrumbs()) {
            Panel breadCrumbsPanel = createBreadCrumbsPanel();
            if (breadCrumbsPanel != null) {
                root.add(breadCrumbsPanel);
            }
        }
        root.add(tableBody);

    }

    private void createTableColumnsWithCheckBoxes(
            final LinkedHashMap<String, CollectionColumnProperties> domainObjectFieldsOnColumnNamesMap, List<ColumnHeaderBlock> columnHeaderBlocks) {
        final CollectionColumn<CollectionRowItem, Boolean> checkColumn =
                new CollectionColumn<CollectionRowItem, Boolean>(new CheckboxCell(true, true)) {
                    @Override
                    public Boolean getValue(CollectionRowItem object) {
                        return selectionModel.isSelected(object);
                    }
                };

        checkColumn.setFieldUpdater(new FieldUpdater<CollectionRowItem, Boolean>() {
            @Override
            public void update(int index, CollectionRowItem object, Boolean value) {
                eventBus.fireEvent(new CheckBoxFieldUpdateEvent(object.getId(), !value));
            }
        });
        checkColumn.setMaxWidth(CHECK_BOX_MAX_WIDTH);
        checkColumn.setMinWidth(CHECK_BOX_MAX_WIDTH);
        checkColumn.setUserWidth(CHECK_BOX_MAX_WIDTH);
        checkColumn.setVisible(true);
        checkColumn.setResizable(false);
        checkColumn.setMoveable(false);
        tableBody.addColumn(checkColumn);
        checkColumn.setDataStoreName(CHECK_BOX_COLUMN_NAME);
        HeaderWidget headerWidget = HeaderWidgetFactory.getInstance(checkColumn, null, null);
        CollectionColumnHeader collectionColumnHeader = new CollectionColumnHeader(tableBody, checkColumn, headerWidget, eventBus);
        ColumnHeaderBlock columnHeaderBlock = new ColumnHeaderBlock(collectionColumnHeader, checkColumn);
        columnHeaderBlocks.add(columnHeaderBlock);
        createTableColumnsWithoutCheckBoxes(domainObjectFieldsOnColumnNamesMap, columnHeaderBlocks);
    }


    private void createTableColumnsWithoutCheckBoxes(
            LinkedHashMap<String, CollectionColumnProperties> domainObjectFieldPropertiesMap, List<ColumnHeaderBlock> columnHeaderBlocks) {

        for (String field : domainObjectFieldPropertiesMap.keySet()) {
            final CollectionColumnProperties columnProperties = domainObjectFieldPropertiesMap.get(field);
            final CollectionColumn column = ColumnFormatter.createFormattedColumn(columnProperties);
            int index = tableBody.getColumnCount();
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
                sortCollectionState = new SortCollectionState(0, rowsChunk, column.getDataStoreName(), ascending, true, field);
            }
        }

        columnHeaderController.setColumnHeaderBlocks(columnHeaderBlocks);
        columnHeaderController.changeVisibilityOfColumns();
        String panelStatus = getPanelState();
        if (panelStatus.equalsIgnoreCase(OPEN)) {
            columnHeaderController.changeFiltersInputsVisibility(true);
            filterButton.setValue(true);
            columnHeaderController.updateFilterValues();
        }

    }

    private String getPanelState() {
        final InitialFiltersConfig initialFiltersConfig =
                ((CollectionViewerConfig) plugin.getConfig()).getInitialFiltersConfig();
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
        tableBody.setRowData(list);
        listCount = items.size();

    }

    private void insertMoreRows(List<CollectionRowItem> list) {
        items.addAll(list);
        tableBody.setRowData(items);

        listCount = items.size();

    }

    private void applySelectionModel() {
        if (singleChoice) {
            selectionModel = new CheckedSelectionModel<>();
        } else {
            selectionModel = new MultiSelectionModel<>();
        }
        tableBody.setSelectionModel(selectionModel);
    }

    private void applyBodyTableStyle() {
        String emptyTableText = "Результаты отсутствуют";
        HTML emptyTableWidget = new HTML("<br/><div align='center'> <h1> " + emptyTableText + " </h1> </div>");
        tableBody.setEmptyTableWidget(emptyTableWidget);
        tableBody.setSelectionModel(selectionModel);

    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    private CollectionRowsRequest createCollectionRowsRequest() {
        CollectionRowsRequest collectionRowsRequest = new CollectionRowsRequest(listCount, rowsChunk, collectionName,
                fieldPropertiesMap, simpleSearchQuery, searchArea);
        collectionRowsRequest.setFiltersMap(filtersMap);
        CollectionPluginData collectionPluginData = plugin.getInitialData();
        InitialFiltersConfig initialFiltersConfig = collectionPluginData.getInitialFiltersConfig();
        collectionRowsRequest.setInitialFiltersConfig(initialFiltersConfig);
        TableBrowserParams tableBrowserParams = collectionPluginData.getTableBrowserParams();
        collectionRowsRequest.setTableBrowserParams(tableBrowserParams);
        return collectionRowsRequest;

    }

    private CollectionRowsRequest createSortedCollectionRowsRequest() {
        CollectionRowsRequest collectionRowsRequest;
        String field = sortCollectionState.getField();
        boolean ascending = sortCollectionState.isAscend();
        CollectionColumnProperties collectionColumnProperties = fieldPropertiesMap.get(field);
        if (sortCollectionState.isResetCollection()) {
            items.clear();
            collectionRowsRequest = new CollectionRowsRequest(sortCollectionState.getOffset(), rowsChunk, collectionName, fieldPropertiesMap,
                    sortCollectionState.isAscend(), sortCollectionState.getColumnName(), field);
            sortCollectionState.setResetCollection(false);
            listCount = 0;

        } else {
            collectionRowsRequest = new CollectionRowsRequest(listCount, rowsChunk, collectionName, fieldPropertiesMap, ascending,
                    sortCollectionState.getColumnName(), sortCollectionState.getField());

        }
        SortCriteriaConfig sortCriteriaConfig = ascending ? collectionColumnProperties.getAscSortCriteriaConfig()
                : collectionColumnProperties.getDescSortCriteriaConfig();
        collectionRowsRequest.setSortCriteriaConfig(sortCriteriaConfig);
        collectionRowsRequest.setFiltersMap(filtersMap);
        CollectionPluginData collectionPluginData = plugin.getInitialData();
        InitialFiltersConfig initialFiltersConfig = collectionPluginData.getInitialFiltersConfig();
        collectionRowsRequest.setInitialFiltersConfig(initialFiltersConfig);
        TableBrowserParams tableBrowserParams = collectionPluginData.getTableBrowserParams();
        collectionRowsRequest.setTableBrowserParams(tableBrowserParams);
        return collectionRowsRequest;

    }

    public void clearScrollHandler() {
        lastScrollPos = 0;
        scrollHandlerRegistration.removeHandler();
    }

    private void collectionRowRequestCommand(final CollectionRowsRequest collectionRowsRequest) {

        clearScrollHandler();
        Command command = new Command("generateCollectionRowItems", "collection.plugin", collectionRowsRequest);
        Application.getInstance().showLoadingIndicator();
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining generateTableRowsForPluginInitialization for ''");
                caught.printStackTrace();
                Application.getInstance().hideLoadingIndicator();
            }

            @Override
            public void onSuccess(Dto result) {
                CollectionRowsResponse collectionRowsResponse = (CollectionRowsResponse) result;
                List<CollectionRowItem> collectionRowItems = collectionRowsResponse.getCollectionRows();
                handleCollectionRowsResponse(collectionRowItems, false);
                Application.getInstance().hideLoadingIndicator();
                if (collectionRowItems.size() < collectionRowsRequest.getLimit()) {
                    return;
                }
                fetchMoreItemsIfRequired();
            }
        });
    }

    public void handleCollectionRowsResponse(List<CollectionRowItem> collectionRowItems, boolean clearPreviousRows) {
        columnHeaderController.saveFilterValues();
        if (clearPreviousRows) {
            insertRows(collectionRowItems);
        } else {
            insertMoreRows(collectionRowItems);
        }
        tableBody.flush();
        final ScrollPanel scroll = tableBody.getScrollPanel();
        scrollHandlerRegistration = scroll.addScrollHandler(new ScrollLazyLoadHandler());
        columnHeaderController.setFocus();
        columnHeaderController.updateFilterValues();


    }

    private void collectionOneRowRequestCommand(CollectionRowsRequest collectionRowsRequest) {

        Command command = new Command("generateCollectionRowItems", "collection.plugin", collectionRowsRequest);
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
                if (collectionRowItems.isEmpty()) { //it happens when the updated item doesn't belong to the collection anymore
                    // перерисовка таблицы(коллекции)
                    tableBody.setRowData(items);
                    tableBody.redraw();
                    tableBody.flush();
                } else {
                    CollectionRowItem item = collectionRowItems.get(0);
                    int index = getSelectedIndex(item.getId());
                    // коллекция не пуста
                    if (items.size() >= 0) {
                        // признак вставки элемента в коллекцию
                        boolean inserted = false;
                        // если была выделена строка коллекции - снимаем выделение
                        for (CollectionRowItem i : items) {
                            if (selectionModel.isSelected(i)) {
                                selectionModel.setSelected(i, false);
                            }
                        }

                        // если был совпадающий по id элемент
                        if (index >= 0) {
                            try {
                                // замена элемента в коллекции
                                items.remove(index);
                                items.add(index, item);
                                selectionModel.setSelected(item, true);
                                inserted = true;
                                // обновляем таблицу
                                tableBody.setRowData(items);
                                tableBody.redraw();
                                tableBody.flush();
                            } catch (IndexOutOfBoundsException ie) {
                            }
                        }

                        // добавление нового элемента
                        if (!inserted) {

                            items.add(item);
                            selectionModel.clear();
                            selectionModel.setSelected(item, true);
                        }
                    }

                    // перерисовка таблицы(коллекции)
                    tableBody.setRowData(items);
                    tableBody.redraw();
                    tableBody.flush();
                    eventBus.fireEvent(new CollectionRowSelectedEvent(item.getId()));
                }
            }
        });
    }

    private class ScrollLazyLoadHandler implements ScrollHandler {
        private ScrollPanel scroll;

        private ScrollLazyLoadHandler() {
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

    private int getSelectedIndex(final Id id) {
        for (CollectionRowItem item : items) {
            if (item.getId().equals(id)) {
                return items.indexOf(item);
            }
        }
        return -1;
    }

    private String getCollectionIdentifier() {
        return ((CollectionPluginData) plugin.getInitialData()).getCollectionViewConfigName();
    }

    private class CollectionColumnWidthChangedHandler implements ComponentWidthChangedHandler {
        @Override
        public void handleEvent(ComponentWidthChangedEvent event) {
            if (event.getComponent() instanceof CollectionColumn) {
                updateWidthSettings(((CollectionColumn) event.getComponent()).getFieldName(), event.getWidth());
            }
        }
    }

    private class CollectionColumnOrderChangedHandler implements ComponentOrderChangedHandler {

        @Override
        public void handleEvent(ComponentOrderChangedEvent event) {
            if (event.getComponent() instanceof CollectionColumn) {
                final CollectionColumnOrderActionContext context = new CollectionColumnOrderActionContext();
                context.setLink(Application.getInstance().getHistoryManager().getLink());
                context.setCollectionViewName(getCollectionIdentifier());
                context.setActionConfig(createActionConfig());
                for (int index = 0; index < tableBody.getColumnCount(); index++) {
                    final CollectionColumn column = (CollectionColumn) tableBody.getColumn(index);
                    context.addOrder(column.fieldName);
                }
                final Action action = ComponentRegistry.instance.get(CollectionColumnOrderActionContext.COMPONENT_NAME);
                action.setInitialContext(context);
                action.perform();
            }
        }
    }

    private void updateWidthSettings(final String field, final int width) {
        final CollectionColumnWidthActionContext context = new CollectionColumnWidthActionContext();
        context.setActionConfig(createActionConfig());
        context.setLink(Application.getInstance().getHistoryManager().getLink());
        context.setCollectionViewName(getCollectionIdentifier());
        context.setField(field);
        context.setWidth(width + "px");
        CollectionColumnWidthAction action =
                ComponentRegistry.instance.get(CollectionColumnWidthActionContext.COMPONENT_NAME);
        action.setInitialContext(context);
        action.perform();
    }

    private ActionConfig createActionConfig() {
        final ActionConfig config = new ActionConfig();
        config.setDirtySensitivity(false);
        config.setImmediate(true);
        return config;
    }


}

