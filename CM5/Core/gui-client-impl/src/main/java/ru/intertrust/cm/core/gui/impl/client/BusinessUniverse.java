package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.gui.api.client.BaseComponent;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.event.NavigationTreeItemSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.panel.HeaderContainer;
import ru.intertrust.cm.core.gui.impl.client.plugins.navigation.RootLinkSelectedEvent;
import ru.intertrust.cm.core.gui.model.BusinessUniverseInitialization;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
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

    FlowPanel rootPanel = new FlowPanel();
    HorizontalPanel toolPanel = new HorizontalPanel();
    HorizontalPanel bodyPanel = new HorizontalPanel();

    HeaderContainer header = new HeaderContainer();
    FlowPanel contentAction = new FlowPanel();
    SplitLayoutPanel splitterNew = new SplitLayoutPanel(8);
    FlowPanel headerPanel = new FlowPanel();

    public void onModuleLoad() {
        AsyncCallback<BusinessUniverseInitialization> callback = new AsyncCallback<BusinessUniverseInitialization>() {
            @Override
            public void onSuccess(BusinessUniverseInitialization result) {

                rootPanel.add(header.createHeader());
                rootPanel.add(toolPanel);

                PluginPanel navigationTreePanel = new PluginPanel(eventBus);
                Plugin navigationTreePlugin = ComponentRegistry.instance.get("navigation.tree");
                Plugin domainObjectSurferPlugin = ComponentRegistry.instance.get("domain.object.surfer.plugin");
                // todo мы должны просто класть туда панель - пустую, а nav tree plugin уже будет открывать в ней что нужно
                PluginPanel domainObjectSurferPanel = new PluginPanel(eventBus);
                domainObjectSurferPanel.open(domainObjectSurferPlugin);

                domainObjectSurferPlugin.registerEventHandlingFromExternalSource(NavigationTreeItemSelectedEvent.TYPE,
                        navigationTreePlugin, domainObjectSurferPlugin);
                /*eventBus.addHandler(CollectionRowSelectedEvent.TYPE,
                        (DomainObjectSurferPlugin) domainObjectSurferPlugin);*/
                navigationTreePanel.open(navigationTreePlugin);

                navigationTreePlugin.registerEventHandlingFromExternalSource(RootLinkSelectedEvent.TYPE, navigationTreePlugin, navigationTreePlugin);
                bodyPanel.add(navigationTreePanel);
                bodyPanel.add(domainObjectSurferPanel);
                rootPanel.add(bodyPanel);

                RootPanel.get().add(rootPanel);
                show();
            }

            @Override
            public void onFailure(Throwable caught) {
                Window.Location.assign("/cm-sochi/Login.html" + Window.Location.getQueryString());
            }
        };
        BusinessUniverseServiceAsync.Impl.getInstance().getBusinessUniverseInitialization(callback);
    }

    private void drawSplitter() {
        int width = Window.getClientWidth() - 360;
        int heigt = Window.getClientHeight() - 130;
        splitterNew.setSize(width + "px", heigt + "px");
        contentAction.setSize(width + "px", heigt + "px");
        headerPanel.setWidth(width + "px");
    }

    public void splitterPanelResize() {

        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                drawSplitter();
            }
        });

    }


    private void show() {
        toolPanel.setStyleName("content-tools");
        toolPanel.setWidth("100%");
        rootPanel.setStyleName("content");
        rootPanel.setSize("100%", "100%");
        contentAction.setStyleName("centerTopBottomDividerRoot");
        splitterPanelResize();
        drawSplitter();
    }

    private void addStickerPanel(DockLayoutPanel mainLayoutPanel) {

        PluginPanel stickerPluginPanel = new PluginPanel(eventBus);
        Plugin stickerPlugin = ComponentRegistry.instance.get("sticker.plugin");
        stickerPluginPanel.open(stickerPlugin);

        mainLayoutPanel.addEast(stickerPluginPanel, 20);
    }

    private void prepareActionPanel(VerticalPanel grid) {
        HorizontalPanel actionPanel = buildFakeActionPanel();
        grid.add(actionPanel);
    }

    private HorizontalPanel buildFakeActionPanel() {
        HorizontalPanel actionPanel = new HorizontalPanel();
        Image imgRefresh = new Image("css/icons/ico-reload.gif");
        Label labelRefresh = new Label("Обновить");
        Image imgCreate = new Image("css/icons/icon-create.png");
        Label labelCreate = new Label("Создать");
        Label labelOther = new Label("Другое");
        Label labelSelect = new Label("Отметить");
        Image imgPlane = new Image("css/icons/icon-datepicker2.png");
        Label labelPlane = new Label("Запланировать");
        Label labelsignate = new Label("Назначить");
        actionPanel.add(imgRefresh);
        actionPanel.add(labelRefresh);
        actionPanel.add(imgCreate);
        actionPanel.add(labelCreate);
        actionPanel.add(labelOther);
        actionPanel.add(labelSelect);
        actionPanel.add(imgPlane);
        actionPanel.add(labelPlane);
        actionPanel.add(labelsignate);
        return actionPanel;
    }

    private VerticalPanel prepareHeaderPanel(PluginPanel searchPluginPanel) {

        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.getElement().getStyle().setProperty("backgroundColor", "#EEE");
        verticalPanel.getElement().getStyle().setProperty("margin", "5px");

        HorizontalPanel headerPanel = new HorizontalPanel();
        Image logo = createLogo();

        Label profileUserLink = new Label("User");
        Image imageUserLink = new Image("images/user.png");
        imageUserLink.getElement().getStyle().setProperty("marginRight", "30px");

        Image settings = new Image("css/images/settings.png");

        Image help = new Image("css/images/help.png");
        help.getElement().getStyle().setProperty("marginRight", "30px");

        Label exit = new Label("Выход");

        headerPanel.add(logo);
        headerPanel.add(searchPluginPanel);
        headerPanel.add(profileUserLink);
        headerPanel.add(imageUserLink);
        headerPanel.add(settings);
        headerPanel.add(help);
        headerPanel.add(exit);

        headerPanel.setHeight("34px");

        verticalPanel.add(headerPanel);

        prepareMenuPanel(verticalPanel);
        verticalPanel.setWidth("100%");
        return verticalPanel;
    }

    private Image createLogo() {
        Image logo = new Image("images/cm-logo.png");
        logo.getElement().getStyle().setProperty("marginRight", "30px");
        return logo;
    }

    private void addFormPanel(DockLayoutPanel mainLayoutPanel) {
        PluginPanel formPluginPanel = new PluginPanel(eventBus);
        formPluginPanel.setSize("500px", "300px");
        Plugin plugin = ComponentRegistry.instance.get("form.plugin");
        FormPluginConfig config = new FormPluginConfig("country");
        plugin.setConfig(config);
        formPluginPanel.open(plugin);
        SimpleLayoutPanel layoutPanel = new SimpleLayoutPanel();
        layoutPanel.setSize("500px", "300px");
        layoutPanel.add(formPluginPanel);
        mainLayoutPanel.addSouth(layoutPanel, 50);
    }

    private void addNavigationTree(DockLayoutPanel dockLayoutPanel, final Plugin navigationTreePlugin, final Plugin domainObjectSurfer) {
        PluginPanel navigationTreePanel = new PluginPanel(eventBus) {
            @Override
            public void beforePluginOpening() {

            }
        };
        navigationTreePanel.open(navigationTreePlugin);
        navigationTreePlugin.registerEventHandlingFromExternalSource(RootLinkSelectedEvent.TYPE, navigationTreePlugin, navigationTreePlugin);
        dockLayoutPanel.addWest(navigationTreePanel, 15);
    }

    private void addCollection(DockLayoutPanel dockLayoutPanel, final Plugin collectionPlugin, final Plugin domainObjectSurfer) {
        PluginPanel collectionPanel = new PluginPanel(eventBus);
        //collectionPanel.open(collectionPlugin);
       dockLayoutPanel.addSouth(collectionPanel, 15);
    }

    @Override
    public Component createNew() {
        return new BusinessUniverse();
    }

    private void prepareMenuPanel(VerticalPanel cmjHeader) {
        DockLayoutPanel menuPanel = new DockLayoutPanel(Style.Unit.EM);

        Button createBtn = new Button("Создать");
        Image treeBtnShow = new Image("css/images/icon-folderlist.png");

        Command cmd = new Command() {
            public void execute() {
                //
            }
        };

        MenuBar allMenu = new MenuBar(true);
        allMenu.addItem("Все", cmd);
        allMenu.addItem("Все", cmd);
        allMenu.addItem("Все", cmd);

        MenuBar notreadMenu = new MenuBar(true);
        notreadMenu.addItem("Непрочтенные", cmd);
        notreadMenu.addItem("Непрочтенные", cmd);
        notreadMenu.addItem("Непрочтенные", cmd);

        MenuBar recMenu = new MenuBar(true);
        recMenu.addItem("Корзина", cmd);
        recMenu.addItem("Корзина", cmd);
        recMenu.addItem("Корзина", cmd);

        MenuBar menu = new MenuBar();
        menu.addItem("Все", allMenu);
        menu.addItem("Непрочтенные", notreadMenu);
        menu.addItem("Корзина", recMenu);

        Image positionForSplitPanelH = new Image("css/images/btn-verthor2.png");
        Image positionForSplitPanelV = new Image("css/images/btn-verthor2.png");
        Image showStickerPanel = new Image("css/images/icon-folderlist.png");

        menuPanel.addWest(createBtn, 15);
        menuPanel.addWest(treeBtnShow, 4);
        menuPanel.addEast(showStickerPanel, 4);

        menuPanel.addEast(positionForSplitPanelH, 2.3);
        menuPanel.addEast(positionForSplitPanelV, 2.3);

        menuPanel.add(menu);
        menuPanel.setHeight("30px");
        cmjHeader.add(menuPanel);
    }
}
