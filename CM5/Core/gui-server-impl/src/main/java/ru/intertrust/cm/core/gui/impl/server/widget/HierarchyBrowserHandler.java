package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.gui.form.widget.HierarchyBrowserConfig;
import ru.intertrust.cm.core.config.gui.form.widget.NodeCollectionDefConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionPatternConfig;
import ru.intertrust.cm.core.gui.api.server.widget.LinkEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItemList;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserWidgetState;
import ru.intertrust.cm.core.gui.model.form.widget.NodeContentMetaData;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        Pattern pattern = createDefaultRegexPattern();
        Matcher selectionMatcher = pattern.matcher(selectionPatternConfig.getValue());
        ArrayList<Id> selectedIds = context.getObjectIds();
        ArrayList<HierarchyBrowserItem> chosenItems = new ArrayList<HierarchyBrowserItem>();
        if (!selectedIds.isEmpty()) {
            List<Filter> filters = new ArrayList<Filter>();
            filters = addIncludeIdsFilter(selectedIds, filters);
            List<String> collectionNames = new ArrayList<String>();
            collectionNames = getCollectionsNames(nodeConfig, collectionNames);
            for (String collectionName : collectionNames){
                 generateChosenItemsForCollection(collectionName, filters, selectionMatcher, chosenItems);
            }
        }
        HierarchyBrowserWidgetState state = new HierarchyBrowserWidgetState();
        state.setHierarchyBrowserConfig(widgetConfig);
        state.setChosenItems(chosenItems);
        return state;
    }

    private HierarchyBrowserItem createHierarchyBrowserItem(String collectionName,
                                                            IdentifiableObject identifiableObject, Matcher matcher) {
        HierarchyBrowserItem item = new HierarchyBrowserItem();
        item.setId(identifiableObject.getId());
        item.setStringRepresentation(format(identifiableObject, matcher));
        item.setNodeCollectionName(collectionName);
        return item;
    }

    private void generateChosenItemsForCollection(String collectionName, List<Filter> filters,
                                                                             Matcher matcher, ArrayList<HierarchyBrowserItem> items) {
        IdentifiableObjectCollection collectionForFacebookStyleItems = collectionsService.
                findCollection(collectionName, null, filters, 0, 0);
        for (IdentifiableObject identifiableObject : collectionForFacebookStyleItems) {
            HierarchyBrowserItem item = createHierarchyBrowserItem(collectionName,identifiableObject, matcher);
            items.add(item);
        }

    }

    public HierarchyBrowserItemList fetchNodeContent(Dto inputParams) {
        NodeContentMetaData nodeContentMetaData = (NodeContentMetaData) inputParams;
        String collectionName = nodeContentMetaData.getCollectionName();
        Pattern pattern = createDefaultRegexPattern();
        Matcher selectionMatcher = pattern.matcher(nodeContentMetaData.getSelectionPattern());
        int numberOfItems = nodeContentMetaData.getNumberOfItemsToDisplay();
        int offset = nodeContentMetaData.getOffset();
        ArrayList<Id> chosenIds = nodeContentMetaData.getChosenIds();
        ArrayList<HierarchyBrowserItem> items = new ArrayList<HierarchyBrowserItem>();
        List<Filter> filters = new ArrayList<Filter>();
        if (nodeContentMetaData.getId() != null) {
            filters = addParentFilter(nodeContentMetaData, filters);
        }
        String inputText = nodeContentMetaData.getInputText();
        if (inputText != null && !inputText.equalsIgnoreCase("")) {
            filters =  addInputTextFilter(nodeContentMetaData.getInputTextFilterName(), inputText, filters);
        }
        IdentifiableObjectCollection collection = collectionsService.
                findCollection(collectionName, null, filters, offset, numberOfItems);
        for (IdentifiableObject identifiableObject : collection) {
            HierarchyBrowserItem item = new HierarchyBrowserItem();
            Id id = identifiableObject.getId();
            item.setId(id);
            item.setStringRepresentation(format(identifiableObject, selectionMatcher));
            item.setNodeCollectionName(collectionName);

            if (chosenIds.contains(id)) {
                item.setChosen(true);
            }
            items.add(item);
        }
        HierarchyBrowserItemList nodeContent = new HierarchyBrowserItemList();
        nodeContent.setNodeContent(items);
        nodeContent.setNodeType(collectionName);
        nodeContent.setParentId(nodeContentMetaData.getId());
        return nodeContent;
    }

    private Pattern createDefaultRegexPattern() {
        return Pattern.compile("\\{\\w+\\}");
    }

    private List<Filter> addParentFilter(NodeContentMetaData nodeContentMetaData, List<Filter> filters) {
        Id nodeId = nodeContentMetaData.getId();
        String filterName = nodeContentMetaData.getParentFilterName();
        Filter parentFilter = new Filter();
        parentFilter.setFilter(filterName);
        parentFilter.addCriterion(0, new ReferenceValue(nodeId));
        filters.add(parentFilter);
        return filters;
    }

    private List<Filter> addIncludeIdsFilter(List<Id> includeIds, List<Filter> filters) {
        List<ReferenceValue> list = new ArrayList<ReferenceValue>();
        for (Id includeId : includeIds) {
            list.add(new ReferenceValue(includeId));
        }
        IdsIncludedFilter includeIdsFilter = new IdsIncludedFilter(list);
        includeIdsFilter.setFilter("includeIds");
        filters.add(includeIdsFilter);
        return filters;
    }
    private List<Filter> addInputTextFilter(String name, String text, List<Filter> filters) {
        Filter textFilter = new Filter();
        textFilter.setFilter(name);
        textFilter.addCriterion(0, new StringValue(text + "%"));
        filters.add(textFilter);
        return filters;
    }

    private List<String> getCollectionsNames(NodeCollectionDefConfig nodeConfig, List<String> collectionNames) {
        if (nodeConfig == null) {
            return collectionNames;
        }
        String collectionName = nodeConfig.getCollection();
        collectionNames.add(collectionName);
        NodeCollectionDefConfig childNodeConfig = nodeConfig.getNodeCollectionDefConfig();
        return getCollectionsNames(childNodeConfig, collectionNames);
    }
}
