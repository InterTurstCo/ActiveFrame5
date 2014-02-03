package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.SearchService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.gui.collection.view.*;
import ru.intertrust.cm.core.config.gui.form.widget.SearchAreaRefConfig;
import ru.intertrust.cm.core.config.gui.navigation.*;
import ru.intertrust.cm.core.gui.api.server.plugin.ActivePluginHandler;
import ru.intertrust.cm.core.gui.impl.server.util.ActionConfigBuilder;
import ru.intertrust.cm.core.gui.impl.server.util.FilterBuilder;
import ru.intertrust.cm.core.gui.impl.server.util.SortOrderBuilder;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.SaveToCSVContext;
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
public class CollectionPluginHandler extends ActivePluginHandler {
    private static final String DESCEND_ARROW = "↓";
    private static final String ASCEND_ARROW = "↑";
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
        DefaultSortCriteriaConfig sortCriteriaConfig = collectionViewerConfig.getDefaultSortCriteriaConfig();
        LinkedHashMap<String, CollectionColumnProperties> map =
                getDomainObjectFieldPropertiesMap(collectionViewConfig, sortCriteriaConfig);
        pluginData.setDomainObjectFieldPropertiesMap(map);
        List<Filter> filters = new ArrayList<Filter>();

        pluginData.setDefaultSortCriteriaConfig(sortCriteriaConfig);
        CollectionDisplayConfig collectionDisplayConfig = collectionViewConfig.getCollectionDisplayConfig();
        SortOrder order = SortOrderBuilder.getInitSortOrder(sortCriteriaConfig, collectionDisplayConfig);
        // todo не совсем верная логика. а в каком режиме обычная коллекция открывается? single choice? display chosen values?
        // todo: по-моему условие singleChoice && !displayChosenValues вполне говорит само за себя :) в следующем условии тоже
        if ((singleChoice && !displayChosenValues) || (!singleChoice && !displayChosenValues)) {

            filters = addFilterByText(collectionViewerConfig, filters);
            filters = addFilterExcludeIds(collectionViewerConfig, filters);
            ArrayList<CollectionRowItem> items = getRows(collectionName,
                    map.keySet(), 0, 70, filters, order);
            pluginData.setItems(items);
        }

