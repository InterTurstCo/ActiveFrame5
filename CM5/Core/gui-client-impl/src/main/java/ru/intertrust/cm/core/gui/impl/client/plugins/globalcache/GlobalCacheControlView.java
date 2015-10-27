package ru.intertrust.cm.core.gui.impl.client.plugins.globalcache;

import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.config.gui.globalcachecontrol.GlobalCacheControlPanel;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;

/**
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 21.10.2015
 */
public class GlobalCacheControlView extends PluginView {



    private Boolean statisticsOnly;
    private GlobalCacheControlPanel controlPanelModel;

    /**
     * Основной конструктор
     *
     * @param plugin плагин, являющийся по сути, контроллером (или представителем) в паттерне MVC
     */
    protected GlobalCacheControlView(Plugin plugin, Boolean statisticsOnly) {
        super(plugin);
        this.statisticsOnly = statisticsOnly;
        this.controlPanelModel = new GlobalCacheControlPanel();
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
        Grid controlGrid = new Grid(3,4);

        controlGrid.setWidget(0,0,new Label(GlobalCacheControlUtils.LBL_CONTROL_PANEL_CACHE_ACTIVE));
        controlGrid.setWidget(1,0,new Label(GlobalCacheControlUtils.LBL_CONTROL_PANEL_EXPANDED_STAT));
        controlGrid.setWidget(2,0,new Label(GlobalCacheControlUtils.LBL_CONTROL_PANEL_DEBUG_MODE));

        // чекбокс Включить кэш
        CheckBox cacheActiveCB = new CheckBox();
        cacheActiveCB.setValue(controlPanelModel.isCacheActive());
        controlGrid.setWidget(0,1,cacheActiveCB);
        // чекбокс Расширенная статистика
        CheckBox expandedStatisticsCB = new CheckBox();
        expandedStatisticsCB.setValue(controlPanelModel.isExpandedStatistics());
        controlGrid.setWidget(1,1,expandedStatisticsCB);

        // чекбокс Режим отладки
        CheckBox debugModeCB = new CheckBox();
        debugModeCB.setValue(controlPanelModel.isDebugMode());
        controlGrid.setWidget(2,1,debugModeCB);

        controlGrid.setWidget(0,2,new Label(GlobalCacheControlUtils.LBL_CONTROL_PANEL_MODE));
        controlGrid.setWidget(1,2,new Label(GlobalCacheControlUtils.LBL_CONTROL_PANEL_MAX_SIZE));

        ListBox modeListBox = new ListBox();
        for(String key :controlPanelModel.getModes().keySet()){
            modeListBox.addItem(controlPanelModel.getModes().get(key),key);
        }
        controlGrid.setWidget(0,3,modeListBox);

        Panel maxSizePanel = new HorizontalPanel();
        IntegerBox maxSizeTB = new IntegerBox();
        maxSizeTB.setValue(controlPanelModel.getMaxSize().intValue());
        maxSizePanel.add(maxSizeTB);
        ListBox uomListBox = new ListBox();
        for(String key :controlPanelModel.getUoms().keySet()){
            uomListBox.addItem(controlPanelModel.getUoms().get(key),key);
        }
        maxSizePanel.add(uomListBox);
        controlGrid.setWidget(1,3,maxSizePanel);
        return controlGrid;
    }



}
