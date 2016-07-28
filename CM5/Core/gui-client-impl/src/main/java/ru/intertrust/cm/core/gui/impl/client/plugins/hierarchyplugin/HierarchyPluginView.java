package ru.intertrust.cm.core.gui.impl.client.plugins.hierarchyplugin;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import ru.intertrust.cm.core.config.gui.navigation.hierarchyplugin.HierarchyPluginConfig;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 26.07.2016
 * Time: 14:36
 * To change this template use File | Settings | File and Code Templates.
 */
public class HierarchyPluginView extends PluginView {
    private HierarchyGuiFactory guiFactory;
    /**
     * Основной конструктор
     *
     * @param plugin плагин, являющийся по сути, контроллером (или представителем) в паттерне MVC
     */
    protected HierarchyPluginView(Plugin plugin, EventBus anEventBus) {
        super(plugin);
        guiFactory = new HierarchyGuiFactory();
    }

    @Override
    public IsWidget getViewWidget() {
        HorizontalPanel rootPanel = new HorizontalPanel();
        rootPanel.addStyleName("wrapWidget");
        rootPanel.add(guiFactory.buildGroup(((HierarchyPluginConfig)plugin.getConfig()).getHierarchyGroupConfig()));
        return rootPanel;
    }
}