        if ((singleChoice && displayChosenValues) || (!singleChoice && displayChosenValues)) {

            filters = addFilterByText(collectionViewerConfig, filters);
            ArrayList<CollectionRowItem> items = getRows(collectionName,
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
        List<ActionContext> activeContexts = new ArrayList<ActionContext>();
        activeContexts.add(new SaveToCSVContext(ActionConfigBuilder.createActionConfig("save-csv.action", "save-csv.action", "Выгрузить в CSV", "icons/icon-csv_download.png")));
        pluginData.setActionContexts(activeContexts);
        return pluginData;
    }

    public CollectionPluginData getExtendedCollectionPluginData(String collectionName, ArrayList<CollectionRowItem> items) {
        CollectionRefConfig refConfig = new CollectionRefConfig();
        refConfig.setName(collectionName);

        CollectionViewerConfig collectionViewerConfig = new CollectionViewerConfig();
        collectionViewerConfig.setCollectionRefConfig(refConfig);

        boolean singleChoice = true;
        boolean displayChosenValues = false;
        CollectionPluginData pluginData = new CollectionPluginData();
        pluginData.setSingleChoice(singleChoice);
        pluginData.setDisplayChosenValues(displayChosenValues);
        CollectionViewConfig collectionViewConfig = getViewForCurrentCollection(collectionViewerConfig, collectionName);

        LinkedHashMap<String, CollectionColumnProperties> map = getDomainObjectFieldPropertiesMap(collectionViewConfig, null);
        pluginData.setDomainObjectFieldPropertiesMap(map);
        pluginData.setItems(items);
        pluginData.setCollectionName(collectionName);
        pluginData.setDomainObjectFieldPropertiesMap(map);
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
            final CollectionViewConfig collectionViewConfig, DefaultSortCriteriaConfig sortCriteriaConfig) {
        LinkedHashMap<String, CollectionColumnProperties> columnPropertiesMap =
                new LinkedHashMap<String, CollectionColumnProperties>();
        CollectionDisplayConfig collectionDisplay = collectionViewConfig.getCollectionDisplayConfig();
        String sortedField = getSortedField(sortCriteriaConfig);
        if (collectionDisplay != null) {
            List<CollectionColumnConfig> columnConfigs = collectionDisplay.getColumnConfig();
            for (CollectionColumnConfig columnConfig : columnConfigs) {
                if (!columnConfig.isHidden()) {
                    final String field = columnConfig.getField();
                    final CollectionColumnProperties properties = new CollectionColumnProperties();
                    String columnName = columnConfig.getName();
                    if (field.equalsIgnoreCase(sortedField)) {
                        columnName = markAsSorted(columnName, sortCriteriaConfig);
                    }
                    properties.addProperty(CollectionColumnProperties.NAME_KEY, columnName)
                            .addProperty(CollectionColumnProperties.TYPE_KEY, columnConfig.getType())
                            .addProperty(CollectionColumnProperties.SEARCH_FILTER_KEY, columnConfig.getSearchFilter())
                            .addProperty(CollectionColumnProperties.PATTERN_KEY, columnConfig.getPattern());
                    AscSortCriteriaConfig ascSortCriteriaConfig = columnConfig.getAscSortCriteriaConfig();
                    properties.setAscSortCriteriaConfig(ascSortCriteriaConfig);
                    DescSortCriteriaConfig descSortCriteriaConfig = columnConfig.getDescSortCriteriaConfig();
                    properties.setDescSortCriteriaConfig(descSortCriteriaConfig);
                    columnPropertiesMap.put(field, properties);

                }
            }
            return columnPropertiesMap;

        } else throw new GuiException("Collection view config has no display tags configured ");
    }

    private String markAsSorted(String columnName, DefaultSortCriteriaConfig sortCriteriaConfig) {
        SortCriterion.Order sortOrder = sortCriteriaConfig.getOrder();
        String marker = getSortedMarker(sortOrder);
        String markedColumnName = columnName + marker;
        return markedColumnName;
    }

    private String getSortedMarker(SortCriterion.Order order) {
        if (order.equals(SortCriterion.Order.DESCENDING)) {
            return DESCEND_ARROW;
        } else {
            return ASCEND_ARROW;
        }
    }

    private String getSortedField(DefaultSortCriteriaConfig sortCriteriaConfig) {
        if (sortCriteriaConfig == null) {
            return null;
        }
        String columnField = sortCriteriaConfig.getColumnField();
        return columnField;
    }

    public CollectionRowItem generateCollectionRowItem(IdentifiableObject identifiableObject, Set<String> fields) {
        CollectionRowItem item = new CollectionRowItem();
        LinkedHashMap<String, Value> row = getRowValues(identifiableObject, fields);
        item.setId(identifiableObject.getId());
        item.setRow(row);
        return item;

    }

    public ArrayList<CollectionRowItem> getRows(
            String collectionName, Set<String> fields, int offset, int count, List<Filter> filters, SortOrder sortOrder) {

        ArrayList<CollectionRowItem> items = new ArrayList<CollectionRowItem>();
        IdentifiableObjectCollection collection = collectionsService.
                findCollection(collectionName, sortOrder, filters, offset, count);
        for (IdentifiableObject identifiableObject : collection) {
            items.add(generateCollectionRowItem(identifiableObject, fields));

        }
        return items;
    }

    public ArrayList<CollectionRowItem> getSimpleSearchRows(String collectionName, Set<String> fields,
                                                            int offset, int count, List<Filter> filters, String simpleSearchQuery, String searchArea) {
        ArrayList<CollectionRowItem> items = new ArrayList<CollectionRowItem>();

        IdentifiableObjectCollection collection = searchService.search(simpleSearchQuery, searchArea, collectionName, 200);

        for (IdentifiableObject identifiableObject : collection) {
            items.add(generateCollectionRowItem(identifiableObject, fields));
        }

        return items;
    }

    public Dto generateCollectionRowItems(Dto dto) {
        // 21.01 12:50 (DB) -> if 21.01 selected -> it's a date between 21.01 00:00 and 22.01 00:00
        CollectionRowsRequest collectionRowsRequest = (CollectionRowsRequest) dto;
        ArrayList<CollectionRowItem> list;
        final String collectionName = collectionRowsRequest.getCollectionName();
        final Set<String> fields = collectionRowsRequest.getFields().keySet();
        final int offset = collectionRowsRequest.getOffset();
        final int limit = collectionRowsRequest.getLimit();
        final List<Filter> filters = transformDateFilters(collectionRowsRequest.getFilterList());
        if (collectionRowsRequest.isSortable()) {
            list = getRows(collectionName, fields, offset, limit, filters, getSortOrder(collectionRowsRequest));
        } else {
            if (collectionRowsRequest.getSimpleSearchQuery().length() > 0) {
                list = getSimpleSearchRows(collectionName, fields, offset, limit, filters,
                        collectionRowsRequest.getSimpleSearchQuery(), collectionRowsRequest.getSearchArea());
            } else {
                list = getRows(collectionName, fields, offset, limit, filters, null);
                CollectionRowItemList collectionRowItemList = new CollectionRowItemList();
                collectionRowItemList.setCollectionRows(list);
            }

        }
        CollectionRowItemList collectionRowItemList = new CollectionRowItemList();
        collectionRowItemList.setCollectionRows(list);

        return collectionRowItemList;
    }

    private ArrayList<Filter> transformDateFilters(List<Filter> filters) {
        if (filters == null || filters.isEmpty()) {
            return new ArrayList<>(0);
        }
        Calendar cal = Calendar.getInstance();
        ArrayList<Filter> result = new ArrayList<>(filters);
        for (int i = 0; i < result.size(); ++i) {
            Filter filter = result.get(i);
            final Value criterion = filter.getCriterion(0);
            if (criterion instanceof DateTimeValue) {
                cal.setTime((Date) criterion.get());
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                Date rangeStart = cal.getTime();

                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MILLISECOND, 999);
                Date rangeEnd = cal.getTime();
                Filter timestampFilter = new Filter();
                timestampFilter.setFilter(filter.getFilter());
                timestampFilter.addCriterion(0, new DateTimeValue(rangeStart));
                timestampFilter.addCriterion(1, new DateTimeValue(rangeEnd));

                result.set(i, timestampFilter);
            }
        }
        return result;
    }

    private SortOrder getSortOrder(CollectionRowsRequest request) {
        SortCriteriaConfig sortCriteriaConfig = request.getSortCriteriaConfig();
        SortOrder sortOrder = SortOrderBuilder.getComplexSortOrder(sortCriteriaConfig);
        if (sortOrder == null) {
            sortOrder = new SortOrder();
            if (request.isSortAscending()) {
                sortOrder.add(new SortCriterion(request.getSortedField(), SortCriterion.Order.ASCENDING));
            } else {
                sortOrder.add(new SortCriterion(request.getSortedField(), SortCriterion.Order.DESCENDING));
            }
        }

        return sortOrder;

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

}
