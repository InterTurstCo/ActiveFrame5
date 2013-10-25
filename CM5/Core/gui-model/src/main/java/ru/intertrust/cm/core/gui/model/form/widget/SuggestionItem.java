package ru.intertrust.cm.core.gui.model.form.widget;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: andrey
 * Date: 25.10.13
 * Time: 15:37
 * To change this template use File | Settings | File Templates.
 */
public class SuggestionItem implements Serializable {

    private Long id;
    private String suggestionText;

    public SuggestionItem(Long id, String suggestionText) {
        this.id = id;
        this.suggestionText = suggestionText;
    }

    public SuggestionItem() {
    }

    public Long getId() {
        return id;
    }

    public String getSuggestionText() {
        return suggestionText;
    }
}
