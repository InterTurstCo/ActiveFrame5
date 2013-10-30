package ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.config.model.gui.navigation.DomainObjectSurferConfig;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.event.SplitterInnerScrollEvent;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionPlugin;
import ru.intertrust.cm.core.gui.model.plugin.CollectionPluginData;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;

import java.util.logging.Logger;

public class DomainObjectSurferPluginView extends PluginView {


    FlowPanel formFlowPanel = new FlowPanel();
    SimplePanel splitterNorthPanel = new SimplePanel();
    ScrollPanel splitterScroll = new ScrollPanel();
    private DomainObjectSurferPlugin domainObjectSurferPlugin;
    SplitLayoutPanel splitterNew = new SplitLayoutPanel(8){
        @Override
        public void onResize() {
            super.onResize();
            domainObjectSurferPlugin.getEventBus()
                    .fireEvent(new SplitterInnerScrollEvent(splitterScroll.getOffsetHeight(),
                            formFlowPanel.getOffsetHeight()));
        }
    };
    static Logger log = Logger.getLogger("DomainObjectSurfer");

    public DomainObjectSurferPluginView(DomainObjectSurferPlugin domainObjectSurferPlugin) {
        super(domainObjectSurferPlugin);
        this.domainObjectSurferPlugin = domainObjectSurferPlugin;
        splitterScroll.getElement().getStyle().setOverflowY(Style.Overflow.HIDDEN);
        splitterSetSize();
        addWindowResizeListeners();


    }

    protected void splitterSetSize(){
        splitterNew.clear();
        int width = Window.getClientWidth() - 235;
        int heigt = Window.getClientHeight() - 98;
        splitterNew.setSize(width + "px", heigt + "px");
        splitterNew.addNorth(splitterScroll, (Window.getClientHeight()-235) / 2);
        splitterNew.add(formFlowPanel);
    }

    private void addWindowResizeListeners(){
        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                splitterSetSize();
            }
        });
    }

    @Override
    protected IsWidget getViewWidget() {

        FlowPanel flowPanel = new FlowPanel();
        flowPanel.setStyleName("centerTopBottomDividerRoot");
        final VerticalPanel container = new VerticalPanel();
        flowPanel.add(container);
        splitterScroll.add(splitterNorthPanel);



//        VerticalPanel w = new VerticalPanel();
//        w.setHeight("40px");
//        w.setWidth("100%");
//        container.add(w);
        container.add(splitterNew);

        final DomainObjectSurferConfig config = (DomainObjectSurferConfig) domainObjectSurferPlugin.getConfig();
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
                    FormPluginConfig formPluginConfig;
                    if (collection == null || collection.size() == 0) {
                        // TODO: New approach needed, collection type is no longer available
                        // open empty form for collection domain object type
                        // config = new SomeActivePluginConfig(collectionPluginData.getCollectionConfig()
                        // .getDomainObjectType());
                        formPluginConfig = new FormPluginConfig(config.getDomainObjectTypeToCreate());
                    } else {
                        formPluginConfig = new FormPluginConfig(collection.get(0).getId());
                    }
                    Plugin plugin = ComponentRegistry.instance.get("form.plugin");
                    domainObjectSurferPlugin.setFormPlugin(plugin);
                    plugin.setConfig(formPluginConfig);
                    formPluginPanel.open(plugin);
                    splitterNorthPanel.add(this.asWidget());



                    formFlowPanel.setStyleName("tab-content");
                    formFlowPanel.setSize("100%", "100%");
                    formFlowPanel.add(new ScrollPanel(formPluginPanel.asWidget()));


                }
            };
            collectionViewerPluginPanel.open(collectionViewerPlugin);


        }
        return flowPanel;
    }

}
