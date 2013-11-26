package ru.intertrust.cm.core.gui.impl.client.form;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.config.gui.form.*;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetDisplayConfig;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.event.SplitterInnerScrollEvent;
import ru.intertrust.cm.core.gui.impl.client.event.SplitterInnerScrollEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.SplitterWidgetResizerEvent;
import ru.intertrust.cm.core.gui.impl.client.event.SplitterWidgetResizerEventHandler;
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
    private FormDisplayData formDisplayData;
    private List<BaseWidget> widgets;
    ScrollPanel scrollPanel ;
    EventBus eventBus;
    public FormPanel (FormDisplayData formDisplayData, EventBus eventBus, int width, int height) {
        this(formDisplayData, eventBus);
        formWidth = width;
        formHeight = height;
    }
    public FormPanel(FormDisplayData formDisplayData, EventBus eventBus) {

        this.formDisplayData = formDisplayData;
        widgets = new ArrayList<BaseWidget>(formDisplayData.getFormState().getFullWidgetsState().size());
        scrollPanel = new ScrollPanel();
        this.eventBus = eventBus;

        eventBus.addHandler(SplitterInnerScrollEvent.TYPE, new SplitterInnerScrollEventHandler() {
            @Override
            public void setScrollPanelHeight(SplitterInnerScrollEvent event) {

                scrollPanel.setHeight(event.getDownPanelHeight() + "px");
                scrollPanel.setWidth(scrollPanel.getParent().getParent().getOffsetWidth()+"px");

            }
        });

        eventBus.addHandler(SplitterWidgetResizerEvent.TYPE, new SplitterWidgetResizerEventHandler() {

            @Override
            public void setWidgetSize(SplitterWidgetResizerEvent event) {
                if (event.isType()){
                    if ((event.getFirstWidgetHeight() * 2) < Window.getClientHeight()) {
                        scrollPanel.setHeight(((event.getFirstWidgetHeight()*2) ) + "px");
                    }  else {
                        scrollPanel.setHeight((event.getFirstWidgetHeight()) + "px");
                    }
                }
                else
                {
                    scrollPanel.setHeight((event.getFirstWidgetHeight() ) + "px");
                }

                scrollPanel.setWidth(event.getFirstWidgetWidth()+"px");


            }
        });


    }
    public void updateSizes(int width, int height) {
        formWidth = width;
        formHeight = height;

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

    private ScrollPanel build() {

        MarkupConfig markup = formDisplayData.getMarkup();
        HeaderConfig header = markup.getHeader();
        IsWidget headerTable = buildTable(header.getTableLayout());

        final HackTabLayoutPanel bodyTabPanel;
        BodyConfig body = markup.getBody();
        List<TabConfig> tabs = body.getTabs();

        int countTab = 1;
        if (body.isDisplaySingleTab() == true && tabs.size() == countTab){

            bodyTabPanel = new HackTabLayoutPanel(0, Style.Unit.PX);
            bodyTabPanel.add(buildTabContent(tabs.get(0)));
        }
        else{

            bodyTabPanel = new HackTabLayoutPanel(35, Style.Unit.PX);

            for (TabConfig tab : tabs) {
                bodyTabPanel.add(buildTabContent(tab), tab.getName());
            }
        }

        bodyTabPanel.selectTab(0);
        bodyTabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
            @Override
            public void onSelection(SelectionEvent<Integer> event) {
                setStyleForAllTabs(event.getSelectedItem(), bodyTabPanel);

            }
        });

        if(header.getTableLayout().getHeight()!= null){

            bodyTabPanel.setHeight(header.getTableLayout().getHeight());
        }
        else{
            bodyTabPanel.setHeight("200px");
        }
        if(header.getTableLayout().getWidth()!= null){
            bodyTabPanel.setWidth((header.getTableLayout().getWidth()));
        }
        else{
            bodyTabPanel.setWidth(formWidth + "px");
        }
        scrollPanel = getScrollPanel(headerTable, bodyTabPanel, formDisplayData);

        return scrollPanel;
    }

    private ScrollPanel getScrollPanel(IsWidget headerTable, HackTabLayoutPanel bodyTabPanel,FormDisplayData formDisplayData){
        VerticalPanel verticalPanel = new VerticalPanel();
        formDisplayData.getMarkup().getHeader().getTableLayout().getHeight();
        formDisplayData.getMarkup().getHeader().getTableLayout().getWidth();

        scrollPanel.add(verticalPanel);
        verticalPanel.add(headerTable);
        verticalPanel.add(bodyTabPanel);

        // scrollPanel.setHeight((Window.getClientHeight()-98)/2 + "px");
        // scrollPanel.setWidth((Window.getClientWidth() - 235) + "px");

        verticalPanel.add(headerTable);
        verticalPanel.add(bodyTabPanel);
        bodyTabPanel.getTabWidget(0).getElement().getStyle().setProperty("backgroundColor", "white");

        return scrollPanel;
    }

    private void setStyleForAllTabs(Integer activeTab, HackTabLayoutPanel bodyTabPanel) {
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
        FlowPanel panel = new FlowPanel();
        //panel.setSize((Window.getClientWidth() - 260) + "px", "100%");
        TabGroupListConfig groupList = tabConfig.getGroupList();
        if (groupList instanceof SingleEntryGroupListConfig) {
            panel.add(buildTable(((SingleEntryGroupListConfig) groupList).getTabGroupConfig().getLayout()));
        }
        if (groupList instanceof BookmarkListConfig) {
//            final BookmarksTabPanel bodyTabPanel = new BookmarksTabPanel();
            final BookmarksHelper bodyTabPanel = new BookmarksHelper();
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
        //panel.setSize((Window.getClientWidth() - 260) + "px", "100%");
        panel.add(buildTable(tabGroupConfig.getLayout()));
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
