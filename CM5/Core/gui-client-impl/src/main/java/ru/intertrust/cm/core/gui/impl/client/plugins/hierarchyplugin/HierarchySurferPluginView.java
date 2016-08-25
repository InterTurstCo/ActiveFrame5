package ru.intertrust.cm.core.gui.impl.client.plugins.hierarchyplugin;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.IsWidget;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 25.08.2016
 * Time: 10:18
 * To change this template use File | Settings | File and Code Templates.
 */
public class HierarchySurferPluginView extends PluginView {
    private AbsolutePanel rootSurferPanel;
    /**
     * Основной конструктор
     *
     * @param plugin плагин, являющийся по сути, контроллером (или представителем) в паттерне MVC
     */
    protected HierarchySurferPluginView(Plugin plugin) {
        super(plugin);
    }

    @Override
    public IsWidget getViewWidget() {
        rootSurferPanel = new AbsolutePanel();
        return rootSurferPanel;
    }
}
