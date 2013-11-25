package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionPatternConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SuggestBoxConfig;
import ru.intertrust.cm.core.gui.api.server.widget.LinkEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
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
public class SuggestBoxHandler extends LinkEditingWidgetHandler {
    @Autowired
    CrudService crudService;
    @Autowired
    CollectionsService collectionsService;

    @Override
    public SuggestBoxState getInitialState(WidgetContext context) {
        SuggestBoxState state = new SuggestBoxState();
        SuggestBoxConfig widgetConfig = context.getWidgetConfig();
        state.setSuggestBoxConfig(widgetConfig);
        ArrayList<Id> selectedIds = context.getObjectIds();
        List<DomainObject> domainObjects;
        if (!selectedIds.isEmpty()) {
            domainObjects = crudService.find(selectedIds);
        } else {
            domainObjects = Collections.emptyList();
        }
        LinkedHashMap<Id, String> objects = new LinkedHashMap<Id, String>();
        SelectionPatternConfig selectionPatternConfig = widgetConfig.getSelectionPatternConfig();
        Pattern pattern = createDefaultRegexPattern();
        Matcher matcher = pattern.matcher(selectionPatternConfig.getValue());
        for (DomainObject domainObject : domainObjects) {
            objects.put(domainObject.getId(), format(domainObject, matcher));
        }
        state.setObjects(objects);
        return state;
    }

    public SuggestionList obtainSuggestions(Dto inputParams) {
        SuggestionRequest suggestionRequest = (SuggestionRequest) inputParams;
        List<Filter> filters = new ArrayList<>();

        if (!suggestionRequest.getExcludeIds().isEmpty()) {
            filters.add(prepareExcludeIdsFilter(suggestionRequest.getExcludeIds(), suggestionRequest.getIdsExclusionFilterName()));
        }
        filters.add(prepareInputTextFilter(suggestionRequest.getText(), suggestionRequest.getInputTextFilterName()));

        IdentifiableObjectCollection collection = collectionsService.findCollection(suggestionRequest.getCollectionName(), null, filters);
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
        textFilter.addCriterion(0, new StringValue(text + "%"));
        return textFilter;
    }

    private Filter prepareExcludeIdsFilter(Set<Id> excludeIds, String idsExclusionFilterName) {
        Filter exludeIdsFilter = new Filter();

        List<Value> excludeIdsCriterion = new ArrayList<>();
        for (Id id : excludeIds) {
            excludeIdsCriterion.add(new ReferenceValue(id));
        }
        exludeIdsFilter.addMultiCriterion(0, excludeIdsCriterion);
        exludeIdsFilter.setFilter(idsExclusionFilterName);
        return exludeIdsFilter;
    }

    private Pattern createDefaultRegexPattern() {
        return Pattern.compile("\\{\\w+\\}");
    }


}
