package ru.intertrust.cm.core.gui.impl.client.plugins.navigation;

import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeImages;
import com.google.gwt.user.client.ui.TreeItem;
import ru.intertrust.cm.core.config.model.gui.navigation.ChildLinksConfig;
import ru.intertrust.cm.core.config.model.gui.navigation.LinkConfig;
import ru.intertrust.cm.core.config.model.gui.navigation.LinkPluginDefinition;

import java.util.*;

class NavigationTreeBuilder {

    private TreeImages images;
    private String childToOpenName;
    private LinkConfig linkConfig;
    private TreeItem treeItem;
    private Tree tree;
    private List<SelectionHandler<TreeItem>> handlers = new ArrayList<SelectionHandler<TreeItem>>();

    public NavigationTreeBuilder(LinkConfig linkConfig) {
        this.linkConfig = linkConfig;
    }

    public Tree toTree() {
        treeItem = composeTreeItem(linkConfig.getName(), linkConfig.getDisplayText(), linkConfig.getPluginDefinition());
        addChildTreeItems(treeItem, linkConfig);
        tree = createTreeWidget(images);
        addSelectionEventToTree(tree, handlers);
        tree.addItem(treeItem);
        if (linkConfig.getName().equals(childToOpenName)) {
            treeItem.setSelected(true);
        }
        fireEventsOnChildsToOpen();
        return tree;
    }

    public NavigationTreeBuilder addSelectionHandler(SelectionHandler<TreeItem> handler) {
        handlers.add(handler);
        return this;
    }

    public NavigationTreeBuilder setChildToOpenName(String childToOpenName) {
        this.childToOpenName = childToOpenName;
        return this;
    }

    public NavigationTreeBuilder setImages(TreeImages images) {
        this.images = images;
        return this;
    }

    private TreeItem composeTreeItem(String treeItemName, String displayText, LinkPluginDefinition pluginDefinition) {
        TreeItem firstLevelTreeItem = new TreeItem(new Label(displayText));

        Map<String, Object> treeUserObjects = new HashMap<String, Object>();
        treeUserObjects.put("name", treeItemName);
        if (pluginDefinition != null) {
            treeUserObjects.put("pluginConfig", pluginDefinition.getPluginConfig());
        }
        firstLevelTreeItem.setUserObject(treeUserObjects);
        return firstLevelTreeItem;
    }

    private void fireEventsOnChildsToOpen() {
        Iterator<TreeItem> treeItemIterator = tree.treeItemIterator();
        while (treeItemIterator.hasNext()) {
            TreeItem next = treeItemIterator.next();
            if (next.isSelected()) {
                tree.setSelectedItem(next, true);
            }
        }
    }

    private Tree createTreeWidget(TreeImages images) {
        Tree firstLevelTree = new Tree(images);
        firstLevelTree.setAnimationEnabled(true);
        firstLevelTree.setStyleName("folder-list");
        return firstLevelTree;
    }

    private void addSelectionEventToTree(Tree tree, List<SelectionHandler<TreeItem>> handlers) {
        for (SelectionHandler<TreeItem> treeItemSelectionHandler : handlers) {
            tree.addSelectionHandler(treeItemSelectionHandler);
        }
    }

    private void addChildTreeItems(TreeItem parentTreeItem, LinkConfig parentLinkConfig) {
        if (!parentLinkConfig.getChildLinksConfigList().isEmpty()) {
            ChildLinksConfig next = parentLinkConfig.getChildLinksConfigList().iterator().next();
            List<LinkConfig> linkConfigList = next.getLinkConfigList();
            for (LinkConfig linkConfig : linkConfigList) {
                TreeItem item = composeTreeItem(linkConfig.getName(), linkConfig.getDisplayText(), linkConfig.getPluginDefinition());
                if (linkConfig.getName().equals(childToOpenName)) {
                    treeItem.setState(true);
                }
                addChildTreeItems(item, linkConfig);
                parentTreeItem.addItem(item);
            }
        }
    }

}
