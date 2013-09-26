package ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import ru.intertrust.cm.core.config.model.gui.navigation.DomainObjectSurferConfig;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionPlugin;
import ru.intertrust.cm.core.gui.model.plugin.SomeActivePluginConfig;

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
        final VerticalPanel container = new VerticalPanel();
      //  container.setWidth("100%");
        DomainObjectSurferConfig config = (DomainObjectSurferConfig) domainObjectSurferPlugin.getConfig();
        if (config != null) {
            log.info("plugin config, collection = " + config.getCollectionViewerConfig().getCollectionRefConfig().getName());
            final PluginPanel formPluginPanel = new PluginPanel(domainObjectSurferPlugin.getEventBus());

            CollectionPlugin collectionViewerPlugin = ComponentRegistry.instance.get("collection.plugin");

            collectionViewerPlugin.setConfig(config.getCollectionViewerConfig());
            PluginPanel collectionViewerPluginPanel = new PluginPanel(domainObjectSurferPlugin.getEventBus()) {
                @Override
                public void beforePluginOpening() {
                   // formPluginPanel.setSize("200px", "300px");
                    Plugin plugin = ComponentRegistry.instance.get("some.active.plugin");
                    SomeActivePluginConfig config = new SomeActivePluginConfig("country");
                    plugin.setConfig(config);
                    formPluginPanel.open(plugin);
                    SimpleLayoutPanel layoutPanel = new SimpleLayoutPanel();
                    //layoutPanel.setSize("500px", "300px");
                    layoutPanel.add(formPluginPanel);
                    formPluginPanel.open(plugin);
                    container.add(formPluginPanel);
                }
            };
            collectionViewerPluginPanel.open(collectionViewerPlugin);
            container.add(collectionViewerPluginPanel);
        }
        return container;
    }

}
