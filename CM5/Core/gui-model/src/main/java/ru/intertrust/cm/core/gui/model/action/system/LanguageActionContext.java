package ru.intertrust.cm.core.gui.model.action.system;

import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.model.action.ActionContext;

/**
 * @author Lesia Puhova
 *         Date: 04.03.2015
 *         Time: 13:20
 */
public class LanguageActionContext extends ActionContext {

    public static final String COMPONENT_NAME = "lang.action";

    private String locale;

    public LanguageActionContext() {
    }

    public LanguageActionContext(ActionConfig actionConfig) {
        super(actionConfig);
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }
}
