package ru.intertrust.cm.core.gui.impl.client.plugins.navigation;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.config.model.gui.navigation.*;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.event.NavigationTreeItemSelectedEvent;
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

    @Override
    protected IsWidget getViewWidget() {
        VerticalPanel rootLinksPanel = new VerticalPanel();
        addStylesToChapterMenu(rootLinksPanel);

        NavigationTreePluginData navigationTreePluginData = plugin.getInitialData();

        NavigationConfig navigationConfig = navigationTreePluginData.getNavigationConfig();
        List<LinkConfig> linkConfigList = navigationConfig.getLinkConfigList();

        String selectedRootLinkName = navigationTreePluginData.getRootLinkSelectedName();
        buildRootLinks(rootLinksPanel, linkConfigList, selectedRootLinkName);
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
        for (LinkConfig firstLevelChildLink : firstLevelChildLinks) {

            List<TreeItem> defaultSelections = new ArrayList<TreeItem>();
            TreeItem firstLevelTreeItem = composeTreeItem(firstLevelChildLink);

            processChildsToOpen(childToOpen, defaultSelections, firstLevelTreeItem);
            buildSubNodes(firstLevelTreeItem, firstLevelChildLink, childToOpen, defaultSelections);

            Tree firstLevelTree = new Tree();

            addSelectionEventToTree(firstLevelTree);

            //generate click events for selected items by default
            for (TreeItem defaultSelection : defaultSelections) {
                firstLevelTree.setSelectedItem(defaultSelection, true);
            }
            firstLevelTree.addItem(firstLevelTreeItem);
            treeContainer.add(firstLevelTree);
        }

        navigationTreePanel.add(treeContainer);
        return navigationTreePanel;
    }

    private void processChildsToOpen(String childToOpen, List<TreeItem> defaultSelections, TreeItem firstLevelTreeItem) {
        Map<String, Object> treeItemUserObjects = (Map<String, Object>) firstLevelTreeItem.getUserObject();
        Object name = treeItemUserObjects.get("name");
        if (name.equals(childToOpen)) {
            defaultSelections.add(firstLevelTreeItem);
        }
    }

    private void addSelectionEventToTree(Tree firstLevelTree) {
        firstLevelTree.addSelectionHandler(new SelectionHandler<TreeItem>() {
            @Override
            public void onSelection(SelectionEvent<TreeItem> event) {
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
                                final String selectedRootLinkName) {
        for (LinkConfig linkConfig : linkConfigList) {
            Image rootLinkImage = decorateRootLinkImage(new Image(linkConfig.getImage()));
            rootLinkImage.setTitle(linkConfig.getName());

            if (linkConfig.getName().equals(selectedRootLinkName)) {
                // add selected style to image
            }
            rootLinkImage.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {

                    Image source = (Image) event.getSource();
                    log.info("root link selected " + source.getTitle());
                    //higlight selection

                    highlightSelection(source);

                    //fire reload plugin event
                    plugin.getEventBus().fireEventFromSource(new RootLinkSelectedEvent(source.getTitle()), plugin);
                }
            });
            rootLinksPanel.add(rootLinkImage);
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


    private void highlightSelection(Image source) {
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
}
