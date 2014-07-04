package ru.intertrust.cm.core.gui.impl.client;

import java.util.logging.Logger;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.web.bindery.event.shared.EventBus;

import ru.intertrust.cm.core.config.SettingsPopupConfig;
import ru.intertrust.cm.core.config.ThemesConfig;
import ru.intertrust.cm.core.config.gui.navigation.PluginConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.BaseComponent;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.event.CentralPluginChildOpeningRequestedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.CentralPluginChildOpeningRequestedHandler;
import ru.intertrust.cm.core.gui.impl.client.event.ExtendedSearchCompleteEvent;
import ru.intertrust.cm.core.gui.impl.client.event.ExtendedSearchCompleteEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.NavigationTreeItemSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.NavigationTreeItemSelectedEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.PluginPanelSizeChangedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.SideBarResizeEvent;
import ru.intertrust.cm.core.gui.impl.client.event.SideBarResizeEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.SideBarResizeEventStyle;
import ru.intertrust.cm.core.gui.impl.client.event.SideBarResizeEventStyleHandler;
import ru.intertrust.cm.core.gui.impl.client.panel.HeaderContainer;
import ru.intertrust.cm.core.gui.impl.client.plugins.navigation.NavigationTreePlugin;
import ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer.DomainObjectSurferPlugin;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.model.BusinessUniverseInitialization;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.DomainObjectSurferPluginData;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

/**
 * @author Denis Mitavskiy
 *         Date: 19.07.13
 *         Time: 16:22
 */
@ComponentName("business.universe")
public class BusinessUniverse extends BaseComponent implements EntryPoint, NavigationTreeItemSelectedEventHandler {
    static Logger logger = Logger.getLogger("Business universe");
    private CentralPluginPanel centralPluginPanel;
    private NavigationTreePlugin navigationTreePlugin;
    private PluginPanel navigationTreePanel;
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

    CurrentUserInfo getUserInfo(BusinessUniverseInitialization result) {
        return new CurrentUserInfo(result.getCurrentLogin(), result.getFirstName(), result.getLastName(), result.geteMail());
    }

    public void onModuleLoad() {

        AsyncCallback<BusinessUniverseInitialization> callback = new AsyncCallback<BusinessUniverseInitialization>() {
            @Override
            public void onSuccess(BusinessUniverseInitialization result) {
                final EventBus glEventBus = Application.getInstance().getEventBus();
                SettingsPopupConfig settingsPopupConfig = result.getSettingsPopupConfig();
                ThemesConfig themesConfig = settingsPopupConfig == null ? null : settingsPopupConfig.getThemesConfig();
                GlobalThemesManager.initTheme(themesConfig);
                header = new AbsolutePanel();
                action = new AbsolutePanel();
                left = new AbsolutePanel();
                centrInner = new AbsolutePanel();
                center = new AbsolutePanel();
                right = new AbsolutePanel();
                footer = new AbsolutePanel();
                AbsolutePanel root = new AbsolutePanel();
                final AbsolutePanel centralDivPanel = new AbsolutePanel();
                centralDivPanel.setStyleName("central-div-panel-test");
                centralDivPanel.getElement().setId(ComponentHelper.DOMAIN_ID);
                centralDivPanel.add(right);

                header.setStyleName("header-section");
                header.getElement().setId(ComponentHelper.HEADER_ID);
                action.setStyleName("action-section");
                left.setStyleName("left-section-active");
                left.getElement().setId(ComponentHelper.LEFT_ID);
                centrInner.setStyleName("centr-inner-section");
                center.setStyleName("center-section");
                center.getElement().setId(ComponentHelper.CENTER_ID);
                right.setStyleName("right-section");
                footer.setStyleName("footer-section");
                root.setStyleName("root-section");
                root.add(header);
                root.add(center);
                root.add(footer);
                center.add(left);

                center.add(centralDivPanel);
                navigationTreePanel = new PluginPanel();
                // todo мы должны просто класть туда панель - пустую, а nav tree plugin уже будет открывать в ней что нужно
                navigationTreePlugin = ComponentRegistry.instance.get("navigation.tree");
                // данному плагину устанавливается глобальная шина событий
                navigationTreePlugin.setEventBus(glEventBus);
                centralPluginPanel = new CentralPluginPanel();
                centralDivPanel.add(centralPluginPanel);
                centralPluginWidth = Window.getClientWidth() - 150;
                centralPluginHeight = Window.getClientHeight();
                centralPluginPanel.setVisibleWidth(centralPluginWidth);
                centralPluginPanel.setVisibleHeight(centralPluginHeight);
                glEventBus.addHandler(CentralPluginChildOpeningRequestedEvent.TYPE, centralPluginPanel);
                glEventBus.addHandler(NavigationTreeItemSelectedEvent.TYPE, BusinessUniverse.this);
                navigationTreePanel.setVisibleWidth(BusinessUniverseConstants.START_SIDEBAR_WIDTH);
                navigationTreePanel.open(navigationTreePlugin);
                String logoImagePath = result.getLogoImagePath();
                CurrentUserInfo currentUserInfo = getUserInfo(result);
                header.add(new HeaderContainer(currentUserInfo, logoImagePath, settingsPopupConfig));
                left.add(navigationTreePanel);

                glEventBus.addHandler(SideBarResizeEvent.TYPE, new SideBarResizeEventHandler() {
                    @Override
                    public void sideBarFixPositionEvent(SideBarResizeEvent event) {

                        left.setStyleName("left-section-active");
                        if (event.getSideBarWidts() == BusinessUniverseConstants.START_SIDEBAR_WIDTH) {
                            centralDivPanel.setStyleName("central-div-panel-test");

                        } else {
                            centralDivPanel.setStyleName("central-div-panel-test-active");

                        }

                        glEventBus.fireEvent(new PluginPanelSizeChangedEvent());

                    }
                });

                glEventBus.addHandler(SideBarResizeEventStyle.TYPE, new SideBarResizeEventStyleHandler() {
                    @Override
                    public void sideBarSetStyleEvent(SideBarResizeEventStyle event) {

                        left.setStyleName(event.getStyleMouseOver());
                        //left.getElement().getStyle().clearWidth();
                    }
                });

                // обработчик окончания расширенного поиска
                glEventBus.addHandler(ExtendedSearchCompleteEvent.TYPE, new ExtendedSearchCompleteEventHandler() {
                    @Override
                    public void onExtendedSearchComplete(ExtendedSearchCompleteEvent event) {
                        extendedSearchComplete(event.getDomainObjectSurferPluginData());
                    }
                });
                addStickerPanel();
                addWindowResizeListener();
                RootLayoutPanel.get().add(root);
                RootLayoutPanel.get().getElement().addClassName("root-layout-panel");
                final Application application = Application.getInstance();
                application.setPageNamePrefix(result.getPageNamePrefix());
                application.setTimeZoneIds(result.getTimeZoneIds());
                application.setHeaderNotificationPeriod(result.getHeaderNotificationPeriod());
                application.setCollectionCountersUpdatePeriod(result.getCollectionCountersUpdatePeriod());
            }

            @Override
            public void onFailure(Throwable caught) {
                Window.Location.assign(GWT.getHostPageBaseURL() + BusinessUniverseConstants.LOGIN_PAGE + Window.Location.getQueryString());
            }
        };
        BusinessUniverseServiceAsync.Impl.getInstance().getBusinessUniverseInitialization(callback);
    }


