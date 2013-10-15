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
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.model.gui.collection.view.CollectionColumnConfig;
import ru.intertrust.cm.core.config.model.gui.collection.view.CollectionDisplayConfig;
import ru.intertrust.cm.core.config.model.gui.collection.view.CollectionViewConfig;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.CellTableEventHandler;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.CheckedSelectionModel;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.SystemColumns;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.TableController;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.resources.CellTableResourcesEx;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.resources.DGCellTableResourceAdapter;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.plugin.CollectionData;
import ru.intertrust.cm.core.gui.model.plugin.CollectionPluginData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 17/9/13
 *         Time: 12:05 PM
 */
public class CollectionPluginView extends PluginView {
    CellTable<CollectionData> tableHeader;
    CellTable<CollectionData> tableBody;
/*    SplitLayoutPanel splitterSize;*/
    TableController tableController;
    List<String> columnNames;
    List<String> columnFields;


    FlowPanel headerPanel = new FlowPanel();
    SimplePanel bodyPanel = new SimplePanel();
    VerticalPanel verticalPanel = new VerticalPanel();
    ScrollPanel root = new ScrollPanel();

    private List<CollectionData> tableContent =new ArrayList<CollectionData>();
    /**
     * Создание стилей для ящеек таблицы
     */
    private final DGCellTableResourceAdapter adapter;

    protected CollectionPluginView(Plugin plugin/*, SplitLayoutPanel splitterSize*/) {
        super(plugin);
     /*   this.splitterSize = splitterSize;*/
        adapter = new DGCellTableResourceAdapter(CellTableResourcesEx.I);
        tableHeader = new CellTable<CollectionData>(999,  adapter.getResources());
        tableBody = new CellTable<CollectionData>(999, adapter.getResources());
        tableController = new TableController(tableHeader, tableBody, headerPanel);



    }

    @Override
    protected IsWidget getViewWidget() {

        CollectionPluginData collectionPluginData =  plugin.getInitialData();

        IdentifiableObjectCollection collection = collectionPluginData.getCollection();
        CollectionViewConfig collectionViewConfig = collectionPluginData.getCollectionViewConfig();
        columnNames = getColumnNames(collectionViewConfig);
        columnFields = getColumnFields(collectionViewConfig);

        init(columnNames, collection, columnFields);

        return root;

    }

    public void init(List<String> columnNames, IdentifiableObjectCollection collection, List<String> columnFields){

        buildPanel();
        buildColumnsOfTables(columnNames, columnFields);
        setLinearSizes();
        settingStyles();

        preparingTableContent(collection, columnFields);
        attachingDataProvider(tableBody, tableContent);

        addHandlers();

       // draw();

    }

    private void headerAndBodyResize(){
//        headerPanel.setWidth("100%");
//        bodyPanel.setWidth("100%");
    }

