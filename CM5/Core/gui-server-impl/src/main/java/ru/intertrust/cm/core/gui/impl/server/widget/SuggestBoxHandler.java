package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.form.PopupTitlesHolder;
import ru.intertrust.cm.core.business.api.util.ObjectCloner;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.form.widget.FormattingConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionPatternConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SingleChoiceConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SuggestBoxConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.SelectionSortCriteriaConfig;
import ru.intertrust.cm.core.config.gui.navigation.DefaultSortCriteriaConfig;
import ru.intertrust.cm.core.gui.api.server.plugin.FilterBuilder;
import ru.intertrust.cm.core.gui.api.server.widget.FormatHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetItemsHandler;
import ru.intertrust.cm.core.gui.impl.server.util.FilterBuilderUtil;
import ru.intertrust.cm.core.gui.impl.server.util.WidgetServerUtil;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.filters.ComplexFiltersParams;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.widget.*;
import ru.intertrust.cm.core.gui.model.util.WidgetUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Created with IntelliJ IDEA.
 * User: andrey
 * Date: 22.10.13
 * Time: 12:33
 * To change this template use File | Settings | File Templates.
 */
@ComponentName("suggest-box")
public class SuggestBoxHandler extends ListWidgetHandler {

    @Autowired
    private CollectionsService collectionsService;

    @Autowired
    private FilterBuilder filterBuilder;

    @Override
    public SuggestBoxState getInitialState(WidgetContext context) {
        SuggestBoxState state = new SuggestBoxState();
        state.setWidgetConfigs(context.getWidgetConfigsById());
        SuggestBoxConfig widgetConfig = ObjectCloner.getInstance().cloneObject(context.getWidgetConfig(), SuggestBoxConfig.class);
        state.setSuggestBoxConfig(widgetConfig);
        ArrayList<Id> selectedIds = context.getAllObjectIds();
        DomainObject root = context.getFormObjects().getRootNode().getDomainObject();
        fillTypeTitleMap(root, widgetConfig.getLinkedFormMappingConfig(), state);
        abandonAccessed(root, widgetConfig.getCreatedObjectsConfig(), null);
        PopupTitlesHolder popupTitlesHolder = titleBuilder.buildPopupTitles(widgetConfig.getLinkedFormConfig(), root);
        state.setPopupTitlesHolder(popupTitlesHolder);
        LinkedHashMap<Id, String> objects = new LinkedHashMap<Id, String>();
        state.setSelectedIds(new LinkedHashSet<>(selectedIds));
        state.setRootObject(root);
        if (!selectedIds.isEmpty()) {
            SelectionSortCriteriaConfig sortCriteriaConfig = widgetConfig.getSelectionSortCriteriaConfig();
            String collectionName = widgetConfig.getCollectionRefConfig().getName();
            SortOrder sortOrder = sortOrderHelper.buildSortOrder(collectionName, sortCriteriaConfig);
            List<Filter> filters = new ArrayList<Filter>();
            filterBuilder.prepareIncludedIdsFilter(selectedIds, filters);
            SelectionFiltersConfig selectionFiltersConfig = widgetConfig.getSelectionFiltersConfig();
            ComplexFiltersParams filtersParams = new ComplexFiltersParams(root.getId());
            boolean hasSelectionFilters = filterBuilder.prepareSelectionFilters(selectionFiltersConfig, filtersParams,filters);
            int limit = WidgetUtil.getLimit(selectionFiltersConfig);
            boolean noLimit = limit == -1;
            IdentifiableObjectCollection collection = collectionsService.findCollection(collectionName, sortOrder, filters);
            state.setFilteredItemsNumber(collection.size());
            WidgetServerUtil.doLimit(collection, limit);
            SelectionPatternConfig selectionPatternConfig = widgetConfig.getSelectionPatternConfig();
            FormattingConfig formattingConfig = widgetConfig.getFormattingConfig();
            objects = (!hasSelectionFilters && collection.size() != selectedIds.size() && noLimit)
                    ? widgetItemsHandler.generateWidgetItemsFromCollectionAndIds(selectionPatternConfig,
                    formattingConfig, collection, selectedIds)
                    : widgetItemsHandler.generateWidgetItemsFromCollection(selectionPatternConfig,
                    formattingConfig, collection);
        }
        SingleChoiceConfig singleChoiceConfig = widgetConfig.getSingleChoice();
        Boolean singleChoiceFromConfig = singleChoiceConfig == null ? false : singleChoiceConfig.isSingleChoice();
        boolean isReportForm = FormConfig.TYPE_REPORT.equals(context.getFormType());
        boolean singleChoice = isReportForm ? (singleChoiceFromConfig != null && singleChoiceFromConfig)
                : isSingleChoice(context, singleChoiceFromConfig);

        state.setSingleChoice(singleChoice);
        state.setListValues(objects);
        state.setExtraWidgetIdsComponentNames(WidgetUtil.
                getWidgetIdsComponentsNamesForFilters(widgetConfig.getCollectionExtraFiltersConfig(), context.getWidgetConfigsById()));
        boolean displayingAsHyperlinks = WidgetUtil.isDisplayingAsHyperlinks(widgetConfig.getDisplayValuesAsLinksConfig());
        state.setDisplayingAsHyperlinks(displayingAsHyperlinks);
        state.setParentWidgetIdsForNewFormMap(createParentWidgetIdsForNewFormMap(widgetConfig,
                context.getWidgetConfigsById().values()));
        return state;
    }

