package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionPatternConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SingleChoiceConfig;
import ru.intertrust.cm.core.config.gui.form.widget.TableBrowserConfig;
import ru.intertrust.cm.core.config.gui.navigation.DefaultSortCriteriaConfig;
import ru.intertrust.cm.core.gui.api.server.widget.FormatHandler;
import ru.intertrust.cm.core.gui.api.server.widget.LinkEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.impl.server.util.FilterBuilder;
import ru.intertrust.cm.core.gui.impl.server.util.SortOrderBuilder;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.FormatRowsRequest;
import ru.intertrust.cm.core.gui.model.form.widget.ParsedRowsList;
import ru.intertrust.cm.core.gui.model.form.widget.TableBrowserItem;
import ru.intertrust.cm.core.gui.model.form.widget.TableBrowserState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 06.11.13
 *         Time: 13:15
 */
@ComponentName("table-browser")
public class TableBrowserHandler extends LinkEditingWidgetHandler {
    private static final int NUMBER_OF_ITEMS = 25;
    @Autowired
    CollectionsService collectionsService;

    @Override
    public TableBrowserState getInitialState(WidgetContext context) {
        TableBrowserState state = new TableBrowserState();
        TableBrowserConfig widgetConfig = context.getWidgetConfig();
        state.setTableBrowserConfig(widgetConfig);
        Id rootId = context.getFormObjects().getRootNode().getDomainObject().getId();
        state.setRootId(rootId);
        List<Id> selectedIds = context.getAllObjectIds();
        String collectionName = widgetConfig.getCollectionRefConfig().getName();
        Filter includeIds = FilterBuilder.prepareFilter(new HashSet<Id>(selectedIds), FilterBuilder.INCLUDED_IDS_FILTER);
        List<Filter> filters = new ArrayList<>();
        filters.add(includeIds);
        DefaultSortCriteriaConfig defaultSortCriteriaConfig = widgetConfig.getDefaultSortCriteriaConfig();
        SortOrder sortOrder = SortOrderBuilder.getSimpleSortOrder(defaultSortCriteriaConfig);
        IdentifiableObjectCollection collection = collectionsService.
                    findCollection(collectionName, sortOrder, filters, 0, NUMBER_OF_ITEMS);
        ArrayList<TableBrowserItem> items = new ArrayList<TableBrowserItem>();
        SelectionPatternConfig selectionPatternConfig = widgetConfig.getSelectionPatternConfig();
        Matcher matcher = FormatHandler.pattern.matcher(selectionPatternConfig.getValue());
        for (IdentifiableObject collectionObject : collection) {
            TableBrowserItem item = new TableBrowserItem();
            item.setId(collectionObject.getId());
            item.setStringRepresentation(formatHandler.format(collectionObject, matcher));
            items.add(item);
        }
        state.setTableBrowserItems(items);
        SingleChoiceConfig singleChoiceConfig = widgetConfig.getSingleChoice();
        boolean singleChoiceFromConfig = singleChoiceConfig == null ? false : singleChoiceConfig.isSingleChoice();
        boolean singleChoice = isSingleChoice(context, singleChoiceFromConfig) ;
        state.setSingleChoice(singleChoice);

        return state;
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
        IdentifiableObjectCollection collection = collectionsService.
                findCollection(collectionName, sortOrder, filters, 0, NUMBER_OF_ITEMS);
        Matcher selectionMatcher = formatHandler.pattern.matcher(formatRowsRequest.getSelectionPattern());
        ArrayList<TableBrowserItem> items = new ArrayList<>();

        for (IdentifiableObject collectionObject : collection) {
            TableBrowserItem item = new TableBrowserItem();
            item.setId(collectionObject.getId());
            item.setStringRepresentation(formatHandler.format(collectionObject, selectionMatcher));
            items.add(item);
        }
        ParsedRowsList parsedRows = new ParsedRowsList();
        parsedRows.setFilteredRows(items);
        return parsedRows;
    }

}
