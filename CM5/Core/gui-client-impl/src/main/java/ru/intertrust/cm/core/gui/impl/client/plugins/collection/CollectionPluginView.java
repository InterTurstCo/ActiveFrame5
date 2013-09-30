package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.ListDataProvider;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.CellTableEventHandler;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.CheckedSelectionModel;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.ColumnResizeController;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.SystemColumns;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.resources.DGCellTableResourceAdapter;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.resources.DGCellTableResourcesCommon;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.resources.DynamicGridStyles;
import ru.intertrust.cm.core.gui.model.plugin.CollectionPluginData;
import ru.intertrust.cm.core.gui.model.plugin.MyData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 17/9/13
 *         Time: 12:05 PM
 */
public class CollectionPluginView extends PluginView {
    CellTable<MyData> tableHeader;
    CellTable<MyData> tableBody;

    FlowPanel headerPanel = new FlowPanel();
    SimplePanel bodyPanel = new SimplePanel();
    VerticalPanel verticalPanel = new VerticalPanel();
    ScrollPanel root = new ScrollPanel();

    private List<MyData> tableContent =new ArrayList<MyData>();
    /**
     * Создание стилей для ящеек таблицы
     */
    private final DGCellTableResourceAdapter adapter;
    private static DynamicGridStyles dgStyles = DynamicGridStyles.I;
    static {
        DGCellTableResourcesCommon.I.cellTableStyle().ensureInjected();
        dgStyles.csfStyle().ensureInjected();
        dgStyles.dgStyle().ensureInjected();

    }

    protected CollectionPluginView(Plugin plugin) {
        super(plugin);
        adapter = new DGCellTableResourceAdapter(DGCellTableResourcesCommon.I);
        tableHeader = new CellTable<MyData>(999,  adapter.getResources());
        tableBody = new CellTable<MyData>(999, adapter.getResources());

    }
    public List<MyData> getTableContent() {
        return tableContent;
    }

    public void setTableContent(List<MyData> tableContent) {
        this.tableContent = tableContent;
    }

    @Override
    protected IsWidget getViewWidget() {

        CollectionPluginData collectionPluginData =  plugin.getInitialData();
        List<Id> ids = collectionPluginData.getIds();
        IdentifiableObjectCollection collection = collectionPluginData.getCollection();
        List<String> columnNames = collectionPluginData.getColumnNames();
        List<List<String>> columnData = collectionPluginData.getStringList();
        init(columnNames, columnData);
        return root;

    }

    public void init(List<String> columnName, List<List<String>> myStringList){

        buildPanel();
        setLinearSizes();
        settingStyles();
        addHandlers();
        transformIncomingData(myStringList);
        buildColumnsOfTables(columnName);
        attachingDataProvider(tableBody, tableContent);
        draw();

    }

    private void addResizeHandler(){

        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
            tableHeader.redraw();
            tableBody.redraw();
             draw();
            }
        });

    }
    private void addHandlers(){
        addResizeHandler();
        tableBody.addCellPreviewHandler(new CellTableEventHandler<MyData>(tableBody, plugin));
    }

    private void draw(){
        for (int i =0; i < tableHeader.getColumnCount(); i++){
            tableHeader.setWidth("100%", true);
            tableBody.setWidth("100%", true);
            int headerPanelwidth = 100/tableHeader.getColumnCount();
            System.out.println("lenght: " + headerPanelwidth);
            if (headerPanelwidth < 35) {
                headerPanelwidth = 35;
            }
            tableHeader.setColumnWidth(tableHeader.getColumn(i), headerPanelwidth+ "%");
            tableBody.setColumnWidth(tableBody.getColumn(i), headerPanelwidth+"%");

        }
    }

    private TextColumn<MyData> buildNameColumn(int key){
        final  String keyWrapped = String.valueOf(key);
        return new TextColumn<MyData>()  {
            @Override
            public String getValue(MyData object) {
                return object.getValueByKey(keyWrapped, String.class);
            }
        };
    }

    private void setLinearSizes(){
        root.setHeight("150px");
        bodyPanel.setWidth("100%");

    }

    private void buildPanel(){

        headerPanel.add(tableHeader);
        bodyPanel.add(tableBody);
        verticalPanel.add(headerPanel);
        verticalPanel.add(bodyPanel);

        root.add(verticalPanel);

    }

    private void buildColumnsOfTables(List<String> columnNames){
        for( int i = 0; i <columnNames.size(); i++ ) {
            Column<MyData, String> column = buildNameColumn(i);
            String nameOfColumn = columnNames.get(i);
            tableHeader.addColumn(column, new ColumnResizeController<MyData>(nameOfColumn, tableHeader,tableBody, column));
            tableBody.addColumn(column);
        }

        tableBody.setRowCount(tableContent.size());

    }
    private void transformIncomingData(List<List<String>> stringLists) {
        List<MyData>  myDataList = new ArrayList<MyData>();
        for(List<String> list: stringLists) {
            MyData myData = createNode(list);
            myDataList.add(myData);
        }
        setTableContent(myDataList);

    }
    private MyData createNode(List<String> list) {
        HashMap<String, Object> values = new HashMap<String,Object>();
        int count = 0;
        for(String s: list) {
            values.put(String.valueOf(count), s);
            count++;
        }

        return new MyData( values );
    }
    private void attachingDataProvider(CellTable<MyData> cellTable, List<MyData> myDataList) {
        ListDataProvider<MyData> dataProvider = new ListDataProvider<MyData>(myDataList);
        // Connect the table to the data provider.
        dataProvider.addDataDisplay(cellTable);
    }
    private void settingStyles() {
        setStyleOfHeaderTable();
        setStyleOfBodyTable();
    }
      private void setStyleOfHeaderTable() {
          tableHeader.setStyleName(adapter.getResources().cellTableStyle().docsCelltableHeader());
          tableHeader.setTableLayoutFixed(true);
          headerPanel.setStyleName(adapter.getResources().cellTableStyle().docsCelltableHeaderPanel());

          tableBody.setStyleName(adapter.getResources().cellTableStyle().docsCelltableBody());
      }
    private void setStyleOfBodyTable() {
        final CheckedSelectionModel<MyData> selectionModel = new CheckedSelectionModel<MyData>();
        String emptyTableText = null;

        HTML emptyTableWidget = new HTML("<br/><div align='center'> <h1> " + emptyTableText + " </h1> </div>");
        emptyTableWidget.getElement().getStyle().setPaddingLeft(60, Style.Unit.PX);

        tableBody.setRowStyles(new RowStyles<MyData>() {
            @Override
            public String getStyleNames(MyData row, int rowIndex) {
                String style = adapter.getResources().cellTableStyle().docsCelltableTrCommon();
                if (row != null) {
                    Boolean value = row.getValueByKey(SystemColumns.ISUNREAD.getColumnName(), Boolean.class);
                    if (value != null && value.booleanValue()) {
                        style = adapter.getResources().cellTableStyle().docsCelltableTrUnread();
                    }
                }
                return style;
            }
        });

        tableBody.setEmptyTableWidget(emptyTableWidget);
        tableBody.setSelectionModel(selectionModel);
        tableBody.setTableLayoutFixed(true);
    }
 }

