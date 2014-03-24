package ru.intertrust.cm.core.gui.impl.client.plugins.navigation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
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
import ru.intertrust.cm.core.gui.impl.client.panel.RootNodeButton;
import ru.intertrust.cm.core.gui.impl.client.panel.SidebarView;
import ru.intertrust.cm.core.gui.impl.client.panel.SystemTreeStyles;
import ru.intertrust.cm.core.gui.model.BusinessUniverseInitialization;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.counters.CollectionCountersRequest;
import ru.intertrust.cm.core.gui.model.counters.CollectionCountersResponse;
import ru.intertrust.cm.core.gui.model.counters.CounterKey;
import ru.intertrust.cm.core.gui.model.plugin.NavigationTreePluginData;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.*;
import java.util.concurrent.TimeUnit;


public class NavigationTreePluginView extends PluginView {

    private static EventBus eventBus = Application.getInstance().getEventBus();
    private final int DURATION = 500;
    private int END_WIDGET_WIDTH = 349;
    private int START_WIDGET_WIDTH = 130;
    private static final int SIDE_BAR_WIDTH = 109;
    private boolean pinButtonClick = false;
    private TreeItem previousSelectedItem;
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

        //  focusContainer.getElement().getStyle().setBackgroundColor("black");
        focusContainer.addStyleName("focusContainer");
        SystemTreeStyles.I.styles().ensureInjected();
        VerticalPanel rootLinksPanel = new VerticalPanel();
        decorateRootlinksPanel(rootLinksPanel);
        List<LinkConfig> linkConfigList = navigationTreePluginData.getNavigationConfig().getLinkConfigList();
        String selectedRootLinkName = navigationTreePluginData.getRootLinkSelectedName();
        buildRootLinks(linkConfigList, selectedRootLinkName, sideBarView);
        navigationTreeContainer.add(sideBarView);
        final HorizontalPanel navigationTreePanel = new HorizontalPanel();
        HorizontalPanel horizontalPanel = new HorizontalPanel();
        horizontalPanel.add(sideBarView);
        horizontalPanel.add(verticalPanel);
        verticalPanel.add(pinButton);
        verticalPanel.add(navigationTreesPanel);
        navigationTreePanel.getElement().getStyle().setLeft(150, Style.Unit.PX);
        focusContainer.add(horizontalPanel);
        LinkConfig firstRootLink = first(linkConfigList);
        navigationTreeContainer.add(focusContainer);
        drawNavigationTrees(firstRootLink);

        pinButton.getElement().getStyle().setZIndex(111);
        pinButton.getElement().getStyle().setDisplay(Style.Display.BLOCK);

        pinButton.removeStyleName("gwt-HTML");
        pinButton.addStyleName("icon pin-normal");


        pinButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
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
               if (stopMouseMoveEvent){
               if (event.getX() < SIDE_BAR_WIDTH || currentWidth > START_WIDGET_WIDTH){
                   if (mouseOutTimer != null) {
                       mouseOutTimer.cancel();
                   }
                   ResizeTreeAnimation resizeTreeAnimation = new ResizeTreeAnimation(END_WIDGET_WIDTH, navigationTreesPanel);
                   resizeTreeAnimation.run(DURATION);
                   currentWidth = END_WIDGET_WIDTH;
                   pinButton.getElement().getStyle().setZIndex(10);
                   navigationTreeContainer.getElement().getStyle().setZIndex(9);


                   if (!pinButtonClick) {
                       eventBus.fireEvent(new SideBarResizeEventStyle(false, "", "left-section", ""));
                   } else {
                       eventBus.fireEvent(new SideBarResizeEventStyle(false, "", "left-section-active", ""));
                   }
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
                            eventBus.fireEvent(new SideBarResizeEventStyle(false, "", "left-section", ""));

                        }
                    };
                    mouseOutTimer.schedule(500);
                    stopMouseMoveEvent = true;
                }
            }
        });
        setIndex(0);

        NavigationTreePlugin navigationTreePlugin = (NavigationTreePlugin) plugin;
        activateCollectionCountersUpdateTimer(navigationTreePlugin.getBusinessUniverseInitialization());
        return navigationTreeContainer;
    }

    private void activateCollectionCountersUpdateTimer(BusinessUniverseInitialization businessUniverseInitialization) {
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
                            CounterKey identifier = counterObject.getCounterKey();
                            counterObject.decorate(countersValues.get(identifier));
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
        Integer collectionCountersUpdatePeriod = businessUniverseInitialization.getCollectionCountersUpdatePeriod();
        int collectionCountersUpdatePeriodMillis = collectionCountersUpdatePeriod * 1000;
        timer.scheduleRepeating(collectionCountersUpdatePeriodMillis);
    }

    private void updateCounterKeys() {
        counterKeys.clear();
        for (CounterDecorator counterDecorator : counterDecorators) {
            counterKeys.put(counterDecorator.getCounterKey(), null);
        }
        for (CounterDecorator rootCounterDecorator : rootCounterDecorators) {
            counterKeys.put(rootCounterDecorator.getCounterKey(), null);
        }
    }

    public void repaintNavigationTrees(String rootLinkName) {
        NavigationTreePluginData navigationTreePluginData = plugin.getInitialData();
        List<LinkConfig> linkConfigList = navigationTreePluginData.getNavigationConfig().getLinkConfigList();
        for (LinkConfig linkConfig : linkConfigList) {
            if (linkConfig.getName().equals(rootLinkName)) {
                updateCounterTimerContext();
                drawNavigationTrees(linkConfig);
                updateCounterKeys();
            }
        }
    }

    private void updateCounterTimerContext() {
        counterDecorators.clear();
        lastCountersUpdateTime = 0;


    }

    private void drawNavigationTrees(LinkConfig firstRootLink) {
        navigationTreesPanel.clear();
        VerticalPanel verticalPanel = new VerticalPanel();
        SelectionHandler<TreeItem> handler = createSelectionHandler();
        TreeImages images = GWT.create(MyTreeImages.class);
        List<ChildLinksConfig> childLinksConfigs = firstRootLink.getChildLinksConfigList();
        for (ChildLinksConfig childLinksConfig : childLinksConfigs) {
            String groupName = childLinksConfig.getGroupName();
            List<LinkConfig> links = childLinksConfig.getLinkConfigList();
            NavigationTreeBuilder navigationTreeBuilder = new NavigationTreeBuilder(links, groupName);
            navigationTreeBuilder
                    .addSelectionHandler(handler)
                    .setChildToOpenName(firstRootLink.getChildToOpen())
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
                TreeItem tempItem = event.getSelectedItem();
                if (previousSelectedItem != null && previousSelectedItem != tempItem) {
                    previousSelectedItem.removeStyleName("synchronized");
                    previousSelectedItem.getElement().getFirstChildElement().removeClassName("gwt-custom-TreeItem-selected");
                }
                TreeItem parent = tempItem.getParentItem();
                tempItem.getTree().setSelectedItem(parent, false);
                tempItem.removeStyleName("gwt-TreeItem-selected");
                tempItem.removeStyleName("gwt-custom-TreeItem-selected");
                previousSelectedItem = tempItem;
                boolean state = tempItem.getState();
                tempItem.setState(!state, false);
                tempItem.setStyleName("synchronized");
                int childCount = tempItem.getChildCount();
                if (childCount != 0) {
                    for (int i = 0; i < childCount; i++) {
                        tempItem.getChild(i).addStyleName("gwt-custom-white");
                    }
                }
                tempItem.addStyleName("gwt-custom-white");
                tempItem.getElement().getFirstChildElement().addClassName("gwt-custom-TreeItem-selected");
                Map<String, Object> treeItemUserObject = (Map<String, Object>) tempItem.getUserObject();
                if (treeItemUserObject != null) {
                    Application.getInstance().getEventBus().fireEventFromSource(
                            new NavigationTreeItemSelectedEvent((PluginConfig) treeItemUserObject.get("pluginConfig")), plugin);
                }

            }
        };
    }

    private void buildRootLinks(final List<LinkConfig> linkConfigList,
                                final String selectedRootLinkName, final SidebarView sideBarView) {
        for (LinkConfig linkConfig : linkConfigList) {

            long collectionCount = 0;

            String name = linkConfig.getName();
            String image = linkConfig.getImage();
            String displayText = linkConfig.getDisplayText();

            final RootNodeButton my = new RootNodeButton(collectionCount, name, image, displayText);
            fillRootNodeButton(collectionCount, name, image, displayText);

            if (linkConfig.getChildToOpen() != null) {
                CounterRootNodeDecorator counterRootNodeDecorator = new CounterRootNodeDecorator(my);
                String collectionToBeOpened = findCollectionForOpen(linkConfig);
                CounterKey counterKey = new CounterKey(linkConfig.getName(), collectionToBeOpened);
                counterRootNodeDecorator.setCounterKey(counterKey);
                rootCounterDecorators.add(counterRootNodeDecorator);
            }
            sideBarView.getMenuItems().add(my);

            if (linkConfig.getName().equals(selectedRootLinkName)) {
                my.setStyleName("selected");
            }
            my.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    RootNodeButton source = (RootNodeButton) event.getSource();
                    Application.getInstance().getEventBus().fireEventFromSource(new RootLinkSelectedEvent(source.getTitle()), plugin);
                    setStyleForAllNAvigationButton(sideBarView.getWidgetIndex(my), sideBarView);
                }
            });
        }
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

    private RootNodeButton fillRootNodeButton(long collectionCount, String name, String image, String displayText) {
        RootNodeButton my = new RootNodeButton(collectionCount, name, image, displayText);

        return my;
    }

    private void setStyleForAllNAvigationButton(Integer activeMenu, SidebarView navigationPanel) {
        for (int i = 0; i < navigationPanel.getMenuItems().getWidgetCount(); i++) {
            if (activeMenu == i) {
                navigationPanel.getMenuItems().getWidget(i).removeStyleName("non-selected");
                navigationPanel.getMenuItems().getWidget(i).setStyleName("selected");
            } else {
                navigationPanel.getMenuItems().getWidget(i).removeStyleName("selected");
                navigationPanel.getMenuItems().getWidget(i).setStyleName("non-selected");
            }
        }
    }

    void setIndex(int index) {

        for (int i = 0; i < sideBarView.getMenuItems().getWidgetCount(); i++) {
            if (i == index) {

                sideBarView.getMenuItems().getWidget(i).setStyleName("selected");
            } else {

                sideBarView.getMenuItems().getWidget(i).setStyleName("non-selected");
            }
        }
    }


    private void decorateRootlinksPanel(VerticalPanel chapterMenu) {
        chapterMenu.getElement().getStyle().setProperty("backgroundColor", "#EEE");
        chapterMenu.getElement().getStyle().setProperty("marginLeft", "5px");
        chapterMenu.getElement().getStyle().setProperty("marginRight", "5px");
    }

    private String selected(String text, int counter) {
        return "<div class='fl-selected'>" + text
                + "<div class='fl-arrow-left'></div><div class='fl-arrow-right'></div>" + counter(counter) + "</div>";
    }

    private String counter(int num) {
        if (num > 0) {
            return "<div class='fl-counter'>" + num + "</div>";
        } else {
            return "";
        }
    }

    private <T> T first(List<T> linkConfigList) {
        return linkConfigList.iterator().next();
    }

}
