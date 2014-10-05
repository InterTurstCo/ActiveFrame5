package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.gui.form.widget.FormattingConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionPatternConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SingleChoiceConfig;
import ru.intertrust.cm.core.config.gui.form.widget.TableBrowserConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.SelectionSortCriteriaConfig;
import ru.intertrust.cm.core.gui.api.server.plugin.FilterBuilder;
import ru.intertrust.cm.core.gui.api.server.widget.LinkEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.impl.server.util.FilterBuilderUtil;
import ru.intertrust.cm.core.gui.impl.server.util.SortOrderBuilder;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.TableBrowserState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetItemsRequest;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetItemsResponse;
import ru.intertrust.cm.core.gui.model.util.WidgetUtil;

import java.util.*;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 06.11.13
 *         Time: 13:15
 */
@ComponentName("table-browser")
public class TableBrowserHandler extends LinkEditingWidgetHandler {

    @Autowired
    CollectionsService collectionsService;

    @Autowired
    private FilterBuilder filterBuilder;

    @Override
    public TableBrowserState getInitialState(WidgetContext context) {
        TableBrowserState state = new TableBrowserState();
        TableBrowserConfig widgetConfig = context.getWidgetConfig();
        state.setTableBrowserConfig(widgetConfig);
        ArrayList<Id> selectedIds = context.getAllObjectIds();
        Set<Id> selectedIdsSet = new LinkedHashSet<>(selectedIds);
        state.setSelectedIds(selectedIdsSet);
        LinkedHashMap<Id, String> listValues = null;

        if (context.getDefaultValues() != null) {
            Value[] defaultValues = context.getDefaultValues();
            ArrayList<Id> defaultValueList = new ArrayList<>();
            for (Value defaultValue : defaultValues) {
                if (defaultValue instanceof ReferenceValue) {
                    defaultValueList.add(((ReferenceValue) defaultValue).get());
                }
            }
            if (!defaultValueList.isEmpty()) {
                selectedIds = defaultValueList;
            }
        }


        if (!selectedIds.isEmpty()) {
            String collectionName = widgetConfig.getCollectionRefConfig().getName();
            Filter includeIds = FilterBuilderUtil.prepareFilter(selectedIdsSet, FilterBuilderUtil.INCLUDED_IDS_FILTER);
            List<Filter> filters = new ArrayList<>();
            filters.add(includeIds);
            SelectionSortCriteriaConfig selectionSortCriteriaConfig = widgetConfig.getSelectionSortCriteriaConfig();
            SortOrder sortOrder = SortOrderBuilder.getSelectionSortOrder(selectionSortCriteriaConfig);
            SelectionFiltersConfig selectionFiltersConfig = widgetConfig.getSelectionFiltersConfig();
            boolean hasSelectionFilters = filterBuilder.prepareSelectionFilters(selectionFiltersConfig, null, filters);
            int limit = WidgetUtil.getLimit(selectionFiltersConfig);
            boolean noLimit = limit == -1;
            IdentifiableObjectCollection collection = noLimit
                    ? collectionsService.findCollection(collectionName, sortOrder, filters)
                    : collectionsService.findCollection(collectionName, sortOrder, filters, 0, limit);
            SelectionPatternConfig selectionPatternConfig = widgetConfig.getSelectionPatternConfig();
            FormattingConfig formattingConfig = widgetConfig.getFormattingConfig();
            listValues = (!hasSelectionFilters && collection.size() != selectedIds.size() && noLimit)
                    ? widgetItemsHandler.generateWidgetItemsFromCollectionAndIds(selectionPatternConfig, formattingConfig, collection, selectedIds)
                    : widgetItemsHandler.generateWidgetItemsFromCollection(selectionPatternConfig,
                    formattingConfig, collection);
            state.setListValues(listValues);

        }

        SingleChoiceConfig singleChoiceConfig = widgetConfig.getSingleChoice();
        boolean singleChoiceFromConfig = singleChoiceConfig == null ? false : singleChoiceConfig.isSingleChoice();
        boolean singleChoice = isSingleChoice(context, singleChoiceFromConfig);
        state.setSingleChoice(singleChoice);
        boolean displayingAsHyperlinks = WidgetUtil.isDisplayingAsHyperlinks(widgetConfig.getDisplayValuesAsLinksConfig());
        state.setDisplayingAsHyperlinks(displayingAsHyperlinks);

        return state;
    }

    public WidgetItemsResponse fetchTableBrowserItems(Dto inputParams) {
        WidgetItemsRequest widgetItemsRequest = (WidgetItemsRequest) inputParams;
        List<Id> selectedIds = widgetItemsRequest.getSelectedIds();
        Filter includeIds = FilterBuilderUtil.prepareFilter(new HashSet<Id>(selectedIds), FilterBuilderUtil.INCLUDED_IDS_FILTER);
        List<Filter> filters = new ArrayList<>();
        filters.add(includeIds);
        String collectionName = widgetItemsRequest.getCollectionName();
        SelectionSortCriteriaConfig defaultSortCriteriaConfig = widgetItemsRequest.getSelectionSortCriteriaConfig();
        SortOrder sortOrder = SortOrderBuilder.getSelectionSortOrder(defaultSortCriteriaConfig);
        SelectionFiltersConfig selectionFiltersConfig = widgetItemsRequest.getSelectionFiltersConfig();
        boolean selectionFiltersWereApplied = filterBuilder.prepareSelectionFilters(selectionFiltersConfig, null, filters);
        IdentifiableObjectCollection collection = null;
        boolean hasLostItems = false;
        int limit = WidgetUtil.getLimit(selectionFiltersConfig);
        boolean noLimit = limit == -1;

            collection = collectionsService.findCollection(collectionName, sortOrder, filters);
            hasLostItems = collection.size() != selectedIds.size();

        if (selectionFiltersWereApplied || !noLimit) {
            hasLostItems = false;
        }
        SelectionPatternConfig selectionPatternConfig = new SelectionPatternConfig();
        selectionPatternConfig.setValue(widgetItemsRequest.getSelectionPattern());
        FormattingConfig formattingConfig = widgetItemsRequest.getFormattingConfig();
        LinkedHashMap<Id, String> listValues = hasLostItems
                ? widgetItemsHandler.generateWidgetItemsFromCollectionAndIds(selectionPatternConfig, formattingConfig, collection, selectedIds)
                : widgetItemsHandler.generateWidgetItemsFromCollection(selectionPatternConfig, formattingConfig, collection);
        WidgetItemsResponse response = new WidgetItemsResponse();
        response.setListValues(listValues);

        return response;
    }


}
