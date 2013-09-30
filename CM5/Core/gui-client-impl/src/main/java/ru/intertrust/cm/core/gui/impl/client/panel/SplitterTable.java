package ru.intertrust.cm.core.gui.impl.client.panel;


import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.gui.impl.client.resources.DGCellTableResourceAdapter;
import ru.intertrust.cm.core.gui.impl.client.resources.DGCellTableResourcesCommon;
import ru.intertrust.cm.core.gui.impl.client.resources.DynamicGridStyles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: lvov
 * Date: 12.09.13
 * Time: 11:58
 * To change this template use File | Settings | File Templates.
 */
public class SplitterTable { // extends Composite {

    private static MyData createNode(String status, String docType, String sygnatory, String addresser, String header, String union, String topic) {
        HashMap<String, Object> map1 = new HashMap<String, Object>();
        map1.put("1", status);
        map1.put("2", docType);
        map1.put("3", sygnatory);
        map1.put("4", addresser);
        map1.put("5", header);
        map1.put("6", union);
        map1.put("7", topic);

        return new MyData("type", map1, null, null);
    }

    static List<MyData> MYDATA;

    static {
        MyData line1 = createNode("sss", "Акт", "Аэрофлот", "Пупкин В", "Заголовок документа", "", "МЭДО");
        MyData line2 = createNode("sss", "Акт", "Аэрофлот", "Пупкин В", "Заголовок документа", "", "МЭДО");
        MyData line3 = createNode("sss", "Акт", "Аэрофлот", "Пупкин В", "Заголовок документа", "", "МЭДО");
        MyData line4 = createNode("sss", "Акт", "Аэрофлот", "Пупкин В", "Заголовок документа", "", "МЭДО");
        MyData line5 = createNode("sss", "Акт", "Аэрофлот", "Пупкин В", "Заголовок документа", "", "МЭДО");
        MyData line6 = createNode("sss", "Акт", "Аэрофлот", "Пупкин В", "Заголовок документа", "", "МЭДО");
        MyData line7 = createNode("sss", "Акт", "Аэрофлот", "Пупкин В", "Заголовок документа", "", "МЭДО");
        MyData line8 = createNode("sss", "Акт", "Аэрофлот", "Пупкин В", "Заголовок документа", "", "МЭДО");
        MyData line9 = createNode("sss", "Акт", "Аэрофлот", "Пупкин В", "Заголовок документа", "", "МЭДО");
        MyData line10 = createNode("sss", "Акт", "Аэрофлот", "Пупкин В", "Заголовок документа", "", "МЭДО");
        MyData line11 = createNode("sss", "Акт", "Аэрофлот", "Пупкин В", "Заголовок документа", "", "МЭДО");
        MyData line12 = createNode("sss", "Акт", "Аэрофлот", "Пупкин В", "Заголовок документа", "", "МЭДО");
        MyData line13 = createNode("sss", "Акт", "Аэрофлот", "Пупкин В", "Заголовок документа", "", "МЭДО");
        MyData line14 = createNode("sss", "Акт", "Аэрофлот", "Пупкин В", "Заголовок документа", "", "МЭДО");
        MyData line15 = createNode("sss", "Акт", "Аэрофлот", "Пупкин В", "Заголовок документа", "", "МЭДО");
        MyData line16 = createNode("sss", "Акт", "Аэрофлот", "Пупкин В", "Заголовок документа", "", "МЭДО");
        MyData line17 = createNode("sss", "Акт", "Аэрофлот", "Пупкин В", "Заголовок документа", "", "МЭДО");


        MYDATA = new ArrayList<MyData>();
        MYDATA.add(line1);
        MYDATA.add(line2);
        MYDATA.add(line3);
        MYDATA.add(line4);
        MYDATA.add(line5);
        MYDATA.add(line6);
        MYDATA.add(line7);
        MYDATA.add(line8);
        MYDATA.add(line9);
        MYDATA.add(line10);
        MYDATA.add(line11);
        MYDATA.add(line12);
        MYDATA.add(line13);
        MYDATA.add(line14);
        MYDATA.add(line15);
        MYDATA.add(line16);
        MYDATA.add(line17);


    }

    /**
     * Создание стилей для ящеек таблицы
     */
    private static DynamicGridStyles dgStyles = DynamicGridStyles.I;


    static {
        DGCellTableResourcesCommon.I.cellTableStyle().ensureInjected();
        dgStyles.csfStyle().ensureInjected();
        dgStyles.dgStyle().ensureInjected();

    }

    private final DGCellTableResourceAdapter adapter;

    public SplitterTable(FlowPanel panel) {
        adapter = new DGCellTableResourceAdapter(DGCellTableResourcesCommon.I);
        tableHeader = new CellTableHeader<MyData>(adapter);
        tableBody = new CellTableBody<MyData>(adapter);
        this.headerPanel = panel;

    }

    CellTableHeader<MyData> tableHeader;
    CellTableBody<MyData> tableBody;
    private int counter = 0;

    SimplePanel cellViewPanel = new SimplePanel();
    ScrollPanel scrollViewPanel = new ScrollPanel();
    HTMLPanel rootPathHeader = new HTMLPanel("");
    FlowPanel headerPanel;
    HTMLPanel root = new HTMLPanel("");
    SimplePanel dummyHeader = new SimplePanel();

