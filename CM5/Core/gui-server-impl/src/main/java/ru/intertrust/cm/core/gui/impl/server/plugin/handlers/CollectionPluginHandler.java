package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.SearchService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionColumnConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionDisplayConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionViewConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SearchAreaRefConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionRefConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionViewRefConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionViewerConfig;
import ru.intertrust.cm.core.config.gui.navigation.SortCriterionConfig;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.impl.server.util.FilterBuilder;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.widget.CollectionRowItemList;
import ru.intertrust.cm.core.gui.model.plugin.CollectionPluginData;
import ru.intertrust.cm.core.gui.model.plugin.CollectionRowItem;
import ru.intertrust.cm.core.gui.model.plugin.CollectionRowsRequest;

import java.util.*;


/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */
@ComponentName("collection.plugin")
public class CollectionPluginHandler extends PluginHandler {

    @Autowired
    CollectionsService collectionsService;

    @Autowired
    ConfigurationService configurationService;

    @Autowired
    SearchService searchService;

    public CollectionPluginData initialize(Dto param) {
        CollectionViewerConfig collectionViewerConfig = (CollectionViewerConfig) param;
        CollectionRefConfig collectionRefConfig = collectionViewerConfig.getCollectionRefConfig();
        String collectionName = collectionRefConfig.getName();
        boolean singleChoice = collectionViewerConfig.isSingleChoice();
        boolean displayChosenValues = collectionViewerConfig.isDisplayChosenValues();
        CollectionPluginData pluginData = new CollectionPluginData();
        pluginData.setSingleChoice(singleChoice);
        pluginData.setDisplayChosenValues(displayChosenValues);
        CollectionViewConfig collectionViewConfig = getViewForCurrentCollection(collectionViewerConfig, collectionName);
        collectionViewerConfig.getSearchAreaRefConfig();

        LinkedHashMap<String, CollectionColumnProperties> map =
                getDomainObjectFieldPropertiesMap(collectionViewConfig);
        pluginData.setDomainObjectFieldPropertiesMap(map);
        List<Filter> filters = new ArrayList<Filter>();
         SortOrder order = getSortOrder(collectionViewerConfig);
        // todo не совсем верная логика. а в каком режиме обычная коллекция открывается? single choice? display chosen values?
        // todo: по-моему условие singleChoice && !displayChosenValues вполне говорит само за себя :) в следующем условии тоже
        if ((singleChoice && !displayChosenValues) || (!singleChoice && !displayChosenValues)) {

            filters = addFilterByText(collectionViewerConfig, filters);
            filters = addFilterExcludeIds(collectionViewerConfig, filters);
            ArrayList<CollectionRowItem> items = generateTableRowsForPluginInitialization(collectionName,
                    map.keySet(), 0, 70, filters, order);
            pluginData.setItems(items);
        }

        if ((singleChoice && displayChosenValues) || (!singleChoice && displayChosenValues)) {

            filters = addFilterByText(collectionViewerConfig, filters);
            ArrayList<CollectionRowItem> items = generateTableRowsForPluginInitialization(collectionName,
                    map.keySet(), 0, 70, filters, order);
            List<Id> chosenIds = collectionViewerConfig.getExcludedIds();
            pluginData.setIndexesOfSelectedItems(getListOfAlreadyChosenItems(chosenIds, items));
            pluginData.setItems(items);
        }

        pluginData.setCollectionName(collectionName);
        if (collectionViewerConfig.getSearchAreaRefConfig() != null) {
            pluginData.setSearchArea(collectionViewerConfig.getSearchAreaRefConfig().getName());
        } else {
            pluginData.setSearchArea("");
        }

        return pluginData;
    }

    private Collection<CollectionViewConfig> getCollectionOfViewConfigs() {
        Collection<CollectionViewConfig> viewConfigs = configurationService.
                getConfigs(CollectionViewConfig.class);

        return viewConfigs;

    }

    private List<Filter> addFilterByText(CollectionViewerConfig collectionViewerConfig, List<Filter> filters) {

        SearchAreaRefConfig searchAreaRefConfig = collectionViewerConfig.getSearchAreaRefConfig();
        if (searchAreaRefConfig == null) {
            return filters;
        }
        String name = searchAreaRefConfig.getName();
        String text = searchAreaRefConfig.getValue();
        if (text == null || text.trim().isEmpty()) {
            return filters;
        }
        Filter filterByText = prepareInputTextFilter(name, text);
        filters.add(filterByText);

        return filters;
    }

