package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.CellPreviewEvent;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.CellTableBody;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.CellTableEx;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.CellTableHeader;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.ColumnResizeController;
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
    CellTableHeader<MyData> tableHeader;
    CellTableBody<MyData> tableBody;

    SimplePanel cellViewPanel = new SimplePanel();
    ScrollPanel scrollViewPanel = new ScrollPanel();
    HTMLPanel rootPathHeader = new HTMLPanel("");
    FlowPanel headerPanel = new FlowPanel();
    HTMLPanel root =  new HTMLPanel("");
    SimplePanel dummyHeader = new SimplePanel();

    /**
     * Создание стилей для ящеек таблицы
     */
    private static DynamicGridStyles dgStyles = DynamicGridStyles.I;
    static {
        DGCellTableResourcesCommon.I.cellTableStyle().ensureInjected();
        dgStyles.csfStyle().ensureInjected();
        dgStyles.dgStyle().ensureInjected();

    }
    /**
     * Вспомогательные компоненты в uibinder
     */
    HorizontalPanel horizontalPanel = new HorizontalPanel();
    VerticalPanel verticalPanelFirst = new VerticalPanel();
    VerticalPanel verticalPanelSecond = new VerticalPanel();
    private final DGCellTableResourceAdapter adapter;

    protected CollectionPluginView(Plugin plugin) {
        super(plugin);
        adapter = new DGCellTableResourceAdapter(DGCellTableResourcesCommon.I);
        tableHeader = new CellTableHeader<MyData>(adapter);
        tableBody = new CellTableBody<MyData>(adapter);
    }

    @Override
    protected IsWidget getViewWidget() {

        CollectionPluginData collectionPluginData =  plugin.getInitialData();
        List<String> columnNames = collectionPluginData.getColumnNames();
        init(columnNames, collectionPluginData.getStringList());
        return root;

    }
    private List<MyData> tableContent =new ArrayList<MyData>();

    public List<MyData> getTableContent() {
        return tableContent;
    }

    public void setTableContent(List<MyData> tableContent) {
        this.tableContent = tableContent;
    }

    public void init(List<String> columnName, List<List<String>> myStringList){
        transformToMyDataList(myStringList);
        createCellTableHeader(columnName);
        root.add(headerPanel);
        headerPanelResize();
        buildPanel();
        show();
        draw();

    }

    public void headerPanelResize(){

        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                tableHeader.redraw();
                tableBody.redraw();
                draw();
            }
        });

    }

    private void draw(){
        for (int i =0; i < tableHeader.getColumnCount(); i++){
            int headerPanelwidth =  headerPanel.getOffsetWidth()/tableHeader.getColumnCount();
            if (headerPanelwidth < 350){
                headerPanelwidth = 350;
            }
            tableHeader.setColumnWidth(tableHeader.getColumn(i), headerPanelwidth+"px");
            tableBody.setColumnWidth(tableBody.getColumn(i), headerPanelwidth+"px");

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

    private void show(){
        headerPanel.clear();
        headerPanel.setStyleName(adapter.getResources().cellTableStyle().docsCelltableHeaderPanel());
        headerPanel.add(tableHeader);
        cellViewPanel.add(tableBody);
        cellViewPanel.setWidth("100%");
        horizontalPanel.setHeight("100%");
        //root.setHeight("500px");

    }

    private void buildPanel(){
        root.add(horizontalPanel);
        horizontalPanel.add(scrollViewPanel);
        scrollViewPanel.add(verticalPanelFirst);
        verticalPanelFirst.add(rootPathHeader);
        verticalPanelFirst.add(headerPanel);
        verticalPanelFirst.add(cellViewPanel);
        verticalPanelSecond.add(dummyHeader);

        scrollViewPanel.setHeight("300px");
        //  rootPathHeader.addStyleName("docs-ctg-listContainerHeader");
    }

    public void createCellTableHeader(List<String> columnNames){
        for( int i = 0; i <columnNames.size(); i++ ) {
            Column<MyData, String> column = buildNameColumn(i);
            String nameOfColumn = columnNames.get(i);
            tableHeader.addColumn(column, new ColumnResizeController<MyData>(nameOfColumn, tableHeader,tableBody, column));
            tableBody.addColumn(column);
        }

        tableBody.setRowCount(tableContent.size());
        tableBody.setData(tableContent);

    }
    public void transformToMyDataList(List<List<String>> stringLists) {
        List<MyData>  myDataList = new ArrayList<MyData>();
        for(List<String> list: stringLists) {
            MyData myData = createNode( list );
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
    private class CellTableEventHandler<T1> implements CellPreviewEvent.Handler<T1> {
                 private CellTableEx<MyData> cellTableEx;
        private CellTableEventHandler() {

        }

        private CellTableEventHandler(CellTableEx<MyData> cellTableEx) {
            this.cellTableEx = cellTableEx;
        }

        /**
         * Обработка события {@link com.google.gwt.view.client.CellPreviewEvent}.
         * Обрабатываются события типа:
         * "click" - устанавливается текущая строка и колонка таблицы;
         * "focus" - устанавливается текущая колонка;
         * "touchstart" - запоминается номер строки, которой коснулся пользователь.
         *
         * @param event
         *          Экземпляр {@link com.google.gwt.view.client.CellPreviewEvent}
         */
        @Override
        public void onCellPreview(CellPreviewEvent<T1> event) {
            NativeEvent nativeEvent = event.getNativeEvent();
            if ("click".equals(nativeEvent.getType())) {
                int currentRow = event.getIndex();
                cellTableEx.setCurrentColumn(event.getColumn());
                MyData myData = cellTableEx.getDataProvider().getList().get(currentRow);
                Window.alert("selected  " + myData.getRowValues() );
                if (currentRow != cellTableEx.getCurrentRow()) {
                    cellTableEx.setCurrentRow(currentRow);
                }
            }
            else if ("focus".equals(nativeEvent.getType())) {
                cellTableEx.setCurrentColumn(event.getColumn());
            }
            else if ("touchstart".equals(nativeEvent.getType())) {
                cellTableEx.setTouchRow(event.getIndex());
            }
        }
    }

 }

