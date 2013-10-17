package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RootPanel;
import ru.intertrust.cm.core.gui.api.client.BaseComponent;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.event.NavigationTreeItemSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.panel.HeaderContainer;
import ru.intertrust.cm.core.gui.impl.client.plugins.navigation.NavigationTreePlugin;
import ru.intertrust.cm.core.gui.impl.client.plugins.navigation.RootLinkSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer.DomainObjectSurferPlugin;
import ru.intertrust.cm.core.gui.model.BusinessUniverseInitialization;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.logging.Logger;

/**
 * @author Denis Mitavskiy
 *         Date: 19.07.13
 *         Time: 16:22
 */
@ComponentName("business.universe")
public class BusinessUniverse extends BaseComponent implements EntryPoint {
    static Logger logger = Logger.getLogger("Business universe");
    private EventBus eventBus = GWT.create(SimpleEventBus.class);

    public void onModuleLoad() {
        AsyncCallback<BusinessUniverseInitialization> callback = new AsyncCallback<BusinessUniverseInitialization>() {
            @Override
            public void onSuccess(BusinessUniverseInitialization result) {
                FlowPanel rootPanel = createRootPanel();
                HorizontalPanel toolPanel = createToolPanel();
                FlowPanel headerPanel = createHeaderPanel();
                HorizontalPanel bodyPanel = new HorizontalPanel();

                rootPanel.add(new HeaderContainer());
                rootPanel.add(toolPanel);

                PluginPanel navigationTreePanel = new PluginPanel(eventBus);
                // todo мы должны просто класть туда панель - пустую, а nav tree plugin уже будет открывать в ней что нужно

                NavigationTreePlugin navigationTreePlugin = ComponentRegistry.instance.get("navigation.tree");
                DomainObjectSurferPlugin domainObjectSurferPlugin = ComponentRegistry.instance.get("domain.object.surfer.plugin");

                PluginPanel domainObjectSurferPanel = new PluginPanel(eventBus);
                domainObjectSurferPanel.open(domainObjectSurferPlugin);

                eventBus.addHandlerToSource(NavigationTreeItemSelectedEvent.TYPE, navigationTreePlugin, domainObjectSurferPlugin);

                navigationTreePanel.open(navigationTreePlugin);

                eventBus.addHandlerToSource(RootLinkSelectedEvent.TYPE, navigationTreePlugin, navigationTreePlugin);
                bodyPanel.add(navigationTreePanel);
                bodyPanel.add(domainObjectSurferPanel);
                rootPanel.add(bodyPanel);

                addResizeHandlerToWindow(headerPanel);
                RootPanel.get().add(rootPanel);
            }

            @Override
            public void onFailure(Throwable caught) {
                Window.Location.assign("/cm-sochi/Login.html" + Window.Location.getQueryString());
            }
        };
        BusinessUniverseServiceAsync.Impl.getInstance().getBusinessUniverseInitialization(callback);
    }

    private FlowPanel createHeaderPanel() {
        FlowPanel headerPanel = new FlowPanel();
        headerPanel.setWidth(Window.getClientWidth() - 360 + "px");
        return headerPanel;
    }

    private HorizontalPanel createToolPanel() {
        HorizontalPanel toolPanel = new HorizontalPanel();
        toolPanel.setStyleName("content-tools");
        toolPanel.setWidth("100%");
        return toolPanel;
    }

    private FlowPanel createRootPanel() {
        FlowPanel rootPanel = new FlowPanel();
        rootPanel.setStyleName("content");
        rootPanel.setSize("100%", "100%");
        return rootPanel;
    }

    public void addResizeHandlerToWindow(final FlowPanel headerPanel) {
        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                int width = Window.getClientWidth() - 360;
                headerPanel.setWidth(width + "px");
            }
        });

    }

    private void addStickerPanel(DockLayoutPanel mainLayoutPanel) {

        PluginPanel stickerPluginPanel = new PluginPanel(eventBus);
        Plugin stickerPlugin = ComponentRegistry.instance.get("sticker.plugin");
        stickerPluginPanel.open(stickerPlugin);

        mainLayoutPanel.addEast(stickerPluginPanel, 20);
    }

    @Override
    public Component createNew() {
        return new BusinessUniverse();
    }

}
