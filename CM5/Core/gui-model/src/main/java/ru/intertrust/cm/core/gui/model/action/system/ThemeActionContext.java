package ru.intertrust.cm.core.gui.model.action.system;

import ru.intertrust.cm.core.config.gui.action.AbstractActionConfig;
import ru.intertrust.cm.core.gui.model.action.ActionContext;

/**
 * @author Sergey.Okolot
 *         Created on 07.08.2014 17:03.
 */
public class ThemeActionContext extends ActionContext {

    public static final String COMPONENT_NAME = "theme.action";

    private String themeName;

    public ThemeActionContext() {
    }

    public ThemeActionContext(AbstractActionConfig actionConfig) {
        super(actionConfig);
    }

    public String getThemeName() {
        return themeName;
    }

    public void setThemeName(String themeName) {
        this.themeName = themeName;
    }
}
