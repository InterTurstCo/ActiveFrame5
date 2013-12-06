package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionColumnConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionDisplayConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionViewConfig;
import ru.intertrust.cm.core.config.gui.form.widget.InputTextFilterConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionRefConfig;
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

    public CollectionPluginData initialize(Dto param) {
        CollectionViewerConfig collectionViewerConfig = (CollectionViewerConfig) param;
        CollectionRefConfig collectionRefConfig = collectionViewerConfig.getCollectionRefConfig();
        String collectionName = collectionRefConfig.getName();
        boolean singleChoice = collectionViewerConfig.isSingleChoice();
        boolean displayChosenValues = collectionViewerConfig.isDisplayChosenValues();
        CollectionPluginData pluginData = new CollectionPluginData();
        pluginData.setSingleChoice(singleChoice);
        pluginData.setDisplayChosenValues(displayChosenValues);
        CollectionViewConfig collectionViewConfig = findRequiredCollectionView(collectionName);
        LinkedHashMap<String, String> map = getDomainObjectFieldOnColumnNameMap(collectionViewConfig);
        pluginData.setDomainObjectFieldOnColumnNameMap(map);

        List<Filter> filters = null;

        if (tableHasSingleSelectionModelAndDoesntShowAlreadyChosenRows(singleChoice, displayChosenValues)) {
          filters = prepareFilters(collectionViewerConfig);
        }
        if (tableHasMultipleSelectionModelAndDoesntShowAlreadyChosenRows(singleChoice, displayChosenValues)) {
            filters = prepareFilters(collectionViewerConfig);
        }
        if (tableHasSingleSelectionModelAndDoesntShowAlreadyChosenRows(singleChoice, displayChosenValues)) {

        }


        ArrayList<CollectionRowItem> items = generateTableRowsForPluginInitialization(collectionName, map.keySet(), 0, 70, filters);

        pluginData.setItems(items);
        pluginData.setCollectionName(collectionName);

        return pluginData;
    }

    private Collection<CollectionViewConfig> getCollectionOfViewConfigs() {
        Collection<CollectionViewConfig> viewConfigs = configurationService.
                getConfigs(CollectionViewConfig.class);

        return viewConfigs;

    }
    private List<Filter> prepareFilters(CollectionViewerConfig collectionViewerConfig){
        List<Filter> filters = new ArrayList<Filter>();
        InputTextFilterConfig inputTextFilterConfig = collectionViewerConfig.getInputTextFilterConfig();
        if (inputTextFilterConfig == null) {
              return filters;
        }
        String name = inputTextFilterConfig.getName();
        String text = inputTextFilterConfig.getValue();
        Filter filterByText = prepareInputTextFilter(name, text);
        Filter filterByIds = prepareExcludeIdsFilter(collectionViewerConfig.getExcludedIds());

        filters.add(filterByText);
        filters.add(filterByIds);
        return filters;
    }

    private CollectionViewConfig findRequiredCollectionView(String collection) {

        Collection<CollectionViewConfig> collectionViewConfigs = getCollectionOfViewConfigs();
        for (CollectionViewConfig collectionViewConfig : collectionViewConfigs) {

            if (collectionViewConfig.getCollection().equalsIgnoreCase(collection)) {
                return collectionViewConfig;
            }
        }
        throw new GuiException("Couldn't find for collection with name '" + collection + "'");
    }

    private LinkedHashMap<String, Value> getRowValues(IdentifiableObject identifiableObject, Set<String> columnFields) {

        LinkedHashMap<String, Value> values = new LinkedHashMap<String, Value>();
        for (String field : columnFields) {
            Value value;
            if ("id".equalsIgnoreCase(field)) {
                value = new StringValue(identifiableObject.getId().toStringRepresentation());
            } else {
                value = identifiableObject.getValue(field);

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
                if (collectionColumnConfig.isHidden()) {
                    continue;
                }
                String columnName = collectionColumnConfig.getName();
                String columnField = collectionColumnConfig.getField();
                columnNames.put(columnField, columnName);
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

    public Dto generateCollectionRowItems(Dto dto){
        CollectionRowsRequest collectionRowsRequest = (CollectionRowsRequest) dto;
        ArrayList<CollectionRowItem> list = generateTableRowsForPluginInitialization(collectionRowsRequest.getCollectionName(),
                collectionRowsRequest.getFields().keySet(), collectionRowsRequest.getOffset(),
                collectionRowsRequest.getLimit(), null);
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

        return excludeIdsFilter;
    }
    private Filter prepareIncludeIdsFilter(List<Id> icludeIds) {

        List<ReferenceValue> list = new ArrayList<ReferenceValue>();
        for (Id includeId :icludeIds) {
            list.add(new ReferenceValue(includeId));
        }
        IdsExcludedFilter includeIdsFilter = new IdsExcludedFilter(list);
        return includeIdsFilter;
    }
    private boolean tableHasSingleSelectionModelAndDoesntShowAlreadyChosenRows(boolean singleChoice, boolean displayChosenValues) {
        return singleChoice && !displayChosenValues;
    }
    private boolean tableHasSingleSelectionModelAndShowsAlreadyChosenRows(boolean singleChoice, boolean displayChosenValues) {
        return singleChoice && displayChosenValues;
    }
    private boolean tableHasMultipleSelectionModelAndDoesntShowAlreadyChosenRows(boolean singleChoice, boolean displayChosenValues) {
        return !singleChoice && !displayChosenValues;
    }
    private boolean tableHasMultiplySelectionModelAndShowsAlreadyChosenRows(boolean singleChoice, boolean displayChosenValues) {
        return !singleChoice && displayChosenValues;
    }
   private CollectionPluginData prepareDynamicContentOfCollectionPluginData(CollectionPluginData collectionPluginData){
        return collectionPluginData;
   }
}
