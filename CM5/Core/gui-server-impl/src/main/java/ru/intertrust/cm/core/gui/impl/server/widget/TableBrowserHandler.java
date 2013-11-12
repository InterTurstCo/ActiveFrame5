package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.model.gui.collection.view.CollectionColumnConfig;
import ru.intertrust.cm.core.config.model.gui.collection.view.CollectionDisplayConfig;
import ru.intertrust.cm.core.config.model.gui.collection.view.CollectionViewConfig;
import ru.intertrust.cm.core.config.model.gui.form.widget.SelectionPatternConfig;
import ru.intertrust.cm.core.config.model.gui.form.widget.TableBrowserConfig;
import ru.intertrust.cm.core.config.model.gui.navigation.CollectionRefConfig;
import ru.intertrust.cm.core.config.model.gui.navigation.CollectionViewRefConfig;
import ru.intertrust.cm.core.gui.api.server.widget.LinkEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.widget.FilteredRowsList;
import ru.intertrust.cm.core.gui.model.form.widget.FilteredRowsRequest;
import ru.intertrust.cm.core.gui.model.form.widget.TableBrowserRowItem;
import ru.intertrust.cm.core.gui.model.form.widget.TableBrowserState;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 06.11.13
 *         Time: 13:15
 */
@ComponentName("table-browser")
public class TableBrowserHandler extends LinkEditingWidgetHandler {
    @Autowired
    private CrudService crudService;
    @Autowired
    private CollectionsService collectionsService;

    @Override
    public TableBrowserState getInitialState(WidgetContext context) {
        TableBrowserState state = new TableBrowserState();
        TableBrowserConfig widgetConfig = context.getWidgetConfig();
        CollectionViewRefConfig viewRefConfig = widgetConfig.getCollectionViewRefConfig();
        CollectionRefConfig collectionRefConfig = widgetConfig.getCollectionRefConfig();
        String collectionName = collectionRefConfig.getName();
        String collectionViewName = viewRefConfig.getName();
        CollectionViewConfig viewConfig = getCollectionViewConfigurationByName(collectionViewName);
        LinkedHashMap<String, String> map = getDomainObjectFieldsOnColumnNamesMap(viewConfig);

        state.setTableBrowserConfig(widgetConfig);
        ArrayList<Id> selectedIds = context.getObjectIds();
        List<DomainObject> domainObjects;
        if (!selectedIds.isEmpty()) {
            domainObjects = crudService.find(selectedIds);
        } else {
            domainObjects = Collections.emptyList();
        }
        ArrayList<TableBrowserRowItem> items = new ArrayList<TableBrowserRowItem>();
        SelectionPatternConfig selectionPatternConfig = widgetConfig.getSelectionPatternConfig();
        Pattern pattern = createDefaultRegexPattern();
        Matcher matcher = pattern.matcher(selectionPatternConfig.getValue());
        for (DomainObject domainObject : domainObjects) {
            TableBrowserRowItem item = new TableBrowserRowItem();
            item.setId(domainObject.getId());
            item.setSelectedRowRepresentation(format(domainObject, matcher));
            LinkedHashMap<String, Value> values = getRowModelValues(domainObject, map.keySet());
            item.setRow(values);
            items.add(item);
        }
        state.setCollectionName(collectionName);
        state.setDomainFieldOnColumnNameMap(map);
        state.setSelectedItems(items);
        return state;
    }

    public FilteredRowsList fetchFilteredRows(Dto inputParams) {
        FilteredRowsRequest filteredRowsRequest = (FilteredRowsRequest) inputParams;
        List<Filter> filters = new ArrayList<>();

        if (!filteredRowsRequest.getExcludeIds().isEmpty()) {
            Set<Id> excludedIds = new HashSet(filteredRowsRequest.getExcludeIds());
            filters.add(prepareExcludeIdsFilter(excludedIds, filteredRowsRequest.getIdsExclusionFilterName()));
        }
        filters.add(prepareInputTextFilter(filteredRowsRequest.getText(), filteredRowsRequest.getInputTextFilterName()));

        IdentifiableObjectCollection collection = collectionsService.
                findCollection(filteredRowsRequest.getCollectionName(), null, filters);
        Pattern pattern = createDefaultRegexPattern();

        Matcher selectionMatcher = pattern.matcher(filteredRowsRequest.getSelectionPattern());

        ArrayList<TableBrowserRowItem> items = new ArrayList<>();

        for (IdentifiableObject identifiableObject : collection) {

            TableBrowserRowItem item = new TableBrowserRowItem();
            item.setId(identifiableObject.getId());
            item.setSelectedRowRepresentation(format(identifiableObject, selectionMatcher));
            LinkedHashMap<String, Value> values = getRowModelValues(identifiableObject, filteredRowsRequest.
                    getColumnFields());
            item.setRow(values);
            items.add(item);
        }
        FilteredRowsList filteredRows = new FilteredRowsList();
        filteredRows.setFilteredRows(items);
        return filteredRows;
    }

    private Filter prepareInputTextFilter(String text, String inputTextFilterName) {
        Filter textFilter = new Filter();
        textFilter.setFilter(inputTextFilterName);
        textFilter.addCriterion(0, new StringValue(text + "%"));
        return textFilter;
    }

    private Filter prepareExcludeIdsFilter(Set<Id> excludeIds, String idsExclusionFilterName) {
        Filter exludeIdsFilter = new Filter();

        List<Value> excludeIdsCriterion = new ArrayList<>();
        for (Id id : excludeIds) {
            excludeIdsCriterion.add(new ReferenceValue(id));
        }
        exludeIdsFilter.addMultiCriterion(0, excludeIdsCriterion);
        exludeIdsFilter.setFilter(idsExclusionFilterName);
        return exludeIdsFilter;
    }

    private Pattern createDefaultRegexPattern() {
        return Pattern.compile("\\{\\w+\\}");
    }

    private LinkedHashMap<String, Value> getRowModelValues(IdentifiableObject identifiableObject, Set<String> columnFields) {

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

    private LinkedHashMap<String, String> getDomainObjectFieldsOnColumnNamesMap(CollectionViewConfig collectionViewConfig) {
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

    private CollectionViewConfig getCollectionViewConfigurationByName(String name) {
        CollectionViewConfig collectionViewConfig = configurationService.
                getConfig(CollectionViewConfig.class, name);

        return collectionViewConfig;

    }

}
