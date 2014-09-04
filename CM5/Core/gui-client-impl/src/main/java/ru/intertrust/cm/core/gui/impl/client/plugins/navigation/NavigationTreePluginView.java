package ru.intertrust.cm.core.gui.impl.client.plugins.navigation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.navigation.ChildLinksConfig;
import ru.intertrust.cm.core.config.gui.navigation.DomainObjectSurferConfig;
import ru.intertrust.cm.core.config.gui.navigation.LinkConfig;
import ru.intertrust.cm.core.config.gui.navigation.PluginConfig;
import ru.intertrust.cm.core.gui.api.client.ActionManager;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.ConfirmCallback;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.event.NavigationTreeItemSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.SideBarResizeEvent;
import ru.intertrust.cm.core.gui.impl.client.panel.SidebarView;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.counters.CollectionCountersRequest;
import ru.intertrust.cm.core.gui.model.counters.CollectionCountersResponse;
import ru.intertrust.cm.core.gui.model.counters.CounterKey;
import ru.intertrust.cm.core.gui.model.plugin.NavigationTreePluginData;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.*;


public class NavigationTreePluginView extends PluginView {

    private final int DURATION = 500;
    private int END_WIDGET_WIDTH = 245;
    private int START_WIDGET_WIDTH = 0;
    private boolean pinButtonClick = false;
    private TreeItem currentSelectedItem;
    private FocusPanel navigationTreesPanel = new FocusPanel();
    private SidebarView sideBarView;
    private Timer mouseOutTimer;
    private List<CounterDecorator> counterDecorators = new ArrayList<>();
    private List<CounterDecorator> rootCounterDecorators = new ArrayList<>();
    private static long lastCountersUpdateTime;
    private HashMap<CounterKey, Id> counterKeys = new HashMap<>(); //<link-name, navBuLinkCollectionObject
    private HTML pinButton;
    private FocusPanel navigationTreeContainer;
    private Timer mouseHoldTimer;

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
        pinButton = new HTML();
        navigationTreesPanel.setStyleName("navigation-dynamic-panel");
        navigationTreeContainer = new FocusPanel();
        navigationTreeContainer.addStyleName("dummy");
        decorateNavigationTreeContainer(navigationTreeContainer);
        sideBarView = new SidebarView();
        GlobalThemesManager.getNavigationTreeStyles().ensureInjected();
        List<LinkConfig> linkConfigList = navigationTreePluginData.getNavigationConfig().getLinkConfigList();
        String selectedRootLinkName = navigationTreePluginData.getRootLinkSelectedName();
        final LinkConfig selectedLinkConfig = buildRootLinks(linkConfigList, selectedRootLinkName, sideBarView);
        final HorizontalPanel horizontalPanel = new HorizontalPanel();

