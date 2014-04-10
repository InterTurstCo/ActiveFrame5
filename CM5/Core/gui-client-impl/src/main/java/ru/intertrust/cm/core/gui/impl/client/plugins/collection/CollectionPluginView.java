package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SetSelectionModel;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.config.gui.navigation.SortCriteriaConfig;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.event.*;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.CheckedSelectionModel;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.CollectionColumnHeader;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.CollectionColumnHeaderController;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.HeaderWidget;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.resources.DataGridResourceAdapter;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.resources.DataGridResources;
import ru.intertrust.cm.core.gui.impl.client.util.CollectionDataGridUtils;
import ru.intertrust.cm.core.gui.impl.client.util.JsonUtil;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.SortedMarker;
import ru.intertrust.cm.core.gui.model.form.widget.CollectionRowItemList;
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
    public static final int FETCHED_ROW_COUNT = 25;
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
    private Map<String, String> filtersMap = new HashMap<String, String>();
    private String simpleSearchQuery = "";
    private String searchArea = "";
    private CollectionColumnHeaderController headerController;
    private int lastScrollPos;
    // локальная шина событий
    private EventBus eventBus;
    private CollectionCsvController csvController;
    private CollectionColumn<CollectionRowItem, Boolean> checkColumn;
    private SetSelectionModel<CollectionRowItem> selectionModel;
    /**
     * Создание стилей для ящеек таблицы
     */
    private final DataGridResourceAdapter adapter;

    protected CollectionPluginView(CollectionPlugin plugin) {
        super(plugin);
        this.eventBus = plugin.getLocalEventBus();
        adapter = new DataGridResourceAdapter(DataGridResources.I);
        tableBody = new CollectionDataGrid(15, adapter.getResources(), eventBus);
        tableWidth = plugin.getOwner().getVisibleWidth();

    }

    private void updateSizes() {
        tableWidth = plugin.getOwner().getVisibleWidth();
        CollectionDataGridUtils.addjustColumnsWidth(tableWidth, tableBody);

    }

    @Override
    public IsWidget getViewWidget() {
        CollectionPluginData collectionPluginData = plugin.getInitialData();
        collectionName = collectionPluginData.getCollectionName();
        fieldPropertiesMap = collectionPluginData.getDomainObjectFieldPropertiesMap();
        items = collectionPluginData.getItems();
        singleChoice = collectionPluginData.isSingleChoice();
        searchArea = collectionPluginData.getSearchArea();
        init();
        final List<Integer> selectedIndexes = collectionPluginData.getIndexesOfSelectedItems();
        for (int index : selectedIndexes) {
            selectionModel.setSelected(items.get(index), true);
        }
        root.addStyleName("collection-plugin-view-container");

        return root;
    }

    public void init() {
        buildPanel();
        createTableColumns();
        applySelectionModel();
        insertRows(items);
        applyBodyTableStyle();
        addHandlers();
        csvController = new CollectionCsvController(root);

    }

    public List<Id> getSelectedIds() {
        final List<Id> selectedIds = new ArrayList<Id>(selectionModel.getSelectedSet().size());
        for (CollectionRowItem item : selectionModel.getSelectedSet()) {
            selectedIds.add(item.getId());
        }
        return selectedIds;
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
                csvController.doPostRequest(requestObj.toString());

            }
        });

    }

    private void onKeyEnterPressed() {
        filtersMap.clear();
        for (CollectionColumnHeader header : headerController.getHeaders()) {
            String filterValue = header.getFilterValue();
            if (filterValue != null && filterValue.length() > 0) {
                filtersMap.put(header.getFieldName(), filterValue);

            }
        }
        clearAllTableData();
    }

    private void onKeyEscapePressed() {
        filterButton.setValue(false);
        headerController.changeFiltersInputsVisibility(false);
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
        int index = -1;
        for (CollectionRowItem i : items) {
            if (i.getId().toStringRepresentation().equalsIgnoreCase(collectionObject.toStringRepresentation())) {
                index = items.indexOf(i);
                break;
            }
        }
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
        Set<Id> includedIds = new HashSet<Id>();
        includedIds.add(collectionObject.getId());

        CollectionRowsRequest collectionRowsRequest = new CollectionRowsRequest(0, 1,
                collectionName, fieldPropertiesMap, simpleSearchQuery, searchArea);
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
        List<CollectionColumnHeader> headers = new ArrayList<CollectionColumnHeader>();
        for (String field : domainObjectFieldPropertiesMap.keySet()) {
            CollectionColumnProperties columnProperties = domainObjectFieldPropertiesMap.get(field);
            CollectionColumn column = ColumnFormatter.createFormattedColumn(columnProperties);
            HeaderWidget headerWidget = new HeaderWidget(column, columnProperties);
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
        CollectionDataGridUtils.addjustColumnsWidth(tableWidth, tableBody);
    }

    public void insertRows(List<CollectionRowItem> list) {
        tableBody.setRowData(list);
        listCount = items.size();

    }

    private void insertMoreRows(List<CollectionRowItem> list) {

        Set<CollectionRowItem> itemsSet = new LinkedHashSet<CollectionRowItem>(items);
        itemsSet.addAll(list);
        List<CollectionRowItem> itemList = new ArrayList<CollectionRowItem>(itemsSet);
        tableBody.setRowData(itemList);
        items = itemList;
        listCount = itemList.size();

    }

    private void applySelectionModel() {
        if (singleChoice) {
            selectionModel = new CheckedSelectionModel<CollectionRowItem>();
        } else {
            selectionModel = new MultiSelectionModel<CollectionRowItem>();
        }
        tableBody.setSelectionModel(selectionModel);
    }

    private void applyBodyTableStyle() {
        selectionModel = new CheckedSelectionModel<CollectionRowItem>();
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
            lastScrollPos = 0;
        } else {
            collectionRowsRequest = new CollectionRowsRequest(listCount, FETCHED_ROW_COUNT, collectionName, fieldPropertiesMap, ascending,
                    sortCollectionState.getColumnName(), sortCollectionState.getField());

        }
        SortCriteriaConfig sortCriteriaConfig = ascending ? collectionColumnProperties.getAscSortCriteriaConfig()
                : collectionColumnProperties.getDescSortCriteriaConfig();
        collectionRowsRequest.setSortCriteriaConfig(sortCriteriaConfig);
        collectionRowsRequest.setFiltersMap(filtersMap);
        collectionRowRequestCommand(collectionRowsRequest);

    }

    private void collectionRowRequestCommand(CollectionRowsRequest collectionRowsRequest) {
        scrollHandlerRegistration.removeHandler();
        lastScrollPos = 0;
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
                ScrollPanel scroll = tableBody.getScrollPanel();
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
                CollectionRowItem item = collectionRowItems.get(0);
                int index = -1;
                for (CollectionRowItem i : items) {
                    if (i.getId().toStringRepresentation().equalsIgnoreCase(item.getId().toStringRepresentation())) {
                        index = items.indexOf(i);
                    }
                }

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
            if (oldScrollPos == 0) {

                return;
            }
            // If scrolling up, ignore the event.
            if (oldScrollPos == scroll.getMaximumVerticalScrollPosition()) {

            }
            if (oldScrollPos >= lastScrollPos) {
                return;
            }
            //Height of grid contents (including outside the viewable area) - height of the scroll panel
            int maxScrollTop = scroll.getWidget().getOffsetHeight() - scroll.getOffsetHeight();
            if (lastScrollPos >= maxScrollTop) {
                if (sortCollectionState != null) {
                    sortCollectionState.setResetCollection(false);
                }

                collectionData();
            }
        }

    }

}

