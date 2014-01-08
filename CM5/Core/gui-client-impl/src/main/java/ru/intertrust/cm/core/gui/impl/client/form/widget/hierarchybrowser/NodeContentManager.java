package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.HierarchyBrowserConfig;
import ru.intertrust.cm.core.config.gui.form.widget.NodeCollectionDefConfig;
import ru.intertrust.cm.core.gui.model.form.widget.NodeContentMetaData;

import java.util.ArrayList;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 26.12.13
 *         Time: 11:15
 */

public abstract class NodeContentManager {
    protected HierarchyBrowserConfig config;
    protected HierarchyBrowserMainPopup mainPopup;
    private ArrayList<Id> chosenIds = new ArrayList<Id>();
    public NodeContentManager(HierarchyBrowserConfig config, HierarchyBrowserMainPopup mainPopup, ArrayList<Id> chosenIds) {
        this.config = config;
        this.mainPopup = mainPopup;
        this.chosenIds = chosenIds;
    }
    protected NodeContentMetaData createRequestDataFromNodeConfig(NodeCollectionDefConfig nodeConfig) {
        NodeContentMetaData nodeContentMetaData = new NodeContentMetaData();
        nodeContentMetaData.setSelectionPattern(nodeConfig.getSelectionPatternConfig().getValue());
        nodeContentMetaData.setNumberOfItemsToDisplay(config.getPageSize());
        nodeContentMetaData.setCollectionName(nodeConfig.getCollection());
        nodeContentMetaData.setInputTextFilterName(nodeConfig.getInputTextFilterConfig().getName());
        nodeContentMetaData.setParentFilterName(nodeConfig.getParentFilter());
        nodeContentMetaData.setChosenIds(chosenIds);
        return nodeContentMetaData;
    }
    public abstract void fetchNodeContent() ;
}
