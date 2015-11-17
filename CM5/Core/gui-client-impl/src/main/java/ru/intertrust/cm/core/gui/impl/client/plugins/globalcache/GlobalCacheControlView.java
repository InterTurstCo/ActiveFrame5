package ru.intertrust.cm.core.gui.impl.client.plugins.globalcache;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.form.widget.buttons.ConfiguredButton;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.plugin.GlobalCacheControlPanel;
import ru.intertrust.cm.core.gui.model.plugin.GlobalCachePluginData;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

/**
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 21.10.2015
 */
public class GlobalCacheControlView extends PluginView {

    private static final String PERCENT = "%";
    private static final String MEGABYTES = " Мб";


    private Boolean statisticsOnly;
    private EventBus eventBus;
    private GlobalCachePluginData globalCachePluginData;
    private AbsolutePanel statPanelRoot;
    private AbsolutePanel shortStatPanel;
    private AbsolutePanel cacheCleaningPanel;
    private Grid shortStatGrid;
    private Grid controlGrid;
    private FlexTable cacheCleaningTable;

    private CheckBox cacheActiveCB;
    private CheckBox expandedStatisticsCB;
    private CheckBox debugModeCB;
    private ListBox modeListBox;
    private IntegerBox maxSizeTB;
    private ListBox uomListBox;
    /**
     * Основной конструктор
     *
     * @param plugin плагин, являющийся по сути, контроллером (или представителем) в паттерне MVC
     */
    protected GlobalCacheControlView(Plugin plugin, Boolean statisticsOnly, EventBus eventBus) {
        super(plugin);
        this.statisticsOnly = statisticsOnly;
        this.eventBus = eventBus;
        controlGrid = new Grid(3, 4);
        statPanelRoot = new AbsolutePanel();
        shortStatPanel = new AbsolutePanel();
        cacheCleaningPanel = new AbsolutePanel();
        shortStatGrid = new Grid(1, 6);
        cacheCleaningTable = new FlexTable();
    }

    @Override
    public IsWidget getViewWidget() {
        globalCachePluginData = plugin.getInitialData();
        return buildRootPanel();
    }

    private Widget buildRootPanel() {
        Panel rootPanel = new AbsolutePanel();
        Panel buttons = new HorizontalPanel();
        buttons.addStyleName(GlobalCacheControlUtils.STYLE_TOP_MENU_BUTTONS);
        rootPanel.add(buttons);
        buttons.add(buildRefreshButton());
        buttons.add(buildApplyButton());
        buttons.add(buildResetHourlyButton());
        buttons.add(buildResetButton());
        buttons.add(buildClearCacheButton());
        TabPanel tabPanel = new TabPanel();
        if (globalCachePluginData.getErrorMsg() != null) {
            Window.alert(globalCachePluginData.getErrorMsg());
        }
            /**
             * Таблица общей статистики
             */
            buildShortStatisticsPanel();
            shortStatPanel.add(shortStatGrid);
            statPanelRoot.add(shortStatPanel);
            /**
             * Статистика очистки кэша
             */
            buildCacheCleaningTable();
            cacheCleaningPanel.add(cacheCleaningTable);
            statPanelRoot.add(cacheCleaningPanel);

            tabPanel.add(statPanelRoot, GlobalCacheControlUtils.LBL_PANEL_STAT);

        tabPanel.add(buildControlPanel(), GlobalCacheControlUtils.LBL_PANEL_CONTROL);
        tabPanel.selectTab(0);
        tabPanel.getWidget(0).getParent().getElement().getParentElement()
                .addClassName("gwt-TabLayoutPanel-wrapper");
        rootPanel.add(tabPanel);
        return rootPanel;
    }

