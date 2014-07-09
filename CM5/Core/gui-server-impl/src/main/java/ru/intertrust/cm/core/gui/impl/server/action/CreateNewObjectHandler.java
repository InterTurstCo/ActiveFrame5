package ru.intertrust.cm.core.gui.impl.server.action;

import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ActionData;

/**
 * @author Sergey.Okolot
 *         Created on 11.06.2014 9:18.
 */
@ComponentName("create.new.object.action")
public class CreateNewObjectHandler extends ActionHandler {
    private static final String TOGGLE_EDIT_KEY = "toggleEdit";

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
    public Status getHandlerStatus(String conditionExpression, final HandlerStatusData condition) {
        conditionExpression = conditionExpression.replaceAll(TOGGLE_EDIT_ATTR, TOGGLE_EDIT_KEY);
        final boolean result = (Boolean) evaluateExpression(conditionExpression, condition);
        return result ? Status.APPLY : Status.SKIP;
    }
}