    private void addResizeHandler(){

        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
            tableHeader.redraw();
            tableBody.redraw();
             //draw();
            }
        });

    }
    private void addHandlers(){
       // addResizeHandler();
        tableBody.addCellPreviewHandler(new CellTableEventHandler<CollectionData>(tableBody, plugin));
    }

    private void draw(){
        for (int i =0; i < tableHeader.getColumnCount(); i++){
//            tableHeader.setWidth("100%", true);
//            tableBody.setWidth("100%", true);
            int headerPanelwidth = 100/tableHeader.getColumnCount();
            if (headerPanelwidth < 35) {
                headerPanelwidth = 35;
            }
            tableHeader.setColumnWidth(tableHeader.getColumn(i), headerPanelwidth+ "%");
            tableBody.setColumnWidth(tableBody.getColumn(i), headerPanelwidth+"%");

        }
    }

    private TextColumn<CollectionData> buildNameColumn(final String s){

        return new TextColumn<CollectionData>()  {
            @Override
            public String getValue(CollectionData object) {
                return object.getValueByKey(s, String.class);
            }
        };
    }

    private void setLinearSizes(){
     //   root.setHeight("150px");
      //  bodyPanel.setWidth("100%");

    }

    private void buildPanel(){

        headerPanel.add(tableHeader);
        bodyPanel.add(tableBody);
        verticalPanel.add(headerPanel);
        verticalPanel.add(bodyPanel);
      //  verticalPanel.setSize("100%","100%");


        root.add(verticalPanel);

    }

    private void buildColumnsOfTables(List<String> columnNames, List<String> columnFields){
        int count =columnNames.size();
        int columnSize = (headerPanel.getOffsetWidth() / count);
        for( int i = 0; i <columnNames.size(); i++ ) {
            Column<CollectionData, String> column = buildNameColumn(columnFields.get(i));
            String nameOfColumn = columnNames.get(i);
            tableHeader.addColumn(column, nameOfColumn);
            tableHeader.setColumnWidth(column, 300/*columnSize*/+"px");
            column.setDataStoreName(nameOfColumn);
            tableBody.addColumn(column);
            tableBody.setColumnWidth(column, 300/*columnSize*/+"px");
        }



        tableBody.setRowCount(tableContent.size());
//        headerPanel.setWidth("700px");
      //  draw();

    }



    private void attachingDataProvider(CellTable<CollectionData> cellTable, List<CollectionData> myDataList) {
        ListDataProvider<CollectionData> dataProvider = new ListDataProvider<CollectionData>(myDataList);
        // Connect the table to the data provider.
        dataProvider.addDataDisplay(cellTable);
    }

    private void settingStyles() {
        setStyleOfHeaderTable();
        setStyleOfBodyTable();
    }

    private void setStyleOfHeaderTable() {
          tableHeader.setStyleName(adapter.getResources().cellTableStyle().docsCommonCelltableHeader());
          tableHeader.setTableLayoutFixed(true);
          headerPanel.setStyleName(adapter.getResources().cellTableStyle().docsCommonCelltableHeaderPanel());

          tableBody.setStyleName(adapter.getResources().cellTableStyle().docsCommonCelltableBody());
     }

    private void setStyleOfBodyTable() {
        final CheckedSelectionModel<CollectionData> selectionModel = new CheckedSelectionModel<CollectionData>();
        String emptyTableText = null;

        HTML emptyTableWidget = new HTML("<br/><div align='center'> <h1> " + emptyTableText + " </h1> </div>");
        emptyTableWidget.getElement().getStyle().setPaddingLeft(60, Style.Unit.PX);

        tableBody.setRowStyles(new RowStyles<CollectionData>() {
            @Override
            public String getStyleNames(CollectionData row, int rowIndex) {
                String style = adapter.getResources().cellTableStyle().docsCommonCelltableTrCommon();
                if (row != null) {
                    Boolean value = row.getValueByKey(SystemColumns.ISUNREAD.getColumnName(), Boolean.class);
                    if (value != null && value.booleanValue()) {
                        style = adapter.getResources().cellTableStyle().docsCommonCelltableTrUnread();
                    }
                }
                return style;
            }
        });

        tableBody.setEmptyTableWidget(emptyTableWidget);
        tableBody.setSelectionModel(selectionModel);
        tableBody.setTableLayoutFixed(true);
    }

    private void preparingTableContent
            (IdentifiableObjectCollection identifiableObjectCollection, List<String> columnFields) {

        for( int i = 0; i < identifiableObjectCollection.size(); i++){
            HashMap<String, Object> values = new HashMap<String,Object>();
            IdentifiableObject identifiableObject = identifiableObjectCollection.get(i);

            for(String field: columnFields){
                String fieldValue;
                if ("id".equalsIgnoreCase(field)) {
                    fieldValue = identifiableObject.getId().toStringRepresentation();
                } else {
                    Value value = identifiableObject.getValue(field);
                    fieldValue = value == null || value.get() == null ? "" : value.get().toString();
                }
                values.put(field, fieldValue);

            }

            tableContent.add(new CollectionData(values));
        }

    }
    private List<String> getColumnFields(CollectionViewConfig collectionViewConfig){
        List<String> columnFields = new ArrayList<String>();
        CollectionDisplayConfig collectionDisplay = collectionViewConfig.getCollectionDisplayConfig();
        List<CollectionColumnConfig> columnConfigs = collectionDisplay.getColumnConfig();
        for (CollectionColumnConfig collectionColumnConfig : columnConfigs) {
            if (collectionColumnConfig.isHidden()) {
                continue;
            }
            String columnName = collectionColumnConfig.getField();
            columnFields.add(columnName);
        }
        return  columnFields;
    }
    private List<String> getColumnNames(CollectionViewConfig collectionViewConfig){
        List<String> columnNames = new ArrayList<String>();
        CollectionDisplayConfig collectionDisplay = collectionViewConfig.getCollectionDisplayConfig();
        if(collectionDisplay != null) {
            List<CollectionColumnConfig> columnConfigs = collectionDisplay.getColumnConfig();
            for (CollectionColumnConfig collectionColumnConfig : columnConfigs) {
                if (collectionColumnConfig.isHidden()) {
                    continue;
                }
                String columnName = collectionColumnConfig.getName();
                columnNames.add(columnName);
            }
            return  columnNames;

        } else throw  new GuiException("Collection view config has no display tags configured ");

    }

}

