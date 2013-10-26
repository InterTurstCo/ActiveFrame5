package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.model.gui.form.widget.SuggestBoxConfig;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: andrey
 * Date: 22.10.13
 * Time: 16:22
 * To change this template use File | Settings | File Templates.
 */
public class SuggestBoxState extends LinkEditingWidgetState {

    private SuggestBoxConfig suggestBoxConfig;

    @Override
    public ArrayList<Id> getIds() {
        return null;
    }

    public SuggestBoxConfig getSuggestBoxConfig() {
        return suggestBoxConfig;
    }

    public void setSuggestBoxConfig(SuggestBoxConfig suggestBoxConfig) {
        this.suggestBoxConfig = suggestBoxConfig;
    }
}
