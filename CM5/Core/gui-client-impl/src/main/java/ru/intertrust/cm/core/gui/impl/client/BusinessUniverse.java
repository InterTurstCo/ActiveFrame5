package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.navigation.LinkConfig;
import ru.intertrust.cm.core.config.gui.navigation.PluginConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.BaseComponent;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.event.*;
import ru.intertrust.cm.core.gui.impl.client.panel.HeaderContainer;
import ru.intertrust.cm.core.gui.impl.client.plugins.navigation.CounterDecorator;
import ru.intertrust.cm.core.gui.impl.client.plugins.navigation.NavigationTreePlugin;
import ru.intertrust.cm.core.gui.impl.client.plugins.navigation.NavigationTreePluginView;
import ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer.DomainObjectSurferPlugin;
import ru.intertrust.cm.core.gui.model.BusinessUniverseInitialization;
import ru.intertrust.cm.core.gui.model.counters.CollectionCountersRequest;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.counters.CollectionCountersResponse;
import ru.intertrust.cm.core.gui.model.plugin.DomainObjectSurferPluginData;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public static final int START_SIDEBAR_WIDTH = 130;

    private static EventBus eventBus = Application.getInstance().getEventBus();
    private CentralPluginPanel centralPluginPanel;
    NavigationTreePlugin navigationTreePlugin;
    PluginPanel navigationTreePanel;
    private int centralPluginWidth;
    private int centralPluginHeight;
    private int stickerPluginWidth = 30;
    private AbsolutePanel left;
    private AbsolutePanel header;
    private AbsolutePanel centrInner;
    private AbsolutePanel action;
    private AbsolutePanel center;
    private AbsolutePanel right;
    private AbsolutePanel footer;
    private AbsolutePanel root;
    private AbsolutePanel cetralDivPanelTest;




    CurrentUserInfo getUserInfo(BusinessUniverseInitialization result) {
        return new CurrentUserInfo(result.getCurrentLogin(), result.getFirstName(), result.getLastName(), result.geteMail());
    }

    public void onModuleLoad() {
        AsyncCallback<BusinessUniverseInitialization> callback = new AsyncCallback<BusinessUniverseInitialization>() {
            @Override
            public void onSuccess(BusinessUniverseInitialization result) {
                header = new AbsolutePanel();
                action = new AbsolutePanel();
                left = new AbsolutePanel();
                centrInner = new AbsolutePanel();
                center = new AbsolutePanel();
                right = new AbsolutePanel();
                footer = new AbsolutePanel();
                root = new AbsolutePanel();
                cetralDivPanelTest = new AbsolutePanel();


                header.setStyleName("header-section");
                header.getElement().setId(ComponentHelper.HEADER_ID);

                action.setStyleName("action-section");


                left.setStyleName("left-section-active");

                left.getElement().setId(ComponentHelper.LEFT_ID);
               //  left.getElement().setId(ComponentHelper.LEFT_ID);


                centrInner.setStyleName("centr-inner-section");


                center.setStyleName("center-section");
                center.getElement().setId(ComponentHelper.CENTER_ID);


                right.setStyleName("right-section");


                footer.setStyleName("footer-section");


                root.setStyleName("root-section");


                //root.addStyleName("content");

                root.add(header);
                root.add(center);
                root.add(footer);

//                centrInner.add(action);
//                centrInner.add(right);


                center.add(left);


                cetralDivPanelTest.setStyleName("central-div-panel-test");


                center.add(cetralDivPanelTest);

                //cetralDivPanelTest.add(centrInner);
                //center.add(centrInner);

                navigationTreePanel = new PluginPanel();
                // todo мы должны просто класть туда панель - пустую, а nav tree plugin уже будет открывать в ней что нужно

                navigationTreePlugin = ComponentRegistry.instance.get("navigation.tree");
                // данному плагину устанавливается глобальная шина событий
                navigationTreePlugin.setEventBus(eventBus);
                navigationTreePlugin.setBusinessUniverseInitialization(result);

                centralPluginPanel = new CentralPluginPanel();
                //11 - отступ справа
                //centralPluginWidth = Window.getClientWidth() - 130 - 11;
                // header 60 ;
                // action panel 51

                centralPluginWidth = Window.getClientWidth() - 150;
                centralPluginHeight = Window.getClientHeight();
                centralPluginPanel.setVisibleWidth(centralPluginWidth);
                centralPluginPanel.setVisibleHeight(centralPluginHeight);
                eventBus.addHandler(CentralPluginChildOpeningRequestedEvent.TYPE, centralPluginPanel);
                eventBus.addHandler(NavigationTreeItemSelectedEvent.TYPE, BusinessUniverse.this);
                navigationTreePanel.setVisibleWidth(START_SIDEBAR_WIDTH);
                navigationTreePanel.open(navigationTreePlugin);
                String logoImagePath = result.getLogoImagePath();
                CurrentUserInfo currentUserInfo = getUserInfo(result);
                header.add(new HeaderContainer(currentUserInfo, logoImagePath));
                //action.add(centralPluginPanel);
                left.add(navigationTreePanel);

                cetralDivPanelTest.getElement().setId(ComponentHelper.DOMAIN_ID);
                eventBus.addHandler(SideBarResizeEvent.TYPE, new SideBarResizeEventHandler() {
                    @Override
                    public void sideBarFixPositionEvent(SideBarResizeEvent event) {
                        //centralPluginWidth = Window.getClientWidth() - event.getSideBarWidts();
                        //11 отступ справа
                        //centralPluginPanel.setVisibleWidth(centralPluginWidth - 11);

                        //centrInner.getElement().getStyle().setLeft(event.getSideBarWidts(), Style.Unit.PX);
                        //60 - высота хеадера
                        //11 - отступ снизу

                        left.setStyleName("left-section-active");
                        if(event.getSideBarWidts() == START_SIDEBAR_WIDTH){
                            cetralDivPanelTest.setStyleName("central-div-panel-test");

                        } else {
                            cetralDivPanelTest.setStyleName("central-div-panel-test-active");

                        }

                        eventBus.fireEvent(new PluginPanelSizeChangedEvent());

                    }
                });

                eventBus.addHandler(SideBarResizeEventStyle.TYPE, new SideBarResizeEventStyleHandler() {
                    @Override
                    public void sideBarSetStyleEvent(SideBarResizeEventStyle event) {
                        //left.removeStyleName();
                        left.setStyleName(event.getStyleMouseOver());
                        //left.getElement().getStyle().clearWidth();
                    }
                });

                // обработчик окончания расширенного поиска
                eventBus.addHandler(ExtendedSearchCompleteEvent.TYPE, new ExtendedSearchCompleteEventHandler() {
                    @Override
                    public void onExtendedSearchComplete(ExtendedSearchCompleteEvent event) {
                        extendedSearchComplete(event.getDomainObjectSurferPluginData());
                    }
                });

                addStickerPanel(root);
                //cetralDivPanelTest.add(action);
                cetralDivPanelTest.add(right);

                //center.add(centralPluginPanel);

                cetralDivPanelTest.add(centralPluginPanel);
                //centrInner.add(centralPluginPanel);
                addWindowResizeListener();
                RootLayoutPanel.get().add(root);
                RootLayoutPanel.get().getElement().addClassName("root-layout-panel");

                Application.getInstance().setTimeZoneIds(result.getTimeZoneIds());
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


//        PopupPanel popup = new PopupPanel(true);
////        popup.center();
//        popup.setGlassEnabled(true);
//        popup.getElement().getStyle().setWidth(Window.getClientWidth(), Style.Unit.PX);
//        popup.getElement().getStyle().setHeight(Window.getClientHeight(), Style.Unit.PX);
//        popup.getElement().getStyle().setOpacity(70);
//        popup.show();

        PluginConfig pluginConfig = event.getPluginConfig();
        String pluginName = pluginConfig.getComponentName();
        Plugin plugin = ComponentRegistry.instance.get(pluginName);
        plugin.setConfig(pluginConfig);
        plugin.setDisplayActionToolBar(true);
        centralPluginPanel.open(plugin);
        //popup.hide();



    }

    // вывод результатов расширенного поиска
    public void extendedSearchComplete(DomainObjectSurferPluginData domainObjectSurferPluginData) {
        DomainObjectSurferPlugin domainObjectSurfer = ComponentRegistry.instance.get("domain.object.surfer.plugin");
        domainObjectSurfer.setConfig(domainObjectSurferPluginData.getDomainObjectSurferConfig());
        domainObjectSurfer.setInitialData(domainObjectSurferPluginData);
        domainObjectSurfer.setDisplayActionToolBar(true);

        centralPluginPanel.open(domainObjectSurfer);
    }

    private HorizontalPanel createToolPanel() {
        HorizontalPanel toolPanel = new HorizontalPanel();
        toolPanel.setStyleName("content-tools");
        //toolPanel.setWidth("100%");
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
                //вставить запрет ресайза если меньше акшн панели
                //11 - margin для центральной панели
                //int centralPanelWidth = event.getWidth() - navigationTreePanel.getVisibleWidth() - 11;
                //int centralPanelHeight = event.getHeight() - 60 - 11;
                //int centralPanelHeight = event.getHeight() - header.getOffsetHeight() - 11;
                //centralPluginHeight = centralPanelHeight;
                int centralPanelWidth = event.getWidth() - navigationTreePanel.getVisibleWidth() - stickerPluginWidth;
                //int centralPanelHeight = event.getHeight() - 120;
                //60 - header height
                //51 height action panel + margin
//                int centralPanelHeight = event.getHeight() - 9;
                //81 Это высота хеадера (60) + тень хеадера(10) + нижний отступ (11)
                //int centralPanelHeight = event.getHeight() - 81;

                centralPluginPanel.setVisibleWidth(centralPanelWidth);
                //centralPluginPanel.setVisibleHeight(centralPanelHeight);
                centralPluginPanel.asWidget().getElement().getFirstChildElement().addClassName("central-plugin-panel-table");
                //centrInner.getElement().getStyle().setHeight(centralPanelHeight - 11, Style.Unit.PX);
                eventBus.fireEvent(new PluginPanelSizeChangedEvent());
                //centrInner.getElement().getStyle().setHeight(centralPanelHeight - 11, Style.Unit.PX);
                //left.getElement().getStyle().setHeight(centralPanelHeight + 11, Style.Unit.PX);
                //centrInner.getElement().getStyle().setWidth(event.getWidth() - navigationTreePanel.getVisibleWidth() - 11, Style.Unit.PX);
            }
        });
    }

    private void addStickerPanel(final AbsolutePanel mainLayoutPanel) {

        final FlowPanel flowPanel = new FlowPanel();
        final ToggleButton toggleBtn = new ToggleButton("sticker");
        final FocusPanel focusPanel = new FocusPanel();
        toggleBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (toggleBtn.getValue()) {
                    //mainLayoutPanel.setWidgetSize(focusPanel, 300);
                    centralPluginWidth -= 300;
                    stickerPluginWidth = 300;
                } else {
                    //mainLayoutPanel.setWidgetSize(focusPanel, 30);
                    centralPluginWidth += 300;
                    stickerPluginWidth = 30;
                }

                centralPluginPanel.setVisibleWidth(centralPluginWidth);
                eventBus.fireEvent(new PluginPanelSizeChangedEvent());
            }
        });


        focusPanel.addMouseOverHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                //mainLayoutPanel.setWidgetSize(focusPanel, 300);
            }
        });

        focusPanel.addMouseOutHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                if (toggleBtn.getValue()) {
                    return;
                }
                //mainLayoutPanel.setWidgetSize(focusPanel, 30);
                //mainLayoutPanel.setWidgetSize(focusPanel, stickerPluginWidth);

            }
        });

        flowPanel.add(toggleBtn);

        focusPanel.add(flowPanel);
        focusPanel.getElement().getStyle().setBackgroundColor("white");


        // mainLayoutPanel.addEast(focusPanel, 30);
        stickerPluginWidth = 30;
        //mainLayoutPanel.addEast(focusPanel, stickerPluginWidth);

    }

    @Override
    public Component createNew() {
        return new BusinessUniverse();
    }

    private static class CentralPluginPanel extends PluginPanel implements CentralPluginChildOpeningRequestedHandler {
        @Override
        public void openChildPlugin(CentralPluginChildOpeningRequestedEvent event) {
            final Plugin child = event.getOpeningChildPlugin();
            this.openChild(child);
        }
    }

}
