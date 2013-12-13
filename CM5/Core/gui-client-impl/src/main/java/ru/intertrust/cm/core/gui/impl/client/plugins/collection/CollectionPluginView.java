package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionModel;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.event.*;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.CellTableEventHandler;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.CheckedSelectionModel;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.TableController;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.resources.CellTableResourcesEx;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.resources.DGCellTableResourceAdapter;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.form.widget.CollectionRowItemList;
import ru.intertrust.cm.core.gui.model.plugin.CollectionPluginData;
import ru.intertrust.cm.core.gui.model.plugin.CollectionRowItem;
import ru.intertrust.cm.core.gui.model.plugin.CollectionRowsRequest;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 17/9/13
 *         Time: 12:05 PM
 */
public class CollectionPluginView extends PluginView {
    private CellTable<CollectionRowItem> tableHeader;
    protected CellTable<CollectionRowItem> tableBody;
    private ScrollPanel scrollTableBody = new ScrollPanel();
    private TableController tableController;
    private ArrayList<CollectionRowItem> items;
    private LinkedHashMap<String, String> columnNamesOnDoFieldsMap;
    private FlowPanel headerPanel = new FlowPanel();
    private FlowPanel bodyPanel = new FlowPanel();
    private VerticalPanel verticalPanel = new VerticalPanel();
    private FlowPanel root = new FlowPanel();
    private String collectionName;
    private int scrollStart;
    private int listCount;
    private int tableWidth;
    private int tableHeight;
    private boolean singleChoice = true;

    // локальная шина событий
    private EventBus eventBus;
    protected Plugin plugin;
    private ArrayList<Integer> chosenIndexes = new ArrayList<Integer>();
    private Column<CollectionRowItem, Boolean> checkColumn;
    private SelectionModel<CollectionRowItem> selectionModel;
    /**
     * Создание стилей для ящеек таблицы
     */
    private final DGCellTableResourceAdapter adapter;

    protected CollectionPluginView(CollectionPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
        this.eventBus = plugin.getEventBus();
        adapter = new DGCellTableResourceAdapter(CellTableResourcesEx.I);
        tableHeader = new CellTable<CollectionRowItem>(999, adapter.getResources());
        tableBody = new CellTable<CollectionRowItem>(999, adapter.getResources());
        tableController = new TableController(tableHeader, tableBody);
        updateSizes();

    }

    public CellTable getTableBody() {
        return tableBody;
    }

    private void updateSizes() {
        tableWidth = plugin.getOwner().getVisibleWidth();
        tableHeight = plugin.getOwner().getVisibleHeight();

    }

//    private void createCollectionData() {
//        CollectionRowsRequest collectionRowsRequest = new CollectionRowsRequest(listCount, 15, collectionName, columnNamesOnDoFieldsMap);
//        Command command = new Command("generateCollectionRowItems", "collection.plugin", collectionRowsRequest);
//
//        BusinessUniverseServiceAsync.Impl.getInstance().executeCommand(command, new AsyncCallback<Dto>() {
//            @Override
//            public void onFailure(Throwable caught) {
//                GWT.log("something was going wrong while obtaining generateCollectionRowItems for ''");
//                caught.printStackTrace();
//            }
//
//            @Override
//            public void onSuccess(Dto result) {
//                CollectionRowItemList collectionRowItemList = (CollectionRowItemList) result;
//                List<CollectionRowItem> collectionRowItems = collectionRowItemList.getCollectionRows();
//                insertMoreRows(collectionRowItems);
//
//            }
//        });
//    }

    @Override
    protected IsWidget getViewWidget() {

        CollectionPluginData collectionPluginData = plugin.getInitialData();
        collectionName = collectionPluginData.getCollectionName();
        columnNamesOnDoFieldsMap = collectionPluginData.getDomainObjectFieldOnColumnNameMap();
        items = collectionPluginData.getItems();
        singleChoice = collectionPluginData.isSingleChoice();
        chosenIndexes = collectionPluginData.getIndexesOfSelectedItems();
        init();

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

    private void createTableColumns() {
        if (singleChoice) {
            createTableColumnsWithoutCheckBoxes(columnNamesOnDoFieldsMap, 0);
        } else {
            createTableColumnsWithCheckBoxes(columnNamesOnDoFieldsMap);
        }
    }

    private int columnMinWidth(int width) {
        if (width < 100) {
            width = 100;
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
                tableController.columnWindowResize(columnMinWidth(event.getUpperPanelWidth() / tableBody.getColumnCount()));
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
                tableController.columnWindowResize(columnMinWidth(event.getFirstWidgetWidth() / tableBody.getColumnCount()));
            }
        });


