package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.HierarchyBrowserConfig;
import ru.intertrust.cm.core.config.gui.form.widget.NodeCollectionDefConfig;
import ru.intertrust.cm.core.gui.impl.client.form.WidgetsContainer;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.filters.WidgetIdComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;
import ru.intertrust.cm.core.gui.model.form.widget.NodeContentRequest;
import ru.intertrust.cm.core.gui.model.form.widget.NodeContentResponse;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 26.12.13
 *         Time: 11:15
 */
public class RefreshNodeContentManager extends RedrawNodeContentManager {
    @Deprecated
    public RefreshNodeContentManager(HierarchyBrowserConfig config, HierarchyBrowserMainPopup mainPopup,
                                     ArrayList<Id> chosenIds, String collectionName, Id parentId,
                                     String inputText, Map<String, NodeCollectionDefConfig> collectionNameNodeMap){
        super(config, mainPopup, chosenIds, collectionName, parentId, inputText, collectionNameNodeMap);

    }
    public RefreshNodeContentManager(HierarchyBrowserConfig config, HierarchyBrowserMainPopup mainPopup,
                                     ArrayList<Id> chosenIds, String collectionName, Id parentId,
                                     String inputText, Map<String, NodeCollectionDefConfig> collectionNameNodeMap,
                                     WidgetsContainer widgetsContainer, Collection<WidgetIdComponentName> widgetIdComponentNames){
        super(config, mainPopup, chosenIds, collectionName, parentId, inputText, collectionNameNodeMap,
                widgetsContainer,widgetIdComponentNames);

    }

    public void fetchNodeContent() {
        NodeContentRequest nodeContentRequest = prepareRequestDataForNodeRedraw();
        nodeContentRequest.setInputText(inputText);
        Command command = new Command("fetchNodeContent", HierarchyBrowserWidget.COMPONENT_NAME, nodeContentRequest);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                NodeContentResponse nodeContent = (NodeContentResponse) result;
                List<HierarchyBrowserItem> items = nodeContent.getNodeContent();
                List<NodeCollectionDefConfig> nodeConfigs = nodeContent.getNodeCollectionDefConfigs();
                mainPopup.redrawNode(nodeConfigs, items);

            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining rows");
            }
        });
    }
}
