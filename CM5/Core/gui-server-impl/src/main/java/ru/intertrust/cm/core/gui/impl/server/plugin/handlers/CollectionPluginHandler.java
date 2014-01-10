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
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
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

        LinkedHashMap<String, String> map = getDomainObjectFieldOnColumnNameMap(collectionViewConfig);
        pluginData.setDomainObjectFieldOnColumnNameMap(map);
        HashMap<String, String> fieldMap = new HashMap<String, String>();
        HashMap<String, String> fieldMapDisplay = new HashMap<String, String>();
        HashMap<String, String> fieldFilter = new HashMap<String, String>();



        List<CollectionColumnConfig> config = collectionViewConfig.getCollectionDisplayConfig().getColumnConfig();
        for (int i = 0; i < config.size(); i++) {
            if (!collectionViewConfig.getCollectionDisplayConfig().getColumnConfig().get(i).isHidden()) {
                fieldMap.put(collectionViewConfig.getCollectionDisplayConfig().getColumnConfig().get(i).getName(),
                        collectionViewConfig.getCollectionDisplayConfig().getColumnConfig().get(i).getField());
                fieldMapDisplay.put(collectionViewConfig.getCollectionDisplayConfig().getColumnConfig().get(i).getName(),
                        collectionViewConfig.getCollectionDisplayConfig().getColumnConfig().get(i).getType());
                fieldFilter.put(collectionViewConfig.getCollectionDisplayConfig().getColumnConfig().get(i).getField(),
                        collectionViewConfig.getCollectionDisplayConfig().getColumnConfig().get(i).getSearchFilter());

            }
        }

        List<Filter> filters = new ArrayList<Filter>();

        // todo не совсем верная логика. а в каком режиме обычная коллекция открывается? single choice? display chosen values?
        // todo: по-моему условие singleChoice && !displayChosenValues вполне говорит само за себя :) в следующем условии тоже
        if ((singleChoice && !displayChosenValues) || (!singleChoice && !displayChosenValues)) {

            filters = addFilterByText(collectionViewerConfig, filters);
            filters = addFilterExcludeIds(collectionViewerConfig, filters);
            ArrayList<CollectionRowItem> items = generateTableRowsForPluginInitialization(collectionName,
                    map.keySet(), 0, 70, filters);
            pluginData.setItems(items);
        }

        if ((singleChoice && displayChosenValues) || (!singleChoice && displayChosenValues)) {

            filters = addFilterByText(collectionViewerConfig, filters);
            ArrayList<CollectionRowItem> items = generateTableRowsForPluginInitialization(collectionName,
                    map.keySet(), 0, 70, filters);
            List<Id> chosenIds = collectionViewerConfig.getExcludedIds();
            pluginData.setIndexesOfSelectedItems(getListOfAlreadyChosenItems(chosenIds, items));
            pluginData.setItems(items);
        }

        pluginData.setCollectionName(collectionName);
        pluginData.setFieldMap(fieldMap);
        pluginData.setFieldMapDisplay(fieldMapDisplay);
        pluginData.setFieldFilter(fieldFilter);

        if (collectionViewerConfig.getSearchAreaRefConfig()!=null){
            pluginData.setSearchArea(collectionViewerConfig.getSearchAreaRefConfig().getName());
        }  else {
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
        SearchAreaRefConfig searchAreaRefConfig = collectionViewerConfig.getSearchAreaRefConfig();
        if (searchAreaRefConfig == null) {
            return filters;
        }
        Filter filterExcludeIds = prepareExcludeIdsFilter(collectionViewerConfig.getExcludedIds());
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

    private LinkedHashMap<String, String> getDomainObjectFieldOnColumnNameMap(CollectionViewConfig collectionViewConfig) {
        LinkedHashMap<String, String> columnNames = new LinkedHashMap<String, String>();
        CollectionDisplayConfig collectionDisplay = collectionViewConfig.getCollectionDisplayConfig();

        if (collectionDisplay != null) {
            List<CollectionColumnConfig> columnConfigs = collectionDisplay.getColumnConfig();
            for (CollectionColumnConfig collectionColumnConfig : columnConfigs) {
                if (!collectionColumnConfig.isHidden()) {

                    String columnName = collectionColumnConfig.getName();
                    String columnField = collectionColumnConfig.getField();
                    columnNames.put(columnField, columnName);
                }

            }
            return columnNames;

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
            (String collectionName, Set<String> fields, int offset, int count, List<Filter> filters) {
        ArrayList<CollectionRowItem> items = new ArrayList<CollectionRowItem>();
        IdentifiableObjectCollection collection = collectionsService.
                findCollection(collectionName, null, filters, offset, count);
        for (IdentifiableObject identifiableObject : collection) {
            items.add(generateCollectionRowItem(identifiableObject, fields));
        }
        return items;
    }

    public ArrayList<CollectionRowItem>  generateTableRowForSimpleSearch(String collectionName, Set<String> fields,
        int offset, int count, List<Filter> filters, String simpleSearchQuery, String searchArea){
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
        if (sortableColumn){
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
        if (((CollectionRowsRequest) dto).isSortable()){
            list = generateSortTableRowsForPluginInitialization(collectionRowsRequest.getCollectionName(),
                    collectionRowsRequest.getFields().keySet(), collectionRowsRequest.getOffset(),
                    collectionRowsRequest.getLimit(), collectionRowsRequest.getFilterList(), ((CollectionRowsRequest) dto).getField(),
                    ((CollectionRowsRequest) dto).isSotrType());
        }   else {
            if (collectionRowsRequest.getSimpleSearchQuery().length() > 0){
                list = generateTableRowForSimpleSearch(collectionRowsRequest.getCollectionName(),
                        collectionRowsRequest.getFields().keySet(), collectionRowsRequest.getOffset(),
                        collectionRowsRequest.getLimit(), collectionRowsRequest.getFilterList(),
                        collectionRowsRequest.getSimpleSearchQuery(), collectionRowsRequest.getSearchArea());
            }   else {
                list = generateTableRowsForPluginInitialization(
                        collectionRowsRequest.getCollectionName(),
                        collectionRowsRequest.getFields().keySet(), collectionRowsRequest.getOffset(),
                        collectionRowsRequest.getLimit(), collectionRowsRequest.getFilterList() );
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

    private Filter prepareExcludeIdsFilter(List<Id> excludeIds) {

        List<ReferenceValue> list = new ArrayList<ReferenceValue>();
        for (Id excludeId : excludeIds) {
            list.add(new ReferenceValue(excludeId));
        }
        IdsExcludedFilter excludeIdsFilter = new IdsExcludedFilter(list);
        excludeIdsFilter.setFilter("excludeIds");
        return excludeIdsFilter;
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
}
