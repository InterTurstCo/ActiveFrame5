package ru.intertrust.cm.core.gui.impl.client.plugins.listplugin;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;

/**
 * Created by Ravil on 11.04.2017.
 */
public class ListPluginView extends PluginView {
    private EventBus commonBus;
    /**
     * Основной конструктор
     *
     * @param plugin плагин, являющийся по сути, контроллером (или представителем) в паттерне MVC
     */
    protected ListPluginView(Plugin plugin, EventBus anEventBus) {
        super(plugin);
        commonBus = anEventBus;
    }

    @Override
    public IsWidget getViewWidget() {
        VerticalPanel rootPanel = new VerticalPanel();
        rootPanel.add(new Label("Just a text"));
        rootPanel.add(new Label("Just a text"));
        rootPanel.add(new Label("Just a text"));
        rootPanel.add(new Label("Just a text"));
        rootPanel.add(new Label("Just a text"));
        rootPanel.add(new Label("Just a text"));
        return rootPanel;
    }
}
