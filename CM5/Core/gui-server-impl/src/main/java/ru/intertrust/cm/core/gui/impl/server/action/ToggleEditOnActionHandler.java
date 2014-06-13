package ru.intertrust.cm.core.gui.impl.server.action;

import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ActionData;

/**
 * @author Sergey.Okolot
 *         Created on 12.06.2014 16:20.
 */
@ComponentName("toggle.edit.on.action")
public class ToggleEditOnActionHandler extends ActionHandler<ActionContext, ActionData> {
    @Override
    public ActionData executeAction(ActionContext context) {
        return null;
    }

    @Override
    public ActionContext getActionContext() {
        return new ActionContext();
    }

    @Override
    public HandlerStatusData getCheckStatusData() {
        return new FormPluginHandlerStatusData();
    }

    @Override
    public Status getHandlerStatus(String conditionExpression, HandlerStatusData condition) {
        conditionExpression = conditionExpression.replaceAll(TOGGLE_EDIT_ATTR, TOGGLE_EDIT_KEY);
        final boolean result = evaluateExpression(conditionExpression, condition);
        return result ? Status.SUCCESSFUL : Status.SKIPPED;
    }
}
