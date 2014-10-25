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
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.CreatedObjectConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.CreatedObjectsConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.LinkedFormMappingConfig;
import ru.intertrust.cm.core.config.gui.navigation.DefaultSortCriteriaConfig;
import ru.intertrust.cm.core.gui.api.server.plugin.FilterBuilder;
import ru.intertrust.cm.core.gui.api.server.plugin.LiteralFieldValueParser;
import ru.intertrust.cm.core.gui.api.server.widget.FieldExtractor;
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

    @Autowired
    private FieldExtractor fieldExtractor;

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
        if (limit == -1 && !tooltipContent) {
            collection = collectionsService.findCollection(collectionName, sortOrder, filters);

        } else if (limit != -1) {
            collection = tooltipContent
                    ? collectionsService.findCollection(collectionName, sortOrder, filters, limit, WidgetConstants.UNBOUNDED_LIMIT)
                    : collectionsService.findCollection(collectionName, sortOrder, filters, 0, limit);
            if (!tooltipContent) {
                int collectionCount = collectionsService.findCollection(collectionName, sortOrder, filters).size();
                nodeConfig.setElementsCount(collectionCount);
            }
        }
        if (collection == null) {
            return;
        }
        SelectionPatternConfig selectionPatternConfig = nodeConfig.getSelectionPatternConfig();
        Matcher matcher = FormatHandler.pattern.matcher(selectionPatternConfig.getValue());

        for (IdentifiableObject identifiableObject : collection) {
            String representation = formatHandler.format(identifiableObject, matcher, formattingConfig);
            HierarchyBrowserItem item = createItem(identifiableObject, nodeConfig, representation, true, false);
            items.add(item);
        }

    }

    private HierarchyBrowserItem createItem(IdentifiableObject identifiableObject, NodeCollectionDefConfig nodeConfig,
                                            String representation, boolean chosen, Boolean singleChoice) {
        Id id = identifiableObject.getId();
        String domainObjectType = crudService.getDomainObjectType(id);
        PopupTitlesHolder popupTitlesHolder = nodeConfig.getDoTypeTitlesMap().get(domainObjectType);
        String popupTitle = popupTitlesHolder == null ? null : popupTitlesHolder.getTitleExistingObject();
        List<NodeCollectionDefConfig> children = nodeConfig.getNodeCollectionDefConfigs();
        boolean mayHaveChildren = !children.isEmpty();
        Boolean displayAsHyperlinks = WidgetUtil.isDisplayingHyperlinks(nodeConfig.getDisplayValuesAsLinksConfig());
        HierarchyBrowserItem item = new HierarchyBrowserItemBuilder()
                .setId(identifiableObject.getId())
                .setDomainObjectType(domainObjectType)
                .setStringRepresentation(representation)
                .setPopupTitle(popupTitle)
                .setChosen(chosen)
                .setSingleChoice(singleChoice)
                .setNodeCollectionName(nodeConfig.getCollection())
                .setMayHaveChildren(mayHaveChildren)
                .setDisplayAsHyperlinks(displayAsHyperlinks)
                .setSelective(nodeConfig.isSelective())
                .createHierarchyBrowserItem();
        return item;
    }

    private Boolean isSingleChoice(DomainObject root, SingleChoiceConfig singleChoiceConfig) {
        if (singleChoiceConfig == null) {
            return null;
        }
        if (singleChoiceConfig.isSingleChoice() == null) {
            return compareField(root, singleChoiceConfig.getParentObjectFieldConfig());
        }

        return singleChoiceConfig.isSingleChoice();

    }

    private boolean compareField(DomainObject root, ParentObjectFieldConfig parentObjectFieldConfig) {
        if (root == null || parentObjectFieldConfig == null) {
            return false;
        }
        Value realValue = fieldExtractor.extractField(root, parentObjectFieldConfig.getName());
        if (realValue == null) {
            return false;
        }
        Value parsedValue = literalFieldValueParser.textToValue(parentObjectFieldConfig.getValue(), realValue.getFieldType());
        return realValue.equals(parsedValue);
    }

    private void fillNodeConfigs(DomainObject root, Map<String, NodeCollectionDefConfig> collectionNameNodeMap) {
        for (Map.Entry<String, NodeCollectionDefConfig> entry : collectionNameNodeMap.entrySet()) {
            NodeCollectionDefConfig nodeCollectionDefConfig = entry.getValue();
            LinkedFormMappingConfig mappingConfig = nodeCollectionDefConfig.getLinkedFormMappingConfig();
            Map<String, PopupTitlesHolder> doTypeTitlesMap = createTitlesMap(root, mappingConfig);
            nodeCollectionDefConfig.setDoTypeTitlesMap(doTypeTitlesMap);
            FillParentOnAddConfig fillParentOnAddConfig = nodeCollectionDefConfig.getFillParentOnAddConfig();
            CreatedObjectsConfig createdObjectsConfig = nodeCollectionDefConfig.getCreatedObjectsConfig();
            Map<String, String> accessedTypesMap = createAccessedTypesMap(root, createdObjectsConfig, fillParentOnAddConfig);
            boolean displayingCreateButton = nodeCollectionDefConfig.isDisplayingCreateButton();
            nodeCollectionDefConfig.setDisplayCreateButton(displayingCreateButton && !accessedTypesMap.isEmpty());
        }

    }

    private Map<String, PopupTitlesHolder> createTitlesMap(DomainObject root, LinkedFormMappingConfig mappingConfig) {
        Map<String, PopupTitlesHolder> result = new HashMap<>();
        if (mappingConfig != null) {
            List<LinkedFormConfig> linkedFormConfigs = mappingConfig.getLinkedFormConfigs();
            if (WidgetUtil.isNotEmpty(linkedFormConfigs)) {
                for (LinkedFormConfig linkedFormConfig : linkedFormConfigs) {
                    PopupTitlesHolder popupTitlesHolder = titleBuilder.buildPopupTitles(linkedFormConfig, root);
                    result.put(linkedFormConfig.getDomainObjectType(), popupTitlesHolder);
                }
            }
        }
        return result;
    }

    private Map<String, String> createAccessedTypesMap(DomainObject root, CreatedObjectsConfig createdObjectsConfig,
                                                       FillParentOnAddConfig fillParentOnAddConfig) {
        Map<String, String> result = new HashMap<>();
        if (createdObjectsConfig != null) {
            List<CreatedObjectConfig> createdObjectConfigs = createdObjectsConfig.getCreateObjectConfigs();
            if (WidgetUtil.isNotEmpty(createdObjectConfigs)) {
                for (CreatedObjectConfig createdObjectConfig : createdObjectConfigs) {
                    String domainObjectType = createdObjectConfig.getDomainObjectType();
                    boolean displayingCreateButton = fillParentOnAddConfig == null
                            ? accessVerificationService.isCreatePermitted(domainObjectType)
                            : accessVerificationService.isCreateChildPermitted(domainObjectType, root.getId());
                    if (displayingCreateButton) {
                        result.put(domainObjectType, createdObjectConfig.getText());
                    }
                }
            }
        }

        return result;
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
        Id rootId = nodeContentRequest.getRootId();
        DomainObject root = rootId == null ? null : crudService.find(rootId);
        for (NodeCollectionDefConfig nodeConfig : nodeCollectionDefConfigs) {

            String collectionName = nodeConfig.getCollection();
            Matcher selectionMatcher = FormatHandler.pattern.matcher(nodeConfig.getSelectionPatternConfig()
                    .getValue());
            List<Filter> filters = new ArrayList<Filter>();
            if (parentId != null) {
                filters = addParentFilter(parentId, nodeConfig.getParentFilter(), filters);
            }
            String inputText = nodeContentRequest.getInputText();
            if (inputText != null && !inputText.equalsIgnoreCase("")) {
                String inputTextFilterName = nodeConfig.getInputTextFilterConfig().getName();
                filters = addInputTextFilter(inputTextFilterName, inputText, filters);
            }
            DefaultSortCriteriaConfig sortCriteriaConfig = nodeConfig.getDefaultSortCriteriaConfig();
            SortOrder sortOrder = SortOrderBuilder.getSimpleSortOrder(sortCriteriaConfig);
            SelectionFiltersConfig selectionFiltersConfig = nodeConfig.getSelectionFiltersConfig();
            filterBuilder.prepareSelectionFilters(selectionFiltersConfig, null, filters);
            IdentifiableObjectCollection collection = collectionsService.
                    findCollection(collectionName, sortOrder, filters, offset, numberOfItems);
            FormattingConfig formattingConfig = nodeContentRequest.getFormattingConfig();
            Boolean singleChoice = isSingleChoice(root, nodeConfig.getSingleChoiceConfig());
            for (IdentifiableObject identifiableObject : collection) {
                boolean chosen = chosenIds.contains(identifiableObject.getId());
                String representation = formatHandler.format(identifiableObject, selectionMatcher, formattingConfig);
                HierarchyBrowserItem item = createItem(identifiableObject, nodeConfig, representation,
                        chosen, singleChoice);
                items.add(item);
            }

        }
        NodeContentResponse nodeContent = new NodeContentResponse();
        nodeContent.setNodeCollectionDefConfigs(nodeCollectionDefConfigs);
        nodeContent.setNodeContent(items);
        nodeContent.setParentCollectionName(rootNodeCollectionDefConfig.getCollection());
        nodeContent.setParentId(parentId);
        return nodeContent;
    }

    private List<Filter> addParentFilter(Id parentId, String filterByParentName, List<Filter> filters) {
        Filter parentFilter = FilterBuilderUtil.prepareReferenceFilter(parentId, filterByParentName);
        filters.add(parentFilter);
        return filters;
    }

    private List<Filter> addIncludeIdsFilter(List<Id> includedIds, List<Filter> filters) {
        Filter idsIncludedFilter = FilterBuilderUtil.prepareFilter(new HashSet<Id>(includedIds), FilterBuilderUtil.INCLUDED_IDS_FILTER);
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

    public HierarchyBrowserTooltipResponse fetchWidgetItems(Dto inputParams) {
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
