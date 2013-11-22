package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import ru.intertrust.cm.core.gui.api.client.BaseComponent;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.event.*;
import ru.intertrust.cm.core.gui.impl.client.panel.HeaderContainer;
import ru.intertrust.cm.core.gui.impl.client.plugins.navigation.NavigationTreePlugin;
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
public class BusinessUniverse extends BaseComponent implements EntryPoint, NavigationTreeItemSelectedEventHandler {
    static Logger logger = Logger.getLogger("Business universe");
    final private EventBus eventBus = GWT.create(SimpleEventBus.class);
    private PluginPanel centralPluginPanel;
    NavigationTreePlugin navigationTreePlugin;
    PluginPanel navigationTreePanel;
    FlowPanel headerPanel;

    CurrentUserInfo getUserInfo(BusinessUniverseInitialization result){
        return new CurrentUserInfo(result.getCurrentLogin(), result.getFirstName(), result.getLastName() , result.geteMail());
    }

    public void onModuleLoad() {
        AsyncCallback<BusinessUniverseInitialization> callback = new AsyncCallback<BusinessUniverseInitialization>() {
            @Override
            public void onSuccess(BusinessUniverseInitialization result) {
                DockLayoutPanel rootPanel = createRootPanel();
                headerPanel = createHeaderPanel();

                navigationTreePanel = new PluginPanel(eventBus);
                // todo мы должны просто класть туда панель - пустую, а nav tree plugin уже будет открывать в ней что нужно

                navigationTreePlugin = ComponentRegistry.instance.get("navigation.tree");

                centralPluginPanel = new PluginPanel(eventBus);

                eventBus.addHandler(NavigationTreeItemSelectedEvent.TYPE, BusinessUniverse.this);

                navigationTreePanel.open(navigationTreePlugin);
                rootPanel.addNorth(new HeaderContainer(getUserInfo(result)), 70);

                rootPanel.addWest(navigationTreePanel, 200);
                rootPanel.add(centralPluginPanel);

                RootLayoutPanel.get().add(rootPanel);

            }

            @Override
            public void onFailure(Throwable caught) {
                Window.Location.assign("/cm-sochi/Login.html" + Window.Location.getQueryString());
            }
        };
        BusinessUniverseServiceAsync.Impl.getInstance().getBusinessUniverseInitialization(callback);
    }

    @Override
    public void onNavigationTreeItemSelected(NavigationTreeItemSelectedEvent event) {
        centralPluginPanel.closeCurrentPlugin();
        final DomainObjectSurferPlugin domainObjectSurfer = ComponentRegistry.instance.get("domain.object.surfer.plugin");
        domainObjectSurfer.setConfig(event.getPluginConfig());
        domainObjectSurfer.setDisplayActionToolBar(true);

        centralPluginPanel.open(domainObjectSurfer);
        float windowWidth = Window.getClientWidth();
        float windowHeight = Window.getClientHeight();
        final float widthRatio = centralPluginPanel.asWidget().getOffsetWidth() / windowWidth ;
        final float heightRatio = centralPluginPanel.asWidget().getOffsetHeight() / windowHeight;
        domainObjectSurfer.addViewCreatedListener(new PluginViewCreatedEventListener() {
            @Override
            public void onViewCreation(PluginViewCreatedEvent source) {

                domainObjectSurfer.getEventBus().fireEvent(new PluginViewCreatedSubEvent(widthRatio, heightRatio));
            }
        });
    }

    private FlowPanel createHeaderPanel() {
        FlowPanel headerPanel = new FlowPanel();
        headerPanel.setWidth("100%");
        return headerPanel;
    }

    private HorizontalPanel createToolPanel() {
        HorizontalPanel toolPanel = new HorizontalPanel();
        toolPanel.setStyleName("content-tools");
        toolPanel.setWidth("100%");
        return toolPanel;
    }

    private DockLayoutPanel createRootPanel() {
        DockLayoutPanel rootPanel = new DockLayoutPanel(Style.Unit.PX);
        rootPanel.setStyleName("content");

        return rootPanel;
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
