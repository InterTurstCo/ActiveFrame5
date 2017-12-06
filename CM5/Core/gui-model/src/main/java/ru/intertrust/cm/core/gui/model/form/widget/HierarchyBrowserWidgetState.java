package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Case;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.form.PopupTitlesHolder;
import ru.intertrust.cm.core.config.gui.form.widget.HierarchyBrowserConfig;
import ru.intertrust.cm.core.config.gui.form.widget.NodeCollectionDefConfig;
import ru.intertrust.cm.core.config.gui.form.widget.RootNodeLinkConfig;
import ru.intertrust.cm.core.gui.model.filters.WidgetIdComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.hierarchybrowser.HierarchyBrowserUtil;
import ru.intertrust.cm.core.gui.model.util.WidgetUtil;

import java.util.*;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 10.12.13
 *         Time: 13:15
 */
public class HierarchyBrowserWidgetState extends LinkEditingWidgetState {
    private HierarchyBrowserConfig hierarchyBrowserConfig;
    private ArrayList<HierarchyBrowserItem> chosenItems = new ArrayList<HierarchyBrowserItem>();
    private Map<String, NodeCollectionDefConfig> collectionNameNodeMap;
    private RootNodeLinkConfig rootNodeLinkConfig;
    private ArrayList<Id> selectedIds;
    private ArrayList<HierarchyBrowserItem> tooltipChosenItems;
    private boolean isHandlingTemporarySate;
    private ArrayList<HierarchyBrowserItem> temporaryChosenItems;
    private ArrayList<Id> temporarySelectedIds;
    private ArrayList<HierarchyBrowserItem> temporaryTooltipChosenItems;
    private Map<String, Integer> temporaryCountOfType;
    private int recursiveDeepness;
    private Collection<WidgetIdComponentName> widgetIdComponentNames;

    public HierarchyBrowserConfig getHierarchyBrowserConfig() {
        return hierarchyBrowserConfig;
    }

    public void setHierarchyBrowserConfig(HierarchyBrowserConfig hierarchyBrowserConfig) {
        this.hierarchyBrowserConfig = hierarchyBrowserConfig;
    }

    public Map<String, NodeCollectionDefConfig> getCollectionNameNodeMap() {
        return collectionNameNodeMap;
    }

    public void setCollectionNameNodeMap(Map<String, NodeCollectionDefConfig> collectionNameNodeMap) {
        this.collectionNameNodeMap = collectionNameNodeMap;
    }

    public RootNodeLinkConfig getRootNodeLinkConfig() {
        return rootNodeLinkConfig;
    }

    public void setRootNodeLinkConfig(RootNodeLinkConfig rootNodeLinkConfig) {
        this.rootNodeLinkConfig = rootNodeLinkConfig;
    }

    public ArrayList<HierarchyBrowserItem> getCurrentItems() {
        return isHandlingTemporarySate ? temporaryChosenItems : chosenItems;
    }

    public ArrayList<Id> getTemporarySelectedIds() {
        return temporarySelectedIds;
    }

    public ArrayList<HierarchyBrowserItem> getChosenItems() {
        return chosenItems;
    }

    public void setChosenItems(ArrayList<HierarchyBrowserItem> chosenItems) {
        this.chosenItems = chosenItems;
    }

    public void setSelectedIds(ArrayList<Id> selectedIds) {
        this.selectedIds = selectedIds;
    }

    public ArrayList<HierarchyBrowserItem> getTooltipChosenItems() {
        return isHandlingTemporarySate ? temporaryTooltipChosenItems : tooltipChosenItems;
    }

    public void setTooltipChosenItems(ArrayList<HierarchyBrowserItem> tooltipChosenItems) {
        if (isHandlingTemporarySate) {
            this.temporaryTooltipChosenItems = tooltipChosenItems;
        } else {
            this.tooltipChosenItems = tooltipChosenItems;
        }
    }

    public ArrayList<HierarchyBrowserItem> getTemporaryChosenItems() {
        return temporaryChosenItems;
    }

    public Collection<WidgetIdComponentName> getWidgetIdComponentNames() {
        return widgetIdComponentNames;
    }

    public void setWidgetIdComponentNames(Collection<WidgetIdComponentName> widgetIdComponentNames) {
        this.widgetIdComponentNames = widgetIdComponentNames;
    }

    public void applyChanges() {
        setHandlingTemporarySate(false);
        selectedIds = temporarySelectedIds;
        chosenItems = temporaryChosenItems;
        tooltipChosenItems = temporaryTooltipChosenItems;
        HierarchyBrowserUtil.updateCountOfType(temporaryCountOfType, collectionNameNodeMap);

    }

    public void resetChanges() {
        setHandlingTemporarySate(false);
    }

    public void initTemporaryState() {
        setHandlingTemporarySate(true);
        temporarySelectedIds = new ArrayList<Id>(selectedIds);
        temporaryTooltipChosenItems = tooltipChosenItems == null
                ? null
                : HierarchyBrowserUtil.getCopyOfChosenItems(tooltipChosenItems);
        temporaryChosenItems = chosenItems == null
                ? new ArrayList<HierarchyBrowserItem>()
                : HierarchyBrowserUtil.getCopyOfChosenItems(chosenItems);
        temporaryCountOfType = HierarchyBrowserUtil.createTemporaryCountOfType(collectionNameNodeMap);
    }

