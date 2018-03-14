package ru.intertrust.cm.core.gui.impl.client.plugins.navigation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.navigation.*;
import ru.intertrust.cm.core.gui.api.client.ActionManager;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.CompactModeState;
import ru.intertrust.cm.core.gui.api.client.ConfirmCallback;
import ru.intertrust.cm.core.gui.impl.client.BusinessUniverse;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.event.NavigationTreeItemSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.SideBarResizeEvent;
import ru.intertrust.cm.core.gui.impl.client.panel.SidebarView;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;
import ru.intertrust.cm.core.gui.impl.client.themes.light.LightThemeBundle;
import ru.intertrust.cm.core.gui.impl.client.util.UserSettingsUtil;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.counters.CollectionCountersRequest;
import ru.intertrust.cm.core.gui.model.counters.CollectionCountersResponse;
import ru.intertrust.cm.core.gui.model.counters.CounterKey;
import ru.intertrust.cm.core.gui.model.plugin.NavigationTreePluginData;
import ru.intertrust.cm.core.gui.model.util.StringUtil;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.*;

import static java.awt.SystemColor.window;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.*;


public class NavigationTreePluginView extends PluginView {
    private static final String BUTTON_PINNED_STYLE = "icon pin-pressed";
    private static final String BUTTON_UNPINNED_STYLE = "icon pin-normal";
    public static final int FIRST_LEVEL_NAVIGATION_PANEL_WIDTH = 134;
    private static final int FIRST_LEVEL_NAVIGATION_PANEL_WIDTH_MARGIN = 17;
    private static final int LEVELS_INTERSECTION = 20;
    private static final int LINK_TEXT_MARGIN = 55;
    private static final double ONE_CHAR_WIDTH = 6.6D;
    private int DURATION = 500;
    private static final int DEFAULT_SECOND_LEVEL_NAVIGATION_PANEL_WIDTH = 220;
    private static final int TOP_MARGIN = 80;
    public static final long SESSION_TIMEOUT = 1800;
    private int START_WIDGET_WIDTH = 0;
    private boolean pinButtonPressed = false;
    private TreeItem currentSelectedItem;
    private FocusPanel navigationTreesPanel = new FocusPanel();
    private SidebarView sideBarView;
    private List<CounterDecorator> counterDecorators = new ArrayList<>();
    private List<CounterDecorator> rootCounterDecorators = new ArrayList<>();
    private static long lastCountersUpdateTime;
    private HashMap<CounterKey, Id> counterKeys = new HashMap<>(); //<link-name, navBuLinkCollectionObject
    private HTML pinButton;
    private FocusPanel navigationTreeContainer;
    private Timer mouseHoldTimer;
    private Integer navigationTreeOpeningTime;
    private boolean animatedTreePanelIsOpened; //is opened or is opening
    private ResizeTreeAnimation resizeTreeAnimation;
    private String endWidgetWidthInPx;
    private Boolean autoOpen = true;
    private String selectedRootNode;

    protected NavigationTreePluginView(Plugin plugin) {
        super(plugin);
    }


    interface MyTreeImages extends Tree.Resources {
        @Source("treeOpen.png")
        ImageResource treeOpen();

        @Source("treeClosed.png")
        ImageResource treeClosed();
    }

