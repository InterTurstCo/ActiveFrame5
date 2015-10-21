package ru.intertrust.cm.core.gui.impl.client.plugins.globalcache;

import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;

/**
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 21.10.2015
 */
public class GlobalCacheControlView extends PluginView {



    private Boolean statisticsOnly;

    /**
     * Основной конструктор
     *
     * @param plugin плагин, являющийся по сути, контроллером (или представителем) в паттерне MVC
     */
    protected GlobalCacheControlView(Plugin plugin, Boolean statisticsOnly) {
        super(plugin);
        this.statisticsOnly = statisticsOnly;
    }

    @Override
    public IsWidget getViewWidget() {
        return buildRootPanel();
    }

    private Widget buildRootPanel() {
        Panel rootPanel = new AbsolutePanel();
        Panel buttons = new HorizontalPanel();
        buttons.addStyleName(GlobalCacheControlUtils.STYLE_TOP_MENU_BUTTONS);
        rootPanel.add(buttons);
        buttons.add(GlobalCacheControlUtils.createButton(GlobalCacheControlUtils.STAT_REFRESH, GlobalCacheControlUtils.BTN_IMG_REFRESH));
        buttons.add(GlobalCacheControlUtils.createButton(GlobalCacheControlUtils.STAT_APPLY, GlobalCacheControlUtils.BTN_IMG_APPLY));
        buttons.add(GlobalCacheControlUtils.createButton(GlobalCacheControlUtils.STAT_RESET, GlobalCacheControlUtils.BTN_IMG_RESET));
        buttons.add(GlobalCacheControlUtils.createButton(GlobalCacheControlUtils.STAT_CLEAR_CACHE, GlobalCacheControlUtils.BTN_IMG_CLEAR));
        TabPanel tabPanel = new TabPanel();
        tabPanel.add(buildStatisticsPanel(),GlobalCacheControlUtils.LBL_PANEL_STAT);
        tabPanel.add(buildControlPanel(),GlobalCacheControlUtils.LBL_PANEL_CONTROL);
        tabPanel.selectTab(0);
        tabPanel.getWidget(0).getParent().getElement().getParentElement()
                .addClassName("gwt-TabLayoutPanel-wrapper");
        rootPanel.add(tabPanel);
        return rootPanel;
    }

    private Widget buildStatisticsPanel(){
        return new Label("Панель статистики");
    }
    private Widget buildControlPanel(){
        return new Label("Панель управления");
    }



}
