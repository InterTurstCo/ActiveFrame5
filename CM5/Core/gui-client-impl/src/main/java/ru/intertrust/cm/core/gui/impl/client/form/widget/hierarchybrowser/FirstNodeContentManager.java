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
public class FirstNodeContentManager extends NodeContentManager {
    public FirstNodeContentManager(HierarchyBrowserConfig config, HierarchyBrowserMainPopup mainPopup,
                                   ArrayList<Id> chosenIds) {
        super(config, mainPopup, chosenIds);
    }

    private NodeContentMetaData prepareRequestDataForFirstNodeOpening() {
        NodeCollectionDefConfig nodeConfig = config.getNodeCollectionDefConfig();
        NodeContentMetaData nodeContentMetaData = createRequestDataFromNodeConfig(nodeConfig);
        return nodeContentMetaData;
    }

    public void fetchNodeContent() {
        NodeContentMetaData nodeContentMetaData = prepareRequestDataForFirstNodeOpening();
        Command command = new Command("fetchNodeContent", "hierarchy-browser", nodeContentMetaData);
        BusinessUniverseServiceAsync.Impl.getInstance().executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                HierarchyBrowserItemList nodeContent = (HierarchyBrowserItemList) result;
                List<HierarchyBrowserItem> items = nodeContent.getNodeContent();
                String nodeType = nodeContent.getNodeType();
                Id parentId = nodeContent.getParentId();
                mainPopup.drawNewNode(items, nodeType, parentId);

            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining rows");
            }
        });
    }
}
