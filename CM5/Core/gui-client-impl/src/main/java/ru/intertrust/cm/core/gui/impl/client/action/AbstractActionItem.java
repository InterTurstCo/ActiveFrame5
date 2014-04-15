package ru.intertrust.cm.core.gui.impl.client.action;

import ru.intertrust.cm.core.config.gui.ActionConfig;
import ru.intertrust.cm.core.gui.model.action.ActionContext;

/**
 * @author Sergey.Okolot
 *         Created on 07.04.2014 18:28.
 */
public abstract class AbstractActionItem implements ActionItem {

    private ActionConfig config;

    public void setActionContext(final ActionContext ctx) {
        config = ctx.getActionConfig();
    }
}
