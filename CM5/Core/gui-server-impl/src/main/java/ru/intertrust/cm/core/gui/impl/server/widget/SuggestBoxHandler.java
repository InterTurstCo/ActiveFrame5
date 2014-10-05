package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.*;
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
import ru.intertrust.cm.core.gui.impl.server.util.FilterBuilderUtil;
import ru.intertrust.cm.core.gui.impl.server.util.SortOrderBuilder;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.*;
import ru.intertrust.cm.core.gui.model.util.WidgetUtil;

import java.util.*;
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
        SuggestBoxConfig widgetConfig = context.getWidgetConfig();
        state.setSuggestBoxConfig(widgetConfig);
        ArrayList<Id> selectedIds = context.getAllObjectIds();
        LinkedHashMap<Id, String> objects = new LinkedHashMap<Id, String>();
        if (context.getDefaultValues() != null) {
            List<Id> defaultIds = HandlerUtils.takeDefaultReferenceValues(context);
            if (!defaultIds.isEmpty()) {
                selectedIds = new ArrayList<>(defaultIds);
            }
        }
        if (!selectedIds.isEmpty()) {
            SelectionSortCriteriaConfig sortCriteriaConfig = widgetConfig.getSelectionSortCriteriaConfig();
            SortOrder sortOrder = SortOrderBuilder.getSelectionSortOrder(sortCriteriaConfig);
            String collectionName = widgetConfig.getCollectionRefConfig().getName();
            List<Filter> filters = new ArrayList<Filter>();
            Set<Id> idsIncluded = new HashSet<Id>(selectedIds);
            Filter idsIncludedFilter = FilterBuilderUtil.prepareFilter(idsIncluded, FilterBuilderUtil.INCLUDED_IDS_FILTER);
            filters.add(idsIncludedFilter);
            SelectionFiltersConfig selectionFiltersConfig = widgetConfig.getSelectionFiltersConfig();
            boolean hasSelectionFilters = filterBuilder.prepareSelectionFilters(selectionFiltersConfig, null, filters);
            int limit = WidgetUtil.getLimit(selectionFiltersConfig);
            boolean noLimit = limit == -1;
            IdentifiableObjectCollection collection = noLimit
                    ? collectionsService.findCollection(collectionName, sortOrder, filters)
                    : collectionsService.findCollection(collectionName, sortOrder, filters, 0, limit);
            SelectionPatternConfig selectionPatternConfig = widgetConfig.getSelectionPatternConfig();
            FormattingConfig formattingConfig = widgetConfig.getFormattingConfig();
            objects = (!hasSelectionFilters && collection.size() != selectedIds.size() && noLimit)
                    ? widgetItemsHandler.generateWidgetItemsFromCollectionAndIds(selectionPatternConfig, formattingConfig, collection, selectedIds)
                    : widgetItemsHandler.generateWidgetItemsFromCollection(selectionPatternConfig,
                    formattingConfig, collection);
        }
        SingleChoiceConfig singleChoiceConfig = widgetConfig.getSingleChoice();
        boolean singleChoiceFromConfig = singleChoiceConfig == null ? false : singleChoiceConfig.isSingleChoice();
        boolean isReportForm = FormConfig.TYPE_REPORT.equals(context.getFormType());
        boolean singleChoice = isReportForm ? singleChoiceFromConfig : isSingleChoice(context, singleChoiceFromConfig);
        state.setSelectedIds(new LinkedHashSet<>(selectedIds));
        state.setSingleChoice(singleChoice);
        state.setListValues(objects);
        boolean displayingAsHyperlinks = WidgetUtil.isDisplayingAsHyperlinks(widgetConfig.getDisplayValuesAsLinksConfig());
        state.setDisplayingAsHyperlinks(displayingAsHyperlinks);
        return state;
    }

    public SuggestionList obtainSuggestions(Dto inputParams) {
        SuggestionRequest suggestionRequest = (SuggestionRequest) inputParams;
        List<Filter> filters = new ArrayList<>();
        if (!suggestionRequest.getExcludeIds().isEmpty()) {
            filters.add(FilterBuilderUtil.prepareFilter(suggestionRequest.getExcludeIds(), FilterBuilderUtil.EXCLUDED_IDS_FILTER));
        }
        filters.add(prepareInputTextFilter(suggestionRequest.getText(), suggestionRequest.getInputTextFilterName()));
        DefaultSortCriteriaConfig sortCriteriaConfig = suggestionRequest.getDefaultSortCriteriaConfig();
        SortOrder sortOrder = SortOrderBuilder.getSimpleSortOrder(sortCriteriaConfig);
        LazyLoadState lazyLoadState = suggestionRequest.getLazyLoadState();
        String collectionName = suggestionRequest.getCollectionName();
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

    private Filter prepareInputTextFilter(String text, String inputTextFilterName) {
        Filter textFilter = new Filter();
        textFilter.setFilter(inputTextFilterName);
        if (text.equals("*")) {
            textFilter.addCriterion(0, new StringValue("%"));
        } else {
            textFilter.addCriterion(0, new StringValue(text + "%"));
        }
        return textFilter;
    }

}