    private List<Filter> addFilterExcludeIds(CollectionViewerConfig collectionViewerConfig, List<Filter> filters) {
       /* SearchAreaRefConfig searchAreaRefConfig = collectionViewerConfig.getSearchAreaRefConfig();
        if (searchAreaRefConfig == null) {
            return filters;
        }*/
        List<Id> excludedIds = collectionViewerConfig.getExcludedIds();
        Set<Id> idsExcluded = new HashSet<Id>(excludedIds);
        Filter filterExcludeIds = FilterBuilder.prepareFilter(idsExcluded, "idsExcluded");
        filters.add(filterExcludeIds);
        return filters;
    }

    private CollectionViewConfig getViewForCurrentCollection(CollectionViewerConfig collectionViewerConfig,
                                                             String collectionName) {
        CollectionViewRefConfig collectionViewRefConfig = collectionViewerConfig.getCollectionViewRefConfig();
        if (collectionViewRefConfig == null) {
            return findRequiredCollectionView(collectionName);
        }
        String viewName = collectionViewRefConfig.getName();
        return findRequiredCollectionViewByName(viewName);

    }

    private CollectionViewConfig findRequiredCollectionView(String collection) {

        Collection<CollectionViewConfig> collectionViewConfigs = getCollectionOfViewConfigs();
        for (CollectionViewConfig collectionViewConfig : collectionViewConfigs) {

            if (collectionViewConfig.getCollection().equalsIgnoreCase(collection)) {
                return collectionViewConfig;
            }
        }
        throw new GuiException("Couldn't find view for collection with name '" + collection + "'");
    }

    private CollectionViewConfig findRequiredCollectionViewByName(String viewName) {
        Collection<CollectionViewConfig> collectionViewConfigs = getCollectionOfViewConfigs();
        for (CollectionViewConfig collectionViewConfig : collectionViewConfigs) {

            if (collectionViewConfig.getName().equalsIgnoreCase(viewName)) {
                return collectionViewConfig;
            }
        }
        throw new GuiException("Couldn't find collection view with name '" + viewName + "'");
    }


    private LinkedHashMap<String, Value> getRowValues(IdentifiableObject identifiableObject, Set<String> columnFields) {

        LinkedHashMap<String, Value> values = new LinkedHashMap<String, Value>();
        for (String field : columnFields) {
            Value value;
            if ("id".equalsIgnoreCase(field)) {
                value = new StringValue(identifiableObject.getId().toStringRepresentation());
            } else {
                value = identifiableObject.getValue(field.toLowerCase());

            }
            values.put(field, value);

        }
        return values;

    }

    private LinkedHashMap<String, CollectionColumnProperties> getDomainObjectFieldPropertiesMap(
            final CollectionViewConfig collectionViewConfig) {
        LinkedHashMap<String, CollectionColumnProperties> columnPropertiesMap =
                new LinkedHashMap<String, CollectionColumnProperties>();
        CollectionDisplayConfig collectionDisplay = collectionViewConfig.getCollectionDisplayConfig();

        if (collectionDisplay != null) {
            List<CollectionColumnConfig> columnConfigs = collectionDisplay.getColumnConfig();
            for (CollectionColumnConfig columnConfig : columnConfigs) {
                if (!columnConfig.isHidden()) {
                    final String field = columnConfig.getField();
                    final CollectionColumnProperties properties = new CollectionColumnProperties();
                    properties.addProperty(CollectionColumnProperties.NAME_KEY, columnConfig.getName())
                            .addProperty(CollectionColumnProperties.TYPE_KEY, columnConfig.getType())
                            .addProperty(CollectionColumnProperties.SEARCH_FILTER_KEY, columnConfig.getSearchFilter())
                            .addProperty(CollectionColumnProperties.PATTERN_KEY, columnConfig.getPattern());
                    columnPropertiesMap.put(field, properties);
                }
            }
            return columnPropertiesMap;

        } else throw new GuiException("Collection view config has no display tags configured ");
    }

    private CollectionRowItem generateCollectionRowItem(IdentifiableObject identifiableObject, Set<String> fields) {
        CollectionRowItem item = new CollectionRowItem();
        LinkedHashMap<String, Value> row = getRowValues(identifiableObject, fields);
        item.setId(identifiableObject.getId());
        item.setRow(row);
        return item;

    }

    public ArrayList<CollectionRowItem> generateTableRowsForPluginInitialization
            (String collectionName, Set<String> fields, int offset, int count, List<Filter> filters, SortOrder sortOrder) {
        ArrayList<CollectionRowItem> items = new ArrayList<CollectionRowItem>();
        IdentifiableObjectCollection collection = collectionsService.
                findCollection(collectionName, sortOrder, filters, offset, count);
        for (IdentifiableObject identifiableObject : collection) {
            items.add(generateCollectionRowItem(identifiableObject, fields));

        }
        return items;
    }

