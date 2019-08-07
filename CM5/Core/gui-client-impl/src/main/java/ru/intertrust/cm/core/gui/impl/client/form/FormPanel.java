package ru.intertrust.cm.core.gui.impl.client.form;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.config.gui.form.*;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetDisplayConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.api.client.history.HistoryItem;
import ru.intertrust.cm.core.gui.api.client.history.HistoryManager;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.event.form.ParentTabSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.model.form.FormDisplayData;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserWidgetState;
import ru.intertrust.cm.core.gui.model.form.widget.TableBrowserState;
import ru.intertrust.cm.core.gui.model.form.widget.TableViewerState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginState;
import ru.intertrust.cm.core.gui.model.util.StringUtil;

import java.util.*;

/**
 * @author Denis Mitavskiy
 *         Date: 15.09.13
 *         Time: 14:53
 */
public class FormPanel extends WidgetsContainer implements IsWidget {
    private static final String TAB_KEY = "tb";
    private static final int DEFAULT_TAB = 0;

    private TabPanel bodyTabPanel;
    private AbsolutePanel footer;
    private FormDisplayData formDisplayData;
    private List<BaseWidget> widgets;
    private HashMap<String, BaseWidget> widgetsById;
    private FlowPanel panel;
    private boolean isHeightFromConfig;
    private boolean isWidthFromConfig;
    private List<TabConfig> tabs;
    private final FormPluginState state;
    private EventBus eventBus;

    public FormPanel(FormDisplayData formDisplayData, FormPluginState state, EventBus eventBus) {
        this.formDisplayData = formDisplayData;
        this.state = state;
        this.eventBus = eventBus;
        panel = new FlowPanel();
        build();
    }
    //plugin is needed for initialization, setting after build method is late for main use case
    public FormPanel(FormDisplayData formDisplayData, FormPluginState state, EventBus eventBus, Plugin plugin) {
        this.formDisplayData = formDisplayData;
        this.state = state;
        this.eventBus = eventBus;
        this.plugin = plugin;
        panel = new FlowPanel();
        build();
    }

    @Override
    public Widget asWidget() {
        return panel;
    }

    public void setClassForPluginPanel(String styleName) {
        panel.getElement().addClassName(styleName);
    }

    public List<BaseWidget> getWidgets() {
        return widgets;
    }

    public void update(FormState formState) {
        for (BaseWidget widget : widgets) {
            WidgetState newState = formState.getWidgetState(widget.getDisplayConfig().getId());
            WidgetState currentState = widget.getCurrentState();
            if(newState == null) { //for LinkedTable
                continue;
            }
            if(newState instanceof HierarchyBrowserWidgetState
                    && ((HierarchyBrowserWidgetState)newState).getHierarchyBrowserConfig()==null) { //for Hierarchy Browser
                continue;
            }
            if (!newState.equals(currentState)) {
                widget.setState(newState);
            }
        }
        this.formDisplayData.setFormState(formState);
    }

   public void updateViewFromHistory() {
        final HistoryManager historyManager = Application.getInstance().getHistoryManager();
        final Integer tabNum = StringUtil.integerFromString(
                historyManager.getValue(getFormIdentifier(), TAB_KEY), DEFAULT_TAB);
        if (bodyTabPanel.getTabBar().getSelectedTab() != tabNum && bodyTabPanel.getTabBar().getSelectedTab() != -1) {
            bodyTabPanel.selectTab(tabNum);
        }
    }

