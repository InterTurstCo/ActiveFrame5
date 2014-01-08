package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.HierarchyBrowserConfig;
import ru.intertrust.cm.core.config.gui.form.widget.NodeCollectionDefConfig;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItemList;
import ru.intertrust.cm.core.gui.model.form.widget.NodeContentMetaData;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 26.12.13
 *         Time: 11:15
 */
public class RedrawNodeContentManager extends NodeContentManager {
    private String collectionName;
    private String inputText;
    private Id parentId;
    public RedrawNodeContentManager(HierarchyBrowserConfig config, HierarchyBrowserMainPopup mainPopup,
                                    ArrayList<Id> chosenIds, String collectionName, Id parentId, String inputText){
        super(config, mainPopup, chosenIds);
        this.collectionName = collectionName;
        this.inputText =inputText;
        this.parentId = parentId;
    }

    private NodeContentMetaData prepareRequestDataForNodeRedraw() {
        NodeCollectionDefConfig nodeConfig = getNodeConfigForNodeRedraw(collectionName, config.getNodeCollectionDefConfig());
        NodeContentMetaData nodeContentMetaData = createRequestDataFromNodeConfig(nodeConfig);
        return nodeContentMetaData;
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

    public void fetchNodeContent() {
        NodeContentMetaData  nodeContentMetaData = prepareRequestDataForNodeRedraw();
        nodeContentMetaData.setId(parentId);
        nodeContentMetaData.setInputText(inputText);
        Command command = new Command("fetchNodeContent", "hierarchy-browser", nodeContentMetaData);
        BusinessUniverseServiceAsync.Impl.getInstance().executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                HierarchyBrowserItemList nodeContent = (HierarchyBrowserItemList) result;
                List<HierarchyBrowserItem> items = nodeContent.getNodeContent();
                Id parentId = nodeContent.getParentId();
                String nodeType = nodeContent.getNodeType();
                mainPopup.redrawNode(items, nodeType, parentId);

            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining rows");
            }
        });
    }
}
