package ru.intertrust.cm.core.gui.impl.server.action;

import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ActionData;

/**
 * @author Sergey.Okolot
 *         Created on 11.06.2014 9:08.
 */
public class FakeActionHandler extends ActionHandler<ActionContext, ActionData> {
    @Override
    public ActionData executeAction(ActionContext context) {
        return null;
    }

    @Override
    public ActionContext getActionContext(final ActionConfig actionConfig) {
        return new ActionContext(actionConfig);
    }
}
