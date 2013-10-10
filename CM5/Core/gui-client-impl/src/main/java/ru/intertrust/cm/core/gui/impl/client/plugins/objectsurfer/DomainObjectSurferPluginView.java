package ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.config.model.gui.navigation.DomainObjectSurferConfig;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionPlugin;
import ru.intertrust.cm.core.gui.model.plugin.CollectionPluginData;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;

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

        FlowPanel flowPanel = new FlowPanel();
        flowPanel.setStyleName("centerTopBottomDividerRoot");
        final SplitLayoutPanel splitterNew = new SplitLayoutPanel(8);
        int width = Window.getClientWidth() - 260;
        int heigt = Window.getClientHeight() - 190;
        splitterNew.setSize(width + "px", heigt + "px");

        final VerticalPanel container = new VerticalPanel();
        flowPanel.add(container);
        VerticalPanel w = new VerticalPanel();
        w.setHeight("40px");
        w.setWidth("100%");
        container.add(w);
        container.add(splitterNew);

        DomainObjectSurferConfig config = (DomainObjectSurferConfig) domainObjectSurferPlugin.getConfig();
        if (config != null) {
            log.info("plugin config, collection = " + config.getCollectionViewerConfig().getCollectionRefConfig().getName());
            final PluginPanel formPluginPanel = new PluginPanel(domainObjectSurferPlugin.getEventBus());
            final CollectionPlugin collectionViewerPlugin = ComponentRegistry.instance.get("collection.plugin");
            collectionViewerPlugin.setConfig(config.getCollectionViewerConfig());
            PluginPanel collectionViewerPluginPanel = new PluginPanel(domainObjectSurferPlugin.getEventBus()) {
                @Override
                public void beforePluginOpening() {
                    CollectionPluginData collectionPluginData = collectionViewerPlugin.getInitialData();
                    IdentifiableObjectCollection collection = collectionPluginData.getCollection();
                    FormPluginConfig config;
                    if (collection == null || collection.size() == 0) {
                        // open empty form for collection domain object type
                        config = new FormPluginConfig(collectionPluginData.getCollectionConfig().getDomainObjectType());
                    } else {
                        config = new FormPluginConfig(collection.get(0).getId());
                    }
                    Plugin plugin = ComponentRegistry.instance.get("form.plugin");
                    domainObjectSurferPlugin.setFormPlugin(plugin);
                    plugin.setConfig(config);
                    formPluginPanel.open(plugin);
                    splitterNew.addNorth(new ScrollPanel(this.asWidget()), 300);
                    FlowPanel formFlowPanel = new FlowPanel();
                    formFlowPanel.setStyleName("tab-content");
                    formFlowPanel.setSize("100%", "100%");
                    formFlowPanel.add(new ScrollPanel(formPluginPanel.asWidget()));
                    splitterNew.add(formFlowPanel);

                }
            };
            collectionViewerPluginPanel.open(collectionViewerPlugin);


        }
        return flowPanel;
    }

}
