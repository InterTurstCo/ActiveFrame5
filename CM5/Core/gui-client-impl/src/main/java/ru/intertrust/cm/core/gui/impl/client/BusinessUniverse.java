package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.event.shared.UmbrellaException;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.SettingsPopupConfig;
import ru.intertrust.cm.core.config.ThemesConfig;
import ru.intertrust.cm.core.config.gui.business.universe.BottomPanelConfig;
import ru.intertrust.cm.core.config.gui.business.universe.RightPanelConfig;
import ru.intertrust.cm.core.config.gui.navigation.*;
import ru.intertrust.cm.core.gui.api.client.*;
import ru.intertrust.cm.core.gui.api.client.history.HistoryException;
import ru.intertrust.cm.core.gui.api.client.history.HistoryManager;
import ru.intertrust.cm.core.gui.impl.client.action.ActionManagerImpl;
import ru.intertrust.cm.core.gui.impl.client.event.*;
import ru.intertrust.cm.core.gui.impl.client.panel.HeaderContainer;
import ru.intertrust.cm.core.gui.impl.client.plugins.navigation.NavigationTreePlugin;
import ru.intertrust.cm.core.gui.impl.client.plugins.navigation.NavigationTreePluginView;
import ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer.DomainObjectSurferPlugin;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.impl.client.util.GuiUtil;
import ru.intertrust.cm.core.gui.impl.client.util.UserSettingsUtil;
import ru.intertrust.cm.core.gui.model.BusinessUniverseInitialization;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.plugin.DomainObjectSurferPluginData;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginState;
import ru.intertrust.cm.core.gui.model.plugin.NavigationTreePluginConfig;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.CENTRAL_SECTION_STYLE;

/**
 * @author Denis Mitavskiy
 *         Date: 19.07.13
 *         Time: 16:22
 */
@ComponentName("business.universe")
public class BusinessUniverse extends BaseComponent implements EntryPoint, NavigationTreeItemSelectedEventHandler {
    private static final int LEFT_PANEL_WIDTH = 375;

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
    private HeaderContainer headerContainer;

    private static Map<String, Element> createdObjects;

    public void onModuleLoad() {
        final AsyncCallback<BusinessUniverseInitialization> callback = new AsyncCallback<BusinessUniverseInitialization>() {
            @Override
            public void onSuccess(BusinessUniverseInitialization result) {
                initialize(result);
            }

            @Override
            public void onFailure(Throwable caught) {
                //do nothing
            }
        };

        BusinessUniverseServiceAsync.Impl.getInstance().getBusinessUniverseInitialization(GuiUtil.getClient(), callback);
    }

    private void initialize(BusinessUniverseInitialization initializationInfo) {
        final Application application = Application.getInstance();
        application.setCurrentLocale(initializationInfo.getCurrentLocale());
        application.setLocalizedResources(initializationInfo.getGlobalLocalizedResources());
        application.getCompactModeState().setRightPanelConfigured(initializationInfo.getRightPanelConfig() != null);
        GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandlerImpl());
        final EventBus glEventBus = Application.getInstance().getEventBus();
        SettingsPopupConfig settingsPopupConfig = initializationInfo.getSettingsPopupConfig();
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
        addBottomPanel(root, initializationInfo.getBottomPanelConfig());
        final AbsolutePanel centralDivPanel = new AbsolutePanel();
        centralDivPanel.setStyleName(CENTRAL_SECTION_STYLE);
        centralDivPanel.getElement().setId(ComponentHelper.DOMAIN_ID);
        centralDivPanel.add(right);

        createdObjects = new HashMap<>();

        header.setStyleName(BusinessUniverseConstants.TOP_SECTION_STYLE);
        header.getElement().getStyle().clearDisplay();
        header.getElement().setId(ComponentHelper.HEADER_ID);
        action.setStyleName("action-section");
        left.setStyleName("left-section-active");
        clearPositionAttribute(left);
        left.getElement().setId(ComponentHelper.LEFT_ID);

        center.addDomHandler(new MouseMoveHandler() {

            @Override
            public void onMouseMove(MouseMoveEvent event) {
                if (event.getClientX() > LEFT_PANEL_WIDTH || event.getClientX() <= 5) {
                    glEventBus.fireEvent(new LeaveLeftPanelEvent());
                }
            }
        }, MouseMoveEvent.getType());

        centrInner.setStyleName("centr-inner-section");
        center.setStyleName("center-section");
        center.getElement().setId(ComponentHelper.CENTER_ID);
        right.setStyleName("right-section");
        footer.getElement().setClassName("footerPanelOff");
        footer.getElement().getStyle().clearPosition();
        root.setStyleName("root-section");
        root.add(header);
        root.add(center);
        root.add(footer);
        center.add(left);

