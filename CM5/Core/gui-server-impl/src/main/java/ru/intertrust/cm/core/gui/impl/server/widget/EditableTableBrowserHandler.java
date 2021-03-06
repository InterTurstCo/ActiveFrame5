package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.gui.form.widget.EditableTableBrowserConfig;
import ru.intertrust.cm.core.config.gui.form.widget.FormattingConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionPatternConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.SelectionSortCriteriaConfig;
import ru.intertrust.cm.core.config.gui.navigation.DefaultSortCriteriaConfig;
import ru.intertrust.cm.core.gui.api.server.plugin.FilterBuilder;
import ru.intertrust.cm.core.gui.api.server.plugin.SortOrderHelper;
import ru.intertrust.cm.core.gui.api.server.widget.*;
import ru.intertrust.cm.core.gui.impl.server.util.FilterBuilderUtil;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.filters.ComplexFiltersParams;
import ru.intertrust.cm.core.gui.model.form.widget.*;
import ru.intertrust.cm.core.gui.model.util.WidgetUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Created by Ravil on 26.09.2017.
 */
@ComponentName("editable-table-browser")
public class EditableTableBrowserHandler  extends ValueEditingWidgetHandler {

    @Autowired
    CollectionsService collectionsService;

    @Autowired
    private FilterBuilder filterBuilder;

    @Autowired
    protected SortOrderHelper sortOrderHelper;

    @Autowired
    protected WidgetItemsHandler widgetItemsHandler;

    @Override
    public EditableTableBrowserState getInitialState(WidgetContext context) {
        EditableTableBrowserState state = new EditableTableBrowserState();
        final FieldConfig fieldConfig = getFieldConfig(context);
        state.setText(context.<String>getFieldPlainValue());
        state.setEditableTableBrowserConfig((EditableTableBrowserConfig) context.getWidgetConfig());
        return state;
    }

    @Override
    public Value getValue(WidgetState state) {
        return new StringValue(((EditableTableBrowserState) state).getText());
    }

    public Dto getDefaultValue(Dto request){
        EditableBrowserRequestData rEquest = (EditableBrowserRequestData)request;
        if(rEquest.getConfig().getDefaultComponentName()!=null &&
                applicationContext.containsBean(rEquest.getConfig().getDefaultComponentName())){
            DefaultValueProvider provider = (DefaultValueProvider)applicationContext.getBean(rEquest.getConfig().getDefaultComponentName());
            ((EditableBrowserRequestData) request).setDefaultValue(provider.provide(((EditableBrowserRequestData) request).getFormState()));

            return rEquest;
        } else throw new GuiException("Не найден компонент предоставляющий значение по умолчанию");
    }

    public WidgetItemsResponse fetchTableBrowserItems(Dto inputParams) {
        WidgetItemsRequest widgetItemsRequest = (WidgetItemsRequest) inputParams;
        List<Id> selectedIds = widgetItemsRequest.getSelectedIds();

        List<Filter> filters = new ArrayList<>();
        filterBuilder.prepareIncludedIdsFilter(selectedIds, filters);
        String collectionName = widgetItemsRequest.getCollectionName();
        SelectionSortCriteriaConfig selectionSortCriteriaConfig = widgetItemsRequest.getSelectionSortCriteriaConfig();
        SortOrder sortOrder = sortOrderHelper.buildSortOrder(collectionName, selectionSortCriteriaConfig);
        SelectionFiltersConfig selectionFiltersConfig = widgetItemsRequest.getSelectionFiltersConfig();
        boolean selectionFiltersWereApplied = filterBuilder.prepareSelectionFilters(selectionFiltersConfig,
                widgetItemsRequest.getComplexFiltersParams(),filters);
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

    public SuggestionList obtainSuggestions(Dto inputParams) {
        SuggestionRequest suggestionRequest = (SuggestionRequest) inputParams;
        List<Filter> filters = new ArrayList<>();
        if (!suggestionRequest.getExcludeIds().isEmpty()) {
            filters.add(FilterBuilderUtil.prepareFilter(suggestionRequest.getExcludeIds(), FilterBuilderUtil.EXCLUDED_IDS_FILTER));
        }
        ComplexFiltersParams filtersParams = suggestionRequest.getComplexFiltersParams();
        prepareInputTextFilter(filtersParams, filters);

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

    private boolean prepareInputTextFilter(ComplexFiltersParams params, List<Filter> filters) {
        boolean result = false;
        if (params != null) {
            String text = params.getInputFilterValue();
            String filterName = params.getInputFilterName();
            if (text != null && !text.isEmpty() && !text.equals("*")) {
                Filter textFilter = new Filter();
                textFilter.setFilter(filterName);
                textFilter.addCriterion(0, new StringValue("%"+ text + "%"));
                filters.add(textFilter);
                result = true;
            }
        }
        return result;
    }
}
