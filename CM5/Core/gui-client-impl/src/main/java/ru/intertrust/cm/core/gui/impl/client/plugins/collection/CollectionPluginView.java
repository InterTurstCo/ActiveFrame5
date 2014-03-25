package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.MultiSelectionModel;

import com.google.gwt.view.client.SetSelectionModel;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.gui.navigation.SortCriteriaConfig;

import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.event.*;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.*;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.resources.DataGridResourceAdapter;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.resources.DataGridResources;

import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseUtils;
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
    private ArrayList<CollectionRowItem> items;
    private LinkedHashMap<String, CollectionColumnProperties> fieldPropertiesMap;
    private VerticalPanel root = new VerticalPanel();
    private String collectionName;
    private String collectionViewConfigName;
    private int listCount;
    private int tableWidth;
    private boolean singleChoice = true;
    private SortCollectionState sortCollectionState;
    private ToggleButton filterButton = new ToggleButton();

    private ArrayList<Filter> filterList;

    private String simpleSearchQuery = "";
    private String searchArea = "";

    private CollectionColumnHeaderController headerController;
    private int lastScrollPos;

    // локальная шина событий
    private EventBus eventBus;

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
        tableBody = new CollectionDataGrid(15, adapter.getResources());
        tableBody.setHeaderBuilder(new HeaderBuilder<CollectionRowItem>(tableBody, false));
        tableBody.addStyleName("collection-plugin-view collection-plugin-view-container");
        filterList = new ArrayList<Filter>();
        updateSizes();


    }

    private void updateSizes() {
        tableWidth = plugin.getOwner().getVisibleWidth();

    }

    @Override
    public IsWidget getViewWidget() {
        CollectionPluginData collectionPluginData = plugin.getInitialData();
        collectionName = collectionPluginData.getCollectionName();
        collectionViewConfigName = collectionPluginData.getCollectionViewConfigName();
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
            createTableColumnsWithoutCheckBoxes(fieldPropertiesMap, 0);
        } else {
            createTableColumnsWithCheckBoxes(fieldPropertiesMap);
        }
    }

    public void onPluginPanelResize() {
        updateSizes();


    }

    private void addHandlers() {
        tableBody.setAutoHeaderRefreshDisabled(true);
        eventBus.addHandler(CollectionPluginResizeBySplitterEvent.TYPE, new CollectionPluginResizeBySplitterEventHandler() {
            @Override
            public void onCollectionPluginResizeBySplitter(CollectionPluginResizeBySplitterEvent event) {
                tableBody.redraw();
                headerController.setFocus();
                headerController.updateFilterValues();

            }
        });
        tableBody.addCellPreviewHandler(new CellTableEventHandler<CollectionRowItem>(tableBody, plugin, eventBus));

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

        scroll.addScrollHandler(new ScrollHandler() {

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


        });
        eventBus.addHandler(SimpleSearchEvent.TYPE, new SimpleSearchEventHandler() {
            @Override
            public void collectionSimpleSearch(SimpleSearchEvent event) {
                //To change body of implemented methods use File | Settings | File Templates.
                listCount = 0;
                items.clear();
                if (!event.isTypeButton()) {
                    filterList.clear();

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
                //To change body of implemented methods use File | Settings | File Templates.
                StringBuilder sb = new StringBuilder();

                sb.append("collectionName=");
                sb.append(collectionName);

                sb.append("&collectionView=");
                sb.append(collectionViewConfigName);

                if (sortCollectionState != null && sortCollectionState.isSortable()) {
                    sb.append("&");
                    sb.append("ColumnName=");
                    sb.append(sortCollectionState.getField());
                    sb.append("&");
                    sb.append("Sortable=");
                    sb.append(sortCollectionState.isAscend());

                }

                if (simpleSearchQuery != null && simpleSearchQuery.length() > 0) {
                    sb.append("&");
                    sb.append("simpleSearchQuery=");
                    sb.append(simpleSearchQuery);
                    sb.append("&");
                    sb.append("searchArea=");

                    sb.append(searchArea);


                } else {
                    sb.append("&");
                    sb.append("filterName=");
                    for (int i = 0; i < filterList.size(); i++) {
                        if (filterList.get(i).getCriterion(0) != null && !filterList.get(i).getCriterion(0).isEmpty())
                            sb.append(filterList.get(i).getFilter());
                        sb.append(":");
                        sb.append(filterList.get(i).getCriterion(0));
                        sb.append(":");

                    }
                }

                String msg = sb.toString().replaceAll("%", "");
                String query = GWT.getHostPageBaseURL() + "export-to-csv?" + msg;

                Window.open(query, "Export to CSV", "");

            }
        });

    }

    private void onKeyEnterPressed() {
        filterList.clear();
        boolean isRequestRequired = false;
        for (CollectionColumnHeader header : headerController.getHeaders()) {
            String filterValue = header.getFilterValue();
            String filterName = header.getFilterName();
            if (filterValue != null && filterName != null && filterValue.length() > 0) {
                isRequestRequired = true;
                Filter filter = new Filter();
                filter.setFilter(filterName);
                Value value = null;
                String fieldType = header.getFieldType();
                if (TIMELESS_DATE_TYPE.equalsIgnoreCase(fieldType)) {


                        try {
                            DateTimeFormat format = header.getDateTimeFormat();
                        Date    date = format.parse(filterValue);

                            TimelessDate timelessDate = new TimelessDate(date.getYear() + 1900, date.getMonth(), date.getDay()) ;
                            value = new TimelessDateValue(timelessDate);
                        } catch (IllegalArgumentException e) {
                            Window.alert("Wrong date!");
                            return;
                        }
                        //   value = new TimelessDateValue(date);

                } else {
                    value = new StringValue("%" + filterValue + "%");
                }
                filter.addCriterion(0, value);
                filterList.add(filter);
            }
        }
        if (isRequestRequired) {
            clearAllTableData();

        }
    }

    private void onKeyEscapePressed() {
        headerController.resetFiltersValues();
        filterList.clear();
        filterButton.setValue(false);
        headerController.changeFiltersInputsVisibility(false);
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
                collectionName, fieldPropertiesMap, filterList, simpleSearchQuery, searchArea);
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
        tableBody.setColumnWidth(checkColumn, CHECK_BOX_MAX_WIDTH + "px");
        createTableColumnsWithoutCheckBoxes(domainObjectFieldsOnColumnNamesMap, CHECK_BOX_MAX_WIDTH);
    }

    private void createTableColumnsWithoutCheckBoxes(
            LinkedHashMap<String, CollectionColumnProperties> domainObjectFieldPropertiesMap, final int sizeOffset) {
        int numberOfColumns = sizeOffset + domainObjectFieldPropertiesMap.keySet().size();
        int tableWidthAvailable = tableWidth;
        List<CollectionColumnHeader> headers = new ArrayList<CollectionColumnHeader>();
        for (String field : domainObjectFieldPropertiesMap.keySet()) {
            final CollectionColumnProperties columnProperties = domainObjectFieldPropertiesMap.get(field);
            CollectionColumn column = ColumnFormatter.createFormattedColumn(columnProperties);
            int columnWidthAverage = (tableWidthAvailable / numberOfColumns);
            int columnWidth = BusinessUniverseUtils.adjustWidth(columnWidthAverage, column.getMinWidth(), column.getMaxWidth());
            tableWidthAvailable -= columnWidth;
            numberOfColumns -= 1;
            String searchFilterName = (String) columnProperties.getProperty(CollectionColumnProperties.SEARCH_FILTER_KEY);
            String datePattern = (String) columnProperties.getProperty(CollectionColumnProperties.PATTERN_KEY);
            String fieldType = (String) columnProperties.getProperty(CollectionColumnProperties.TYPE_KEY);
            String searchAreaId = column.hashCode() + column.getDataStoreName();
            HeaderWidget headerWidget = new HeaderWidget(column.getDataStoreName(), fieldType, searchFilterName, searchAreaId.replaceAll(" ", ""), String.valueOf(column.getMinWidth()), datePattern);
            CollectionColumnHeader collectionColumnHeader = new CollectionColumnHeader(tableBody, column, headerWidget, eventBus);
            headers.add(collectionColumnHeader);
            tableBody.addColumn(column, collectionColumnHeader);
            tableBody.setColumnWidth(column, columnWidth + "px");
            SortedMarker sortedMarker = (SortedMarker) columnProperties.getProperty(CollectionColumnProperties.SORTED_MARKER);
            if (sortedMarker != null) {
                boolean ascending = sortedMarker.isAscending();
                column.setDefaultSortAscending(ascending);
                tableBody.getColumnSortList().push(new ColumnSortList.ColumnSortInfo(column, ascending));
                sortCollectionState = new SortCollectionState(0, FETCHED_ROW_COUNT, column.getDataStoreName(), ascending, true, field);
            }
            headerController.setHeaders(headers);
        }
    }

    public void insertRows(List<CollectionRowItem> list) {
        tableBody.setRowData(list);
        listCount = items.size();

    }

    private void insertMoreRows(List<CollectionRowItem> list) {
        listCount += list.size();
        items.addAll(list);
        tableBody.setRowData(items);

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
                fieldPropertiesMap, filterList, simpleSearchQuery, searchArea);
        collectionRowRequestCommand(collectionRowsRequest);
    }

    private void createSortedCollectionData() {
        CollectionRowsRequest collectionRowsRequest;
        String field = sortCollectionState.getField();
        boolean ascending = sortCollectionState.isAscend();
        CollectionColumnProperties collectionColumnProperties = fieldPropertiesMap.get(field);
        if (sortCollectionState.isResetCollection()) {
            items.clear();
            collectionRowsRequest = new CollectionRowsRequest(sortCollectionState.getOffset(),
                    FETCHED_ROW_COUNT, collectionName, fieldPropertiesMap,
                    sortCollectionState.isAscend(), sortCollectionState.getColumnName(),
                    field, filterList);
            sortCollectionState.setResetCollection(false);
            listCount = 0;
            lastScrollPos = 0;
        } else {

            collectionRowsRequest = new CollectionRowsRequest(listCount,
                    FETCHED_ROW_COUNT, collectionName, fieldPropertiesMap,
                    ascending, sortCollectionState.getColumnName(),
                    sortCollectionState.getField(), filterList);
        }
        SortCriteriaConfig sortCriteriaConfig = ascending ? collectionColumnProperties.getAscSortCriteriaConfig()
                : collectionColumnProperties.getDescSortCriteriaConfig();
        collectionRowsRequest.setSortCriteriaConfig(sortCriteriaConfig);
        collectionRowRequestCommand(collectionRowsRequest);

    }

    private void collectionRowRequestCommand(CollectionRowsRequest collectionRowsRequest) {

        Command command = new Command("generateCollectionRowItems", "collection.plugin", collectionRowsRequest);
        BusinessUniverseServiceAsync.Impl.getInstance().executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining generateTableRowsForPluginInitialization for ''");
                caught.printStackTrace();
            }

            @Override
            public void onSuccess(Dto result) {
                CollectionRowItemList collectionRowItemList = (CollectionRowItemList) result;
                List<CollectionRowItem> collectionRowItems = collectionRowItemList.getCollectionRows();

                insertMoreRows(collectionRowItems);
                headerController.setFocus();
                headerController.updateFilterValues();
            }
        });
    }

    private void collectionOneRowRequestCommand(CollectionRowsRequest collectionRowsRequest) {

        Command command = new Command("generateCollectionRowItems", "collection.plugin", collectionRowsRequest);
        BusinessUniverseServiceAsync.Impl.getInstance().executeCommand(command, new AsyncCallback<Dto>() {
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


}