        scrollTableBody.addScrollHandler(new ScrollHandler() {
            @Override
            public void onScroll(ScrollEvent event) {

                if (scrollStart + 25 < scrollTableBody.getVerticalScrollPosition() || scrollStart == scrollTableBody.
                        getMaximumVerticalScrollPosition()) {
                    createCollectionData();
                    scrollStart = scrollTableBody.getVerticalScrollPosition();
                    listCount += 15;
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
    }

    // метод для удаления из коллекции
    public void delCollectionRow(Id collectionObject) {
        // ищем какаой элемент коллекции должен быть удален
        int index = 0;
        for (CollectionRowItem i : items) {
            if (i.getId().toStringRepresentation().equalsIgnoreCase(collectionObject.toStringRepresentation())) {
                index = items.indexOf(i);
            }
        }

        // если в коллекции не было элементов
        // операция удаления ни к чему не приводит
        if (items.size() == 0)
            return;

        // удаляем из коллекции
        items.remove(index);

        // если в коллекции был один элемент перед удалением
        if (index == items.size() && items.size() == 0) {
            tableBody.setRowData(items);
            tableBody.redraw();
            tableBody.flush();
            return;
        }
        // выделение строки - если удалили последнюю строку, выделяем предыдущую
       if (index == items.size()) {
           index--;
       }

       CollectionRowItem itemRow = items.get(index);
       if (index > 0)
           selectionModel.setSelected(itemRow, true);
       if (index == 0)
           selectionModel.setSelected(itemRow, false);
       // обновляем таблицу
       tableBody.setRowData(items);
       tableBody.redraw();
       tableBody.flush();
    }
    // метод для обновления коллекции
    public void refreshCollection(IdentifiableObject collectionObject) {
        CollectionRowItem item = new CollectionRowItem();
        LinkedHashMap<String, Value> rowValues = new LinkedHashMap<String, Value>();
        for (String field : columnNamesOnDoFieldsMap.keySet()) {
             Value value = null;
             value = collectionObject.getValue(field);

            if (field.equalsIgnoreCase("id")) {
                value = new StringValue(collectionObject.getId().toStringRepresentation().toLowerCase());
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

    private TextColumn<CollectionRowItem> buildNameColumn(final String string) {

        return new TextColumn<CollectionRowItem>() {
            @Override
            public String getValue(CollectionRowItem object) {
                return object.getStringValue(string);
            }
        };
    }

    private void buildPanel() {
        headerPanel.add(tableHeader);
        bodyPanel.add(tableBody);
        verticalPanel.add(headerPanel);
        scrollTableBody.getElement().getStyle().setOverflowX(Style.Overflow.HIDDEN);

        scrollTableBody.setHeight(tableHeight + "px");
        scrollTableBody.add(bodyPanel);
        verticalPanel.add(scrollTableBody);
        verticalPanel.setSize("100%", "100%");
        root.add(verticalPanel);

    }

    private void createTableColumnsWithCheckBoxes(LinkedHashMap<String, String> domainObjectFieldsOnColumnNamesMap) {
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
        createTableColumnsWithoutCheckBoxes(domainObjectFieldsOnColumnNamesMap, 1);

    }

    private void createTableColumnsWithoutCheckBoxes(LinkedHashMap<String, String> domainObjectFieldsOnColumnNamesMap, int startNumberOfColumns) {
        int numberOfColumns = startNumberOfColumns + domainObjectFieldsOnColumnNamesMap.keySet().size();
        int columnWidth = (tableWidth / numberOfColumns);
        for (String field : domainObjectFieldsOnColumnNamesMap.keySet()) {
            Column<CollectionRowItem, String> column = buildNameColumn(field);
            String columnName = domainObjectFieldsOnColumnNamesMap.get(field);
            tableHeader.addColumn(column, columnName);
            tableHeader.setColumnWidth(column, columnWidth + "px");
            column.setDataStoreName(columnName);
            tableBody.addColumn(column);
            tableBody.setColumnWidth(column, columnWidth + "px");
        }

    }

    public void insertRows(List<CollectionRowItem> list) {
        tableBody.setRowData(items);
        listCount = items.size();
        selectChosenRows();
    }

    private void insertMoreRows(List<CollectionRowItem> list) {

        items.addAll(list);
        tableBody.setRowData(items);
    }

    private void selectChosenRows() {
        for (Integer index : chosenIndexes) {
            CollectionRowItem rowItem = items.get(index);
            selectionModel.setSelected(rowItem, true);

        }
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
        String emptyTableText = null;

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
        CollectionPluginData collectionPluginData = plugin.getInitialData();

        CollectionRowsRequest collectionRowsRequest = new CollectionRowsRequest(listCount, 15,
                collectionName, columnNamesOnDoFieldsMap);
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

//    private void addHandlers() {
//        addResizeHandler();
//        tableBody.addCellPreviewHandler(new CellTableEventHandler<CollectionRowItem>(tableBody, plugin, eventBus));
//
//        eventBus.addHandler(SplitterInnerScrollEvent.TYPE, new SplitterInnerScrollEventHandler() {
//            @Override
//            public void setScrollPanelHeight(SplitterInnerScrollEvent event) {
//
//                scrollTableBody.setHeight((event.getUpperPanelHeight() - (headerPanel.getOffsetHeight())) + "px");
//                tableController.columnWindowResize(columnMinWidth(event.getUpperPanelWidth() / tableBody.getColumnCount()));
//            }
//        });
//
//        eventBus.addHandler(SplitterWidgetResizerEvent.TYPE, new SplitterWidgetResizerEventHandler() {
//
//            @Override
//            public void setWidgetSize(SplitterWidgetResizerEvent event) {
//
//                if ((event.getFirstWidgetHeight() * 2) < Window.getClientHeight()) {
//                    scrollTableBody.setHeight(((event.getFirstWidgetHeight() * 2) - headerPanel.getOffsetHeight()) + "px");
//                } else {
//                    scrollTableBody.setHeight((event.getFirstWidgetHeight() - headerPanel.getOffsetHeight()) + "px");
//                }
//
//                tableController.columnWindowResize(columnMinWidth(event.getFirstWidgetWidth() / tableBody.getColumnCount()));
//            }
//        });
//
//
//        scrollTableBody.addScrollHandler(new ScrollHandler() {
//            @Override
//            public void onScroll(ScrollEvent event) {
//
//                if (scrollStart + 25 < scrollTableBody.getVerticalScrollPosition() || scrollStart == scrollTableBody.
//                        getMaximumVerticalScrollPosition()) {
//                    createCollectionData();
//                    scrollStart = scrollTableBody.getVerticalScrollPosition();
//                    listCount += 15;
//                }
//
//            }
//        });
//
//    }

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

