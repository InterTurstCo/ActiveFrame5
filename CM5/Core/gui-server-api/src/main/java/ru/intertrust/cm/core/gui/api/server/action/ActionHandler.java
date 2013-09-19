package ru.intertrust.cm.core.gui.api.server.action;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ActionData;

/**
 * @author Denis Mitavskiy
 *         Date: 19.09.13
 *         Time: 13:14
 */
public abstract class ActionHandler implements ComponentHandler {
    public <T extends ActionData> T executeAction(Dto context) {
        return executeAction((ActionContext) context);
    }

    public abstract <T extends ActionData> T executeAction(ActionContext context);
}
