package ru.intertrust.cm.core.gui.impl.client.plugins.navigation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeImages;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.web.bindery.event.shared.EventBus;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.navigation.ChildLinksConfig;
import ru.intertrust.cm.core.config.gui.navigation.DomainObjectSurferConfig;
import ru.intertrust.cm.core.config.gui.navigation.LinkConfig;
import ru.intertrust.cm.core.config.gui.navigation.PluginConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.event.NavigationTreeItemSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.SideBarResizeEvent;
import ru.intertrust.cm.core.gui.impl.client.event.SideBarResizeEventStyle;
import ru.intertrust.cm.core.gui.impl.client.panel.SidebarView;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.counters.CollectionCountersRequest;
import ru.intertrust.cm.core.gui.model.counters.CollectionCountersResponse;
import ru.intertrust.cm.core.gui.model.counters.CounterKey;
import ru.intertrust.cm.core.gui.model.plugin.NavigationTreePluginData;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;


public class NavigationTreePluginView extends PluginView {

    private final int DURATION = 500;
    private int END_WIDGET_WIDTH = 349;
    private int START_WIDGET_WIDTH = 130;
    private static final int SIDE_BAR_WIDTH = 109;
    private boolean pinButtonClick = false;
    private TreeItem currentSelectedItem;
    private FocusPanel navigationTreesPanel = new FocusPanel();
    private SidebarView sideBarView;
    private Timer mouseOutTimer;
    private boolean stopMouseMoveEvent = true;
    private int currentWidth = START_WIDGET_WIDTH;
    private List<CounterDecorator> counterDecorators = new ArrayList<>();
    private List<CounterDecorator> rootCounterDecorators = new ArrayList<>();
    private static long lastCountersUpdateTime;
    private HashMap<CounterKey, Id> counterKeys = new HashMap<>(); //<link-name, navBuLinkCollectionObject
    private static Timer timer;


    protected NavigationTreePluginView(Plugin plugin) {
        super(plugin);
    }

    interface MyTreeImages extends TreeImages {
        @Resource("treeOpen.png")
        AbstractImagePrototype treeOpen();

        @Resource("treeClosed.png")
        AbstractImagePrototype treeClosed();
    }

