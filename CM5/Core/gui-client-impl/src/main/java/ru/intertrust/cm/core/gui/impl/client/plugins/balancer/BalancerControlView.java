package ru.intertrust.cm.core.gui.impl.client.plugins.balancer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.form.widget.buttons.ConfiguredButton;
import ru.intertrust.cm.core.gui.impl.client.plugins.globalcache.GlobalCacheControlUtils;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.plugin.BalancerControlPluginConfiguration;
import ru.intertrust.cm.core.gui.model.plugin.BalancerControlPluginData;
import ru.intertrust.cm.core.gui.model.plugin.BalancerControlPluginStatRow;
import ru.intertrust.cm.core.gui.model.plugin.BalancerControlPluginTypesRow;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

/**
 * Created by Ravil on 22.01.2018.
 */
public class BalancerControlView extends PluginView {
    private static final String LBL_MAIN_PANEL = "Главная";
    private static final String LBL_TYPES_PANEL = "Время изменения типов";
    private static final String LBL_CONFIG_PANEL = "Настройки";
    private Panel mainButtons;
    private Panel typesButtons;
    private Panel configButtons;
    private  TabPanel tabPanel;
    private AbsolutePanel mainPanel;
    private AbsolutePanel typesPanel;
    private AbsolutePanel configPanel;
    private BalancerControlPluginData pluginData;
    private FlexTable mainTable;
    private FlexTable typesTable;
    private TextBox rejectCounterTB;
    private TextBox problemCounterTB;
    /**
     * Основной конструктор
     *
     * @param plugin плагин, являющийся по сути, контроллером (или представителем) в паттерне MVC
     */
    protected BalancerControlView(Plugin plugin, EventBus eBus) {
        super(plugin);
        pluginData = plugin.getInitialData();
    }

    @Override
    public IsWidget getViewWidget() {
        Panel rootPanel = new AbsolutePanel();
        tabPanel = new TabPanel();
        buildMainPanel();
        buildTypesPanel();
        buildConfigPanel();
        tabPanel.selectTab(0);
        rootPanel.add(tabPanel);
        return rootPanel;
    }



    /**
     * Панели закладок
     */

    private void buildMainPanel(){
        mainTable = new FlexTable();
        mainPanel = new AbsolutePanel();
        mainPanel.add(buildMainPanelButtons());
        buildMainTable();
        mainPanel.add(mainTable);
        tabPanel.add(mainPanel,LBL_MAIN_PANEL);
    }