        horizontalPanel.add(sideBarView);
        horizontalPanel.add(navigationTreesPanel);
        navigationTreeContainer.add(horizontalPanel);
        drawNavigationTrees(selectedLinkConfig);
        pinButton.setStyleName("icon pin-normal");
        pinButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final EventBus eventBus = Application.getInstance().getEventBus();
                if (!pinButtonClick) {
                    pinButtonClick = true;
                    pinButton.setStyleName("icon pin-pressed");
                    eventBus.fireEvent(new SideBarResizeEvent(END_WIDGET_WIDTH, LEFT_SECTION_ACTIVE_STYLE, CENTRAL_SECTION_ACTIVE_STYLE));
                    navigationTreesPanel.setStyleName("navigation-dynamic-panel-expanded");
                } else {
                    pinButtonClick = false;
                    pinButton.setStyleName("icon pin-normal");
                    eventBus.fireEvent(new SideBarResizeEvent(START_WIDGET_WIDTH, LEFT_SECTION_STYLE, CENTRAL_SECTION_STYLE));
                    navigationTreesPanel.setStyleName("navigation-dynamic-panel");
                }
                Application.getInstance().getCompactModeState().setNavigationTreePanelExpanded(pinButtonClick);
            }
        });

        Event.addNativePreviewHandler(new Event.NativePreviewHandler() {
            public void onPreviewNativeEvent(final Event.NativePreviewEvent event) {
                final int eventType = event.getTypeInt();
                switch (eventType) {
                    case Event.ONMOUSEOUT:

                        if (!pinButtonClick && event.getNativeEvent().getRelatedEventTarget() == null) {
                            hideTreePanel();
                            event.getNativeEvent().stopPropagation();
                            event.getNativeEvent().preventDefault();
                        }
                        break;

                    default:
                        // not interested in other events
                }
            }
        });
        navigationTreeContainer.addMouseOverHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {

                if (mouseOutTimer != null) {
                    mouseOutTimer.cancel();
                    mouseOutTimer = null;

                }

                navigationTreesPanel.setHeight(Window.getClientHeight() - 100 + "px");
                String leftSectionStyle = pinButtonClick ? LEFT_SECTION_ACTIVE_STYLE : LEFT_SECTION_STYLE;
                String centralSectionStyle = pinButtonClick ? CENTRAL_SECTION_ACTIVE_STYLE : CENTRAL_SECTION_STYLE;
                SideBarResizeEvent sideBarResizeEvent =
                        new SideBarResizeEvent(0, leftSectionStyle, centralSectionStyle);
                Application.getInstance().getEventBus().fireEvent(sideBarResizeEvent);

                mouseHoldTimer = new Timer(){
                    @Override
                    public void run(){
                        final ResizeTreeAnimation resizeTreeAnimation = new ResizeTreeAnimation(END_WIDGET_WIDTH,
                                navigationTreesPanel);
                        resizeTreeAnimation.run(DURATION);
                    }
                };

                mouseHoldTimer.schedule(650);
            }
        });
        navigationTreeContainer.addMouseOutHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                mouseHoldTimer.cancel();
                hideTreePanel();
            }
        });

        if (Application.getInstance().getCollectionCountersUpdatePeriod() > 0) {
            activateCollectionCountersUpdateTimer(Application.getInstance().getCollectionCountersUpdatePeriod());
        }
        return navigationTreeContainer;
    }

    private void hideTreePanel() {
        if (!pinButtonClick) {
            mouseOutTimer = new Timer() {
                @Override
                public void run() {
                    final ResizeTreeAnimation resizeTreeAnimation = new ResizeTreeAnimation(START_WIDGET_WIDTH, navigationTreesPanel);
                    Application.getInstance().getEventBus()
                            .fireEvent(new SideBarResizeEvent(0, LEFT_SECTION_STYLE, CENTRAL_SECTION_STYLE));
                    resizeTreeAnimation.run(DURATION);

                }
            };
            mouseOutTimer.schedule(500);

        }
    }

    private void activateCollectionCountersUpdateTimer(final int collectionCountersUpdatePeriodMillis) {
        final Command collectionsCountersCommand = new Command();
        collectionsCountersCommand.setName("getCounters");
        collectionsCountersCommand.setComponentName("collection_counters_handler");
        final CollectionCountersRequest collectionCountersRequest = new CollectionCountersRequest();
        collectionsCountersCommand.setParameter(collectionCountersRequest);
        updateCounterKeys();
        final Timer timer = new Timer() {
            @Override
            public void run() {
                collectionCountersRequest.setCounterKeys(counterKeys);
                collectionCountersRequest.setLastUpdatedTime(lastCountersUpdateTime);
                BusinessUniverseServiceAsync.Impl.getInstance().executeCommand(collectionsCountersCommand, new AsyncCallback<Dto>() {
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
                            if (counterObject.getCounterKey().getCollectionName() != null) {
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
            if (rootCounterDecorator.getCounterKey().getCollectionName() != null) {
                counterKeys.put(rootCounterDecorator.getCounterKey(), null);
            }
        }
    }

    public String getSelectedLinkName() {
        if (currentSelectedItem != null) {
            final Map<String, Object> userObject = (Map<String, Object>) currentSelectedItem.getUserObject();
            return (String) userObject.get(TREE_ITEM_NAME);
        }
        return null;
    }

    public void clearCurrentSelectedItemValue() {
        if (currentSelectedItem != null) {
            currentSelectedItem.removeStyleName("synchronized");
            currentSelectedItem.getElement().getFirstChildElement().removeClassName("gwt-custom-TreeItem-selected");
            currentSelectedItem.getElement().getFirstChildElement().getStyle().clearDisplay();
            currentSelectedItem = null;
        }
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
        verticalPanel.add(pinButton);
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

    private void decorateNavigationTreeContainer(FocusPanel navigationTreeContainer) {
        navigationTreeContainer.getElement().getStyle().setColor("white");
    }

    private void handleItemSelection(TreeItem tempItem) {
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

        tempItem.addStyleName("tree-item-padding-style");
        tempItem.addStyleName("gwt-custom-white");
        tempItem.getElement().getFirstChildElement().addClassName("gwt-custom-TreeItem-selected");
        Map<String, Object> treeItemUserObject = (Map<String, Object>) tempItem.getUserObject();
        if (treeItemUserObject != null) {
            final String pageTitle = Application.getInstance().getPageName(
                    (String) treeItemUserObject.get(TREE_ITEM_ORIGINAL_TEXT));
            Window.setTitle(pageTitle);
            final PluginConfig pluginConfig =
                    (PluginConfig) treeItemUserObject.get(TREE_ITEM_PLUGIN_CONFIG);
            final String linkName = (String) treeItemUserObject.get(TREE_ITEM_NAME);
            NavigationTreePluginData navigationTreePluginData = plugin.getInitialData();
            Application.getInstance().getEventBus().fireEventFromSource(
                    new NavigationTreeItemSelectedEvent(pluginConfig, linkName, navigationTreePluginData.getNavigationConfig()),
                    plugin);
        }
    }

    private SelectionHandler<TreeItem> createSelectionHandler() {
        return new SelectionHandler<TreeItem>() {

            @Override
            public void onSelection(final SelectionEvent<TreeItem> event) {
                ActionManager actionManager = Application.getInstance().getActionManager();
                final TreeItem selectedItem = event.getSelectedItem();
                actionManager.checkChangesBeforeExecution(new ConfirmCallback() {
                    @Override
                    public void onAffirmative() {
                        handleItemSelection(selectedItem);
                    }

                    @Override
                    public void onCancel() {
                        //nothing to do
                    }
                });
            }
        };
    }

    private LinkConfig buildRootLinks(final List<LinkConfig> linkConfigList,
                                      final String selectedRootLinkName, final SidebarView sideBarView) {
        LinkConfig result = null;
        final ClickHandler clickHandler = new RootNodeButtonClickHandler();
        for (LinkConfig linkConfig : linkConfigList) {
            String name = linkConfig.getName();
            String image = GlobalThemesManager.getResourceFolder() + linkConfig.getImage();
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
                            return ((DomainObjectSurferConfig) config.getPluginDefinition().getPluginConfig()).
                                    getCollectionViewerConfig().getCollectionRefConfig().getName();
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
