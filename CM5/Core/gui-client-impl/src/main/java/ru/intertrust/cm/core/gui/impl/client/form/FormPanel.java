package ru.intertrust.cm.core.gui.impl.client.form;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.config.gui.form.*;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetDisplayConfig;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.model.form.FormDisplayData;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 15.09.13
 *         Time: 14:53
 */
public class FormPanel implements IsWidget {
    private int formWidth;
    private int formHeight;
    private HackTabLayoutPanel bodyTabPanel;
    private FormDisplayData formDisplayData;
    private List<BaseWidget> widgets;
    private FlowPanel panel = new FlowPanel();
    private boolean isHeightFromConfig;
    private boolean isWidthFromConfig;
    private List<TabConfig> tabs;

    public FormPanel(FormDisplayData formDisplayData, int width, int height) {
        this(formDisplayData);
        formWidth = width;
        formHeight = height;

    }

    public FormPanel(FormDisplayData formDisplayData) {

        this.formDisplayData = formDisplayData;
        widgets = new ArrayList<BaseWidget>(formDisplayData.getFormState().getFullWidgetsState().size());

    }

    public FlowPanel getPanel() {
        return panel;
    }

    public void updateSizes(int width, int height) {
        panel.setSize(width + "px", height + "px");
      //  bodyTabPanel.setSize(width + "px", height + "px");

    }

    @Override
    public com.google.gwt.user.client.ui.Widget asWidget() {

        return build();
    }

    public List<BaseWidget> getWidgets() {
        return widgets;
    }

    public void update(FormState formState) {
        for (BaseWidget widget : widgets) {
            WidgetState newState = formState.getWidgetState(widget.getDisplayConfig().getId());
            WidgetState currentState = widget.getCurrentState();
            if (!newState.equals(currentState)) {
                widget.setState(newState);
            }
        }
        this.formDisplayData.setFormState(formState);
    }


