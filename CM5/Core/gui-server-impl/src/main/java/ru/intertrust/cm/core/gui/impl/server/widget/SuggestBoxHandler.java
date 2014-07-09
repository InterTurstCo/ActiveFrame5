package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.gui.form.widget.FormattingConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionPatternConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SingleChoiceConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SuggestBoxConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;
import ru.intertrust.cm.core.config.gui.navigation.DefaultSortCriteriaConfig;
import ru.intertrust.cm.core.gui.api.server.widget.FormatHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.impl.server.util.FilterBuilder;
import ru.intertrust.cm.core.gui.impl.server.util.SortOrderBuilder;
import ru.intertrust.cm.core.gui.impl.server.util.WidgetConstants;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.SuggestBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.SuggestionItem;
import ru.intertrust.cm.core.gui.model.form.widget.SuggestionList;
import ru.intertrust.cm.core.gui.model.form.widget.SuggestionRequest;

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
    CollectionsService collectionsService;

    @Override
    public SuggestBoxState getInitialState(WidgetContext context) {
        SuggestBoxState state = new SuggestBoxState();
        SuggestBoxConfig widgetConfig = context.getWidgetConfig();
        state.setSuggestBoxConfig(widgetConfig);
        ArrayList<Id> selectedIds = context.getAllObjectIds();
        LinkedHashMap<Id, String> objects = new LinkedHashMap<Id, String>();

        if (!selectedIds.isEmpty()) {
            DefaultSortCriteriaConfig sortCriteriaConfig = widgetConfig.getDefaultSortCriteriaConfig();
            SortOrder sortOrder = SortOrderBuilder.getSimpleSortOrder(sortCriteriaConfig);
            String collectionName = widgetConfig.getCollectionRefConfig().getName();
            List<Filter> filters = new ArrayList<Filter>();
            Set<Id> idsIncluded = new HashSet<Id>(selectedIds);
            Filter idsIncludedFilter = FilterBuilder.prepareFilter(idsIncluded, FilterBuilder.INCLUDED_IDS_FILTER);
            filters.add(idsIncludedFilter);
            IdentifiableObjectCollection collection = collectionsService.findCollection(collectionName, sortOrder, filters);
            int selectedIdsNumber = selectedIds.size();
            int collectionItemsNumber = collection.size();
            SelectionFiltersConfig selectionFiltersConfig = widgetConfig.getSelectionFiltersConfig();
            if (selectedIdsNumber == collectionItemsNumber || (selectedIdsNumber != collectionItemsNumber
                    && selectionFiltersConfig != null)) {
                FilterBuilder.prepareSelectionFilters(selectionFiltersConfig, null, filters);
                collection = collectionsService.findCollection(collectionName, sortOrder, filters);
                objects = generateIdRepresentationMapFromCollection(widgetConfig, collection);
            } else {
                objects = generateIdRepresentationMapFromCollectionAndIds(widgetConfig, collection, selectedIds);
            }
            SingleChoiceConfig singleChoiceConfig = widgetConfig.getSingleChoice();
            boolean singleChoiceFromConfig = singleChoiceConfig == null ? false : singleChoiceConfig.isSingleChoice();
            boolean singleChoice = isSingleChoice(context, singleChoiceFromConfig);
            state.setSingleChoice(singleChoice);
            state.setListValues(objects);
        } else {
            SingleChoiceConfig singleChoiceConfig = widgetConfig.getSingleChoice();
            boolean singleChoiceFromConfig = singleChoiceConfig == null ? false : singleChoiceConfig.isSingleChoice();
            state.setSingleChoice(singleChoiceFromConfig);
        }
        return state;
    }

    private LinkedHashMap<Id, String> generateIdRepresentationMapFromCollection(SuggestBoxConfig widgetConfig,
                                                                                IdentifiableObjectCollection collection) {
        LinkedHashMap<Id, String> objects = new LinkedHashMap<Id, String>();
        SelectionPatternConfig selectionPatternConfig = widgetConfig.getSelectionPatternConfig();
        Matcher matcher = formatHandler.pattern.matcher(selectionPatternConfig.getValue());
        FormattingConfig formattingConfig = widgetConfig.getFormattingConfig();
        for (IdentifiableObject domainObject : collection) {
            objects.put(domainObject.getId(), formatHandler.format(domainObject, matcher, formattingConfig));
        }
        return objects;
    }

    private LinkedHashMap<Id, String> generateIdRepresentationMapFromCollectionAndIds(SuggestBoxConfig widgetConfig,
                                                                                      IdentifiableObjectCollection collection,
                                                                                      List<Id> selectedIds) {
        LinkedHashMap<Id, String> objects = new LinkedHashMap<Id, String>();
        SelectionPatternConfig selectionPatternConfig = widgetConfig.getSelectionPatternConfig();
        Matcher matcher = formatHandler.pattern.matcher(selectionPatternConfig.getValue());
        FormattingConfig formattingConfig = widgetConfig.getFormattingConfig();
        for (IdentifiableObject domainObject : collection) {
            Id id = domainObject.getId();
            objects.put(id, formatHandler.format(domainObject, matcher, formattingConfig));
            selectedIds.remove(id);
        }
        for (Id selectedId : selectedIds) {
            objects.put(selectedId, WidgetConstants.REPRESENTATION_STUB);
        }
        return objects;
    }

    public SuggestionList obtainSuggestions(Dto inputParams) {
        SuggestionRequest suggestionRequest = (SuggestionRequest) inputParams;
        List<Filter> filters = new ArrayList<>();

        if (!suggestionRequest.getExcludeIds().isEmpty()) {
            filters.add(FilterBuilder.prepareFilter(suggestionRequest.getExcludeIds(), FilterBuilder.EXCLUDED_IDS_FILTER));
        }

        filters.add(prepareInputTextFilter(suggestionRequest.getText(), suggestionRequest.getInputTextFilterName()));
        DefaultSortCriteriaConfig sortCriteriaConfig = suggestionRequest.getDefaultSortCriteriaConfig();
        SortOrder sortOrder = SortOrderBuilder.getSimpleSortOrder(sortCriteriaConfig);
        IdentifiableObjectCollection collection = collectionsService.findCollection(suggestionRequest.getCollectionName(),
                sortOrder, filters);
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
        SuggestionList suggestionList = new SuggestionList();
        suggestionList.setSuggestions(suggestionItems);
        return suggestionList;
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
