package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.gui.form.widget.FormattingConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionPatternConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SingleChoiceConfig;
import ru.intertrust.cm.core.config.gui.form.widget.TableBrowserConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;
import ru.intertrust.cm.core.config.gui.navigation.DefaultSortCriteriaConfig;
import ru.intertrust.cm.core.gui.api.server.widget.FormatHandler;
import ru.intertrust.cm.core.gui.api.server.widget.LinkEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.impl.server.util.FilterBuilder;
import ru.intertrust.cm.core.gui.impl.server.util.SortOrderBuilder;
import ru.intertrust.cm.core.gui.impl.server.util.WidgetConstants;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.FormatRowsRequest;
import ru.intertrust.cm.core.gui.model.form.widget.ParsedRowsList;
import ru.intertrust.cm.core.gui.model.form.widget.TableBrowserItem;
import ru.intertrust.cm.core.gui.model.form.widget.TableBrowserState;

import java.util.*;
import java.util.regex.Matcher;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 06.11.13
 *         Time: 13:15
 */
@ComponentName("table-browser")
public class TableBrowserHandler extends LinkEditingWidgetHandler {

    @Autowired
    CollectionsService collectionsService;

    @Override
    public TableBrowserState getInitialState(WidgetContext context) {
        TableBrowserState state = new TableBrowserState();
        TableBrowserConfig widgetConfig = context.getWidgetConfig();
        state.setTableBrowserConfig(widgetConfig);
        ArrayList<Id> selectedIds = context.getAllObjectIds();
        Set<Id> selectedIdsSet = new LinkedHashSet<>();
        state.setSelectedIds(selectedIdsSet);
        ArrayList<TableBrowserItem> items = new ArrayList<>(0);
        if (!selectedIds.isEmpty()) {
            String collectionName = widgetConfig.getCollectionRefConfig().getName();
            Filter includeIds = FilterBuilder.prepareFilter(selectedIdsSet, FilterBuilder.INCLUDED_IDS_FILTER);
            List<Filter> filters = new ArrayList<>();
            filters.add(includeIds);
            DefaultSortCriteriaConfig defaultSortCriteriaConfig = widgetConfig.getDefaultSortCriteriaConfig();
            SortOrder sortOrder = SortOrderBuilder.getSimpleSortOrder(defaultSortCriteriaConfig);
            IdentifiableObjectCollection collection = collectionsService.findCollection(collectionName, sortOrder, filters);
            int selectedIdsNumber = selectedIds.size();
            int collectionItemsNumber = collection.size();
            SelectionFiltersConfig selectionFiltersConfig = widgetConfig.getSelectionFiltersConfig();
            if (selectedIdsNumber == collectionItemsNumber || (selectedIdsNumber != collectionItemsNumber
                    && selectionFiltersConfig != null)) {
                FilterBuilder.prepareSelectionFilters(selectionFiltersConfig, null, filters);
                collection = collectionsService.findCollection(collectionName, sortOrder, filters);

                items = generateTableBrowserItemsFromCollection(widgetConfig, collection);
            } else {
                items = generateTableBrowserItemsFromCollectionAndIds(widgetConfig,
                        collection, new ArrayList<Id>(selectedIds));
            }
        }
        state.setTableBrowserItems(items);
        SingleChoiceConfig singleChoiceConfig = widgetConfig.getSingleChoice();
        boolean singleChoiceFromConfig = singleChoiceConfig == null ? false : singleChoiceConfig.isSingleChoice();
        boolean singleChoice = isSingleChoice(context, singleChoiceFromConfig);
        state.setSingleChoice(singleChoice);

        return state;
    }

    private ArrayList<TableBrowserItem> generateTableBrowserItemsFromCollection(TableBrowserConfig widgetConfig,
                                                                                IdentifiableObjectCollection collection) {
        ArrayList<TableBrowserItem> items = new ArrayList<TableBrowserItem>();
        SelectionPatternConfig selectionPatternConfig = widgetConfig.getSelectionPatternConfig();
        Matcher matcher = FormatHandler.pattern.matcher(selectionPatternConfig.getValue());
        FormattingConfig formattingConfig = widgetConfig.getFormattingConfig();
        for (IdentifiableObject collectionObject : collection) {
            TableBrowserItem item = new TableBrowserItem();
            item.setId(collectionObject.getId());
            item.setStringRepresentation(formatHandler.format(collectionObject, matcher, formattingConfig));
            items.add(item);
        }
        return items;
    }

    private ArrayList<TableBrowserItem> generateTableBrowserItemsFromCollectionAndIds(TableBrowserConfig widgetConfig,
                                                                                      IdentifiableObjectCollection collection, List<Id> selectedIds) {
        ArrayList<TableBrowserItem> items = new ArrayList<TableBrowserItem>();
        SelectionPatternConfig selectionPatternConfig = widgetConfig.getSelectionPatternConfig();
        Matcher matcher = FormatHandler.pattern.matcher(selectionPatternConfig.getValue());
        FormattingConfig formattingConfig = widgetConfig.getFormattingConfig();
        for (IdentifiableObject collectionObject : collection) {
            TableBrowserItem item = new TableBrowserItem();
            Id id = collectionObject.getId();
            item.setId(id);
            selectedIds.remove(id);
            item.setStringRepresentation(formatHandler.format(collectionObject, matcher, formattingConfig));
            items.add(item);
        }
        for (Id selectedId : selectedIds) {
            TableBrowserItem item = new TableBrowserItem();
            item.setId(selectedId);
            item.setStringRepresentation(WidgetConstants.REPRESENTATION_STUB);
            items.add(item);
        }
        return items;
    }


    public ParsedRowsList fetchParsedRows(Dto inputParams) {
        FormatRowsRequest formatRowsRequest = (FormatRowsRequest) inputParams;
        List<Id> idsToParse = formatRowsRequest.getIdsShouldBeFormatted();
        Filter includeIds = FilterBuilder.prepareFilter(new HashSet<Id>(idsToParse), FilterBuilder.INCLUDED_IDS_FILTER);
        List<Filter> filters = new ArrayList<>();
        filters.add(includeIds);
        String collectionName = formatRowsRequest.getCollectionName();
        DefaultSortCriteriaConfig defaultSortCriteriaConfig = formatRowsRequest.getDefaultSortCriteriaConfig();
        SortOrder sortOrder = SortOrderBuilder.getSimpleSortOrder(defaultSortCriteriaConfig);
        IdentifiableObjectCollection collection = collectionsService.findCollection(collectionName, sortOrder, filters);

        Matcher selectionMatcher = formatHandler.pattern.matcher(formatRowsRequest.getSelectionPattern());
        ArrayList<TableBrowserItem> items = new ArrayList<>();
        FormattingConfig formattingConfig = formatRowsRequest.getFormattingConfig();
        for (IdentifiableObject collectionObject : collection) {
            TableBrowserItem item = new TableBrowserItem();
            item.setId(collectionObject.getId());
            item.setStringRepresentation(formatHandler.format(collectionObject, selectionMatcher, formattingConfig));
            items.add(item);
        }
        ParsedRowsList parsedRows = new ParsedRowsList();
        parsedRows.setFilteredRows(items);
        return parsedRows;
    }

}
