package ru.intertrust.cm.core.gui.impl.client.plugins.navigation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.config.model.gui.navigation.*;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.event.NavigationTreeItemSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.panel.RootNodeButton;
import ru.intertrust.cm.core.gui.impl.client.panel.SidebarView;
import ru.intertrust.cm.core.gui.impl.client.panel.SystemTreeStyles;
import ru.intertrust.cm.core.gui.model.plugin.NavigationTreePluginData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class NavigationTreePluginView extends PluginView {

    static Logger log = Logger.getLogger("navigation tree plugin view");

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
        HorizontalPanel navigationTreeContainer = new HorizontalPanel();
        SidebarView sideBarView = new SidebarView();
        navigationTreeContainer.getElement().getStyle().setColor("white");
        SystemTreeStyles.I.styles().ensureInjected();

        TreeImages images = GWT.create(MyTreeImages.class);

        VerticalPanel rootLinksPanel = new VerticalPanel();
        addStylesToChapterMenu(rootLinksPanel);

        NavigationTreePluginData navigationTreePluginData = plugin.getInitialData();

        NavigationConfig navigationConfig = navigationTreePluginData.getNavigationConfig();
        List<LinkConfig> linkConfigList = navigationConfig.getLinkConfigList();

        String selectedRootLinkName = navigationTreePluginData.getRootLinkSelectedName();
        buildRootLinks(rootLinksPanel, linkConfigList, selectedRootLinkName, sideBarView);
        navigationTreeContainer.add(sideBarView);
        HorizontalPanel navigationTreePanel = new HorizontalPanel();

        navigationTreePanel.add(rootLinksPanel);

        LinkConfig rootLinkConfig = navigationTreePluginData.getRootLinkConfig();

        VerticalPanel treeContainer = new VerticalPanel();
        addStylesToTreeContainer(treeContainer);

        // build children of selected root link
        List<ChildLinksConfig> childLinksConfigList = rootLinkConfig.getChildLinksConfigList();
        ChildLinksConfig firstLevelChildrenContainer = childLinksConfigList.iterator().next();
        List<LinkConfig> firstLevelChildLinks = firstLevelChildrenContainer.getLinkConfigList();

        String childToOpen = navigationTreePluginData.getChildToOpen();
        VerticalPanel navigationTrees = new VerticalPanel();
        for (LinkConfig firstLevelChildLink : firstLevelChildLinks) {

            List<TreeItem> defaultSelections = new ArrayList<TreeItem>();
            TreeItem firstLevelTreeItem = composeTreeItem(firstLevelChildLink);

            processChildsToOpen(childToOpen, defaultSelections, firstLevelTreeItem);
            buildSubNodes(firstLevelTreeItem, firstLevelChildLink, childToOpen, defaultSelections);

            Tree firstLevelTree = new Tree(images);
            firstLevelTree.setAnimationEnabled(true);
            firstLevelTree.setStyleName("folder-list");

            addSelectionEventToTree(firstLevelTree);

            //generate click events for selected items by default
            for (TreeItem defaultSelection : defaultSelections) {
                firstLevelTree.setSelectedItem(defaultSelection, true);
            }
            firstLevelTree.addItem(firstLevelTreeItem);
            navigationTrees.add(firstLevelTree);
        }
        navigationTreeContainer.add(navigationTrees);
        return navigationTreeContainer;

    }

    private void processChildsToOpen(String childToOpen, List<TreeItem> defaultSelections, TreeItem firstLevelTreeItem) {
        Map<String, Object> treeItemUserObjects = (Map<String, Object>) firstLevelTreeItem.getUserObject();
        Object name = treeItemUserObjects.get("name");
        if (name.equals(childToOpen)) {
            defaultSelections.add(firstLevelTreeItem);
        }
    }

    private void addSelectionEventToTree(Tree tree) {
        tree.addSelectionHandler(new SelectionHandler<TreeItem>() {
            @Override
            public void onSelection(SelectionEvent<TreeItem> event) {
                if (currentActiveItem != null && currentActiveItem != event.getSelectedItem()) {
                    currentActiveItem.removeStyleName("synchronized");
                    currentActiveItem.setStyleName("tree-cell");
                }
                currentActiveItem = event.getSelectedItem();
                event.getSelectedItem().setStyleName("synchronized");
                log.info("tree selected, selected item = " + event.getSelectedItem().getUserObject().toString());
                Map<String, Object> treeItemUserObject = (Map<String, Object>) event.getSelectedItem().getUserObject();
                plugin.getEventBus().fireEventFromSource(
                        new NavigationTreeItemSelectedEvent((PluginConfig) treeItemUserObject.get("pluginConfig")), plugin);
            }
        });
    }

    private TreeItem composeTreeItem(LinkConfig firstLevelChildLink) {
        TreeItem firstLevelTreeItem = new TreeItem(new Label(firstLevelChildLink.getDisplayText()));
        Map<String, Object> treeUserObjects = new HashMap<String, Object>();
        treeUserObjects.put("name", firstLevelChildLink.getName());
        LinkPluginDefinition pluginDefinition = firstLevelChildLink.getPluginDefinition();
        if (pluginDefinition != null) {
            treeUserObjects.put("pluginConfig", pluginDefinition.getPluginConfig());
        }
        firstLevelTreeItem.setUserObject(treeUserObjects);
        return firstLevelTreeItem;
    }

    private void buildRootLinks(VerticalPanel rootLinksPanel, List<LinkConfig> linkConfigList,
                                final String selectedRootLinkName, SidebarView sideBarView) {

        for (LinkConfig linkConfig : linkConfigList) {

            RootNodeButton my = new RootNodeButton(linkConfig.getDisplayText());
            sideBarView.sidebarItem("images/inbox.png", linkConfig.getDisplayText(), linkConfig.getName(), 3587L, my);
            sideBarView.getMenuItems().add(my);

            if (linkConfig.getName().equals(selectedRootLinkName)) {
                my.setStyleName("selected");
            }
            my.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    RootNodeButton source = (RootNodeButton) event.getSource();
                    log.info("root link selected " + source.getTitle());
                    //higlight selection
                    highlightSelection(source);
                    //fire reload plugin event
                    plugin.getEventBus().fireEventFromSource(new RootLinkSelectedEvent(source.getTitle()), plugin);
                }
            });
        }
    }

    private void buildSubNodes(TreeItem firstLevelTreeItem, LinkConfig rootLink, String childToOpen,
                               List<TreeItem> defaultSelections) {
        if (!rootLink.getChildLinksConfigList().isEmpty()) {
            ChildLinksConfig next = rootLink.getChildLinksConfigList().iterator().next();
            List<LinkConfig> linkConfigList = next.getLinkConfigList();
            for (LinkConfig linkConfig : linkConfigList) {
                TreeItem item = composeTreeItem(linkConfig);
                processChildsToOpen(childToOpen, defaultSelections, item);
                buildSubNodes(item, linkConfig, childToOpen, defaultSelections);
                firstLevelTreeItem.addItem(item);
            }
        }
    }


    private void highlightSelection(RootNodeButton source) {
        //To change body of created methods use File | Settings | File Templates.
    }


    private void buildRootLinkChildrenNodes(LinkConfig linkConfig) {
        List<ChildLinksConfig> childLinksConfigList = linkConfig.getChildLinksConfigList();
        for (ChildLinksConfig childLinksConfig : childLinksConfigList) {
            childLinksConfig.getLinkConfigList();
        }
    }

    private void addStylesToTreeContainer(VerticalPanel treeContainer) {
        treeContainer.getElement().getStyle().setProperty("backgroundColor", "#EEE");
        treeContainer.getElement().getStyle().setProperty("marginRight", "5px");
    }

    private void addStylesToChapterMenu(VerticalPanel chapterMenu) {
        chapterMenu.getElement().getStyle().setProperty("backgroundColor", "#EEE");
        chapterMenu.getElement().getStyle().setProperty("marginLeft", "5px");
        chapterMenu.getElement().getStyle().setProperty("marginRight", "5px");
    }

    private Image decorateRootLinkImage(Image element) {
        element.getElement().getStyle().setProperty("marginLeft", "5px");// ?
        element.getElement().getStyle().setProperty("borderStyle", "solid");
        element.getElement().getStyle().setProperty("borderWidth", "1");
        element.getElement().getStyle().setProperty("borderColor", "BLACK");
        return element;
    }

    public String selected(String text, int counter) {
        return "<div class='fl-selected'>" + text
                + "<div class='fl-arrow-left'></div><div class='fl-arrow-right'></div>" + counter(counter) + "</div>";
    }

    public String textWrap(String text, int counter) {
        return "<span>" + text + "</span>" + counter(counter);
    }

    public String counter(int num) {
        if (num > 0) {
            return "<div class='fl-counter'>" + num + "</div>";
        } else {
            return "";
        }
    }
}
