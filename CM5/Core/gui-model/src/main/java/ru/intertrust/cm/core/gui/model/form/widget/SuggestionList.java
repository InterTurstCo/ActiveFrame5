package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: andrey
 * Date: 24.10.13
 * Time: 14:23
 * To change this template use File | Settings | File Templates.
 */
public class SuggestionList implements Dto {
    private ArrayList<SuggestionItem> suggestions;
    private boolean responseForMoreItems;

    public ArrayList<SuggestionItem> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(ArrayList<SuggestionItem> suggestions) {
        this.suggestions = suggestions;
    }

    public boolean isResponseForMoreItems() {
        return responseForMoreItems;
    }

    public void setResponseForMoreItems(boolean responseForMoreItems) {
        this.responseForMoreItems = responseForMoreItems;
    }
}
