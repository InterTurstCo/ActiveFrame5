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
 * @author Denis Mitavskiy
 *         Date: 19.08.13
 *         Time: 13:57
 */
public abstract class PluginView implements IsWidget {
    protected Plugin plugin;

    protected PluginView(Plugin plugin) {
        this.plugin = plugin;
    }

    protected IsWidget buildActionToolBar() {
        // todo: do this only if plugin is Active
        List<ActionConfig> actionConfigs = ((ActivePluginData) plugin.getInitialData()).getActionConfigs();
        return new Label("This is a tool bar for now");
    }

    protected abstract IsWidget getViewWidget();

    @Override
    public Widget asWidget() {
        VerticalPanel panel = new VerticalPanel();
        if (plugin instanceof IsActive) {
            IsWidget actionToolBar = buildActionToolBar();
            if (actionToolBar != null) {
                panel.add(actionToolBar);
            }
        }
        panel.add(getViewWidget());
        return panel;
    }
}