    private void buildMainTable(){
        mainTable.removeAllRows();
        mainTable.setStyleName("cacheCleaningTable");
        mainTable.getFlexCellFormatter().setRowSpan(0, 0, 3);
        mainTable.setWidget(0, 0, new InlineHTML("<span> </span>"));
        mainTable.getFlexCellFormatter().setRowSpan(0, 1, 3);
        mainTable.setWidget(0, 1, new InlineHTML("<span>Datasource</span>"));
        mainTable.getFlexCellFormatter().setRowSpan(0, 2, 3);
        mainTable.setWidget(0, 2, new InlineHTML("<span>Состояние</span>"));
        mainTable.getFlexCellFormatter().setRowSpan(0, 3, 3);
        mainTable.setWidget(0, 3, new InlineHTML("<span>Задержка, сек.</span>"));
        mainTable.getFlexCellFormatter().setRowSpan(0, 4, 3);
        mainTable.setWidget(0, 4, new InlineHTML("<span>Задержка, данные СУБД, сек.</span>"));
        mainTable.getFlexCellFormatter().setRowSpan(0, 5, 3);
        mainTable.setWidget(0, 5, new InlineHTML("<span>TX ID, данные СУБД</span>"));
        mainTable.getFlexCellFormatter().setColSpan(0, 6, 6);
        mainTable.setWidget(0, 6, new InlineHTML("<span>Расширенная статистика</span>"));
        mainTable.getFlexCellFormatter().setColSpan(1, 0, 3);
        mainTable.setWidget(1, 0, new InlineHTML("<span>Сейчас</span>"));
        mainTable.getFlexCellFormatter().setColSpan(1, 1, 3);
        mainTable.setWidget(1, 1, new InlineHTML("<span>Час</span>"));


        mainTable.setWidget(2, 0, new InlineHTML("<span>% попаданий</span>"));
        mainTable.setWidget(2, 1, new InlineHTML("<span>% сбоев</span>"));
        mainTable.setWidget(2, 2, new InlineHTML("<span>SELECT/сек</span>"));
        mainTable.setWidget(2, 3, new InlineHTML("<span>% попаданий</span>"));
        mainTable.setWidget(2, 4, new InlineHTML("<span>% сбоев</span>"));
        mainTable.setWidget(2, 5, new InlineHTML("<span>SELECT/сек</span>"));

        if(pluginData.getRows().isEmpty()){
            mainTable.getFlexCellFormatter().setColSpan(3, 0, 12);
            mainTable.setWidget(3, 0, new InlineHTML("<span> Нет данных </span>"));
        } else {
            int rowCounter = 3;
            for(BalancerControlPluginStatRow row : pluginData.getRows()){
                CheckBox cBox = new CheckBox();
                cBox.setName("cbox"+rowCounter);
                mainTable.setWidget(rowCounter, 0, cBox);
                mainTable.setWidget(rowCounter, 1, new InlineHTML("<span>"+row.getDataSource()+"</span>"));
                mainTable.setWidget(rowCounter, 2, new InlineHTML("<span>"+row.getState().getValue()+"</span>"));
                mainTable.setWidget(rowCounter, 3, new InlineHTML("<span>"+row.getDelay()+"</span>"));
                mainTable.setWidget(rowCounter, 4, new InlineHTML("<span>"+row.getDelayDbms()+"</span>"));
                mainTable.setWidget(rowCounter, 5, new InlineHTML("<span>"+row.gettXId()+"</span>"));
                mainTable.setWidget(rowCounter, 6, new InlineHTML("<span>"+row.getPercentageHitNow()+"</span>"));
                mainTable.setWidget(rowCounter, 7, new InlineHTML("<span>"+row.getFaultsNow()+"</span>"));
                mainTable.setWidget(rowCounter, 8, new InlineHTML("<span>"+row.getSelectSecNow()+"</span>"));
                mainTable.setWidget(rowCounter, 9, new InlineHTML("<span>"+row.getPercentageHitHour()+"</span>"));
                mainTable.setWidget(rowCounter, 10, new InlineHTML("<span>"+row.getFaultsHour()+"</span>"));
                mainTable.setWidget(rowCounter, 11, new InlineHTML("<span>"+row.getSelectSecHour()+"</span>"));
                rowCounter++;
            }
        }
    }

    private void buildTypesPanel(){
        typesTable = new FlexTable();
        typesPanel = new AbsolutePanel();
        typesPanel.add(buildTypesPanelButtons());
        buildTypesTable();
        typesPanel.add(typesTable);
        tabPanel.add(typesPanel,LBL_TYPES_PANEL);
    }

    private void buildTypesTable(){
        typesTable.removeAllRows();

        typesTable.setStyleName("cacheCleaningTable");

        typesTable.setWidget(0, 0, new InlineHTML("<span>Тип</span>"));
        typesTable.setWidget(0, 1, new InlineHTML("<span>MASTER, время</span>"));
        typesTable.setWidget(0, 2, new InlineHTML("<span>Мнимое</span>"));
        typesTable.setWidget(0, 3, new InlineHTML("<span>SLAVE_1, dt, сек</span>"));
        typesTable.setWidget(0, 4, new InlineHTML("<span>SLAVE_2, dt, сек</span>"));
        typesTable.setWidget(0, 5, new InlineHTML("<span>SLAVE_3, dt, сек</span>"));
        typesTable.setWidget(0, 6, new InlineHTML("<span>SLAVE_4, dt, сек</span>"));
        typesTable.setWidget(0, 7, new InlineHTML("<span>SLAVE_5, dt, сек</span>"));

        if(pluginData.getTypes().isEmpty()){
            typesTable.getFlexCellFormatter().setColSpan(1, 0, 8);
            typesTable.setWidget(1, 0, new InlineHTML("<span> Нет данных </span>"));
        } else {
            int rowCounter = 1;
            for(BalancerControlPluginTypesRow row : pluginData.getTypes()) {
                typesTable.setWidget(rowCounter, 0, new InlineHTML("<span>"+row.getType()+"</span>"));
                typesTable.setWidget(rowCounter, 1, new InlineHTML("<span>"+row.getMasterTime()+"</span>"));
                typesTable.setWidget(rowCounter, 2, new InlineHTML("<span>"+row.getImaginary()+"</span>"));
                typesTable.setWidget(rowCounter, 3, new InlineHTML("<span>"+row.getSlave1dt()+"</span>"));
                typesTable.setWidget(rowCounter, 4, new InlineHTML("<span>"+row.getSlave2dt()+"</span>"));
                typesTable.setWidget(rowCounter, 5, new InlineHTML("<span>"+row.getSlave3dt()+"</span>"));
                typesTable.setWidget(rowCounter, 6, new InlineHTML("<span>"+row.getSlave4dt()+"</span>"));
                typesTable.setWidget(rowCounter, 7, new InlineHTML("<span>"+row.getSlave5dt()+"</span>"));
            }
        }

    }

