package ru.intertrust.cm.core.gui.impl.server.action;

import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ActionData;

/**
 * @author Sergey.Okolot
 *         Created on 23.09.2014 11:50.
 */
@ComponentName("simple.action")
public class SimpleActionHandler extends ActionHandler<ActionContext, ActionData> {
    @Override
    public ActionData executeAction(ActionContext context) {
        final ActionData result = new ActionData();
        final ActionConfig config = context.getActionConfig();
        if (config.getAfterConfig() != null) {
            result.setOnErrorMessage(config.getAfterConfig().getErrorMessageConfig() == null
                    ? null
                    : config.getAfterConfig().getErrorMessageConfig().getText());
            result.setOnSuccessMessage(config.getAfterConfig().getMessageConfig() == null
                    ? null
                    : config.getAfterConfig().getMessageConfig().getText());
        }
        return result;
    }

    @Override
    public ActionContext getActionContext(ActionConfig actionConfig) {
        return new ActionContext(actionConfig);
    }
}
