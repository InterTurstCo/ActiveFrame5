package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.form.PopupTitlesHolder;
import ru.intertrust.cm.core.config.gui.form.widget.*;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.SelectionSortCriteriaConfig;
import ru.intertrust.cm.core.gui.api.server.plugin.FilterBuilder;
import ru.intertrust.cm.core.gui.api.server.widget.LinkEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.impl.server.util.SortOrderBuilder;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.filters.ComplicatedFiltersParams;
import ru.intertrust.cm.core.gui.model.form.widget.TableBrowserState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetItemsRequest;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetItemsResponse;
import ru.intertrust.cm.core.gui.model.util.WidgetUtil;
import ru.intertrust.cm.core.util.ObjectCloner;

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
        ObjectCloner cloner = new ObjectCloner();
        TableBrowserConfig widgetConfig = cloner.cloneObject(context.getWidgetConfig(), TableBrowserConfig.class);

        state.setTableBrowserConfig(widgetConfig);
        ArrayList<Id> selectedIds = context.getAllObjectIds();
        Set<Id> selectedIdsSet = new LinkedHashSet<>(selectedIds);
        state.setSelectedIds(selectedIdsSet);
        LinkedHashMap<Id, String> listValues = null;
        DomainObject root = context.getFormObjects().getRootNode().getDomainObject();
        fillTypeTitleMap(root, widgetConfig.getLinkedFormMappingConfig(), state);

        abandonAccessed(root, widgetConfig.getCreatedObjectsConfig(), null);
        PopupTitlesHolder popupTitlesHolder = titleBuilder.buildPopupTitles(widgetConfig.getLinkedFormConfig(), root);
        Map<String, WidgetConfig> widgetIdConfigMap = context.getWidgetConfigsById();
        state.setPopupTitlesHolder(popupTitlesHolder);
        if (!selectedIds.isEmpty()) {
            String collectionName = widgetConfig.getCollectionRefConfig().getName();
            List<Filter> filters = new ArrayList<>();
            filterBuilder.prepareIncludedIdsFilter(selectedIds, filters);
            SelectionSortCriteriaConfig selectionSortCriteriaConfig = widgetConfig.getSelectionSortCriteriaConfig();
            SortOrder sortOrder = SortOrderBuilder.getSelectionSortOrder(selectionSortCriteriaConfig);
            SelectionFiltersConfig selectionFiltersConfig = widgetConfig.getSelectionFiltersConfig();

            ComplicatedFiltersParams filtersParams = new ComplicatedFiltersParams(root.getId());
            boolean hasSelectionFilters = filterBuilder.prepareSelectionFilters(selectionFiltersConfig, filtersParams,filters);
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
        state.setExtraWidgetIdsComponentNames(WidgetUtil.
                getWidgetIdsComponentsNamesForFilters(widgetConfig.getCollectionExtraFiltersConfig(),
                        widgetIdConfigMap));

        SingleChoiceConfig singleChoiceConfig = widgetConfig.getSingleChoice();
        Boolean singleChoiceFromConfig = singleChoiceConfig == null ? false : singleChoiceConfig.isSingleChoice();
        boolean singleChoice = isSingleChoice(context, singleChoiceFromConfig);
        state.setSingleChoice(singleChoice);
        boolean displayingAsHyperlinks = WidgetUtil.isDisplayingAsHyperlinks(widgetConfig.getDisplayValuesAsLinksConfig());
        state.setDisplayingAsHyperlinks(displayingAsHyperlinks);

        return state;
    }

    public WidgetItemsResponse fetchTableBrowserItems(Dto inputParams) {
        WidgetItemsRequest widgetItemsRequest = (WidgetItemsRequest) inputParams;
        List<Id> selectedIds = widgetItemsRequest.getSelectedIds();

        List<Filter> filters = new ArrayList<>();
        filterBuilder.prepareIncludedIdsFilter(selectedIds, filters);
        String collectionName = widgetItemsRequest.getCollectionName();
        SelectionSortCriteriaConfig defaultSortCriteriaConfig = widgetItemsRequest.getSelectionSortCriteriaConfig();
        SortOrder sortOrder = SortOrderBuilder.getSelectionSortOrder(defaultSortCriteriaConfig);
        SelectionFiltersConfig selectionFiltersConfig = widgetItemsRequest.getSelectionFiltersConfig();
        boolean selectionFiltersWereApplied = filterBuilder.prepareSelectionFilters(selectionFiltersConfig,
                widgetItemsRequest.getComplicatedFiltersParams(),filters);
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
