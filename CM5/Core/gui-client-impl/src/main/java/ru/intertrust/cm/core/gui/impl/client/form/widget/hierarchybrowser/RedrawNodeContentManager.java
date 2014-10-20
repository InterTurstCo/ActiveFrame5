package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.HierarchyBrowserConfig;
import ru.intertrust.cm.core.config.gui.form.widget.NodeCollectionDefConfig;
import ru.intertrust.cm.core.gui.model.form.widget.NodeContentRequest;

import java.util.ArrayList;
import java.util.Map;

/**
 * /**
 * @author Yaroslav Bondarchuk
 *         Date: 26.12.13
 *         Time: 11:15
 */
public abstract class RedrawNodeContentManager extends NodeContentManager {
    protected String collectionName;
    protected String inputText;

    public RedrawNodeContentManager(HierarchyBrowserConfig config, HierarchyBrowserMainPopup mainPopup,
                                    ArrayList<Id> chosenIds, String collectionName, Id rootId, Id parentId,
                                    String inputText, Map<String, NodeCollectionDefConfig> collectionNameNodeMap){
        super(config, mainPopup, chosenIds, rootId, parentId, collectionNameNodeMap);
        this.collectionName = collectionName;
        this.inputText = inputText;
        this.parentId = parentId;
        this.collectionNameNodeMap = collectionNameNodeMap;
    }

    protected NodeContentRequest prepareRequestDataForNodeRedraw() {
        NodeCollectionDefConfig nodeConfig = collectionNameNodeMap.get(collectionName);
        NodeContentRequest nodeContentRequest = createRequestDataFromNodeConfig(nodeConfig);
        nodeContentRequest.setOpenChildren(false);
        return nodeContentRequest;
    }

    public abstract void fetchNodeContent() ;
}
