package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.gui.form.widget.HierarchyBrowserConfig;
import ru.intertrust.cm.core.config.gui.form.widget.NodeCollectionDefConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionPatternConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SingleChoiceConfig;
import ru.intertrust.cm.core.config.gui.navigation.DefaultSortCriteriaConfig;
import ru.intertrust.cm.core.gui.api.server.widget.FormatHandler;
import ru.intertrust.cm.core.gui.api.server.widget.LinkEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.impl.server.util.FilterBuilder;
import ru.intertrust.cm.core.gui.impl.server.util.SortOrderBuilder;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserWidgetState;
import ru.intertrust.cm.core.gui.model.form.widget.NodeContentRequest;
import ru.intertrust.cm.core.gui.model.form.widget.NodeContentResponse;

import java.util.*;
import java.util.regex.Matcher;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 10.12.13
 *         Time: 13:15
 */
@ComponentName("hierarchy-browser")
public class HierarchyBrowserHandler extends LinkEditingWidgetHandler {

    @Autowired
    private CollectionsService collectionsService;

    @Override
    public HierarchyBrowserWidgetState getInitialState(WidgetContext context) {

        HierarchyBrowserConfig widgetConfig = context.getWidgetConfig();
        NodeCollectionDefConfig nodeConfig = widgetConfig.getNodeCollectionDefConfig();
        SelectionPatternConfig selectionPatternConfig = nodeConfig.getSelectionPatternConfig();
        Matcher selectionMatcher = FormatHandler.pattern.matcher(selectionPatternConfig.getValue());
        ArrayList<Id> selectedIds = context.getAllObjectIds();
        ArrayList<HierarchyBrowserItem> chosenItems = new ArrayList<HierarchyBrowserItem>();
        Map<String, NodeCollectionDefConfig> collectionNameNodeMap = new HashMap<>();
        String firstCollectionName = nodeConfig.getCollection();
        collectionNameNodeMap.put(firstCollectionName, nodeConfig);
        fillCollectionNameNodeMap(nodeConfig, collectionNameNodeMap);
        if (!selectedIds.isEmpty()) {
            List<Filter> filters = new ArrayList<Filter>();
            filters = addIncludeIdsFilter(selectedIds, filters);
            Set<String> collectionNames = collectionNameNodeMap.keySet();
            for (String collectionName : collectionNames) {
                generateChosenItemsForCollection(collectionName, filters, selectionMatcher, chosenItems);
            }

        }
        HierarchyBrowserWidgetState state = new HierarchyBrowserWidgetState();
        state.setCollectionNameNodeMap(collectionNameNodeMap);
        SingleChoiceConfig singleChoiceConfig = widgetConfig.getSingleChoice();
        boolean singleChoiceFromConfig = singleChoiceConfig == null ? false : singleChoiceConfig.isSingleChoice();
        boolean singleChoice = isSingleChoice(context, singleChoiceFromConfig);
        state.setSingleChoice(singleChoice);
        state.setHierarchyBrowserConfig(widgetConfig);
        state.setChosenItems(chosenItems);
        state.setRootNodeLinkConfig(nodeConfig.getRootNodeLinkConfig());
        return state;
    }

    private void generateChosenItemsForCollection(String collectionName, List<Filter> filters,
                                                  Matcher matcher, ArrayList<HierarchyBrowserItem> items) {
        IdentifiableObjectCollection collectionForFacebookStyleItems = collectionsService.
                findCollection(collectionName, null, filters, 0, 0);
        for (IdentifiableObject identifiableObject : collectionForFacebookStyleItems) {
            HierarchyBrowserItem item = createHierarchyBrowserItem(collectionName, identifiableObject, matcher);
            items.add(item);
        }

    }

    private HierarchyBrowserItem createHierarchyBrowserItem(String collectionName,
                                                            IdentifiableObject identifiableObject, Matcher matcher) {
        HierarchyBrowserItem item = new HierarchyBrowserItem();
        item.setId(identifiableObject.getId());
        item.setStringRepresentation(formatHandler.format(identifiableObject, matcher));
        item.setNodeCollectionName(collectionName);
        return item;
    }