    @Override
    public void onNavigationTreeItemSelected(NavigationTreeItemSelectedEvent event) {
        Application.getInstance().showLoadingIndicator();
        PluginConfig pluginConfig = event.getPluginConfig();
        String pluginName = pluginConfig.getComponentName();
        Plugin plugin = ComponentRegistry.instance.get(pluginName);
        plugin.setConfig(pluginConfig);

        plugin.setDisplayActionToolBar(true);
        centralPluginPanel.open(plugin);
    }

    // вывод результатов расширенного поиска
    public void extendedSearchComplete(DomainObjectSurferPluginData domainObjectSurferPluginData) {
        DomainObjectSurferPlugin domainObjectSurfer = ComponentRegistry.instance.get("domain.object.surfer.plugin");
        domainObjectSurfer.setConfig(domainObjectSurferPluginData.getDomainObjectSurferConfig());
        domainObjectSurfer.setInitialData(domainObjectSurferPluginData);
        domainObjectSurfer.setDisplayActionToolBar(true);

        centralPluginPanel.open(domainObjectSurfer);
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
                Application.getInstance().getEventBus().fireEvent(new PluginPanelSizeChangedEvent());

            }
        });
    }


    private void addStickerPanel() {

        final FlowPanel flowPanel = new FlowPanel();
        final ToggleButton toggleBtn = new ToggleButton("sticker");
        final FocusPanel focusPanel = new FocusPanel();
        toggleBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (toggleBtn.getValue()) {

                    centralPluginWidth -= 300;
                    stickerPluginWidth = 300;
                } else {
                    centralPluginWidth += 300;
                    stickerPluginWidth = 30;
                }

                centralPluginPanel.setVisibleWidth(centralPluginWidth);
                Application.getInstance().getEventBus().fireEvent(new PluginPanelSizeChangedEvent());
            }
        });

        focusPanel.addMouseOutHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                if (toggleBtn.getValue()) {
                    return;
                }

            }
        });

        flowPanel.add(toggleBtn);
        focusPanel.add(flowPanel);
        focusPanel.getElement().getStyle().setBackgroundColor("white");
        stickerPluginWidth = 30;

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