    /**
     * Вспомогательные компоненты в uibinder
     */
    HorizontalPanel horizontalPanel = new HorizontalPanel();
    VerticalPanel verticalPanelFirst = new VerticalPanel();
    VerticalPanel verticalPanelSecond = new VerticalPanel();

    public void init() {

        Column<MyData, String> column1 = buildNameColumn();
        Column<MyData, String> column2 = buildName2Column();
        Column<MyData, String> column3 = buildName3Column();
        Column<MyData, String> column4 = buildName4Column();
        Column<MyData, String> column5 = buildName5Column();
        Column<MyData, String> column6 = buildName6Column();
        Column<MyData, String> column7 = buildName7Column();


        tableHeader.addColumn(column1, new ColumnResizeController<MyData>("Статус", tableHeader, tableBody, column1));
        tableHeader.addColumn(column2, new ColumnResizeController<MyData>("Вид документа", tableHeader, tableBody, column2));
        tableHeader.addColumn(column3, new ColumnResizeController<MyData>("Подписант", tableHeader, tableBody, column3));
        tableHeader.addColumn(column4, new ColumnResizeController<MyData>("Адресаты", tableHeader, tableBody, column4));
        tableHeader.addColumn(column5, new ColumnResizeController<MyData>("Заголовок", tableHeader, tableBody, column5));
        tableHeader.addColumn(column6, new ColumnResizeController<MyData>("Связи", tableHeader, tableBody, column6));
        tableHeader.addColumn(column7, new ColumnResizeController<MyData>("Тема", tableHeader, tableBody, column7));


        tableBody.addColumn(column1);
        tableBody.addColumn(column2);
        tableBody.addColumn(column3);
        tableBody.addColumn(column4);
        tableBody.addColumn(column5);
        tableBody.addColumn(column6);
        tableBody.addColumn(column7);
        tableBody.setRowCount(MYDATA.size());
        tableBody.setData(MYDATA);

        root.add(headerPanel);
        headerPanelResize();
        buildPanel();
        show();
        draw();
    }


    public void headerPanelResize() {

        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                tableHeader.redraw();
                tableBody.redraw();
                draw();
            }
        });

    }


    private void draw() {
        for (int i = 0; i < tableHeader.getColumnCount(); i++) {
            int headerPanelwidth = headerPanel.getOffsetWidth() / tableHeader.getColumnCount();
//                if (headerPanelwidth < 350){
//                    headerPanelwidth = 350;
//                }
            tableHeader.setColumnWidth(tableHeader.getColumn(i), headerPanelwidth + "px");
            tableBody.setColumnWidth(tableBody.getColumn(i), headerPanelwidth + "px");

        }
    }


    private TextColumn<MyData> buildNameColumn() {
        return new TextColumn<MyData>() {
            @Override
            public String getValue(MyData object) {
                return object.getValueByKey("1", String.class);
            }
        };
    }

    private TextColumn<MyData> buildName2Column() {
        return new TextColumn<MyData>() {
            @Override
            public String getValue(MyData object) {
                return object.getValueByKey("2", String.class);
            }
        };
    }

    private TextColumn<MyData> buildName3Column() {
        return new TextColumn<MyData>() {
            @Override
            public String getValue(MyData object) {
                return object.getValueByKey("3", String.class);
            }
        };
    }

    private TextColumn<MyData> buildName4Column() {
        return new TextColumn<MyData>() {
            @Override
            public String getValue(MyData object) {
                return object.getValueByKey("4", String.class);
            }
        };
    }

    private TextColumn<MyData> buildName5Column() {
        return new TextColumn<MyData>() {
            @Override
            public String getValue(MyData object) {
                return object.getValueByKey("5", String.class);
            }
        };
    }

    private TextColumn<MyData> buildName6Column() {
        return new TextColumn<MyData>() {
            @Override
            public String getValue(MyData object) {
                return object.getValueByKey("6", String.class);
            }
        };
    }

    private TextColumn<MyData> buildName7Column() {
        return new TextColumn<MyData>() {
            @Override
            public String getValue(MyData object) {
                return object.getValueByKey("7", String.class);
            }
        };
    }

    private void show() {
        headerPanel.clear();
        headerPanel.setStyleName(adapter.getResources().cellTableStyle().docsCelltableHeaderPanel());
        headerPanel.add(tableHeader);
        cellViewPanel.add(tableBody);
        cellViewPanel.setWidth("100%");
        cellViewPanel.setHeight("100%");
        horizontalPanel.setHeight("100%");

        root.setHeight("500px");

        RootPanel.get().add(root);
    }


    private void buildPanel() {
        root.add(horizontalPanel);
        horizontalPanel.add(scrollViewPanel);
        scrollViewPanel.add(verticalPanelFirst);
        verticalPanelFirst.add(rootPathHeader);
        verticalPanelFirst.add(headerPanel);
        verticalPanelFirst.add(cellViewPanel);
        verticalPanelSecond.add(dummyHeader);

        scrollViewPanel.setHeight("100%");
        rootPathHeader.addStyleName("docs-ctg-listContainerHeader");
    }

    public HTMLPanel getRoot() {
        return root;
    }
}
