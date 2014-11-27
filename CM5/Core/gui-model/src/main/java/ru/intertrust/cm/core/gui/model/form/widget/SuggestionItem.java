package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Created with IntelliJ IDEA.
 * User: andrey
 * Date: 25.10.13
 * Time: 15:37
 * To change this template use File | Settings | File Templates.
 */
public class SuggestionItem implements Dto {

    private Id id;
    private String displayText;
    private String replacementText;

    public SuggestionItem(Id id, String displayText, String replacementText) {
        this.id = id;
        this.displayText = displayText;
        this.replacementText = replacementText;
    }

    public SuggestionItem() {
    }

    public Id getId() {
        return id;
    }

    public String getDisplayText() {
        return displayText;
    }

    public String getReplacementText() {
        return replacementText;
    }

}
