package ru.intertrust.cm.core.gui.model.action.system;

import ru.intertrust.cm.core.config.gui.action.AbstractActionConfig;
import ru.intertrust.cm.core.gui.model.action.ActionContext;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 18.07.2015
 *         Time: 22:20
 */
public class NavigationPanelStateActionContext extends ActionContext {
    public static final String COMPONENT_NAME = "navigation.panel.state.action";
    private boolean pinned;

    public NavigationPanelStateActionContext() {
    }

    public NavigationPanelStateActionContext(AbstractActionConfig actionConfig) {
        super(actionConfig);
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }
}
