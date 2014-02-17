package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.user.cellview.client.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SetSelectionModel;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.gui.navigation.SortCriteriaConfig;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.event.*;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.CellTableEventHandler;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.CheckedSelectionModel;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.TableController;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.resources.CellTableResourcesEx;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.resources.DGCellTableResourceAdapter;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
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

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 17/9/13
 *         Time: 12:05 PM
 */
public class CollectionPluginView extends PluginView {
    public static final int START_ROW_COUNT = 70;
    private CellTable<CollectionRowItem> tableHeader;
    private CellTable<CollectionRowItem> tableBody;
    private int startHeight;
    private ScrollPanel scrollTableBody = new ScrollPanel();
    private ScrollPanel outerSideScroll = new ScrollPanel();    //удалить
    private ScrollPanel scrollTableHeader = new ScrollPanel();
    private TableController tableController;
    private ArrayList<CollectionRowItem> items;
    private LinkedHashMap<String, CollectionColumnProperties> fieldPropertiesMap;
    private FlowPanel headerPanel = new FlowPanel();
    private FlowPanel bodyPanel = new FlowPanel();
    //панель для растягивания внешней скрол панели
    private FlowPanel panelHeight = new FlowPanel();

    private FlexTable flexTable = new FlexTable();

    private AbsolutePanel root = new AbsolutePanel();
    private String collectionName;
    private String collectionViewConfigName;
    private int listCount;
    private int tableWidth;
    private int tableHeight;
    private boolean singleChoice = true;
    private SortCollectionState sortCollectionState;
    private Button filterButton = new Button();
    private HorizontalPanel searchPanel = new HorizontalPanel();
    private ArrayList<Filter> filterList;
    private ArrayList<CollectionSearchBox> searchBoxList = new ArrayList<CollectionSearchBox>();
    private AbsolutePanel treeLinkWidget = new AbsolutePanel();
    private String simpleSearchQuery = "";
    private String searchArea = "";
    //   private HashMap<String, ValueConverter> converterMap = new HashMap<String, ValueConverter>();
    private FlowPanel simpleSearch = new FlowPanel();
    private int lastScrollPos ;

    private SplitterBehavior splitterBehavior;

    // локальная шина событий
    private EventBus eventBus;
    protected Plugin plugin;
    private Column<CollectionRowItem, Boolean> checkColumn;
    private SetSelectionModel<CollectionRowItem> selectionModel;
    /**
     * Создание стилей для ящеек таблицы
     */
    private final DGCellTableResourceAdapter adapter;

    protected CollectionPluginView(CollectionPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
        this.eventBus = plugin.getLocalEventBus();
        adapter = new DGCellTableResourceAdapter(CellTableResourcesEx.I);
        tableHeader = new CellTable<CollectionRowItem>(999, adapter.getResources());
        tableBody = new CellTable<CollectionRowItem>(999, adapter.getResources());
        filterList = new ArrayList<Filter>();
        tableController = new TableController(tableHeader, tableBody, eventBus, searchPanel);
        updateSizes();
        splitterBehavior = new SplitterBehavior(eventBus, plugin);


    }

    private void updateSizes() {
        tableWidth = plugin.getOwner().getVisibleWidth() - BusinessUniverseConstants.COLLECTION_VIEW_SIZE_SCROLL_WIDTH;
        tableHeight = plugin.getOwner().getVisibleHeight();

    }

    @Override
    protected IsWidget getViewWidget() {
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

        return root;
    }

    public void init() {
        buildPanel();

        createTableColumns();
        applySelectionModel();
        insertRows(items);
        applyStyles();
        addHandlers();
        splitterBehavior.basePanelHeightSize();
    }

    public List<Id> getSelectedIds() {
        final List<Id> selectedIds = new ArrayList<Id>(selectionModel.getSelectedSet().size());
        for (CollectionRowItem item : selectionModel.getSelectedSet()) {
            selectedIds.add(item.getId());
        }
        return selectedIds;
    }

