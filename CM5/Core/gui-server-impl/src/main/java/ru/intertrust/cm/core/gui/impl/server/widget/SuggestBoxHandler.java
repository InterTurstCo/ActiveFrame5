package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionPatternConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SingleChoiceConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SuggestBoxConfig;
import ru.intertrust.cm.core.config.gui.navigation.DefaultSortCriteriaConfig;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.impl.server.util.FilterBuilder;
import ru.intertrust.cm.core.gui.impl.server.util.SortOrderBuilder;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.SuggestBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.SuggestionItem;
import ru.intertrust.cm.core.gui.model.form.widget.SuggestionList;
import ru.intertrust.cm.core.gui.model.form.widget.SuggestionRequest;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        IdentifiableObjectCollection domainObjects = null;

        if (!selectedIds.isEmpty()) {
            String collectionName = widgetConfig.getCollectionRefConfig().getName();
            List<Filter> filters = new ArrayList<Filter>();
            Set<Id> idsIncluded = new HashSet<Id>(selectedIds);
            Filter idsIncludedFilter = FilterBuilder.prepareFilter(idsIncluded, "idsIncluded");
            filters.add(idsIncludedFilter);
            domainObjects = collectionsService.findCollection(collectionName, null, filters);
        }
        LinkedHashMap<Id, String> objects = new LinkedHashMap<Id, String>();
        if (domainObjects != null) {
        SelectionPatternConfig selectionPatternConfig = widgetConfig.getSelectionPatternConfig();
        Pattern pattern = createDefaultRegexPattern();
        Matcher matcher = pattern.matcher(selectionPatternConfig.getValue());
        for (IdentifiableObject domainObject : domainObjects) {
            objects.put(domainObject.getId(), format(domainObject, matcher));
        }

        }
        SingleChoiceConfig singleChoiceConfig = widgetConfig.getSingleChoice();
        boolean singleChoiceFromConfig = singleChoiceConfig == null ? false : singleChoiceConfig.isSingleChoice();
        boolean singleChoice = isSingleChoice(context, singleChoiceFromConfig) ;
        state.setSingleChoice(singleChoice);
        state.setListValues(objects);
        return state;
    }

    public SuggestionList obtainSuggestions(Dto inputParams) {
        SuggestionRequest suggestionRequest = (SuggestionRequest) inputParams;
        List<Filter> filters = new ArrayList<>();

        if (!suggestionRequest.getExcludeIds().isEmpty()) {
            filters.add(FilterBuilder.prepareFilter(suggestionRequest.getExcludeIds(), "idsExcluded"));
        }
        filters.add(prepareInputTextFilter(suggestionRequest.getText(), suggestionRequest.getInputTextFilterName()));
        DefaultSortCriteriaConfig sortCriteriaConfig = suggestionRequest.getDefaultSortCriteriaConfig();
        SortOrder sortOrder = SortOrderBuilder.getDefaultSortOrder(sortCriteriaConfig);
        IdentifiableObjectCollection collection = collectionsService.findCollection(suggestionRequest.getCollectionName(),
                sortOrder, filters);
        Pattern pattern = createDefaultRegexPattern();
        Matcher dropDownMatcher = pattern.matcher(suggestionRequest.getDropdownPattern());
        Matcher selectionMatcher = pattern.matcher(suggestionRequest.getSelectionPattern());

        ArrayList<SuggestionItem> suggestionItems = new ArrayList<>();

        for (IdentifiableObject identifiableObject : collection) {
            SuggestionItem suggestionItem = new SuggestionItem(identifiableObject.getId(),
                    format(identifiableObject, dropDownMatcher), format(identifiableObject, selectionMatcher));
            suggestionItems.add(suggestionItem);
        }
        SuggestionList suggestionList = new SuggestionList();
        suggestionList.setSuggestions(suggestionItems);
        return suggestionList;
    }

    private Filter prepareInputTextFilter(String text, String inputTextFilterName) {
        Filter textFilter = new Filter();
        textFilter.setFilter(inputTextFilterName);
        if (text.equals("*")){
            textFilter.addCriterion(0, new StringValue("%"));
        }
        else{
            textFilter.addCriterion(0, new StringValue(text + "%"));
        }
        return textFilter;
    }

    private Pattern createDefaultRegexPattern() {
        return Pattern.compile("\\{\\w+\\}");
    }


}
