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
    private ChildLinksConfig childLinksConfig;
    private Tree tree;
    private List<SelectionHandler<TreeItem>> handlers = new ArrayList<>();
    private List<CounterDecorator> counterDecorators = new ArrayList<>();
    private int visibleCharsLength;
    private boolean baseAutoCut;
    public NavigationTreeBuilder(ChildLinksConfig childLinksConfig) {
        this.childLinksConfig = childLinksConfig;
    }

    public Tree toTree() {
        tree = createTreeWidget(resources);
        addSelectionEventToTree(tree, handlers);
        List<LinkConfig> linkConfigs = childLinksConfig.getLinkConfigList();
        if (childLinksConfig.getGroupName() != null) {
            TreeItem group = composeGroupItem();
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
        int startDepth = 0;
        TreeItem treeItem = composeTreeItem(linkConfig, startDepth);
        collectCounterDecorators(linkConfig, treeItem);
        addChildrenTreeItems(treeItem, linkConfig, startDepth);
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
        int startDepth = 0;
        TreeItem treeItem = composeTreeItem(linkConfig, startDepth);
        collectCounterDecorators(linkConfig, treeItem);
        addChildrenTreeItems(treeItem, linkConfig, startDepth);
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

    public NavigationTreeBuilder setVisibleCharsLength(int  visibleCharsLength) {
        this.visibleCharsLength = visibleCharsLength;
        return this;
    }
    public NavigationTreeBuilder setBaseAutoCut(boolean baseAutoCut) {
        this.baseAutoCut = baseAutoCut;
        return this;
    }

    private TreeItem composeGroupItem() {
        Label label = new Label();
        label.getElement().getStyle().setFloat(Style.Float.LEFT);
        String displayText = childLinksConfig.getGroupName();
        Boolean autoCut = childLinksConfig.isAutoCut();
        int startDepth = 0;
        if (isTextCut(displayText, autoCut, startDepth)) {
            String cutDisplayText = displayText.substring(0, visibleCharsLength);
            label.setText(cutDisplayText + "...");
        } else {
            label.setText(displayText);
        }
        label.setTitle(getTooltip(childLinksConfig.getTooltip(), displayText, autoCut, startDepth));
        label.setStyleName("tree-label");

        final TreeItem treeItem = new TreeItem(label);
        label.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                treeItem.setState(!treeItem.getState());
            }
        });
        treeItem.addStyleName("groupTree");
        return treeItem;
    }

    private TreeItem composeTreeItem(LinkConfig linkConfig, int depth) {
        String displayText = linkConfig.getDisplayText();
        String treeItemName = linkConfig.getName();

        Boolean autoCut = linkConfig.isAutoCut();
        Panel container = new AbsolutePanel();
        container.setStyleName("tree-label");
        container.getElement().getStyle().clearOverflow();
        Label label = new Label();
        if (isTextCut(displayText, autoCut, depth)) {
            String cutDisplayText = displayText.substring(0, visibleCharsLength);
            label.setText(cutDisplayText + "...");
        } else {
            label.setText(displayText);
        }
        label.setTitle(getTooltip(linkConfig.getTooltip(), displayText, autoCut, depth));
        label.setStyleName("treeItemTitle");
        label.getElement().getStyle().setTextDecoration(Style.TextDecoration.UNDERLINE);
        container.add(label);
        TreeItem treeItem = new TreeItem();
        treeItem.setWidget(container);

        Map<String, Object> treeUserObjects = new HashMap<>();
        treeUserObjects.put(BusinessUniverseConstants.TREE_ITEM_NAME, treeItemName);
        treeUserObjects.put(BusinessUniverseConstants.TREE_ITEM_ORIGINAL_TEXT, treeItem.getText());
        LinkPluginDefinition pluginDefinition = linkConfig.getPluginDefinition();
        if(pluginDefinition!=null)
            treeUserObjects.put(BusinessUniverseConstants.TREE_ITEM_PLUGIN_CONFIG, pluginDefinition.getPluginConfig());
        treeItem.setUserObject(treeUserObjects);

        treeItem.addStyleName("tree-item-padding-style");
        return treeItem;
    }

    private boolean isTextCut(String displayText, Boolean autoCut, int depth){
        return ((autoCut != null && autoCut) || (autoCut == null && baseAutoCut))
                && displayText.length() > visibleCharsLength && depth < 5;
    }

    private String getTooltip(String tooltip, String displayText, Boolean autoCut, int depth){
        return tooltip == null ? (isTextCut(displayText, autoCut, depth) ? displayText : "") : tooltip;
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

    private void addChildrenTreeItems(TreeItem parentTreeItem, LinkConfig parentLinkConfig, int depth) {
        List<ChildLinksConfig> childLinksConfigs = parentLinkConfig.getChildLinksConfigList();
        for (ChildLinksConfig childLinksConfig : childLinksConfigs) {
            List<LinkConfig> linkConfigList = childLinksConfig.getLinkConfigList();
            for (LinkConfig linkConfig : linkConfigList) {
                TreeItem item = composeTreeItem(linkConfig, depth);
                collectCounterDecorators(linkConfig, item);
                parentTreeItem.addItem(item);
                addChildrenTreeItems(item, linkConfig, ++depth);
            }
        }
    }

    public List<CounterDecorator> getCounterDecorators() {
        return counterDecorators;
    }
}
