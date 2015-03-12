package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.HierarchyBrowserConfig;
import ru.intertrust.cm.core.config.gui.form.widget.NodeCollectionDefConfig;
import ru.intertrust.cm.core.gui.impl.client.form.WidgetsContainer;
import ru.intertrust.cm.core.gui.impl.client.util.GuiUtil;
import ru.intertrust.cm.core.gui.model.filters.WidgetIdComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.NodeContentRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 26.12.13
 *         Time: 11:15
 */

public abstract class NodeContentManager {
    protected HierarchyBrowserConfig config;
    protected Map<String, NodeCollectionDefConfig> collectionNameNodeMap;
    protected HierarchyBrowserMainPopup mainPopup;
    protected Id parentId;
    private ArrayList<Id> chosenIds = new ArrayList<Id>();
    private WidgetsContainer widgetsContainer;
    private Collection<WidgetIdComponentName> widgetIdComponentNames;
    @Deprecated
    public NodeContentManager(HierarchyBrowserConfig config, HierarchyBrowserMainPopup mainPopup, ArrayList<Id> chosenIds,
                              Id parentId, Map<String, NodeCollectionDefConfig> collectionNameNodeMap) {
        this.config = config;
        this.mainPopup = mainPopup;
        this.chosenIds = chosenIds;
        this.parentId = parentId;
        this.collectionNameNodeMap = collectionNameNodeMap;
    }

    public NodeContentManager(HierarchyBrowserConfig config, HierarchyBrowserMainPopup mainPopup, ArrayList<Id> chosenIds,
                              Id parentId, Map<String, NodeCollectionDefConfig> collectionNameNodeMap,
                              WidgetsContainer widgetsContainer, Collection<WidgetIdComponentName> widgetIdComponentNames) {
        this.config = config;
        this.mainPopup = mainPopup;
        this.chosenIds = chosenIds;
        this.parentId = parentId;
        this.collectionNameNodeMap = collectionNameNodeMap;
        this.widgetsContainer = widgetsContainer;
        this.widgetIdComponentNames = widgetIdComponentNames;
    }

    protected NodeContentRequest createRequestDataFromNodeConfig(NodeCollectionDefConfig nodeConfig) {
        NodeContentRequest nodeContentRequest = new NodeContentRequest();
        nodeContentRequest.setNumberOfItemsToDisplay(config.getPageSize());
        nodeContentRequest.setChosenIds(chosenIds);
        nodeContentRequest.setParentId(parentId);
        nodeContentRequest.setNodeCollectionDefConfig(nodeConfig);
        nodeContentRequest.setFormattingConfig(config.getFormattingConfig());
        nodeContentRequest.setComplexFiltersParams(GuiUtil.createComplexFiltersParams(widgetsContainer, widgetIdComponentNames));
        return nodeContentRequest;
    }

    public abstract void fetchNodeContent();
}
