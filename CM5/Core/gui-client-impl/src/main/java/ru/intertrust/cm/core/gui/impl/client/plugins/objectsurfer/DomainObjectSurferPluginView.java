package ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.PluginView;

public class DomainObjectSurferPluginView extends PluginView {

    public DomainObjectSurferPluginView(Plugin domainObjectSurferPlugin) {
        super(domainObjectSurferPlugin);
    }

    @Override
    protected IsWidget getViewWidget() {
        final VerticalPanel container = new VerticalPanel();
        final PluginPanel itemViewerPluginPanel = new PluginPanel(plugin.getEventBus());
        final ItemViewerPlugin itemViewerPlugin = ComponentRegistry.instance.get("item.viewer.plugin");
        CollectionViewerPlugin collectionViewerPlugin = ComponentRegistry.instance.get("collection.viewer.plugin");
        PluginPanel collectionViewerPluginPanel = new PluginPanel(plugin.getEventBus()) {

            @Override
            public void beforePluginOpening() {
                itemViewerPluginPanel.open(itemViewerPlugin);
                container.add(itemViewerPluginPanel);
            }
        };
        collectionViewerPluginPanel.open(collectionViewerPlugin);
        container.add(collectionViewerPluginPanel);
        return container;
    }

}
