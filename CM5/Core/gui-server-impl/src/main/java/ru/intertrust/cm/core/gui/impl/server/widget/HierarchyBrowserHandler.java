package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.access.AccessVerificationService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.form.PopupTitlesHolder;
import ru.intertrust.cm.core.config.gui.form.widget.*;
import ru.intertrust.cm.core.config.gui.form.widget.filter.AbstractFilterConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.LinkedFormMappingConfig;
import ru.intertrust.cm.core.config.gui.navigation.DefaultSortCriteriaConfig;
import ru.intertrust.cm.core.gui.api.server.plugin.FilterBuilder;
import ru.intertrust.cm.core.gui.api.server.plugin.LiteralFieldValueParser;
import ru.intertrust.cm.core.gui.api.server.widget.FormatHandler;
import ru.intertrust.cm.core.gui.api.server.widget.LinkEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.impl.server.util.FilterBuilderUtil;
import ru.intertrust.cm.core.gui.impl.server.util.SortOrderBuilder;
import ru.intertrust.cm.core.gui.impl.server.util.WidgetConstants;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.*;
import ru.intertrust.cm.core.gui.model.util.WidgetUtil;
import ru.intertrust.cm.core.util.ObjectCloner;

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

    @Autowired
    private FilterBuilder filterBuilder;

    @Autowired
    private CrudService crudService;

    @Autowired
    private AccessVerificationService accessVerificationService;

    @Autowired
    private LiteralFieldValueParser literalFieldValueParser;

    @Override
    public HierarchyBrowserWidgetState getInitialState(WidgetContext context) {
        HierarchyBrowserConfig widgetConfig = context.getWidgetConfig();
        final ObjectCloner cloner = new ObjectCloner();
        widgetConfig = cloner.cloneObject(widgetConfig, HierarchyBrowserConfig.class);
        NodeCollectionDefConfig nodeConfig = widgetConfig.getNodeCollectionDefConfig();
        ArrayList<Id> selectedIds = context.getAllObjectIds();
        Map<String, NodeCollectionDefConfig> collectionNameNodeMap = new HashMap<>();
        String firstCollectionName = nodeConfig.getCollection();
        collectionNameNodeMap.put(firstCollectionName, nodeConfig);
        fillCollectionNameNodeMap(nodeConfig, collectionNameNodeMap);
        DomainObject root = context.getFormObjects().getRootNode().getDomainObject();
        fillNodeConfigs(root, collectionNameNodeMap);
        FormattingConfig formattingConfig = widgetConfig.getFormattingConfig();
        ArrayList<HierarchyBrowserItem> chosenItems = new ArrayList<HierarchyBrowserItem>();
        boolean hasSelectionFilters = false;
        boolean noLimit = true;
        if (!selectedIds.isEmpty()) {
            Set<String> collectionNames = collectionNameNodeMap.keySet();
            for (String collectionName : collectionNames) {
                hasSelectionFilters = hasSelectionFilters || hasSelectionFilters(collectionName, collectionNameNodeMap);
                noLimit = noLimit && hasNoLimit(collectionName, collectionNameNodeMap);
                generateChosenItems(collectionName, collectionNameNodeMap, formattingConfig, selectedIds, chosenItems,
                        false);

            }

        }
        if (!hasSelectionFilters && selectedIds.size() != chosenItems.size() && noLimit) {
            correctChosenItems(selectedIds, chosenItems);
        }
        HierarchyBrowserWidgetState state = new HierarchyBrowserWidgetState();
        state.setSelectedIds(selectedIds);
        state.setCollectionNameNodeMap(collectionNameNodeMap);
        SingleChoiceConfig singleChoiceConfig = widgetConfig.getSingleChoice();
        Boolean singleChoiceFromConfig = singleChoiceConfig == null ? false : singleChoiceConfig.isSingleChoice();
        boolean singleChoice = isSingleChoice(context, singleChoiceFromConfig);
        state.setSingleChoice(singleChoice);
        state.setHierarchyBrowserConfig(widgetConfig);
        state.setChosenItems(chosenItems);
        state.setRootNodeLinkConfig(nodeConfig.getRootNodeLinkConfig());
        Id rootId = root == null ? null : root.getId();
        state.setRootId(rootId);
        return state;
    }

    private void generateChosenItems(String collectionName, Map<String, NodeCollectionDefConfig> collectionNameNodeMap,
                                                   FormattingConfig formattingConfig, List<Id> selectedIds,
                                                   List<HierarchyBrowserItem> items, boolean tooltipContent) {

        List<Filter> filters = new ArrayList<Filter>();
        filters = addIncludeIdsFilter(selectedIds, filters);
        NodeCollectionDefConfig nodeConfig = collectionNameNodeMap.get(collectionName);
        SelectionFiltersConfig selectionFiltersConfig = nodeConfig.getSelectionFiltersConfig();
        filterBuilder.prepareSelectionFilters(selectionFiltersConfig, null, filters);
        Integer limit = WidgetUtil.getLimit(selectionFiltersConfig);
        SortOrder sortOrder = SortOrderBuilder.getSelectionSortOrder(nodeConfig.getSelectionSortCriteriaConfig());

        IdentifiableObjectCollection collection = null;
        if(limit == -1 && !tooltipContent) {
            collection = collectionsService.findCollection(collectionName, sortOrder, filters);

        } else if(limit != -1) {
            collection = tooltipContent
                    ? collectionsService.findCollection(collectionName, sortOrder, filters, limit, WidgetConstants.UNBOUNDED_LIMIT)
                    : collectionsService.findCollection(collectionName, sortOrder, filters, 0, limit);
            if(!tooltipContent){
            int collectionCount = collectionsService.findCollection(collectionName, sortOrder, filters).size();
            nodeConfig.setElementsCount(collectionCount);
            }
        }
        if(collection == null){
            return;
        }
        SelectionPatternConfig selectionPatternConfig = nodeConfig.getSelectionPatternConfig();
        Matcher matcher = FormatHandler.pattern.matcher(selectionPatternConfig.getValue());
        PopupTitlesHolder popupTitlesHolder = nodeConfig.getPopupTitlesHolder();

        for (IdentifiableObject identifiableObject : collection) {
            String representation = formatHandler.format(identifiableObject, matcher, formattingConfig);
            HierarchyBrowserItem item = new HierarchyBrowserItemBuilder()
                    .setId(identifiableObject.getId())
                    .setStringRepresentation(representation)
                    .setPopupTitle(popupTitlesHolder.getTitleExistingObject())
                    .setDisplayAsHyperlinks(WidgetUtil.isDisplayingHyperlinks(nodeConfig.getDisplayValuesAsLinksConfig()))
                    .createHierarchyBrowserItem();
            items.add(item);
        }

    }

    private Boolean isSingleChoice(DomainObject root, SingleChoiceConfig singleChoiceConfig){
        if(singleChoiceConfig == null){
            return null;
        }
        if(singleChoiceConfig.isSingleChoice() == null){
            return compareField(root, singleChoiceConfig.getParentObjectFieldConfig());
        }

        return  singleChoiceConfig.isSingleChoice();

    }

    private boolean compareField(DomainObject root, ParentObjectFieldConfig parentObjectFieldConfig){
        if(root == null || parentObjectFieldConfig == null){
            return false;
        }
        Value realValue = root.getValue(parentObjectFieldConfig.getName()); //TODO add complex filed pa support
        if(realValue == null){
           return false;
        }
        Value parsedValue = literalFieldValueParser.textToValue(parentObjectFieldConfig.getValue(), realValue.getFieldType());
        return realValue.equals(parsedValue);
    }

    private void fillNodeConfigs(DomainObject root, Map<String, NodeCollectionDefConfig> collectionNameNodeMap){
        for (Map.Entry<String, NodeCollectionDefConfig> entry : collectionNameNodeMap.entrySet()) {
            NodeCollectionDefConfig nodeCollectionDefConfig = entry.getValue();
            LinkedFormMappingConfig mappingConfig = nodeCollectionDefConfig.getLinkedFormMappingConfig();
            if(mappingConfig != null) {
            LinkedFormConfig formConfig = nodeCollectionDefConfig.getLinkedFormMappingConfig().getLinkedFormConfigs().get(0);
            PopupTitlesHolder popupTitlesHolder = titleBuilder.buildPopupTitles(formConfig, root);
            nodeCollectionDefConfig.setPopupTitlesHolder(popupTitlesHolder);
            }
            FillParentOnAddConfig fillParentOnAddConfig = nodeCollectionDefConfig.getFillParentOnAddConfig();
            String domainObjectType = nodeCollectionDefConfig.getDomainObjectType();
            boolean displayingCreateButton = fillParentOnAddConfig == null
                    ? accessVerificationService.isCreatePermitted(domainObjectType)
                    : accessVerificationService.isCreateChildPermitted(domainObjectType, root.getId());
            nodeCollectionDefConfig.setDisplayCreateButton(displayingCreateButton);
        }

    }

    private void correctChosenItems(List<Id> selectedIds, List<HierarchyBrowserItem> chosenItems) {
        List<Id> chosenItemsIds = getChosenItemsIds(chosenItems);
        for (Id selectedId : selectedIds) {
            if (!chosenItemsIds.contains(selectedId)) {
                HierarchyBrowserItem item = new HierarchyBrowserItemBuilder()
                .setStringRepresentation(WidgetConstants.REPRESENTATION_STUB)
                .setNodeCollectionName(WidgetConstants.UNDEFINED_COLLECTION_NAME)
                .setId(selectedId)
                        .createHierarchyBrowserItem();
                chosenItems.add(item);
            }
        }

    }

    private List<Id> getChosenItemsIds(List<HierarchyBrowserItem> chosenItems) {
        List<Id> itemsIds = new ArrayList<>();
        for (HierarchyBrowserItem chosenItem : chosenItems) {
            Id id = chosenItem.getId();
            itemsIds.add(id);
        }
        return itemsIds;
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
        Id rootId = nodeContentRequest.getRootId();
        DomainObject root = rootId == null ? null : crudService.find(rootId);
        for (NodeCollectionDefConfig nodeCollectionDefConfig : nodeCollectionDefConfigs) {

            String collectionName = nodeCollectionDefConfig.getCollection();
            Matcher selectionMatcher = FormatHandler.pattern.matcher(nodeCollectionDefConfig.getSelectionPatternConfig()
                    .getValue());
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
            SelectionFiltersConfig selectionFiltersConfig = nodeCollectionDefConfig.getSelectionFiltersConfig();
            filterBuilder.prepareSelectionFilters(selectionFiltersConfig, null, filters);
            IdentifiableObjectCollection collection = collectionsService.
                    findCollection(collectionName, sortOrder, filters, offset, numberOfItems);
            List<NodeCollectionDefConfig> children = nodeCollectionDefConfig.getNodeCollectionDefConfigs();
            boolean mayHaveChildren = !children.isEmpty();
            FormattingConfig formattingConfig = nodeContentRequest.getFormattingConfig();
            PopupTitlesHolder titlesHolder = nodeCollectionDefConfig.getPopupTitlesHolder();
            Boolean singleChoice = isSingleChoice(root, nodeCollectionDefConfig.getSingleChoiceConfig());
            for (IdentifiableObject identifiableObject : collection) {
                String representation = formatHandler.format(identifiableObject, selectionMatcher, formattingConfig);
                Id id = identifiableObject.getId();
                String titleExistingObject = titlesHolder == null ? null : titlesHolder.getTitleExistingObject();
                Boolean displayAsHyperlinks = WidgetUtil.isDisplayingHyperlinks(nodeCollectionDefConfig
                        .getDisplayValuesAsLinksConfig());
                HierarchyBrowserItem item = new HierarchyBrowserItemBuilder()
                        .setId(id)
                        .setStringRepresentation(representation)
                        .setPopupTitle(titleExistingObject)
                        .setDisplayAsHyperlinks(displayAsHyperlinks)
                        .setSingleChoice(singleChoice)
                        .setNodeCollectionName(collectionName)
                        .setMayHaveChildren(mayHaveChildren)
                        .createHierarchyBrowserItem();

                if (chosenIds.contains(id)) {
                    item.setChosen(true);
                }
                item.setMayHaveChildren(mayHaveChildren);
                items.add(item);
            }

        }
        NodeContentResponse nodeContent = new NodeContentResponse();
        nodeContent.setNodeCollectionDefConfigs(nodeCollectionDefConfigs);
        nodeContent.setNodeContent(items);
        nodeContent.setDomainObjectTypesAndTitles(domainObjectTypesAndTitles);
        nodeContent.setParentCollectionName(rootNodeCollectionDefConfig.getCollection());
        nodeContent.setSelective(nodeContentRequest.isSelective());
        nodeContent.setParentId(parentId);
        return nodeContent;
    }

    private List<Filter> addParentFilter(Id parentId, String filterByParentName, List<Filter> filters) {
        Filter parentFilter = FilterBuilderUtil.prepareReferenceFilter(parentId, filterByParentName);
        filters.add(parentFilter);
        return filters;
    }

    private List<Filter> addIncludeIdsFilter(List<Id> includedIds, List<Filter> filters) {
        Set<Id> idsIncluded = new HashSet<Id>(includedIds);
        Filter idsIncludedFilter = FilterBuilderUtil.prepareFilter(idsIncluded, FilterBuilderUtil.INCLUDED_IDS_FILTER);
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

    public HierarchyBrowserTooltipResponse fetchWidgetItems(Dto inputParams){
        HierarchyBrowserTooltipRequest request = (HierarchyBrowserTooltipRequest) inputParams;
        HierarchyBrowserConfig config = request.getHierarchyBrowserConfig();
        Map<String, NodeCollectionDefConfig> collectionNameNodeMap = new HashMap<>();
        NodeCollectionDefConfig nodeConfig = config.getNodeCollectionDefConfig();
        String firstCollectionName = nodeConfig.getCollection();
        collectionNameNodeMap.put(firstCollectionName, nodeConfig);
        fillCollectionNameNodeMap(nodeConfig, collectionNameNodeMap);
        Set<String> collectionNames = collectionNameNodeMap.keySet();
        ArrayList<Id> selectedIds = request.getSelectedIds();
        FormattingConfig formattingConfig = config.getFormattingConfig();
        ArrayList<HierarchyBrowserItem> chosenItems = new ArrayList<HierarchyBrowserItem>();
        for (String collectionName : collectionNames) {
            generateChosenItems(collectionName, collectionNameNodeMap, formattingConfig, selectedIds, chosenItems, true);

        }
        HierarchyBrowserTooltipResponse response = new HierarchyBrowserTooltipResponse(chosenItems, selectedIds);
        return response;
    }

    private boolean hasSelectionFilters(String collectionName, Map<String, NodeCollectionDefConfig> collectionNameNodeMap) {
        NodeCollectionDefConfig nodeCollectionDefConfig = collectionNameNodeMap.get(collectionName);
        SelectionFiltersConfig selectionFiltersConfig = nodeCollectionDefConfig.getSelectionFiltersConfig();
        List<AbstractFilterConfig> list = selectionFiltersConfig == null ? null : selectionFiltersConfig.getAbstractFilterConfigs();
        return list != null && !list.isEmpty();
    }

    private boolean hasNoLimit(String collectionName, Map<String, NodeCollectionDefConfig> collectionNameNodeMap) {
        NodeCollectionDefConfig nodeCollectionDefConfig = collectionNameNodeMap.get(collectionName);
        SelectionFiltersConfig selectionFiltersConfig = nodeCollectionDefConfig.getSelectionFiltersConfig();
        return selectionFiltersConfig == null || selectionFiltersConfig.getRowLimit() == 0;
    }

}
