package ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer;

import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.PluginView;

import java.util.logging.Logger;

public class DomainObjectSurferPluginView extends PluginView {
    SplitLayoutPanel splitterNew = new SplitLayoutPanel(8);
    FlowPanel southPanel = new FlowPanel();
    SimplePanel northPanel = new SimplePanel();
    private DomainObjectSurferPlugin domainObjectSurferPlugin;
    static Logger log = Logger.getLogger("DomainObjectSurfer");

    public DomainObjectSurferPluginView(DomainObjectSurferPlugin domainObjectSurferPlugin) {
        super(domainObjectSurferPlugin);
        this.domainObjectSurferPlugin = domainObjectSurferPlugin;
        splitterSetSize();
        addWindowResizeListeners();
    }

    protected void splitterSetSize(){
        splitterNew.clear();
        int width = Window.getClientWidth() - 230;
        int heigt = Window.getClientHeight() - 98;
        splitterNew.setSize(width + "px", heigt + "px");
        splitterNew.addNorth(northPanel, (Window.getClientHeight()-230) / 2);
        splitterNew.add(southPanel);
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
        northPanel.addStyleName("scrollHeader");

        container.add(splitterNew);

        PluginPanel collectionPluginPanel = new PluginPanel(domainObjectSurferPlugin.getEventBus());
        collectionPluginPanel.open(domainObjectSurferPlugin.getCollectionPlugin());

        final PluginPanel formPluginPanel = new PluginPanel(domainObjectSurferPlugin.getEventBus());
        formPluginPanel.open(domainObjectSurferPlugin.getFormPlugin());

        northPanel.add(collectionPluginPanel.asWidget());

        southPanel.setStyleName("tab-content");
        southPanel.setSize("100%", "100%");
        southPanel.add(new ScrollPanel(formPluginPanel.asWidget()));

        return flowPanel;
    }

}
