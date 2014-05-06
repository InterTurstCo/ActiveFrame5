package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.HierarchyBrowserConfig;
import ru.intertrust.cm.core.config.gui.form.widget.NodeCollectionDefConfig;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.form.widget.*;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * /**
 * @author Yaroslav Bondarchuk
 *         Date: 26.12.13
 *         Time: 11:15
 */

public class ScrollNodeContentManager extends RedrawNodeContentManager {

    private int offset;
    public ScrollNodeContentManager(HierarchyBrowserConfig config,
                                    HierarchyBrowserMainPopup mainPopup, ArrayList<Id> chosenIds,
                                    String collectionName, Id parentId, String inputText,
                                    int offset, Map<String, NodeCollectionDefConfig> collectionNameNodeMap){
        super(config, mainPopup, chosenIds, collectionName, parentId, inputText, collectionNameNodeMap);
        this.offset = offset;

    }

    public void fetchNodeContent() {
        NodeContentRequest nodeContentRequest = prepareRequestDataForNodeRedraw();
        nodeContentRequest.setOffset(offset);
        Command command = new Command("fetchNodeContent", "hierarchy-browser", nodeContentRequest);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                NodeContentResponse nodeContent = (NodeContentResponse) result;
                List<HierarchyBrowserItem> items = nodeContent.getNodeContent();
                List<String> domainObjectTypes = nodeContent.getDomainObjectTypes();
                mainPopup.redrawNodeWithMoreItems(domainObjectTypes, items);

            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining rows");
            }
        });
    }
}