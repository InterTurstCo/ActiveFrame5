package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.model.gui.form.widget.SuggestBoxConfig;
import ru.intertrust.cm.core.gui.api.server.widget.LinkEditingWidgetHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.*;

import java.util.ArrayList;
import java.util.List;
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
        SuggestBoxConfig widgetConfig = context.getWidgetConfig();
        SuggestBoxState result = new SuggestBoxState();
        result.setSuggestBoxConfig(widgetConfig);
        return result;
    }

    public SuggestionList obtainSuggestions(Dto inputParams) {
        SuggestionRequest suggestionRequest = (SuggestionRequest) inputParams;
        List<Filter> filters = new ArrayList<>();

        // TODO uncomment when multicriterion filtering will be resolvable
        // filters.add(prepareExcludeIdsFilter(suggestionRequest.getExcludeIds()));

        filters.add(prepareInputTextFilter(suggestionRequest.getText()));

        IdentifiableObjectCollection collection = collectionsService.findCollection(suggestionRequest.getCollectionName(), null, filters);
        Pattern pattern = Pattern.compile("\\{\\w+\\}");
        Matcher matcher = pattern.matcher(suggestionRequest.getPattern());

        ArrayList<SuggestionItem> suggestionItems = new ArrayList<>();

        for (IdentifiableObject identifiableObject : collection) {
            SuggestionItem suggestionItem = new SuggestionItem(Long.valueOf(identifiableObject.getId().toStringRepresentation()),
                    format(identifiableObject, matcher));
            suggestionItems.add(suggestionItem);
        }
        SuggestionList suggestionList = new SuggestionList();
        suggestionList.setSuggestions(suggestionItems);
        return suggestionList;
    }

    private Filter prepareInputTextFilter(String text) {
        Filter textFilter = new Filter();
        textFilter.setFilter("byText");
        textFilter.addCriterion(0, new StringValue(text+"%"));
        return textFilter;
    }

    private Filter prepareExcludeIdsFilter(ArrayList<Long> excludeIds) {
        Filter exludeIdsFilter = new Filter();

        List<Value> excludeIdsCriterion = new ArrayList<>();
        for (Long id : excludeIds) {
            excludeIdsCriterion.add(new LongValue(id));
        }
        exludeIdsFilter.addMultiCriterion(0, excludeIdsCriterion);
        exludeIdsFilter.setFilter("idsExcluded");
        return exludeIdsFilter;
    }


}
