package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.gui.model.ActionConfig;
import ru.intertrust.cm.core.gui.model.plugin.ActivePluginData;
import ru.intertrust.cm.core.gui.model.plugin.IsActive;

import java.util.List;

/**
 * Базовый класс представления плагина.
 *
 * @author Denis Mitavskiy
 *         Date: 19.08.13
 *         Time: 13:57
 */
public abstract class PluginView implements IsWidget {
    protected Plugin plugin;

    /**
     * Основной конструктор
     * @param plugin плагин, являющийся по сути, контроллером (или представителем) в паттерне MVC
     */
    protected PluginView(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Строит "Панель действий" плагина
     * @return возвращает виджет, отображающий "Панель действий"
     */
    protected IsWidget getActionToolBar() {
        // todo: do this only if plugin is Active
        ActivePluginData initialData = plugin.getInitialData();
        if (initialData == null) {
            return new Label("This is an empty tool bar for now");
            // todo return null;
        }
        List<ActionConfig> actionConfigs = initialData.getActionConfigs();
        int size = actionConfigs == null ? 0 : actionConfigs.size();

        return new Label("This is a tool bar with actions for now. Actions: " + actionConfigs);
    }

    /**
     * Строит и возвращает представление (внешнее отображение) плагина
     * @return виджет, представляющий плагин
     */
    protected abstract IsWidget getViewWidget();

    @Override
    public Widget asWidget() {
        VerticalPanel panel = new VerticalPanel();
        if (plugin instanceof IsActive) {
            IsWidget actionToolBar = getActionToolBar();
            if (actionToolBar != null) {
                panel.add(actionToolBar);
            }
        }
        panel.add(getViewWidget());
        return panel;
    }
}
