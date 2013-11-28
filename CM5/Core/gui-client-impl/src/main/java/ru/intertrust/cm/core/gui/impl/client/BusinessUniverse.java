package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.BaseComponent;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.event.NavigationTreeItemSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.NavigationTreeItemSelectedEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.PluginPanelSizeChangedEvent;
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
    // глобальная шина событий - доступна во всем приложении
    private static EventBus eventBus = Application.getInstance().getEventBus(); //GWT.create(SimpleEventBus.class);
    private PluginPanel centralPluginPanel;
    NavigationTreePlugin navigationTreePlugin;
    PluginPanel navigationTreePanel;
    FlowPanel headerPanel;
    private int centralPluginWidth;
    private int centralPluginHeight;
    private int stickerPluginWidth;
    CurrentUserInfo getUserInfo(BusinessUniverseInitialization result) {
        return new CurrentUserInfo(result.getCurrentLogin(), result.getFirstName(), result.getLastName(), result.geteMail());
    }

    public void onModuleLoad() {
        AsyncCallback<BusinessUniverseInitialization> callback = new AsyncCallback<BusinessUniverseInitialization>() {
            @Override
            public void onSuccess(BusinessUniverseInitialization result) {
                DockLayoutPanel rootPanel = createRootPanel();
                headerPanel = createHeaderPanel();

                navigationTreePanel = new PluginPanel();
                // todo мы должны просто класть туда панель - пустую, а nav tree plugin уже будет открывать в ней что нужно

                navigationTreePlugin = ComponentRegistry.instance.get("navigation.tree");
                // данному плагину устанавливается глобальная шина событий
                navigationTreePlugin.setEventBus(eventBus);

                centralPluginPanel = new PluginPanel();
                centralPluginWidth = Window.getClientWidth() - 250;
                centralPluginHeight = Window.getClientHeight()- 100;
                centralPluginPanel.setVisibleWidth(centralPluginWidth);
                centralPluginPanel.setVisibleHeight(centralPluginHeight);
                eventBus.addHandler(NavigationTreeItemSelectedEvent.TYPE, BusinessUniverse.this);
                navigationTreePanel.setVisibleWidth(250);
                navigationTreePanel.open(navigationTreePlugin);
                rootPanel.addNorth(new HeaderContainer(getUserInfo(result)), 70);

                rootPanel.addWest(navigationTreePanel, 200);
                addStickerPanel(rootPanel);
                rootPanel.add(centralPluginPanel);

                addWindowResizeListener();
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

    private void addWindowResizeListener() {
        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                int centralPanelWidth = event.getWidth() - navigationTreePanel.getVisibleWidth() - stickerPluginWidth;
                int centralPanelHeight = event.getHeight() - 100;
                centralPluginPanel.setVisibleWidth(centralPanelWidth);
                centralPluginPanel.setVisibleHeight(centralPanelHeight);
                eventBus.fireEvent(new PluginPanelSizeChangedEvent());

            }
        });
    }

    private void addStickerPanel(final DockLayoutPanel mainLayoutPanel) {

        final FlowPanel flowPanel = new FlowPanel();
        final ToggleButton toggleBtn = new ToggleButton("sticker");
        final FocusPanel focusPanel = new FocusPanel();
        toggleBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (toggleBtn.getValue())    {
                mainLayoutPanel.setWidgetSize(focusPanel, 300);
                centralPluginWidth -= 300;
                stickerPluginWidth = 30;
                }
                else {
                centralPluginWidth += 300;
                stickerPluginWidth = 300;
                }

                centralPluginPanel.setVisibleWidth(centralPluginWidth);
                eventBus.fireEvent(new PluginPanelSizeChangedEvent());
            }
        });


        focusPanel.addMouseOverHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                mainLayoutPanel.setWidgetSize(focusPanel, 300);
            }
        });

        focusPanel.addMouseOutHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                if (toggleBtn.getValue()) {
                    return;
                }
                mainLayoutPanel.setWidgetSize(focusPanel, 30);

            }
        });

        flowPanel.add(toggleBtn);

        focusPanel.add(flowPanel);
        focusPanel.getElement().getStyle().setBackgroundColor("white");


        mainLayoutPanel.addEast(focusPanel, 30);
        stickerPluginWidth = 30;
    }

    @Override
    public Component createNew() {
        return new BusinessUniverse();
    }

}