    private void createTableColumns() {
        if (singleChoice) {
            createTableColumnsWithoutCheckBoxes(fieldPropertiesMap, 0);
        } else {
            createTableColumnsWithCheckBoxes(fieldPropertiesMap);
        }
    }

    public void onPluginPanelResize() {
        updateSizes();
        tableController.columnWindowResize(tableWidth / tableBody.getColumnCount());
        splitterBehavior.widgetSizeBySplitterPosition();

    }

    private void addHandlers() {

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

        tableHeader.addColumnSortHandler(new ColumnSortEvent.Handler() {
            @Override
            public void onColumnSort(ColumnSortEvent event) {
                CollectionColumn column = (CollectionColumn) event.getColumn();
                String dataStoreName = column.getDataStoreName();
                boolean ascending = event.isSortAscending();
                String field = column.getFieldName();
                sortCollectionState = new SortCollectionState(0, START_ROW_COUNT, dataStoreName, ascending, true, field);
                tableHeader.redrawHeaders();
                createSortedCollectionData();
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

                if (searchPanel.isVisible()) {
                    searchPanel.setVisible(false);
                } else {
                    for (int i =0; i < searchBoxList.size(); i++){
                        CollectionSearchBox searchBox = searchBoxList.get(i);
                        searchBox.getTextBox().setText("");
                        searchBox.setText("");
                        searchBox.getClose().addStyleName("flush-serch-hide");
                    }
                    searchPanel.setVisible(true);
                    CollectionSearchBox searchBox = searchBoxList.get(0);
                    searchBox.getTextBox().setFocus(true);

                }
            }
        });


        //событие которое инициализирует поиск по колонкам таблицы
        eventBus.addHandler(TableSearchEvent.TYPE, new TableSearchEventHandler() {
            @Override
            public void searchByFields(TableSearchEvent event) {
                filterList.clear();
                if (event.getBox().isEscPress()){

                    searchPanel.setVisible(false);

                    clearAllTAbleData();

                }  else {


                boolean isRequestRequired = false;
                for (int i = 0; i < searchBoxList.size(); i++) {
                    CollectionSearchBox searchBox = searchBoxList.get(i);
                    String filterType = searchBox.getFilterType();
                    if (filterType != null && searchBox.getText().length() > 0) {
                        isRequestRequired = true;
                        Filter filter = new Filter();
                        filter.setFilter(filterType);
                        Value value;
                        if (searchBox.getType() == CollectionSearchBox.Type.DATEBOX) {
                            Date date = searchBox.getDate();
                            if (date != null) {
                                value = new DateTimeValue(date);
                            } else {
                                try {
                                    date = BusinessUniverseConstants.DATE_TIME_FORMAT.parse(searchBox.getText());
                                } catch (IllegalArgumentException e) {
                                    Window.alert("Wrong date!");
                                    return;
                                }
                                value = new DateTimeValue(date);
                            }
                        } else {
                            value = new StringValue("%" + searchBox.getText() + "%");
                        }
                        filter.addCriterion(0, value);
                        filterList.add(filter);
                    }
                }
                if (isRequestRequired) {
                    clearAllTAbleData();
                    }
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

    private void clearAllTAbleData(){
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
        searchPanel.addStyleName("horizont-container");
     //   headerPanel.add(treeLinkWidget);
        treeLinkWidget.setWidth("100%");
        treeLinkWidget.getElement().getStyle().setBackgroundColor("white");
        treeLinkWidget.add(filterButton);
        AbsolutePanel containerForToolbar = new AbsolutePanel();
        containerForToolbar.addStyleName("search-header");
        if (searchArea != null && searchArea.length() > 0) {
            SimpleSearchPanel simpleSearchPanel = new SimpleSearchPanel(simpleSearch, eventBus);

            treeLinkWidget.add(simpleSearchPanel);
          //  startHeight = BusinessUniverseConstants.COLLECTION_WIDGET_HEADER_ROW_WITH_SIMPLE_SEARCH;
        }/* else {
            startHeight = BusinessUniverseConstants.COLLECTION_WIDGET_HEADER_ROW;
        }*/
        startHeight = BusinessUniverseConstants.COLLECTION_WIDGET_HEADER_ROW_WITH_SIMPLE_SEARCH;

        headerPanel.add(tableHeader);
      //  headerPanel.add(filterButton);
        filterButton.removeStyleName("gwt-Button");
        filterButton.addStyleName("show-filter-button");
     //   filterButton.addStyleName("search-button");
        headerPanel.add(searchPanel);
        searchPanel.setVisible(false);
        bodyPanel.add(tableBody);
        //scrollTableBody.getElement().getStyle().setOverflowX(Style.Overflow.HIDDEN);
        scrollTableBody.getElement().getStyle().setOverflowY(Style.Overflow.HIDDEN);
        scrollTableHeader.getElement().getStyle().setOverflow(Style.Overflow.HIDDEN);
        scrollTableBody.add(bodyPanel);
        outerSideScroll.getElement().getStyle().setWidth(18, Style.Unit.PX);
        FlexTable.FlexCellFormatter formater = flexTable.getFlexCellFormatter();
        scrollTableHeader.add(headerPanel);
        scrollTableHeader.setWidth(tableWidth + "px");
        scrollTableBody.setWidth(tableWidth + "px");

        flexTable.setWidget(0, 0, treeLinkWidget);
        flexTable.setWidget(1, 0, scrollTableHeader);
       /* flexTable.setWidget(2,0, searchPanel);*/
        flexTable.setWidget(2, 0, scrollTableBody);
        flexTable.setWidget(2, 1, outerSideScroll);
        flexTable.setCellSpacing(0);
        flexTable.setCellPadding(0);
        tableBody.getElement().getStyle().setPosition(Style.Position.RELATIVE);
        outerSideScroll.add(panelHeight);

        outerSideScroll.setHeight(tableHeight - startHeight + "px");
        formater.setVerticalAlignment(2, 1, HasVerticalAlignment.ALIGN_TOP);
        formater.setVerticalAlignment(2, 0, HasVerticalAlignment.ALIGN_TOP);
        root.add(flexTable);

    }

    private void createTableColumnsWithCheckBoxes(
            final LinkedHashMap<String, CollectionColumnProperties> domainObjectFieldsOnColumnNamesMap) {
        checkColumn = new Column<CollectionRowItem, Boolean>(
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
        tableHeader.addColumn(checkColumn, "");
        tableBody.addColumn(checkColumn);

        int columnWidth = 35;
        tableHeader.setColumnWidth(checkColumn, columnWidth + "px");
        checkColumn.setDataStoreName(BusinessUniverseConstants.CHECK_BOX_COLUMN_NAME);
        tableBody.setColumnWidth(checkColumn, columnWidth + "px");
        createTableColumnsWithoutCheckBoxes(domainObjectFieldsOnColumnNamesMap, columnWidth);
    }



    private void createTableColumnsWithoutCheckBoxes(
            LinkedHashMap<String, CollectionColumnProperties> domainObjectFieldPropertiesMap, final int sizeOffset) {
        int numberOfColumns = sizeOffset + domainObjectFieldPropertiesMap.keySet().size();
        int tableWidthAvailable = tableWidth;
        for (String field : domainObjectFieldPropertiesMap.keySet()) {
            final CollectionColumnProperties columnProperties = domainObjectFieldPropertiesMap.get(field);
            CollectionColumn column = ColumnFormatter.createFormattedColumn(columnProperties);
            int columnWidthAverage = (tableWidthAvailable / numberOfColumns);
            int columnWidth = BusinessUniverseUtils.adjustWidth(columnWidthAverage, column.getMinWidth(), column.getMaxWidth());
            tableWidthAvailable -= columnWidth;
            numberOfColumns -= 1;
            tableHeader.addColumn(column, column.getDataStoreName());
            tableHeader.setColumnWidth(column, columnWidth + "px");
            SortedMarker sortedMarker = (SortedMarker) columnProperties.getProperty(CollectionColumnProperties.SORTED_MARKER);
            if (sortedMarker != null) {
                boolean ascending = sortedMarker.isAscending();
                column.setDefaultSortAscending(ascending);
                tableHeader.getColumnSortList().push(new ColumnSortList.ColumnSortInfo(column, ascending));
            }
            final String filterType = (String) columnProperties.getProperty(CollectionColumnProperties.SEARCH_FILTER_KEY);
            final String fieldType = (String) columnProperties.getProperty(CollectionColumnProperties.TYPE_KEY);
            CollectionSearchBox box;
            if (fieldType.equals("datetime")) {
                box = new CollectionSearchBox(new DateBox(), filterType, eventBus);
            } else {
                box = new CollectionSearchBox(new TextBox(), filterType, eventBus);
            }
            searchBoxList.add(box);

            box.setWidth(columnWidth + "px");
            searchPanel.add(box);
            tableBody.addColumn(column);
            tableBody.setColumnWidth(column, columnWidth + "px");
            if (sizeOffset != 0) {
                searchPanel.getElement().getStyle().setPaddingLeft(columnWidth + sizeOffset, Style.Unit.PX);
            }
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
        syncVerticalScroll();


    }

    private void syncVerticalScroll(){
        if (listCount<=START_ROW_COUNT){
            splitterBehavior.basePanelHeightSize();
        }
        panelHeight.setHeight(((items.size()) * 16)+"px");
        System.out.println(" rows "+items.size() );

    }

    private void applyStyles() {
        applyHeaderTableStyle();
        applyBodyTableStyle();
    }

    private void applyHeaderTableStyle() {
        tableHeader.setStyleName(adapter.getResources().cellTableStyle().docsCommonCelltableHeader());
        tableHeader.setTableLayoutFixed(true);
        headerPanel.setStyleName(adapter.getResources().cellTableStyle().docsCommonCelltableHeaderPanel());

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
        emptyTableWidget.getElement().getStyle().setPaddingLeft(60, Style.Unit.PX);
        tableBody.setRowStyles(new RowStyles<CollectionRowItem>() {
            @Override
            public String getStyleNames(CollectionRowItem row, int rowIndex) {
                return adapter.getResources().cellTableStyle().docsCommonCelltableTrCommon();

            }
        });
        tableBody.setStyleName(adapter.getResources().cellTableStyle().docsCommonCelltableBody());

        tableBody.setEmptyTableWidget(emptyTableWidget);
        tableBody.setSelectionModel(selectionModel);
        tableBody.setTableLayoutFixed(true);

    }


    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }


    private void createCollectionData() {
        CollectionRowsRequest collectionRowsRequest = new CollectionRowsRequest(listCount, START_ROW_COUNT,
                collectionName, fieldPropertiesMap, filterList, simpleSearchQuery, searchArea);

        collectionRowRequestCommand(collectionRowsRequest);
    }

    private void createSortedCollectionData() {
        CollectionRowsRequest collectionRowsRequest;
        String field = sortCollectionState.getField();
        boolean ascending = sortCollectionState.isAscend();
        CollectionColumnProperties collectionColumnProperties = fieldPropertiesMap.get(field);

        if (sortCollectionState.isResetCollection()) {
            items.clear();

            collectionRowsRequest = new CollectionRowsRequest(sortCollectionState.getCount(),
                    sortCollectionState.getOffset(), collectionName, fieldPropertiesMap,
                    sortCollectionState.isAscend(), sortCollectionState.getColumnName(),
                    field, filterList);

            scrollTableBody.scrollToTop();
            outerSideScroll.scrollToTop();
            sortCollectionState.setResetCollection(false);
            listCount = 0;
            lastScrollPos =0;
        } else {
            collectionRowsRequest = new CollectionRowsRequest(listCount,
                    START_ROW_COUNT, collectionName, fieldPropertiesMap,
                    ascending, sortCollectionState.getColumnName(),
                    sortCollectionState.getField(), filterList);
        }
        SortCriteriaConfig sortCriteriaConfig = ascending ? collectionColumnProperties.getAscSortCriteriaConfig()
                : collectionColumnProperties.getDescSortCriteriaConfig();
        collectionRowsRequest.setSortCriteriaConfig(sortCriteriaConfig);
        collectionRowRequestCommand(collectionRowsRequest);
    }

    private HashMap<String, String> getFieldToNameMap() {
        final HashMap<String, String> fieldToNameMap = new HashMap<String, String>();
        for (Map.Entry<String, CollectionColumnProperties> entry : fieldPropertiesMap.entrySet()) {
            fieldToNameMap.put(entry.getKey(),
                    (String) entry.getValue().getProperty(CollectionColumnProperties.NAME_KEY));
        }
        return fieldToNameMap;
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
                class SplitterBehavior{
                    private EventBus eventBus;
                    private Plugin plugin;

                    private int horizontalWidth;
                    private int horizontalHeight;
                    private int verticalWidth;
                    private int verticalHeight;
                    private boolean splitterHorizontalPosition;



                    SplitterBehavior(EventBus eventBus, Plugin plugin) {
                        this.eventBus = eventBus;
                        this.plugin = plugin;

                        horizontalWidth = tableWidth;
                        horizontalHeight = tableHeight;
                        addSplitterHendler();
                    }



                    void addSplitterHendler(){
                        eventBus.addHandler(SplitterInnerScrollEvent.TYPE, new SplitterInnerScrollEventHandler() {
                            @Override
                            public void setScrollPanelHeight(SplitterInnerScrollEvent event) {
                                splitterHorizontalPosition = event.isScrollState();
                                scrollTableHeader.setVerticalScrollPosition(scrollTableBody.getVerticalScrollPosition());
                                widgetSize(event.getUpperPanelHeight() , event.getUpperPanelWidth()-BusinessUniverseConstants.COLLECTION_VIEW_SIZE_SCROLL_WIDTH);
                                if (splitterHorizontalPosition){
                                    tableController.columnWindowResize((event.getUpperPanelWidth() - BusinessUniverseConstants.COLLECTION_VIEW_SIZE_SCROLL_WIDTH) /tableBody.getColumnCount());
                                }
                                if (event.isScrollState()) {
                                    setVerticalSize(event.getUpperPanelHeight() , event.getUpperPanelWidth()-BusinessUniverseConstants.COLLECTION_VIEW_SIZE_SCROLL_WIDTH);
                                } else {
                                    setHorizontalSize(event.getUpperPanelHeight() , event.getUpperPanelWidth()-BusinessUniverseConstants.COLLECTION_VIEW_SIZE_SCROLL_WIDTH);
                                }
                            }
                        });

                        eventBus.addHandler(SplitterWidgetResizerEvent.TYPE, new SplitterWidgetResizerEventHandler() {

                            @Override
                            public void setWidgetSize(SplitterWidgetResizerEvent event) {
                                splitterHorizontalPosition = event.isType();

                                int height=0;
                                int width =0;
                                if(event.isArrowsPress()){
                                    height = event.getFirstWidgetHeight();
                                    width = (event.getFirstWidgetWidth()- BusinessUniverseConstants.COLLECTION_VIEW_SIZE_SCROLL_WIDTH);
                                    tableController.columnWindowResize(width/tableBody.getColumnCount());
                                    widgetSize(height, width);
                                    if (splitterHorizontalPosition){
                                        setVerticalSize(height, width);
                                    }  else {

                                        setHorizontalSize(height, width);
                                    }

                                }  else {
                                    int headerPanelHeight = headerPanel.getOffsetHeight() +treeLinkWidget.getOffsetHeight();

                                    if (splitterHorizontalPosition){


                                        if (verticalHeight == 0 && !event.isArrowsPress()){
                                            height =  (tableHeight *2  - headerPanelHeight-BusinessUniverseConstants.COLLECTION_BOTTOM_SCROLL_HEIGHT);
                                            width = (tableWidth/2 - BusinessUniverseConstants.COLLECTION_VIEW_SIZE_SCROLL_WIDTH);
                                            tableController.columnWindowResize(width/tableBody.getColumnCount());
                                            widgetInnerPanelSize(height, width);

                                        } else {
                                            height = verticalHeight  - headerPanelHeight;
                                            tableController.columnWindowResize(width/tableBody.getColumnCount());
                                            widgetInnerPanelSize(height, verticalWidth);
                                        }


                                    } else {



                                        if (horizontalHeight == 0 && !event.isArrowsPress() ){
                                            height =  (tableHeight /2   - headerPanelHeight-BusinessUniverseConstants.COLLECTION_BOTTOM_SCROLL_HEIGHT);
                                            width = (tableWidth*2 - BusinessUniverseConstants.COLLECTION_VIEW_SIZE_SCROLL_WIDTH);
                                            tableController.columnWindowResize(width/tableBody.getColumnCount());
                                            widgetInnerPanelSize(height, width);

                                        } else {
                                            height =  (horizontalHeight  - headerPanelHeight);
                                            tableController.columnWindowResize(width/tableBody.getColumnCount());
                                            widgetInnerPanelSize(height, horizontalWidth);
                                        }

                                    }
                                }
                            }

                        });

                        outerSideScroll.addScrollHandler(new ScrollHandler() {
                            @Override
                            public void onScroll(ScrollEvent event) {
                                scrollTableBody.setVerticalScrollPosition(outerSideScroll.getVerticalScrollPosition());
                                if (outerSideScroll.getVerticalScrollPosition() == outerSideScroll.getMaximumVerticalScrollPosition()
                                        && simpleSearchQuery.length() == 0 && outerSideScroll.getMaximumVerticalScrollPosition()> lastScrollPos) {
                                    lastScrollPos = outerSideScroll.getMaximumVerticalScrollPosition();
                                    collectionData();
                                }

                                    if (splitterHorizontalPosition){
                                        widgetSize(verticalHeight, verticalWidth);
                                    }  else {

                                        widgetSize(horizontalHeight, horizontalWidth);
                                    }

                                }

                        });


                        scrollTableBody.addScrollHandler(new ScrollHandler() {
                            @Override
                            public void onScroll(ScrollEvent event) {
                                scrollTableHeader.setHorizontalScrollPosition(scrollTableBody.getHorizontalScrollPosition());

                            }
                        });
                    }

                    private void widgetSizeBySplitterPosition(){
                        if (!splitterHorizontalPosition){
                            horizontalWidth = tableWidth;
                            horizontalHeight = tableHeight;
                            verticalHeight = 0;
                            verticalWidth = 0;

                        } else{
                            horizontalWidth = 0;
                            horizontalHeight = 0;
                            verticalHeight = tableHeight;
                            verticalWidth = tableWidth;

                        }
                        widgetSize(tableHeight, tableWidth);
                    }


                    private void widgetSize(int height, int width){
                        int tmp;
                        if (headerPanel.getOffsetHeight() == 0){
                            tmp = height -startHeight;
                            basePanelHeightSize();
                        } else {
                            tmp =  height - headerPanel.getOffsetHeight()- treeLinkWidget.getOffsetHeight();

                        }



                        panelHeight.setHeight(bodyPanel.getOffsetHeight()+"px");
                        widgetInnerPanelSize(tmp, width);

                    }

                    private void widgetInnerPanelSize(int height, int width){
                        scrollTableBody.setHeight(height +"px");
                        scrollTableBody.setWidth(width+"px");
                        outerSideScroll.setHeight(height +"px");
                        scrollTableHeader.setWidth(width+"px");


                    }

                    private void setHorizontalSize(int height, int width){
                        horizontalWidth = width;
                        horizontalHeight = height;
                    }

                    private void setVerticalSize(int height, int width){
                        verticalWidth = width;
                        verticalHeight = height;
                    }

                    void basePanelHeightSize(){
                        panelHeight.setHeight((items.size() * 16)+"px");
                    }
                }

}

