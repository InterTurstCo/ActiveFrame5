package ru.intertrust.cm.core.gui.impl.client.plugins.dragpanel;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;

public class DragPanelPluginView extends PluginView {
    protected DragPanelPluginView(Plugin plugin) {
        super(plugin);
    }

    @Override
    public IsWidget getViewWidget() {
        FlowPanel dragPanelContainer = new FlowPanel();
        final Button drugBtn = new Button("dragpanel");
        dragPanelContainer.getElement().getStyle().setProperty("backgroundColor", "#EEE");
        dragPanelContainer.getElement().getStyle().setProperty("margin", "5px");
        dragPanelContainer.add(drugBtn);
        return dragPanelContainer;
    }
}
