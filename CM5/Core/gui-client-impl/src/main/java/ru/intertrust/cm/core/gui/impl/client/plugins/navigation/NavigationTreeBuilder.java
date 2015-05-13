package ru.intertrust.cm.core.gui.impl.client.plugins.navigation;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.config.gui.navigation.ChildLinksConfig;
import ru.intertrust.cm.core.config.gui.navigation.DomainObjectSurferConfig;
import ru.intertrust.cm.core.config.gui.navigation.LinkConfig;
import ru.intertrust.cm.core.config.gui.navigation.LinkPluginDefinition;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.model.counters.CounterKey;

import java.util.*;

class NavigationTreeBuilder {

    private Tree.Resources resources;
    private String childToOpenName;
    private List<LinkConfig> linkConfigs;
    private String groupName;
    private Tree tree;
    private List<SelectionHandler<TreeItem>> handlers = new ArrayList<>();
    private List<CounterDecorator> counterDecorators = new ArrayList<>();

    public NavigationTreeBuilder(List<LinkConfig> linkConfigs, String groupName) {
        this.linkConfigs = linkConfigs;
        this.groupName = groupName;
    }

    public Tree toTree() {
        tree = createTreeWidget(resources);
        addSelectionEventToTree(tree, handlers);
        if (groupName != null) {
            TreeItem group = composeGroupItem(groupName);
            for (LinkConfig linkConfig : linkConfigs) {
                buildGroup(linkConfig, group);
            }
            fireEventsOnChildrenToOpen();
        } else {
            for (LinkConfig linkConfig : linkConfigs) {
                buildTree(linkConfig);
            }
            fireEventsOnChildrenToOpen();
        }
        return tree;
    }

    private void buildGroup(LinkConfig linkConfig, TreeItem group) {
        tree.addItem(group);
        TreeItem treeItem = composeTreeItem(linkConfig.getName(), linkConfig.getDisplayText(), linkConfig.getPluginDefinition());
        collectCounterDecorators(linkConfig, treeItem);
        addChildrenTreeItems(treeItem, linkConfig);
        group.addItem(treeItem);
        if (linkConfig.getName().equals(childToOpenName)) {
            treeItem.setSelected(true);
        }
    }

    private void collectCounterDecorators(LinkConfig linkConfig, TreeItem treeItem) {
        if (linkConfig.getPluginDefinition() != null) {
            if (linkConfig.getPluginDefinition().getPluginConfig() instanceof DomainObjectSurferConfig) {
                TreeItemCounterDecorator treeItemCounterDecorator = new TreeItemCounterDecorator(treeItem);
                CounterKey counterKey = new CounterKey(linkConfig.getName(), ((DomainObjectSurferConfig) linkConfig.getPluginDefinition()
                        .getPluginConfig()).getCollectionViewerConfig().getCollectionRefConfig().getName());
                treeItemCounterDecorator.setCounterKey(counterKey);
                counterDecorators.add(treeItemCounterDecorator);
            }
        }
    }

    private void buildTree(LinkConfig linkConfig) {
        LinkPluginDefinition pluginDefinition = linkConfig.getPluginDefinition();
        TreeItem treeItem = composeTreeItem(linkConfig.getName(), linkConfig.getDisplayText(), pluginDefinition);
        collectCounterDecorators(linkConfig, treeItem);
        addChildrenTreeItems(treeItem, linkConfig);
        tree.addItem(treeItem);
        if (linkConfig.getName().equals(childToOpenName)) {
            treeItem.setSelected(true);
        }
        treeItem.setStyleName("treeItemWrapper");
    }

    public NavigationTreeBuilder setChildToOpenName(String childToOpenName) {
        this.childToOpenName = childToOpenName;
        return this;
    }

    public NavigationTreeBuilder addSelectionHandler(SelectionHandler<TreeItem> handler) {
        handlers.add(handler);
        return this;
    }

    public NavigationTreeBuilder setResources(Tree.Resources resources) {
        this.resources = resources;
        return this;
    }

    private TreeItem composeGroupItem(String displayText) {
        Label label = new Label();
        label.getElement().getStyle().setFloat(Style.Float.LEFT);

        if (displayText.length() > 18) {
            String cutDisplayText = displayText.substring(0, 18);
            label.setText(cutDisplayText + "...");
            label.setTitle(displayText);
        } else {
            label.setText(displayText);
        }
        label.setStyleName("tree-label");

        final TreeItem treeItem = new TreeItem(label);
        label.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                treeItem.setState(!treeItem.getState());
            }
        });
        return treeItem;
    }

    private TreeItem composeTreeItem(String treeItemName, String displayText, LinkPluginDefinition pluginDefinition) {
        Panel container = new AbsolutePanel();
        container.setStyleName("tree-label");
        container.getElement().getStyle().clearOverflow();
        Label label = new Label();
        if (displayText.length() > 18) {
            String cutDisplayText = displayText.substring(0, 18);
            label.setText(cutDisplayText + "...");
            label.setTitle(displayText);
        } else {
            label.setText(displayText);
        }
        label.setStyleName("treeItemTitle");
        label.getElement().getStyle().setTextDecoration(Style.TextDecoration.UNDERLINE);
        container.add(label);
        TreeItem treeItem = new TreeItem();
        treeItem.setWidget(container);

        Map<String, Object> treeUserObjects = new HashMap<>();
        treeUserObjects.put(BusinessUniverseConstants.TREE_ITEM_NAME, treeItemName);
        treeUserObjects.put(BusinessUniverseConstants.TREE_ITEM_ORIGINAL_TEXT, treeItem.getText());
        treeUserObjects.put(BusinessUniverseConstants.TREE_ITEM_PLUGIN_CONFIG, pluginDefinition.getPluginConfig());
        treeItem.setUserObject(treeUserObjects);

        treeItem.addStyleName("tree-item-padding-style");
        return treeItem;
    }

    private void fireEventsOnChildrenToOpen() {
        Iterator<TreeItem> treeItemIterator = tree.treeItemIterator();
        while (treeItemIterator.hasNext()) {
            TreeItem next = treeItemIterator.next();
            if (next.isSelected()) {
                tree.setSelectedItem(next, true);
            }
        }
    }

    private Tree createTreeWidget(Tree.Resources resources) {
        Tree firstLevelTree = new Tree(resources);
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
                TreeItem item = composeTreeItem(
                        linkConfig.getName(), linkConfig.getDisplayText(), linkConfig.getPluginDefinition());
                collectCounterDecorators(linkConfig, item);
                parentTreeItem.addItem(item);
                addChildrenTreeItems(item, linkConfig);
            }
        }
    }

    public List<CounterDecorator> getCounterDecorators() {
        return counterDecorators;
    }
}