    private Widget buildRefreshButton() {
        ConfiguredButton refreshButton = GlobalCacheControlUtils.createButton(GlobalCacheControlUtils.STAT_REFRESH, GlobalCacheControlUtils.BTN_IMG_REFRESH);
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                refreshStatisticsModel();
                buildShortStatisticsPanel();
                buildCacheCleaningTable();
            }
        });
        return refreshButton;
    }

    private Widget buildResetButton() {
        ConfiguredButton resetButton = GlobalCacheControlUtils.createButton(GlobalCacheControlUtils.STAT_RESET, GlobalCacheControlUtils.BTN_IMG_RESET);
        resetButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                resetStatistics(false);
            }
        });
        return resetButton;
    }

    private Widget buildResetHourlyButton() {
        ConfiguredButton resetHourlyButton = GlobalCacheControlUtils.createButton(GlobalCacheControlUtils.STAT_HOURLY_RESET, GlobalCacheControlUtils.BTN_IMG_RESET);
        resetHourlyButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                resetStatistics(true);
            }
        });
        return resetHourlyButton;
    }

    private Widget buildClearCacheButton() {
        ConfiguredButton clearCacheButton = GlobalCacheControlUtils.createButton(GlobalCacheControlUtils.STAT_CLEAR_CACHE, GlobalCacheControlUtils.BTN_IMG_CLEAR);
        clearCacheButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                clearCache();
            }
        });
        return clearCacheButton;
    }

    private Widget buildApplyButton() {
        ConfiguredButton applyButton = GlobalCacheControlUtils.createButton(GlobalCacheControlUtils.STAT_APPLY, GlobalCacheControlUtils.BTN_IMG_APPLY);
        applyButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getControlPanelState();
                applySettings();
            }
        });
        return applyButton;
    }


    private void buildShortStatisticsPanel() {
        //TODO: Прикрутить нормальные стили
        shortStatGrid.clear();
        shortStatGrid.setStyleName("shortStatGrid");
        shortStatGrid.setWidget(0, 0, new Label(GlobalCacheControlUtils.LBL_SHORT_STAT_SIZE));
        shortStatGrid.setWidget(0, 1, new Label(globalCachePluginData.getStatPanel().getSize() + MEGABYTES));
        shortStatGrid.setWidget(0, 2, new Label(GlobalCacheControlUtils.LBL_SHORT_STAT_FREE));
        shortStatGrid.setWidget(0, 3, new Label(globalCachePluginData.getStatPanel().getFreeSpacePercentage() + PERCENT));
        shortStatGrid.setWidget(0, 4, new Label(GlobalCacheControlUtils.LBL_SHORT_STAT_HITS));
        shortStatGrid.setWidget(0, 5, new Label(globalCachePluginData.getStatPanel().getHitCount() + PERCENT));
    }

    private void buildCacheCleaningTable() {
        cacheCleaningTable.clear();
        cacheCleaningTable.setStyleName("cacheCleaningTable");
        cacheCleaningTable.getFlexCellFormatter().setRowSpan(0, 0, 2);
        cacheCleaningTable.setWidget(2, 0, new InlineHTML("<span>Фоновая очистка кэша</span>"));
        cacheCleaningTable.getFlexCellFormatter().setColSpan(0, 1, 3);
        cacheCleaningTable.setWidget(0, 1, new InlineHTML("<span>Время, мс.</span>"));
        cacheCleaningTable.setWidget(1, 0, new InlineHTML("<span>Мин.</span>"));
        cacheCleaningTable.setWidget(1, 1, new InlineHTML("<span>Макс.</span>"));
        cacheCleaningTable.setWidget(1, 2, new InlineHTML("<span>Среднее.</span>"));
        cacheCleaningTable.setWidget(2, 1, new InlineHTML("<span>"+globalCachePluginData.getStatPanel().getTimeMin()+"</span>"));
        cacheCleaningTable.setWidget(2, 2, new InlineHTML("<span>"+globalCachePluginData.getStatPanel().getTimeMax()+"</span>"));
        cacheCleaningTable.setWidget(2, 3, new InlineHTML("<span>" + globalCachePluginData.getStatPanel().getTimeAvg() + "</span>"));
        cacheCleaningTable.getFlexCellFormatter().setColSpan(0, 2, 3);
        cacheCleaningTable.setWidget(0, 2, new InlineHTML("<span>Очистка, %</span>"));
        cacheCleaningTable.setWidget(1, 3, new InlineHTML("<span>Мин.</span>"));
        cacheCleaningTable.setWidget(1, 4, new InlineHTML("<span>Макс.</span>"));
        cacheCleaningTable.setWidget(1, 5, new InlineHTML("<span>Среднее.</span>"));
        cacheCleaningTable.setWidget(2, 4, new InlineHTML("<span>"+globalCachePluginData.getStatPanel().getFreedSpaceMin()+"</span>"));
        cacheCleaningTable.setWidget(2, 5, new InlineHTML("<span>"+globalCachePluginData.getStatPanel().getFreedSpaceMax() + "</span>"));
        cacheCleaningTable.setWidget(2, 6, new InlineHTML("<span>" + globalCachePluginData.getStatPanel().getFreedSpaceMin()+"</span>"));
        cacheCleaningTable.getFlexCellFormatter().setRowSpan(0, 3, 2);
        cacheCleaningTable.setWidget(0, 3, new InlineHTML("<span>Кол-во</span>"));
        cacheCleaningTable.setWidget(2, 7, new InlineHTML("<span>"+globalCachePluginData.getStatPanel().getTotalInvocations()+"</span>"));
    }

    private Widget buildControlPanel() {
        controlGrid.clear();

        controlGrid.setStyleName(GlobalCacheControlUtils.STYLE_CONTROL_PANEL);

        controlGrid.setWidget(0, 0, new Label(GlobalCacheControlUtils.LBL_CONTROL_PANEL_CACHE_ACTIVE));
        controlGrid.setWidget(1, 0, new Label(GlobalCacheControlUtils.LBL_CONTROL_PANEL_EXPANDED_STAT));
        controlGrid.setWidget(2, 0, new Label(GlobalCacheControlUtils.LBL_CONTROL_PANEL_DEBUG_MODE));

        // чекбокс Включить кэш
        cacheActiveCB = new CheckBox();
        cacheActiveCB.setValue(globalCachePluginData.getControlPanelModel().isCacheEnabled());
        controlGrid.setWidget(0, 1, cacheActiveCB);
        // чекбокс Расширенная статистика
        expandedStatisticsCB = new CheckBox();
        expandedStatisticsCB.setValue(globalCachePluginData.getControlPanelModel().isExpandedStatistics());
        controlGrid.setWidget(1, 1, expandedStatisticsCB);

        // чекбокс Режим отладки
        debugModeCB = new CheckBox();
        debugModeCB.setValue(globalCachePluginData.getControlPanelModel().isDebugMode());
        controlGrid.setWidget(2, 1, debugModeCB);

        controlGrid.setWidget(0, 2, new Label(GlobalCacheControlUtils.LBL_CONTROL_PANEL_MODE));
        controlGrid.setWidget(1, 2, new Label(GlobalCacheControlUtils.LBL_CONTROL_PANEL_MAX_SIZE));

        modeListBox = new ListBox();
        for (String key : globalCachePluginData.getControlPanelModel().getModes().keySet()) {
            modeListBox.addItem(globalCachePluginData.getControlPanelModel().getModes().get(key), key);
        }
        controlGrid.setWidget(0, 3, modeListBox);

        Panel maxSizePanel = new HorizontalPanel();
        maxSizeTB = new IntegerBox();
        maxSizeTB.setValue(globalCachePluginData.getControlPanelModel().getMaxSize().intValue());
        maxSizePanel.add(maxSizeTB);
        uomListBox = new ListBox();
        for (String key : globalCachePluginData.getControlPanelModel().getUoms().keySet()) {
            uomListBox.addItem(globalCachePluginData.getControlPanelModel().getUoms().get(key), key);
        }
        maxSizePanel.add(uomListBox);
        controlGrid.setWidget(1, 3, maxSizePanel);
        return controlGrid;
    }


    private void refreshStatisticsModel() {
        Command command = new Command("refreshStatistics", "GlobalCacheControl.plugin", getGlobalCachePluginData());
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining statistics");
                caught.printStackTrace();
            }

            @Override
            public void onSuccess(Dto result) {
                globalCachePluginData = (GlobalCachePluginData) result;
                if (globalCachePluginData.getErrorMsg() != null) {
                    Window.alert(globalCachePluginData.getErrorMsg());
                }
                    buildShortStatisticsPanel();
                    buildCacheCleaningTable();

            }
        });
    }


    private void resetStatistics(Boolean hourly) {

        String methodName = (hourly) ? "resetHourlyStatistics" : "resetAllStatistics";

        Command command = new Command(methodName, "GlobalCacheControl.plugin", getGlobalCachePluginData());
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while reset statistics");
                caught.printStackTrace();
            }

            @Override
            public void onSuccess(Dto result) {
                refreshStatisticsModel();
            }
        });
    }

    private void clearCache() {
        Command command = new Command("clearCache", "GlobalCacheControl.plugin", getGlobalCachePluginData());
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while clear cache");
                caught.printStackTrace();
            }

            @Override
            public void onSuccess(Dto result) {
                refreshStatisticsModel();
            }
        });
    }

    private void applySettings() {
        Command command = new Command("applySettings", "GlobalCacheControl.plugin", getGlobalCachePluginData());
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while apply settings");
                caught.printStackTrace();
            }

            @Override
            public void onSuccess(Dto result) {
                globalCachePluginData = (GlobalCachePluginData) result;
                buildControlPanel();
                refreshStatisticsModel();
            }
        });
    }


    private GlobalCachePluginData getGlobalCachePluginData() {
        if (globalCachePluginData == null) {
            globalCachePluginData = new GlobalCachePluginData();
        }
        return globalCachePluginData;
    }

    /**
     * Опросить состояние контролов перед отправкой новых настроек на сервер
     */
    private void getControlPanelState(){

        if(expandedStatisticsCB.getValue() != globalCachePluginData.getControlPanelModel().isExpandedStatistics() &&
                expandedStatisticsCB.getValue().equals(Boolean.TRUE)){
            Window.alert("Внимание, сбор расширенной статистики приведёт к снижению производительности глобального кэша");
        }
        if(debugModeCB.getValue() != globalCachePluginData.getControlPanelModel().isDebugMode() &&
                debugModeCB.getValue().equals(Boolean.TRUE)){
            Window.alert("Внимание, режим отладки приведёт к снижению производительности приложения.");
        }
        globalCachePluginData.getControlPanelModel().setCacheEnabled(cacheActiveCB.getValue());
        globalCachePluginData.getControlPanelModel().setDebugMode(debugModeCB.getValue());
        globalCachePluginData.getControlPanelModel().setExpandedStatistics(expandedStatisticsCB.getValue());
    }
}
