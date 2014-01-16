package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.HierarchyBrowserConfig;
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
public class RedrawNodeContentWithNewItemContentManager extends RedrawNodeContentManager {

    public RedrawNodeContentWithNewItemContentManager(HierarchyBrowserConfig config, HierarchyBrowserMainPopup mainPopup,
                                                      ArrayList<Id> chosenIds, String collectionName, Id parentId, String inputText){
        super(config, mainPopup, chosenIds, collectionName, parentId, inputText);

    }

    public void fetchNodeContent() {
        NodeContentRequest nodeContentRequest = prepareRequestDataForNodeRedraw();
        nodeContentRequest.getNodeMetadata().setParentId(parentId);
        nodeContentRequest.setInputText(inputText);
        Command command = new Command("fetchNodeContent", "hierarchy-browser", nodeContentRequest);
        BusinessUniverseServiceAsync.Impl.getInstance().executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                NodeContentResponse nodeContent = (NodeContentResponse) result;
                List<HierarchyBrowserItem> items = nodeContent.getNodeContent();
                NodeMetadata nodeMetadata = nodeContent.getNodeMetadata();
                mainPopup.redrawNode(items, nodeMetadata);

            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining rows");
            }
        });
    }
}
