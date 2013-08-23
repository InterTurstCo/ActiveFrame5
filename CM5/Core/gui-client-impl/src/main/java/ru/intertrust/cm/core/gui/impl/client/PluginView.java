package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

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
        return new Label("This is a tool bar for now");
    }

    protected abstract IsWidget getViewWidget();

    @Override
    public Widget asWidget() {
        VerticalPanel panel = new VerticalPanel();
        IsWidget actionToolBar = buildActionToolBar();
        if (actionToolBar != null) {
            panel.add(actionToolBar);
        }
        panel.add(getViewWidget());
        return panel;
    }
}
