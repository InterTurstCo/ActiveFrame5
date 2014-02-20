package ru.intertrust.cm.core.gui.impl.client.plugins.navigation;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeImages;
import com.google.gwt.user.client.ui.TreeItem;
import ru.intertrust.cm.core.config.gui.navigation.ChildLinksConfig;
import ru.intertrust.cm.core.config.gui.navigation.LinkConfig;
import ru.intertrust.cm.core.config.gui.navigation.LinkPluginDefinition;

import java.util.*;

class NavigationTreeBuilder {

    private TreeImages images;
    private String childToOpenName;
    private List<LinkConfig> linkConfigs;
    private String groupName;
    private Tree tree;
    private List<SelectionHandler<TreeItem>> handlers = new ArrayList<SelectionHandler<TreeItem>>();

    public NavigationTreeBuilder(List<LinkConfig> linkConfigs, String groupName) {
        this.linkConfigs = linkConfigs;
        this.groupName = groupName;
    }

    public Tree toTree() {
        tree = createTreeWidget(images);
        addSelectionEventToTree(tree, handlers);
        if (groupName != null) {
            TreeItem group = composeGroupItem(groupName);
            for (LinkConfig linkConfig : linkConfigs) {
                buildGroup(linkConfig, group);
            }
            fireEventsOnChildsToOpen();
        } else {
            for (LinkConfig linkConfig : linkConfigs) {
                buildTree(linkConfig);
            }
            fireEventsOnChildsToOpen();
        }
        return tree;
    }

    private void buildGroup(LinkConfig linkConfig, TreeItem group) {
        tree.addItem(group);
        TreeItem treeItem = composeTreeItem(linkConfig.getName(), linkConfig.getDisplayText(), linkConfig.getPluginDefinition());
        addChildrenTreeItems(treeItem, linkConfig);
        group.addItem(treeItem);
        if (linkConfig.getName().equals(childToOpenName)) {
            treeItem.setSelected(true);
        }
    }

    private void buildTree(LinkConfig linkConfig) {
        TreeItem treeItem = composeTreeItem(linkConfig.getName(), linkConfig.getDisplayText(), linkConfig.getPluginDefinition());
        addChildrenTreeItems(treeItem, linkConfig);
        tree.addItem(treeItem);
        if (linkConfig.getName().equals(childToOpenName)) {
            treeItem.setSelected(true);
        }
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
    private TreeItem composeGroupItem(String displayText) {
        Label label = new Label();
        label.getElement().getStyle().setFloat(Style.Float.LEFT);
        label.getElement().getStyle().setMarginRight(120, Style.Unit.PX);

        if (displayText.length() > 18) {
            String cutDisplayText = displayText.substring(0, 18);
            label.setText(cutDisplayText + "...");
            label.setTitle(displayText);
        } else {
            label.setText(displayText);
        }
        label.addStyleName("tree-label");
        TreeItem treeItem = new TreeItem(label);
        treeItem.removeStyleName("gwt-Label");
        return treeItem;
    }

    private TreeItem composeTreeItem(String treeItemName, String displayText, LinkPluginDefinition pluginDefinition) {
        Label label = new Label();
        label.getElement().getStyle().setFloat(Style.Float.LEFT);
        label.getElement().getStyle().setMarginRight(120, Style.Unit.PX);

        if (displayText.length() > 18) {
            String cutDisplayText = displayText.substring(0, 18);
            label.setText(cutDisplayText + "...");
            label.setTitle(displayText);
        } else {
            label.setText(displayText);
        }
        label.addStyleName("tree-label");
        TreeItem treeItem = new TreeItem(label);
        treeItem.removeStyleName("gwt-Label");
        Map<String, Object> treeUserObjects = new HashMap<String, Object>();
        treeUserObjects.put("name", treeItemName);
        if (pluginDefinition != null) {
            treeUserObjects.put("pluginConfig", pluginDefinition.getPluginConfig());
        }
        treeItem.setUserObject(treeUserObjects);
        return treeItem;
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
        firstLevelTree.removeStyleName("folder-list");
        firstLevelTree.addStyleName("group-tree");
        return firstLevelTree;
    }

    private void addSelectionEventToTree(Tree tree, List<SelectionHandler<TreeItem>> handlers) {
        for (SelectionHandler<TreeItem> treeItemSelectionHandler : handlers) {
            tree.addSelectionHandler(treeItemSelectionHandler);
        }
    }

    private void addChildrenTreeItems(TreeItem parentTreeItem, LinkConfig parentLinkConfig) {
        List<ChildLinksConfig> childLinksConfigs = parentLinkConfig.getChildLinksConfigList();
        for (ChildLinksConfig childLinksConfig : childLinksConfigs) {
            List<LinkConfig> linkConfigList = childLinksConfig.getLinkConfigList();
            for (LinkConfig linkConfig : linkConfigList) {
                TreeItem item = composeTreeItem(linkConfig.getName(), linkConfig.getDisplayText(), linkConfig.getPluginDefinition());
                item.removeStyleName("gwt-Label");
                parentTreeItem.addItem(item);
                addChildrenTreeItems(item, linkConfig);

            }
        }
    }

}
