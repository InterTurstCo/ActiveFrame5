package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.model.gui.form.widget.SuggestBoxConfig;

/**
 * Created with IntelliJ IDEA.
 * User: andrey
 * Date: 22.10.13
 * Time: 16:22
 * To change this template use File | Settings | File Templates.
 */
public class SuggestBoxState extends ListBoxState {

    private SuggestBoxConfig suggestBoxConfig;

    @Override
    public Value toValue() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public SuggestBoxConfig getSuggestBoxConfig() {
        return suggestBoxConfig;
    }

    public void setSuggestBoxConfig(SuggestBoxConfig suggestBoxConfig) {
        this.suggestBoxConfig = suggestBoxConfig;
    }
}
