package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.HierarchyBrowserConfig;
import ru.intertrust.cm.core.config.gui.form.widget.NodeCollectionDefConfig;
import ru.intertrust.cm.core.gui.model.form.widget.NodeContentRequest;

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

    protected NodeContentRequest createRequestDataFromNodeConfig(NodeCollectionDefConfig nodeConfig) {
        NodeContentRequest nodeContentRequest = new NodeContentRequest();
        nodeContentRequest.setSelectionPattern(nodeConfig.getSelectionPatternConfig().getValue());
        nodeContentRequest.setNumberOfItemsToDisplay(config.getPageSize());
        nodeContentRequest.setCollectionName(nodeConfig.getCollection());
        nodeContentRequest.setInputTextFilterName(nodeConfig.getInputTextFilterConfig().getName());
        nodeContentRequest.setParentFilterName(nodeConfig.getParentFilter());
        nodeContentRequest.setChosenIds(chosenIds);
        nodeContentRequest.setSelective(nodeConfig.isSelective());
        return nodeContentRequest;
    }
    public abstract void fetchNodeContent() ;
}
