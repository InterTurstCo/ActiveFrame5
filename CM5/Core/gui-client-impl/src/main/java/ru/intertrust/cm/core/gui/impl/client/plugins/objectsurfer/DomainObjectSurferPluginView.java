package ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import ru.intertrust.cm.core.config.model.gui.navigation.DomainObjectSurferConfig;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.PluginView;

import java.util.logging.Logger;

public class DomainObjectSurferPluginView extends PluginView {

    private DomainObjectSurferPlugin domainObjectSurferPlugin;
    static Logger log = Logger.getLogger("DomainObjectSurfer");

    public DomainObjectSurferPluginView(DomainObjectSurferPlugin domainObjectSurferPlugin) {
        super(domainObjectSurferPlugin);
        this.domainObjectSurferPlugin = domainObjectSurferPlugin;
    }

    @Override
    protected IsWidget getViewWidget() {
        DomainObjectSurferConfig config = (DomainObjectSurferConfig) domainObjectSurferPlugin.getConfig();
        log.info("plugin config, collection = " + config.getCollectionViewerConfig().getCollectionRefConfig().getName());
        final VerticalPanel container = new VerticalPanel();
        final PluginPanel itemViewerPluginPanel = new PluginPanel(domainObjectSurferPlugin.getEventBus());
        final ItemViewerPlugin itemViewerPlugin = ComponentRegistry.instance.get("item.viewer.plugin");

        CollectionViewerPlugin collectionViewerPlugin = ComponentRegistry.instance.get("collection.viewer.plugin");


        collectionViewerPlugin.setConfig(config);
        PluginPanel collectionViewerPluginPanel = new PluginPanel(domainObjectSurferPlugin.getEventBus()) {
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
