package ru.intertrust.cm.core.gui.model.action;

import ru.intertrust.cm.core.config.gui.action.ActionConfig;

/**
 * @author Sergey.Okolot
 */
public class ToggleActionContext extends ActionContext {

    private boolean pushed;

    public ToggleActionContext() {
    }

    public ToggleActionContext(ActionConfig actionConfig) {
        super(actionConfig);
    }

    public boolean isPushed() {
        return pushed;
    }

    public void setPushed(boolean pushed) {
        this.pushed = pushed;
    }
}