        center.add(centralDivPanel);
        navigationTreePanel = new PluginPanel();
        NavigationTreePluginConfig navigationTreePluginConfig = new NavigationTreePluginConfig();
        if (initializationInfo.getApplicationName() != null) {
            navigationTreePluginConfig.setApplicationName(initializationInfo.getApplicationName());
            History.setApplication(initializationInfo.getApplicationName());
        }
        // todo мы должны просто класть туда панель - пустую, а nav tree plugin уже будет открывать в ней что нужно

        navigationTreePlugin = ComponentRegistry.instance.get("navigation.tree");
        navigationTreePlugin.setConfig(navigationTreePluginConfig);
        // данному плагину устанавливается глобальная шина событий
        navigationTreePlugin.setEventBus(glEventBus);

        Integer sideBarOpeningTime = initializationInfo.getSideBarOpenningTimeConfig();
        navigationTreePlugin.setSideBarOpenningTime(sideBarOpeningTime);
        centralPluginPanel = new CentralPluginPanel();
        centralPluginPanel.setStyle("rightSectionWrapper");
        centralDivPanel.add(centralPluginPanel);
        centralPluginWidth = Window.getClientWidth() - NavigationTreePluginView.FIRST_LEVEL_NAVIGATION_PANEL_WIDTH - stickerPluginWidth;
        centralPluginHeight = Window.getClientHeight();
        centralPluginPanel.setVisibleWidth(centralPluginWidth);
        centralPluginPanel.setVisibleHeight(centralPluginHeight);
        glEventBus.addHandler(CentralPluginChildOpeningRequestedEvent.TYPE, centralPluginPanel);
        glEventBus.addHandler(NavigationTreeItemSelectedEvent.TYPE, this);
        glEventBus.addHandler(LeaveLeftPanelEvent.TYPE, navigationTreePlugin);

        headerContainer = new HeaderContainer(initializationInfo);

        header.add(headerContainer);

        left.add(navigationTreePanel);

        glEventBus.addHandler(SideBarResizeEvent.TYPE, new BusinessUniverseSideBarEventHandler());

        // обработчик окончания расширенного поиска
        glEventBus.addHandler(ExtendedSearchCompleteEvent.TYPE, new ExtendedSearchCompleteEventHandler() {
            @Override
            public void onExtendedSearchComplete(ExtendedSearchCompleteEvent event) {
                extendedSearchComplete(event.getDomainObjectSurferPluginData());
            }
        });
        addRightPanel(initializationInfo.getRightPanelConfig());
        addWindowResizeListener();


        application.setPageNamePrefix(initializationInfo.getPageNamePrefix());
        application.setTimeZoneIds(initializationInfo.getTimeZoneIds());
        application.setHeaderNotificationPeriod(initializationInfo.getHeaderNotificationPeriod());
        application.setCollectionCountersUpdatePeriod(initializationInfo.getCollectionCountersUpdatePeriod());
        application.setActionManager(new ActionManagerImpl(centralPluginPanel));

