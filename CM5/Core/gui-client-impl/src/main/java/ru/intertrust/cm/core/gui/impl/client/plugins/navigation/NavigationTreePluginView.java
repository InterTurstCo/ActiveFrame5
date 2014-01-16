package ru.intertrust.cm.core.gui.impl.client.plugins.navigation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
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
import java.util.logging.Logger;

public class NavigationTreePluginView extends PluginView {

    private static EventBus eventBus = Application.getInstance().getEventBus();
    private final int DURATION = 500;
    private int END_WIDGET_WIDTH = 380;
    private int START_WIDGET_WIDTH = 130;
    private boolean pinButtonClick = false;

    static Logger log = Logger.getLogger("navigation tree plugin view");
    private FlowPanel navigationTreesPanel = new FlowPanel();
    private SidebarView sideBarView;
    protected NavigationTreePluginView(Plugin plugin) {
        super(plugin);
    }

    TreeItem currentActiveItem;

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
        navigationTreesPanel.getElement().getStyle().setLeft(15, Style.Unit.PX);
        navigationTreesPanel.setWidth("130px");
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

     //   verticalPanel.getElement().getStyle().setPaddingLeft(125, Style.Unit.PX);
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
        navigationTreesPanel.getElement().getStyle().setPaddingTop(20, Style.Unit.PX);

        pinButton.removeStyleName("gwt-HTML");

        pinButton.addStyleName("icon pin-normal");


        pinButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (pinButtonClick == false ){
                    pinButtonClick = true;
                    pinButton.removeStyleName("icon pin-normal");
                    pinButton.addStyleName("icon pin-pressed");
                    eventBus.fireEvent(new SideBarResizeEvent(true, END_WIDGET_WIDTH));
                    eventBus.fireEvent(new SideBarResizeEventStyle(false, "", "left-section-active", ""));


                }  else {
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
                if(!pinButtonClick){
                    eventBus.fireEvent(new SideBarResizeEventStyle(false, "", "left-section", ""));
                }
                 else{
                    eventBus.fireEvent(new SideBarResizeEventStyle(false, "", "left-section-active", ""));
                }
            }
        });

        focusContainer.addMouseOutHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                if (!pinButtonClick){
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
        SelectionHandler<TreeItem> handler = createSelectionHandler();
        TreeImages images = GWT.create(MyTreeImages.class);
        for (LinkConfig firstLevelChildLink : first(firstRootLink.getChildLinksConfigList()).getLinkConfigList()) {
            NavigationTreeBuilder navigationTreeBuilder = new NavigationTreeBuilder(firstLevelChildLink);
            navigationTreeBuilder
                    .addSelectionHandler(handler)
                    .setChildToOpenName(firstRootLink.getChildToOpen())
                    .setImages(images);
            navigationTreesPanel.add(navigationTreeBuilder.toTree());
        }
    }

    private void decorateNavigationTreeContainer(HorizontalPanel navigationTreeContainer) {
        navigationTreeContainer.getElement().getStyle().setColor("white");
    }

    private SelectionHandler<TreeItem> createSelectionHandler() {
        return new SelectionHandler<TreeItem>() {
            @Override
            public void onSelection(SelectionEvent<TreeItem> event) {
                if (currentActiveItem != null && currentActiveItem != event.getSelectedItem()) {
                    currentActiveItem.removeStyleName("synchronized");
                    currentActiveItem.setStyleName("tree-cell");
                    currentActiveItem.getElement().getFirstChildElement().removeClassName("gwt-TreeItem-selected");
                }
                currentActiveItem = event.getSelectedItem();
                event.getSelectedItem().setStyleName("synchronized");
                currentActiveItem.getElement().getFirstChildElement().addClassName("gwt-TreeItem-selected");

                Map<String, Object> treeItemUserObject = (Map<String, Object>) event.getSelectedItem().getUserObject();
                Application.getInstance().getEventBus().fireEventFromSource(
                        new NavigationTreeItemSelectedEvent((PluginConfig) treeItemUserObject.get("pluginConfig")), plugin);
            }
        };
    }

    private void buildRootLinks(final List<LinkConfig> linkConfigList,
                                final String selectedRootLinkName, final SidebarView sideBarView) {
        for (LinkConfig linkConfig : linkConfigList) {
            final RootNodeButton my = new RootNodeButton();
            sideBarView.sidebarItem("images/inbox.png", linkConfig.getDisplayText(), linkConfig.getName(), 3587L, my);
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
            }
            else {
                navigationPanel.getMenuItems().getWidget(i).removeStyleName("selected");
                navigationPanel.getMenuItems().getWidget(i).setStyleName("non-selected");
            }
        }
    }

    void setIndex(int index){

        for (int i = 0; i < sideBarView.getMenuItems().getWidgetCount(); i++) {
            if (i == index) {

                sideBarView.getMenuItems().getWidget(i).setStyleName("selected");
            }
            else {

                sideBarView.getMenuItems().getWidget(i).setStyleName("non-selected");
            }
        }
    }

     void setStyle(RootNodeButton my, List<LinkConfig> linkConfigList){
         if(linkConfigList.size() == 1){
             my.setStyleName("selected");
         }
         else {
             my.setStyleName("non-selected");
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
