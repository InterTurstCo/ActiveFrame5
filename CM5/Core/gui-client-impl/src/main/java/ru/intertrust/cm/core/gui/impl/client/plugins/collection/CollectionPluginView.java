package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SetSelectionModel;
import com.google.web.bindery.event.shared.EventBus;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.config.gui.navigation.FilterPanelConfig;
import ru.intertrust.cm.core.config.gui.navigation.InitialFiltersConfig;
import ru.intertrust.cm.core.config.gui.navigation.SortCriteriaConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.history.HistoryItem;
import ru.intertrust.cm.core.gui.api.client.history.HistoryManager;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.event.CheckBoxFieldUpdateEvent;
import ru.intertrust.cm.core.gui.impl.client.event.CollectionPluginResizeBySplitterEvent;
import ru.intertrust.cm.core.gui.impl.client.event.CollectionPluginResizeBySplitterEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.CollectionRowSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.ComponentOrderChangedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.ComponentOrderChangedHandler;
import ru.intertrust.cm.core.gui.impl.client.event.ComponentWidthChangedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.ComponentWidthChangedHandler;
import ru.intertrust.cm.core.gui.impl.client.event.DeleteCollectionRowEvent;
import ru.intertrust.cm.core.gui.impl.client.event.DeleteCollectionRowEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.FilterEvent;
import ru.intertrust.cm.core.gui.impl.client.event.FilterEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.SaveToCsvEvent;
import ru.intertrust.cm.core.gui.impl.client.event.SaveToCsvEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.SimpleSearchEvent;
import ru.intertrust.cm.core.gui.impl.client.event.SimpleSearchEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.UpdateCollectionEvent;
import ru.intertrust.cm.core.gui.impl.client.event.UpdateCollectionEventHandler;
import ru.intertrust.cm.core.gui.impl.client.history.UserSettingsObject;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.CheckedSelectionModel;
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
import ru.intertrust.cm.core.gui.model.form.widget.CollectionRowItemList;
import ru.intertrust.cm.core.gui.model.plugin.CollectionPluginData;
import ru.intertrust.cm.core.gui.model.plugin.CollectionRowItem;
import ru.intertrust.cm.core.gui.model.plugin.CollectionRowsRequest;
import ru.intertrust.cm.core.gui.model.util.UserSettingsHelper;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.CHECK_BOX_COLUMN_NAME;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.CHECK_BOX_MAX_WIDTH;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.CHECK_BOX_MIN_WIDTH;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.CLOSED;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.OPEN;
import static ru.intertrust.cm.core.gui.model.util.UserSettingsHelper.SORT_DIRECT_KEY;
import static ru.intertrust.cm.core.gui.model.util.UserSettingsHelper.SORT_FIELD_KEY;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 17/9/13
 *         Time: 12:05 PM
 */
public class CollectionPluginView extends PluginView {
    public static final int FETCHED_ROW_COUNT = 25;

    private CollectionDataGrid tableBody;
    private List<CollectionRowItem> items;
    private LinkedHashMap<String, CollectionColumnProperties> fieldPropertiesMap;
    private VerticalPanel root = new VerticalPanel();
    private String collectionName;
    private InitialFiltersConfig initialFiltersConfig;
    private int listCount;
    private int tableWidth;
    private boolean singleChoice = true;
    private SortCollectionState sortCollectionState;
    private ToggleButton filterButton = new ToggleButton();
    private HandlerRegistration scrollHandlerRegistration;
    private Map<String, List<String>> filtersMap = new HashMap<String, List<String>>();
    private String simpleSearchQuery = "";
    private String searchArea = "";
    private CollectionColumnHeaderController headerController;
    private int lastScrollPos = -1;
    // локальная шина событий
    private EventBus eventBus;
    private CollectionCsvController csvController;
    private CollectionColumn<CollectionRowItem, Boolean> checkColumn;
    private SetSelectionModel<CollectionRowItem> selectionModel;
    private FilterPanelConfig filterPanelConfig;


