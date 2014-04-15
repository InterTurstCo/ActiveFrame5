package ru.intertrust.cm.core.gui.impl.client.action;


import java.util.List;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;

import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;

/**
 * @author Sergey.Okolot
 *         Created on 07.04.2014 11:50.
 */
@ComponentName(value = "plugin.history.action")
public class PluginHistoryAction extends Action {
    @Override
    public void execute() {
        final RootLayoutPanel rootLayoutPanel = RootLayoutPanel.get();
        final int widgetCount = rootLayoutPanel.getWidgetCount();
        for (int index = 0; index < widgetCount; index++) {
            final Widget childWidget = rootLayoutPanel.getWidget(index);

        }
    }

    private void fillHistoryData(final Widget parent) {

    }

    @Override
    public Component createNew() {
        return new PluginHistoryAction();
    }
}
