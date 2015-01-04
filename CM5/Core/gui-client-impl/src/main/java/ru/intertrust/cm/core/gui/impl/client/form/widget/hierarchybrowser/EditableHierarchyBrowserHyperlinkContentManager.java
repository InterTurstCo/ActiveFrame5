package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.HierarchyBrowserConfig;
import ru.intertrust.cm.core.config.gui.form.widget.NodeCollectionDefConfig;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItemBuilder;
import ru.intertrust.cm.core.gui.model.form.widget.RepresentationRequest;
import ru.intertrust.cm.core.gui.model.form.widget.RepresentationResponse;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.01.14
 *         Time: 13:15
 */
public abstract class EditableHierarchyBrowserHyperlinkContentManager extends HierarchyBrowserHyperlinkContentManager {

    public EditableHierarchyBrowserHyperlinkContentManager(Id id, String collectionName, HierarchyBrowserConfig config,
                                                           Map<String, NodeCollectionDefConfig> collectionNameNodeMap) {
        super(id, collectionName, config, collectionNameNodeMap);
    }

    @Override
    protected void updateHyperlink() {
        NodeCollectionDefConfig nodeConfig = collectionNameNodeMap.get(collectionName);
        String selectionPattern = nodeConfig.getSelectionPatternConfig().getValue();
        List<Id> ids = new ArrayList<Id>();
        ids.add(id);
        RepresentationRequest request = new RepresentationRequest(ids, selectionPattern, nodeConfig.getCollection(),
                config.getFormattingConfig());
        Command command = new Command("getRepresentationForOneItem", HierarchyBrowserWidget.COMPONENT_NAME, request);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                RepresentationResponse response = (RepresentationResponse) result;
                Id id = response.getId();
                String representation = response.getRepresentation();
                HierarchyBrowserItem updatedItem = new HierarchyBrowserItemBuilder()
                        .setId(id).setStringRepresentation(representation)
                        .createHierarchyBrowserItem();
                handleHyperlinkUpdate(updatedItem);

            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining rows");
            }
        });
    }
    protected abstract void handleHyperlinkUpdate(HierarchyBrowserItem updatedItem);

}
