package ru.intertrust.cm.core.gui.model.plugin.hierarchy;

import ru.intertrust.cm.core.config.gui.collection.view.CollectionViewConfig;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 26.07.2016
 * Time: 15:17
 * To change this template use File | Settings | File and Code Templates.
 */
public class HierarchyPluginData extends PluginData {
    private List<CollectionRowItem> collectionRowItems;
    private HierarchyRequest hierarchyRequest;
    private CollectionViewConfig collectionViewConfig;
    private HierarchyHistoryNode openedNodeList;

    public List<CollectionRowItem> getCollectionRowItems() {
        if (collectionRowItems == null) {
            collectionRowItems = new ArrayList<>();
        }
        return collectionRowItems;
    }

    public void setCollectionRowItems(List<CollectionRowItem> collectionRowItems) {
        this.collectionRowItems = collectionRowItems;
    }

    public HierarchyRequest getHierarchyRequest() {
        return hierarchyRequest;
    }

    public void setHierarchyRequest(HierarchyRequest hierarchyRequest) {
        this.hierarchyRequest = hierarchyRequest;
    }

    public CollectionViewConfig getCollectionViewConfig() {
        return collectionViewConfig;
    }

    public void setCollectionViewConfig(CollectionViewConfig collectionViewConfig) {
        this.collectionViewConfig = collectionViewConfig;
    }

    public HierarchyHistoryNode getOpenedNodeList() {
        return openedNodeList;
    }

    public void setOpenedNodeList(HierarchyHistoryNode openedNodeList) {
        this.openedNodeList = openedNodeList;
    }
}
