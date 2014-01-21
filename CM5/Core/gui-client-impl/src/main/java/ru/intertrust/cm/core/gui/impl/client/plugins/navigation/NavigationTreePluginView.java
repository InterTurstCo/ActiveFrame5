package ru.intertrust.cm.core.gui.impl.client.plugins.navigation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.config.gui.navigation.ChildLinksConfig;
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
import ru.intertrust.cm.core.gui.model.plugin.NavigationTreePluginData;

import java.util.List;
import java.util.Map;

public class NavigationTreePluginView extends PluginView {

    private static EventBus eventBus = Application.getInstance().getEventBus();
    private final int DURATION = 500;
    private int END_WIDGET_WIDTH = 380;
    private int START_WIDGET_WIDTH = 130;
    private boolean pinButtonClick = false;
    private TreeItem previousSelectedItem;
    private FocusPanel navigationTreesPanel = new FocusPanel();
    private SidebarView sideBarView;

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
    protected IsWidget getViewWidget() {
        NavigationTreePluginData navigationTreePluginData = plugin.getInitialData();
        final HTML pinButton = new HTML();
        pinButton.getElement().getStyle().setLeft(150, Style.Unit.PX);
        pinButton.getElement().getStyle().setTop(20, Style.Unit.PX);

        navigationTreesPanel.addStyleName("navigation-dynamic-panel");
        final HorizontalPanel navigationTreeContainer = new HorizontalPanel();
        decorateNavigationTreeContainer(navigationTreeContainer);
        VerticalPanel verticalPanel = new VerticalPanel();
        sideBarView = new SidebarView();
        FocusPanel focusContainer = new FocusPanel();
        SystemTreeStyles.I.styles().ensureInjected();
        VerticalPanel rootLinksPanel = new VerticalPanel();
        decorateRootlinksPanel(rootLinksPanel);
        List<LinkConfig> linkConfigList = navigationTreePluginData.getNavigationConfig().getLinkConfigList();
        String selectedRootLinkName = navigationTreePluginData.getRootLinkSelectedName();
        buildRootLinks(linkConfigList, selectedRootLinkName, sideBarView);
        navigationTreeContainer.add(sideBarView);
        HorizontalPanel navigationTreePanel = new HorizontalPanel();
        HorizontalPanel horizontalPanel = new HorizontalPanel();
        horizontalPanel.add(sideBarView);
        horizontalPanel.add(verticalPanel);
        verticalPanel.add(pinButton);
        verticalPanel.add(navigationTreesPanel);
        navigationTreePanel.getElement().getStyle().setLeft(150, Style.Unit.PX);
        focusContainer.add(horizontalPanel);
        navigationTreeContainer.add(focusContainer);
        LinkConfig firstRootLink = first(linkConfigList);

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

        focusContainer.addMouseOverHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                ResizeTreeAnimation resizeTreeAnimation = new ResizeTreeAnimation(END_WIDGET_WIDTH, navigationTreesPanel);
                resizeTreeAnimation.run(DURATION);
                pinButton.getElement().getStyle().setZIndex(10);
                navigationTreeContainer.getElement().getStyle().setZIndex(9);
                if (!pinButtonClick) {
                    eventBus.fireEvent(new SideBarResizeEventStyle(false, "", "left-section", ""));
                } else {
                    eventBus.fireEvent(new SideBarResizeEventStyle(false, "", "left-section-active", ""));
                }
            }
        });

        focusContainer.addMouseOutHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                if (!pinButtonClick) {
                    ResizeTreeAnimation resizeTreeAnimation = new ResizeTreeAnimation(START_WIDGET_WIDTH, navigationTreesPanel);
                    resizeTreeAnimation.run(DURATION);
                    pinButton.getElement().getStyle().setZIndex(0);
                    navigationTreeContainer.getElement().getStyle().setZIndex(0);
                    eventBus.fireEvent(new SideBarResizeEventStyle(false, "", "left-section", ""));

                }
            }
        });
        setIndex(0);

        return navigationTreeContainer;
    }

    public void repaintNavigationTrees(String rootLinkName) {
        NavigationTreePluginData navigationTreePluginData = plugin.getInitialData();
        List<LinkConfig> linkConfigList = navigationTreePluginData.getNavigationConfig().getLinkConfigList();
        for (LinkConfig linkConfig : linkConfigList) {
            if (linkConfig.getName().equals(rootLinkName)) {
                drawNavigationTrees(linkConfig);
            }
        }
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
            final RootNodeButton my = new RootNodeButton();
            sideBarView.sidebarItem(linkConfig.getImage(), linkConfig.getDisplayText(), linkConfig.getName(), 3587L, my);
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