    @Override
    public IsWidget getViewWidget() {
        NavigationTreePluginData navigationTreePluginData = plugin.getInitialData();
        final HTML pinButton = new HTML();
        navigationTreesPanel.addStyleName("navigation-dynamic-panel");
        final HorizontalPanel navigationTreeContainer = new HorizontalPanel();
        decorateNavigationTreeContainer(navigationTreeContainer);
        VerticalPanel verticalPanel = new VerticalPanel();
        sideBarView = new SidebarView();
        final FocusPanel focusContainer = new FocusPanel();
        focusContainer.addStyleName("focusContainer");

        GlobalThemesManager.getNavigationTreeStyles().ensureInjected();
        VerticalPanel rootLinksPanel = new VerticalPanel();
        decorateRootlinksPanel(rootLinksPanel);
        List<LinkConfig> linkConfigList = navigationTreePluginData.getNavigationConfig().getLinkConfigList();
        String selectedRootLinkName = navigationTreePluginData.getRootLinkSelectedName();
        final LinkConfig selectedLinkConfig = buildRootLinks(linkConfigList, selectedRootLinkName, sideBarView);


        HorizontalPanel horizontalPanel = new HorizontalPanel();
        horizontalPanel.add(sideBarView);
        horizontalPanel.add(verticalPanel);
        verticalPanel.add(pinButton);
        verticalPanel.add(navigationTreesPanel);

        focusContainer.add(horizontalPanel);
        navigationTreeContainer.add(focusContainer);
        drawNavigationTrees(selectedLinkConfig);

        pinButton.getElement().getStyle().setZIndex(111);
        pinButton.getElement().getStyle().setDisplay(Style.Display.BLOCK);

        pinButton.removeStyleName("gwt-HTML");
        pinButton.addStyleName("icon pin-normal");


        pinButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final EventBus eventBus = Application.getInstance().getEventBus();
                if (pinButtonClick == false) {
                    pinButtonClick = true;
                    pinButton.removeStyleName("icon pin-normal");
                    pinButton.addStyleName("icon pin-pressed");
                    eventBus.fireEvent(new SideBarResizeEvent(true, END_WIDGET_WIDTH));
                    eventBus.fireEvent(new SideBarResizeEventStyle(false, "", "left-section-active", ""));
                } else {
                    pinButtonClick = false;
                    pinButton.removeStyleName("icon pin-pressed");
                    pinButton.addStyleName("icon pin-normal");
                    eventBus.fireEvent(new SideBarResizeEvent(false, START_WIDGET_WIDTH));
                    eventBus.fireEvent(new SideBarResizeEventStyle(false, "", "left-section", ""));
                }
            }
        });

        focusContainer.addMouseMoveHandler(new MouseMoveHandler() {
            @Override
            public void onMouseMove(MouseMoveEvent event) {
                if (stopMouseMoveEvent) {
                    if (event.getX() < SIDE_BAR_WIDTH || currentWidth > START_WIDGET_WIDTH) {
                        if (mouseOutTimer != null) {
                            mouseOutTimer.cancel();
                        }
                        ResizeTreeAnimation resizeTreeAnimation = new ResizeTreeAnimation(END_WIDGET_WIDTH,
                                navigationTreesPanel);
                        resizeTreeAnimation.run(DURATION);
                        currentWidth = END_WIDGET_WIDTH;
                        pinButton.getElement().getStyle().setZIndex(10);
                        navigationTreeContainer.getElement().getStyle().setZIndex(9);
                        navigationTreesPanel.setStyleName("navigation-dynamic-panel-expanded");
                        navigationTreesPanel.setHeight(Window.getClientHeight() - 100 + "px");
                        final String style = pinButtonClick ? "left-section-active" : "left-section";
                        final SideBarResizeEventStyle sideBarResizeEventStyle =
                                new SideBarResizeEventStyle(false, "", style, "");
                        Application.getInstance().getEventBus().fireEvent(sideBarResizeEventStyle);
                        stopMouseMoveEvent = false;
                    }
                }

            }
        });

        focusContainer.addMouseOutHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                if (!pinButtonClick) {
                    mouseOutTimer = new Timer() {
                        @Override
                        public void run() {
                            ResizeTreeAnimation resizeTreeAnimation = new ResizeTreeAnimation(START_WIDGET_WIDTH, navigationTreesPanel);
                            resizeTreeAnimation.run(DURATION);
                            currentWidth = START_WIDGET_WIDTH;
                            pinButton.getElement().getStyle().setZIndex(0);
                            navigationTreeContainer.getElement().getStyle().setZIndex(0);
                            navigationTreesPanel.setStyleName("navigation-dynamic-panel");
                            Application.getInstance().getEventBus().fireEvent(
                                    new SideBarResizeEventStyle(false, "", "left-section", ""));
                        }
                    };
                    mouseOutTimer.schedule(500);
                    stopMouseMoveEvent = true;
                }
            }
        });
        if (Application.getInstance().getCollectionCountersUpdatePeriod() > 0) {
            activateCollectionCountersUpdateTimer(Application.getInstance().getCollectionCountersUpdatePeriod());
        }
        return navigationTreeContainer;
    }

    private void activateCollectionCountersUpdateTimer(final int collectionCountersUpdatePeriodMillis) {
        final Command collectionsCountersCommand = new Command();
        collectionsCountersCommand.setName("getCounters");
        collectionsCountersCommand.setComponentName("collection_counters_handler");
        final CollectionCountersRequest collectionCountersRequest = new CollectionCountersRequest();
        collectionsCountersCommand.setParameter(collectionCountersRequest);
        updateCounterKeys();
        timer = new Timer() {
            @Override
            public void run() {
                collectionCountersRequest.setCounterKeys(counterKeys);
                collectionCountersRequest.setLastUpdatedTime(lastCountersUpdateTime);
                BusinessUniverseServiceAsync.Impl.executeCommand(collectionsCountersCommand, new AsyncCallback<Dto>() {
                    @Override
                    public void onSuccess(Dto result) {
                        CollectionCountersResponse response = (CollectionCountersResponse) result;
                        lastCountersUpdateTime = response.getLastUpdatedTime();
                        Map<CounterKey, Long> countersValues = response.getCounterValues();
                        for (CounterDecorator counterObject : counterDecorators) {
                            CounterKey identifier = counterObject.getCounterKey();
                            counterObject.decorate(countersValues.get(identifier));
                        }
                        for (CounterDecorator counterObject : rootCounterDecorators) {
                            if (counterObject.getCounterKey().getCollectionName() != null){
                                CounterKey identifier = counterObject.getCounterKey();
                                counterObject.decorate(countersValues.get(identifier));
                            }
                        }
                        counterKeys.clear();
                        counterKeys.putAll(response.getCounterServerObjectIds());
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("error while getting collection counters info");
                    }
                });
            }
        };
        timer.scheduleRepeating(collectionCountersUpdatePeriodMillis);
    }

    private void updateCounterKeys() {
        counterKeys.clear();
        for (CounterDecorator counterDecorator : counterDecorators) {
            counterKeys.put(counterDecorator.getCounterKey(), null);
        }
        for (CounterDecorator rootCounterDecorator : rootCounterDecorators) {
            if (rootCounterDecorator.getCounterKey().getCollectionName() !=null){
                counterKeys.put(rootCounterDecorator.getCounterKey(), null);
            }
        }
    }

    public String getSelectedLinkName() {
        if (currentSelectedItem != null) {
            final Map<String, Object> userObject  = (Map<String, Object>) currentSelectedItem.getUserObject();
            return (String) userObject.get(BusinessUniverseConstants.TREE_ITEM_NAME);
        }
        return null;
    }

    public void clearCurrentSelectedItemValue() {
        currentSelectedItem.removeStyleName("synchronized");
        currentSelectedItem.getElement().getFirstChildElement().removeClassName("gwt-custom-TreeItem-selected");
        currentSelectedItem = null;
    }

    public void repaintNavigationTrees(String rootLinkName, String childToOpenName) {
        NavigationTreePluginData navigationTreePluginData = plugin.getInitialData();
        List<LinkConfig> linkConfigList = navigationTreePluginData.getNavigationConfig().getLinkConfigList();
        for (LinkConfig linkConfig : linkConfigList) {
            if (linkConfig.getName().equals(rootLinkName)) {
                final String originChildToOpenName = linkConfig.getChildToOpen();
                if (childToOpenName != null) {
                    linkConfig.setChildToOpen(childToOpenName);
                }
                updateCounterTimerContext();
                drawNavigationTrees(linkConfig);
                updateCounterKeys();
                linkConfig.setChildToOpen(originChildToOpenName);
                break;
            }
        }
    }

    public void showAsSelectedRootLink(final String rootLinkName) {
        clearSelectedButton();
        for (int index = 0; index < sideBarView.getMenuItems().getWidgetCount(); index++) {
            final RootNodeButton btn = (RootNodeButton) sideBarView.getWidgetMenuItems(index);
            if (rootLinkName.equals(btn.getName())) {
                btn.setSelected(true);
                break;
            }
        }
    }

    private void updateCounterTimerContext() {
        counterDecorators.clear();
        lastCountersUpdateTime = 0;
    }

    private void drawNavigationTrees(LinkConfig selectedRootLinkConfig) {
        navigationTreesPanel.clear();
        VerticalPanel verticalPanel = new VerticalPanel();
        SelectionHandler<TreeItem> handler = createSelectionHandler();
        TreeImages images = GWT.create(MyTreeImages.class);
        List<ChildLinksConfig> childLinksConfigs = selectedRootLinkConfig.getChildLinksConfigList();
        for (ChildLinksConfig childLinksConfig : childLinksConfigs) {
            String groupName = childLinksConfig.getGroupName();
            List<LinkConfig> links = childLinksConfig.getLinkConfigList();
            NavigationTreeBuilder navigationTreeBuilder = new NavigationTreeBuilder(links, groupName);
            navigationTreeBuilder
                    .addSelectionHandler(handler)
                    .setChildToOpenName(selectedRootLinkConfig.getChildToOpen())
                    .setImages(images);
            Tree tree = navigationTreeBuilder.toTree();
            counterDecorators.addAll(navigationTreeBuilder.getCounterDecorators());
            verticalPanel.add(tree);

        }
        navigationTreesPanel.add(verticalPanel);
    }

    private void decorateNavigationTreeContainer(HorizontalPanel navigationTreeContainer) {
        navigationTreeContainer.getElement().getStyle().setColor("white");
    }

    private SelectionHandler<TreeItem> createSelectionHandler() {
        return new SelectionHandler<TreeItem>() {

            @Override
            public void onSelection(SelectionEvent<TreeItem> event) {
                if(!Application.getInstance().getActionManager().isExecuteIfWorkplaceDirty()) {
                    return;
                }
                TreeItem tempItem = event.getSelectedItem();
                if (currentSelectedItem != null && currentSelectedItem != tempItem) {
                    currentSelectedItem.removeStyleName("synchronized");
                    currentSelectedItem.getElement().getFirstChildElement().removeClassName("gwt-custom-TreeItem-selected");
                }
                TreeItem parent = tempItem.getParentItem();
                tempItem.getTree().setSelectedItem(parent, false);

                currentSelectedItem = tempItem;
                boolean state = tempItem.getState();
                tempItem.setState(!state, false);
                tempItem.setStyleName("synchronized");
                int childCount = tempItem.getChildCount();
                if (childCount != 0) {
                    for (int i = 0; i < childCount; i++) {
                        tempItem.getChild(i).addStyleName("gwt-custom-white");
                    }
                }
                tempItem.getElement().getStyle().clearPaddingLeft();
                tempItem.getElement().getStyle().clearPadding();
                tempItem.addStyleName("tree-item-padding-style");
                tempItem.addStyleName("gwt-custom-white");
                tempItem.getElement().getFirstChildElement().addClassName("gwt-custom-TreeItem-selected");
                Map<String, Object> treeItemUserObject = (Map<String, Object>) tempItem.getUserObject();
                if (treeItemUserObject != null) {
                    final String pageTitle = Application.getInstance().getPageName(
                            (String) treeItemUserObject.get(BusinessUniverseConstants.TREE_ITEM_ORIGINAL_TEXT));
                    Window.setTitle(pageTitle);
                    final PluginConfig pluginConfig =
                            (PluginConfig) treeItemUserObject.get(BusinessUniverseConstants.TREE_ITEM_PLUGIN_CONFIG);
                    final String linkName = (String) treeItemUserObject.get(BusinessUniverseConstants.TREE_ITEM_NAME);
                    Application.getInstance().getEventBus().fireEventFromSource(
                            new NavigationTreeItemSelectedEvent(pluginConfig, linkName),
                            plugin);
                }
            }
        };
    }

    private LinkConfig buildRootLinks(final List<LinkConfig> linkConfigList,
                                final String selectedRootLinkName, final SidebarView sideBarView) {
        LinkConfig result = null;
        final ClickHandler clickHandler = new RootNodeButtonClickHandler();
        for (LinkConfig linkConfig : linkConfigList) {
            String name = linkConfig.getName();
            String image = linkConfig.getImage();
            String displayText = linkConfig.getDisplayText();

            final RootNodeButton my = new RootNodeButton(null, name, image, displayText);
            if (linkConfig.getChildToOpen() != null) {
                CounterRootNodeDecorator counterRootNodeDecorator = new CounterRootNodeDecorator(my);
                String collectionToBeOpened = findCollectionForOpen(linkConfig);
                CounterKey counterKey = new CounterKey(linkConfig.getName(), collectionToBeOpened);
                counterRootNodeDecorator.setCounterKey(counterKey);
                rootCounterDecorators.add(counterRootNodeDecorator);
            }
            sideBarView.getMenuItems().add(my);
            if (selectedRootLinkName == null) {
                if (result == null) {
                    result = linkConfig;
                    my.setSelected(true);
                }
            } else {
                if (linkConfig.getName().equals(selectedRootLinkName)) {
                    result = linkConfig;
                    my.setSelected(true);
                }
            }
            my.addClickHandler(clickHandler);
        }
        sideBarView.correctContentStyles();
        return result;
    }

    private String findCollectionForOpen(LinkConfig linkConfig) {
        String childToOpen = linkConfig.getChildToOpen();
        for (ChildLinksConfig childLinksConfig : linkConfig.getChildLinksConfigList()) {
            for (LinkConfig config : childLinksConfig.getLinkConfigList()) {
                if (config.getName().equals(childToOpen)) {
                    if (config.getPluginDefinition() != null) {
                        if (config.getPluginDefinition().getPluginConfig() instanceof DomainObjectSurferConfig) {
                            return ((DomainObjectSurferConfig) config.getPluginDefinition().getPluginConfig()).getCollectionViewerConfig().getCollectionRefConfig().getName();
                        }
                    }
                }
            }
        }
        return null;
    }

    private void clearSelectedButton() {
        for (int index = 0; index < sideBarView.getMenuItems().getWidgetCount(); index++) {
            final RootNodeButton rnButton = (RootNodeButton) sideBarView.getMenuItems().getWidget(index);
            rnButton.setSelected(false);
        }
    }

    private void decorateRootlinksPanel(VerticalPanel chapterMenu) {
        chapterMenu.getElement().getStyle().setProperty("backgroundColor", "#EEE");
        chapterMenu.getElement().getStyle().setProperty("marginLeft", "5px");
        chapterMenu.getElement().getStyle().setProperty("marginRight", "5px");
    }

    private class RootNodeButtonClickHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
            RootNodeButton source = (RootNodeButton) event.getSource();
            Application.getInstance().getEventBus().fireEventFromSource(new RootLinkSelectedEvent(source
                    .getTitle()), plugin);
            clearSelectedButton();
            source.setSelected(true);
        }
    }
}
