package ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.model.gui.navigation.DomainObjectSurferConfig;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionPlugin;
import ru.intertrust.cm.core.gui.model.plugin.CollectionPluginData;
import ru.intertrust.cm.core.gui.model.plugin.SomeActivePluginConfig;

import java.util.List;
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
                    List<Id> ids = collectionPluginData.getIds();
                    SomeActivePluginConfig config;
                    if (ids == null || ids.size() == 0) {
                        // open empty form for collection domain object type
                        config = new SomeActivePluginConfig(collectionPluginData.getCollectionConfig().getDomainObjectType());
                    } else {
                        config = new SomeActivePluginConfig(ids.get(0));
                    }
                    Plugin plugin = ComponentRegistry.instance.get("some.active.plugin");
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
