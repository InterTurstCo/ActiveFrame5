package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SetSelectionModel;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.converter.ValueConverter;
import ru.intertrust.cm.core.gui.impl.client.converter.ValueConverterFactory;
import ru.intertrust.cm.core.gui.impl.client.event.*;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.CellTableEventHandler;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.CheckedSelectionModel;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.TableController;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.resources.CellTableResourcesEx;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.resources.DGCellTableResourceAdapter;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;
import ru.intertrust.cm.core.gui.model.Command;
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
    private CellTable<CollectionRowItem> tableHeader;
    private CellTable<CollectionRowItem> tableBody;
    private ScrollPanel scrollTableBody = new ScrollPanel();
    private TableController tableController;
    private ArrayList<CollectionRowItem> items;
    private LinkedHashMap<String, CollectionColumnProperties> fieldPropertiesMap;
    private FlowPanel headerPanel = new FlowPanel();
    private FlowPanel bodyPanel = new FlowPanel();
    private VerticalPanel verticalPanel = new VerticalPanel();
    private FlowPanel root = new FlowPanel();
    private String collectionName;
    private int listCount;
    private int tableWidth;
    private int tableHeight;
    private boolean singleChoice = true;
    private SortCollectionState sortCollectionState;
    private Button filterButton = new Button();
    private HorizontalPanel searchPanel = new HorizontalPanel();
    private ArrayList<Filter> filterList;
    private ArrayList<CollectionSearchBox> searchBoxList =  new ArrayList<CollectionSearchBox>();
    private HorizontalPanel treeLinkWidget = new HorizontalPanel();
    private String simpleSearchQuery = "";
    private String searchArea = "";
    private HashMap<String, ValueConverter> converterMap = new HashMap<String, ValueConverter>();


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
    }

    public CellTable getTableBody() {
        return tableBody;
    }


    private void updateSizes() {
        tableWidth = plugin.getOwner().getVisibleWidth();
        tableHeight = plugin.getOwner().getVisibleHeight();

    }

    @Override
    protected IsWidget getViewWidget() {
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
        return root;
    }

    public void init() {
        buildPanel();
        createTableColumns();
        applySelectionModel();
        insertRows(items);
        applyStyles();
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
        if (singleChoice) {
            createTableColumnsWithoutCheckBoxes(fieldPropertiesMap, 0);
        } else {
            createTableColumnsWithCheckBoxes(fieldPropertiesMap);
        }
    }

    private int columnMinWidth(int width) {
        if (width < 120) {
            width = 120;
        }
        return width;
    }

    public void onPluginPanelResize() {
        updateSizes();
        tableController.columnWindowResize(columnMinWidth(tableWidth / tableHeader.getColumnCount()));
        scrollTableBody.setHeight(tableHeight + "px");
    }

    private void addHandlers() {
        addResizeHandler();
        tableBody.addCellPreviewHandler(new CellTableEventHandler<CollectionRowItem>(tableBody, plugin, eventBus));
        eventBus.addHandler(SplitterInnerScrollEvent.TYPE, new SplitterInnerScrollEventHandler() {
            @Override
            public void setScrollPanelHeight(SplitterInnerScrollEvent event) {

                scrollTableBody.setHeight((event.getUpperPanelHeight() - headerPanel.getOffsetHeight()) + "px");
                if (event.isScrollState()) {
                    tableController.columnWindowResize(columnMinWidth(event.getUpperPanelWidth() / tableBody.getColumnCount()));
                }
            }
        });

        eventBus.addHandler(SplitterWidgetResizerEvent.TYPE, new SplitterWidgetResizerEventHandler() {

            @Override
            public void setWidgetSize(SplitterWidgetResizerEvent event) {
                if (event.isType()) {
                    if ((event.getFirstWidgetHeight() * 2) < Window.getClientHeight()) {
                        scrollTableBody.setHeight(((event.getFirstWidgetHeight() * 2) - headerPanel.getOffsetHeight()) + "px");
                    } else {
                        scrollTableBody.setHeight((event.getFirstWidgetHeight() - headerPanel.getOffsetHeight()) + "px");
                    }
                } else {
                    scrollTableBody.setHeight((event.getFirstWidgetHeight() - headerPanel.getOffsetHeight()) + "px");
                }
                if (!event.isType()) {
                    tableController.columnWindowResize(columnMinWidth((event.getFirstWidgetWidth() * 2) / tableBody.getColumnCount()));
                } else {
                    tableController.columnWindowResize(columnMinWidth(event.getFirstWidgetWidth() / tableBody.getColumnCount()));
                }
            }
        });


        scrollTableBody.addScrollHandler(new ScrollHandler() {
            @Override
            public void onScroll(ScrollEvent event) {
                if (scrollTableBody.getVerticalScrollPosition() == scrollTableBody.getMaximumVerticalScrollPosition()) {
                    collectionData();
                }

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

        eventBus.addHandler(TableControllerSortEvent.TYPE, new TableControllerSortEventHandler() {
            @Override
            public void setSort(TableControllerSortEvent event) {
                sortCollectionState = event.getSortCollectionState();

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
                    searchPanel.setVisible(true);
                }
            }
        });


        //событие которое инициализирует поиск по колонкам таблицы
        eventBus.addHandler(TableSearchEvent.TYPE, new TableSearchEventHandler() {
            @Override
            public void seachByFields(TableSearchEvent event) {
                filterList.clear();
                for (int i = 0; i < searchBoxList.size(); i++) {
                    if (searchBoxList.get(i).getFilterType() != null &&
                            searchBoxList.get(i).getText().length() > 0) {

                        Filter filter = new Filter();
                        filter.setFilter(searchBoxList.get(i).getFilterType());
                        Value value;
                        if (searchBoxList.get(i).getType() == CollectionSearchBox.Type.DATEBOX &&
                                searchBoxList.get(i).getText().length() > 0) {

                            Date date = new Date();
                            date = searchBoxList.get(i).getDate();

                            value = new DateTimeValue(date);


                        } else {
                            value = new StringValue("%" + searchBoxList.get(i).getText() + "%");
                        }
                        filter.addCriterion(0, value);
                        filterList.add(filter);


                    }
                }

                items.clear();
                listCount = 0;
                collectionData();


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


                if(sortCollectionState !=null && sortCollectionState.isSortable()){
                    sb.append("&");
                    sb.append("ColumnName=");
                    sb.append(sortCollectionState.getField());
                    sb.append("&");
                    sb.append("Sortable=");
                    sb.append(sortCollectionState.isSortDirection());

                }

                if (simpleSearchQuery != null && simpleSearchQuery.length()>0){
                    sb.append("&");
                    sb.append("simpleSearchQuery=");
                    sb.append(simpleSearchQuery);
                    sb.append("&");
                    sb.append("searchArea=");

                    sb.append(searchArea);


                }  else {
                    sb.append("&");
                    sb.append("filterName=");
                    for (int i = 0; i < filterList.size(); i++){
                         if (filterList.get(i).getCriterion(0) != null && !filterList.get(i).getCriterion(0).isEmpty())
                         sb.append(filterList.get(i).getFilter());
                         sb.append(":");
                         sb.append(filterList.get(i).getCriterion(0));
                         sb.append(":");

                    }
                }

                String msg = sb.toString().replaceAll("%", "");
                String query = GWT.getHostPageBaseURL() + "export-to-csv?"+msg;

                Window.open(query , "Export to CSV", "");

            }
        });




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
        CollectionRowItem item = new CollectionRowItem();
        LinkedHashMap<String, Value> rowValues = new LinkedHashMap<String, Value>();
        for (String field : fieldPropertiesMap.keySet()) {
             Value value = null;
             value = collectionObject.getValue(field);

            if (field.equalsIgnoreCase("id")) {
                value = new StringValue(collectionObject.getId().toStringRepresentation());
            }

            if (field.equalsIgnoreCase("name")) {
                value = new StringValue(collectionObject.getString(field));
                // это для коллекции организаций(временное решение)
                if (value.toString() == "null")
                    value = new StringValue(collectionObject.getString("Name"));
                rowValues.put(field, value);
                continue;
            }

            rowValues.put(field, value);
        }

        item.setId(collectionObject.getId());
        item.setRow(rowValues);

        // ищем совпадающий элемент, берем его индекс
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
    }

    private void buildPanel() {
        searchPanel.addStyleName("horizont-container");
        headerPanel.add(treeLinkWidget);
        treeLinkWidget.setWidth("100%");
        treeLinkWidget.getElement().getStyle().setBackgroundColor("white");
        FlowPanel simpleSearch = new FlowPanel();
        if (searchArea != null && searchArea.length() > 0) {
            SimpleSearchPanel simpleSearchPanel = new SimpleSearchPanel(simpleSearch, eventBus);
            treeLinkWidget.add(simpleSearchPanel);


        }
        headerPanel.add(tableHeader);
        headerPanel.add(filterButton);
        filterButton.removeStyleName("gwt-Button");
        filterButton.addStyleName("search-button");
        headerPanel.add(searchPanel);
        searchPanel.setVisible(false);



        bodyPanel.add(tableBody);
        verticalPanel.add(headerPanel);

        scrollTableBody.getElement().getStyle().setOverflowX(Style.Overflow.HIDDEN);

        scrollTableBody.setHeight(tableHeight + "px");
        scrollTableBody.add(bodyPanel);
        verticalPanel.add(scrollTableBody);
        root.add(verticalPanel);


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
        int numberOfColumns = 1 + domainObjectFieldsOnColumnNamesMap.keySet().size();
        int columnWidth = (tableWidth / numberOfColumns);
        columnWidth = columnMinWidth(columnWidth);
        tableHeader.setColumnWidth(checkColumn, columnWidth + "px");
        tableBody.setColumnWidth(checkColumn, columnWidth + "px");
        createTableColumnsWithoutCheckBoxes(domainObjectFieldsOnColumnNamesMap, 1);
    }

    private void createTableColumnsWithoutCheckBoxes(
            final LinkedHashMap<String, CollectionColumnProperties> domainObjectFieldPropertiesMap,
            final int startNumberOfColumns) {
        int numberOfColumns = startNumberOfColumns + domainObjectFieldPropertiesMap.keySet().size();
        int columnWidth = (tableWidth / numberOfColumns);
        columnWidth = columnMinWidth(columnWidth);
        for (String field : domainObjectFieldPropertiesMap.keySet()) {
            final CollectionColumnProperties columnProperties = domainObjectFieldPropertiesMap.get(field);
            final String filterType =
                    (String) columnProperties.getProperty(CollectionColumnProperties.SEARCH_FILTER_KEY);
            final String fieldType = (String) columnProperties.getProperty(CollectionColumnProperties.TYPE_KEY);
            final ValueConverter converter = ValueConverterFactory.getConverter(fieldType);
            converter.init(columnProperties.getProperties());
            converterMap.put(field, converter);

            final Column<CollectionRowItem, String> column = new CollectionColumn(field, converter);
            column.setDataStoreName((String) columnProperties.getProperty(CollectionColumnProperties.NAME_KEY));
            tableHeader.addColumn(column, (String) columnProperties.getProperty(CollectionColumnProperties.NAME_KEY));
            tableHeader.setColumnWidth(column, columnWidth + "px");
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
            if(startNumberOfColumns != 0) {
                searchPanel.getElement().getStyle().setPaddingLeft(columnWidth * startNumberOfColumns, Style.Unit.PX);
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

    public ScrollPanel getScrollTableBody() {
        return scrollTableBody;
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }


    private void createCollectionData() {
        CollectionRowsRequest collectionRowsRequest = new CollectionRowsRequest(listCount, 70,
                collectionName, getFieldToNameMap(), filterList, simpleSearchQuery, searchArea);

        collectionRowRequestCommand(collectionRowsRequest);
    }

    private void createSortedCollectionData() {
        CollectionRowsRequest collectionRowsRequest;
        if (sortCollectionState.isResetCollection()) {
            items.clear();
            collectionRowsRequest = new CollectionRowsRequest(sortCollectionState.getCount(),
                    sortCollectionState.getOffset(), collectionName, getFieldToNameMap(),
                    sortCollectionState.isSortDirection(), sortCollectionState.getColumnName(),
                    sortCollectionState.getField(), filterList);

            scrollTableBody.scrollToTop();
            sortCollectionState.setResetCollection(false);
            listCount = 0;
        } else {
            collectionRowsRequest = new CollectionRowsRequest(listCount,
                    70, collectionName, getFieldToNameMap(),
                    sortCollectionState.isSortDirection(), sortCollectionState.getColumnName(),
                    sortCollectionState.getField(), filterList);
        }
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

    private void addResizeHandler() {
        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {

                tableController.columnWindowResize(columnMinWidth(tableWidth / tableHeader.getColumnCount()));
                scrollTableBody.setHeight(tableHeight + "px");
            }
        });
    }
}

