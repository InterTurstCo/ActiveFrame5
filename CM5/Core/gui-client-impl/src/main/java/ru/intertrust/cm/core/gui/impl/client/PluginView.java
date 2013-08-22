package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Denis Mitavskiy
 *         Date: 19.08.13
 *         Time: 13:57
 */
public class PluginView implements IsWidget {
    @Override
    public Widget asWidget() {
        return new Button("This is a button");
    }
}