    public void setHandlingTemporarySate(boolean isHandlingTemporarySate) {
        this.isHandlingTemporarySate = isHandlingTemporarySate;
    }

    public void handleAddingItem(HierarchyBrowserItem item) {
        HierarchyBrowserUtil.preHandleAddingItemToTempState(item, this);
        handleAddingToTempSate(item);
    }

    public void handleRemovingItem(HierarchyBrowserItem item) {
        boolean isTooltipContent = getTooltipChosenItems() != null && getTooltipChosenItems().contains(item);
        if (isTooltipContent) {
            handleRemovingItemFromTooltipContent(item);
        } else {
            handleRemovingFromContent(item);
        }
    }

    private void handleRemovingItemFromTooltipContent(HierarchyBrowserItem item) {
        Id id = item.getId();
        String collectionName = item.getNodeCollectionName();
        if (isHandlingTemporarySate) {
            temporaryTooltipChosenItems.remove(item);
            temporarySelectedIds.remove(id);
            decrementTempCountOfType(collectionName);
        } else {
            tooltipChosenItems.remove(item);
            selectedIds.remove(id);
            decrementCountOfType(collectionName);
        }
    }

    private void handleRemovingFromContent(HierarchyBrowserItem item) {
        HierarchyBrowserUtil.preHandleRemovingItem(item, this);
        if (isHandlingTemporarySate) {
            handleRemovingFromTempSate(item);
        } else {
            handleRemoving(item);
        }
    }

    public void clearState() {
        selectedIds.clear();
        chosenItems.clear();
        tooltipChosenItems = null;
    }

    public boolean isTooltipAvailable() {

        return isTooltipAvailable(0);
    }

    public boolean isTooltipAvailable(int delta) {

        Set<String> collectionNames = collectionNameNodeMap.keySet();
        boolean result = false;
        for (String collectionName : collectionNames) {
            NodeCollectionDefConfig config = collectionNameNodeMap.get(collectionName);
            int count = isHandlingTemporarySate ? temporaryCountOfType.get(collectionName) : config.getElementsCount();
            int limit = WidgetUtil.getLimit(config.getSelectionFiltersConfig());
            if (limit != -1 && count + delta > limit) {
                result = true;
                break;
            }

        }
        return result;
    }

    public String getHyperlinkPopupTitle(String collectionName, String domainObjectType) {
        PopupTitlesHolder popupTitlesHolder = collectionNameNodeMap.get(collectionName).getDoTypeTitlesMap()
                .get(Case.toLower(domainObjectType));
        return popupTitlesHolder == null ? null : popupTitlesHolder.getTitleExistingObject();
    }

    public void handleRemoving(HierarchyBrowserItem item) {
        chosenItems.remove(item);
        selectedIds.remove(item.getId());
        decrementCountOfType(item.getNodeCollectionName());
    }

    public void handleCommonSingleChoice(HierarchyBrowserItem item) {
        if (temporaryChosenItems == null){
            temporaryChosenItems = chosenItems;
        }
        if(temporarySelectedIds == null){
            temporarySelectedIds = selectedIds;
        }
        if(temporaryCountOfType == null){
            temporaryCountOfType = HierarchyBrowserUtil.createTemporaryCountOfType(collectionNameNodeMap);
        }

            Iterator<HierarchyBrowserItem> iterator = temporaryChosenItems.iterator();
            while (iterator.hasNext()) {
                HierarchyBrowserItem chosenItem = iterator.next();
                iterator.remove();
                postTempItemRemove(chosenItem);
            }

        if (item.isChosen()) {
            handleAddingToTempSate(item);

        }
    }

    private void postTempItemRemove(HierarchyBrowserItem item) {
        temporarySelectedIds.remove(item.getId());
        decrementTempCountOfType(item.getNodeCollectionName());

    }

    public void handleNodeSingleChoice(HierarchyBrowserItem item) {
        if (item.isChosen()) {
            handleAddingToTempSate(item);

        } else {
            temporaryChosenItems.remove(item);
            postTempItemRemove(item);

        }
    }

    private void handleAddingToTempSate(HierarchyBrowserItem item) {
        temporaryChosenItems.add(item);
        temporarySelectedIds.add(item.getId());
        incrementTempCountOfType(item.getNodeCollectionName());
    }

    private void incrementTempCountOfType(String collectionName) {
        int oldValue = temporaryCountOfType.get(collectionName);
        int newValue = ++oldValue;
        temporaryCountOfType.put(collectionName, newValue);
    }

    private void decrementCountOfType(String collectionName) {
        NodeCollectionDefConfig defConfig = collectionNameNodeMap.get(collectionName);
        int oldValue = defConfig.getElementsCount();
        int newValue = --oldValue;
        defConfig.setElementsCount(newValue);
    }

    private void handleRemovingFromTempSate(HierarchyBrowserItem item) {
        temporaryChosenItems.remove(item);
        postTempItemRemove(item);
    }

    private void decrementTempCountOfType(String collectionName) {
        Integer oldValue = temporaryCountOfType.get(collectionName);
        Integer newValue = --oldValue;
        temporaryCountOfType.put(collectionName, newValue);
    }

    @Deprecated //not used anymore
    public int getRecursiveDeepness() {
        return recursiveDeepness;
    }

    @Deprecated //not used anymore
    public void setRecursiveDeepness(int recursiveDeepness) {
        this.recursiveDeepness = recursiveDeepness;
    }

    @Override
    public ArrayList<Id> getIds() {
        return selectedIds;
    }
}

