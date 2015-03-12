package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.gui.form.widget.FormattingConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionPatternConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.SelectionSortCriteriaConfig;
import ru.intertrust.cm.core.gui.api.server.plugin.FilterBuilder;
import ru.intertrust.cm.core.gui.api.server.plugin.SortOrderHelper;
import ru.intertrust.cm.core.gui.api.server.widget.FormatHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetItemsHandler;
import ru.intertrust.cm.core.gui.impl.server.util.FilterBuilderUtil;
import ru.intertrust.cm.core.gui.impl.server.util.WidgetConstants;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.filters.ComplexFiltersParams;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetItemsRequest;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetItemsResponse;
import ru.intertrust.cm.core.gui.model.util.WidgetUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 06.07.2014
 *         Time: 12:37
 */
@ComponentName("widget-items-handler")
public class WidgetItemsHandlerImpl implements WidgetItemsHandler {
    @Autowired
    protected FormatHandler formatHandler;

    @Autowired
    protected CollectionsService collectionsService;

    @Autowired
    private FilterBuilder filterBuilder;

    @Autowired
    private SortOrderHelper sortOrderHelper;

    public LinkedHashMap<Id, String> generateWidgetItemsFromCollection(SelectionPatternConfig selectionPatternConfig,
                                                                       FormattingConfig formattingConfig,
                                                                       IdentifiableObjectCollection collection) {
        LinkedHashMap<Id, String> listValues = new LinkedHashMap<>();
        Matcher matcher = FormatHandler.pattern.matcher(selectionPatternConfig.getValue());
        for (IdentifiableObject collectionObject : collection) {
            Id id = collectionObject.getId();
            String representation = formatHandler.format(collectionObject, matcher, formattingConfig);
            listValues.put(id, representation);
        }
        return listValues;
    }

    public LinkedHashMap<Id, String> generateWidgetItemsFromCollectionAndIds(SelectionPatternConfig selectionPatternConfig,
                                                                             FormattingConfig formattingConfig,
                                                                             IdentifiableObjectCollection collection,
                                                                             List<Id> selectedIds) {
        LinkedHashMap<Id, String> listValues = new LinkedHashMap<>();
        Matcher matcher = FormatHandler.pattern.matcher(selectionPatternConfig.getValue());
        for (IdentifiableObject collectionObject : collection) {
            Id id = collectionObject.getId();
            selectedIds.remove(id);
            String representation = formatHandler.format(collectionObject, matcher, formattingConfig);
            listValues.put(id, representation);
        }
        for (Id selectedId : selectedIds) {
            listValues.put(selectedId, WidgetConstants.REPRESENTATION_STUB);
        }
        return listValues;
    }

    public WidgetItemsResponse fetchWidgetItems(Dto inputParams) {
        WidgetItemsRequest widgetItemsRequest = (WidgetItemsRequest) inputParams;
        List<Id> selectedIds = widgetItemsRequest.getSelectedIds();
        Filter includeIds = FilterBuilderUtil.prepareFilter(new HashSet<Id>(selectedIds), FilterBuilderUtil.INCLUDED_IDS_FILTER);
        List<Filter> filters = new ArrayList<>();
        filters.add(includeIds);
        String collectionName = widgetItemsRequest.getCollectionName();
        SelectionSortCriteriaConfig selectionSortCriteriaConfig = widgetItemsRequest.getSelectionSortCriteriaConfig();
        SortOrder sortOrder = sortOrderHelper.buildSortOrder(collectionName, selectionSortCriteriaConfig);
        SelectionFiltersConfig selectionFiltersConfig = widgetItemsRequest.getSelectionFiltersConfig();
        ComplexFiltersParams filtersParams = widgetItemsRequest.getComplexFiltersParams();
        boolean selectionFiltersWereApplied = filterBuilder.prepareSelectionFilters(selectionFiltersConfig, filtersParams,filters);
        Integer limit = WidgetUtil.getLimit(selectionFiltersConfig);
        IdentifiableObjectCollection collection = null;
        boolean hasLostItems = false;
        if (limit != null) {
            collection = collectionsService.findCollection(collectionName, sortOrder, filters, limit,
                    WidgetConstants.UNBOUNDED_LIMIT); //limit becomes offset for tooltip
            hasLostItems = collection.size() < (selectedIds.size() - limit);

        } else {
            collection = collectionsService.findCollection(collectionName, sortOrder, filters);
            hasLostItems = collection.size() != selectedIds.size();
        }
        if (selectionFiltersWereApplied) {
            hasLostItems = false;
        }
        SelectionPatternConfig selectionPatternConfig = new SelectionPatternConfig();
        selectionPatternConfig.setValue(widgetItemsRequest.getSelectionPattern());
        FormattingConfig formattingConfig = widgetItemsRequest.getFormattingConfig();
        LinkedHashMap<Id, String> listValues = hasLostItems
                ? generateWidgetItemsFromCollectionAndIds(selectionPatternConfig, formattingConfig, collection, selectedIds)
                : generateWidgetItemsFromCollection(selectionPatternConfig, formattingConfig, collection);
        WidgetItemsResponse response = new WidgetItemsResponse();
        response.setListValues(listValues);
        return response;
    }

}
