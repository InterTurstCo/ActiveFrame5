package ru.intertrust.cm.core.gui.impl.server.action;

import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ActionData;

/**
 * @author Denis Mitavskiy
 *         Date: 23.10.13
 *         Time: 15:19
 */
@ComponentName("send.process.event.action")
public class SendProcessEventAction extends ActionHandler {
    @Override
    public <T extends ActionData> T executeAction(ActionContext context) {
        return null;
    }
}