    @Override
    public IsWidget getViewWidget() {
        NavigationTreePluginData navigationTreePluginData = plugin.getInitialData();
        NavigationConfig navigationConfig = navigationTreePluginData.getNavigationConfig();
        if (navigationConfig.getSideBarOpeningTime() != null) {
            navigationTreeOpeningTime = navigationConfig.getSideBarOpeningTime();
        } else {
            navigationTreeOpeningTime = navigationTreePluginData.getSideBarOpenningTime();
        }
        int secondLevelNavigationPanelWidth = navigationConfig.getSecondLevelPanelWidth() == null
                ? DEFAULT_SECOND_LEVEL_NAVIGATION_PANEL_WIDTH
                : StringUtil.getIntValue(navigationConfig.getSecondLevelPanelWidth());
        CompactModeState compactModeState = Application.getInstance().getCompactModeState();
        compactModeState.setSecondLevelNavigationPanelWidth(secondLevelNavigationPanelWidth);
        compactModeState.setFirstLevelNavigationPanelWidth(FIRST_LEVEL_NAVIGATION_PANEL_WIDTH);
        boolean minimalMargin = NavigationPanelSecondLevelMarginSize.MINIMAL.equals(navigationConfig.getMarginSize());
        int levelsIntersection = minimalMargin ? LEVELS_INTERSECTION : 0;
        compactModeState.setLevelsIntersection(levelsIntersection);
        endWidgetWidthInPx = secondLevelNavigationPanelWidth + "px";
        pinButton = new HTML();
        navigationTreesPanel.setStyleName("navigation-dynamic-panel");
        navigationTreeContainer = new FocusPanel();
        if (minimalMargin) {
            navigationTreeContainer.addStyleName("minimalLeftMargin");

        }
        decorateNavigationTreeContainer(navigationTreeContainer);
        sideBarView = new SidebarView();
        GlobalThemesManager.getNavigationTreeStyles().ensureInjected();
        List<LinkConfig> linkConfigList = navigationConfig.getLinkConfigList();
        String selectedRootLinkName = navigationTreePluginData.getRootLinkSelectedName();
        boolean isDynamic = navigationTreePluginData.hasSecondLevelNavigationPanel() &&
                navigationConfig.isUnpinEnabled();
        final LinkConfig selectedLinkConfig = buildRootLinks(linkConfigList, selectedRootLinkName, sideBarView,
                isDynamic);
        final HorizontalPanel horizontalPanel = new HorizontalPanel();

        horizontalPanel.add(sideBarView);
        horizontalPanel.add(navigationTreesPanel);
        navigationTreeContainer.add(horizontalPanel);
        drawNavigationTrees(selectedLinkConfig, navigationTreePluginData.getChildToOpen());

        pinButton.setStyleName(BUTTON_UNPINNED_STYLE);
        pinButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onPinButtonClick();
            }
        });
        changeSecondLevelNavigationPanelHeight();
        navigationTreeContainer.addMouseOutHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                onLeavingLeftPanel();
            }
        });


        if (Application.getInstance().getCollectionCountersUpdatePeriod() > 0) {
            activateCollectionCountersUpdateTimer(Application.getInstance().getCollectionCountersUpdatePeriod());
        }
        if (navigationTreePluginData.isPinned()) {
            changeState();
        }

        Event.addNativePreviewHandler(new Event.NativePreviewHandler() {
            public void onPreviewNativeEvent(final Event.NativePreviewEvent event) {
                final int eventType = event.getTypeInt();
                switch (eventType) {

                    case Event.ONCLICK:
                        final int eventX = event.getNativeEvent().getClientX();
                        if (animatedTreePanelIsOpened && (eventX > navigationTreeContainer.getOffsetWidth()) && !pinButtonPressed && !autoOpen) {
                            animatedTreePanelIsOpened = false;
                            hideTreePanel();
                        }
                        break;
                    default:
                        // not interested in other events
                }
            }
        });

        return navigationTreeContainer;
    }

    public void changeSecondLevelNavigationPanelHeight() {
        navigationTreesPanel.setHeight(Window.getClientHeight() - TOP_MARGIN + "px");
    }

    private void changeState() {
        final Timer timer = new Timer() {
            @Override
            public void run() {
                onPinButtonClick();
                this.cancel();
            }
        };
        timer.schedule(navigationTreeOpeningTime);
    }

    private void onPinButtonClick() {
        final EventBus eventBus = Application.getInstance().getEventBus();
        if (!pinButtonPressed) {
            pinButtonPressed = true;
            pinButton.setStyleName(BUTTON_PINNED_STYLE);
            // in case tree panel is in process of hiding, reopen it
            if (mouseHoldTimer != null) {
                mouseHoldTimer.cancel();
                mouseHoldTimer = null;
            }
            if (resizeTreeAnimation != null) {
                resizeTreeAnimation.cancel();
            }
            navigationTreesPanel.getElement().getStyle().setDisplay(Style.Display.BLOCK);
            navigationTreesPanel.setWidth(endWidgetWidthInPx);
            navigationTreesPanel.setStyleName("navigation-dynamic-panel-expanded");
            eventBus.fireEvent(new SideBarResizeEvent(Application.getInstance().getCompactModeState().getSecondLevelNavigationPanelWidth(),
                    LEFT_SECTION_ACTIVE_STYLE, CENTRAL_SECTION_ACTIVE_STYLE));

        } else {
            pinButtonPressed = false;
            pinButton.setStyleName(BUTTON_UNPINNED_STYLE);
            eventBus.fireEvent(new SideBarResizeEvent(START_WIDGET_WIDTH, LEFT_SECTION_STYLE, CENTRAL_SECTION_STYLE));
            navigationTreesPanel.setStyleName("navigation-dynamic-panel");
            animatedTreePanelIsOpened = true;
        }
        Application.getInstance().getCompactModeState().setNavigationTreePanelExpanded(pinButtonPressed);
        UserSettingsUtil.storeNavigationPanelState(pinButtonPressed);
    }

    void onLeavingLeftPanel() {
        if (animatedTreePanelIsOpened && !pinButtonPressed && autoOpen) {
            if (mouseHoldTimer != null) {
                mouseHoldTimer.cancel();
                mouseHoldTimer = null;
            }
            animatedTreePanelIsOpened = false;
            hideTreePanel();
        }
    }

    private void openTreePanel() {
        mouseHoldTimer = new Timer() {
            @Override
            public void run() {

                String leftSectionStyle = pinButtonPressed ? LEFT_SECTION_ACTIVE_STYLE : LEFT_SECTION_STYLE;
                String centralSectionStyle = pinButtonPressed ? CENTRAL_SECTION_ACTIVE_STYLE : CENTRAL_SECTION_STYLE;
                SideBarResizeEvent sideBarResizeEvent =
                        new SideBarResizeEvent(START_WIDGET_WIDTH, leftSectionStyle, centralSectionStyle);
                Application.getInstance().getEventBus().fireEvent(sideBarResizeEvent);
                resizeTreeAnimation = new ResizeTreeAnimation(Application.getInstance().getCompactModeState().getSecondLevelNavigationPanelWidth(),
                        navigationTreesPanel);
                resizeTreeAnimation.run(DURATION);

                navigationTreesPanel.getElement().getStyle().setDisplay(Style.Display.BLOCK);
            }
        };
        mouseHoldTimer.schedule(navigationTreeOpeningTime);
    }

    private void hideTreePanel() {
        mouseHoldTimer = new Timer() {
            @Override
            public void run() {
                resizeTreeAnimation = new ResizeTreeAnimation(START_WIDGET_WIDTH, navigationTreesPanel);
                Application.getInstance().getEventBus()
                        .fireEvent(new SideBarResizeEvent(START_WIDGET_WIDTH, LEFT_SECTION_STYLE, CENTRAL_SECTION_STYLE));
                resizeTreeAnimation.run(DURATION);

            }
        };
        mouseHoldTimer.schedule(navigationTreeOpeningTime);
    }

    public static native double getLastActivity() /*-{
        if (typeof($wnd.dateObj) !== "undefined") {
            return $wnd.dateObj;
        } else {
            return 0;
        }
    }-*/;

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
                Date now = new Date();
                if ((now.getTime() - getLastActivity() < SESSION_TIMEOUT) || getLastActivity()==0 ) {
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

    @Deprecated //use repaintNavigationTrees(LinkConfig linkConfig, String childToOpenName) instead
    public void repaintNavigationTrees(String rootLinkName, String childToOpenName) {
        NavigationTreePluginData navigationTreePluginData = plugin.getInitialData();
        List<LinkConfig> linkConfigList = navigationTreePluginData.getNavigationConfig().getLinkConfigList();
        for (LinkConfig linkConfig : linkConfigList) {
            if (linkConfig.getName().equals(rootLinkName)) {
                updateCounterTimerContext();
                drawNavigationTrees(linkConfig, childToOpenName);
                updateCounterKeys();
                break;
            }
        }
    }

    public void repaintNavigationTrees(LinkConfig linkConfig, String childToOpenName) {
        if (linkConfig == null) {
            return;
        }
        updateCounterTimerContext();
        drawNavigationTrees(linkConfig, childToOpenName);
        updateCounterKeys();
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

    private void drawNavigationTrees(LinkConfig selectedRootLinkConfig, final String childToOpen) {
        navigationTreesPanel.clear();

        VerticalPanel verticalPanel = new VerticalPanel();
        NavigationTreePluginData data = plugin.getInitialData();
        if (data.getNavigationConfig().isUnpinEnabled()) {
            verticalPanel.add(pinButton);
        }
        SelectionHandler<TreeItem> handler = createSelectionHandler();
        Tree.Resources resources = GWT.create(MyTreeImages.class);
        List<ChildLinksConfig> childLinksConfigs = selectedRootLinkConfig.getChildLinksConfigList();
        int linkTextMargin = NavigationPanelSecondLevelMarginSize.DEFAULT.equals(data.getNavigationConfig().getMarginSize())
                ? LINK_TEXT_MARGIN + FIRST_LEVEL_NAVIGATION_PANEL_WIDTH_MARGIN : LINK_TEXT_MARGIN;
        int visibleCharsLength = (int) ((Application.getInstance().getCompactModeState().getSecondLevelNavigationPanelWidth()
                - linkTextMargin) / ONE_CHAR_WIDTH);
        for (ChildLinksConfig childLinksConfig : childLinksConfigs) {
            NavigationTreeBuilder navigationTreeBuilder = new NavigationTreeBuilder(childLinksConfig);
            navigationTreeBuilder
                    .addSelectionHandler(handler)
                    .setChildToOpenName(childToOpen == null ? selectedRootLinkConfig.getChildToOpen() : childToOpen)
                    .setResources(resources)
                    .setVisibleCharsLength(visibleCharsLength)
                    .setBaseAutoCut(data.getNavigationConfig().isTextAutoCut());
            Tree tree = navigationTreeBuilder.toTree();
            counterDecorators.addAll(navigationTreeBuilder.getCounterDecorators());
            verticalPanel.add(tree);

        }
        navigationTreesPanel.add(verticalPanel);


    }

    private native void setScrollLeft(Element elem) /*-{
        elem.scrollLeft = 0;
    }-*/;

    private void decorateNavigationTreeContainer(FocusPanel navigationTreeContainer) {
        navigationTreeContainer.getElement().getStyle().setColor("white");
    }

    private void handleItemSelection(TreeItem tempItem) {
        if (currentSelectedItem != null && currentSelectedItem != tempItem) {
            currentSelectedItem.removeStyleName("synchronized");
            currentSelectedItem.getElement().getFirstChildElement().removeClassName("gwt-custom-TreeItem-selected");
        }
        currentSelectedItem = tempItem;

        TreeItem parent = tempItem.getParentItem();
        if (parent != null) { //CMFIVE-1836 - Кнопки Ctrl/Alt/Shift сбрасывают положение в дереве навигации
            tempItem.getTree().setSelectedItem(parent, false);
            // Here we change state only for non-top-level items. Because of workaround for CMFIVE-1836,
            // top-level tempItem remains selected, and setState(false) in this case causes
            // firing subsequent selection event, which resets the state back to true. For top-level items we
            // change the state separately in ClickHandler.
            boolean state = tempItem.getState();
            tempItem.setState(!state, false);
        }
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
            /**
             * scroll is moved in the middle  after selection for a some reason,
             * so we set it to the left position
             */
            setScrollLeft(navigationTreesPanel.getElement());
        }
    }

    private SelectionHandler<TreeItem> createSelectionHandler() {
        return new SelectionHandler<TreeItem>() {

            @Override
            public void onSelection(final SelectionEvent<TreeItem> event) {
                ActionManager actionManager = Application.getInstance().getActionManager();
                final TreeItem selectedItem = event.getSelectedItem();
                if (isNoNewPluginOpening(selectedItem)) {
                    handleItemSelection(selectedItem);
                } else {
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
            }
        };
    }

    private boolean isNoNewPluginOpening(TreeItem selectedItem) {
        Map<String, Object> treeItemUserObject = (Map<String, Object>) selectedItem.getUserObject();
        return treeItemUserObject == null || treeItemUserObject.get(TREE_ITEM_PLUGIN_CONFIG) == null;

    }

    private LinkConfig buildRootLinks(final List<LinkConfig> linkConfigList, final String selectedRootLinkName, final
    SidebarView sideBarView, boolean isDynamic) {
        LinkConfig result = null;
        final ClickHandler clickHandler = new RootNodeButtonClickHandler();
        NavigationTreePluginData data = plugin.getInitialData();
        boolean baseAutoCut = data.getNavigationConfig().isTextAutoCut();
        for (LinkConfig linkConfig : linkConfigList) {

            final RootNodeButton nodeButton = new RootNodeButton(linkConfig, baseAutoCut);
            if (linkConfig.getChildToOpen() != null) {
                CounterRootNodeDecorator counterRootNodeDecorator = new CounterRootNodeDecorator(nodeButton);
                String collectionToBeOpened = findCollectionForOpen(linkConfig);
                CounterKey counterKey = new CounterKey(linkConfig.getName(), collectionToBeOpened);
                counterRootNodeDecorator.setCounterKey(counterKey);
                rootCounterDecorators.add(counterRootNodeDecorator);
            }
            sideBarView.getMenuItems().add(nodeButton);
            if (isDynamic) {
                navigationTreeContainer.addDomHandler(new MouseMoveHandler() {
                    @Override
                    public void onMouseMove(MouseMoveEvent mouseMoveEvent) {

                        if (GlobalThemesManager.getCurrentTheme() instanceof LightThemeBundle) {
                            autoOpen = Boolean.parseBoolean(
                                    ((LightThemeBundle) GlobalThemesManager.getCurrentTheme()).lightCss().autoOpenNavigationPanel()
                            );
                            DURATION = 0;
                        }
                        if (!animatedTreePanelIsOpened && (mouseMoveEvent.getClientX() < START_SIDEBAR_WIDTH) && !pinButtonPressed && autoOpen) {
                            animatedTreePanelIsOpened = true;
                            openTreePanel();
                        }
                    }
                }, MouseMoveEvent.getType());
            }

            if (selectedRootLinkName == null) {
                if (result == null) {
                    result = linkConfig;
                    nodeButton.setSelected(true);
                }
            } else {
                if (linkConfig.getName().equals(selectedRootLinkName)) {
                    result = linkConfig;
                    nodeButton.setSelected(true);
                }
            }
            nodeButton.addClickHandler(clickHandler);
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

            if (!animatedTreePanelIsOpened && !pinButtonPressed && !autoOpen) {
                animatedTreePanelIsOpened = true;
                openTreePanel();
            } else if (animatedTreePanelIsOpened && !pinButtonPressed && !autoOpen) {
                if (selectedRootNode.equals(((RootNodeButton) event.getSource()).getName())) {
                    animatedTreePanelIsOpened = false;
                    hideTreePanel();
                }
            }

            RootNodeButton source = (RootNodeButton) event.getSource();

            /*
             if(autoOpen || selectedRootNode==null ||
                    (!autoOpen && animatedTreePanelIsOpened && (selectedRootNode!=null && !selectedRootNode.equals(source.getName()))))
             */

            if (autoOpen || selectedRootNode == null ||
                    (!autoOpen && (selectedRootNode != null && !selectedRootNode.equals(source.getName())))) {
                Application.getInstance().getEventBus().fireEventFromSource(new RootLinkSelectedEvent(source
                        .getName()), plugin);
                clearSelectedButton();
                source.setSelected(true);
                selectedRootNode = source.getName();
            }
        }
    }


}
