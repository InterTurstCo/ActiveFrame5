package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 17/9/13
 *         Time: 12:05 PM
 */
public class CollectionPluginView extends PluginView {
    protected CollectionPluginView(Plugin plugin) {
        super(plugin);
    }

    @Override
    protected IsWidget getViewWidget() {
        //FlowPanel dragPanelContainer = new FlowPanel();
        final Button drugBtn = new Button("collection");
        //dragPanelContainer.getElement().getStyle().setProperty("backgroundColor", "#EEE");
        //dragPanelContainer.getElement().getStyle().setProperty("margin", "5px");
        //dragPanelContainer.add(drugBtn);
        return drugBtn;
    }
}