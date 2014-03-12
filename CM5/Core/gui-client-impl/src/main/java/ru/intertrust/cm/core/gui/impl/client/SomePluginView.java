package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;

import ru.intertrust.cm.core.gui.model.plugin.SomePluginData;

/**
 * @author Denis Mitavskiy
 *         Date: 23.08.13
 *         Time: 15:28
 */
public class SomePluginView extends PluginView {
    protected SomePluginView(Plugin plugin) {
        super(plugin);
    }

    @Override
    public IsWidget getViewWidget() {
       SomePluginData somePluginData = plugin.getInitialData();
        return new Label(somePluginData.getText());
    }
}
