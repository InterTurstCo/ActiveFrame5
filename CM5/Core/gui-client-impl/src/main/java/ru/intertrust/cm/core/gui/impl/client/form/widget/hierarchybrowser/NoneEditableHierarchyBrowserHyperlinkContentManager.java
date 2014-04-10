package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.HierarchyBrowserConfig;
import ru.intertrust.cm.core.config.gui.form.widget.NodeCollectionDefConfig;
import ru.intertrust.cm.core.gui.impl.client.form.widget.HierarchyBrowserNoneEditablePanelWithHyperlinks;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;
import ru.intertrust.cm.core.gui.model.form.widget.HyperlinkUpdateRequest;
import ru.intertrust.cm.core.gui.model.form.widget.HyperlinkUpdateResponse;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.01.14
 *         Time: 13:15
 */
public class NoneEditableHierarchyBrowserHyperlinkContentManager extends HierarchyBrowserHyperlinkContentManager{
    private HierarchyBrowserNoneEditablePanelWithHyperlinks panel;
    private List<HierarchyBrowserItem> items;

    public NoneEditableHierarchyBrowserHyperlinkContentManager(Id id, String collectionName, HierarchyBrowserConfig config, HierarchyBrowserNoneEditablePanelWithHyperlinks panel, List<HierarchyBrowserItem> items) {
        super(id, collectionName, config);
        this.panel = panel;
        this.items = items;
    }

    protected void updateHyperlink() {
        NodeCollectionDefConfig nodeConfig = getNodeConfigForRedraw(collectionName, config.getNodeCollectionDefConfig());
        String selectionPattern = nodeConfig.getSelectionPatternConfig().getValue();
        HyperlinkUpdateRequest request = new HyperlinkUpdateRequest(id, selectionPattern);
        Command command = new Command("updateHyperlink", "linked-domain-object-hyperlink", request);

        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                HyperlinkUpdateResponse response = (HyperlinkUpdateResponse) result;
                Id id = response.getId();
                String representation = response.getRepresentation();
                HierarchyBrowserItem updatedItem = new HierarchyBrowserItem(id, representation);

                panel.cleanPanel();
                List<HierarchyBrowserItem> browserItems = getUpdatedHyperlinks(updatedItem, items);
                for (HierarchyBrowserItem item : browserItems) {
                    panel.displayHyperlink(item);
                }

            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining rows");
            }
        });
    }
    private List<HierarchyBrowserItem> getUpdatedHyperlinks(HierarchyBrowserItem updatedItem, List<HierarchyBrowserItem> items) {

        Id idToFind = updatedItem.getId();

        for (HierarchyBrowserItem item : items) {
            if (idToFind.equals(item.getId())) {
                int index = items.indexOf(item);
                String collectionName = item.getNodeCollectionName();
                updatedItem.setChosen(true);
                updatedItem.setNodeCollectionName(collectionName);
                items.set(index, updatedItem);
            }
        }
        return items;
    }

}