    private void buildConfigPanel(){
        configPanel = new AbsolutePanel();
        configPanel.add(buildConfigPanelButtons());
        Panel componentsPanel = new HorizontalPanel();
        componentsPanel.add(new Label(BalancerControlUtils.LBL_1));
        rejectCounterTB = new TextBox();
        rejectCounterTB.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent changeEvent) {
                pluginData.getConfiguration().setFaultRequest(Long.valueOf(rejectCounterTB.getValue()));
            }
        });
        rejectCounterTB.addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                String input = rejectCounterTB.getText();
                if (!input.matches("[0-9]*")) {
                    Window.alert("Должно быть число");
                    return;
                }
            }
        });

        rejectCounterTB.setValue(pluginData.getConfiguration().getFaultRequest().toString());
        componentsPanel.add(rejectCounterTB);
        componentsPanel.add(new Label(BalancerControlUtils.LBL_2));
        problemCounterTB = new TextBox();
        problemCounterTB.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent changeEvent) {
                pluginData.getConfiguration().setFaultRequest(Long.valueOf(problemCounterTB.getValue()));
            }
        });
        problemCounterTB.addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                String input = problemCounterTB.getText();
                if (!input.matches("[0-9]*")) {
                    Window.alert("Должно быть число");
                    return;
                }
            }
        });
        problemCounterTB.setValue(pluginData.getConfiguration().getProblemDelay().toString());
        componentsPanel.add(problemCounterTB);
        configPanel.add(componentsPanel);
        tabPanel.add(configPanel,LBL_CONFIG_PANEL);
    }


    /**
     * Панели кнопок
     * @return Панель с кнопками
     */
    private Widget buildMainPanelButtons(){
        mainButtons = new HorizontalPanel();
        mainButtons.addStyleName(GlobalCacheControlUtils.STYLE_TOP_MENU_BUTTONS);
        addCommonButtons(mainButtons);
        mainButtons.add(buildExtStatOnOffButton());
        mainButtons.add(buildExtStatResetButton());
        return mainButtons;
    }
    private Widget buildTypesPanelButtons(){
        typesButtons = new HorizontalPanel();
        typesButtons.addStyleName(GlobalCacheControlUtils.STYLE_TOP_MENU_BUTTONS);
        addCommonButtons(typesButtons);
        return typesButtons;
    }
    private Widget buildConfigPanelButtons(){
        configButtons = new HorizontalPanel();
        configButtons.addStyleName(GlobalCacheControlUtils.STYLE_TOP_MENU_BUTTONS);
        addCommonButtons(configButtons);
        configButtons.add(buildExtStatOnOffButton());
        configButtons.add(buildExtStatResetButton());
        configButtons.add(buildSaveButton());
        return configButtons;
    }

    private void addCommonButtons(Panel buttonPanel){
        buttonPanel.add(buildRefreshButton());
        buttonPanel.add(buildTurnOnButton());
        buttonPanel.add(buildTurnOffButton());
        buttonPanel.add(buildCheckButton());
    }

    /**
     * Кнопки с действиями
     * @return Кнопка
     */

    private Widget buildRefreshButton() {
        ConfiguredButton refreshButton = GlobalCacheControlUtils.createButton(BalancerControlUtils.BTN_REFRESH, GlobalCacheControlUtils.BTN_IMG_REFRESH);
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Command command = new Command("refreshPage", "BalancerControl.plugin", new BalancerControlPluginData());
                BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        caught.printStackTrace();
                        Window.alert("Ошибка команды обновления: "+caught.getMessage());
                    }

                    @Override
                    public void onSuccess(Dto result) {
                        pluginData = (BalancerControlPluginData) result;
                        buildMainTable();
                        buildTypesTable();
                    }
                });
            }
        });
        return refreshButton;
    }

    private Widget buildTurnOnButton() {
        ConfiguredButton refreshButton = GlobalCacheControlUtils.createButton(BalancerControlUtils.BTN_TURNON, GlobalCacheControlUtils.BTN_IMG_REFRESH);
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                serversOperation(true);;
            }
        });
        return refreshButton;
    }

    private Widget buildTurnOffButton() {
        ConfiguredButton refreshButton = GlobalCacheControlUtils.createButton(BalancerControlUtils.BTN_TURNOFF, GlobalCacheControlUtils.BTN_IMG_REFRESH);
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                serversOperation(false);
            }
        });
        return refreshButton;
    }

    private void serversOperation(Boolean isOn){
        BalancerControlPluginData request = new BalancerControlPluginData();
        request.setTurnOn(isOn);
        Command command = new Command("turnOnOff", "BalancerControl.plugin", request);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onFailure(Throwable caught) {
                caught.printStackTrace();
                Window.alert("Ошибка операции с сервером: "+caught.getMessage());
            }

            @Override
            public void onSuccess(Dto result) {

            }
        });
    }

    private Widget buildCheckButton() {
        ConfiguredButton refreshButton = GlobalCacheControlUtils.createButton(BalancerControlUtils.BTN_CHECK, GlobalCacheControlUtils.BTN_IMG_REFRESH);
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                BalancerControlPluginData request = new BalancerControlPluginData();
                Command command = new Command("checkServer", "BalancerControl.plugin", request);
                BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        caught.printStackTrace();
                        Window.alert("Ошибка операции с сервером: "+caught.getMessage());
                    }

                    @Override
                    public void onSuccess(Dto result) {
                        Window.alert(((BalancerControlPluginData)result).getMessage());
                    }
                });
            }
        });
        return refreshButton;
    }

    private Widget buildExtStatOnOffButton() {
        ConfiguredButton refreshButton = GlobalCacheControlUtils.createButton(BalancerControlUtils.BTN_EXTSTATONOFF, GlobalCacheControlUtils.BTN_IMG_REFRESH);
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                BalancerControlPluginData request = new BalancerControlPluginData();
                Command command = new Command("extStatOnOff", "BalancerControl.plugin", request);
                BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        caught.printStackTrace();
                        Window.alert("Ошибка операции с сервером: "+caught.getMessage());
                    }

                    @Override
                    public void onSuccess(Dto result) {
                        Window.alert(((BalancerControlPluginData)result).getMessage());
                    }
                });
            }
        });
        return refreshButton;
    }

    private Widget buildExtStatResetButton() {
        ConfiguredButton refreshButton = GlobalCacheControlUtils.createButton(BalancerControlUtils.BTN_EXTSTATRESET, GlobalCacheControlUtils.BTN_IMG_REFRESH);
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Command command = new Command("extStatReset", "BalancerControl.plugin", new BalancerControlPluginData());
                BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        caught.printStackTrace();
                        Window.alert("Ошибка операции с сервером: "+caught.getMessage());
                    }

                    @Override
                    public void onSuccess(Dto result) {
                        ;
                    }
                });
            }
        });
        return refreshButton;
    }

    private Widget buildSaveButton() {
        ConfiguredButton refreshButton = GlobalCacheControlUtils.createButton(BalancerControlUtils.BTN_SAVE, GlobalCacheControlUtils.BTN_IMG_REFRESH);
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                BalancerControlPluginData request = new BalancerControlPluginData();
                request.setConfiguration(pluginData.getConfiguration());
                Command command = new Command("saveConfig", "BalancerControl.plugin", new BalancerControlPluginData());
                BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        caught.printStackTrace();
                        Window.alert("Ошибка операции с сервером: "+caught.getMessage());
                    }

                    @Override
                    public void onSuccess(Dto result) {
                        Window.alert(((BalancerControlPluginData)result).getMessage());
                    }
                });
            }
        });
        return refreshButton;
    }
}
