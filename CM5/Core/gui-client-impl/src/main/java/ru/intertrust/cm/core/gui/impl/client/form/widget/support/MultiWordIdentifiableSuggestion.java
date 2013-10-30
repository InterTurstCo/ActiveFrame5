package ru.intertrust.cm.core.gui.impl.client.form.widget.support;

import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Created with IntelliJ IDEA.
 * User: andrey
 * Date: 28.10.13
 * Time: 16:59
 * To change this template use File | Settings | File Templates.
 */
public class MultiWordIdentifiableSuggestion extends MultiWordSuggestOracle.MultiWordSuggestion {

    private Id id;

    public MultiWordIdentifiableSuggestion(Id id, String replacementString, String displayString) {
        super(replacementString, displayString);
        this.id = id;
    }

    public MultiWordIdentifiableSuggestion() {
    }

    public Id getId() {
        return id;
    }
}
