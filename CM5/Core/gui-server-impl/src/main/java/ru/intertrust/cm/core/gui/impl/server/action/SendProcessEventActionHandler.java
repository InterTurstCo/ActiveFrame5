package ru.intertrust.cm.core.gui.impl.server.action;

import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.SendProcessEventActionContext;

/**
 * @author Denis Mitavskiy
 *         Date: 23.10.13
 *         Time: 15:19
 */
@ComponentName("send.process.event.action")
public class SendProcessEventActionHandler extends ActionHandler<ActionContext, ActionData> {

    @Override
    public ActionData executeAction(ActionContext context) {
        return null;
    }

    @Override
    public ActionContext getActionContext(final ActionConfig actionConfig) {
        return new SendProcessEventActionContext(actionConfig);
    }
}