        RootLayoutPanel.get().add(root);
        RootLayoutPanel.get().getElement().addClassName("root-layout-panel");
        String userUrlNavLink = History.getToken();
        String initialToken = userUrlNavLink.isEmpty() ? initializationInfo.getInitialNavigationLink() : userUrlNavLink;
        if (initialToken != null && !initialToken.isEmpty()) {
            try {
                Application.getInstance().getHistoryManager().setToken(initialToken);
            } catch (HistoryException e) {
                Window.alert(e.getMessage());
            }

        }
        History.addValueChangeHandler(new HistoryValueChangeHandler());
        navigationTreePanel.setVisibleWidth(NavigationTreePluginView.FIRST_LEVEL_NAVIGATION_PANEL_WIDTH);
        navigationTreePanel.open(navigationTreePlugin);
    }

    @Override
    public void onNavigationTreeItemSelected(NavigationTreeItemSelectedEvent event) {
        List<LinkConfig> configs = event.getNavigationConfig().getLinkConfigList();
        String linkName = event.getLinkName();
        LinkConfig selectedLinkConfig = getLinkConfigByName(linkName, configs);
        if (selectedLinkConfig.getOuterTypeConfig() != null) {
            OuterTypeConfig outerLink = selectedLinkConfig.getOuterTypeConfig();
            String actualUrl = null;
            if (outerLink.getUrlTypeConfig().isAbsolute()) {
                actualUrl = outerLink.getUrl();
            } else if (!outerLink.getUrlTypeConfig().isAbsolute()
                    && outerLink.getUrlTypeConfig().getPropertyWithBaseUrl() != null) {
                String baseUrl = outerLink.getUrlTypeConfig().getPropertyWithBaseUrl().equals(BaseUrlPropertyTypeConfig.BASEURLONE.value()) ?
                        event.getNavigationConfig().getBaseUrlOne() : event.getNavigationConfig().getBaseUrlTwo();
                baseUrl = baseUrl.replaceAll("\"", "");
                if (!baseUrl.endsWith("/")) {
                    baseUrl = baseUrl + "/";
                }
                String relativeUrl = (outerLink.getUrl().startsWith("/")) ? outerLink.getUrl().substring(1) : outerLink.getUrl().substring(0);
                actualUrl = baseUrl.concat(relativeUrl);
            }

            if (actualUrl != null) {
                if (outerLink.getOpenPosition().equals(OpenPositionTypeConfig.TAB.value())) {
                    if (createdObjects.get(actualUrl) != null && isOpen(createdObjects.get(actualUrl))) {
                        focus(createdObjects.get(actualUrl));
                    } else {
                        createdObjects.put(actualUrl, newWindow(actualUrl, "_blank", ""));
                    }
                } else if (outerLink.getOpenPosition().equals(OpenPositionTypeConfig.WINDOW.value())) {
                    if (createdObjects.get(actualUrl) != null && isOpen(createdObjects.get(actualUrl))) {
                        focus(createdObjects.get(actualUrl));
                    } else {
                        createdObjects.put(actualUrl, newWindow(actualUrl, "_blank", "enabled"));
                    }
                } else if (outerLink.getOpenPosition().equals(OpenPositionTypeConfig.CURRENT.value())) {
                    createdObjects.put(actualUrl, newWindow(actualUrl, "_self", ""));

                }
            }

        } else {

            final HistoryManager manager = Application.getInstance().getHistoryManager();
            if (manager.hasLink() || manager.getSelectedIds().isEmpty()) {
                Application.getInstance().showLoadingIndicator();
                PluginConfig pluginConfig = event.getPluginConfig();
                String pluginName = pluginConfig.getComponentName();
                final Plugin plugin = ComponentRegistry.instance.get(pluginName);
                plugin.setConfig(pluginConfig);
                manager.setMode(HistoryManager.Mode.WRITE, plugin.getClass().getSimpleName())
                        .setLink(event.getLinkName());
                plugin.setDisplayActionToolBar(true);
                plugin.setNavigationConfig(event.getNavigationConfig());
                navigationTreePlugin.setNavigationConfig(event.getNavigationConfig());
                centralPluginPanel.open(plugin);
            } else {
                History.fireCurrentHistoryState();
            }
            UserSettingsUtil.storeCurrentNavigationLink();
        }
    }

    private static native Element newWindow(String url, String name, String features)/*-{
        var window = $wnd.open(url, name, features);
        return window;
    }-*/;

    private native boolean isOpen(Element win) /*-{
        return !! (win && !win.closed);
    }-*/;

    private native void focus(Element win) /*-{
        win.blur();
        setTimeout(function() { win.focus(); },500);
    }-*/;

    private LinkConfig getLinkConfigByName(String linkName, List<LinkConfig> configs) {
        for (LinkConfig lConfig : configs) {
            if (lConfig.getName().equals(linkName)) {
                return lConfig;
            }
            if (lConfig.getChildLinksConfigList() != null && lConfig.getChildLinksConfigList().size() > 0) {
                for (ChildLinksConfig chConfig : lConfig.getChildLinksConfigList()) {
                    LinkConfig internalConfig = getLinkConfigByName(linkName, chConfig.getLinkConfigList());
                    if (internalConfig != null) {
                        return internalConfig;
                    }
                }
            }
        }
        return null;
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
                centralPluginPanel.asWidget().getElement().getFirstChildElement().addClassName
                        ("central-plugin-panel-table");
                Application.getInstance().getEventBus().fireEvent(new PluginPanelSizeChangedEvent());

            }
        });
    }

    private void addRightPanel(RightPanelConfig rightPanelConfig) {
        if (rightPanelConfig != null) {
            AbsolutePanel panel = new AbsolutePanel();
            panel.getElement().setId(ComponentHelper.RIGHT_ID);
            panel.getElement().getStyle().clearPosition();
            panel.getElement().setClassName("stickerPanelOff");

            center.add(panel);
        }

    }

    private void addBottomPanel(Panel root, BottomPanelConfig bottomPanelConfig) {
        if (bottomPanelConfig != null) {
            Panel footerButton = new AbsolutePanel();
            footerButton.addDomHandler(new FooterPanelHandler(footerButton), ClickEvent.getType());
            footerButton.setStyleName("footerOpenButton");
            footerButton.getElement().getStyle().clearPosition();
            root.add(footerButton);
        }

    }

    @Override
    public Component createNew() {
        return new BusinessUniverse();
    }

    private void handleHistory(String url) {
        Application.getInstance().showLoadingIndicator();
        final HistoryManager manager = Application.getInstance().getHistoryManager();
        if (url != null && !url.isEmpty()) {
            manager.setToken(url);
            if (manager.hasLink()) {
                if (!navigationTreePlugin.restoreHistory()) {
                    final Plugin plugin = centralPluginPanel.getCurrentPlugin();
                    plugin.restoreHistory();
                }
            } else if (!manager.getSelectedIds().isEmpty()) {
                final Id selectedId = manager.getSelectedIds().get(0);
                final FormPluginConfig formPluginConfig = new FormPluginConfig(selectedId);
                final FormPluginState formPluginState = new FormPluginState();
                formPluginState.setInCentralPanel(Application.getInstance().getCompactModeState().isExpanded());
                formPluginConfig.setPluginState(formPluginState);

                final FormPlugin formPlugin = ComponentRegistry.instance.get("form.plugin");
                formPlugin.setConfig(formPluginConfig);
                formPlugin.setDisplayActionToolBar(true);
                formPlugin.setLocalEventBus((EventBus) GWT.create(SimpleEventBus.class));
                manager.setMode(HistoryManager.Mode.WRITE, FormPlugin.class.getSimpleName());
                navigationTreePlugin.clearCurrentSelectedItemValue();
                Window.setTitle("Форма документа");
                centralPluginPanel.open(formPlugin);
            } else {
                throw new HistoryException("Переход по данным '" + url + "' невозможен");
            }
        }
        Application.getInstance().hideLoadingIndicator();
    }

    private class HistoryValueChangeHandler implements ValueChangeHandler<String> {

        @Override
        public void onValueChange(final ValueChangeEvent<String> event) {
            if (!"logout".equals(event.getValue())) {
                final HistoryManager manager = Application.getInstance().getHistoryManager();
                ActionManager actionManager = Application.getInstance().getActionManager();
                actionManager.checkChangesBeforeExecution(new ConfirmCallback() {

                    @Override
                    public void onAffirmative() {
                        handleHistory(event.getValue());
                    }

                    @Override
                    public void onCancel() {
                        manager.applyUrl();
                    }
                });
            }
        }
    }

    private static class CentralPluginPanel extends PluginPanel implements CentralPluginChildOpeningRequestedHandler {
        @Override
        public void openChildPlugin(CentralPluginChildOpeningRequestedEvent event) {
            final Plugin child = event.getOpeningChildPlugin();
            this.openChild(child);
        }
    }

    private static class UncaughtExceptionHandlerImpl implements GWT.UncaughtExceptionHandler {
        @Override
        public void onUncaughtException(Throwable ex) {
            Application.getInstance().hideLoadingIndicator();
            final String message;
            ex = unwrap(ex);
            if (ex instanceof HistoryException) {
                message = "Ошибка поддержки истории: " + ex.getMessage();
            } else if (ex.getCause() instanceof HistoryException) {
                message = "Ошибка поддержки истории: " + ex.getCause().getMessage();
            } else if (ex instanceof GuiException) {
                message = ex.getMessage();
            } else {
                GWT.log("Uncaught exception escaped", ex);
                message = null;
            }
            if (message != null) {
                ApplicationWindow.errorAlert(message);
            }
        }
    }

    private static Throwable unwrap(Throwable e) {
        if (e instanceof UmbrellaException) {
            UmbrellaException ue = (UmbrellaException) e;
            if (ue.getCauses().size() == 1) {
                return unwrap(ue.getCauses().iterator().next());
            }
        }
        return e;
    }

    private class FooterPanelHandler implements ClickHandler {
        private Widget widget;

        public FooterPanelHandler(Widget widget) {
            this.widget = widget;
        }

        @Override
        public void onClick(ClickEvent event) {
            if (footer.getStyleName().equals("footerPanelOff")) {
                widget.getElement().setClassName("footerCloseButton");
                footer.getElement().setClassName("footerPanelOn");
            } else {
                widget.getElement().setClassName("footerOpenButton");
                footer.getElement().setClassName("footerPanelOff");
            }
        }
    }

    private void clearPositionAttribute(Widget element) {
        element.getElement().getStyle().clearPosition();
    }

}