    public ArrayList<CollectionRowItem> generateTableRowForSimpleSearch(String collectionName, Set<String> fields,
                                                                        int offset, int count, List<Filter> filters, String simpleSearchQuery, String searchArea) {
        ArrayList<CollectionRowItem> items = new ArrayList<CollectionRowItem>();

        IdentifiableObjectCollection collection = searchService.search(simpleSearchQuery, searchArea, collectionName, 200);

        for (IdentifiableObject identifiableObject : collection) {
            items.add(generateCollectionRowItem(identifiableObject, fields));
        }

        return items;
    }

    public ArrayList<CollectionRowItem> generateSortTableRowsForPluginInitialization
            (String collectionName, Set<String> fields, int offset, int count, List<Filter> filters, String field, boolean sortableColumn) {
        ArrayList<CollectionRowItem> items = new ArrayList<CollectionRowItem>();
        SortCriterion.Order order;
        if (sortableColumn) {
            order = SortCriterion.Order.ASCENDING;
        } else {
            order = SortCriterion.Order.DESCENDING;
        }

        SortOrder sortOrder = new SortOrder();
        sortOrder.add(new SortCriterion(field, order));

        IdentifiableObjectCollection collection = collectionsService.
                findCollection(collectionName, sortOrder, filters, offset, count);
        for (IdentifiableObject identifiableObject : collection) {
            items.add(generateCollectionRowItem(identifiableObject, fields));
        }
        return items;
    }

    public Dto generateCollectionRowItems(Dto dto) {
        CollectionRowsRequest collectionRowsRequest = (CollectionRowsRequest) dto;
        ArrayList<CollectionRowItem> list;
        if (((CollectionRowsRequest) dto).isSortable()) {
            list = generateSortTableRowsForPluginInitialization(collectionRowsRequest.getCollectionName(),
                    collectionRowsRequest.getFields().keySet(), collectionRowsRequest.getOffset(),
                    collectionRowsRequest.getLimit(), collectionRowsRequest.getFilterList(), ((CollectionRowsRequest) dto).getField(),
                    ((CollectionRowsRequest) dto).isSortType());
        } else {
            if (collectionRowsRequest.getSimpleSearchQuery().length() > 0) {
                list = generateTableRowForSimpleSearch(collectionRowsRequest.getCollectionName(),
                        collectionRowsRequest.getFields().keySet(), collectionRowsRequest.getOffset(),
                        collectionRowsRequest.getLimit(), collectionRowsRequest.getFilterList(),
                        collectionRowsRequest.getSimpleSearchQuery(), collectionRowsRequest.getSearchArea());
            } else {
                list = generateTableRowsForPluginInitialization(
                        collectionRowsRequest.getCollectionName(),
                        collectionRowsRequest.getFields().keySet(), collectionRowsRequest.getOffset(),
                        collectionRowsRequest.getLimit(), collectionRowsRequest.getFilterList(), null);
                CollectionRowItemList collectionRowItemList = new CollectionRowItemList();
                collectionRowItemList.setCollectionRows(list);
            }

        }
        CollectionRowItemList collectionRowItemList = new CollectionRowItemList();
        collectionRowItemList.setCollectionRows(list);

        return collectionRowItemList;
    }

    private Filter prepareInputTextFilter(String name, String text) {
        Filter textFilter = new Filter();
        textFilter.setFilter(name);
        textFilter.addCriterion(0, new StringValue(text + "%"));
        return textFilter;
    }

    private ArrayList<Integer> getListOfAlreadyChosenItems(List<Id> chosenIds, List<CollectionRowItem> itemsForClient) {
        ArrayList<Integer> indexesOfChosenItems = new ArrayList<Integer>();
        for (Id id : chosenIds) {
            for (int i = 0; i < itemsForClient.size(); i++) {
                if (id.equals(itemsForClient.get(i).getId())) {
                    indexesOfChosenItems.add(i);
                }
            }
        }
        return indexesOfChosenItems;
    }

    private SortOrder getSortOrder(CollectionViewerConfig collectionViewerConfig) {
        SortOrder sortOrder = new SortOrder();
        List<SortCriterionConfig> sortCriterions = collectionViewerConfig.getSortCriteriaConfig().getSortCriterionConfigs();
        for (SortCriterionConfig criterionConfig : sortCriterions) {
            String field = criterionConfig.getField();
            SortCriterion.Order order = criterionConfig.getOrder();
            sortOrder.add(new SortCriterion(field, order));
        }
        return sortOrder;
    }
}
