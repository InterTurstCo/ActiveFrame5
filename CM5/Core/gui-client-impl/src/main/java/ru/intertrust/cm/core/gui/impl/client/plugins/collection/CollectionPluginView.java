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
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.event.SplitterInnerScrollEvent;
import ru.intertrust.cm.core.gui.impl.client.event.SplitterInnerScrollEventHandler;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.CellTableEventHandler;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.CheckedSelectionModel;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.TableController;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.resources.CellTableResourcesEx;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.resources.DGCellTableResourceAdapter;
import ru.intertrust.cm.core.gui.model.plugin.CollectionPluginData;
import ru.intertrust.cm.core.gui.model.plugin.CollectionRowItem;

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
    private CellTable<CollectionRowItem> tableBody;
    private ScrollPanel scrollTableBody = new ScrollPanel();
    private TableController tableController;
    private ArrayList<CollectionRowItem> items;
    private LinkedHashMap<String, String> columnNamesOnDoFieldsMap;
    private FlowPanel headerPanel = new FlowPanel();
    private FlowPanel bodyPanel = new FlowPanel();
    private VerticalPanel verticalPanel = new VerticalPanel();
    private FlowPanel root = new FlowPanel();

    /**
     * Создание стилей для ящеек таблицы
     */
    private final DGCellTableResourceAdapter adapter;

    protected CollectionPluginView(Plugin plugin) {
        super(plugin);
        adapter = new DGCellTableResourceAdapter(CellTableResourcesEx.I);
        tableHeader = new CellTable<CollectionRowItem>(999, adapter.getResources());
        tableBody = new CellTable<CollectionRowItem>(999, adapter.getResources());
        tableController = new TableController(tableHeader, tableBody);

        plugin.getEventBus().addHandler(SplitterInnerScrollEvent.TYPE, new SplitterInnerScrollEventHandler() {
            @Override
            public void setScrollPanelHeight(SplitterInnerScrollEvent event) {

                scrollTableBody.setHeight((event.getUpperPanelHeight() - headerPanel.getOffsetHeight()) + "px");
            }
        });
    }

    @Override
    protected IsWidget getViewWidget() {

        CollectionPluginData collectionPluginData = plugin.getInitialData();
        columnNamesOnDoFieldsMap = collectionPluginData.getDomainObjectFieldOnColumnNameMap();
        items = collectionPluginData.getItems();
        init();
        return root;

    }

    public void init() {
        buildPanel();
        buildTableColumns(columnNamesOnDoFieldsMap);
        insertRows(items);
        applyStyles();
        addHandlers();

    }

    private void addResizeHandler() {

        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                int width = (Window.getClientWidth() - 235) / tableHeader.getColumnCount();
                if (width < 100) {
                    width = 100;
                }
                tableController.columnWindowResize(width);
                scrollTableBody.setHeight(((Window.getClientHeight() - 300) / 2) + "px");
            }
        });
    }

    private void addHandlers() {
        addResizeHandler();
        tableHeader.addCellPreviewHandler(new CellTableEventHandler<CollectionRowItem>(tableHeader, plugin));
    }


    private TextColumn<CollectionRowItem> buildNameColumn(final String s) {

        return new TextColumn<CollectionRowItem>() {
            @Override
            public String getValue(CollectionRowItem object) {
                return object.getStringValue(s);
            }
        };
    }

    private void buildPanel() {
        headerPanel.add(tableHeader);
        bodyPanel.add(tableBody);
        verticalPanel.add(headerPanel);
        scrollTableBody.getElement().getStyle().setOverflowX(Style.Overflow.HIDDEN);
        scrollTableBody.setHeight(((Window.getClientHeight() - 300) / 2) + "px");
        scrollTableBody.add(bodyPanel);
        verticalPanel.add(scrollTableBody);
        verticalPanel.setSize("100%", "100%");
        root.add(verticalPanel);

    }

    private void buildTableColumns(LinkedHashMap<String, String> domainObjectFieldsOnColumnNamesMap) {
        int count = domainObjectFieldsOnColumnNamesMap.keySet().size();
        int columnWidth = ((Window.getClientWidth() - 235) / count);
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

    private void insertRows(List<CollectionRowItem> items) {
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

    private void applyBodyTableStyle() {
        final CheckedSelectionModel<CollectionRowItem> selectionModel = new CheckedSelectionModel<CollectionRowItem>();
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

}

