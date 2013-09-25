package ru.intertrust.cm.core.gui.impl.client.form;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.config.model.gui.form.*;
import ru.intertrust.cm.core.config.model.gui.form.widget.WidgetDisplayConfig;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.model.form.Form;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetData;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 15.09.13
 *         Time: 14:53
 */
public class FormPanel implements IsWidget {
    private Form form;
    private List<BaseWidget> widgets;

    public FormPanel(Form form) {
        this.form = form;
        widgets = new ArrayList<BaseWidget>(form.getFullWidgetData().size());
    }

    @Override
    public com.google.gwt.user.client.ui.Widget asWidget() {
        return build();
    }

    public List<BaseWidget> getWidgets() {
        return widgets;
    }

    public void update(Form form) {

    }

    private VerticalPanel build() {
        MarkupConfig markup = form.getMarkup();
        HeaderConfig header = markup.getHeader();
        IsWidget headerTable = buildTable(header.getTableLayout());


        TabLayoutPanel bodyTabPanel = new TabLayoutPanel(30, Style.Unit.PX);
        bodyTabPanel.setSize("500px", "200px"); // todo - something else
        BodyConfig body = markup.getBody();
        List<TabConfig> tabs = body.getTabs();
        for (TabConfig tab : tabs) {
            bodyTabPanel.add(buildTabContent(tab), tab.getName());
        }
        bodyTabPanel.selectTab(0);

        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.setSize("500px", "100%");
        verticalPanel.add(headerTable);
        verticalPanel.add(bodyTabPanel);

        return verticalPanel;
    }

    private IsWidget buildTabContent(TabConfig tabConfig) {
        SimpleLayoutPanel panel = new SimpleLayoutPanel();
        panel.setSize("500px", "100%");
        TabGroupListConfig groupList = tabConfig.getGroupList();
        if (groupList instanceof SingleEntryGroupListConfig) {
            panel.add(buildTable(((SingleEntryGroupListConfig) groupList).getTabGroupConfig().getLayout()));
        }
        return panel;
    }

    private IsWidget buildTable(LayoutConfig layout) {
        TableLayoutConfig tableLayout = (TableLayoutConfig) layout;
        FlexTable table = new FlexTable();
        table.setSize("500px", "100%");
        FlexTable.FlexCellFormatter cellFormatter = table.getFlexCellFormatter();
        HTMLTable.ColumnFormatter columnFormatter = table.getColumnFormatter();
        int rowIndex = 0;
        // todo colspan, rowspan, row height, col width, alignments - correct handling
        for (RowConfig row : tableLayout.getRows()) {
            List<CellConfig> cells = row.getCells();
            int colIndex = 0;
            String rowHeight = row.getHeight();
            for (CellConfig cell : cells) {
                WidgetDisplayConfig displayConfig = cell.getWidgetDisplayConfig();
                WidgetData widgetData = form.getWidgetData(displayConfig.getId());
                BaseWidget widget = ComponentRegistry.instance.get(widgetData.getComponentName());
                widget.setEditable(form.isEditable());
                widget.setDisplayConfig(displayConfig);
                widget.setState(widgetData);
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
                ++colIndex;
            }
            ++rowIndex;
        }
        return table;
    }

    private int getSpan(String configValue) {
        return configValue == null || configValue.isEmpty() ? 1 : Integer.parseInt(configValue);
    }
}
