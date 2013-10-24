package ru.intertrust.cm.core.gui.impl.client.form;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.config.model.gui.form.*;
import ru.intertrust.cm.core.config.model.gui.form.widget.WidgetDisplayConfig;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.model.GuiException;
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
    private FormDisplayData formDisplayData;
    private List<BaseWidget> widgets;

    public FormPanel(FormDisplayData formDisplayData) {
        this.formDisplayData = formDisplayData;
        widgets = new ArrayList<BaseWidget>(formDisplayData.getFormState().getFullWidgetsState().size());
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
            try {
                WidgetState currentState = widget.getCurrentState();
                if (!newState.equals(currentState)) {
                    widget.setState(newState);
                }
            } catch (GuiException e) { // current state is illegal, thus force update
                widget.setState(newState);
            }
        }
        this.formDisplayData.setFormState(formState);
    }

    private VerticalPanel build() {
        MarkupConfig markup = formDisplayData.getMarkup();
        HeaderConfig header = markup.getHeader();
        IsWidget headerTable = buildTable(header.getTableLayout());

        final TabLayoutPanel bodyTabPanel = new TabLayoutPanel(30, Style.Unit.PX);
        bodyTabPanel.setSize("800px", "200px"); // todo - something else
        BodyConfig body = markup.getBody();
        List<TabConfig> tabs = body.getTabs();
        for (TabConfig tab : tabs) {
            bodyTabPanel.add(buildTabContent(tab), tab.getName());
        }
        bodyTabPanel.selectTab(0);

        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.setSize("800px", "100%");
        verticalPanel.add(headerTable);
        verticalPanel.add(bodyTabPanel);
        bodyTabPanel.getTabWidget(0).getElement().getStyle().setProperty("backgroundColor", "white");
        bodyTabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
            @Override
            public void onSelection(SelectionEvent<Integer> event) {
                setStyleForAllTabs(event.getSelectedItem(), bodyTabPanel);
            }
        });


        return verticalPanel;
    }
    void setStyleForAllTabs(Integer activeTab, TabLayoutPanel bodyTabPanel) {
        for (int i = 0; i < bodyTabPanel.getWidgetCount(); i++) {
            if (activeTab == i) {
                bodyTabPanel.getTabWidget(i).getElement().getStyle().setProperty("backgroundColor", "white");
            }
            else {
                bodyTabPanel.getTabWidget(i).getElement().getStyle().setProperty("backgroundColor", "#c2e7f0");
            }

        }

    }
    private IsWidget buildTabContent(TabConfig tabConfig) {
        SimpleLayoutPanel panel = new SimpleLayoutPanel();
        panel.setSize("1200px", "100%");
        TabGroupListConfig groupList = tabConfig.getGroupList();
        if (groupList instanceof SingleEntryGroupListConfig) {
            panel.add(buildTable(((SingleEntryGroupListConfig) groupList).getTabGroupConfig().getLayout()));
        }
        return panel;
    }

    private IsWidget buildTable(LayoutConfig layout) {
        TableLayoutConfig tableLayout = (TableLayoutConfig) layout;
        FlexTable table = new FlexTable();
        if (formDisplayData.isDebug()){
            table.setBorderWidth(1);
            table.getElement().setId("debug"); // todo: why ID?
        }
        table.setSize("500px", "100%");
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
    private HasHorizontalAlignment.HorizontalAlignmentConstant getHorizontalAlignmentForCurrentCell(String cellAlignment){
        HasHorizontalAlignment.HorizontalAlignmentConstant  horizontalAllignment = HasHorizontalAlignment.ALIGN_LEFT;
                if (cellAlignment == null || cellAlignment.equals("left")){
                    horizontalAllignment = HasHorizontalAlignment.ALIGN_LEFT;
                }
                if (cellAlignment != null && cellAlignment.equals("right")){
                    horizontalAllignment = HasHorizontalAlignment.ALIGN_RIGHT;
                }
                if (cellAlignment != null && cellAlignment.equals("center")){
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
}
