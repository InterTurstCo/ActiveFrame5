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
        mainPanel = new AbsolutePanel();
        mainPanel.add(buildMainPanelButtons());
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