    public boolean isDirty() {
        for (BaseWidget widget : widgets) {
            if (widget.isDirty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public <T extends BaseWidget> T getWidget(String id) {
        if (widgetsById != null) {
            return (T) widgetsById.get(id);
        }
        if (widgets == null) {
            return null;
        }
        widgetsById = new HashMap<>(widgets.size());
        for (BaseWidget widget : widgets) {
            widgetsById.put(widget.getDisplayConfig().getId(), widget);
        }
        return (T) widgetsById.get(id);
    }

    public void setReadOnly(){
        panel.clear();
        state.setEditable(false);
        build();
    }

    private FlowPanel build() {


        if (isExtraStyleRequired()) {
            panel.setStyleName("frm-pnl-top");
        }
        panel.getElement().addClassName("modalFormWrapper");
        widgets = new ArrayList<>(formDisplayData.getFormState().getFullWidgetsState().size());
        MarkupConfig markup = formDisplayData.getMarkup();
        if (markup.getHeader().getTableLayout() != null) {
            IsWidget headerTable = buildHeader(markup);
            panel.add(headerTable);
        }
        footer = new AbsolutePanel();

        footer.getElement().getStyle().clearPosition();
        footer.getElement().getStyle().clearOverflow();
        buildTabs(markup);

        panel.add(bodyTabPanel);
        panel.add(footer);
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
        headerTable.asWidget().addStyleName("form-panel-header");
        return headerTable;
    }

    private void buildTabs(MarkupConfig markup) {
        BodyConfig body = markup.getBody();
        tabs = body.getTabs();

        if (body.isDisplaySingleTab() == false && tabs.size() == 1) {
            bodyTabPanel = new TabPanel();
            FlowPanel tabPanel = (FlowPanel) buildTabContent(tabs.get(0));

            bodyTabPanel.add(tabPanel, "");
            NodeList<Element> el = bodyTabPanel.getTabBar().getElement().getElementsByTagName("div");
            el.getItem(1).removeClassName("gwt-TabBarItem");
            el.getItem(2).removeClassName("gwt-Label");

        } else {
            bodyTabPanel = new TabPanel();
            for (TabConfig tab : tabs) {
                bodyTabPanel.add(buildTabContent(tab), tab.getName());
            }
        }


        bodyTabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
            @Override
            public void onSelection(SelectionEvent<Integer> event) {

                Integer numberOfSelected = event.getSelectedItem();
                FlowPanel selectedPanel = (FlowPanel) bodyTabPanel.getWidget(numberOfSelected);
                Widget selectedWidget = selectedPanel.getWidget(0);
                Object marker = selectedWidget.getLayoutData();
                final HistoryItem historyItem = new HistoryItem(
                        HistoryItem.Type.USER_INTERFACE, TAB_KEY, numberOfSelected.toString());
                Application.getInstance().getHistoryManager().addHistoryItems(getFormIdentifier(), historyItem);
                if (BusinessUniverseConstants.FOOTER_LONG.equals(marker)) {
                    footer.setStyleName("form-footer-long");
                } else if (BusinessUniverseConstants.FOOTER_SHORT.equals(marker)) {
                    footer.setStyleName("form-footer-short");
                } else {
                    footer.setStyleName("form-footer-blank");
                }
                eventBus.fireEvent(new ParentTabSelectedEvent(selectedWidget));
            }
        });
        if (!tabs.isEmpty()) {
            final HistoryManager historyManager = Application.getInstance().getHistoryManager();
            final String indexAsStr = historyManager.getValue(getFormIdentifier(), TAB_KEY);
            int selectedTab = StringUtil.integerFromString(indexAsStr, DEFAULT_TAB);
            if (selectedTab >= tabs.size()) {
                selectedTab = DEFAULT_TAB;
            }
            bodyTabPanel.selectTab(selectedTab);
            bodyTabPanel.getWidget(selectedTab).getParent().getElement().getParentElement()
                    .addClassName("gwt-TabLayoutPanel-wrapper");
        }
    }

    private IsWidget buildTabContent(TabConfig tabConfig) {
        FlowPanel panel = new FlowPanel();

        TabGroupListConfig groupList = tabConfig.getGroupList();
        if (groupList instanceof SingleEntryGroupListConfig) {

            panel.add(buildTable(((SingleEntryGroupListConfig) groupList).getTabGroupConfig().getLayout()));
        }
        if (groupList instanceof BookmarkListConfig) {

            final BookmarksHelper bodyTabPanel = new BookmarksHelper(eventBus);
            bodyTabPanel.asWidget().setLayoutData(BusinessUniverseConstants.FOOTER_LONG);
            addStyleHandlersForBookMarks(bodyTabPanel);
            List<TabGroupConfig> bookmarkTabs = ((BookmarkListConfig) groupList).getTabGroupConfigs();
            for (TabGroupConfig tab : bookmarkTabs) {
                bodyTabPanel.add(tab.getName(), buildBookmarksTabContent(tab));
            }
            panel.add(bodyTabPanel);
            bodyTabPanel.selectedIndex(DEFAULT_TAB);
        }

        if (groupList instanceof HidingGroupListConfig) {

            HiddenGroupHelper bodyTabPanel = new HiddenGroupHelper(eventBus);
            List<TabGroupConfig> bookmarkTabs = ((HidingGroupListConfig) groupList).getTabGroupConfigs();
            for (TabGroupConfig tab : bookmarkTabs) {
                String initialSate = tab.getInitialState();
                bodyTabPanel.add(tab.getName(), initialSate, hidingGroupListTabContent(tab));
            }
            panel.add(bodyTabPanel);
        }

        return panel;
    }

    private void addStyleHandlersForBookMarks(final BookmarksHelper bodyTabPanel) {
        bodyTabPanel.addDivLeftClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                footer.setStyleName("form-footer-short");
                bodyTabPanel.asWidget().setLayoutData(BusinessUniverseConstants.FOOTER_SHORT);

            }
        });
        bodyTabPanel.addDivRightClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                footer.setStyleName("form-footer-long");
                bodyTabPanel.asWidget().setLayoutData(BusinessUniverseConstants.FOOTER_LONG);
            }
        });
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
        if (tableLayout.getWidth() != null) {
            table.setWidth(tableLayout.getWidth());
        }
        if (tableLayout.getHeight() != null) {
            table.setHeight(tableLayout.getHeight());
        }

        if (formDisplayData.isDebug()) {
            table.getElement().addClassName("debug");
        }

        FlexTable.FlexCellFormatter cellFormatter = table.getFlexCellFormatter();
        HTMLTable.ColumnFormatter columnFormatter = table.getColumnFormatter();
        int rowIndex = 0;
        // todo colspan, rowspan, row height, col width, alignments - correct handling
        for (RowConfig row : tableLayout.getRows()) {
            List<CellConfig> cells = row.getCells();
            int colIndex = 0;
            String rowHeight = row.getHeight();
            FormState formState = formDisplayData.getFormState();

            for (CellConfig cell : cells) {
                WidgetDisplayConfig displayConfig = cell.getWidgetDisplayConfig();
                displayConfig.setParentName(formState.getName());
                WidgetState widgetState = formState.getWidgetState(displayConfig.getId());
                if (widgetState == null) {
                    continue;
                }
                String widgetComponent = formDisplayData.getWidgetComponent(displayConfig.getId());
                BaseWidget widget = ComponentRegistry.instance.get(widgetComponent);
                widget.setEditable(!widgetState.isForceReadOnly() && state.isEditable() && widgetState.isEditable());
                widget.setDisplayConfig(displayConfig);
                Map<String, String> messages = formState.getMessages();
                widget.setMessages(messages);
                widget.setEventBus(eventBus);
                widget.setContainer(this);

                //CMFIVE-7785
                if(widgetState instanceof TableBrowserState){
                    if(row.getHeight()==null)
                    ((TableBrowserState)widgetState).setStretched(true);
                }
                if(widgetState instanceof TableViewerState){
                    if(row.getHeight()==null)
                        ((TableViewerState)widgetState).setStretched(true);
                }

                widget.setState(widgetState);
                widgets.add(widget);
                AbsolutePanel wrapper = new AbsolutePanel();
                wrapper.addStyleName("widget-wrapper");
                wrapper.getElement().getStyle().clearOverflow();
                wrapper.getElement().getStyle().clearPosition();
                wrapper.add(widget);
                table.setWidget(rowIndex, colIndex,wrapper);

                String cellWidth = cell.getWidth();
                if (cellWidth != null && !cellWidth.isEmpty()) {
                    cellFormatter.setWidth(rowIndex, colIndex, cellWidth);
                    columnFormatter.setWidth(colIndex, cellWidth);
                }

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

    private boolean isExtraStyleRequired() {
        //boolean result = state.isEditable() && state.isToggleEdit();
        boolean result = /*state.isToggleEdit() &&*/ state.isInCentralPanel();
        return result;
    }

    public String getFormIdentifier() {
        return formDisplayData.getFormState().getName();
    }

    /**
     * Проверяет есть ли хотя бы одна вкладка на форме.
     *
     * @return true - есть<br>
     * false - ни одной вкладки нет
     */
    private boolean hasAnyTab() {
        final TabBar tabBar = bodyTabPanel.getTabBar();
        final int selectedTabIndex = tabBar.getSelectedTab();

        boolean hasAnyTab = (selectedTabIndex == -1) ? false : true;
        return hasAnyTab;
    }

    /**
     * Возвращает набор идентификаторов виджетов, расположенных на вкладках<br>
     *
     * @return набор идентификаторов виджетов на вкладках;<br>
     * пустой набор, если их нет (виджетов, либо самих вкладок как таковых)
     */
    public Set<String> getAllTabsWidgetIds() {
        if (hasAnyTab()) {

            Set<String> allTabsWidgetIdsSet = new HashSet<>();
            final int tabsCount = tabs.size();

            for (int index = 0; index < tabsCount; index++) {
                final Set<String> tabWidgetIds = getTabWidgetIds(index);
                allTabsWidgetIdsSet.addAll(tabWidgetIds);
            }
            return allTabsWidgetIdsSet;
        }
        return new HashSet<>();
    }

    /**
     * Возвращает набор идентификаторов виджетов, расположенных на выбранной вкладке<br>
     *
     * @return набор идентификаторов виджетов на выбранной вкладке;<br>
     * пустой набор, если их нет (виджетов, либо самих вкладок как таковых)
     */
    public Set<String> getSelectedTabWidgetIds() {
        if (hasAnyTab()) {
            final TabBar tabBar = bodyTabPanel.getTabBar();
            final int selectedTabIndex = tabBar.getSelectedTab();

            final Set<String> tabWidgetIds = getTabWidgetIds(selectedTabIndex);
            return tabWidgetIds;
        }
        return new HashSet<>();
    }

    /**
     * Возвращает набор идентификаторов виджетов, расположенных на вкладке с указанным индексом<br>
     *
     * @param tabIndex индекс вкладки
     * @return набор идентификаторов виджетов на вкладке с указанным индексом;<br>
     * пустой набор, если их нет (виджетов, либо самих вкладок как таковых)
     */
    private Set<String> getTabWidgetIds(int tabIndex) {
        if (hasAnyTab()) {
            Set<String> widgetIdsSet = new HashSet<>();
            final TabConfig selectedTabConfig = tabs.get(tabIndex);

            final TabGroupListConfig groupList = selectedTabConfig.getGroupList();
            final List<TabGroupConfig> tabGroupConfigsList = groupList.getTabGroupConfigs();

            for (TabGroupConfig tabGroupConfig : tabGroupConfigsList) {
                final TableLayoutConfig layout = tabGroupConfig.getLayout();
                final List<RowConfig> rowConfigsList = layout.getRows();

                for (RowConfig rowConfig : rowConfigsList) {
                    final List<CellConfig> cellConfigsList = rowConfig.getCells();

                    for (CellConfig cellConfig : cellConfigsList) {

                        final WidgetDisplayConfig widgetDisplayConfig = cellConfig.getWidgetDisplayConfig();
                        final String widgetId = widgetDisplayConfig.getId();

                        widgetIdsSet.add(widgetId);
                    }
                }
            }
            return widgetIdsSet;
        }
        return new HashSet<>();
    }

    /**
     * Выделяет первую вкладку на панели формы, если они (вкладки) имеются.
     */
    public void selectFirstTab() {
        if (hasAnyTab()) {
            bodyTabPanel.selectTab(DEFAULT_TAB);
        }
    }

}
