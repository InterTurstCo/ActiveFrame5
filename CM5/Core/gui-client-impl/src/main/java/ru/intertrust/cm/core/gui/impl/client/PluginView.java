package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.config.model.gui.ActionConfig;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.action.Action;
import ru.intertrust.cm.core.gui.model.plugin.ActivePluginData;
import ru.intertrust.cm.core.gui.model.plugin.IsActive;

import java.util.List;
import java.util.logging.Logger;

/**
 * Базовый класс представления плагина.
 *
 * @author Denis Mitavskiy
 *         Date: 19.08.13
 *         Time: 13:57
 */
public abstract class PluginView implements IsWidget {
    protected Plugin plugin;
    protected static Logger log = Logger.getLogger("PluginView console logger");


    /**
     * Основной конструктор
     *
     * @param plugin плагин, являющийся по сути, контроллером (или представителем) в паттерне MVC
     */
    protected PluginView(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Строит "Панель действий" плагина
     *
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
        HorizontalPanel actionPanel = new HorizontalPanel();
        if (actionConfigs == null) {
            return new Label("Empty panel - fix later");
        }
        for (final ActionConfig actionConfig : actionConfigs) {
            actionPanel.add(new Button(actionConfig.getText(), new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    String component = actionConfig.getComponent();
                    if (component == null) {
                        component = "generic.workflow.action";
                    }
                    ((Action) ComponentRegistry.instance.get(component)).execute();
                }
            }));
        }

        return actionPanel;
    }

    /**
     * Строит и возвращает представление (внешнее отображение) плагина
     *
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
