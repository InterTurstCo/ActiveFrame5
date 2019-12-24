package ru.intertrust.cm.core.gui.impl.client.plugins.globalcache;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.GlobalCacheStatistics;
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
    private AbsolutePanel extendedStatPanel;
    private AbsolutePanel cacheCleaningPanel;
    private AbsolutePanel controlPanel;
    private Grid shortStatGrid;
    private Grid controlGrid;
    private FlexTable cacheCleaningTable;
    private Panel buttons;
    private FlexTable controlAlert;

    private CheckBox cacheActiveCB;
    private CheckBox expandedStatisticsCB;
    private CheckBox debugModeCB;
    private ListBox modeListBox;
    private TextBox maxSizeTB;
    private TextBox waitLockMillies;
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
        controlGrid = new Grid(4, 4);
        statPanelRoot = new AbsolutePanel();
        shortStatPanel = new AbsolutePanel();
        extendedStatPanel = new AbsolutePanel();
        cacheCleaningPanel = new AbsolutePanel();
        controlPanel = new AbsolutePanel();
        shortStatGrid = new Grid(1, 8);
        cacheCleaningTable = new FlexTable();
        controlAlert = new FlexTable();
    }

    @Override
    public IsWidget getViewWidget() {
        globalCachePluginData = plugin.getInitialData();
        return buildRootPanel();
    }

    private Widget buildRootPanel() {
        Panel rootPanel = new AbsolutePanel();
        buttons = new HorizontalPanel();
        buttons.addStyleName(GlobalCacheControlUtils.STYLE_TOP_MENU_BUTTONS);
        rootPanel.add(buildButtons());

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
        statPanelRoot.setStyleName("statPanelRoot");

        buildExtendedStatisticsPanel();
        statPanelRoot.add(extendedStatPanel);
        /**
         * Статистика очистки кэша
         */
        buildCacheCleaningTable();
        cacheCleaningPanel.add(cacheCleaningTable);
        statPanelRoot.add(cacheCleaningPanel);

        tabPanel.add(statPanelRoot, GlobalCacheControlUtils.LBL_PANEL_STAT);
        controlAlert.setStyleName("controlAlert");
        controlAlert.setWidget(0, 0, new Label(GlobalCacheControlUtils.MSG_CONTROL_WARNING));

        tabPanel.add(buildControlPanel(), GlobalCacheControlUtils.LBL_PANEL_CONTROL);
        tabPanel.selectTab(0);
        tabPanel.getWidget(0).getParent().getElement().getParentElement()
                .addClassName("gwt-TabLayoutPanel-wrapper");
        rootPanel.add(tabPanel);
        return rootPanel;
    }

    private Widget buildButtons() {
        buttons.clear();
        buttons.add(buildRefreshButton());
        if (globalCachePluginData.isSuperUser()) {
            buttons.add(buildApplyButton());
            if (globalCachePluginData.getControlPanelModel().isExpandedStatistics()) {
                buttons.add(buildResetHourlyButton());
            }
            buttons.add(buildResetButton());
            buttons.add(buildClearCacheButton());
        }
        return buttons;
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
        shortStatGrid.setWidget(0, 6, new Label(GlobalCacheControlUtils.LBL_CONTROL_PANEL_MAX_SIZE));
        shortStatGrid.setWidget(0, 7, new Label(globalCachePluginData.getControlPanelModel().getMaxSize()/1024/1024 + MEGABYTES));
    }

    private void buildExtendedStatisticsPanel() {
        extendedStatPanel.clear();
        if (globalCachePluginData.getControlPanelModel().isExpandedStatistics()) {
            FlexTable extendedStatTable = new FlexTable();
            extendedStatTable.setStyleName("cacheCleaningTable");
            extendedStatTable.getFlexCellFormatter().setRowSpan(0, 0, 3);
            extendedStatTable.setWidget(0, 0, new InlineHTML("<span>Операция</span>"));
            extendedStatTable.getFlexCellFormatter().setColSpan(0, 1, 6);
            extendedStatTable.setWidget(0, 1, new InlineHTML("<span>Час</span>"));
            extendedStatTable.getFlexCellFormatter().setColSpan(0, 2, 6);
            extendedStatTable.setWidget(0, 2, new InlineHTML("<span>Всего</span>"));
            extendedStatTable.getFlexCellFormatter().setColSpan(1, 0, 3);
            extendedStatTable.setWidget(1, 0, new InlineHTML("<span>Время, мкс</span>"));
            extendedStatTable.getFlexCellFormatter().setRowSpan(1, 1, 2);
            extendedStatTable.setWidget(1, 1, new InlineHTML("<span>Кол-во</span>"));
            extendedStatTable.getFlexCellFormatter().setRowSpan(1, 2, 2);
            extendedStatTable.setWidget(1, 2, new InlineHTML("<span>Частота, %</span>"));
            extendedStatTable.getFlexCellFormatter().setRowSpan(1, 3, 2);
            extendedStatTable.setWidget(1, 3, new InlineHTML("<span>Попаданий, %</span>"));
            extendedStatTable.setWidget(2, 0, new InlineHTML("<span>Мин.</span>"));
            extendedStatTable.setWidget(2, 1, new InlineHTML("<span>Макс.</span>"));
            extendedStatTable.setWidget(2, 2, new InlineHTML("<span>Среднее</span>"));
            extendedStatTable.getFlexCellFormatter().setColSpan(1, 4, 3);
            extendedStatTable.setWidget(1, 4, new InlineHTML("<span>Время, мкс</span>"));
            extendedStatTable.setWidget(2, 3, new InlineHTML("<span>Мин.</span>"));
            extendedStatTable.setWidget(2, 4, new InlineHTML("<span>Макс.</span>"));
            extendedStatTable.setWidget(2, 5, new InlineHTML("<span>Среднее</span>"));
            extendedStatTable.getFlexCellFormatter().setRowSpan(1, 5, 2);
            extendedStatTable.setWidget(1, 5, new InlineHTML("<span>Кол-во</span>"));
            extendedStatTable.getFlexCellFormatter().setRowSpan(1, 6, 2);
            extendedStatTable.setWidget(1, 6, new InlineHTML("<span>Частота, %</span>"));
            extendedStatTable.getFlexCellFormatter().setRowSpan(1, 7, 2);
            extendedStatTable.setWidget(1, 7, new InlineHTML("<span>Попаданий, %</span>"));

            extendedStatTable.getFlexCellFormatter().setColSpan(3, 0, 13);
            extendedStatTable.setWidget(3, 0, new InlineHTML("<span>ЗАПИСЬ</span>"));

            Integer rowCounter = 4;
            if (globalCachePluginData.getStatPanel().getNotifierRecords() != null) {
                for (GlobalCacheStatistics.Record record : globalCachePluginData.getStatPanel().getNotifierRecords()) {
                    extendedStatTable.setWidget(rowCounter, 0, new InlineHTML("<span>" + record.getMethodDescription() + "</span>"));
                    extendedStatTable.setWidget(rowCounter, 1, new InlineHTML("<span>" + record.getTimeMinPerHour() / 1000 + "</span>"));
                    extendedStatTable.setWidget(rowCounter, 2, new InlineHTML("<span>" + record.getTimeMaxPerHour() / 1000 + "</span>"));
                    extendedStatTable.setWidget(rowCounter, 3, new InlineHTML("<span>" + NumberFormat.getFormat("##0.00").format(record.getTimeAvgPerHour() / 1000) + "</span>"));
                    extendedStatTable.setWidget(rowCounter, 4, new InlineHTML("<span>" + record.getInvocationsPerHour() + "</span>"));
                    extendedStatTable.setWidget(rowCounter, 5, new InlineHTML("<span>" + NumberFormat.getFormat("##0.00").format(record.getHourlyFrequency() * 100) + "%</span>"));
                    extendedStatTable.setWidget(rowCounter, 6, new InlineHTML("<span>" + NumberFormat.getFormat("##0.00").format(record.getCacheHitPercentagePerHour() * 100) + "%</span>"));
                    extendedStatTable.setWidget(rowCounter, 7, new InlineHTML("<span>" + record.getTimeMinTotal() / 1000 + "</span>"));
                    extendedStatTable.setWidget(rowCounter, 8, new InlineHTML("<span>" + record.getTimeMaxTotal() / 1000 + "</span>"));
                    extendedStatTable.setWidget(rowCounter, 9, new InlineHTML("<span>" + NumberFormat.getFormat("##0.0").format(record.getTimeAvgTotal() / 1000) + "</span>"));
                    extendedStatTable.setWidget(rowCounter, 10, new InlineHTML("<span>" + record.getInvocationsTotal() + "</span>"));
                    extendedStatTable.setWidget(rowCounter, 11, new InlineHTML("<span>" + NumberFormat.getFormat("##0.00").format(record.getTotalFrequency() * 100) + "%</span>"));
                    extendedStatTable.setWidget(rowCounter, 12, new InlineHTML("<span>" + NumberFormat.getFormat("##0.00").format(record.getCacheHitPercentageTotal() * 100) + "%</span>"));
                    rowCounter++;
                }
                extendedStatTable.setWidget(rowCounter, 0, new InlineHTML("<span>ИТОГО ЗАПИСЬ:</span>"));
                extendedStatTable.setWidget(rowCounter, 1, new InlineHTML("<span>" + globalCachePluginData.getStatPanel().getNotifierSummary().getTimeMinPerHour() / 1000 + "</span>"));
                extendedStatTable.setWidget(rowCounter, 2, new InlineHTML("<span>" + globalCachePluginData.getStatPanel().getNotifierSummary().getTimeMaxPerHour() / 1000 + "</span>"));
                extendedStatTable.setWidget(rowCounter, 3, new InlineHTML("<span>" + NumberFormat.getFormat("##0.00").format(globalCachePluginData.getStatPanel().getNotifierSummary().getTimeAvgPerHour() / 1000) + "</span>"));
                extendedStatTable.setWidget(rowCounter, 4, new InlineHTML("<span>" + globalCachePluginData.getStatPanel().getNotifierSummary().getInvocationsPerHour() + "</span>"));
                extendedStatTable.setWidget(rowCounter, 5, new InlineHTML("<span>" + NumberFormat.getFormat("##0.00").format(globalCachePluginData.getStatPanel().getNotifierSummary().getHourlyFrequency() * 100) + "%</span>"));
                extendedStatTable.setWidget(rowCounter, 6, new InlineHTML("<span>" + NumberFormat.getFormat("##0.00").format(globalCachePluginData.getStatPanel().getNotifierSummary().getCacheHitPercentagePerHour() * 100) + "%</span>"));
                extendedStatTable.setWidget(rowCounter, 7, new InlineHTML("<span>" + globalCachePluginData.getStatPanel().getNotifierSummary().getTimeMinTotal() / 1000 + "</span>"));
                extendedStatTable.setWidget(rowCounter, 8, new InlineHTML("<span>" + globalCachePluginData.getStatPanel().getNotifierSummary().getTimeMaxTotal() / 1000 + "</span>"));
                extendedStatTable.setWidget(rowCounter, 9, new InlineHTML("<span>" + NumberFormat.getFormat("##0.0").format(globalCachePluginData.getStatPanel().getNotifierSummary().getTimeAvgTotal() / 1000) + "</span>"));
                extendedStatTable.setWidget(rowCounter, 10, new InlineHTML("<span>" + globalCachePluginData.getStatPanel().getNotifierSummary().getInvocationsTotal() + "</span>"));
                extendedStatTable.setWidget(rowCounter, 11, new InlineHTML("<span>" + NumberFormat.getFormat("##0.00").format(globalCachePluginData.getStatPanel().getNotifierSummary().getTotalFrequency() * 100) + "%</span>"));
                extendedStatTable.setWidget(rowCounter, 12, new InlineHTML("<span>" + NumberFormat.getFormat("##0.00").format(globalCachePluginData.getStatPanel().getNotifierSummary().getCacheHitPercentageTotal() * 100) + "%</span>"));
                rowCounter++;
            } else {
                extendedStatTable.setWidget(3, 0, new InlineHTML("<span>ЗАПИСЬ: Данные не доступны</span>"));
            }


            extendedStatTable.getFlexCellFormatter().setColSpan(rowCounter, 0, 13);
            extendedStatTable.setWidget(rowCounter, 0, new InlineHTML("<span>ЧТЕНИЕ</span>"));
            rowCounter++;

            if (globalCachePluginData.getStatPanel().getReadersRecords() != null) {
                for (GlobalCacheStatistics.Record readRecord : globalCachePluginData.getStatPanel().getReadersRecords()) {
                    extendedStatTable.setWidget(rowCounter, 0, new InlineHTML("<span>" + readRecord.getMethodDescription() + "</span>"));
                    extendedStatTable.setWidget(rowCounter, 1, new InlineHTML("<span>" + readRecord.getTimeMinPerHour() / 1000 + "</span>"));
                    extendedStatTable.setWidget(rowCounter, 2, new InlineHTML("<span>" + readRecord.getTimeMaxPerHour() / 1000 + "</span>"));
                    extendedStatTable.setWidget(rowCounter, 3, new InlineHTML("<span>" + NumberFormat.getFormat("##0.00").format(readRecord.getTimeAvgPerHour() / 1000) + "</span>"));
                    extendedStatTable.setWidget(rowCounter, 4, new InlineHTML("<span>" + readRecord.getInvocationsPerHour() + "</span>"));
                    extendedStatTable.setWidget(rowCounter, 5, new InlineHTML("<span>" + NumberFormat.getFormat("##0.00").format(readRecord.getHourlyFrequency() * 100) + "%</span>"));
                    extendedStatTable.setWidget(rowCounter, 6, new InlineHTML("<span>" + NumberFormat.getFormat("##0.00").format(readRecord.getCacheHitPercentagePerHour() * 100) + "%</span>"));
                    extendedStatTable.setWidget(rowCounter, 7, new InlineHTML("<span>" + readRecord.getTimeMinTotal() / 1000 + "</span>"));
                    extendedStatTable.setWidget(rowCounter, 8, new InlineHTML("<span>" + readRecord.getTimeMaxTotal() / 1000 + "</span>"));
                    extendedStatTable.setWidget(rowCounter, 9, new InlineHTML("<span>" + NumberFormat.getFormat("##0.0").format(readRecord.getTimeAvgTotal() / 1000) + "</span>"));
                    extendedStatTable.setWidget(rowCounter, 10, new InlineHTML("<span>" + readRecord.getInvocationsTotal() + "</span>"));
                    extendedStatTable.setWidget(rowCounter, 11, new InlineHTML("<span>" + NumberFormat.getFormat("##0.00").format(readRecord.getTotalFrequency() * 100) + "%</span>"));
                    extendedStatTable.setWidget(rowCounter, 12, new InlineHTML("<span>" + NumberFormat.getFormat("##0.00").format(readRecord.getCacheHitPercentageTotal() * 100) + "%</span>"));
                    rowCounter++;
                }
                extendedStatTable.setWidget(rowCounter, 0, new InlineHTML("<span>ИТОГО ЧТЕНИЕ:</span>"));
                extendedStatTable.setWidget(rowCounter, 1, new InlineHTML("<span>" + globalCachePluginData.getStatPanel().getReaderSummary().getTimeMinPerHour() / 1000 + "</span>"));
                extendedStatTable.setWidget(rowCounter, 2, new InlineHTML("<span>" + globalCachePluginData.getStatPanel().getReaderSummary().getTimeMaxPerHour() / 1000 + "</span>"));
                extendedStatTable.setWidget(rowCounter, 3, new InlineHTML("<span>" + NumberFormat.getFormat("##0.00").format(globalCachePluginData.getStatPanel().getReaderSummary().getTimeAvgPerHour() / 1000) + "</span>"));
                extendedStatTable.setWidget(rowCounter, 4, new InlineHTML("<span>" + globalCachePluginData.getStatPanel().getReaderSummary().getInvocationsPerHour() + "</span>"));
                extendedStatTable.setWidget(rowCounter, 5, new InlineHTML("<span>" + NumberFormat.getFormat("##0.00").format(globalCachePluginData.getStatPanel().getReaderSummary().getHourlyFrequency() * 100) + "%</span>"));
                extendedStatTable.setWidget(rowCounter, 6, new InlineHTML("<span>" + NumberFormat.getFormat("##0.00").format(globalCachePluginData.getStatPanel().getReaderSummary().getCacheHitPercentagePerHour() * 100) + "%</span>"));
                extendedStatTable.setWidget(rowCounter, 7, new InlineHTML("<span>" + globalCachePluginData.getStatPanel().getReaderSummary().getTimeMinTotal() / 1000 + "</span>"));
                extendedStatTable.setWidget(rowCounter, 8, new InlineHTML("<span>" + globalCachePluginData.getStatPanel().getReaderSummary().getTimeMaxTotal() / 1000 + "</span>"));
                extendedStatTable.setWidget(rowCounter, 9, new InlineHTML("<span>" + NumberFormat.getFormat("##0.0").format(globalCachePluginData.getStatPanel().getReaderSummary().getTimeAvgTotal() / 1000) + "</span>"));
                extendedStatTable.setWidget(rowCounter, 10, new InlineHTML("<span>" + globalCachePluginData.getStatPanel().getReaderSummary().getInvocationsTotal() + "</span>"));
                extendedStatTable.setWidget(rowCounter, 11, new InlineHTML("<span>" + NumberFormat.getFormat("##0.00").format(globalCachePluginData.getStatPanel().getReaderSummary().getTotalFrequency() * 100) + "%</span>"));
                extendedStatTable.setWidget(rowCounter, 12, new InlineHTML("<span>" + NumberFormat.getFormat("##0.00").format(globalCachePluginData.getStatPanel().getReaderSummary().getCacheHitPercentageTotal() * 100) + "%</span>"));
                rowCounter++;
            } else {
                extendedStatTable.setWidget(rowCounter, 0, new InlineHTML("<span>ЧТЕНИЕ: Данные не доступны</span>"));
            }
            extendedStatTable.getFlexCellFormatter().setColSpan(rowCounter, 0, 13);
            extendedStatTable.setWidget(rowCounter, 0, new InlineHTML("<span></span>"));
            rowCounter++;

            if (globalCachePluginData.getStatPanel().getGlobalSummary() != null) {
                extendedStatTable.setWidget(rowCounter, 0, new InlineHTML("<span>ИТОГО:</span>"));
                extendedStatTable.setWidget(rowCounter, 1, new InlineHTML("<span>" + globalCachePluginData.getStatPanel().getGlobalSummary().getTimeMinPerHour() / 1000 + "</span>"));
                extendedStatTable.setWidget(rowCounter, 2, new InlineHTML("<span>" + globalCachePluginData.getStatPanel().getGlobalSummary().getTimeMaxPerHour() / 1000 + "</span>"));
                extendedStatTable.setWidget(rowCounter, 3, new InlineHTML("<span>" + NumberFormat.getFormat("##0.00").format(globalCachePluginData.getStatPanel().getGlobalSummary().getTimeAvgPerHour() / 1000) + "</span>"));
                extendedStatTable.setWidget(rowCounter, 4, new InlineHTML("<span>" + globalCachePluginData.getStatPanel().getGlobalSummary().getInvocationsPerHour() + "</span>"));
                extendedStatTable.setWidget(rowCounter, 5, new InlineHTML("<span>" + NumberFormat.getFormat("##0.00").format(globalCachePluginData.getStatPanel().getGlobalSummary().getHourlyFrequency() * 100) + "%</span>"));
                extendedStatTable.setWidget(rowCounter, 6, new InlineHTML("<span>" + NumberFormat.getFormat("##0.00").format(globalCachePluginData.getStatPanel().getGlobalSummary().getCacheHitPercentagePerHour() * 100) + "%</span>"));
                extendedStatTable.setWidget(rowCounter, 7, new InlineHTML("<span>" + globalCachePluginData.getStatPanel().getGlobalSummary().getTimeMinTotal() / 1000 + "</span>"));
                extendedStatTable.setWidget(rowCounter, 8, new InlineHTML("<span>" + globalCachePluginData.getStatPanel().getGlobalSummary().getTimeMaxTotal() / 1000 + "</span>"));
                extendedStatTable.setWidget(rowCounter, 9, new InlineHTML("<span>" + NumberFormat.getFormat("##0.0").format(globalCachePluginData.getStatPanel().getGlobalSummary().getTimeAvgTotal() / 1000) + "</span>"));
                extendedStatTable.setWidget(rowCounter, 10, new InlineHTML("<span>" + globalCachePluginData.getStatPanel().getGlobalSummary().getInvocationsTotal() + "</span>"));
                extendedStatTable.setWidget(rowCounter, 11, new InlineHTML("<span>" + NumberFormat.getFormat("##0.00").format(globalCachePluginData.getStatPanel().getGlobalSummary().getTotalFrequency() * 100) + "%</span>"));
                extendedStatTable.setWidget(rowCounter, 12, new InlineHTML("<span>" + NumberFormat.getFormat("##0.00").format(globalCachePluginData.getStatPanel().getGlobalSummary().getCacheHitPercentageTotal() * 100) + "%</span>"));

            }
            extendedStatPanel.add(extendedStatTable);
        }

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
        cacheCleaningTable.setWidget(2, 1, new InlineHTML("<span>" + globalCachePluginData.getStatPanel().getTimeMin() + "</span>"));
        cacheCleaningTable.setWidget(2, 2, new InlineHTML("<span>" + globalCachePluginData.getStatPanel().getTimeMax() + "</span>"));
        cacheCleaningTable.setWidget(2, 3, new InlineHTML("<span>" + globalCachePluginData.getStatPanel().getTimeAvg() + "</span>"));
        cacheCleaningTable.getFlexCellFormatter().setColSpan(0, 2, 3);
        cacheCleaningTable.setWidget(0, 2, new InlineHTML("<span>Очистка, %</span>"));
        cacheCleaningTable.setWidget(1, 3, new InlineHTML("<span>Мин.</span>"));
        cacheCleaningTable.setWidget(1, 4, new InlineHTML("<span>Макс.</span>"));
        cacheCleaningTable.setWidget(1, 5, new InlineHTML("<span>Среднее.</span>"));
        cacheCleaningTable.setWidget(2, 4, new InlineHTML("<span>" + globalCachePluginData.getStatPanel().getFreedSpaceMin() + "</span>"));
        cacheCleaningTable.setWidget(2, 5, new InlineHTML("<span>" + globalCachePluginData.getStatPanel().getFreedSpaceMax() + "</span>"));
        cacheCleaningTable.setWidget(2, 6, new InlineHTML("<span>" + globalCachePluginData.getStatPanel().getFreedSpaceMin() + "</span>"));
        cacheCleaningTable.getFlexCellFormatter().setRowSpan(0, 3, 2);
        cacheCleaningTable.setWidget(0, 3, new InlineHTML("<span>Кол-во</span>"));
        cacheCleaningTable.setWidget(2, 7, new InlineHTML("<span>" + globalCachePluginData.getStatPanel().getTotalInvocations() + "</span>"));
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
        int i = 0;
        for (String key : globalCachePluginData.getControlPanelModel().getModes().keySet()) {
            modeListBox.addItem(globalCachePluginData.getControlPanelModel().getModes().get(key), key);

            // Установка выделенного элемента
            if (globalCachePluginData.getControlPanelModel().getMode().equals(key)){
                modeListBox.setItemSelected(i, true);
            }
            i++;
        }


        controlGrid.setWidget(0, 3, modeListBox);

        Panel maxSizePanel = new HorizontalPanel();
        Long sizeM = globalCachePluginData.getControlPanelModel().getMaxSize() / 1024 / 1024;
        Long sizeG = globalCachePluginData.getControlPanelModel().getMaxSize() / 1024 / 1024 / 1000;
        maxSizeTB = new TextBox();
        maxSizeTB.setValue((globalCachePluginData.getControlPanelModel().getSizeUom().equals(GlobalCacheControlPanel.VALUE_UOM_MEGABYTE)?sizeM.toString():sizeG.toString()));
        maxSizePanel.add(maxSizeTB);

        uomListBox = new ListBox();
        for (String key : globalCachePluginData.getControlPanelModel().getUoms().keySet()) {
            uomListBox.addItem(globalCachePluginData.getControlPanelModel().getUoms().get(key), key);
        }
        uomListBox.setItemSelected(globalCachePluginData.getControlPanelModel().getUomIndex(),true);

        maxSizePanel.add(uomListBox);
        controlGrid.setWidget(1, 3, maxSizePanel);

        waitLockMillies = new TextBox();
        waitLockMillies.setValue(Integer.toString(globalCachePluginData.getControlPanelModel().getWaitLockMillies()));
        controlGrid.setWidget(2, 2, new Label(GlobalCacheControlUtils.WAIT_LOCK));
        controlGrid.setWidget(2, 3, waitLockMillies);
        /**
         * Если не админ то только просмотр
         */
        if(!globalCachePluginData.isSuperUser()){
            cacheActiveCB.setEnabled(false);
            expandedStatisticsCB.setEnabled(false);
            debugModeCB.setEnabled(false);
            modeListBox.setEnabled(false);
            maxSizeTB.setEnabled(false);
            waitLockMillies.setEnabled(false);
            uomListBox.setEnabled(false);
        }
        controlPanel.add(controlAlert);
        controlPanel.add(controlGrid);
        return controlPanel;
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
                buildExtendedStatisticsPanel();
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
                Window.alert(GlobalCacheControlUtils.MSG_SETTINGS_APPLYED);
                globalCachePluginData = (GlobalCachePluginData) result;
                buildControlPanel();
                buildButtons();
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
    private void getControlPanelState() {

        if (Boolean.TRUE.equals(expandedStatisticsCB.getValue()) &&
            !expandedStatisticsCB.getValue().equals(globalCachePluginData.getControlPanelModel().isExpandedStatistics())) {
            Window.alert("Внимание, сбор расширенной статистики приведёт к снижению производительности глобального кэша");
        }
        if (Boolean.TRUE.equals(debugModeCB.getValue()) &&
            !debugModeCB.getValue().equals(globalCachePluginData.getControlPanelModel().isDebugMode())) {
            Window.alert("Внимание, режим отладки приведёт к снижению производительности приложения.");
        }
        if(modeListBox.getSelectedValue().equals(GlobalCacheControlPanel.NON_BLOCKING_MODE_VALUE) &&
                !globalCachePluginData.getControlPanelModel().getMode().equals(GlobalCacheControlPanel.NON_BLOCKING_MODE_VALUE)){
            Window.alert("Внимание, асинхронный режим работы кэша является тестовым и категорически не рекомендуется при реальной эксплуатации");
        }

        globalCachePluginData.getControlPanelModel().setCacheEnabled(cacheActiveCB.getValue());
        globalCachePluginData.getControlPanelModel().setDebugMode(debugModeCB.getValue());
        globalCachePluginData.getControlPanelModel().setExpandedStatistics(expandedStatisticsCB.getValue());
        globalCachePluginData.getControlPanelModel().setMaxSize(Long.valueOf(maxSizeTB.getValue()));
        globalCachePluginData.getControlPanelModel().setWaitLockMillies(Integer.valueOf(waitLockMillies.getValue()));
        globalCachePluginData.getControlPanelModel().setMode(modeListBox.getSelectedValue());
        globalCachePluginData.getControlPanelModel().setSizeUom(uomListBox.getSelectedValue());
    }
}