    public SuggestionList obtainSuggestions(Dto inputParams) {
        SuggestionRequest suggestionRequest = (SuggestionRequest) inputParams;
        List<Filter> filters = new ArrayList<>();
        if (!suggestionRequest.getExcludeIds().isEmpty()) {
            filters.add(FilterBuilderUtil.prepareFilter(suggestionRequest.getExcludeIds(), FilterBuilderUtil.EXCLUDED_IDS_FILTER));
        }
        ComplexFiltersParams filtersParams = suggestionRequest.getComplexFiltersParams();
        filterBuilder.prepareExtraFilters(suggestionRequest.getCollectionExtraFiltersConfig(), filtersParams, filters);
        DefaultSortCriteriaConfig sortCriteriaConfig = suggestionRequest.getDefaultSortCriteriaConfig();
        LazyLoadState lazyLoadState = suggestionRequest.getLazyLoadState();
        String collectionName = suggestionRequest.getCollectionName();
        SortOrder sortOrder = sortOrderHelper.buildSortOrder(collectionName, sortCriteriaConfig);
        boolean isRequestForMoreItems = lazyLoadState.getOffset() != 0;
        IdentifiableObjectCollection collection = collectionsService.findCollection(collectionName, sortOrder, filters,
                lazyLoadState.getOffset(), lazyLoadState.getPageSize());
        Matcher dropDownMatcher = FormatHandler.pattern.matcher(suggestionRequest.getDropdownPattern());
        Matcher selectionMatcher = FormatHandler.pattern.matcher(suggestionRequest.getSelectionPattern());
        ArrayList<SuggestionItem> suggestionItems = new ArrayList<>();
        FormattingConfig formattingConfig = suggestionRequest.getFormattingConfig();
        for (IdentifiableObject identifiableObject : collection) {
            SuggestionItem suggestionItem = new SuggestionItem(identifiableObject.getId(),
                    formatHandler.format(identifiableObject, dropDownMatcher, formattingConfig),
                    formatHandler.format(identifiableObject, selectionMatcher, formattingConfig));
            suggestionItems.add(suggestionItem);
        }
        SuggestionList suggestionResponse = new SuggestionList();
        suggestionResponse.setSuggestions(suggestionItems);
        suggestionResponse.setResponseForMoreItems(isRequestForMoreItems);
        return suggestionResponse;
    }

    public Dto getRepresentationForOneItem(Dto inputParams){
        return formatHandler.getRepresentationForOneItem(inputParams);
    }

    public Dto fetchWidgetItems(Dto inputParams){
        return widgetItemsHandler.fetchWidgetItems(inputParams);
    }

}
