package ru.intertrust.cm.core.gui.impl.client.plugins.balancer;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.form.widget.buttons.ConfiguredButton;
import ru.intertrust.cm.core.gui.impl.client.plugins.globalcache.GlobalCacheControlUtils;

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
    /**
     * Основной конструктор
     *
     * @param plugin плагин, являющийся по сути, контроллером (или представителем) в паттерне MVC
     */
    protected BalancerControlView(Plugin plugin, EventBus eBus) {
        super(plugin);
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
        FlexTable mainTable = new FlexTable();
        mainPanel = new AbsolutePanel();
        mainPanel.add(buildMainPanelButtons());

        mainTable.clear();
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
        mainTable.getFlexCellFormatter().setColSpan(0, 5, 6);
        mainTable.setWidget(0, 5, new InlineHTML("<span>Расширенная статистика</span>"));
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

/*



        mainTable.setWidget(1, 2, new InlineHTML("<span>Среднее.</span>"));
        mainTable.setWidget(2, 1, new InlineHTML("<span>" + "4" + "</span>"));
        mainTable.setWidget(2, 2, new InlineHTML("<span>" + "18" + "</span>"));
        mainTable.setWidget(2, 3, new InlineHTML("<span>" + "34" + "</span>"));
        mainTable.getFlexCellFormatter().setColSpan(0, 2, 3);
        mainTable.setWidget(0, 2, new InlineHTML("<span>Очистка, %</span>"));
        mainTable.setWidget(1, 3, new InlineHTML("<span>Мин.</span>"));
        mainTable.setWidget(1, 4, new InlineHTML("<span>Макс.</span>"));
        mainTable.setWidget(1, 5, new InlineHTML("<span>Среднее.</span>"));
        mainTable.setWidget(2, 4, new InlineHTML("<span>" + "45" + "</span>"));
        mainTable.setWidget(2, 5, new InlineHTML("<span>" + "1" + "</span>"));
        mainTable.setWidget(2, 6, new InlineHTML("<span>" + "12" + "</span>"));
        mainTable.getFlexCellFormatter().setRowSpan(0, 3, 2);
        mainTable.setWidget(0, 3, new InlineHTML("<span>Кол-во</span>"));
        mainTable.setWidget(2, 7, new InlineHTML("<span>" + "54" + "</span>"));
        */
        mainPanel.add(mainTable);
        tabPanel.add(mainPanel,LBL_MAIN_PANEL);
    }

    private void buildTypesPanel(){
        typesPanel = new AbsolutePanel();
        typesPanel.add(buildTypesPanelButtons());
        tabPanel.add(typesPanel,LBL_TYPES_PANEL);
    }

    private void buildConfigPanel(){
        configPanel = new AbsolutePanel();
        configPanel.add(buildConfigPanelButtons());
        Panel componentsPanel = new HorizontalPanel();
        componentsPanel.add(new Label(BalancerControlUtils.LBL_1));
        TextBox rejectCounterTB = new TextBox();
        componentsPanel.add(rejectCounterTB);
        componentsPanel.add(new Label(BalancerControlUtils.LBL_2));
        TextBox problemCounterTB = new TextBox();
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
                ;
            }
        });
        return refreshButton;
    }

    private Widget buildTurnOnButton() {
        ConfiguredButton refreshButton = GlobalCacheControlUtils.createButton(BalancerControlUtils.BTN_TURNON, GlobalCacheControlUtils.BTN_IMG_REFRESH);
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ;
            }
        });
        return refreshButton;
    }

    private Widget buildTurnOffButton() {
        ConfiguredButton refreshButton = GlobalCacheControlUtils.createButton(BalancerControlUtils.BTN_TURNOFF, GlobalCacheControlUtils.BTN_IMG_REFRESH);
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ;
            }
        });
        return refreshButton;
    }

    private Widget buildCheckButton() {
        ConfiguredButton refreshButton = GlobalCacheControlUtils.createButton(BalancerControlUtils.BTN_CHECK, GlobalCacheControlUtils.BTN_IMG_REFRESH);
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ;
            }
        });
        return refreshButton;
    }

    private Widget buildExtStatOnOffButton() {
        ConfiguredButton refreshButton = GlobalCacheControlUtils.createButton(BalancerControlUtils.BTN_EXTSTATONOFF, GlobalCacheControlUtils.BTN_IMG_REFRESH);
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ;
            }
        });
        return refreshButton;
    }

    private Widget buildExtStatResetButton() {
        ConfiguredButton refreshButton = GlobalCacheControlUtils.createButton(BalancerControlUtils.BTN_EXTSTATRESET, GlobalCacheControlUtils.BTN_IMG_REFRESH);
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ;
            }
        });
        return refreshButton;
    }

    private Widget buildSaveButton() {
        ConfiguredButton refreshButton = GlobalCacheControlUtils.createButton(BalancerControlUtils.BTN_SAVE, GlobalCacheControlUtils.BTN_IMG_REFRESH);
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ;
            }
        });
        return refreshButton;
    }
}