    public NodeContentResponse fetchNodeContent(Dto inputParams) {
        NodeContentRequest nodeContentRequest = (NodeContentRequest) inputParams;
        ArrayList<HierarchyBrowserItem> items = new ArrayList<HierarchyBrowserItem>();
        Id parentId = nodeContentRequest.getParentId();
        NodeCollectionDefConfig rootNodeCollectionDefConfig = nodeContentRequest.getNodeCollectionDefConfig();
        List<NodeCollectionDefConfig> nodeCollectionDefConfigs = getNodeCollectionConfigs(rootNodeCollectionDefConfig,
                parentId);
        int numberOfItems = nodeContentRequest.getNumberOfItemsToDisplay();
        int offset = nodeContentRequest.getOffset();
        ArrayList<Id> chosenIds = nodeContentRequest.getChosenIds();
        Map<String, String> domainObjectTypesAndTitles = new HashMap<>();

        for (NodeCollectionDefConfig nodeCollectionDefConfig : nodeCollectionDefConfigs) {
            String domainObjectType = nodeCollectionDefConfig.getDomainObjectType();
            String titleFromConfig = nodeCollectionDefConfig.getTitle();
            String title = titleFromConfig == null ? domainObjectType : titleFromConfig;
            domainObjectTypesAndTitles.put(domainObjectType, title);
            String collectionName = nodeCollectionDefConfig.getCollection();
            Matcher selectionMatcher = FormatHandler.pattern.matcher(nodeCollectionDefConfig.getSelectionPatternConfig().getValue());
            List<Filter> filters = new ArrayList<Filter>();
            if (parentId != null) {
                filters = addParentFilter(parentId, nodeCollectionDefConfig.getParentFilter(), filters);
            }
            String inputText = nodeContentRequest.getInputText();
            if (inputText != null && !inputText.equalsIgnoreCase("")) {
                String inputTextFilterName = nodeCollectionDefConfig.getInputTextFilterConfig().getName();
                filters = addInputTextFilter(inputTextFilterName, inputText, filters);
            }
            DefaultSortCriteriaConfig sortCriteriaConfig = nodeCollectionDefConfig.getDefaultSortCriteriaConfig();
            SortOrder sortOrder = SortOrderBuilder.getSimpleSortOrder(sortCriteriaConfig);
            IdentifiableObjectCollection collection = collectionsService.
                    findCollection(collectionName, sortOrder, filters, offset, numberOfItems);

            List<NodeCollectionDefConfig> children = nodeCollectionDefConfig.getNodeCollectionDefConfigs();
            boolean mayHaveChildren  = !children.isEmpty();
            for (IdentifiableObject identifiableObject : collection) {
                HierarchyBrowserItem item = new HierarchyBrowserItem();
                Id id = identifiableObject.getId();
                item.setId(id);
                item.setStringRepresentation(formatHandler.format(identifiableObject, selectionMatcher));
                item.setNodeCollectionName(collectionName);
                if (chosenIds.contains(id)) {
                    item.setChosen(true);
                }
                item.setMayHaveChildren(mayHaveChildren);
                items.add(item);
            }

        }
        NodeContentResponse nodeContent = new NodeContentResponse();
        nodeContent.setNodeContent(items);
        nodeContent.setDomainObjectTypesAndTitles(domainObjectTypesAndTitles);
        nodeContent.setParentCollectionName(rootNodeCollectionDefConfig.getCollection());
        nodeContent.setSelective(nodeContentRequest.isSelective());
        nodeContent.setParentId(parentId);
        return nodeContent;
    }

    private List<Filter> addParentFilter(Id parentId, String filterByParentName, List<Filter> filters) {
        Filter parentFilter = FilterBuilder.prepareReferenceFilter(parentId, filterByParentName);
        filters.add(parentFilter);
        return filters;
    }

    private List<Filter> addIncludeIdsFilter(List<Id> includedIds, List<Filter> filters) {
        Set<Id> idsIncluded = new HashSet<Id>(includedIds);
        Filter idsIncludedFilter = FilterBuilder.prepareFilter(idsIncluded, FilterBuilder.INCLUDED_IDS_FILTER);
        filters.add(idsIncludedFilter);
        return filters;
    }

    private List<Filter> addInputTextFilter(String name, String text, List<Filter> filters) {
        Filter textFilter = new Filter();
        textFilter.setFilter(name);
        textFilter.addCriterion(0, new StringValue(text + "%"));
        filters.add(textFilter);
        return filters;
    }

    private void fillCollectionNameNodeMap(NodeCollectionDefConfig nodeConfig,
                                           Map<String, NodeCollectionDefConfig> collectionNameNodeMap) {
        List<NodeCollectionDefConfig> nodeCollectionConfigs = nodeConfig.getNodeCollectionDefConfigs();
        for (NodeCollectionDefConfig node : nodeCollectionConfigs) {
            collectionNameNodeMap.put(node.getCollection(), node);
            List<NodeCollectionDefConfig> childNodes = node.getNodeCollectionDefConfigs();
            for (NodeCollectionDefConfig childNode : childNodes) {
                collectionNameNodeMap.put(childNode.getCollection(), childNode);
                fillCollectionNameNodeMap(childNode, collectionNameNodeMap);
            }
        }
    }

    private List<NodeCollectionDefConfig> getNodeCollectionConfigs(NodeCollectionDefConfig rootNodeCollectionDefConfig,
                                                                   Id parentId) {

        if (parentId != null) {
            return rootNodeCollectionDefConfig.getNodeCollectionDefConfigs();
        } else {
            List<NodeCollectionDefConfig> nodeCollectionDefConfigs = new ArrayList<>();
            nodeCollectionDefConfigs.add(rootNodeCollectionDefConfig);
            return nodeCollectionDefConfigs;
        }
    }
}
