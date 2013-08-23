package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.IsWidget;

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
        return new Button("This is a button");
    }
}
