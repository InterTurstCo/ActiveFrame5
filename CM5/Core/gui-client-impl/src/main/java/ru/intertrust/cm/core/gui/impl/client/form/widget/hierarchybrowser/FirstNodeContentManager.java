package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.HierarchyBrowserConfig;
import ru.intertrust.cm.core.config.gui.form.widget.NodeCollectionDefConfig;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;
import ru.intertrust.cm.core.gui.model.form.widget.NodeContentRequest;
import ru.intertrust.cm.core.gui.model.form.widget.NodeContentResponse;
import ru.intertrust.cm.core.gui.model.form.widget.NodeMetadata;
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

    private NodeContentRequest prepareRequestDataForFirstNodeOpening() {
        NodeCollectionDefConfig nodeConfig = config.getNodeCollectionDefConfig();
        NodeContentRequest nodeContentRequest = createRequestDataFromNodeConfig(nodeConfig);
        return nodeContentRequest;
    }

    public void fetchNodeContent() {
        NodeContentRequest nodeContentRequest = prepareRequestDataForFirstNodeOpening();
        Command command = new Command("fetchNodeContent", "hierarchy-browser", nodeContentRequest);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                NodeContentResponse nodeContent = (NodeContentResponse) result;
                List<HierarchyBrowserItem> items = nodeContent.getNodeContent();
                NodeMetadata nodeMetadata = nodeContent.getNodeMetadata();
                boolean selective = nodeContent.isSelective();
                mainPopup.drawNewNode(items, nodeMetadata, selective);

            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining rows");
            }
        });
    }
}
