package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.HierarchyBrowserConfig;
import ru.intertrust.cm.core.config.gui.form.widget.NodeCollectionDefConfig;
import ru.intertrust.cm.core.config.gui.form.widget.RootNodeLinkConfig;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 10.12.13
 *         Time: 13:15
 */
public class HierarchyBrowserWidgetState extends LinkEditingWidgetState {
    private HierarchyBrowserConfig hierarchyBrowserConfig;
    private ArrayList<HierarchyBrowserItem> chosenItems = new ArrayList<HierarchyBrowserItem>();
    private Map<String, NodeCollectionDefConfig> collectionNameNodeMap;
    public HierarchyBrowserConfig getHierarchyBrowserConfig() {
        return hierarchyBrowserConfig;
    }
    private RootNodeLinkConfig rootNodeLinkConfig;
    public void setHierarchyBrowserConfig(HierarchyBrowserConfig hierarchyBrowserConfig) {
        this.hierarchyBrowserConfig = hierarchyBrowserConfig;
    }

    public Map<String, NodeCollectionDefConfig> getCollectionNameNodeMap() {
        return collectionNameNodeMap;
    }

    public void setCollectionNameNodeMap(Map<String, NodeCollectionDefConfig> collectionNameNodeMap) {
        this.collectionNameNodeMap = collectionNameNodeMap;
    }

    public RootNodeLinkConfig getRootNodeLinkConfig() {
        return rootNodeLinkConfig;
    }

    public void setRootNodeLinkConfig(RootNodeLinkConfig rootNodeLinkConfig) {
        this.rootNodeLinkConfig = rootNodeLinkConfig;
    }

    public ArrayList<HierarchyBrowserItem> getChosenItems() {
        return chosenItems;
    }

    public void setChosenItems(ArrayList<HierarchyBrowserItem> chosenItems) {
        this.chosenItems = chosenItems;
    }

    @Override
    public ArrayList<Id> getIds() {
        ArrayList<Id> chosenIds = new ArrayList<Id>();
        for (HierarchyBrowserItem item : chosenItems) {
            chosenIds.add(item.getId());
        }
        return chosenIds;
    }
}

