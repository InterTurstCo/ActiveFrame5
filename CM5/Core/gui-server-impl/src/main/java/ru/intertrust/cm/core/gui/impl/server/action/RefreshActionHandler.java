package ru.intertrust.cm.core.gui.impl.server.action;

import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ActionData;

/**
 * @author Sergey.Okolot
 *         Created on 09.06.2014 13:42.
 */
@ComponentName("refresh.action")
public class RefreshActionHandler extends ActionHandler<ActionContext, ActionData> {

    @Override
    public ActionData executeAction(ActionContext context) {
        return null;
    }

    @Override
    public ActionContext getActionContext() {
        return new ActionContext();
    }
}