    protected CollectionPluginView(CollectionPlugin plugin) {
        super(plugin);
        this.eventBus = plugin.getLocalEventBus();
        DataGrid.Resources resources = GlobalThemesManager.getDataGridResources();
        tableBody = new CollectionDataGrid(15, resources, eventBus);
        tableWidth = plugin.getOwner().getVisibleWidth();
        final CollectionColumnWidthChangedHandler changedWidthHandler = new CollectionColumnWidthChangedHandler();
        eventBus.addHandler(ComponentWidthChangedEvent.TYPE, changedWidthHandler);
        final CollectionColumnOrderChangedHandler orderChangedHandler = new CollectionColumnOrderChangedHandler();
        eventBus.addHandler(ComponentOrderChangedEvent.TYPE, orderChangedHandler);
    }

    private void updateSizes() {
        tableWidth = plugin.getOwner().getVisibleWidth();
        CollectionDataGridUtils.adjustColumnsWidth(tableWidth, tableBody);

    }

    @Override
    public IsWidget getViewWidget() {
        CollectionPluginData collectionPluginData = plugin.getInitialData();
        initialFiltersConfig = collectionPluginData.getInitialFiltersConfig();
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
            for(CollectionRowItem item : items) {
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
        headerController = new CollectionColumnHeaderController();
        if (singleChoice) {
            createTableColumnsWithoutCheckBoxes(fieldPropertiesMap);
        } else {
            createTableColumnsWithCheckBoxes(fieldPropertiesMap);
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
                headerController.setFocus();
                headerController.updateFilterValues();

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
                sortCollectionState = new SortCollectionState(0, FETCHED_ROW_COUNT, dataStoreName, ascending, false, field);
                final HistoryManager historyManager = Application.getInstance().getHistoryManager();
                historyManager.addHistoryItems(getCollectionIdentifier(),
                        new HistoryItem(HistoryItem.Type.PLUGIN_CONDITION, SORT_FIELD_KEY, field),
                        new HistoryItem(HistoryItem.Type.PLUGIN_CONDITION, SORT_DIRECT_KEY, Boolean.toString(ascending)));
                clearAllTableData();
                scroll.scrollToTop();

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
                createCollectionData();
            }
        });
        //показать/спрятать панель поиска в таблицы
        filterButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                headerController.changeFiltersInputsVisibility(filterButton.getValue());

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
            }
        });


        // экспорт в csv
        eventBus.addHandler(SaveToCsvEvent.TYPE, new SaveToCsvEventHandler() {
            @Override
            public void saveToCsv(SaveToCsvEvent saveToCsvEvent) {

                JSONObject requestObj = new JSONObject();
                int rowCount = items.size();
                JsonUtil.prepareJsonAttributes(requestObj, collectionName, simpleSearchQuery, searchArea, rowCount);
                JsonUtil.prepareJsonSortCriteria(requestObj, fieldPropertiesMap, sortCollectionState);
                JsonUtil.prepareJsonColumnProperties(requestObj, fieldPropertiesMap, filtersMap);
                JsonUtil.prepareJsonInitialFilters(requestObj, initialFiltersConfig);
                csvController.doPostRequest(requestObj.toString());

            }
        });

    }

    private void onKeyEnterPressed() {
        filtersMap.clear();
        for (CollectionColumnHeader header : headerController.getHeaders()) {
            List<String> filterValues = header.getHeaderWidget().getFilterValues();
            if (filterValues != null) {
                filtersMap.put(header.getHeaderWidget().getFieldName(), filterValues);

            }
        }
        clearAllTableData();
    }

    private void onKeyEscapePressed() {
        filterButton.setValue(false);
        headerController.clearFilters();
        headerController.changeFiltersInputsVisibility(false);
        lastScrollPos = 0;
        filtersMap.clear();
        clearAllTableData();

    }

    private void clearAllTableData() {
        items.clear();
        listCount = 0;
        lastScrollPos = 0;
        collectionData();
    }

    private void collectionData() {
        if (sortCollectionState == null) {
            createCollectionData();
        } else {
            createSortedCollectionData();
        }
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
        AbsolutePanel containerForToolbar = new AbsolutePanel();
        containerForToolbar.addStyleName("search-header");
        if (searchArea != null && searchArea.length() > 0) {
            FlowPanel simpleSearch = new FlowPanel();
            SimpleSearchPanel simpleSearchPanel = new SimpleSearchPanel(simpleSearch, eventBus);
            treeLinkWidget.add(simpleSearchPanel);

        }

        filterButton.removeStyleName("gwt-Button");
        filterButton.removeStyleName("gwt-ToggleButton");
        filterButton.removeStyleName("gwt-ToggleButton-Up");
        filterButton.addStyleName("show-filter-button");
        root.add(treeLinkWidget);
        root.add(tableBody);

    }

    private void createTableColumnsWithCheckBoxes(
            final LinkedHashMap<String, CollectionColumnProperties> domainObjectFieldsOnColumnNamesMap) {
        checkColumn = new CollectionColumn<CollectionRowItem, Boolean>(
                new CheckboxCell(true, true)) {
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
        checkColumn.setMinWidth(CHECK_BOX_MIN_WIDTH);
        checkColumn.setResizable(false);
        checkColumn.setMoveable(false);
        tableBody.addColumn(checkColumn);
        checkColumn.setDataStoreName(CHECK_BOX_COLUMN_NAME);
        createTableColumnsWithoutCheckBoxes(domainObjectFieldsOnColumnNamesMap);
    }


    private void createTableColumnsWithoutCheckBoxes(
            LinkedHashMap<String, CollectionColumnProperties> domainObjectFieldPropertiesMap) {
        final UserSettingsObject userSettingsForColumn =
                checkUpdates(getUserSettingsObjectForColumns(), domainObjectFieldPropertiesMap.keySet());
        final JSONObject jsonObject = new JSONObject(userSettingsForColumn);
        List<CollectionColumnHeader> headers = new ArrayList<>();
        for (String field : jsonObject.keySet()) {
            final CollectionColumnProperties columnProperties = domainObjectFieldPropertiesMap.get(field);
            final CollectionColumn column = ColumnFormatter.createFormattedColumn(columnProperties);
            final ColumnSettingsObject columnSettingsObject = userSettingsForColumn.getAttr(field).cast();
            if (columnSettingsObject != null && columnSettingsObject.getWidth() > 0) {
                column.setWidth(columnSettingsObject.getWidth());
            }
            List<String> initialFilterValues = (List<String>) columnProperties.getProperty(CollectionColumnProperties.INITIAL_FILTER_VALUES);
            HeaderWidget headerWidget = HeaderWidgetFactory.getInstance(column, columnProperties, initialFilterValues);
            CollectionColumnHeader collectionColumnHeader = new CollectionColumnHeader(tableBody, column, headerWidget, eventBus);
            headers.add(collectionColumnHeader);
            tableBody.addColumn(column, collectionColumnHeader);
            SortedMarker sortedMarker = (SortedMarker) columnProperties.getProperty(CollectionColumnProperties.SORTED_MARKER);
            if (sortedMarker != null) {
                boolean ascending = sortedMarker.isAscending();
                column.setDefaultSortAscending(ascending);
                tableBody.getColumnSortList().push(new ColumnSortList.ColumnSortInfo(column, ascending));
                sortCollectionState = new SortCollectionState(0, FETCHED_ROW_COUNT, column.getDataStoreName(), ascending, true, field);
            }
        }
        headerController.setHeaders(headers);
        CollectionDataGridUtils.adjustColumnsWidth(tableWidth, tableBody);
        String panelStatus = getPanelState();
        if (panelStatus.equalsIgnoreCase(OPEN)) {
            headerController.changeFiltersInputsVisibility(true);
            filterButton.setValue(true);
            headerController.updateFilterValues();
        }
    }

    private UserSettingsObject getUserSettingsObjectForColumns() {
        final String columnSettingsAsString = Application.getInstance().getHistoryManager()
                .getValue(getCollectionIdentifier(), UserSettingsHelper.COLUMN_SETTINGS_KEY);
        UserSettingsObject result = UserSettingsObject.createObject().cast();
        if (columnSettingsAsString != null && !columnSettingsAsString.isEmpty()) {
            try {
                result = JSONParser.parseStrict(columnSettingsAsString).isObject().getJavaScriptObject().cast();
            } catch (Exception ignored) {}
        }
        return result;
    }

    private String getPanelState() {
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

    private void createCollectionData() {
        CollectionRowsRequest collectionRowsRequest = new CollectionRowsRequest(listCount, FETCHED_ROW_COUNT, collectionName,
                fieldPropertiesMap, simpleSearchQuery, searchArea);
        collectionRowsRequest.setFiltersMap(filtersMap);
        collectionRowsRequest.setInitialFiltersConfig(initialFiltersConfig);
        collectionRowRequestCommand(collectionRowsRequest);
    }

    private void createSortedCollectionData() {
        CollectionRowsRequest collectionRowsRequest;
        String field = sortCollectionState.getField();
        boolean ascending = sortCollectionState.isAscend();
        CollectionColumnProperties collectionColumnProperties = fieldPropertiesMap.get(field);
        if (sortCollectionState.isResetCollection()) {
            items.clear();
            collectionRowsRequest = new CollectionRowsRequest(sortCollectionState.getOffset(), FETCHED_ROW_COUNT, collectionName, fieldPropertiesMap,
                    sortCollectionState.isAscend(), sortCollectionState.getColumnName(), field);
            sortCollectionState.setResetCollection(false);
            listCount = 0;

        } else {
            collectionRowsRequest = new CollectionRowsRequest(listCount, FETCHED_ROW_COUNT, collectionName, fieldPropertiesMap, ascending,
                    sortCollectionState.getColumnName(), sortCollectionState.getField());

        }
        SortCriteriaConfig sortCriteriaConfig = ascending ? collectionColumnProperties.getAscSortCriteriaConfig()
                : collectionColumnProperties.getDescSortCriteriaConfig();
        collectionRowsRequest.setSortCriteriaConfig(sortCriteriaConfig);
        collectionRowsRequest.setFiltersMap(filtersMap);
        collectionRowsRequest.setInitialFiltersConfig(initialFiltersConfig);
        collectionRowRequestCommand(collectionRowsRequest);

    }

    private void collectionRowRequestCommand(CollectionRowsRequest collectionRowsRequest) {
        lastScrollPos = 0;
        scrollHandlerRegistration.removeHandler();
        Command command = new Command("generateCollectionRowItems", "collection.plugin", collectionRowsRequest);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining generateTableRowsForPluginInitialization for ''");
                caught.printStackTrace();
            }

            @Override
            public void onSuccess(Dto result) {
                CollectionRowItemList collectionRowItemList = (CollectionRowItemList) result;
                List<CollectionRowItem> collectionRowItems = collectionRowItemList.getCollectionRows();
                headerController.saveFilterValues();
                insertMoreRows(collectionRowItems);
                tableBody.flush();
                final ScrollPanel scroll = tableBody.getScrollPanel();
                scrollHandlerRegistration = scroll.addScrollHandler(new ScrollLazyLoadHandler());
                headerController.setFocus();
                headerController.updateFilterValues();
            }
        });
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
                CollectionRowItemList collectionRowItemList = (CollectionRowItemList) result;
                List<CollectionRowItem> collectionRowItems = collectionRowItemList.getCollectionRows();
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

                collectionData();
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
        return ((CollectionPluginData) plugin.getInitialData()).getCollectionName();
    }

    private UserSettingsObject checkUpdates(UserSettingsObject userSettingsObject, Collection<String> configFields) {
        final JSONObject jsonObject = new JSONObject(userSettingsObject);
        final List<String> historyFields = new ArrayList<>();
        for (Iterator<String> it = jsonObject.keySet().iterator(); it.hasNext();) {
            historyFields.add(it.next());
        }
        boolean updated = false;
        final List<String> copyConfigList = new ArrayList<>(configFields);
        copyConfigList.removeAll(historyFields);
        if (!copyConfigList.isEmpty()) {
            historyFields.addAll(copyConfigList);
            updated = true;
        }
        for (Iterator<String> it = historyFields.iterator(); it.hasNext();) {
            if (!configFields.contains(it.next())) {
                it.remove();
                updated = true;
            }
        }
        if (updated) {
            UserSettingsObject result = JavaScriptObject.createObject().cast();
            for (String field: historyFields) {
                result.setAttr(field, getColumnSettingsObject(userSettingsObject, field));
            }
            final HistoryItem item = new HistoryItem(HistoryItem.Type.USER_INTERFACE,
                    UserSettingsHelper.COLUMN_SETTINGS_KEY, new JSONObject(result).toString());
            Application.getInstance().getHistoryManager().addHistoryItems(getCollectionIdentifier(), item);
            return result;
        } else {
            return userSettingsObject;
        }
    }

    private static ColumnSettingsObject getColumnSettingsObject(final UserSettingsObject userSettingsObject, final String key) {
        ColumnSettingsObject result = userSettingsObject.getAttr(key).cast();
        if (result == null) {
            result = ColumnSettingsObject.createObject();
            userSettingsObject.setAttr(key, result);
        }
        return result;
    }

    private class CollectionColumnWidthChangedHandler implements ComponentWidthChangedHandler {
        @Override
        public void handleEvent(ComponentWidthChangedEvent event) {
            if (event.getComponent() instanceof CollectionColumn) {
                final UserSettingsObject userSettingsObject = getUserSettingsObjectForColumns();
                final String field = ((CollectionColumn) event.getComponent()).getFieldName();
                final ColumnSettingsObject columnSettingsObject = getColumnSettingsObject(userSettingsObject, field);
                columnSettingsObject.setWidth(event.getWidth());
                final HistoryItem item = new HistoryItem(HistoryItem.Type.USER_INTERFACE,
                        UserSettingsHelper.COLUMN_SETTINGS_KEY, new JSONObject(userSettingsObject).toString());
                Application.getInstance().getHistoryManager().addHistoryItems(getCollectionIdentifier(), item);
            }
        }
    }

    private class CollectionColumnOrderChangedHandler implements ComponentOrderChangedHandler {
        @Override
        public void handleEvent(ComponentOrderChangedEvent event) {
            if (event.getComponent() instanceof CollectionColumn) {
                final UserSettingsObject currentSettingsObject = getUserSettingsObjectForColumns();
                final UserSettingsObject newSettingsObject = UserSettingsObject.createObject().cast();
                for (int index = 0; index < tableBody.getColumnCount(); index++) {
                    final CollectionColumn column = (CollectionColumn) tableBody.getColumn(index);
                    final ColumnSettingsObject columnSettingsObject =
                            getColumnSettingsObject(currentSettingsObject, column.getFieldName());
                    newSettingsObject.setAttr(column.getFieldName(), columnSettingsObject);
                }
                final HistoryItem item = new HistoryItem(HistoryItem.Type.USER_INTERFACE,
                        UserSettingsHelper.COLUMN_SETTINGS_KEY, new JSONObject(newSettingsObject).toString());
                Application.getInstance().getHistoryManager().addHistoryItems(getCollectionIdentifier(), item);
            }
        }
    }
}

