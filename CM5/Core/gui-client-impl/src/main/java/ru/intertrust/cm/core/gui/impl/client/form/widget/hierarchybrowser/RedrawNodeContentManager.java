package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.HierarchyBrowserConfig;
import ru.intertrust.cm.core.config.gui.form.widget.NodeCollectionDefConfig;
import ru.intertrust.cm.core.gui.model.form.widget.NodeContentRequest;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 09.01.14
 * Time: 11:12
 * To change this template use File | Settings | File Templates.
 */
public abstract class RedrawNodeContentManager extends NodeContentManager {
    protected String collectionName;
    protected String inputText;
    protected Id parentId;
    public RedrawNodeContentManager(HierarchyBrowserConfig config, HierarchyBrowserMainPopup mainPopup,
                                    ArrayList<Id> chosenIds, String collectionName, Id parentId, String inputText){
        super(config, mainPopup, chosenIds);
        this.collectionName = collectionName;
        this.inputText = inputText;
        this.parentId = parentId;
    }

    protected NodeContentRequest prepareRequestDataForNodeRedraw() {
        NodeCollectionDefConfig nodeConfig = getNodeConfigForNodeRedraw(collectionName, config.getNodeCollectionDefConfig());
        NodeContentRequest nodeContentRequest = createRequestDataFromNodeConfig(nodeConfig);
        return nodeContentRequest;
    }
    private NodeCollectionDefConfig getNodeConfigForNodeRedraw(String collectionName, NodeCollectionDefConfig nodeConfig) {
        if (nodeConfig == null) {
            return null;
        }

        if (collectionName.equalsIgnoreCase(nodeConfig.getCollection())){
            return nodeConfig;
        }
        else return getNodeConfigForNodeRedraw(collectionName, nodeConfig.getNodeCollectionDefConfig());
    }

    public abstract void fetchNodeContent() ;
}