    private FlowPanel build() {
        setMinimalFormWidth();
        MarkupConfig markup = formDisplayData.getMarkup();
        IsWidget headerTable = buildHeader(markup);
        buildTabs(markup);

        bodyTabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
            @Override
            public void onSelection(SelectionEvent<Integer> event) {
                setStyleForAllTabs(event.getSelectedItem(), bodyTabPanel);

            }
        });
        panel.add(headerTable);
        panel.add(bodyTabPanel);

        return panel;
    }

    private IsWidget buildHeader(MarkupConfig markup) {
        HeaderConfig header = markup.getHeader();
        IsWidget headerTable = buildTable(header.getTableLayout());
        String configHeight = header.getTableLayout().getHeight();
        if (isHeightDeclaredInConfig(configHeight)) {
            headerTable.asWidget().setHeight(configHeight);
        }
        String configWidth = header.getTableLayout().getWidth();
        if (isWidthDeclaredInConfig(configWidth)) {
            headerTable.asWidget().setWidth(configWidth);
        }
        return headerTable;
    }

    private void buildTabs(MarkupConfig markup) {
        BodyConfig body = markup.getBody();
        tabs = body.getTabs();

        if (body.isDisplaySingleTab() == true && tabs.size() == 1) {

            bodyTabPanel = new HackTabLayoutPanel(0, Style.Unit.PX);
            bodyTabPanel.add(buildTabContent(tabs.get(0)));
        } else {
            bodyTabPanel = new HackTabLayoutPanel(35, Style.Unit.PX);
            for (TabConfig tab : tabs) {
                bodyTabPanel.add(buildTabContent(tab), tab.getName());
            }
        }

        if (!tabs.isEmpty()) {
            bodyTabPanel.selectTab(0);
            bodyTabPanel.getTabWidget(0).getElement().getStyle().setProperty("backgroundColor", "white");
        }

        bodyTabPanel.getWidget(0).addStyleName("gwt-TabLayoutPanel-No-Padding");

        bodyTabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
            @Override
            public void onSelection(SelectionEvent<Integer> event) {
                setStyleForAllTabs(event.getSelectedItem(), bodyTabPanel);

            }
        });
        bodyTabPanel.getElement().getStyle().setProperty("minWidth", formWidth + "px");

        bodyTabPanel.setWidth(formWidth - 8 + "px");


    }

    private void setStyleForAllTabs(Integer activeTab, HackTabLayoutPanel bodyTabPanel) {
        for (int i = 0; i < bodyTabPanel.getWidgetCount(); i++) {
            if (activeTab == i) {
                bodyTabPanel.getTabWidget(i).getElement().getStyle().setProperty("backgroundColor", "white");
            } else {
                bodyTabPanel.getTabWidget(i).getElement().getStyle().setProperty("backgroundColor", "#c2e7f0");
            }
        }
    }

    private IsWidget buildTabContent(TabConfig tabConfig) {
        FlowPanel panel = new FlowPanel();

        TabGroupListConfig groupList = tabConfig.getGroupList();
        if (groupList instanceof SingleEntryGroupListConfig) {
            panel.add(buildTable(((SingleEntryGroupListConfig) groupList).getTabGroupConfig().getLayout()));
        }
        if (groupList instanceof BookmarkListConfig) {

            final BookmarksHelper bodyTabPanel = new BookmarksHelper();
            panel.getElement().getStyle().setOverflow(Style.Overflow.VISIBLE);
            List<TabGroupConfig> bookmarkTabs = ((BookmarkListConfig) groupList).getTabGroupConfigs();
            for (TabGroupConfig tab : bookmarkTabs) {
                bodyTabPanel.add(tab.getName(), buildBookmarksTabContent(tab));
            }
            panel.add(bodyTabPanel);

            bodyTabPanel.selectedIndex(0);
        }

        if (groupList instanceof HidingGroupListConfig) {

            HiddenGroupHelper bodyTabPanel = new HiddenGroupHelper();
            List<TabGroupConfig> bookmarkTabs = ((HidingGroupListConfig) groupList).getTabGroupConfigs();
            for (TabGroupConfig tab : bookmarkTabs) {
                bodyTabPanel.add(tab.getName(), hidingGroupListTabContent(tab));
            }
            panel.add(bodyTabPanel);
        }

        return panel;
    }

    private IsWidget hidingGroupListTabContent(TabGroupConfig tabGroupConfig) {
        FlowPanel panel = new FlowPanel();

        IsWidget table = buildTable(tabGroupConfig.getLayout());
        panel.add(table);
        return panel;
    }

    private IsWidget buildBookmarksTabContent(TabGroupConfig tabGroupConfig) {
        FlowPanel panel = new FlowPanel();

        panel.add(buildTable(tabGroupConfig.getLayout()));
        return panel;
    }

    private IsWidget buildTable(LayoutConfig layout) {
        TableLayoutConfig tableLayout = (TableLayoutConfig) layout;
        FlexTable table = new FlexTable();
        if (formDisplayData.isDebug()) {
            table.setBorderWidth(1);
            table.getElement().setId("debug"); // todo: why ID?
        }
        //  table.setHeight("100%");

        table.getElement().getStyle().setBackgroundColor("yellow");
        FlexTable.FlexCellFormatter cellFormatter = table.getFlexCellFormatter();
        HTMLTable.ColumnFormatter columnFormatter = table.getColumnFormatter();
        int rowIndex = 0;
        // todo colspan, rowspan, row height, col width, alignments - correct handling
        for (RowConfig row : tableLayout.getRows()) {
            List<CellConfig> cells = row.getCells();
            int colIndex = 0;
            String rowHeight = row.getHeight();
            FormState formState = formDisplayData.getFormState();
            boolean formEditable = formDisplayData.isEditable();

            for (CellConfig cell : cells) {
                WidgetDisplayConfig displayConfig = cell.getWidgetDisplayConfig();
                WidgetState widgetState = formState.getWidgetState(displayConfig.getId());
                if (widgetState == null) {
                    continue;
                }
                String widgetComponent = formDisplayData.getWidgetComponent(displayConfig.getId());
                BaseWidget widget = ComponentRegistry.instance.get(widgetComponent);
                widget.setEditable(formEditable && widgetState.isEditable());
                widget.setDisplayConfig(displayConfig);
                widget.setState(widgetState);
                widgets.add(widget);
                table.setWidget(rowIndex, colIndex, widget);

                String cellWidth = cell.getWidth();
                if (cellWidth != null && !cellWidth.isEmpty()) {
                    cellFormatter.setWidth(rowIndex, colIndex, cellWidth);

                    columnFormatter.setWidth(colIndex, cellWidth);
                }
                columnFormatter.setWidth(colIndex, cellWidth);
                if (rowHeight != null && !rowHeight.isEmpty()) {
                    cellFormatter.setHeight(rowIndex, colIndex, rowHeight);
                }
                cellFormatter.setColSpan(rowIndex, colIndex, getSpan(cell.getColumnSpan()));
                cellFormatter.setRowSpan(rowIndex, colIndex, getSpan(cell.getRowSpan()));
                cellFormatter.setHorizontalAlignment(rowIndex, colIndex, getHorizontalAlignmentForCurrentCell(cell.getHorizontalAlignment()));
                cellFormatter.setVerticalAlignment(rowIndex, colIndex, getVerticalAlignmentForCurrentCell(cell.getVerticalAlignment()));
                ++colIndex;
            }
            ++rowIndex;
        }
        return table;
    }

    private HasHorizontalAlignment.HorizontalAlignmentConstant getHorizontalAlignmentForCurrentCell(String cellAlignment) {
        HasHorizontalAlignment.HorizontalAlignmentConstant horizontalAllignment = HasHorizontalAlignment.ALIGN_LEFT;
        if (cellAlignment == null || cellAlignment.equals("left")) {
            horizontalAllignment = HasHorizontalAlignment.ALIGN_LEFT;
        }
        if (cellAlignment != null && cellAlignment.equals("right")) {
            horizontalAllignment = HasHorizontalAlignment.ALIGN_RIGHT;
        }
        if (cellAlignment != null && cellAlignment.equals("center")) {
            horizontalAllignment = HasHorizontalAlignment.ALIGN_CENTER;
        }
        return horizontalAllignment;
    }

    private HasVerticalAlignment.VerticalAlignmentConstant getVerticalAlignmentForCurrentCell(String cellAlignment) {
        HasVerticalAlignment.VerticalAlignmentConstant verticalAlligment = HasVerticalAlignment.ALIGN_MIDDLE;
        if (cellAlignment == null || cellAlignment.equals("middle")) {
            verticalAlligment = HasVerticalAlignment.ALIGN_MIDDLE;
        }
        if (cellAlignment != null && cellAlignment.equals("top")) {
            verticalAlligment = HasVerticalAlignment.ALIGN_TOP;
        }
        if (cellAlignment != null && cellAlignment.equals("bottom")) {
            verticalAlligment = HasVerticalAlignment.ALIGN_BOTTOM;
        }
        return verticalAlligment;
    }

    private int getSpan(String configValue) {
        return configValue == null || configValue.isEmpty() ? 1 : Integer.parseInt(configValue);
    }

    private boolean isHeightDeclaredInConfig(String height) {
        isHeightFromConfig = height != null;
        return isHeightFromConfig;

    }

    private boolean isWidthDeclaredInConfig(String width) {
        isWidthFromConfig = width != null;
        return isWidthFromConfig;

    }

    private int getNumberFromSizeString(String sizeString) {
        if (sizeString == null || sizeString.equalsIgnoreCase("")) {
            return 0;
        }
        int UnitPx = 2;
        return Integer.parseInt(sizeString.substring(0, sizeString.length() - UnitPx));
    }

    private void setMinimalFormWidth() {
        String minimalWidth = formDisplayData.getMinWidth();
        if (minimalWidth != null) {
            panel.getElement().getStyle().setProperty("minWidth", minimalWidth);
        } else {
            panel.getElement().getStyle().setProperty("minWidth", formWidth + "px");
        }
    }

}
